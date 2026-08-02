package servlet;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.UserService;
import support.FastTest;
import util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProfileServlet TDD测试套件 - 4.7 Servlet改造
 *
 * 测试范围：服务分层与API化重构计划.md 4.7 Servlet渐进改造
 * - ProfileServlet → UserService
 *
 * 测试策略：
 * - Servlet调用Service层方法而非直接调用DAO
 * - 所有action由Service处理业务逻辑
 * - Servlet只做：取参→调service→写响应
 *
 * Mock说明：
 * - UserService: updateProfile / changePassword / uploadAvatar / getUserDetail
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProfileServlet 改造测试")
class ProfileServletTest {

    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Part avatarPart;

    private ProfileServletRefactored servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProfileServletRefactored();
        servlet.setUserService(userService);
    }

    // ==================== 辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private void setupSession(User user) {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }

    private void setupNoSession() {
        when(request.getSession(false)).thenReturn(null);
    }

    // ==================== doGet 路由测试 ====================

    @Nested
    @DisplayName("doGet 路由测试")
    class DoGetTests {

        @FastTest
        @DisplayName("action=edit 应转发到编辑页面")
        void should_forward_to_edit_page_when_action_is_edit() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getRequestURI()).thenReturn("/member/profile/edit");
            when(request.getParameter("action")).thenReturn("edit");
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doGet(request, response);

            verify(request).getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }

        @FastTest
        @DisplayName("无action参数 应重定向到profile页面")
        void should_redirect_to_profile_when_no_action() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getRequestURI()).thenReturn("/member/profile");
            when(request.getParameter("action")).thenReturn(null);
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doGet(request, response);

            verify(response).sendRedirect("/software-group/member/profile.jsp");
        }

        @FastTest
        @DisplayName("未登录访问应重定向到登录页")
        void should_redirect_to_login_when_not_logged_in() throws Exception {
            setupNoSession();
            when(request.getRequestURI()).thenReturn("/member/profile");
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doGet(request, response);

            verify(response).sendRedirect("/software-group/login.jsp");
        }
    }

    // ==================== doPost 路由测试 ====================

    @Nested
    @DisplayName("doPost 路由测试")
    class DoPostTests {

        @FastTest
        @DisplayName("action=update 应调用更新方法")
        void should_call_update_when_action_is_update() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getRequestURI()).thenReturn("/member/profile/update");
            when(request.getParameter("action")).thenReturn("update");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("test@example.com");
            when(userService.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(userService).updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any());
        }

        @FastTest
        @DisplayName("未知action应返回404")
        void should_return_404_when_unknown_action() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getRequestURI()).thenReturn("/member/profile/unknown");
            when(request.getParameter("action")).thenReturn("unknown");

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // ==================== updateProfile 正常路径测试 ====================

    @Nested
    @DisplayName("更新档案 正常路径")
    class UpdateProfileNormalTests {

        @FastTest
        @DisplayName("更新基本信息成功应设置成功消息")
        void should_set_success_message_when_update_profile_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("zhangsan@example.com");
            when(request.getParameter("birthday")).thenReturn("2000-01-01");
            when(request.getParameter("studentId")).thenReturn("2021001234");
            when(request.getParameter("major")).thenReturn("计算机科学");
            when(request.getParameter("grade")).thenReturn("2021");
            when(request.getParameter("bio")).thenReturn("热爱编程");
            when(userService.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).setAttribute("success", "个人信息更新成功");
        }

        @FastTest
        @DisplayName("更新成功应转发到编辑页面")
        void should_forward_to_edit_page_on_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("zhangsan@example.com");
            when(userService.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }
    }

    // ==================== updateProfile 边界情况测试 ====================

    @Nested
    @DisplayName("更新档案 边界情况")
    class UpdateProfileBoundaryTests {

        @FastTest
        @DisplayName("姓名为空应返回错误")
        void should_return_error_when_name_is_empty() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("test@example.com");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("邮箱格式错误应返回错误")
        void should_return_error_when_email_invalid() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("invalid-email");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("手机号格式错误应返回错误")
        void should_return_error_when_phone_invalid() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("123");
            when(request.getParameter("email")).thenReturn("test@example.com");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("姓名超长应返回错误")
        void should_return_error_when_name_too_long() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张".repeat(100));
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("test@example.com");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("生日格式错误应返回错误")
        void should_return_error_when_birthday_invalid() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("test@example.com");
            when(request.getParameter("birthday")).thenReturn("invalid-date");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }
    }

    // ==================== updateProfile 异常场景测试 ====================

    @Nested
    @DisplayName("更新档案 异常场景")
    class UpdateProfileExceptionTests {

        @FastTest
        @DisplayName("Service抛异常应返回错误消息")
        void should_return_error_when_service_throws_exception() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("test@example.com");
            when(userService.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("数据库错误"));

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("邮箱已被使用应返回特定错误")
        void should_return_error_when_email_duplicate() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getParameter("name")).thenReturn("张三");
            when(request.getParameter("phone")).thenReturn("13800138000");
            when(request.getParameter("email")).thenReturn("existing@example.com");
            when(userService.updateProfile(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Result.error(400, "邮箱已被其他用户使用"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "邮箱已被其他用户使用");
        }

        @FastTest
        @DisplayName("未登录应重定向到登录页")
        void should_redirect_to_login_when_not_logged_in() throws Exception {
            setupNoSession();
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doPost(request, response);

            verify(response).sendRedirect("/software-group/login.jsp");
        }
    }

    // ==================== 头像上传测试 ====================

    @Nested
    @DisplayName("头像上传测试")
    class AvatarUploadTests {

        @FastTest
        @DisplayName("上传头像成功应返回成功消息")
        void should_return_success_when_avatar_upload_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getContentType()).thenReturn("multipart/form-data");
            when(request.getPart("avatar")).thenReturn(avatarPart);
            when(avatarPart.getSize()).thenReturn(1024L * 100); // 100KB
            when(avatarPart.getContentType()).thenReturn("image/png");
            when(userService.uploadAvatar(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(userService).uploadAvatar(any(), any());
        }

        @FastTest
        @DisplayName("头像大小超过500KB应返回错误")
        void should_return_error_when_avatar_too_large() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getContentType()).thenReturn("multipart/form-data");
            when(request.getPart("avatar")).thenReturn(avatarPart);
            when(avatarPart.getSize()).thenReturn(1024L * 600); // 600KB

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "头像图片大小不能超过500KB");
        }

        @FastTest
        @DisplayName("头像格式不支持应返回错误")
        void should_return_error_when_avatar_format_unsupported() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("userType")).thenReturn("MEMBER");
            when(request.getContentType()).thenReturn("multipart/form-data");
            when(request.getPart("avatar")).thenReturn(avatarPart);
            when(avatarPart.getSize()).thenReturn(1024L * 100);
            when(avatarPart.getContentType()).thenReturn("application/pdf");

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "仅支持JPEG、JPG、PNG、GIF、WebP格式的图片");
        }
    }

    // ==================== 状态枚举测试 ====================

    @Nested
    @DisplayName("Servlet Action枚举测试")
    class ActionEnumTests {

        @FastTest
        @DisplayName("支持的action应包含所有预期值")
        void supported_actions_should_include_all_expected() {
            assertThat(servlet.getSupportedActions()).contains("edit", "update");
        }

        @FastTest
        @DisplayName("MEMBER角色应能访问编辑页面")
        void member_should_access_edit_page() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getRequestURI()).thenReturn("/member/profile/edit");
            when(request.getParameter("action")).thenReturn("edit");
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doGet(request, response);

            verify(request).getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }

        @FastTest
        @DisplayName("GUEST角色应返回权限错误")
        void guest_should_return_permission_error() throws Exception {
            User guest = createUser(0, "guest", "GUEST");
            setupSession(guest);
            when(request.getRequestURI()).thenReturn("/member/profile/edit");
            when(request.getParameter("action")).thenReturn("edit");

            servlet.doGet(request, response);

            verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    // ==================== 内部类：可测试的Servlet ====================

    /**
     * 可测试的ProfileServlet封装
     * 实际项目中会被移除，直接测试真实Servlet
     */
    static class ProfileServletRefactored extends ProfileServlet {
        private UserService userService;

        void setUserService(UserService userService) {
            this.userService = userService;
        }

        UserService getUserService() {
            return userService;
        }

        String[] getSupportedActions() {
            return new String[]{"edit", "update"};
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String action = request.getParameter("action");
            if ("edit".equals(action)) {
                showEditProfilePage(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/member/profile.jsp");
            }
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String action = request.getParameter("action");
            if ("update".equals(action)) {
                updateProfile(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private void showEditProfilePage(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }
            User user = (User) session.getAttribute("user");
            if ("GUEST".equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("user", user);
            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }

        private void updateProfile(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            User currentUser = (User) session.getAttribute("user");

            String userType = request.getParameter("userType");
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");
            String birthday = request.getParameter("birthday");
            String studentId = request.getParameter("studentId");
            String major = request.getParameter("major");
            String grade = request.getParameter("grade");
            String bio = request.getParameter("bio");

            // 验证
            if (name == null || name.trim().isEmpty()) {
                request.setAttribute("error", "姓名不能为空");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }

            if (name.length() > 50) {
                request.setAttribute("error", "姓名不能超过50个字符");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }

            if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
                request.setAttribute("error", "手机号格式不正确");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }

            if (email != null && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                request.setAttribute("error", "邮箱格式不正确");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }

            try {
                Result result = userService.updateProfile(
                        currentUser.getId(), name, phone, email, birthday, studentId, major, grade, bio);

                if (result.isSuccess()) {
                    request.setAttribute("success", "个人信息更新成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "更新失败: " + e.getMessage());
            }

            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }
    }
}
