package com.softwaregroup.user.service;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.dao.MemberProfileDAO;
import com.softwaregroup.user.dao.UserDAO;
import com.softwaregroup.user.model.dto.LoginRequest;
import com.softwaregroup.user.model.dto.RegisterRequest;
import com.softwaregroup.user.model.entity.User;
import com.softwaregroup.user.util.DESUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    private AuthService authService;

    private DESUtil desUtil;

    @BeforeEach
    void setUp() {
        desUtil = new DESUtil("(^&%gasie_%^)"); // 测试用DES key
        authService = new AuthService(userDAO, memberProfileDAO, desUtil);
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        // 准备测试数据
        String username = "admin";
        String password = "admin123";
        String encryptedPassword = desUtil.encrypt(password);

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(username);
        mockUser.setPassword(encryptedPassword);
        mockUser.setRole("ADMIN");
        mockUser.setStatus(1);

        when(userDAO.findByUsername(username)).thenReturn(mockUser);
        when(userDAO.findByUsernameAndPassword(username, encryptedPassword)).thenReturn(mockUser);

        // 执行测试
        LoginRequest request = new LoginRequest(username, password);
        Result result = authService.login(request);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof java.util.Map);
    }

    @Test
    void login_withInvalidUsername_shouldReturnError() {
        when(userDAO.findByUsername("nonexistent")).thenReturn(null);

        LoginRequest request = new LoginRequest("nonexistent", "password");
        Result result = authService.login(request);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
    }

    @Test
    void login_withInvalidPassword_shouldReturnError() {
        String username = "admin";
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(username);
        mockUser.setPassword(desUtil.encrypt("correctpassword"));
        mockUser.setRole("ADMIN");
        mockUser.setStatus(1);

        when(userDAO.findByUsername(username)).thenReturn(mockUser);
        when(userDAO.findByUsernameAndPassword(eq(username), anyString())).thenReturn(null);

        LoginRequest request = new LoginRequest(username, "wrongpassword");
        Result result = authService.login(request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }

    @Test
    void login_withDisabledUser_shouldReturnError() {
        String username = "disabled";
        String password = "password";
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(username);
        mockUser.setPassword(desUtil.encrypt(password));
        mockUser.setRole("MEMBER");
        mockUser.setStatus(0); // 禁用状态

        when(userDAO.findByUsername(username)).thenReturn(mockUser);
        when(userDAO.findByUsernameAndPassword(username, desUtil.encrypt(password))).thenReturn(mockUser);

        LoginRequest request = new LoginRequest(username, password);
        Result result = authService.login(request);

        assertFalse(result.isSuccess());
        assertEquals(403, result.getCode());
    }

    @Test
    void login_withEmptyCredentials_shouldReturnError() {
        LoginRequest request1 = new LoginRequest("", "password");
        Result result1 = authService.login(request1);
        assertFalse(result1.isSuccess());
        assertEquals(400, result1.getCode());

        LoginRequest request2 = new LoginRequest("admin", "");
        Result result2 = authService.login(request2);
        assertFalse(result2.isSuccess());
        assertEquals(400, result2.getCode());
    }

    @Test
    void register_withValidData_shouldReturnSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole("MEMBER");
        request.setEmail("newuser@example.com");

        when(userDAO.existsByUsername("newuser")).thenReturn(false);
        when(userDAO.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userDAO.insert(any(User.class))).thenReturn(100);

        Result result = authService.register(request);

        assertTrue(result.isSuccess());
        verify(userDAO).insert(any(User.class));
    }

    @Test
    void register_withExistingUsername_shouldReturnError() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        request.setRole("MEMBER");

        when(userDAO.existsByUsername("existinguser")).thenReturn(true);

        Result result = authService.register(request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("用户名已存在"));
    }

    @Test
    void register_withInvalidRole_shouldReturnError() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRole("INVALID_ROLE");

        Result result = authService.register(request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }

    @Test
    void getUserDetail_withValidId_shouldReturnUser() {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("admin");
        mockUser.setName("Administrator");
        mockUser.setRole("ADMIN");
        mockUser.setStatus(1);

        when(userDAO.findById(1)).thenReturn(mockUser);
        when(memberProfileDAO.findByUserId(1)).thenReturn(null);

        Result result = authService.getUserDetail(1);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getUserDetail_withInvalidId_shouldReturnError() {
        when(userDAO.findById(999)).thenReturn(null);

        Result result = authService.getUserDetail(999);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
    }

    @Test
    void getUserDetail_withNullId_shouldReturnError() {
        Result result = authService.getUserDetail(null);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
    }
}
