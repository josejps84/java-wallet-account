package com.wallet.account;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end check of security and persistence against real PostgreSQL and RabbitMQ containers.
 * Skipped automatically when Docker is not available.
 */
@SpringBootTest(properties = "jwt.secret=" + AccountApplicationTests.SECRET)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AccountApplicationTests {

    static final String SECRET = "test-secret-test-secret-test-secret-1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-alpine");

    @Autowired
    MockMvc mvc;

    @Test
    void requestsWithoutOrWithInvalidTokenAreRejected() throws Exception {
        mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/accounts/new")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/accounts").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerLifecycleAndIsolation() throws Exception {
        String alice = token(101, "CUSTOMER");
        String bob = token(102, "CUSTOMER");

        String body = mvc.perform(post("/api/accounts/new").header("Authorization", alice))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(101))
                .andReturn().getResponse().getContentAsString();
        String accountNmbr = body.replaceAll(".*\"accountNmbr\":\"([^\"]+)\".*", "$1");

        mvc.perform(post("/api/accounts/deposit").header("Authorization", alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"" + accountNmbr + "\",\"amount\":10.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(10.5));

        mvc.perform(post("/api/accounts/withdraw").header("Authorization", alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"" + accountNmbr + "\",\"amount\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Saldo insuficiente"));

        // Bob can neither see nor move Alice's money
        mvc.perform(get("/api/accounts/balance/" + accountNmbr).header("Authorization", bob))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/accounts/withdraw").header("Authorization", bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"" + accountNmbr + "\",\"amount\":1}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/accounts").header("Authorization", bob))
                .andExpect(jsonPath("$.totalItems").value(0));

        // Customers can't edit; admins can
        String edit = "{\"accountNmbr\":\"" + accountNmbr + "\",\"status\":\"INACTIVE\"}";
        mvc.perform(put("/api/accounts/edit").header("Authorization", alice)
                        .contentType(MediaType.APPLICATION_JSON).content(edit))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/accounts/edit").header("Authorization", token(1, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(edit))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void invalidRequestsReturnProblemDetails() throws Exception {
        String alice = token(103, "CUSTOMER");
        mvc.perform(post("/api/accounts/deposit").header("Authorization", alice)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
        mvc.perform(get("/api/accounts?pageSize=1000").header("Authorization", alice))
                .andExpect(status().isBadRequest());
    }

    private static String token(long userId, String userType) {
        return "Bearer " + Jwts.builder()
                .subject("user" + userId + "@example.com")
                .claim("userId", userId)
                .claim("userType", userType)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
