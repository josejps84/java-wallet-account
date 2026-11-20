package com.wallet.account.infrastructure.security;

import com.wallet.account.domain.model.Caller;
import com.wallet.account.domain.model.UserType;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-1234";

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(SECRET);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesTheCaller() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("Bearer " + token(SECRET).compact(), chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getPrincipal()).isEqualTo(new Caller(7L, UserType.ADMIN));
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
    }

    @Test
    void requestWithoutTokenContinuesUnauthenticated() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run(null, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void invalidTokensAreRejected() throws Exception {
        String otherKey = "another-secret-another-secret-another-1234";
        String[] badTokens = {
                "garbage",
                token(otherKey).compact(),
                token(SECRET).expiration(new Date(System.currentTimeMillis() - 1000)).compact(),
                token(SECRET).claim("userType", "SUPERUSER").compact(),
                Jwts.builder().subject("x").signWith(key(SECRET)).compact(),
        };

        for (String bad : badTokens) {
            MockFilterChain chain = new MockFilterChain();
            MockHttpServletResponse response = run("Bearer " + bad, chain);

            assertThat(response.getStatus()).as(bad).isEqualTo(401);
            assertThat(chain.getRequest()).as(bad).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    private MockHttpServletResponse run(String authorization, MockFilterChain chain) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/accounts");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    /** userId is written as a small number, which JSON parsers read back as an Integer. */
    private static JwtBuilder token(String secret) {
        return Jwts.builder()
                .subject("admin@example.com")
                .claim("userId", 7)
                .claim("userType", "ADMIN")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key(secret));
    }

    private static javax.crypto.SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
