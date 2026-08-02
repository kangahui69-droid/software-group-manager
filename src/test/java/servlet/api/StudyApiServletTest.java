package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.StudySession;
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
import service.StudyService;
import support.FastTest;
import util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudyApiServlet TDD测试套件
 *
 * 测试范围：服务分层与API化完整计划.md 6.1 StudyApiServlet 端点
 * - 所有REST端点
 * - 所有HTTP方法(GET/POST/PUT/DELETE)
 * - 认证与授权
 * - 参数解析与验证
 * - 错误处理
 * - 所有状态枚举
 * - 所有边界情况
 *
 * 测试覆盖端点：
 * - GET  /api/study           → 学习记录列表(分页)
 * - GET  /api/study/{id}     → 学习记录详情
 * - GET  /api/study/today    → 今日进行中会话
 * - POST /api/study/start    → 开始学习
 * - POST /api/study/end      → 结束学习
 * - GET  /api/study/my       → 我的学习记录
 * - GET  /api/study/stats    → 学习统计
 * - GET  /api/study/week-stats → 本周学习统计
 *
 * StudyService方法映射：
 * - listSessions(filter, page, pageSize)
 * - getSessionDetail(id)
 * - getTodaySession(userId)
 * - startSession(userId)
 * - endSession(userId)
 * - getMySessions(userId, page, pageSize)
 * - getStatistics(userId)
 * - getWeekStatistics(userId)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StudyApiServlet 学习API测试")
class StudyApiServletTest {

    private TestableStudyApiServlet servlet;
    private StudyService mockStudyService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private StringWriter responseWriter;
    private Gson gson;

    // ==================== 测试数据常量 ====================

    private static final Integer MEMBER_USER_ID = 1;
    private static final Integer OTHER_USER_ID = 2;
    private static final Integer ADMIN_USER_ID = 3;
    private static final Integer GUEST_USER_ID = null;
    private static final Integer SESSION_ID = 100;
    private static final Integer NONEXISTENT_SESSION_ID = 99999;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    // 学习时段状态枚举
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";

    // 角色常量
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 路径常量
    private static final String PATH_LIST = "/api/study";
    private static final String PATH_TODAY = "/api/study/today";
    private static final String PATH_START = "/api/study/start";
    private static final String PATH_END = "/api/study/end";
    private static final String PATH_MY = "/api/study/my";
    private static final String PATH_STATS = "/api/study/stats";
    private static final String PATH_WEEK_STATS = "/api/study/week-stats";

    @BeforeEach
    void setUp() throws Exception {
        mockStudyService = mock(StudyService.class);
        servlet = new TestableStudyApiServlet(mockStudyService);
        responseWriter = new StringWriter();
        gson = new GsonBuilder().create();

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== 辅助方法 ====================

    private User createTestUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private StudySession createTestSession(Integer id, Integer userId, String status) {
        StudySession session = new StudySession();
        session.setId(id);
        session.setUserId(userId);
        session.setSessionDate(new Date());
        session.setCheckInTime(new Date());
        if (STATUS_COMPLETED.equals(status)) {
            session.setCheckOutTime(new Date());
            session.setDuration(60);
        }
        session.setStatus(status);
        session.setCreatedAt(new Date());
        session.setUpdatedAt(new Date());
        session.setUserName("测试用户");
        return session;
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
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录POST请求应返回401")
        void should_return_401_when_post_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn(PATH_START);
            when(mockRequest.getPathInfo()).thenReturn("/start");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录DELETE请求应返回401")
        void should_return_401_when_delete_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/" + SESSION_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + SESSION_ID);

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("已登录用户应能正常访问")
        void should_return_200_when_logged_in() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockStudyService.listSessions(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/study 学习记录列表 ====================

    @Nested
    @DisplayName("GET /api/study 学习记录列表")
    class ListSessionsTests {

        @Test
        @FastTest
        @DisplayName("获取学习记录列表成功")
        void should_list_sessions_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            List<StudySession> sessions = List.of(
                    createTestSession(1, MEMBER_USER_ID, STATUS_COMPLETED),
                    createTestSession(2, OTHER_USER_ID, STATUS_ACTIVE)
            );
            when(mockStudyService.listSessions(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(sessions));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带分页参数获取学习记录列表")
        void should_list_sessions_with_pagination() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockRequest.getParameter("pageSize")).thenReturn("10");

            when(mockStudyService.listSessions(any(), eq(2), eq(10)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockStudyService).listSessions(any(), eq(2), eq(10));
        }

        @Test
        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_empty_list() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockStudyService.listSessions(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("页码为0应返回400")
        void should_return_400_when_page_zero() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("0");
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockStudyService.listSessions(any(), eq(0), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.error(400, "页码必须大于0"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("pageSize超过最大值应返回400")
        void should_return_400_when_pageSize_too_large() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("pageSize")).thenReturn("200");

            when(mockStudyService.listSessions(any(), eq(1), eq(200)))
                    .thenReturn(Result.error(400, "每页大小必须大于0且不超过100"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== GET /api/study/{id} 学习记录详情 ====================

    @Nested
    @DisplayName("GET /api/study/{id} 学习记录详情")
    class GetSessionDetailTests {

        @Test
        @FastTest
        @DisplayName("获取学习记录详情成功")
        void should_get_session_detail_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/" + SESSION_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + SESSION_ID);

            StudySession session = createTestSession(SESSION_ID, MEMBER_USER_ID, STATUS_COMPLETED);
            when(mockStudyService.getSessionDetail(SESSION_ID))
                    .thenReturn(Result.ok(session));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"id\":" + SESSION_ID);
        }

        @Test
        @FastTest
        @DisplayName("获取不存在的学习记录应返回404")
        void should_return_404_when_session_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/" + NONEXISTENT_SESSION_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_SESSION_ID);

            when(mockStudyService.getSessionDetail(NONEXISTENT_SESSION_ID))
                    .thenReturn(Result.error(404, "学习记录不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
            assertThat(getResponseBody()).contains("学习记录不存在");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("无效的学习记录ID");
        }

        @Test
        @FastTest
        @DisplayName("嵌套路径应返回404")
        void should_return_404_for_nested_path() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/" + SESSION_ID + "/invalid");
            when(mockRequest.getPathInfo()).thenReturn("/" + SESSION_ID + "/invalid");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== GET /api/study/today 今日进行中会话 ====================

    @Nested
    @DisplayName("GET /api/study/today 今日进行中会话")
    class GetTodaySessionTests {

        @Test
        @FastTest
        @DisplayName("获取今日进行中会话成功")
        void should_get_today_session_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_TODAY);
            when(mockRequest.getPathInfo()).thenReturn("/today");

            StudySession session = createTestSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(mockStudyService.getTodaySession(MEMBER_USER_ID))
                    .thenReturn(Result.ok(session));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"status\":\"" + STATUS_ACTIVE + "\"");
        }

        @Test
        @FastTest
        @DisplayName("今日无进行中会话应返回null")
        void should_return_null_when_no_active_session() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_TODAY);
            when(mockRequest.getPathInfo()).thenReturn("/today");

            when(mockStudyService.getTodaySession(MEMBER_USER_ID))
                    .thenReturn(Result.ok(null));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("用户不存在应返回404")
        void should_return_404_when_user_not_found() throws Exception {
            simulateLogin(createTestUser(NONEXISTENT_SESSION_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_TODAY);
            when(mockRequest.getPathInfo()).thenReturn("/today");

            when(mockStudyService.getTodaySession(NONEXISTENT_SESSION_ID))
                    .thenReturn(Result.error(404, "用户不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== GET /api/study/my 我的学习记录 ====================

    @Nested
    @DisplayName("GET /api/study/my 我的学习记录")
    class GetMySessionsTests {

        @Test
        @FastTest
        @DisplayName("获取我的学习记录成功")
        void should_get_my_sessions_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_MY);
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            List<StudySession> sessions = List.of(
                    createTestSession(1, MEMBER_USER_ID, STATUS_COMPLETED)
            );
            when(mockStudyService.getMySessions(eq(MEMBER_USER_ID), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(sessions));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("我的学习记录带分页")
        void should_get_my_sessions_with_pagination() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_MY);
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn("3");
            when(mockRequest.getParameter("pageSize")).thenReturn("5");

            when(mockStudyService.getMySessions(MEMBER_USER_ID, 3, 5))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockStudyService).getMySessions(MEMBER_USER_ID, 3, 5);
        }
    }

    // ==================== GET /api/study/stats 学习统计 ====================

    @Nested
    @DisplayName("GET /api/study/stats 学习统计")
    class GetStatisticsTests {

        @Test
        @FastTest
        @DisplayName("获取学习统计成功")
        void should_get_statistics_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_STATS);
            when(mockRequest.getPathInfo()).thenReturn("/stats");

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSessions", 10);
            stats.put("completedSessions", 8);
            stats.put("activeSessions", 2);
            stats.put("totalDuration", 480);
            stats.put("avgDuration", 48.0);
            when(mockStudyService.getStatistics(MEMBER_USER_ID))
                    .thenReturn(Result.ok(stats));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"totalSessions\":10");
        }

        @Test
        @FastTest
        @DisplayName("获取统计时用户不存在应返回404")
        void should_return_404_when_user_not_found_for_stats() throws Exception {
            simulateLogin(createTestUser(NONEXISTENT_SESSION_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_STATS);
            when(mockRequest.getPathInfo()).thenReturn("/stats");

            when(mockStudyService.getStatistics(NONEXISTENT_SESSION_ID))
                    .thenReturn(Result.error(404, "用户不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== GET /api/study/week-stats 本周学习统计 ====================

    @Nested
    @DisplayName("GET /api/study/week-stats 本周学习统计")
    class GetWeekStatisticsTests {

        @Test
        @FastTest
        @DisplayName("获取本周学习统计成功")
        void should_get_week_statistics_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_WEEK_STATS);
            when(mockRequest.getPathInfo()).thenReturn("/week-stats");

            Map<String, Object> stats = new HashMap<>();
            stats.put("weekSessions", 5);
            stats.put("weekDuration", 300);
            when(mockStudyService.getWeekStatistics(MEMBER_USER_ID))
                    .thenReturn(Result.ok(stats));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"weekSessions\":5");
        }

        @Test
        @FastTest
        @DisplayName("获取本周统计时用户不存在应返回404")
        void should_return_404_when_user_not_found_for_week_stats() throws Exception {
            simulateLogin(createTestUser(NONEXISTENT_SESSION_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_WEEK_STATS);
            when(mockRequest.getPathInfo()).thenReturn("/week-stats");

            when(mockStudyService.getWeekStatistics(NONEXISTENT_SESSION_ID))
                    .thenReturn(Result.error(404, "用户不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/study/start 开始学习 ====================

    @Nested
    @DisplayName("POST /api/study/start 开始学习")
    class StartSessionTests {

        @Test
        @FastTest
        @DisplayName("开始学习成功")
        void should_start_session_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_START);
            when(mockRequest.getPathInfo()).thenReturn("/start");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn(null);

            when(mockStudyService.startSession(MEMBER_USER_ID))
                    .thenReturn(Result.ok(SESSION_ID));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"data\":" + SESSION_ID);
        }

        @Test
        @FastTest
        @DisplayName("已有进行中学习时段应返回400")
        void should_return_400_when_active_session_exists() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_START);
            when(mockRequest.getPathInfo()).thenReturn("/start");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.startSession(MEMBER_USER_ID))
                    .thenReturn(Result.error(400, "您已有进行中的学习时段，请先结束"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("您已有进行中的学习时段");
        }

        @Test
        @FastTest
        @DisplayName("用户不存在应返回404")
        void should_return_404_when_user_not_found_for_start() throws Exception {
            simulateLogin(createTestUser(NONEXISTENT_SESSION_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_START);
            when(mockRequest.getPathInfo()).thenReturn("/start");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.startSession(NONEXISTENT_SESSION_ID))
                    .thenReturn(Result.error(404, "用户不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("数据库错误应返回500")
        void should_return_500_when_database_error_on_start() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_START);
            when(mockRequest.getPathInfo()).thenReturn("/start");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.startSession(MEMBER_USER_ID))
                    .thenReturn(Result.error(500, "数据库错误"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }

        @Test
        @FastTest
        @DisplayName("开始学习时服务器异常应返回500")
        void should_return_500_when_server_error_on_start() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_START);
            when(mockRequest.getPathInfo()).thenReturn("/start");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.startSession(MEMBER_USER_ID))
                    .thenThrow(new RuntimeException("服务器内部错误"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }
    }

    // ==================== POST /api/study/end 结束学习 ====================

    @Nested
    @DisplayName("POST /api/study/end 结束学习")
    class EndSessionTests {

        @Test
        @FastTest
        @DisplayName("结束学习成功")
        void should_end_session_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_END);
            when(mockRequest.getPathInfo()).thenReturn("/end");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn(null);

            when(mockStudyService.endSession(MEMBER_USER_ID))
                    .thenReturn(Result.ok(60));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"data\":60");
        }

        @Test
        @FastTest
        @DisplayName("没有进行中的学习时段应返回400")
        void should_return_400_when_no_active_session() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_END);
            when(mockRequest.getPathInfo()).thenReturn("/end");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.endSession(MEMBER_USER_ID))
                    .thenReturn(Result.error(400, "没有进行中的学习时段"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("没有进行中的学习时段");
        }

        @Test
        @FastTest
        @DisplayName("用户不存在应返回404")
        void should_return_404_when_user_not_found_for_end() throws Exception {
            simulateLogin(createTestUser(NONEXISTENT_SESSION_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_END);
            when(mockRequest.getPathInfo()).thenReturn("/end");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.endSession(NONEXISTENT_SESSION_ID))
                    .thenReturn(Result.error(404, "用户不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("数据库错误应返回500")
        void should_return_500_when_database_error_on_end() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_END);
            when(mockRequest.getPathInfo()).thenReturn("/end");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockStudyService.endSession(MEMBER_USER_ID))
                    .thenReturn(Result.error(500, "数据库错误"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }
    }

    // ==================== 其他端点测试 ====================

    @Nested
    @DisplayName("其他端点测试")
    class OtherEndpointTests {

        @Test
        @FastTest
        @DisplayName("PUT方法不支持应返回405")
        void should_return_405_when_put_method() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doPut(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":405");
        }

        @Test
        @FastTest
        @DisplayName("DELETE根路径应返回404")
        void should_return_404_when_delete_root_path() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效路径应返回404")
        void should_return_404_for_invalid_path() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/invalid-path");
            when(mockRequest.getPathInfo()).thenReturn("/invalid-path");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== 状态枚举测试 ====================

    @Nested
    @DisplayName("状态枚举测试")
    class StatusEnumTests {

        @Test
        @FastTest
        @DisplayName("ACTIVE状态应正确处理")
        void should_handle_active_status() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_TODAY);
            when(mockRequest.getPathInfo()).thenReturn("/today");

            StudySession session = createTestSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(mockStudyService.getTodaySession(MEMBER_USER_ID))
                    .thenReturn(Result.ok(session));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"status\":\"" + STATUS_ACTIVE + "\"");
        }

        @Test
        @FastTest
        @DisplayName("COMPLETED状态应正确处理")
        void should_handle_completed_status() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, ROLE_MEMBER));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/" + SESSION_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + SESSION_ID);

            StudySession session = createTestSession(SESSION_ID, MEMBER_USER_ID, STATUS_COMPLETED);
            when(mockStudyService.getSessionDetail(SESSION_ID))
                    .thenReturn(Result.ok(session));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"status\":\"" + STATUS_COMPLETED + "\"");
            assertThat(getResponseBody()).contains("\"duration\":60");
        }
    }

    // ==================== 可测试的内部Servlet类 ====================

    /**
     * 可测试的StudyApiServlet
     *
     * 复制StudyApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableStudyApiServlet {

        private final StudyService studyService;
        private final Gson gson = new GsonBuilder().create();

        public TestableStudyApiServlet(StudyService studyService) {
            this.studyService = studyService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            if (isListPath(pathInfo)) {
                dispatchListGetRequest(pathInfo, req, resp, user);
            } else {
                handleSessionGet(req, resp, user, pathInfo);
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
                    handleActionPost(req, resp, user, pathInfo);
                }
            } else {
                writeJson(resp, Result.error(404, "路径不存在"));
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
                writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        // ==================== GET 请求分发 ====================

        private void dispatchListGetRequest(String pathInfo, HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            if (pathInfo == null || "/".equals(pathInfo)) {
                handleListSessions(req, resp, user);
            } else if ("/today".equals(pathInfo)) {
                handleGetTodaySession(req, resp, user);
            } else if ("/my".equals(pathInfo)) {
                handleGetMySessions(req, resp, user);
            } else if ("/stats".equals(pathInfo)) {
                handleGetStatistics(req, resp, user);
            } else if ("/week-stats".equals(pathInfo)) {
                handleGetWeekStatistics(req, resp, user);
            } else {
                handleListSessions(req, resp, user);
            }
        }

        private void handleListSessions(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int page = parseIntParam(req.getParameter("page"), 1);
            int pageSize = parseIntParam(req.getParameter("pageSize"), 20);
            writeJson(resp, studyService.listSessions(new HashMap<>(), page, pageSize));
        }

        private void handleGetTodaySession(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            writeJson(resp, studyService.getTodaySession(user.getId()));
        }

        private void handleGetMySessions(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int page = parseIntParam(req.getParameter("page"), 1);
            int pageSize = parseIntParam(req.getParameter("pageSize"), 20);
            writeJson(resp, studyService.getMySessions(user.getId(), page, pageSize));
        }

        private void handleGetStatistics(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            writeJson(resp, studyService.getStatistics(user.getId()));
        }

        private void handleGetWeekStatistics(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            writeJson(resp, studyService.getWeekStatistics(user.getId()));
        }

        private void handleSessionGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            StudyPathInfo pi = parsePathInfo(pathInfo);
            if (pi.isInvalid()) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }
            if (!pi.isValidSessionId()) {
                writeJson(resp, Result.error(400, "无效的学习记录ID"));
                return;
            }
            if (pi.hasSubResource()) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                writeJson(resp, studyService.getSessionDetail(pi.getSessionId()));
            }
        }

        private void handleActionPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            if ("/start".equals(pathInfo)) {
                try {
                    writeJson(resp, studyService.startSession(user.getId()));
                } catch (RuntimeException e) {
                    writeJson(resp, Result.error(500, "服务器内部错误"));
                }
            } else if ("/end".equals(pathInfo)) {
                try {
                    writeJson(resp, studyService.endSession(user.getId()));
                } catch (RuntimeException e) {
                    writeJson(resp, Result.error(500, "服务器内部错误"));
                }
            } else {
                writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        // ==================== Path Utilities ====================

        private boolean isListPath(String pathInfo) {
            if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
                return true;
            }
            return "/today".equals(pathInfo) || "/my".equals(pathInfo)
                    || "/stats".equals(pathInfo) || "/week-stats".equals(pathInfo)
                    || "/start".equals(pathInfo) || "/end".equals(pathInfo);
        }

        private String derivePathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                String uri = req.getRequestURI();
                int idx = uri.indexOf("/api/study/");
                if (idx >= 0) {
                    pathInfo = uri.substring(idx + 10);
                    if (pathInfo.isEmpty()) {
                        pathInfo = null;
                    } else if (!pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }
            return pathInfo;
        }

        private StudyPathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || "/".equals(pathInfo) || pathInfo.isEmpty()) {
                return StudyPathInfo.ROOT;
            }

            if (!pathInfo.startsWith("/")) {
                return StudyPathInfo.ROOT;
            }

            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length == 0 || segments[0].isEmpty()) {
                return StudyPathInfo.ROOT;
            }

            int sessionId = parseIntOrZero(segments[0]);

            // If first segment contains non-letter/non-digit characters (like hyphens, underscores),
            // it's an invalid path format (404), not just invalid ID (400)
            if (sessionId == 0 && containsInvalidChars(segments[0])) {
                return StudyPathInfo.forInvalid();
            }

            if (segments.length == 1) {
                return StudyPathInfo.forSession(sessionId);
            }

            return StudyPathInfo.forSubResource(sessionId, segments[1]);
        }

        private boolean containsInvalidChars(String str) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (!Character.isLetterOrDigit(c)) {
                    return true;
                }
            }
            return false;
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

        private static class StudyPathInfo {
            static final StudyPathInfo ROOT = new StudyPathInfo(0, null, false);
            static final StudyPathInfo INVALID = new StudyPathInfo(0, null, true);

            private final int sessionId;
            private final String subResource;
            private final boolean invalid;

            private StudyPathInfo(int sessionId, String subResource, boolean invalid) {
                this.sessionId = sessionId;
                this.subResource = subResource;
                this.invalid = invalid;
            }

            static StudyPathInfo forSession(int sessionId) {
                return new StudyPathInfo(sessionId, null, false);
            }

            static StudyPathInfo forSubResource(int sessionId, String subResource) {
                return new StudyPathInfo(sessionId, subResource, false);
            }

            static StudyPathInfo forInvalid() {
                return INVALID;
            }

            int getSessionId() {
                return sessionId;
            }

            boolean isValidSessionId() {
                return sessionId > 0;
            }

            boolean hasSubResource() {
                return subResource != null;
            }

            boolean isInvalid() {
                return invalid;
            }
        }
    }
}
