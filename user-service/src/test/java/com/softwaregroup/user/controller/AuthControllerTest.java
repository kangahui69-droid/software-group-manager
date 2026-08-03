package com.softwaregroup.user.controller;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.model.dto.LoginRequest;
import com.softwaregroup.user.model.dto.RegisterRequest;
import com.softwaregroup.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AuthController 单元测试（简化版，不加载Spring上下文）
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Result.ok(Map.of("token", "jwt-token", "user", Map.of("id", 1, "username", "admin"))));

        Result result = authService.login(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void login_withInvalidCredentials_shouldReturnError() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Result.error(400, "密码错误"));

        Result result = authService.login(request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertEquals("密码错误", result.getMessage());
    }

    @Test
    void register_withValidData_shouldReturnSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole("MEMBER");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(Result.ok(Map.of("token", "jwt-token", "user", Map.of("id", 100, "username", "newuser"))));

        Result result = authService.register(request);

        assertTrue(result.isSuccess());
    }

    @Test
    void health_shouldReturnServiceStatus() {
        Result result = authController.health();

        assertTrue(result.isSuccess());
        assertEquals("UP", ((Map)result.getData()).get("status"));
        assertEquals("user-service", ((Map)result.getData()).get("service"));
    }

    @Test
    void getProfile_shouldDelegateToService() {
        when(authService.getUserDetail(1))
                .thenReturn(Result.ok(Map.of("id", 1, "username", "admin")));

        Result result = authController.getProfile(1);

        assertTrue(result.isSuccess());
    }
}
