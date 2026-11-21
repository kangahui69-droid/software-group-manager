package com.softwaregroup.common;

import com.softwaregroup.common.util.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET = "test-only-new-signing-key-32-bytes-or-longer";
    private static final String OLD_SECRET = "software-group-jwt-secret-key-2024-very-long-and-secure";
    private final JwtService jwt = new JwtService(SECRET, "key-2", "software-group", "software-group-api");

    @Test
    void signsAndValidatesIdentity() {
        String token = jwt.generateToken(1, "admin", "ADMIN");

        assertThat(jwt.validateToken(token)).isTrue();
        assertThat(jwt.getUserId(token)).isEqualTo(1);
        assertThat(jwt.getUsername(token)).isEqualTo("admin");
        assertThat(jwt.getRole(token)).isEqualTo("ADMIN");
        assertThat(jwt.parseToken(token).getIssuer()).isEqualTo("software-group");
        assertThat(jwt.parseToken(token).getAudience()).contains("software-group-api");
    }

    @Test
    void refusesMissingWeakAndLegacySecrets() {
        assertThatThrownBy(() -> new JwtService(null, "v1", "issuer", "audience"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtService("short", "v1", "issuer", "audience"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtService(OLD_SECRET, "v1", "issuer", "audience"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void serviceStartupRequiresConfiguredSecret() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(JwtService.class, JwtConsumer.class);
        runner.run(context -> assertThat(context).hasFailed());
        runner.withPropertyValues("JWT_SECRET=" + OLD_SECRET)
                .run(context -> assertThat(context).hasFailed());
        runner.withPropertyValues("JWT_SECRET=" + SECRET)
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void rejectsTokensSignedWithOldKey() {
        String token = signedWith(OLD_SECRET, "key-2", "software-group", "software-group-api");
        assertThat(jwt.validateToken(token)).isFalse();
    }

    @Test
    void requiresExpectedIssuerAudienceAndKeyId() {
        assertThat(jwt.validateToken(signedWith(SECRET, "key-2", "other", "software-group-api"))).isFalse();
        assertThat(jwt.validateToken(signedWith(SECRET, "key-2", "software-group", "other"))).isFalse();
        assertThat(jwt.validateToken(signedWith(SECRET, "old-key", "software-group", "software-group-api"))).isFalse();
    }

    @Test
    void detectsExpiredTokenAndInvalidHeader() {
        assertThat(jwt.isTokenExpired(jwt.generateToken(1, "admin", "ADMIN", -1000L))).isTrue();
        assertThat(jwt.extractTokenFromHeader("Bearer token")).isEqualTo("token");
        assertThat(jwt.extractTokenFromHeader("Basic token")).isNull();
        assertThat(jwt.extractTokenFromHeader(null)).isNull();
    }

    private String signedWith(String secret, String keyId, String issuer, String audience) {
        Date now = new Date();
        return Jwts.builder().header().keyId(keyId).and()
                .subject("1").claim("username", "admin").claim("role", "ADMIN")
                .issuer(issuer).audience().add(audience).and()
                .issuedAt(now).expiration(new Date(now.getTime() + 60000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    @Configuration(proxyBeanMethods = false)
    static class JwtConsumer {
        @Bean
        Object jwtDependentComponent(JwtService jwtService) {
            return new Object();
        }
    }
}
