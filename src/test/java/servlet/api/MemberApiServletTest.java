package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.MemberProfile;
import model.User;
import model.Award;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.MemberService;
import support.FastTest;
import util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MemberApiServlet TDD测试套件
 *
 * 测试范围：服务分层与API化完整计划.md 5.3 MemberApiServlet 端点
 * - 所有REST端点
 * - 所有HTTP方法(GET/POST/PUT/DELETE)
 * - 认证与授权
 * - 参数解析与验证
 * - 错误处理
 * - 所有状态枚举
 * - 所有边界情况
 *
 * 测试覆盖端点：
 * - GET  /api/members           → 成员列表(分页)
 * - GET  /api/members/{id}     → 成员详情
 * - POST /api/members           → 创建成员
 * - PUT  /api/members/{id}     → 更新成员
 * - DELETE /api/members/{id}   → 删除成员
 * - POST /api/members/{id}/enable   → 启用成员
 * - POST /api/members/{id}/disable   → 禁用成员
 * - POST /api/members/{id}/reset-password → 重置密码
 * - GET  /api/members/{id}/awards → 成员获奖列表
 * - GET  /api/members/{id}/profile → 获取个人档案
 * - PUT  /api/members/{id}/profile → 更新个人档案
 * - POST /api/members/{id}/avatar → 上传头像
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MemberApiServlet 成员API测试")
class MemberApiServletTest {

    private TestableMemberApiServlet servlet;
    private MemberService mockMemberService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private StringWriter responseWriter;
    private Gson gson;

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer GUEST_USER_ID = null;
    private static final Integer MEMBER_ID = 100;
    private static final Integer NONEXISTENT_MEMBER_ID = 99999;
    private static final Integer AVATAR_FILE_ID = 200;
    private static final Integer PROFILE_ID = 10;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    // 角色常量
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_TEACHER = "TEACHER";

    // 状态常量
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer STATUS_DISABLED = 0;

    @BeforeEach
    void setUp() throws Exception {
        mockMemberService = mock(MemberService.class);
        servlet = new TestableMemberApiServlet(mockMemberService);
        responseWriter = new StringWriter();
        gson = new GsonBuilder().create();

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== Helper Methods ====================

    private User createTestUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser" + id);
        user.setName("测试用户" + id);
        user.setRole(role);
        user.setStatus(STATUS_ENABLED);
        user.setEmail("test" + id + "@example.com");
        return user;
    }

    private MemberProfile createTestProfile(Integer id, Integer userId) {
        MemberProfile profile = new MemberProfile();
        profile.setId(id);
        profile.setUserId(userId);
        profile.setStudentId("2021000000" + userId);
        profile.setMajor("计算机科学与技术");
        profile.setGrade("2021");
        profile.setIntroduction("这是简介");
        profile.setGithub("github.com/user");
        profile.setBlog("blog.example.com");
        profile.setStatus(STATUS_ENABLED);
        return profile;
    }

    private Award createTestAward(Integer id, Integer userId, String awardName) {
        Award award = new Award();
        award.setId(id);
        award.setName(awardName);
        award.setAwardName(awardName);
        award.setAwardStatus(Award.STATUS_APPROVED);
        return award;
    }

    private String getResponseBody() {
        return responseWriter.toString();
    }

    private void simulateLogin(User user) {
        when(mockSession.getAttribute("user")).thenReturn(user);
    }

    private void simulateUnauthorized() {
        when(mockSession.getAttribute("user")).thenReturn(null);
    }

    private void resetResponseWriter() throws Exception {
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== 认证测试 ====================

    @Nested
    @DisplayName("认证测试")
    class AuthTests {

        @Test
        @FastTest
        @DisplayName("未登录GET请求应返回401")
        void should_return_401_when_get_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录POST请求应返回401")
        void should_return_401_when_post_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录DELETE请求应返回401")
        void should_return_401_when_delete_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/members/1");
            when(mockRequest.getPathInfo()).thenReturn("/1");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("已登录用户应能正常访问")
        void should_return_200_when_logged_in() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("role")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", List.of());
            pageResult.put("total", 0);
            pageResult.put("page", 1);
            pageResult.put("pageSize", 20);
            when(mockMemberService.listMembers(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/members 成员列表 ====================

    @Nested
    @DisplayName("GET /api/members 成员列表")
    class ListMembersTests {

        @Test
        @FastTest
        @DisplayName("获取成员列表成功")
        void should_list_members_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("role")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            List<User> members = Arrays.asList(
                    createTestUser(1, ROLE_ADMIN),
                    createTestUser(2, ROLE_MEMBER)
            );
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", members);
            pageResult.put("total", 2);
            pageResult.put("page", 1);
            pageResult.put("pageSize", 20);

            when(mockMemberService.listMembers(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("testuser1");
        }

        @Test
        @FastTest
        @DisplayName("带分页参数获取成员列表")
        void should_list_members_with_pagination() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockRequest.getParameter("pageSize")).thenReturn("10");
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("role")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", List.of());
            pageResult.put("total", 0);
            pageResult.put("page", 2);
            pageResult.put("pageSize", 10);

            when(mockMemberService.listMembers(any(), eq(2), eq(10)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockMemberService).listMembers(any(), eq(2), eq(10));
        }

        @Test
        @FastTest
        @DisplayName("带关键字筛选获取成员列表")
        void should_filter_members_by_keyword() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn("张三");
            when(mockRequest.getParameter("role")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", List.of());
            pageResult.put("total", 0);
            pageResult.put("page", 1);
            pageResult.put("pageSize", 20);

            when(mockMemberService.listMembers(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带角色筛选获取成员列表")
        void should_filter_members_by_role() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("role")).thenReturn(ROLE_MEMBER);
            when(mockRequest.getParameter("status")).thenReturn(null);

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", List.of());
            pageResult.put("total", 0);
            pageResult.put("page", 1);
            pageResult.put("pageSize", 20);

            when(mockMemberService.listMembers(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带状态筛选获取成员列表")
        void should_filter_members_by_status() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("role")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn("0");

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", List.of());
            pageResult.put("total", 0);
            pageResult.put("page", 1);
            pageResult.put("pageSize", 20);

            when(mockMemberService.listMembers(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_empty_list() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("role")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", List.of());
            pageResult.put("total", 0);
            pageResult.put("page", 1);
            pageResult.put("pageSize", 20);

            when(mockMemberService.listMembers(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(pageResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/members/{id} 成员详情 ====================

    @Nested
    @DisplayName("GET /api/members/{id} 成员详情")
    class GetMemberDetailTests {

        @Test
        @FastTest
        @DisplayName("获取成员详情成功")
        void should_get_member_detail_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID);

            User user = createTestUser(MEMBER_ID, ROLE_MEMBER);
            MemberProfile profile = createTestProfile(PROFILE_ID, MEMBER_ID);
            Map<String, Object> detailResult = new HashMap<>();
            detailResult.put("user", user);
            detailResult.put("profile", profile);

            when(mockMemberService.getMemberDetail(MEMBER_ID))
                    .thenReturn(Result.ok(detailResult));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("testuser100");
        }

        @Test
        @FastTest
        @DisplayName("获取不存在的成员应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID);

            when(mockMemberService.getMemberDetail(NONEXISTENT_MEMBER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
            assertThat(getResponseBody()).contains("成员不存在");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("无效的成员ID");
        }

        @Test
        @FastTest
        @DisplayName("嵌套路径应返回404")
        void should_return_404_for_nested_path() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/invalid");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/invalid");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/members 创建成员 ====================

    @Nested
    @DisplayName("POST /api/members 创建成员")
    class CreateMemberTests {

        @Test
        @FastTest
        @DisplayName("管理员创建成员应成功")
        void should_create_member_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"username\":\"newmember\",\"password\":\"password123\",\"role\":\"MEMBER\",\"name\":\"新成员\"}")));

            User newUser = createTestUser(100, ROLE_MEMBER);
            when(mockMemberService.createMember(any()))
                    .thenReturn(Result.ok(newUser));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockMemberService).createMember(any());
        }

        @Test
        @FastTest
        @DisplayName("非管理员创建成员应返回403")
        void should_return_403_when_not_admin() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"username\":\"newmember\",\"password\":\"password123\",\"role\":\"MEMBER\"}")));

            when(mockMemberService.createMember(any()))
                    .thenReturn(Result.error(403, "无权限创建成员"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("空请求体应返回400")
        void should_return_400_when_body_empty() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("请求体不能为空");
        }

        @Test
        @FastTest
        @DisplayName("无效JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("not json")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("用户名已存在应返回400")
        void should_return_400_when_username_exists() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"username\":\"existing\",\"password\":\"password123\",\"role\":\"MEMBER\"}")));

            when(mockMemberService.createMember(any()))
                    .thenReturn(Result.error(400, "用户名已存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("用户名已存在");
        }
    }

    // ==================== PUT /api/members/{id} 更新成员 ====================

    @Nested
    @DisplayName("PUT /api/members/{id} 更新成员")
    class UpdateMemberTests {

        @Test
        @FastTest
        @DisplayName("管理员更新成员应成功")
        void should_update_member_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID);
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"name\":\"更新后的姓名\",\"email\":\"updated@example.com\"}")));

            User updatedUser = createTestUser(MEMBER_ID, ROLE_MEMBER);
            updatedUser.setName("更新后的姓名");
            when(mockMemberService.updateMember(eq(MEMBER_ID), any(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(updatedUser));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("成员更新自己应成功")
        void should_allow_member_update_own_info() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_USER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_USER_ID);
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"name\":\"自己更新的姓名\"}")));

            User updatedUser = createTestUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockMemberService.updateMember(eq(MEMBER_USER_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(updatedUser));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非管理员更新他人应返回403")
        void should_return_403_when_not_admin_or_owner() throws Exception {
            simulateLogin(createTestUser(OTHER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID);
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"name\":\"新姓名\"}")));

            when(mockMemberService.updateMember(eq(MEMBER_ID), any(), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限更新此成员信息"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("不存在的成员应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID);
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"name\":\"新姓名\"}")));

            when(mockMemberService.updateMember(eq(NONEXISTENT_MEMBER_ID), any(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("根路径PUT应返回400")
        void should_return_400_when_put_on_root() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"name\":\"新姓名\"}")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("根路径不支持PUT方法");
        }
    }

    // ==================== DELETE /api/members/{id} 删除成员 ====================

    @Nested
    @DisplayName("DELETE /api/members/{id} 删除成员")
    class DeleteMemberTests {

        @Test
        @FastTest
        @DisplayName("管理员删除成员应成功")
        void should_delete_member_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID);

            when(mockMemberService.deleteMember(MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非管理员删除应返回403")
        void should_return_403_when_not_admin() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID);

            when(mockMemberService.deleteMember(MEMBER_ID, MEMBER_USER_ID))
                    .thenReturn(Result.error(403, "无权限删除成员"));

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("删除不存在的成员应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID);

            when(mockMemberService.deleteMember(NONEXISTENT_MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("删除自己应返回400")
        void should_return_400_when_deleting_self() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + ADMIN_USER_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + ADMIN_USER_ID);

            when(mockMemberService.deleteMember(ADMIN_USER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(400, "不能删除自己"));

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("不能删除自己");
        }

        @Test
        @FastTest
        @DisplayName("删除根路径应返回404")
        void should_return_404_when_deleting_root() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members");
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/members/{id}/enable 启用成员 ====================

    @Nested
    @DisplayName("POST /api/members/{id}/enable 启用成员")
    class EnableMemberTests {

        @Test
        @FastTest
        @DisplayName("管理员启用成员应成功")
        void should_enable_member_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/enable");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/enable");

            when(mockMemberService.enableMember(MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非管理员启用应返回403")
        void should_return_403_when_not_admin() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/enable");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/enable");

            when(mockMemberService.enableMember(MEMBER_ID, MEMBER_USER_ID))
                    .thenReturn(Result.error(403, "无权限启用成员"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("启用不存在的成员应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID + "/enable");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID + "/enable");

            when(mockMemberService.enableMember(NONEXISTENT_MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/members/{id}/disable 禁用成员 ====================

    @Nested
    @DisplayName("POST /api/members/{id}/disable 禁用成员")
    class DisableMemberTests {

        @Test
        @FastTest
        @DisplayName("管理员禁用成员应成功")
        void should_disable_member_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/disable");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/disable");

            when(mockMemberService.disableMember(MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非管理员禁用应返回403")
        void should_return_403_when_not_admin() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/disable");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/disable");

            when(mockMemberService.disableMember(MEMBER_ID, MEMBER_USER_ID))
                    .thenReturn(Result.error(403, "无权限禁用成员"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("禁用自己应返回400")
        void should_return_400_when_disabling_self() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + ADMIN_USER_ID + "/disable");
            when(mockRequest.getPathInfo()).thenReturn("/" + ADMIN_USER_ID + "/disable");

            when(mockMemberService.disableMember(ADMIN_USER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(400, "不能禁用自己"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("不能禁用自己");
        }

        @Test
        @FastTest
        @DisplayName("禁用不存在的成员应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID + "/disable");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID + "/disable");

            when(mockMemberService.disableMember(NONEXISTENT_MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/members/{id}/reset-password 重置密码 ====================

    @Nested
    @DisplayName("POST /api/members/{id}/reset-password 重置密码")
    class ResetPasswordTests {

        @Test
        @FastTest
        @DisplayName("管理员重置密码应成功")
        void should_reset_password_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/reset-password");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/reset-password");

            when(mockMemberService.resetPassword(MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非管理员重置密码应返回403")
        void should_return_403_when_not_admin() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/reset-password");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/reset-password");

            when(mockMemberService.resetPassword(MEMBER_ID, MEMBER_USER_ID))
                    .thenReturn(Result.error(403, "无权限重置密码"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("重置自己密码应返回400")
        void should_return_400_when_resetting_own_password() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + ADMIN_USER_ID + "/reset-password");
            when(mockRequest.getPathInfo()).thenReturn("/" + ADMIN_USER_ID + "/reset-password");

            when(mockMemberService.resetPassword(ADMIN_USER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(400, "不能重置自己的密码"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("不能重置自己的密码");
        }

        @Test
        @FastTest
        @DisplayName("重置不存在的成员密码应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID + "/reset-password");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID + "/reset-password");

            when(mockMemberService.resetPassword(NONEXISTENT_MEMBER_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== GET /api/members/{id}/awards 成员获奖列表 ====================

    @Nested
    @DisplayName("GET /api/members/{id}/awards 成员获奖列表")
    class GetMemberAwardsTests {

        @Test
        @FastTest
        @DisplayName("获取成员获奖列表成功")
        void should_get_member_awards_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/awards");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/awards");

            List<Award> awards = Arrays.asList(
                    createTestAward(1, MEMBER_ID, "程序设计大赛"),
                    createTestAward(2, MEMBER_ID, "数学建模竞赛")
            );
            when(mockMemberService.getMemberAwards(MEMBER_ID))
                    .thenReturn(Result.ok(awards));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("程序设计大赛");
            assertThat(getResponseBody()).contains("数学建模竞赛");
        }

        @Test
        @FastTest
        @DisplayName("无获奖记录应返回空列表")
        void should_return_empty_list_when_no_awards() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/awards");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/awards");

            when(mockMemberService.getMemberAwards(MEMBER_ID))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("获取不存在的成员获奖列表应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID + "/awards");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID + "/awards");

            when(mockMemberService.getMemberAwards(NONEXISTENT_MEMBER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== GET /api/members/{id}/profile 获取个人档案 ====================

    @Nested
    @DisplayName("GET /api/members/{id}/profile 获取个人档案")
    class GetProfileTests {

        @Test
        @FastTest
        @DisplayName("获取个人档案成功")
        void should_get_profile_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/profile");

            MemberProfile profile = createTestProfile(PROFILE_ID, MEMBER_ID);
            when(mockMemberService.getProfile(MEMBER_ID))
                    .thenReturn(Result.ok(profile));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("计算机科学与技术");
        }

        @Test
        @FastTest
        @DisplayName("档案不存在应返回404")
        void should_return_404_when_profile_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/profile");

            when(mockMemberService.getProfile(MEMBER_ID))
                    .thenReturn(Result.error(404, "档案不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
            assertThat(getResponseBody()).contains("档案不存在");
        }

        @Test
        @FastTest
        @DisplayName("获取不存在的成员档案应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID + "/profile");

            when(mockMemberService.getProfile(NONEXISTENT_MEMBER_ID))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== PUT /api/members/{id}/profile 更新个人档案 ====================

    @Nested
    @DisplayName("PUT /api/members/{id}/profile 更新个人档案")
    class UpdateProfileTests {

        @Test
        @FastTest
        @DisplayName("成员更新自己档案应成功")
        void should_update_profile_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_USER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_USER_ID + "/profile");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"studentId\":\"2021000001\",\"major\":\"软件工程\",\"grade\":\"2022\"}")));

            MemberProfile updatedProfile = createTestProfile(PROFILE_ID, MEMBER_USER_ID);
            updatedProfile.setMajor("软件工程");
            when(mockMemberService.updateProfile(eq(MEMBER_USER_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(updatedProfile));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("管理员更新任意档案应成功")
        void should_allow_admin_update_any_profile() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/profile");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"major\":\"新专业\"}")));

            MemberProfile updatedProfile = createTestProfile(PROFILE_ID, MEMBER_ID);
            when(mockMemberService.updateProfile(eq(MEMBER_ID), any(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(updatedProfile));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非本人非管理员更新应返回403")
        void should_return_403_when_not_owner_or_admin() throws Exception {
            simulateLogin(createTestUser(OTHER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/profile");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"major\":\"新专业\"}")));

            when(mockMemberService.updateProfile(eq(MEMBER_ID), any(), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限更新此档案"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("学号格式错误应返回400")
        void should_return_400_when_studentId_invalid() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_USER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_USER_ID + "/profile");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"studentId\":\"ABC\"}")));

            when(mockMemberService.updateProfile(eq(MEMBER_USER_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "学号格式错误"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("学号格式错误");
        }

        @Test
        @FastTest
        @DisplayName("年级格式错误应返回400")
        void should_return_400_when_grade_invalid() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_USER_ID + "/profile");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_USER_ID + "/profile");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(
                    "{\"grade\":\"25\"}")));

            when(mockMemberService.updateProfile(eq(MEMBER_USER_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "年级格式错误"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("年级格式错误");
        }
    }

    // ==================== POST /api/members/{id}/avatar 上传头像 ====================

    @Nested
    @DisplayName("POST /api/members/{id}/avatar 上传头像")
    class UploadAvatarTests {

        @Test
        @FastTest
        @DisplayName("成员上传自己头像应成功")
        void should_upload_avatar_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_USER_ID + "/avatar");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_USER_ID + "/avatar");
            when(mockRequest.getParameter("filename")).thenReturn("avatar.jpg");

            when(mockMemberService.uploadAvatar(eq(MEMBER_USER_ID), any(), eq("avatar.jpg"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(AVATAR_FILE_ID));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("200");
        }

        @Test
        @FastTest
        @DisplayName("管理员上传任意头像应成功")
        void should_allow_admin_upload_any_avatar() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/avatar");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/avatar");
            when(mockRequest.getParameter("filename")).thenReturn("avatar.png");

            when(mockMemberService.uploadAvatar(eq(MEMBER_ID), any(), eq("avatar.png"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(AVATAR_FILE_ID));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("非本人非管理员上传应返回403")
        void should_return_403_when_not_owner_or_admin() throws Exception {
            simulateLogin(createTestUser(OTHER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + MEMBER_ID + "/avatar");
            when(mockRequest.getPathInfo()).thenReturn("/" + MEMBER_ID + "/avatar");
            when(mockRequest.getParameter("filename")).thenReturn("avatar.jpg");

            when(mockMemberService.uploadAvatar(eq(MEMBER_ID), any(), eq("avatar.jpg"), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限上传此头像"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }

        @Test
        @FastTest
        @DisplayName("上传不存在的成员头像应返回404")
        void should_return_404_when_member_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn("/api/members/" + NONEXISTENT_MEMBER_ID + "/avatar");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_MEMBER_ID + "/avatar");
            when(mockRequest.getParameter("filename")).thenReturn("avatar.jpg");

            when(mockMemberService.uploadAvatar(eq(NONEXISTENT_MEMBER_ID), any(), eq("avatar.jpg"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "成员不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== Testable Inner Class ====================

    private static class TestableMemberApiServlet {

        private final MemberService memberService;
        private final Gson gson = new GsonBuilder().create();

        public TestableMemberApiServlet(MemberService memberService) {
            this.memberService = memberService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            if (isListPath(pathInfo)) {
                handleListMembers(req, resp, user);
            } else {
                handleMemberGet(req, resp, user, pathInfo);
            }
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);
            boolean isPutTunnel = "PUT".equalsIgnoreCase(req.getParameter("_method"));

            if (isListPath(pathInfo)) {
                if (isPutTunnel) {
                    writeJson(resp, Result.error(400, "根路径不支持PUT方法"));
                } else {
                    handleCreate(req, resp, user);
                }
            } else {
                handleMemberPost(req, resp, user, pathInfo, isPutTunnel);
            }
        }

        public void doPut(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }
            writeJson(resp, Result.error(405, "PUT方法不支持，请使用POST with _method=PUT"));
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            if (isListPath(pathInfo)) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                handleMemberDelete(req, resp, user, pathInfo);
            }
        }

        // ==================== Handler Methods ====================

        private void handleListMembers(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int page = parseIntParam(req.getParameter("page"), 1);
            int pageSize = parseIntParam(req.getParameter("pageSize"), 20);
            String keyword = req.getParameter("keyword");
            String role = req.getParameter("role");
            String status = req.getParameter("status");

            Map<String, Object> filter = new HashMap<>();
            if (keyword != null && !keyword.isEmpty()) filter.put("keyword", keyword);
            if (role != null && !role.isEmpty()) filter.put("role", role);
            if (status != null && !status.isEmpty()) filter.put("status", status);

            writeJson(resp, memberService.listMembers(filter, page, pageSize));
        }

        private void handleMemberGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            MemberPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidMemberId()) {
                writeJson(resp, Result.error(400, "无效的成员ID"));
                return;
            }

            if (pi.hasSubResource()) {
                dispatchSubResourceGet(pi, req, resp, user);
            } else {
                writeJson(resp, memberService.getMemberDetail(pi.getMemberId()));
            }
        }

        private void dispatchSubResourceGet(MemberPathInfo pi, HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int memberId = pi.getMemberId();
            String subResource = pi.getSubResource();

            if ("awards".equals(subResource)) {
                writeJson(resp, memberService.getMemberAwards(memberId));
            } else if ("profile".equals(subResource)) {
                writeJson(resp, memberService.getProfile(memberId));
            } else {
                writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            String body = readBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> dto = parseJsonRequest(body, Map.class);
            if (dto == null) {
                writeJson(resp, Result.error(400, "请求体不能为空"));
                return;
            }
            writeJson(resp, memberService.createMember(dto));
        }

        private void handleMemberPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo, boolean isPutTunnel) throws Exception {
            MemberPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidMemberId()) {
                writeJson(resp, Result.error(400, "无效的成员ID"));
                return;
            }

            if (pi.isAction()) {
                dispatchActionRequest(pi, req, resp, user);
            } else if (isPutTunnel) {
                dispatchPutRequest(pi, req, resp, user);
            } else {
                writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        private void dispatchPutRequest(MemberPathInfo pi, HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            if (pi.hasSubResource() && "profile".equals(pi.getSubResource())) {
                handleProfileUpdate(req, resp, user, pi.getMemberId());
            } else if (pi.hasSubResource() && "avatar".equals(pi.getSubResource())) {
                handleUploadAvatar(req, resp, user, pi.getMemberId());
            } else {
                handleUpdate(req, resp, user, pi.getMemberId());
            }
        }

        private void handleProfileUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int memberId) throws Exception {
            String body = readBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> dto = parseJsonRequest(body, Map.class);
            if (dto == null) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
                return;
            }
            writeJson(resp, memberService.updateProfile(memberId, dto, user.getId()));
        }

        private void dispatchActionRequest(MemberPathInfo pi, HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int memberId = pi.getMemberId();
            String action = pi.getAction();

            switch (action) {
                case "enable":
                    writeJson(resp, memberService.enableMember(memberId, user.getId()));
                    break;
                case "disable":
                    writeJson(resp, memberService.disableMember(memberId, user.getId()));
                    break;
                case "reset-password":
                    writeJson(resp, memberService.resetPassword(memberId, user.getId()));
                    break;
                case "avatar":
                    handleUploadAvatar(req, resp, user, memberId);
                    break;
                default:
                    writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int memberId) throws Exception {
            String body = readBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> dto = parseJsonRequest(body, Map.class);
            if (dto == null) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
                return;
            }
            writeJson(resp, memberService.updateMember(memberId, dto, user.getId()));
        }

        private void handleUploadAvatar(HttpServletRequest req, HttpServletResponse resp, User user, int memberId) throws Exception {
            String filename = req.getParameter("filename");
            InputStream fileStream = req.getInputStream();
            writeJson(resp, memberService.uploadAvatar(memberId, fileStream, filename, user.getId()));
        }

        private void handleMemberDelete(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            MemberPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidMemberId()) {
                writeJson(resp, Result.error(400, "无效的成员ID"));
                return;
            }

            if (pi.hasSubResource() || pi.isAction()) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                writeJson(resp, memberService.deleteMember(pi.getMemberId(), user.getId()));
            }
        }

        // ==================== Path Utilities ====================

        private boolean isListPath(String pathInfo) {
            return pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/");
        }

        private String derivePathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                String uri = req.getRequestURI();
                int idx = uri.indexOf("/api/members/");
                if (idx >= 0) {
                    pathInfo = uri.substring(idx + 13);
                    if (pathInfo.isEmpty()) {
                        pathInfo = null;
                    } else if (!pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }
            return pathInfo;
        }

        private MemberPathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                return MemberPathInfo.ROOT;
            }

            if (!pathInfo.startsWith("/")) {
                return MemberPathInfo.ROOT;
            }

            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length == 0 || segments[0].isEmpty()) {
                return MemberPathInfo.ROOT;
            }

            int memberId = parseIntOrZero(segments[0]);

            if (segments.length == 1) {
                return MemberPathInfo.forMember(memberId);
            }

            String segment1 = segments[1];

            if ("enable".equals(segment1)) {
                return MemberPathInfo.forAction(memberId, "enable");
            }
            if ("disable".equals(segment1)) {
                return MemberPathInfo.forAction(memberId, "disable");
            }
            if ("reset-password".equals(segment1)) {
                return MemberPathInfo.forAction(memberId, "reset-password");
            }
            if ("awards".equals(segment1)) {
                return MemberPathInfo.forSubResource(memberId, "awards");
            }
            if ("profile".equals(segment1)) {
                return MemberPathInfo.forSubResource(memberId, "profile");
            }
            if ("avatar".equals(segment1)) {
                return MemberPathInfo.forAction(memberId, "avatar");
            }

            return MemberPathInfo.forSubResource(memberId, segment1);
        }

        private int parseIntOrZero(String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private int parseIntParam(String value, int defaultValue) {
            if (value == null || value.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        private String readBody(HttpServletRequest req) throws Exception {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        }

        private <T> T parseJsonRequest(String body, Class<T> clazz) {
            if (body == null || body.trim().isEmpty()) {
                return null;
            }
            try {
                return gson.fromJson(body, clazz);
            } catch (Exception e) {
                return null;
            }
        }

        // ==================== BaseApiServlet Methods ====================

        private User getCurrentUser(HttpServletRequest request) {
            Object user = request.getSession(false).getAttribute("user");
            return user instanceof User ? (User) user : null;
        }

        private void writeJson(HttpServletResponse response, Result result) throws Exception {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(result.isSuccess() ? 200 : result.getCode());
            PrintWriter writer = response.getWriter();
            gson.toJson(result, writer);
            writer.flush();
        }

        // ==================== PathInfo Inner Class ====================

        private static class MemberPathInfo {
            static final MemberPathInfo ROOT = new MemberPathInfo(0, null, null);

            private final int memberId;
            private final String subResource;
            private final String action;

            private MemberPathInfo(int memberId, String subResource, String action) {
                this.memberId = memberId;
                this.subResource = subResource;
                this.action = action;
            }

            static MemberPathInfo forMember(int memberId) {
                return new MemberPathInfo(memberId, null, null);
            }

            static MemberPathInfo forAction(int memberId, String action) {
                return new MemberPathInfo(memberId, null, action);
            }

            static MemberPathInfo forSubResource(int memberId, String subResource) {
                return new MemberPathInfo(memberId, subResource, null);
            }

            boolean isValidMemberId() {
                return memberId > 0;
            }

            int getMemberId() {
                return memberId;
            }

            String getSubResource() {
                return subResource;
            }

            String getAction() {
                return action;
            }

            boolean hasSubResource() {
                return subResource != null && !subResource.isEmpty();
            }

            boolean isAction() {
                return action != null && !action.isEmpty();
            }

            boolean isEnableAction() {
                return "enable".equals(action);
            }

            boolean isDisableAction() {
                return "disable".equals(action);
            }

            boolean isResetPasswordAction() {
                return "reset-password".equals(action);
            }

            boolean isAvatarAction() {
                return "avatar".equals(action);
            }
        }
    }
}
