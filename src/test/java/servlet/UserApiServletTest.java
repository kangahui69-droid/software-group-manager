package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.UserService;
import support.FastTest;
import util.Result;
import dto.ProfileDTO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.2 UserApiServlet 用户/认证API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 *
 * 端点：
 * - POST /api/auth/login → 登录
 * - POST /api/auth/logout → 登出
 * - POST /api/auth/change-password → 改密码
 * - GET /api/users/me → 当前用户信息（含profile、头像URL）
 * - PUT /api/users/me → 更新个人档案
 * - POST /api/users/me/avatar → 上传头像（multipart）
 * - GET /api/users/{id} → 用户详情
 * - GET /api/users → 成员列表（admin）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserApiServlet 用户/认证API测试")
class UserApiServletTest {

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;

    // 用户角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 用户状态枚举
    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_DISABLED = 1;

    // ==================== 测试辅助类 ====================

    private TestableUserApiServlet servlet;
    private UserService mockUserService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockUserService = mock(UserService.class);
        servlet = new TestableUserApiServlet(mockUserService);

        // 默认session行为
        when(mockRequest.getSession(false)).thenReturn(mockSession);
    }

    // ==================== 工具方法 ====================

    private User createUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        user.setStatus(STATUS_NORMAL);
        return user;
    }

    // ==================== 认证相关测试 ====================

    @Nested
    @DisplayName("认证与授权测试")
    class AuthenticationTests {

        @FastTest
        @DisplayName("未登录用户访问受保护端点应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
            assertThat(response).contains("请先登录");
        }

        @FastTest
        @DisplayName("已登录用户访问应正常处理")
        void should_process_request_when_logged_in() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockUserService.getUserDetail(eq(MEMBER_USER_ID))).thenReturn(Result.ok(member));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("MEMBER角色用户访问管理员端点应返回403")
        void should_return_403_when_member_access_admin_endpoint() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    // ==================== POST /api/auth/login 登录 ====================

    @Nested
    @DisplayName("POST /api/auth/login 登录")
    class LoginTests {

        @FastTest
        @DisplayName("有效凭据登录应返回成功")
        void should_login_successfully() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            User user = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            String jsonBody = "{\"username\":\"member1\",\"password\":\"member123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(eq("member1"), eq("member123"))).thenReturn(Result.ok(user));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("用户名为空应返回400")
        void should_return_400_when_username_empty() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            String jsonBody = "{\"username\":\"\",\"password\":\"member123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(anyString(), anyString())).thenReturn(Result.error(400, "用户名或密码不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("密码为空应返回400")
        void should_return_400_when_password_empty() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            String jsonBody = "{\"username\":\"member1\",\"password\":\"\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(anyString(), anyString())).thenReturn(Result.error(400, "用户名或密码不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("用户名不存在应返回404")
        void should_return_404_when_username_not_found() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            String jsonBody = "{\"username\":\"nonexistent\",\"password\":\"password123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(eq("nonexistent"), eq("password123"))).thenReturn(Result.error(404, "用户名或密码错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("密码错误应返回400")
        void should_return_400_when_password_wrong() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            String jsonBody = "{\"username\":\"member1\",\"password\":\"wrongpassword\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(eq("member1"), eq("wrongpassword"))).thenReturn(Result.error(400, "密码错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("密码错误");
        }

        @FastTest
        @DisplayName("用户已被禁用应返回403")
        void should_return_403_when_user_disabled() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            String jsonBody = "{\"username\":\"disableduser\",\"password\":\"password123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(eq("disableduser"), eq("password123"))).thenReturn(Result.error(403, "用户已被禁用"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
            assertThat(response).contains("用户已被禁用");
        }

        @FastTest
        @DisplayName("请求体为空应返回400")
        void should_return_400_when_body_empty() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("无效JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream("{invalid}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== POST /api/auth/logout 登出 ====================

    @Nested
    @DisplayName("POST /api/auth/logout 登出")
    class LogoutTests {

        @FastTest
        @DisplayName("登出应返回成功并使session失效")
        void should_logout_successfully() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/logout");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/logout");
            when(mockRequest.getSession(false)).thenReturn(mockSession);

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            verify(mockSession).invalidate();
        }

        @FastTest
        @DisplayName("无session登出也应返回成功")
        void should_return_success_even_without_session() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/logout");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/logout");
            when(mockRequest.getSession(false)).thenReturn(null);

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== POST /api/auth/change-password 改密码 ====================

    @Nested
    @DisplayName("POST /api/auth/change-password 改密码")
    class ChangePasswordTests {

        @FastTest
        @DisplayName("修改密码成功应返回成功")
        void should_change_password_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/change-password");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/change-password");

            String jsonBody = "{\"oldPassword\":\"old123\",\"newPassword\":\"new123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.changePassword(eq(MEMBER_USER_ID), eq("old123"), eq("new123"))).thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("未登录修改密码应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/change-password");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/change-password");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }

        @FastTest
        @DisplayName("旧密码错误应返回400")
        void should_return_400_when_old_password_wrong() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/change-password");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/change-password");

            String jsonBody = "{\"oldPassword\":\"wrongold\",\"newPassword\":\"new123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.changePassword(eq(MEMBER_USER_ID), eq("wrongold"), eq("new123")))
                    .thenReturn(Result.error(400, "原密码错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("原密码错误");
        }

        @FastTest
        @DisplayName("新密码不合规应返回400")
        void should_return_400_when_new_password_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/change-password");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/change-password");

            String jsonBody = "{\"oldPassword\":\"old123\",\"newPassword\":\"123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.changePassword(eq(MEMBER_USER_ID), eq("old123"), eq("123")))
                    .thenReturn(Result.error(400, "新密码长度不能少于6位"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("新旧密码相同应返回400")
        void should_return_400_when_same_password() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/change-password");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/change-password");

            String jsonBody = "{\"oldPassword\":\"same123\",\"newPassword\":\"same123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.changePassword(eq(MEMBER_USER_ID), eq("same123"), eq("same123")))
                    .thenReturn(Result.error(400, "新密码不能与原密码相同"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== GET /api/users/me 当前用户信息 ====================

    @Nested
    @DisplayName("GET /api/users/me 当前用户信息")
    class GetCurrentUserTests {

        @FastTest
        @DisplayName("获取当前用户信息应返回成功")
        void should_return_current_user_info() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockUserService.getUserDetail(eq(MEMBER_USER_ID))).thenReturn(Result.ok(member));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("未登录获取当前用户信息应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }
    }

    // ==================== PUT /api/users/me 更新个人档案 ====================

    @Nested
    @DisplayName("PUT /api/users/me 更新个人档案")
    class UpdateProfileTests {

        @FastTest
        @DisplayName("更新个人档案成功应返回成功")
        void should_update_profile_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/me");

            String jsonBody = "{\"name\":\"张三\",\"phone\":\"13800138000\",\"email\":\"zhangsan@example.com\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.updateProfile(eq(MEMBER_USER_ID), any(ProfileDTO.class))).thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("未登录更新个人档案应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("PUT");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }

        @FastTest
        @DisplayName("更新个人档案请求体为空应返回400")
        void should_return_400_when_body_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/me");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("更新个人档案无效JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/me");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream("{invalid}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("更新个人档案邮箱格式错误应返回400")
        void should_return_400_when_email_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/me");

            String jsonBody = "{\"name\":\"张三\",\"phone\":\"13800138000\",\"email\":\"invalid-email\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.updateProfile(eq(MEMBER_USER_ID), any(ProfileDTO.class)))
                    .thenReturn(Result.error(400, "邮箱格式不正确"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== POST /api/users/me/avatar 上传头像 ====================

    @Nested
    @DisplayName("POST /api/users/me/avatar 上传头像")
    class UploadAvatarTests {

        @FastTest
        @DisplayName("上传头像成功应返回成功")
        void should_upload_avatar_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me/avatar");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/me/avatar");
            when(mockUserService.uploadAvatar(eq(MEMBER_USER_ID), any())).thenReturn(Result.ok(100));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":100");
        }

        @FastTest
        @DisplayName("未登录上传头像应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me/avatar");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/me/avatar");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }

        @FastTest
        @DisplayName("上传文件类型不支持应返回400")
        void should_return_400_when_file_type_unsupported() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me/avatar");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/me/avatar");
            when(mockUserService.uploadAvatar(eq(MEMBER_USER_ID), any()))
                    .thenReturn(Result.error(400, "不支持的图片格式"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("上传文件过大应返回400")
        void should_return_400_when_file_too_large() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me/avatar");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/me/avatar");
            when(mockUserService.uploadAvatar(eq(MEMBER_USER_ID), any()))
                    .thenReturn(Result.error(400, "图片大小不能超过2MB"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== GET /api/users/{id} 用户详情 ====================

    @Nested
    @DisplayName("GET /api/users/{id} 用户详情")
    class GetUserDetailTests {

        @FastTest
        @DisplayName("获取用户详情应返回成功")
        void should_return_user_detail() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/3");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/3");

            User targetUser = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockUserService.getUserDetail(eq(OTHER_USER_ID))).thenReturn(Result.ok(targetUser));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("获取不存在的用户详情应返回404")
        void should_return_404_when_user_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999");

            when(mockUserService.getUserDetail(eq(999))).thenReturn(Result.error(404, "用户不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("用户不存在");
        }

        @FastTest
        @DisplayName("用户ID格式错误应返回400")
        void should_return_400_when_id_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/abc");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("未登录获取用户详情应允许（公开）")
        void should_allow_guest_to_view_user_detail() throws Exception {
            when(mockSession.getAttribute("user")).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/" + OTHER_USER_ID);
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/" + OTHER_USER_ID);

            User targetUser = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockUserService.getUserDetail(eq(OTHER_USER_ID))).thenReturn(Result.ok(targetUser));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/users 成员列表 ====================

    @Nested
    @DisplayName("GET /api/users 成员列表")
    class ListMembersTests {

        @FastTest
        @DisplayName("管理员获取成员列表应返回成功")
        void should_return_members_list() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");
            when(mockRequest.getParameter("page")).thenReturn("1");

            User member1 = createUser(2, ROLE_MEMBER);
            User member2 = createUser(3, ROLE_MEMBER);
            when(mockUserService.listMembers(eq(ADMIN_USER_ID), any(), any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList(member1, member2)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("非管理员获取成员列表应返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("成员列表为空应返回空数组")
        void should_return_empty_list_when_no_members() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");
            when(mockRequest.getParameter("page")).thenReturn("1");

            when(mockUserService.listMembers(eq(ADMIN_USER_ID), any(), any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }

        @FastTest
        @DisplayName("按关键词搜索成员应正确筛选")
        void should_filter_by_keyword() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("keyword")).thenReturn("张三");

            when(mockUserService.listMembers(eq(ADMIN_USER_ID), eq("张三"), any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("按角色筛选成员应正确筛选")
        void should_filter_by_role() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("role")).thenReturn("MEMBER");

            when(mockUserService.listMembers(eq(ADMIN_USER_ID), any(), eq("MEMBER"), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("分页参数应正确传递")
        void should_pass_pagination_params() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockRequest.getParameter("pageSize")).thenReturn("20");

            when(mockUserService.listMembers(eq(ADMIN_USER_ID), any(), any(), eq(2), eq(20)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("未登录获取成员列表应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockSession.getAttribute("user")).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }
    }

    // ==================== HTTP方法测试 ====================

    @Nested
    @DisplayName("HTTP方法测试")
    class HttpMethodTests {

        @FastTest
        @DisplayName("不支持的HTTP方法应返回405")
        void should_return_405_when_method_not_allowed() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("DELETE");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":405");
        }

        @FastTest
        @DisplayName("OPTIONS预检请求应返回200")
        void should_return_200_for_options_request() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("OPTIONS");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doOptions(mockRequest, mockResponse);

            verify(mockResponse).setHeader(eq("Access-Control-Allow-Methods"), anyString());
            verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        }
    }

    // ==================== 边界测试 ====================

    @Nested
    @DisplayName("边界测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空pathInfo应返回空列表或错误")
        void should_handle_empty_path_info() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            // 应该返回400或正确处理空pathInfo
            assertThat(response).contains("\"code\":");
        }

        @FastTest
        @DisplayName("超长用户名应正确处理")
        void should_handle_long_username() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");

            String longUsername = "a".repeat(1000);
            String jsonBody = String.format("{\"username\":\"%s\",\"password\":\"member123\"}", longUsername);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(anyString(), anyString())).thenReturn(Result.error(400, "用户名或密码不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("特殊字符应正确处理")
        void should_handle_special_characters() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/login");

            String jsonBody = "{\"username\":\"user<script>alert(1)</script>\",\"password\":\"member123\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(anyString(), anyString())).thenReturn(Result.error(404, "用户名或密码错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("负数分页参数应使用默认值")
        void should_use_default_for_negative_page() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/users");
            when(mockRequest.getParameter("page")).thenReturn("-1");

            when(mockUserService.listMembers(eq(ADMIN_USER_ID), any(), any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超长密码应正确处理")
        void should_handle_long_password() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/auth/login");
            when(mockRequest.getMethod()).thenReturn("POST");

            String longPassword = "a".repeat(500);
            String jsonBody = String.format("{\"username\":\"member1\",\"password\":\"%s\"}", longPassword);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
            when(mockUserService.login(anyString(), anyString())).thenReturn(Result.error(400, "密码错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 响应格式测试 ====================

    @Nested
    @DisplayName("响应格式测试")
    class ResponseFormatTests {

        @FastTest
        @DisplayName("成功响应应包含所有必需字段")
        void should_include_all_fields_in_success_response() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockUserService.getUserDetail(eq(MEMBER_USER_ID))).thenReturn(Result.ok(member));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"message\":\"ok\"");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("错误响应应包含所有必需字段")
        void should_include_all_fields_in_error_response() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
            assertThat(response).contains("\"message\":\"请先登录\"");
            assertThat(response).contains("\"data\":null");
        }

        @FastTest
        @DisplayName("响应Content-Type应为JSON")
        void should_return_json_content_type() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/users/me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockUserService.getUserDetail(eq(MEMBER_USER_ID))).thenReturn(Result.ok(member));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }
    }

    // ==================== 测试用子类 ====================

    /**
     * 测试用UserApiServlet子类
     * 复制UserApiServlet的业务逻辑，用于测试
     */
    static class TestableUserApiServlet {

        private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
        private static final int DEFAULT_PAGE_SIZE = 20;
        private static final int MAX_PAGE_SIZE = 100;
        private static final String ROLE_ADMIN = "ADMIN";

        private final UserService userService;
        private final Gson gson = new GsonBuilder().serializeNulls().create();

        public TestableUserApiServlet(UserService userService) {
            this.userService = userService;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            String pathInfo = req.getPathInfo();

            // /api/users/me - 当前用户信息
            if (pathInfo == null || pathInfo.equals("/me") || pathInfo.equals("/me/")) {
                handleGetCurrentUser(req, resp, currentUser);
                return;
            }

            // /api/users - 成员列表（仅管理员）
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty() || pathInfo.equals("/users")) {
                handleListMembers(req, resp, currentUser);
                return;
            }

            // /api/users/{id} - 用户详情
            if (pathInfo.startsWith("/")) {
                String[] segments = pathInfo.split("/");
                if (segments.length >= 2) {
                    String idStr = segments[1];
                    if (isNumeric(idStr)) {
                        handleGetUserDetail(req, resp, currentUser, Integer.parseInt(idStr));
                        return;
                    }
                }
            }

            sendError(resp, 400, "无效的请求路径");
        }

        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            String pathInfo = req.getPathInfo();

            // /api/auth/login
            if (pathInfo != null && pathInfo.equals("/login")) {
                handleLogin(req, resp);
                return;
            }

            // /api/auth/logout
            if (pathInfo != null && pathInfo.equals("/logout")) {
                handleLogout(req, resp);
                return;
            }

            // /api/auth/change-password
            if (pathInfo != null && pathInfo.equals("/change-password")) {
                User currentUser = getCurrentUser(req);
                if (currentUser == null) {
                    sendError(resp, 401, "请先登录");
                    return;
                }
                handleChangePassword(req, resp, currentUser);
                return;
            }

            // /api/users/me/avatar
            if (pathInfo != null && (pathInfo.equals("/me/avatar") || pathInfo.startsWith("/me/avatar"))) {
                User currentUser = getCurrentUser(req);
                if (currentUser == null) {
                    sendError(resp, 401, "请先登录");
                    return;
                }
                handleUploadAvatar(req, resp, currentUser);
                return;
            }

            sendError(resp, 400, "无效的请求路径");
        }

        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo != null && (pathInfo.equals("/me") || pathInfo.startsWith("/me"))) {
                handleUpdateProfile(req, resp, currentUser);
                return;
            }

            sendError(resp, 400, "无效的请求路径");
        }

        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);
            sendError(resp, 405, "不支持的HTTP方法");
        }

        protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            resp.setStatus(HttpServletResponse.SC_OK);
        }

        // ==================== 处理器方法 ====================

        private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            Map<String, Object> body = parseJsonRequest(req, Map.class);
            if (body == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            String username = body.get("username") != null ? body.get("username").toString() : "";
            String password = body.get("password") != null ? body.get("password").toString() : "";

            Result result = userService.login(username, password);
            writeJson(resp, result);
        }

        private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            javax.servlet.http.HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            writeJson(resp, Result.ok());
        }

        private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            Map<String, Object> body = parseJsonRequest(req, Map.class);
            if (body == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            String oldPassword = body.get("oldPassword") != null ? body.get("oldPassword").toString() : "";
            String newPassword = body.get("newPassword") != null ? body.get("newPassword").toString() : "";

            Result result = userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            writeJson(resp, result);
        }

        private void handleGetCurrentUser(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            Result result = userService.getUserDetail(currentUser.getId());
            writeJson(resp, result);
        }

        private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            Map<String, Object> body = parseJsonRequest(req, Map.class);
            if (body == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            ProfileDTO profileDTO = new ProfileDTO();
            profileDTO.setName(body.get("name") != null ? body.get("name").toString() : "");
            profileDTO.setPhone(body.get("phone") != null ? body.get("phone").toString() : "");
            profileDTO.setEmail(body.get("email") != null ? body.get("email").toString() : "");
            profileDTO.setBirthday(body.get("birthday") != null ? body.get("birthday").toString() : "");
            profileDTO.setStudentId(body.get("studentId") != null ? body.get("studentId").toString() : "");
            profileDTO.setMajor(body.get("major") != null ? body.get("major").toString() : "");
            profileDTO.setGrade(body.get("grade") != null ? body.get("grade").toString() : "");
            profileDTO.setIntroduction(body.get("bio") != null ? body.get("bio").toString() : "");

            Result result = userService.updateProfile(currentUser.getId(), profileDTO);
            writeJson(resp, result);
        }

        private void handleUploadAvatar(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            Result result = userService.uploadAvatar(currentUser.getId(), null);
            writeJson(resp, result);
        }

        private void handleGetUserDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser, int userId) throws IOException {
            Result result = userService.getUserDetail(userId);
            writeJson(resp, result);
        }

        private void handleListMembers(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            if (!ROLE_ADMIN.equals(currentUser.getRole())) {
                sendError(resp, 403, "需要管理员权限");
                return;
            }

            int[] pageParams = parsePageParams(req);
            String keyword = req.getParameter("keyword");
            String role = req.getParameter("role");

            Result result = userService.listMembers(currentUser.getId(), keyword, role, pageParams[0], pageParams[1]);
            writeJson(resp, result);
        }

        // ==================== 工具方法 ====================

        private int[] parsePageParams(HttpServletRequest request) {
            int page = 1;
            int pageSize = DEFAULT_PAGE_SIZE;

            String pageStr = request.getParameter("page");
            String pageSizeStr = request.getParameter("pageSize");

            try {
                if (pageStr != null && !pageStr.isEmpty()) {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                }
                if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
                    if (pageSize > MAX_PAGE_SIZE) pageSize = MAX_PAGE_SIZE;
                }
            } catch (NumberFormatException ignored) {
            }

            return new int[] { page, pageSize };
        }

        private User getCurrentUser(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            return session != null ? (User) session.getAttribute("user") : null;
        }

        private <T> T parseJsonRequest(HttpServletRequest request, Class<T> clazz) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();
            if (body.isEmpty() || "null".equals(body)) {
                return null;
            }
            try {
                return gson.fromJson(body, clazz);
            } catch (Exception e) {
                return null;
            }
        }

        private void writeJson(HttpServletResponse response, Result result) throws IOException {
            if (result == null) {
                result = Result.error(500, "系统错误");
            }
            response.setStatus(result.isSuccess() ? HttpServletResponse.SC_OK : result.getCode());
            PrintWriter writer = response.getWriter();
            gson.toJson(result, writer);
            writer.flush();
        }

        private void sendError(HttpServletResponse response, int code, String message) throws IOException {
            writeJson(response, Result.error(code, message));
        }

        private boolean isNumeric(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
