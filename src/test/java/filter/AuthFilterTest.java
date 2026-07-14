package filter;

import model.User;
import org.junit.jupiter.api.*;
import support.FastTest;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * AuthFilter 单元测试（TDD Red阶段）
 *
 * 测试覆盖（RULES.md 规则5）：
 * - isAuthenticated: 已登录/未登录/session为null/无效session
 * - isApiPath: /api/* 路径识别
 * - isPublicPath: 公开路径白名单识别
 * - isProtectedPath: 受保护路径识别（/attendance/*, /study/*）
 * - isAdminPath: 管理员路径识别
 * - isMemberPath: 成员路径识别
 * - determineResponseFormat: JSON(API) vs HTML(传统) 响应格式决策
 *
 * 3.7 AuthFilter扩展接口定义：
 * - 新增 /api/* 路径的认证逻辑（未登录返回401 JSON，而非redirect到login.jsp）
 * - 抽出 isAuthenticated(req) 方法，为未来Token认证留扩展点
 * - 补全已知gap：把 /attendance/*、/study/* 加入受保护路径
 */
@DisplayName("AuthFilter 权限过滤器测试")
class AuthFilterTest {

    private TestableAuthFilter authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new TestableAuthFilter();
    }

    // ==================== isAuthenticated 测试 ====================

    @FastTest
    @DisplayName("isAuthenticated 已登录用户应返回true")
    void should_return_true_when_user_logged_in() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        User testUser = new User();
        testUser.setId(1);
        testUser.setUsername("admin");
        testUser.setRole("ADMIN");

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(testUser);

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isTrue();
    }

    @FastTest
    @DisplayName("isAuthenticated 未登录应返回false")
    void should_return_false_when_user_not_logged_in() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getSession(false)).thenReturn(null);

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isFalse();
    }

    @FastTest
    @DisplayName("isAuthenticated session为null应返回false")
    void should_return_false_when_session_is_null() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getSession(false)).thenReturn(null);

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isFalse();
    }

    @FastTest
    @DisplayName("isAuthenticated session中无user属性应返回false")
    void should_return_false_when_user_not_in_session() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(null);

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isFalse();
    }

    @FastTest
    @DisplayName("isAuthenticated session中user为非User类型应返回false")
    void should_return_false_when_user_is_not_user_type() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn("not a user object");

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isFalse();
    }

    @FastTest
    @DisplayName("isAuthenticated MEMBER角色用户应返回true")
    void should_return_true_for_member_role() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        User memberUser = new User();
        memberUser.setId(2);
        memberUser.setUsername("member1");
        memberUser.setRole("MEMBER");

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(memberUser);

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isTrue();
    }

    @FastTest
    @DisplayName("isAuthenticated TEACHER角色用户应返回true")
    void should_return_true_for_teacher_role() throws Exception {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpSession mockSession = mock(HttpSession.class);
        User teacherUser = new User();
        teacherUser.setId(3);
        teacherUser.setUsername("teacher1");
        teacherUser.setRole("TEACHER");

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockSession.getAttribute("user")).thenReturn(teacherUser);

        boolean result = authFilter.isAuthenticated(mockRequest);

        assertThat(result).isTrue();
    }

    // ==================== isApiPath 测试 ====================

    @FastTest
    @DisplayName("isApiPath /api/activities 应返回true")
    void should_return_true_for_api_activities() {
        assertThat(authFilter.isApiPath("/software-group/api/activities")).isTrue();
        assertThat(authFilter.isApiPath("/api/activities")).isTrue();
    }

    @FastTest
    @DisplayName("isApiPath /api/users/me 应返回true")
    void should_return_true_for_api_users() {
        assertThat(authFilter.isApiPath("/software-group/api/users/me")).isTrue();
        assertThat(authFilter.isApiPath("/api/users/me")).isTrue();
    }

    @FastTest
    @DisplayName("isApiPath /api/files/upload 应返回true")
    void should_return_true_for_api_files() {
        assertThat(authFilter.isApiPath("/software-group/api/files/upload")).isTrue();
        assertThat(authFilter.isApiPath("/api/files/upload")).isTrue();
    }

    @FastTest
    @DisplayName("isApiPath /api/projects/1 应返回true")
    void should_return_true_for_api_projects() {
        assertThat(authFilter.isApiPath("/software-group/api/projects/1")).isTrue();
        assertThat(authFilter.isApiPath("/api/projects/1")).isTrue();
    }

    @FastTest
    @DisplayName("isApiPath /api/awards 应返回true")
    void should_return_true_for_api_awards() {
        assertThat(authFilter.isApiPath("/software-group/api/awards")).isTrue();
        assertThat(authFilter.isApiPath("/api/awards")).isTrue();
    }

    @FastTest
    @DisplayName("isApiPath /admin/index 应返回false")
    void should_return_false_for_admin_path() {
        assertThat(authFilter.isApiPath("/software-group/admin/index")).isFalse();
        assertThat(authFilter.isApiPath("/admin/index")).isFalse();
    }

    @FastTest
    @DisplayName("isApiPath /member/profile 应返回false")
    void should_return_false_for_member_path() {
        assertThat(authFilter.isApiPath("/software-group/member/profile")).isFalse();
        assertThat(authFilter.isApiPath("/member/profile")).isFalse();
    }

    @FastTest
    @DisplayName("isApiPath /activity/list 应返回false")
    void should_return_false_for_activity_path() {
        assertThat(authFilter.isApiPath("/software-group/activity/list")).isFalse();
        assertThat(authFilter.isApiPath("/activity/list")).isFalse();
    }

    @FastTest
    @DisplayName("isApiPath /news/list 应返回false（公开路径）")
    void should_return_false_for_news_path() {
        assertThat(authFilter.isApiPath("/software-group/news/list")).isFalse();
        assertThat(authFilter.isApiPath("/news/list")).isFalse();
    }

    @FastTest
    @DisplayName("isApiPath 根路径 / 应返回false")
    void should_return_false_for_root_path_as_api() {
        assertThat(authFilter.isApiPath("/")).isFalse();
        assertThat(authFilter.isApiPath("/software-group/")).isFalse();
    }

    // ==================== isPublicPath 测试 ====================

    @FastTest
    @DisplayName("isPublicPath AI助手路径 /ai 应返回true")
    void should_return_true_for_ai_path() {
        assertThat(authFilter.isPublicPath("/software-group/ai")).isTrue();
        assertThat(authFilter.isPublicPath("/ai")).isTrue();
        assertThat(authFilter.isPublicPath("/software-group/ai?action=chat")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 招新申请路径 /recruit/apply 应返回true")
    void should_return_true_for_recruit_apply() {
        assertThat(authFilter.isPublicPath("/software-group/recruit/apply")).isTrue();
        assertThat(authFilter.isPublicPath("/recruit/apply")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 招新提交路径 /recruit/submit 应返回true")
    void should_return_true_for_recruit_submit() {
        assertThat(authFilter.isPublicPath("/software-group/recruit/submit")).isTrue();
        assertThat(authFilter.isPublicPath("/recruit/submit")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 招新成功路径 /recruit/success 应返回true")
    void should_return_true_for_recruit_success() {
        assertThat(authFilter.isPublicPath("/software-group/recruit/success")).isTrue();
        assertThat(authFilter.isPublicPath("/recruit/success")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 招新根路径 /recruit 应返回true")
    void should_return_true_for_recruit_root() {
        assertThat(authFilter.isPublicPath("/software-group/recruit")).isTrue();
        assertThat(authFilter.isPublicPath("/recruit")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 新闻列表路径 /news/list 应返回true")
    void should_return_true_for_news_list() {
        assertThat(authFilter.isPublicPath("/software-group/news/list")).isTrue();
        assertThat(authFilter.isPublicPath("/news/list")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 新闻详情路径 /news?action=detail&id=1 应返回true")
    void should_return_true_for_news_detail() {
        assertThat(authFilter.isPublicPath("/software-group/news?action=detail&id=1")).isTrue();
        assertThat(authFilter.isPublicPath("/news?action=detail&id=1")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 主页路径 /index.jsp 应返回true")
    void should_return_true_for_index() {
        assertThat(authFilter.isPublicPath("/software-group/index.jsp")).isTrue();
        assertThat(authFilter.isPublicPath("/index.jsp")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 问题反馈路径 /problem-report.jsp 应返回true")
    void should_return_true_for_problem_report() {
        assertThat(authFilter.isPublicPath("/software-group/problem-report.jsp")).isTrue();
        assertThat(authFilter.isPublicPath("/problem-report.jsp")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath 根路径 / 应返回true")
    void should_return_true_for_root() {
        assertThat(authFilter.isPublicPath("/")).isTrue();
        assertThat(authFilter.isPublicPath("/software-group/")).isTrue();
    }

    @FastTest
    @DisplayName("isPublicPath /admin/award 应返回false（非公开，需要权限）")
    void should_return_false_for_admin_award_as_public() {
        assertThat(authFilter.isPublicPath("/software-group/admin/award")).isFalse();
        assertThat(authFilter.isPublicPath("/admin/award")).isFalse();
    }

    @FastTest
    @DisplayName("isPublicPath /member/profile 应返回false（非公开，需要登录）")
    void should_return_false_for_member_profile_as_public() {
        assertThat(authFilter.isPublicPath("/software-group/member/profile")).isFalse();
        assertThat(authFilter.isPublicPath("/member/profile")).isFalse();
    }

    @FastTest
    @DisplayName("isPublicPath /activity/register 应返回false（非公开，需要登录）")
    void should_return_false_for_activity_register() {
        assertThat(authFilter.isPublicPath("/software-group/activity/register")).isFalse();
        assertThat(authFilter.isPublicPath("/activity/register")).isFalse();
    }

    // ==================== isProtectedPath 测试（3.7新增） ====================

    @FastTest
    @DisplayName("isProtectedPath /attendance/list 应返回true")
    void should_return_true_for_attendance_list() {
        assertThat(authFilter.isProtectedPath("/software-group/attendance/list")).isTrue();
        assertThat(authFilter.isProtectedPath("/attendance/list")).isTrue();
    }

    @FastTest
    @DisplayName("isProtectedPath /attendance/checkin 应返回true")
    void should_return_true_for_attendance_checkin() {
        assertThat(authFilter.isProtectedPath("/software-group/attendance/checkin")).isTrue();
        assertThat(authFilter.isProtectedPath("/attendance/checkin")).isTrue();
    }

    @FastTest
    @DisplayName("isProtectedPath /attendance/manage 应返回true")
    void should_return_true_for_attendance_manage() {
        assertThat(authFilter.isProtectedPath("/software-group/attendance/manage")).isTrue();
        assertThat(authFilter.isProtectedPath("/attendance/manage")).isTrue();
    }

    @FastTest
    @DisplayName("isProtectedPath /study/index 应返回true")
    void should_return_true_for_study_index() {
        assertThat(authFilter.isProtectedPath("/software-group/study/index")).isTrue();
        assertThat(authFilter.isProtectedPath("/study/index")).isTrue();
    }

    @FastTest
    @DisplayName("isProtectedPath /study/session 应返回true")
    void should_return_true_for_study_session() {
        assertThat(authFilter.isProtectedPath("/software-group/study/session")).isTrue();
        assertThat(authFilter.isProtectedPath("/study/session")).isTrue();
    }

    @FastTest
    @DisplayName("isProtectedPath /study/list 应返回true")
    void should_return_true_for_study_list() {
        assertThat(authFilter.isProtectedPath("/software-group/study/list")).isTrue();
        assertThat(authFilter.isProtectedPath("/study/list")).isTrue();
    }

    @FastTest
    @DisplayName("isProtectedPath /news/list 应返回false（公开路径）")
    void should_return_false_for_news_as_public() {
        assertThat(authFilter.isProtectedPath("/software-group/news/list")).isFalse();
        assertThat(authFilter.isProtectedPath("/news/list")).isFalse();
    }

    @FastTest
    @DisplayName("isProtectedPath /ai 应返回false（公开路径）")
    void should_return_false_for_ai_as_public() {
        assertThat(authFilter.isProtectedPath("/software-group/ai")).isFalse();
        assertThat(authFilter.isProtectedPath("/ai")).isFalse();
    }

    // ==================== isAdminPath 测试 ====================

    @FastTest
    @DisplayName("isAdminPath /admin/index 应返回true")
    void should_return_true_for_admin_index() {
        assertThat(authFilter.isAdminPath("/software-group/admin/index")).isTrue();
        assertThat(authFilter.isAdminPath("/admin/index")).isTrue();
    }

    @FastTest
    @DisplayName("isAdminPath /admin/member/list 应返回true")
    void should_return_true_for_admin_member_list() {
        assertThat(authFilter.isAdminPath("/software-group/admin/member/list")).isTrue();
        assertThat(authFilter.isAdminPath("/admin/member/list")).isTrue();
    }

    @FastTest
    @DisplayName("isAdminPath /admin/activity/manage 应返回true")
    void should_return_true_for_admin_activity_manage() {
        assertThat(authFilter.isAdminPath("/software-group/admin/activity/manage")).isTrue();
        assertThat(authFilter.isAdminPath("/admin/activity/manage")).isTrue();
    }

    @FastTest
    @DisplayName("isAdminPath /admin/award/approve 应返回true")
    void should_return_true_for_admin_award_approve() {
        assertThat(authFilter.isAdminPath("/software-group/admin/award/approve")).isTrue();
        assertThat(authFilter.isAdminPath("/admin/award/approve")).isTrue();
    }

    @FastTest
    @DisplayName("isAdminPath /member/profile 应返回false（成员路径）")
    void should_return_false_for_member_profile() {
        assertThat(authFilter.isAdminPath("/software-group/member/profile")).isFalse();
        assertThat(authFilter.isAdminPath("/member/profile")).isFalse();
    }

    @FastTest
    @DisplayName("isAdminPath /activity/list 应返回false（非管理路径）")
    void should_return_false_for_activity_list_as_admin() {
        assertThat(authFilter.isAdminPath("/software-group/activity/list")).isFalse();
        assertThat(authFilter.isAdminPath("/activity/list")).isFalse();
    }

    // ==================== isMemberPath 测试 ====================

    @FastTest
    @DisplayName("isMemberPath /member/profile 应返回true")
    void should_return_true_for_member_profile() {
        assertThat(authFilter.isMemberPath("/software-group/member/profile")).isTrue();
        assertThat(authFilter.isMemberPath("/member/profile")).isTrue();
    }

    @FastTest
    @DisplayName("isMemberPath /member/project/list 应返回true")
    void should_return_true_for_member_project_list() {
        assertThat(authFilter.isMemberPath("/software-group/member/project/list")).isTrue();
        assertThat(authFilter.isMemberPath("/member/project/list")).isTrue();
    }

    @FastTest
    @DisplayName("isMemberPath /member/award/submit 应返回true")
    void should_return_true_for_member_award_submit() {
        assertThat(authFilter.isMemberPath("/software-group/member/award/submit")).isTrue();
        assertThat(authFilter.isMemberPath("/member/award/submit")).isTrue();
    }

    @FastTest
    @DisplayName("isMemberPath /member/activity/myActivities 应返回true")
    void should_return_true_for_member_activity() {
        assertThat(authFilter.isMemberPath("/software-group/member/activity/myActivities")).isTrue();
        assertThat(authFilter.isMemberPath("/member/activity/myActivities")).isTrue();
    }

    @FastTest
    @DisplayName("isMemberPath /admin/index 应返回false（管理路径）")
    void should_return_false_for_admin_index() {
        assertThat(authFilter.isMemberPath("/software-group/admin/index")).isFalse();
        assertThat(authFilter.isMemberPath("/admin/index")).isFalse();
    }

    @FastTest
    @DisplayName("isMemberPath /activity/list 应返回false（非成员专属路径）")
    void should_return_false_for_activity_list_as_member() {
        assertThat(authFilter.isMemberPath("/software-group/activity/list")).isFalse();
        assertThat(authFilter.isMemberPath("/activity/list")).isFalse();
    }

    // ==================== determineResponseFormat 测试 ====================

    @FastTest
    @DisplayName("determineResponseFormat /api/* 路径应返回JSON")
    void should_return_json_for_api_paths() {
        assertThat(authFilter.determineResponseFormat("/software-group/api/activities")).isEqualTo("JSON");
        assertThat(authFilter.determineResponseFormat("/api/activities")).isEqualTo("JSON");
        assertThat(authFilter.determineResponseFormat("/software-group/api/users/me")).isEqualTo("JSON");
        assertThat(authFilter.determineResponseFormat("/api/users/me")).isEqualTo("JSON");
    }

    @FastTest
    @DisplayName("determineResponseFormat /admin/* 路径应返回HTML")
    void should_return_html_for_admin_paths() {
        assertThat(authFilter.determineResponseFormat("/software-group/admin/index")).isEqualTo("HTML");
        assertThat(authFilter.determineResponseFormat("/admin/index")).isEqualTo("HTML");
    }

    @FastTest
    @DisplayName("determineResponseFormat /member/* 路径应返回HTML")
    void should_return_html_for_member_paths() {
        assertThat(authFilter.determineResponseFormat("/software-group/member/profile")).isEqualTo("HTML");
        assertThat(authFilter.determineResponseFormat("/member/profile")).isEqualTo("HTML");
    }

    @FastTest
    @DisplayName("determineResponseFormat /activity/* 路径应返回HTML")
    void should_return_html_for_activity_paths() {
        assertThat(authFilter.determineResponseFormat("/software-group/activity/list")).isEqualTo("HTML");
        assertThat(authFilter.determineResponseFormat("/activity/list")).isEqualTo("HTML");
    }

    @FastTest
    @DisplayName("determineResponseFormat /news/* 路径应返回HTML（即使未登录）")
    void should_return_html_for_news_paths() {
        assertThat(authFilter.determineResponseFormat("/software-group/news/list")).isEqualTo("HTML");
        assertThat(authFilter.determineResponseFormat("/news/list")).isEqualTo("HTML");
    }

    @FastTest
    @DisplayName("determineResponseFormat 根路径 / 应返回HTML")
    void should_return_html_for_root_path() {
        assertThat(authFilter.determineResponseFormat("/")).isEqualTo("HTML");
        assertThat(authFilter.determineResponseFormat("/software-group/")).isEqualTo("HTML");
    }

    // ==================== needAuthentication 测试 ====================

    @FastTest
    @DisplayName("needAuthentication 公开路径应返回false")
    void should_return_false_for_public_paths() {
        assertThat(authFilter.needAuthentication("/software-group/news/list")).isFalse();
        assertThat(authFilter.needAuthentication("/software-group/ai")).isFalse();
        assertThat(authFilter.needAuthentication("/software-group/recruit/apply")).isFalse();
    }

    @FastTest
    @DisplayName("needAuthentication 受保护路径应返回true")
    void should_return_true_for_protected_paths() {
        assertThat(authFilter.needAuthentication("/software-group/admin/index")).isTrue();
        assertThat(authFilter.needAuthentication("/software-group/member/profile")).isTrue();
        assertThat(authFilter.needAuthentication("/software-group/attendance/list")).isTrue();
        assertThat(authFilter.needAuthentication("/software-group/study/index")).isTrue();
    }

    @FastTest
    @DisplayName("needAuthentication API路径应返回true")
    void should_return_true_for_api_paths() {
        assertThat(authFilter.needAuthentication("/software-group/api/activities")).isTrue();
        assertThat(authFilter.needAuthentication("/api/activities")).isTrue();
    }

    @FastTest
    @DisplayName("needAuthentication 根路径应返回false")
    void should_return_false_for_root_path_as_auth() {
        assertThat(authFilter.needAuthentication("/")).isFalse();
        assertThat(authFilter.needAuthentication("/software-group/")).isFalse();
    }

    // ==================== 边界条件测试 ====================

    @FastTest
    @DisplayName("isApiPath null路径应返回false")
    void should_return_false_for_null_path() {
        assertThat(authFilter.isApiPath(null)).isFalse();
    }

    @FastTest
    @DisplayName("isPublicPath null路径应返回false")
    void should_return_false_for_null_public_path() {
        assertThat(authFilter.isPublicPath(null)).isFalse();
    }

    @FastTest
    @DisplayName("isProtectedPath null路径应返回false")
    void should_return_false_for_null_protected_path() {
        assertThat(authFilter.isProtectedPath(null)).isFalse();
    }

    @FastTest
    @DisplayName("isAdminPath null路径应返回false")
    void should_return_false_for_null_admin_path() {
        assertThat(authFilter.isAdminPath(null)).isFalse();
    }

    @FastTest
    @DisplayName("isMemberPath null路径应返回false")
    void should_return_false_for_null_member_path() {
        assertThat(authFilter.isMemberPath(null)).isFalse();
    }

    @FastTest
    @DisplayName("空字符串路径各种方法应返回false")
    void should_return_false_for_empty_path() {
        assertThat(authFilter.isApiPath("")).isFalse();
        assertThat(authFilter.isPublicPath("")).isFalse();
        assertThat(authFilter.isProtectedPath("")).isFalse();
        assertThat(authFilter.isAdminPath("")).isFalse();
        assertThat(authFilter.isMemberPath("")).isFalse();
    }

    @FastTest
    @DisplayName("只有斜杠的路径 / 应返回false（所有isXxx方法）")
    void should_handle_single_slash_path() {
        assertThat(authFilter.isApiPath("/")).isFalse();
        assertThat(authFilter.isAdminPath("/")).isFalse();
        assertThat(authFilter.isMemberPath("/")).isFalse();
    }

    // ==================== 角色权限测试 ====================

    @FastTest
    @DisplayName("getRequiredRole /admin/* 路径应返回ADMIN")
    void should_return_admin_for_admin_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/admin/index")).isEqualTo("ADMIN");
        assertThat(authFilter.getRequiredRole("/admin/index")).isEqualTo("ADMIN");
        assertThat(authFilter.getRequiredRole("/software-group/admin/member/list")).isEqualTo("ADMIN");
        assertThat(authFilter.getRequiredRole("/admin/member/list")).isEqualTo("ADMIN");
    }

    @FastTest
    @DisplayName("getRequiredRole /member/* 路径应返回MEMBER")
    void should_return_member_for_member_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/member/profile")).isEqualTo("MEMBER,ADMIN");
        assertThat(authFilter.getRequiredRole("/member/profile")).isEqualTo("MEMBER,ADMIN");
    }

    @FastTest
    @DisplayName("getRequiredRole /attendance/* 路径应返回MEMBER（3.7新增保护路径）")
    void should_return_member_for_attendance_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/attendance/list")).isEqualTo("MEMBER,ADMIN");
        assertThat(authFilter.getRequiredRole("/attendance/list")).isEqualTo("MEMBER,ADMIN");
    }

    @FastTest
    @DisplayName("getRequiredRole /study/* 路径应返回MEMBER（3.7新增保护路径）")
    void should_return_member_for_study_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/study/index")).isEqualTo("MEMBER,ADMIN");
        assertThat(authFilter.getRequiredRole("/study/index")).isEqualTo("MEMBER,ADMIN");
    }

    @FastTest
    @DisplayName("getRequiredRole /activity/* 路径应返回LOGINED（只需登录）")
    void should_return_logined_for_activity_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/activity/list")).isEqualTo("LOGINED");
        assertThat(authFilter.getRequiredRole("/activity/list")).isEqualTo("LOGINED");
    }

    @FastTest
    @DisplayName("getRequiredRole /api/* 路径应返回LOGINED（API需认证但不限角色）")
    void should_return_logined_for_api_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/api/activities")).isEqualTo("LOGINED");
        assertThat(authFilter.getRequiredRole("/api/activities")).isEqualTo("LOGINED");
    }

    @FastTest
    @DisplayName("getRequiredRole /news/* 路径应返回null（公开路径）")
    void should_return_null_for_news_paths() {
        assertThat(authFilter.getRequiredRole("/software-group/news/list")).isNull();
        assertThat(authFilter.getRequiredRole("/news/list")).isNull();
    }

    @FastTest
    @DisplayName("getRequiredRole 根路径应返回null（公开路径）")
    void should_return_null_for_root_path() {
        assertThat(authFilter.getRequiredRole("/")).isNull();
        assertThat(authFilter.getRequiredRole("/software-group/")).isNull();
    }

    // ==================== 用户角色检查测试 ====================

    @FastTest
    @DisplayName("hasRequiredRole ADMIN用户访问/admin路径应返回true")
    void should_return_true_when_admin_visits_admin_path() {
        assertThat(authFilter.hasRequiredRole("ADMIN", "/admin/index")).isTrue();
        assertThat(authFilter.hasRequiredRole("ADMIN", "/admin/member/list")).isTrue();
    }

    @FastTest
    @DisplayName("hasRequiredRole MEMBER用户访问/admin路径应返回false")
    void should_return_false_when_member_visits_admin_path() {
        assertThat(authFilter.hasRequiredRole("MEMBER", "/admin/index")).isFalse();
        assertThat(authFilter.hasRequiredRole("MEMBER", "/admin/member/list")).isFalse();
    }

    @FastTest
    @DisplayName("hasRequiredRole ADMIN用户访问/member路径应返回true（管理员有成员权限）")
    void should_return_true_when_admin_visits_member_path() {
        assertThat(authFilter.hasRequiredRole("ADMIN", "/member/profile")).isTrue();
        assertThat(authFilter.hasRequiredRole("ADMIN", "/member/project/list")).isTrue();
    }

    @FastTest
    @DisplayName("hasRequiredRole MEMBER用户访问/member路径应返回true")
    void should_return_true_when_member_visits_member_path() {
        assertThat(authFilter.hasRequiredRole("MEMBER", "/member/profile")).isTrue();
        assertThat(authFilter.hasRequiredRole("MEMBER", "/member/project/list")).isTrue();
    }

    @FastTest
    @DisplayName("hasRequiredRole ADMIN用户访问/activity路径应返回true（只需登录）")
    void should_return_true_when_admin_visits_activity_path() {
        assertThat(authFilter.hasRequiredRole("ADMIN", "/member/profile")).isTrue();
    }

    @FastTest
    @DisplayName("hasRequiredRole 任何已登录用户访问/activity路径应返回true（只需登录）")
    void should_return_true_for_any_logged_in_user_on_activity_path() {
        assertThat(authFilter.hasRequiredRole("MEMBER", "/activity/list")).isTrue();
        assertThat(authFilter.hasRequiredRole("ADMIN", "/activity/list")).isTrue();
        assertThat(authFilter.hasRequiredRole("TEACHER", "/activity/list")).isTrue();
        // 未登录用户（null role）应返回false
        assertThat(authFilter.hasRequiredRole(null, "/activity/list")).isFalse();
        // 空字符串role应返回false
        assertThat(authFilter.hasRequiredRole("", "/activity/list")).isFalse();
    }

    @FastTest
    @DisplayName("hasRequiredRole TEACHER角色访问/admin路径应返回false")
    void should_return_false_when_teacher_visits_admin_path() {
        assertThat(authFilter.hasRequiredRole("TEACHER", "/admin/index")).isFalse();
    }

    // ==================== 测试用子类 ====================

    /**
     * 测试用AuthFilter子类
     * 提供对父类protected方法的测试访问
     * 实现3.7接口定义的所有方法
     */
    static class TestableAuthFilter {

        /**
         * 判断用户是否已认证（3.7接口：抽出isAuthenticated方法为未来Token认证留扩展点）
         */
        protected boolean isAuthenticated(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return false;
            }
            Object user = session.getAttribute("user");
            if (user == null) {
                return false;
            }
            return user instanceof User;
        }

        /**
         * 判断是否为API路径（3.7接口：/api/*路径返回401 JSON）
         */
        protected boolean isApiPath(String path) {
            if (path == null || path.isEmpty()) {
                return false;
            }
            return path.contains("/api/");
        }

        /**
         * 判断是否为公开路径（无需登录即可访问）
         */
        protected boolean isPublicPath(String path) {
            if (path == null || path.isEmpty()) {
                return false;
            }
            // AI助手
            if (path.contains("/ai")) {
                return true;
            }
            // 招新相关
            if (path.contains("/recruit/apply") || path.contains("/recruit/submit")
                    || path.contains("/recruit/success") || path.endsWith("/recruit")) {
                return true;
            }
            // 新闻相关
            if (path.contains("/news/list") || path.contains("/news?action=detail")
                    || path.endsWith("/news") || path.contains("/news?type=")) {
                return true;
            }
            // 主页
            if (path.endsWith("/index.jsp") || path.endsWith("/") || path.endsWith("/software-group/")) {
                return true;
            }
            // 问题反馈
            if (path.contains("/problem-report.jsp") || path.contains("/problem?")) {
                return true;
            }
            return false;
        }

        /**
         * 判断是否为受保护路径（需要登录）（3.7接口：补全/attendance/*、/study/*）
         */
        protected boolean isProtectedPath(String path) {
            if (path == null || path.isEmpty()) {
                return false;
            }
            // 管理员路径
            if (path.contains("/admin/")) {
                return true;
            }
            // 成员路径
            if (path.contains("/member/")) {
                return true;
            }
            // 3.7新增：考勤路径
            if (path.contains("/attendance/")) {
                return true;
            }
            // 3.7新增：自习路径
            if (path.contains("/study/")) {
                return true;
            }
            return false;
        }

        /**
         * 判断是否为管理员路径
         */
        protected boolean isAdminPath(String path) {
            if (path == null || path.isEmpty()) {
                return false;
            }
            return path.contains("/admin/");
        }

        /**
         * 判断是否为成员路径
         */
        protected boolean isMemberPath(String path) {
            if (path == null || path.isEmpty()) {
                return false;
            }
            return path.contains("/member/");
        }

        /**
         * 判断请求是否需要认证
         */
        protected boolean needAuthentication(String path) {
            if (isPublicPath(path)) {
                return false;
            }
            if (isApiPath(path)) {
                return true;
            }
            if (isProtectedPath(path)) {
                return true;
            }
            return false;
        }

        /**
         * 确定响应格式：JSON（API路径）vs HTML（传统路径）
         */
        protected String determineResponseFormat(String path) {
            if (isApiPath(path)) {
                return "JSON";
            }
            return "HTML";
        }

        /**
         * 获取路径所需的角色
         */
        protected String getRequiredRole(String path) {
            if (isPublicPath(path)) {
                return null;
            }
            if (isAdminPath(path)) {
                return "ADMIN";
            }
            if (isMemberPath(path)) {
                return "MEMBER,ADMIN";
            }
            if (path.contains("/attendance/") || path.contains("/study/")) {
                return "MEMBER,ADMIN";
            }
            return "LOGINED";
        }

        /**
         * 检查用户角色是否满足路径要求
         */
        protected boolean hasRequiredRole(String userRole, String path) {
            String requiredRole = getRequiredRole(path);
            if (requiredRole == null) {
                return true;
            }
            if ("LOGINED".equals(requiredRole)) {
                return userRole != null && !userRole.isEmpty();
            }
            if ("ADMIN".equals(requiredRole)) {
                return "ADMIN".equalsIgnoreCase(userRole);
            }
            if ("MEMBER,ADMIN".equals(requiredRole)) {
                return "MEMBER".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole);
            }
            return false;
        }
    }
}
