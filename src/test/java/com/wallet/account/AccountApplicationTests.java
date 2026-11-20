package com.wallet.account;

import com.wallet.account.domain.model.Account;
import com.wallet.account.domain.model.Caller;
import com.wallet.account.domain.model.UserType;
import com.wallet.account.domain.model.exception.InsufficientBalanceException;
import com.wallet.account.domain.ports.in.CreateAccountUseCase;
import com.wallet.account.domain.ports.in.DepositUseCase;
import com.wallet.account.domain.ports.in.GetBalanceUseCase;
import com.wallet.account.domain.ports.in.WithdrawUseCase;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    CreateAccountUseCase createAccount;
    @Autowired
    DepositUseCase deposit;
    @Autowired
    WithdrawUseCase withdraw;
    @Autowired
    GetBalanceUseCase balance;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    JdbcTemplate jdbc;

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

    @Test
    void concurrentWithdrawalsNeverOverdraw() throws Exception {
        Caller owner = new Caller(201, UserType.CUSTOMER);
        String accountNmbr = createAccount.createAccount(owner, null, null).accountNmbr();
        deposit.deposit(owner, accountNmbr, new BigDecimal("100"));

        // 10 parallel withdrawals of 15 against a balance of 100: only 6 can succeed
        List<Callable<Boolean>> withdrawals = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            withdrawals.add(() -> {
                try {
                    withdraw.withdraw(owner, accountNmbr, new BigDecimal("15"));
                    return true;
                } catch (InsufficientBalanceException ex) {
                    return false;
                }
            });
        }
        ExecutorService pool = Executors.newFixedThreadPool(10);
        int succeeded = 0;
        try {
            for (Future<Boolean> result : pool.invokeAll(withdrawals)) {
                if (result.get()) {
                    succeeded++;
                }
            }
        } finally {
            pool.shutdown();
        }

        assertThat(succeeded).isEqualTo(6);
        assertThat(balance.getAccount(owner, accountNmbr).currentBalance()).isEqualByComparingTo("10");
    }

    @Test
    void auditEventsAreSentAsJsonOnlyForCommittedOperations() {
        amqpAdmin.purgeQueue("audit_queue", false);
        Caller owner = new Caller(301, UserType.CUSTOMER);
        Account account = createAccount.createAccount(owner, null, "EUR");
        deposit.deposit(owner, account.accountNmbr(), new BigDecimal("5.25"));
        assertThatThrownBy(() -> withdraw.withdraw(owner, account.accountNmbr(), new BigDecimal("999")))
                .isInstanceOf(InsufficientBalanceException.class);

        String created = receiveAuditMessage();
        String deposited = receiveAuditMessage();

        assertThat(created).contains("\"type\":\"ACCOUNT_CREATED\"", "\"accountNmbr\":\"" + account.accountNmbr() + "\"",
                "\"accountUserId\":301", "\"eventId\":");
        assertThat(deposited).contains("\"type\":\"DEPOSIT\"", "\"amount\":5.25", "\"balance\":5.25",
                "\"performedBy\":301");
        // The failed withdrawal was rolled back, so nothing else was published
        assertThat(rabbitTemplate.receive("audit_queue", 1000)).isNull();
    }

    @Test
    void adminsListEveryAccountAndCanOpenOneForAnotherUser() throws Exception {
        String admin = token(1, "ADMIN");
        mvc.perform(post("/api/accounts/new").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":401,\"currency\":\"EUR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(401))
                .andExpect(jsonPath("$.currency").value("EUR"));

        mvc.perform(post("/api/accounts/new").header("Authorization", token(402, "CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"userId\":401}"))
                .andExpect(status().isForbidden());

        long total = jdbc.queryForObject("select count(*) from account", Long.class);
        mvc.perform(get("/api/accounts?page=0&pageSize=1").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(total))
                .andExpect(jsonPath("$.accounts.length()").value(1));
    }

    @Test
    void databaseRejectsNegativeBalancesAndUnknownStatuses() {
        assertThatThrownBy(() -> jdbc.update("insert into account values ('neg', -1, 'USD', 1, 1, now(), now())"))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("insert into account values ('bad', 0, 'USD', 7, 1, now(), now())"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private String receiveAuditMessage() {
        Message message = rabbitTemplate.receive("audit_queue", 5000);
        assertThat(message).as("audit message").isNotNull();
        return new String(message.getBody(), StandardCharsets.UTF_8);
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
