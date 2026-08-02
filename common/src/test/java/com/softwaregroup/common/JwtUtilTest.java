package com.softwaregroup.common;

import com.softwaregroup.common.util.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil 测试
 */
class JwtUtilTest {

    @Test
    void shouldGenerateValidToken() {
        // given
        int userId = 1;
        String username = "admin";
        String role = "ADMIN";

        // when
        String token = JwtUtil.generateToken(userId, username, role);

        // then
        assertThat(token).isNotBlank();
        assertThat(JwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void shouldParseTokenCorrectly() {
        // given
        int userId = 1;
        String username = "admin";
        String role = "ADMIN";
        String token = JwtUtil.generateToken(userId, username, role);

        // when
        int parsedUserId = JwtUtil.getUserId(token);
        String parsedUsername = JwtUtil.getUsername(token);
        String parsedRole = JwtUtil.getRole(token);

        // then
        assertThat(parsedUserId).isEqualTo(userId);
        assertThat(parsedUsername).isEqualTo(username);
        assertThat(parsedRole).isEqualTo(role);
    }

    @Test
    void shouldExtractTokenFromHeader() {
        // given
        String token = "eyJhbGciOiJIUzI1NiJ9.test";
        String authHeader = "Bearer " + token;

        // when
        String extracted = JwtUtil.extractTokenFromHeader(authHeader);

        // then
        assertThat(extracted).isEqualTo(token);
    }

    @Test
    void shouldReturnNullForInvalidHeader() {
        // given
        String authHeader = "Basic dXNlcjpwYXNz";

        // when
        String extracted = JwtUtil.extractTokenFromHeader(authHeader);

        // then
        assertThat(extracted).isNull();
    }

    @Test
    void shouldReturnNullForNullHeader() {
        assertThat(JwtUtil.extractTokenFromHeader(null)).isNull();
    }

    @Test
    void shouldDetectExpiredToken() {
        // given - generate token with very short expiration
        String token = JwtUtil.generateToken(1, "admin", "ADMIN", -1000L); // already expired

        // then
        assertThat(JwtUtil.isTokenExpired(token)).isTrue();
    }
}
