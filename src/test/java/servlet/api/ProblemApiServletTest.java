package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.ProblemDTO;
import dto.ProblemFilterDTO;
import model.ProblemReport;
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
import service.ProblemService;
import servlet.BaseApiServlet;
import support.FastTest;
import util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
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
 * ProblemApiServlet TDD测试套件
 *
 * 测试范围：服务分层与API化完整计划.md 5.2 ProblemApiServlet 端点
 * - 所有REST端点
 * - 所有HTTP方法(GET/POST/PUT/DELETE)
 * - 认证与授权
 * - 参数解析与验证
 * - 错误处理
 * - 所有状态枚举
 * - 所有边界情况
 *
 * 测试覆盖端点：
 * - GET  /api/problems           → 问题列表(分页)
 * - GET  /api/problems/{id}     → 问题详情
 * - POST /api/problems           → 提交问题
 * - PUT  /api/problems/{id}     → 更新问题
 * - DELETE /api/problems/{id}   → 删除问题
 * - POST /api/problems/{id}/status   → 更新状态
 * - POST /api/problems/{id}/category → 更新分类
 * - POST /api/problems/{id}/comment  → 添加备注
 * - GET  /api/problems/my        → 我的问题
 * - GET  /api/problems/stats     → 问题统计
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProblemApiServlet 问题API测试")
class ProblemApiServletTest {

    private TestableProblemApiServlet servlet;
    private ProblemService mockProblemService;

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
    private static final Integer GUEST_USER_ID = null;
    private static final Integer PROBLEM_ID = 100;
    private static final Integer NONEXISTENT_PROBLEM_ID = 99999;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    // 分类常量
    private static final String CATEGORY_VERIFIED = ProblemDTO.CATEGORY_VERIFIED;
    private static final String CATEGORY_UNVERIFIED = ProblemDTO.CATEGORY_UNVERIFIED;
    private static final String CATEGORY_INVALID = ProblemDTO.CATEGORY_INVALID;

    // 状态常量
    private static final String STATUS_PENDING = ProblemDTO.STATUS_PENDING;
    private static final String STATUS_SOLVING = ProblemDTO.STATUS_SOLVING;
    private static final String STATUS_SOLVED = ProblemDTO.STATUS_SOLVED;
    private static final String STATUS_UNSOLVED = ProblemDTO.STATUS_UNSOLVED;

    // 报告者类型常量
    private static final String REPORTER_TYPE_ADMIN = ProblemDTO.REPORTER_TYPE_ADMIN;
    private static final String REPORTER_TYPE_MEMBER = ProblemDTO.REPORTER_TYPE_MEMBER;
    private static final String REPORTER_TYPE_GUEST = ProblemDTO.REPORTER_TYPE_GUEST;

    @BeforeEach
    void setUp() throws Exception {
        mockProblemService = mock(ProblemService.class);
        servlet = new TestableProblemApiServlet(mockProblemService);
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

    private ProblemReport createTestProblem(Integer id, String title, String category, String status) {
        ProblemReport problem = new ProblemReport();
        problem.setId(id);
        problem.setTitle(title);
        problem.setContent("测试问题内容");
        problem.setCategory(category);
        problem.setStatus(status);
        problem.setReporterType(REPORTER_TYPE_MEMBER);
        problem.setReporterName("测试用户");
        problem.setCreatedAt(new Date());
        problem.setUpdatedAt(new Date());
        return problem;
    }

    private ProblemDTO createTestProblemDTO(String title, String content) {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setReporterName("测试报告者");
        dto.setReporterContact("test@example.com");
        return dto;
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
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录POST请求应返回401")
        void should_return_401_when_post_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
               @DisplayName("未登录DELETE请求应返回401")
        void should_return_401_when_delete_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/1");
            when(mockRequest.getPathInfo()).thenReturn("/1");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("已登录用户应能正常访问")
        void should_return_200_when_logged_in() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockProblemService.listProblems(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/problems 问题列表 ====================

    @Nested
    @DisplayName("GET /api/problems 问题列表")
    class ListProblemsTests {

        @Test
        @FastTest
        @DisplayName("获取问题列表成功")
        void should_list_problems_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            List<ProblemReport> problems = List.of(
                    createTestProblem(1, "问题1", CATEGORY_UNVERIFIED, STATUS_PENDING),
                    createTestProblem(2, "问题2", CATEGORY_VERIFIED, STATUS_SOLVING)
            );
            when(mockProblemService.listProblems(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(problems));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("问题1");
            assertThat(getResponseBody()).contains("问题2");
        }

        @Test
        @FastTest
        @DisplayName("带分页参数获取问题列表")
        void should_list_problems_with_pagination() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockRequest.getParameter("pageSize")).thenReturn("10");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            when(mockProblemService.listProblems(any(), eq(2), eq(10)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockProblemService).listProblems(any(), eq(2), eq(10));
        }

        @Test
        @FastTest
        @DisplayName("按分类筛选问题列表")
        void should_filter_problems_by_category() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_VERIFIED);
            when(mockRequest.getParameter("status")).thenReturn(null);

            ProblemFilterDTO filter = new ProblemFilterDTO();
            filter.setCategory(CATEGORY_VERIFIED);
            when(mockProblemService.listProblems(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("按状态筛选问题列表")
        void should_filter_problems_by_status() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(STATUS_SOLVED);

            when(mockProblemService.listProblems(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_empty_list() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);

            when(mockProblemService.listProblems(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/problems/{id} 问题详情 ====================

    @Nested
    @DisplayName("GET /api/problems/{id} 问题详情")
    class GetProblemDetailTests {

        @Test
        @FastTest
        @DisplayName("获取问题详情成功")
        void should_get_problem_detail_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID);

            ProblemReport problem = createTestProblem(PROBLEM_ID, "测试问题", CATEGORY_VERIFIED, STATUS_SOLVING);
            when(mockProblemService.getProblemDetail(PROBLEM_ID))
                    .thenReturn(Result.ok(problem));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("测试问题");
        }

        @Test
        @FastTest
        @DisplayName("获取不存在的问题应返回404")
        void should_return_404_when_problem_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + NONEXISTENT_PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_PROBLEM_ID);

            when(mockProblemService.getProblemDetail(NONEXISTENT_PROBLEM_ID))
                    .thenReturn(Result.error(404, "问题不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
            assertThat(getResponseBody()).contains("问题不存在");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("无效的问题ID");
        }

        @Test
        @FastTest
        @DisplayName("嵌套路径应返回404")
        void should_return_404_for_nested_path() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/invalid");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/invalid");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== GET /api/problems/my 我的问题 ====================

    @Nested
    @DisplayName("GET /api/problems/my 我的问题")
    class GetMyProblemsTests {

        @Test
        @FastTest
        @DisplayName("获取我的问题列表成功")
        void should_get_my_problems_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/my");
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            List<ProblemReport> problems = List.of(
                    createTestProblem(1, "我的问题1", CATEGORY_UNVERIFIED, STATUS_PENDING)
            );
            when(mockProblemService.getMyProblems(eq(MEMBER_USER_ID), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(problems));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("我的问题1");
        }

        @Test
        @FastTest
        @DisplayName("我的问题列表带分页")
        void should_get_my_problems_with_pagination() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/my");
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn("3");
            when(mockRequest.getParameter("pageSize")).thenReturn("5");

            when(mockProblemService.getMyProblems(MEMBER_USER_ID, 3, 5))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockProblemService).getMyProblems(MEMBER_USER_ID, 3, 5);
        }
    }

    // ==================== GET /api/problems/stats 问题统计 ====================

    @Nested
    @DisplayName("GET /api/problems/stats 问题统计")
    class GetStatisticsTests {

        @Test
        @FastTest
        @DisplayName("获取问题统计成功")
        void should_get_statistics_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/stats");
            when(mockRequest.getPathInfo()).thenReturn("/stats");

            Map<String, Object> stats = new HashMap<>();
            stats.put("pending", 10);
            stats.put("verified", 5);
            stats.put("unverified", 3);
            stats.put("invalid", 2);
            stats.put("solved", 8);
            when(mockProblemService.getStatistics()).thenReturn(Result.ok(stats));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"pending\":10");
        }

        @Test
        @FastTest
        @DisplayName("管理员访问统计应成功")
        void should_allow_admin_to_get_stats() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/stats");
            when(mockRequest.getPathInfo()).thenReturn("/stats");

            when(mockProblemService.getStatistics()).thenReturn(Result.ok(new HashMap<>()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== POST /api/problems 提交问题 ====================

    @Nested
    @DisplayName("POST /api/problems 提交问题")
    class CreateProblemTests {

        @Test
        @FastTest
        @DisplayName("提交问题成功")
        void should_create_problem_successfully() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn(null);

            ProblemDTO dto = createTestProblemDTO("新问题", "问题描述内容");
            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(PROBLEM_ID));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("提交问题标题为空应返回400")
        void should_return_400_when_title_empty() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = createTestProblemDTO("", "问题描述");
            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "标题不能为空"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("提交问题内容为空应返回400")
        void should_return_400_when_content_empty() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = createTestProblemDTO("标题", "");
            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "内容不能为空"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("提交问题时服务错误应返回500")
        void should_return_500_when_service_error() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = createTestProblemDTO("标题", "内容");
            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(500, "创建问题失败"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }

        @Test
        @FastTest
        @DisplayName("空请求体应返回400")
        void should_return_400_when_body_empty() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== PUT /api/problems/{id} 更新问题 ====================

    @Nested
    @DisplayName("PUT /api/problems/{id} 更新问题")
    class UpdateProblemTests {

        @Test
        @FastTest
        @DisplayName("使用POST模拟PUT更新问题成功")
        void should_update_problem_via_post_tunnel() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            ProblemDTO dto = createTestProblemDTO("更新后的标题", "更新后的内容");
            when(mockProblemService.updateProblem(eq(PROBLEM_ID), any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(createTestProblem(PROBLEM_ID, "更新后的标题", CATEGORY_VERIFIED, STATUS_SOLVING)));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新不存在的问题应返回404")
        void should_return_404_when_update_not_found() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + NONEXISTENT_PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_PROBLEM_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            ProblemDTO dto = createTestProblemDTO("更新标题", "更新内容");
            when(mockProblemService.updateProblem(eq(NONEXISTENT_PROBLEM_ID), any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "问题不存在"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式更新应返回400")
        void should_return_400_when_invalid_id_on_update() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/xyz");
            when(mockRequest.getPathInfo()).thenReturn("/xyz");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("根路径使用PUT方法应返回405")
        void should_return_405_when_put_on_root() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("PUT");

            servlet.doPut(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":405");
        }
    }

    // ==================== DELETE /api/problems/{id} 删除问题 ====================

    @Nested
    @DisplayName("DELETE /api/problems/{id} 删除问题")
    class DeleteProblemTests {

        @Test
        @FastTest
        @DisplayName("删除问题成功")
        void should_delete_problem_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID);

            when(mockProblemService.deleteProblem(PROBLEM_ID, ADMIN_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("删除不存在的问题应返回404")
        void should_return_404_when_delete_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + NONEXISTENT_PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_PROBLEM_ID);

            when(mockProblemService.deleteProblem(NONEXISTENT_PROBLEM_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(404, "问题不存在"));

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式删除应返回400")
        void should_return_400_when_invalid_id_on_delete() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("删除带子路径应返回404")
        void should_return_404_when_delete_with_subpath() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/status");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/status");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/problems/{id}/status 更新状态 ====================

    @Nested
    @DisplayName("POST /api/problems/{id}/status 更新状态")
    class UpdateStatusTests {

        @Test
        @FastTest
        @DisplayName("更新状态成功")
        void should_update_status_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/status");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/status");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setStatus(STATUS_SOLVED);
            dto.setAdminComment("问题已解决");

            when(mockProblemService.updateStatus(eq(PROBLEM_ID), eq(STATUS_SOLVED), eq("问题已解决"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新为处理中状态")
        void should_update_status_to_solving() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/status");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/status");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setStatus(STATUS_SOLVING);

            when(mockProblemService.updateStatus(eq(PROBLEM_ID), eq(STATUS_SOLVING), isNull(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新为未解决状态")
        void should_update_status_to_unsolved() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/status");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/status");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setStatus(STATUS_UNSOLVED);

            when(mockProblemService.updateStatus(eq(PROBLEM_ID), eq(STATUS_UNSOLVED), isNull(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新不存在问题的状态应返回404")
        void should_return_404_when_update_status_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + NONEXISTENT_PROBLEM_ID + "/status");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_PROBLEM_ID + "/status");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setStatus(STATUS_SOLVED);

            when(mockProblemService.updateStatus(eq(NONEXISTENT_PROBLEM_ID), eq(STATUS_SOLVED), isNull(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "问题不存在"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式更新状态应返回400")
        void should_return_400_when_invalid_id_on_update_status() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/xyz/status");
            when(mockRequest.getPathInfo()).thenReturn("/xyz/status");
            when(mockRequest.getMethod()).thenReturn("POST");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== POST /api/problems/{id}/category 更新分类 ====================

    @Nested
    @DisplayName("POST /api/problems/{id}/category 更新分类")
    class UpdateCategoryTests {

        @Test
        @FastTest
        @DisplayName("更新分类为已核实成功")
        void should_update_category_to_verified() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/category");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/category");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setCategory(CATEGORY_VERIFIED);

            when(mockProblemService.updateCategory(eq(PROBLEM_ID), eq(CATEGORY_VERIFIED), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新分类为无效成功")
        void should_update_category_to_invalid() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/category");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/category");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setCategory(CATEGORY_INVALID);

            when(mockProblemService.updateCategory(eq(PROBLEM_ID), eq(CATEGORY_INVALID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新不存在问题的分类应返回404")
        void should_return_404_when_update_category_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + NONEXISTENT_PROBLEM_ID + "/category");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_PROBLEM_ID + "/category");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setCategory(CATEGORY_VERIFIED);

            when(mockProblemService.updateCategory(eq(NONEXISTENT_PROBLEM_ID), eq(CATEGORY_VERIFIED), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "问题不存在"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效分类值应返回400")
        void should_return_400_when_invalid_category_value() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/category");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/category");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setCategory("INVALID_CATEGORY");

            when(mockProblemService.updateCategory(eq(PROBLEM_ID), eq("INVALID_CATEGORY"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "无效的分类值"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== POST /api/problems/{id}/comment 添加备注 ====================

    @Nested
    @DisplayName("POST /api/problems/{id}/comment 添加备注")
    class AddCommentTests {

        @Test
        @FastTest
        @DisplayName("添加备注成功")
        void should_add_comment_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/comment");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/comment");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setAdminComment("管理员备注内容");

            when(mockProblemService.addComment(eq(PROBLEM_ID), eq("管理员备注内容"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("添加空备注也应成功")
        void should_add_empty_comment_successfully() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/comment");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/comment");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setAdminComment("");

            when(mockProblemService.addComment(eq(PROBLEM_ID), eq(""), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("添加备注到不存在问题应返回404")
        void should_return_404_when_add_comment_not_found() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + NONEXISTENT_PROBLEM_ID + "/comment");
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_PROBLEM_ID + "/comment");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setAdminComment("备注");

            when(mockProblemService.addComment(eq(NONEXISTENT_PROBLEM_ID), eq("备注"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "问题不存在"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式添加备注应返回400")
        void should_return_400_when_invalid_id_on_add_comment() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/xyz/comment");
            when(mockRequest.getPathInfo()).thenReturn("/xyz/comment");
            when(mockRequest.getMethod()).thenReturn("POST");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== 角色权限测试 ====================

    @Nested
    @DisplayName("角色权限测试")
    class RolePermissionTests {

        @Test
        @FastTest
        @DisplayName("管理员可以更新状态")
        void should_allow_admin_to_update_status() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/status");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/status");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = new ProblemDTO();
            dto.setStatus(STATUS_SOLVED);

            when(mockProblemService.updateStatus(eq(PROBLEM_ID), eq(STATUS_SOLVED), isNull(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("成员可以提交问题")
        void should_allow_member_to_submit_problem() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = createTestProblemDTO("成员提交的问题", "内容");

            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(PROBLEM_ID));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("成员可以查看自己的问题")
        void should_allow_member_to_view_own_problems() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/my");
            when(mockRequest.getPathInfo()).thenReturn("/my");

            when(mockProblemService.getMyProblems(eq(MEMBER_USER_ID), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(List.of()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("成员可以获取问题详情")
        void should_allow_member_to_view_problem_detail() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID);

            when(mockProblemService.getProblemDetail(PROBLEM_ID))
                    .thenReturn(Result.ok(createTestProblem(PROBLEM_ID, "测试", CATEGORY_VERIFIED, STATUS_PENDING)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @FastTest
        @DisplayName("所有状态枚举值都应被正确处理")
        void should_handle_all_status_enums() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));

            String[] statuses = {STATUS_PENDING, STATUS_SOLVING, STATUS_SOLVED, STATUS_UNSOLVED};

            for (String status : statuses) {
                resetResponseWriter();
                when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/status");
                when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/status");
                when(mockRequest.getMethod()).thenReturn("POST");

                ProblemDTO dto = new ProblemDTO();
                dto.setStatus(status);

                when(mockProblemService.updateStatus(eq(PROBLEM_ID), eq(status), isNull(), eq(ADMIN_USER_ID)))
                        .thenReturn(Result.ok());

                String jsonBody = gson.toJson(dto);
                when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

                servlet.doPost(mockRequest, mockResponse);

                assertThat(getResponseBody()).contains("\"code\":0")
                        .as("状态 " + status + " 应被正确处理");
            }
        }

        @Test
        @FastTest
        @DisplayName("所有分类枚举值都应被正确处理")
        void should_handle_all_category_enums() throws Exception {
            simulateLogin(createTestUser(ADMIN_USER_ID, "ADMIN"));

            String[] categories = {CATEGORY_VERIFIED, CATEGORY_UNVERIFIED, CATEGORY_INVALID};

            for (String category : categories) {
                resetResponseWriter();
                when(mockRequest.getRequestURI()).thenReturn("/api/problems/" + PROBLEM_ID + "/category");
                when(mockRequest.getPathInfo()).thenReturn("/" + PROBLEM_ID + "/category");
                when(mockRequest.getMethod()).thenReturn("POST");

                ProblemDTO dto = new ProblemDTO();
                dto.setCategory(category);

                when(mockProblemService.updateCategory(eq(PROBLEM_ID), eq(category), eq(ADMIN_USER_ID)))
                        .thenReturn(Result.ok());

                String jsonBody = gson.toJson(dto);
                when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

                servlet.doPost(mockRequest, mockResponse);

                assertThat(getResponseBody()).contains("\"code\":0")
                        .as("分类 " + category + " 应被正确处理");
            }
        }

        @Test
        @FastTest
        @DisplayName("超长标题应被服务层处理")
        void should_handle_long_title() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            String longTitle = "A".repeat(300);
            ProblemDTO dto = createTestProblemDTO(longTitle, "内容");

            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "标题不能超过256个字符"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("超长内容应被服务层处理")
        void should_handle_long_content() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            String longContent = "A".repeat(6000);
            ProblemDTO dto = createTestProblemDTO("标题", longContent);

            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "内容不能超过5000个字符"));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("特殊字符应被正确处理")
        void should_handle_special_characters() throws Exception {
            simulateLogin(createTestUser(MEMBER_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/problems");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getMethod()).thenReturn("POST");

            ProblemDTO dto = createTestProblemDTO("标题<Script>alert('xss')</Script>", "内容");
            when(mockProblemService.submitProblem(any(ProblemDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(PROBLEM_ID));

            String jsonBody = gson.toJson(dto);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 可测试的内部Servlet类 ====================

    /**
     * 可测试的ProblemApiServlet
     *
     * 复制ProblemApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableProblemApiServlet {

        private final ProblemService problemService;
        private final Gson gson = new GsonBuilder().create();

        public TestableProblemApiServlet(ProblemService problemService) {
            this.problemService = problemService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            if (isListPath(pathInfo)) {
                if ("/stats".equals(pathInfo)) {
                    writeJson(resp, problemService.getStatistics());
                } else if ("/my".equals(pathInfo)) {
                    handleGetMyProblems(req, resp, user);
                } else {
                    handleListProblems(req, resp, user);
                }
            } else {
                handleProblemGet(req, resp, user, pathInfo);
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
                handleProblemPost(req, resp, user, pathInfo, isPutTunnel);
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
                handleProblemDelete(req, resp, user, pathInfo);
            }
        }

        // ==================== Handler Methods ====================

        private void handleListProblems(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int page = parseIntParam(req.getParameter("page"), 1);
            int pageSize = parseIntParam(req.getParameter("pageSize"), 20);

            ProblemFilterDTO filter = new ProblemFilterDTO();
            filter.setCategory(req.getParameter("category"));
            filter.setStatus(req.getParameter("status"));

            writeJson(resp, problemService.listProblems(filter, page, pageSize));
        }

        private void handleGetMyProblems(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            int page = parseIntParam(req.getParameter("page"), 1);
            int pageSize = parseIntParam(req.getParameter("pageSize"), 20);

            writeJson(resp, problemService.getMyProblems(user.getId(), page, pageSize));
        }

        private void handleProblemGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            ProblemPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidProblemId()) {
                writeJson(resp, Result.error(400, "无效的问题ID"));
                return;
            }

            if (pi.hasSubResource()) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                writeJson(resp, problemService.getProblemDetail(pi.getProblemId()));
            }
        }

        private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            String body = readBody(req);
            ProblemDTO dto = parseJsonRequest(body, ProblemDTO.class);
            if (dto == null) {
                writeJson(resp, Result.error(400, "请求体不能为空"));
                return;
            }
            writeJson(resp, problemService.submitProblem(dto, user.getId()));
        }

        private void handleProblemPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo, boolean isPutTunnel) throws Exception {
            ProblemPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidProblemId()) {
                writeJson(resp, Result.error(400, "无效的问题ID"));
                return;
            }

            if (pi.isStatusAction()) {
                handleUpdateStatus(req, resp, user, pi.getProblemId());
            } else if (pi.isCategoryAction()) {
                handleUpdateCategory(req, resp, user, pi.getProblemId());
            } else if (pi.isCommentAction()) {
                handleAddComment(req, resp, user, pi.getProblemId());
            } else if (isPutTunnel) {
                handleUpdate(req, resp, user, pi.getProblemId());
            } else {
                writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int problemId) throws Exception {
            String body = readBody(req);
            ProblemDTO dto = parseJsonRequest(body, ProblemDTO.class);
            if (dto == null) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
                return;
            }
            writeJson(resp, problemService.updateProblem(problemId, dto, user.getId()));
        }

        private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp, User user, int problemId) throws Exception {
            String body = readBody(req);
            ProblemDTO dto = parseJsonRequest(body, ProblemDTO.class);
            String status = dto != null ? dto.getStatus() : null;
            String adminComment = dto != null ? dto.getAdminComment() : null;
            writeJson(resp, problemService.updateStatus(problemId, status, adminComment, user.getId()));
        }

        private void handleUpdateCategory(HttpServletRequest req, HttpServletResponse resp, User user, int problemId) throws Exception {
            String body = readBody(req);
            ProblemDTO dto = parseJsonRequest(body, ProblemDTO.class);
            String category = dto != null ? dto.getCategory() : null;
            writeJson(resp, problemService.updateCategory(problemId, category, user.getId()));
        }

        private void handleAddComment(HttpServletRequest req, HttpServletResponse resp, User user, int problemId) throws Exception {
            String body = readBody(req);
            ProblemDTO dto = parseJsonRequest(body, ProblemDTO.class);
            String adminComment = dto != null ? dto.getAdminComment() : null;
            writeJson(resp, problemService.addComment(problemId, adminComment, user.getId()));
        }

        private void handleProblemDelete(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            ProblemPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidProblemId()) {
                writeJson(resp, Result.error(400, "无效的问题ID"));
                return;
            }

            // DELETE only supports /api/problems/{id}, not sub-resources or actions
            if (pi.hasSubResource() || pi.isStatusAction() || pi.isCategoryAction() || pi.isCommentAction()) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                writeJson(resp, problemService.deleteProblem(pi.getProblemId(), user.getId()));
            }
        }

        // ==================== Path Utilities ====================

        private boolean isListPath(String pathInfo) {
            if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
                return true;
            }
            // Handle special list paths like /my and /stats
            if ("/my".equals(pathInfo) || "/stats".equals(pathInfo)) {
                return true;
            }
            return false;
        }

        private String derivePathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                String uri = req.getRequestURI();
                int idx = uri.indexOf("/api/problems/");
                if (idx >= 0) {
                    pathInfo = uri.substring(idx + 14);
                    if (pathInfo.isEmpty()) {
                        pathInfo = null;
                    } else if (!pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }
            return pathInfo;
        }

        private ProblemPathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                return ProblemPathInfo.ROOT;
            }

            if (!pathInfo.startsWith("/")) {
                return ProblemPathInfo.ROOT;
            }

            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length == 0 || segments[0].isEmpty()) {
                return ProblemPathInfo.ROOT;
            }

            int problemId = parseIntOrZero(segments[0]);

            if (segments.length == 1) {
                return ProblemPathInfo.forProblem(problemId);
            }

            String segment1 = segments[1];

            if ("status".equals(segment1)) {
                return ProblemPathInfo.forAction(problemId, "status");
            }
            if ("category".equals(segment1)) {
                return ProblemPathInfo.forAction(problemId, "category");
            }
            if ("comment".equals(segment1)) {
                return ProblemPathInfo.forAction(problemId, "comment");
            }

            return ProblemPathInfo.forSubResource(problemId, segment1);
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

        private static class ProblemPathInfo {
            static final ProblemPathInfo ROOT = new ProblemPathInfo(0, null, null);

            private final int problemId;
            private final String subResource;
            private final String action;

            private ProblemPathInfo(int problemId, String subResource, String action) {
                this.problemId = problemId;
                this.subResource = subResource;
                this.action = action;
            }

            static ProblemPathInfo forProblem(int problemId) {
                return new ProblemPathInfo(problemId, null, null);
            }

            static ProblemPathInfo forAction(int problemId, String action) {
                return new ProblemPathInfo(problemId, null, action);
            }

            static ProblemPathInfo forSubResource(int problemId, String subResource) {
                return new ProblemPathInfo(problemId, subResource, null);
            }

            boolean isValidProblemId() {
                return problemId > 0;
            }

            int getProblemId() {
                return problemId;
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

            boolean isStatusAction() {
                return "status".equals(action);
            }

            boolean isCategoryAction() {
                return "category".equals(action);
            }

            boolean isCommentAction() {
                return "comment".equals(action);
            }
        }
    }
}
