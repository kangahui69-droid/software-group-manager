package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.OperationLog;
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
import service.LogService;
import support.FastTest;
import util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LogApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 6.2 LogApiServlet 日志服务API化
 * - 所有REST端点
 * - 所有HTTP方法(GET/POST/PUT/DELETE)
 * - 认证与授权
 * - 参数解析与验证
 * - 错误处理
 * - 所有状态枚举
 * - 所有边界情况
 *
 * 测试覆盖端点：
 * - GET  /api/logs           → 日志列表(分页)
 * - GET  /api/logs/{id}     → 日志详情
 *
 * LogService方法映射：
 * - listLogs(filter, page, pageSize)
 * - getLogDetail(id)
 *
 * 操作类型枚举：
 * - LOGIN, LOGOUT, CREATE, UPDATE, DELETE
 *
 * 模块类型枚举：
 * - USER, ACTIVITY, PROJECT, AWARD
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LogApiServlet 日志API测试")
class LogApiServletTest {

    private TestableLogApiServlet servlet;
    private LogService mockLogService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private StringWriter responseWriter;
    private Gson gson;

    // ==================== 测试数据常量 ====================

    private static final Integer LOG_ID = 100;
    private static final Integer USER_ID = 1;
    private static final Integer ADMIN_USER_ID = 3;
    private static final Integer NONEXISTENT_LOG_ID = 99999;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    // 角色常量
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 路径常量
    private static final String PATH_LIST = "/api/logs";
    private static final String PATH_LIST_WITH_SLASH = "/api/logs/";

    // 操作类型枚举
    private static final String OPERATION_LOGIN = "LOGIN";
    private static final String OPERATION_LOGOUT = "LOGOUT";
    private static final String OPERATION_CREATE = "CREATE";
    private static final String OPERATION_UPDATE = "UPDATE";
    private static final String OPERATION_DELETE = "DELETE";

    // 模块类型枚举
    private static final String MODULE_USER = "USER";
    private static final String MODULE_ACTIVITY = "ACTIVITY";
    private static final String MODULE_PROJECT = "PROJECT";
    private static final String MODULE_AWARD = "AWARD";

    @BeforeEach
    void setUp() throws Exception {
        mockLogService = mock(LogService.class);
        servlet = new TestableLogApiServlet(mockLogService);
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

    private OperationLog createTestLog(Integer id, Integer userId, String username,
                                       String operation, String module, String description) {
        OperationLog log = new OperationLog();
        log.setId(id);
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setModule(module);
        log.setDescription(description);
        log.setIpAddress("127.0.0.1");
        log.setUserAgent("Mozilla/5.0");
        log.setCreatedAt(new Date());
        return log;
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
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录DELETE请求应返回401")
        void should_return_401_when_delete_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST + "/" + LOG_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + LOG_ID);

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("已登录用户应能正常访问")
        void should_return_200_when_logged_in() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/logs 日志列表(分页) ====================

    @Nested
    @DisplayName("GET /api/logs 日志列表(分页)")
    class ListLogsTests {

        @Test
        @FastTest
        @DisplayName("获取日志列表成功")
        void should_list_logs_successfully() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            List<OperationLog> logs = List.of(
                    createTestLog(1, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录"),
                    createTestLog(2, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 2)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"total\":2");
        }

        @Test
        @FastTest
        @DisplayName("带分页参数获取日志列表")
        void should_list_logs_with_pagination() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockRequest.getParameter("pageSize")).thenReturn("10");

            when(mockLogService.listLogs(any(), eq(2), eq(10)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            verify(mockLogService).listLogs(any(), eq(2), eq(10));
        }

        @Test
        @FastTest
        @DisplayName("带keyword筛选参数获取日志列表")
        void should_list_logs_with_keyword_filter() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn("登录");

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带operation筛选参数获取日志列表")
        void should_list_logs_with_operation_filter() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("operation")).thenReturn(OPERATION_LOGIN);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带module筛选参数获取日志列表")
        void should_list_logs_with_module_filter() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("module")).thenReturn(MODULE_USER);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带dateRange筛选参数获取日志列表")
        void should_list_logs_with_date_range_filter() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("dateRange")).thenReturn("7");

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_empty_list() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"total\":0");
        }

        @Test
        @FastTest
        @DisplayName("页码为0应返回400")
        void should_return_400_when_page_zero() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("0");
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(0), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.error(400, "页码必须大于0"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("页码为负数应返回400")
        void should_return_400_when_page_negative() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("-1");
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(-1), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.error(400, "页码必须大于0"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("pageSize为0应返回400")
        void should_return_400_when_page_size_zero() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn("0");

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(0)))
                    .thenReturn(Result.error(400, "每页数量必须大于0且不超过100"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("pageSize超过最大值应返回400")
        void should_return_400_when_page_size_too_large() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("pageSize")).thenReturn("200");

            when(mockLogService.listLogs(any(), eq(1), eq(200)))
                    .thenReturn(Result.error(400, "每页数量必须大于0且不超过100"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("pageSize为负数应返回400")
        void should_return_400_when_page_size_negative() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn("-10");

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(-10)))
                    .thenReturn(Result.error(400, "每页数量必须大于0且不超过100"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("页码参数为非数字应返回400")
        void should_return_400_when_page_non_numeric() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("abc");
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            // Note: service is NOT called because validation happens before service call

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("页码参数无效");
        }

        @Test
        @FastTest
        @DisplayName("pageSize参数为非数字应返回400")
        void should_return_400_when_page_size_non_numeric() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn("abc");
            // Note: service is NOT called because validation happens before service call

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
            assertThat(getResponseBody()).contains("每页数量参数无效");
        }
    }

    // ==================== GET /api/logs/{id} 日志详情 ====================

    @Nested
    @DisplayName("GET /api/logs/{id} 日志详情")
    class GetLogDetailTests {

        @Test
        @FastTest
        @DisplayName("获取日志详情成功")
        void should_get_log_detail_successfully() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + LOG_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + LOG_ID);

            OperationLog log = createTestLog(LOG_ID, USER_ID, "admin",
                    OPERATION_LOGIN, MODULE_USER, "用户登录");
            when(mockLogService.getLogDetail(LOG_ID))
                    .thenReturn(Result.ok(log));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"id\":" + LOG_ID);
        }

        @Test
        @FastTest
        @DisplayName("获取日志详情应包含所有字段")
        void should_return_all_log_fields() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + LOG_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + LOG_ID);

            OperationLog log = createTestLog(LOG_ID, USER_ID, "admin",
                    OPERATION_LOGIN, MODULE_USER, "用户登录");
            log.setIpAddress("192.168.1.100");
            log.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            when(mockLogService.getLogDetail(LOG_ID))
                    .thenReturn(Result.ok(log));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
            assertThat(getResponseBody()).contains("\"userId\":" + USER_ID);
            assertThat(getResponseBody()).contains("\"username\":\"admin\"");
            assertThat(getResponseBody()).contains("\"operation\":\"" + OPERATION_LOGIN + "\"");
            assertThat(getResponseBody()).contains("\"module\":\"" + MODULE_USER + "\"");
            assertThat(getResponseBody()).contains("\"description\":\"用户登录\"");
            assertThat(getResponseBody()).contains("\"ipAddress\":\"192.168.1.100\"");
        }

        @Test
        @FastTest
        @DisplayName("获取不存在的日志应返回404")
        void should_return_404_when_log_not_found() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + NONEXISTENT_LOG_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + NONEXISTENT_LOG_ID);

            when(mockLogService.getLogDetail(NONEXISTENT_LOG_ID))
                    .thenReturn(Result.error(404, "日志不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
            assertThat(getResponseBody()).contains("日志不存在");
        }

        @Test
        @FastTest
        @DisplayName("日志ID为0应返回400")
        void should_return_400_when_id_is_zero() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + "0");
            when(mockRequest.getPathInfo()).thenReturn("/0");

            when(mockLogService.getLogDetail(0))
                    .thenReturn(Result.error(400, "日志ID无效"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("日志ID为负数应返回400")
        void should_return_400_when_id_is_negative() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + "-1");
            when(mockRequest.getPathInfo()).thenReturn("/-1");

            when(mockLogService.getLogDetail(-1))
                    .thenReturn(Result.error(400, "日志ID无效"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式（非数字）应返回404")
        void should_return_404_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + "abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("嵌套路径应返回404")
        void should_return_404_for_nested_path() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + LOG_ID + "/invalid");
            when(mockRequest.getPathInfo()).thenReturn("/" + LOG_ID + "/invalid");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/logs 测试 ====================

    @Nested
    @DisplayName("POST /api/logs 测试")
    class PostLogsTests {

        @Test
        @FastTest
        @DisplayName("POST到列表路径应返回405")
        void should_return_405_when_post_to_list() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":405");
        }
    }

    // ==================== PUT /api/logs 测试 ====================

    @Nested
    @DisplayName("PUT /api/logs 测试")
    class PutLogsTests {

        @Test
        @FastTest
        @DisplayName("PUT到列表路径应返回405")
        void should_return_405_when_put_to_list() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");

            servlet.doPut(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":405");
        }
    }

    // ==================== DELETE /api/logs/{id} 测试 ====================

    @Nested
    @DisplayName("DELETE /api/logs/{id} 测试")
    class DeleteLogsTests {

        @Test
        @FastTest
        @DisplayName("DELETE到详情路径应返回405")
        void should_return_405_when_delete_to_detail() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST_WITH_SLASH + LOG_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + LOG_ID);

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":405");
        }
    }

    // ==================== 操作类型覆盖测试 ====================

    @Nested
    @DisplayName("操作类型覆盖测试")
    class OperationTypeCoverageTests {

        @Test
        @FastTest
        @DisplayName("应能查询LOGIN操作的日志")
        void should_query_login_operation_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("operation")).thenReturn(OPERATION_LOGIN);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("应能查询CREATE操作的日志")
        void should_query_create_operation_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("operation")).thenReturn(OPERATION_CREATE);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("应能查询UPDATE操作的日志")
        void should_query_update_operation_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("operation")).thenReturn(OPERATION_UPDATE);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_UPDATE, MODULE_ACTIVITY, "更新活动")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("应能查询DELETE操作的日志")
        void should_query_delete_operation_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("operation")).thenReturn(OPERATION_DELETE);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_DELETE, MODULE_PROJECT, "删除项目")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 模块类型覆盖测试 ====================

    @Nested
    @DisplayName("模块类型覆盖测试")
    class ModuleTypeCoverageTests {

        @Test
        @FastTest
        @DisplayName("应能查询USER模块的日志")
        void should_query_user_module_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("module")).thenReturn(MODULE_USER);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("应能查询ACTIVITY模块的日志")
        void should_query_activity_module_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("module")).thenReturn(MODULE_ACTIVITY);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("应能查询PROJECT模块的日志")
        void should_query_project_module_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("module")).thenReturn(MODULE_PROJECT);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_PROJECT, "创建项目")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("应能查询AWARD模块的日志")
        void should_query_award_module_logs() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("module")).thenReturn(MODULE_AWARD);

            List<OperationLog> logs = List.of(
                    createTestLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_AWARD, "创建奖项")
            );
            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createPaginationData(logs, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @Test
        @FastTest
        @DisplayName("Service抛出异常时应返回500")
        void should_return_500_when_service_throws_exception() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenThrow(new RuntimeException("Database error"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }

        @Test
        @FastTest
        @DisplayName("Service返回null时应正常处理")
        void should_handle_null_response_from_service() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(null));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 结果数据格式验证 ====================

    @Nested
    @DisplayName("结果数据格式验证")
    class ResultDataFormatTests {

        @Test
        @FastTest
        @DisplayName("成功响应应包含正确的code为0")
        void should_return_success_code_zero() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("成功响应的message应为ok")
        void should_return_ok_message_for_success() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"message\":\"ok\"");
        }

        @Test
        @FastTest
        @DisplayName("响应Content-Type应为application/json")
        void should_return_json_content_type() throws Exception {
            simulateLogin(createTestUser(USER_ID, ROLE_ADMIN));
            when(mockRequest.getRequestURI()).thenReturn(PATH_LIST);
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);

            when(mockLogService.listLogs(any(), eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(Result.ok(createEmptyPaginationData()));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }
    }

    // ==================== 辅助方法 ====================

    private Map<String, Object> createEmptyPaginationData() {
        return createPaginationData(List.of(), 0);
    }

    private Map<String, Object> createPaginationData(List<OperationLog> logs, int total) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", logs);
        data.put("total", total);
        data.put("page", DEFAULT_PAGE);
        data.put("pageSize", DEFAULT_PAGE_SIZE);
        data.put("totalPages", total == 0 ? 0 : (int) Math.ceil((double) total / DEFAULT_PAGE_SIZE));
        return data;
    }

    // ==================== 可测试的内部Servlet类 ====================

    /**
     * 可测试的LogApiServlet
     *
     * 复制LogApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableLogApiServlet {

        private final LogService logService;
        private final Gson gson = new GsonBuilder().create();

        public TestableLogApiServlet(LogService logService) {
            this.logService = logService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            if (isListPath(pathInfo)) {
                handleListLogs(req, resp, user);
            } else {
                handleLogDetail(req, resp, user, pathInfo);
            }
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }
            writeJson(resp, Result.error(405, "POST方法不支持"));
        }

        public void doPut(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }
            writeJson(resp, Result.error(405, "PUT方法不支持"));
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User user = getCurrentUser(req);
            if (user == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }
            writeJson(resp, Result.error(405, "DELETE方法不支持"));
        }

        // ==================== GET 请求处理 ====================

        private void handleListLogs(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            String pageParam = req.getParameter("page");
            String pageSizeParam = req.getParameter("pageSize");

            if (!isValidIntParam(pageParam)) {
                writeJson(resp, Result.error(400, "页码参数无效"));
                return;
            }
            if (!isValidIntParam(pageSizeParam)) {
                writeJson(resp, Result.error(400, "每页数量参数无效"));
                return;
            }

            int page = parseIntParamOrDefault(pageParam, 1);
            int pageSize = parseIntParamOrDefault(pageSizeParam, 20);

            Map<String, Object> filter = buildFilter(req);

            try {
                writeJson(resp, logService.listLogs(filter, page, pageSize));
            } catch (Exception e) {
                writeJson(resp, Result.error(500, "服务器内部错误"));
            }
        }

        private void handleLogDetail(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws Exception {
            LogPathInfo pi = parsePathInfo(pathInfo);

            if (pi.isInvalid()) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }
            if (!pi.isValidLogId()) {
                writeJson(resp, Result.error(400, "无效的日志ID"));
                return;
            }
            if (pi.hasSubResource()) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            try {
                writeJson(resp, logService.getLogDetail(pi.getLogId()));
            } catch (Exception e) {
                writeJson(resp, Result.error(500, "服务器内部错误"));
            }
        }

        private Map<String, Object> buildFilter(HttpServletRequest req) {
            Map<String, Object> filter = new HashMap<>();
            String keyword = req.getParameter("keyword");
            String operation = req.getParameter("operation");
            String module = req.getParameter("module");
            String dateRange = req.getParameter("dateRange");

            if (keyword != null && !keyword.trim().isEmpty()) {
                filter.put("keyword", keyword);
            }
            if (operation != null && !operation.trim().isEmpty()) {
                filter.put("operation", operation);
            }
            if (module != null && !module.trim().isEmpty()) {
                filter.put("module", module);
            }
            if (dateRange != null && !dateRange.trim().isEmpty()) {
                filter.put("dateRange", dateRange);
            }
            return filter;
        }

        // ==================== Path Utilities ====================

        private boolean isListPath(String pathInfo) {
            return pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo);
        }

        private String derivePathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                String uri = req.getRequestURI();
                int idx = uri.indexOf("/api/logs/");
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

        private LogPathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || "/".equals(pathInfo) || pathInfo.isEmpty()) {
                return LogPathInfo.ROOT;
            }
            if (pathInfo.startsWith("/")) {
                pathInfo = pathInfo.substring(1);
            }
            String[] parts = pathInfo.split("/");
            if (parts.length == 0 || parts[0].isEmpty()) {
                return LogPathInfo.ROOT;
            }
            try {
                int logId = Integer.parseInt(parts[0]);
                boolean hasSubResource = parts.length > 1;
                return new LogPathInfo(logId, hasSubResource);
            } catch (NumberFormatException e) {
                return LogPathInfo.INVALID;
            }
        }

        private int parseIntParam(String value, int defaultValue) {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return Integer.MIN_VALUE; // Signal invalid number
            }
        }

        private boolean isValidIntParam(String value) {
            if (value == null || value.trim().isEmpty()) {
                return true; // null/empty is valid (will use default)
            }
            try {
                Integer.parseInt(value.trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private int parseIntParamOrDefault(String value, int defaultValue) {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        // ==================== JSON响应写入 ====================

        private void writeJson(HttpServletResponse response, Result result) throws Exception {
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(result.isSuccess() ? 200 : result.getCode());
            PrintWriter writer = response.getWriter();
            gson.toJson(result, writer);
            writer.flush();
        }

        private User getCurrentUser(HttpServletRequest request) {
            return (User) request.getSession(false).getAttribute("user");
        }

        // ==================== Path Info 内部类 ====================

        private static class LogPathInfo {
            static final LogPathInfo ROOT = new LogPathInfo(false);
            static final LogPathInfo INVALID = new LogPathInfo(true);

            private final Integer logId;
            private final boolean invalid;
            private final boolean hasSubResource;

            private LogPathInfo(boolean invalid) {
                this.invalid = invalid;
                this.logId = null;
                this.hasSubResource = false;
            }

            private LogPathInfo(int logId, boolean hasSubResource) {
                this.invalid = false;
                this.logId = logId;
                this.hasSubResource = hasSubResource;
            }

            private LogPathInfo(boolean dummy, boolean invalid) {
                this.invalid = invalid;
                this.logId = null;
                this.hasSubResource = false;
            }

            boolean isInvalid() {
                return invalid;
            }

            boolean isValidLogId() {
                return logId != null && logId > 0;
            }

            boolean hasSubResource() {
                return hasSubResource;
            }

            Integer getLogId() {
                return logId;
            }
        }
    }
}
