package service;

import dao.OperationLogDAO;
import model.OperationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LogService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 6.2 LogService 日志服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 *
 * 核心方法：
 * - listLogs(filter, page, pageSize) - 日志列表(分页)
 * - getLogDetail(id) - 日志详情
 *
 * 涉及的DAO：
 * - OperationLogDAO: findAll(page, pageSize) / findByConditions(keyword, operation, module, dateRange, page, pageSize)
 * - OperationLogDAO: countAll() / countByConditions(keyword, operation, module, dateRange)
 *
 * 业务规则：
 * - 分页参数必须大于0
 * - filter可以为null（返回所有日志）
 * - 日志按创建时间倒序排列
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LogService 日志服务测试")
class LogServiceTest {

    @Mock
    private OperationLogDAO operationLogDAO;

    private LogService logService;

    @BeforeEach
    void setUp() {
        logService = new LogService(operationLogDAO);
    }

    // ==================== 测试数据常量 ====================

    private static final Integer LOG_ID = 100;
    private static final Integer USER_ID = 1;
    private static final Integer PAGE = 1;
    private static final Integer PAGE_SIZE = 20;
    private static final Integer MAX_PAGE_SIZE = 100;

    // 操作类型枚举
    private static final String OPERATION_LOGIN = "LOGIN";
    private static final String OPERATION_LOGOUT = "LOGOUT";
    private static final String OPERATION_CREATE = "CREATE";
    private static final String OPERATION_UPDATE = "UPDATE";
    private static final String OPERATION_DELETE = "DELETE";

    // 模块枚举
    private static final String MODULE_USER = "USER";
    private static final String MODULE_ACTIVITY = "ACTIVITY";
    private static final String MODULE_PROJECT = "PROJECT";
    private static final String MODULE_AWARD = "AWARD";

    // ==================== 测试初始化辅助方法 ====================

    private OperationLog createOperationLog(Integer id, Integer userId, String username,
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

    private Map<String, Object> createLogFilter(String keyword, String operation,
                                               String module, String dateRange) {
        Map<String, Object> filter = new HashMap<>();
        if (keyword != null) filter.put("keyword", keyword);
        if (operation != null) filter.put("operation", operation);
        if (module != null) filter.put("module", module);
        if (dateRange != null) filter.put("dateRange", dateRange);
        return filter;
    }

    // ==================== listLogs 日志列表(分页) ====================

    @Nested
    @DisplayName("listLogs 日志列表(分页)")
    class ListLogsTests {

        @FastTest
        @DisplayName("获取日志列表成功应返回成功")
        void should_list_logs_successfully() {
            List<OperationLog> logs = Arrays.asList(
                createOperationLog(1, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录"),
                createOperationLog(2, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(2);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("日志列表应返回分页信息")
        void should_return_pagination_info() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(1);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsKey("list");
            assertThat(data).containsKey("total");
            assertThat(data).containsKey("page");
            assertThat(data).containsKey("pageSize");
        }

        @FastTest
        @DisplayName("日志列表应按创建时间倒序排列")
        void should_return_logs_ordered_by_created_at_desc() {
            List<OperationLog> logs = Arrays.asList(
                createOperationLog(2, USER_ID, "admin", OPERATION_UPDATE, MODULE_ACTIVITY, "更新活动"),
                createOperationLog(1, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(2);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            // DAO层已经按created_at DESC排序，Service层应保持该顺序
            List<OperationLog> resultLogs = (List<OperationLog>) ((Map<String, Object>) result.getData()).get("list");
            assertThat(resultLogs).hasSize(2);
            assertThat(resultLogs.get(0).getId()).isEqualTo(2);
            assertThat(resultLogs.get(1).getId()).isEqualTo(1);
        }

        @FastTest
        @DisplayName("filter为null时应返回所有日志")
        void should_return_all_logs_when_filter_is_null() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(1);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findAll(PAGE, PAGE_SIZE);
            verify(operationLogDAO).countAll();
        }

        @FastTest
        @DisplayName("filter为空Map时应返回所有日志")
        void should_return_all_logs_when_filter_is_empty() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(1);

            Result result = logService.listLogs(new HashMap<>(), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findAll(PAGE, PAGE_SIZE);
        }

        @FastTest
        @DisplayName("带keyword筛选条件应正确查询")
        void should_filter_by_keyword() {
            String keyword = "登录";
            Map<String, Object> filter = createLogFilter(keyword, null, null, null);
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(keyword, null, null, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(keyword, null, null, null))
                .thenReturn(1);

            Result result = logService.listLogs(filter, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findByConditions(keyword, null, null, null, PAGE, PAGE_SIZE);
            verify(operationLogDAO).countByConditions(keyword, null, null, null);
        }

        @FastTest
        @DisplayName("带operation筛选条件应正确查询")
        void should_filter_by_operation() {
            String operation = OPERATION_LOGIN;
            Map<String, Object> filter = createLogFilter(null, operation, null, null);
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(null, operation, null, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, operation, null, null))
                .thenReturn(1);

            Result result = logService.listLogs(filter, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findByConditions(null, operation, null, null, PAGE, PAGE_SIZE);
        }

        @FastTest
        @DisplayName("带module筛选条件应正确查询")
        void should_filter_by_module() {
            String module = MODULE_USER;
            Map<String, Object> filter = createLogFilter(null, null, module, null);
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(null, null, module, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, null, module, null))
                .thenReturn(1);

            Result result = logService.listLogs(filter, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findByConditions(null, null, module, null, PAGE, PAGE_SIZE);
        }

        @FastTest
        @DisplayName("带dateRange筛选条件应正确查询")
        void should_filter_by_date_range() {
            String dateRange = "7";
            Map<String, Object> filter = createLogFilter(null, null, null, dateRange);
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(null, null, null, dateRange, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, null, null, dateRange))
                .thenReturn(1);

            Result result = logService.listLogs(filter, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findByConditions(null, null, null, dateRange, PAGE, PAGE_SIZE);
        }

        @FastTest
        @DisplayName("带多个筛选条件应正确查询")
        void should_filter_by_multiple_conditions() {
            String keyword = "登录";
            String operation = OPERATION_LOGIN;
            String module = MODULE_USER;
            String dateRange = "7";
            Map<String, Object> filter = createLogFilter(keyword, operation, module, dateRange);
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(keyword, operation, module, dateRange, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(keyword, operation, module, dateRange))
                .thenReturn(1);

            Result result = logService.listLogs(filter, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(operationLogDAO).findByConditions(keyword, operation, module, dateRange, PAGE, PAGE_SIZE);
            verify(operationLogDAO).countByConditions(keyword, operation, module, dateRange);
        }

        @FastTest
        @DisplayName("日志列表为空时应返回空列表")
        void should_return_empty_list_when_no_logs() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat((List<?>) data.get("list")).isEmpty();
            assertThat(data.get("total")).isEqualTo(0);
        }

        // ==================== 边界情况 ====================

        @FastTest
        @DisplayName("页码为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = logService.listLogs(null, 0, PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("页码");
        }

        @FastTest
        @DisplayName("页码为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = logService.listLogs(null, -1, PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize为0时应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = logService.listLogs(null, PAGE, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每页");
        }

        @FastTest
        @DisplayName("pageSize为负数时应返回错误")
        void should_return_error_when_page_size_is_negative() {
            Result result = logService.listLogs(null, PAGE, -10);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize超过最大限制时应返回错误")
        void should_return_error_when_page_size_exceeds_max() {
            Result result = logService.listLogs(null, PAGE, MAX_PAGE_SIZE + 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每页数量");
        }

        @FastTest
        @DisplayName("第一页应从第0条开始查询")
        void should_query_from_offset_zero_for_first_page() {
            when(operationLogDAO.findAll(1, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            logService.listLogs(null, 1, PAGE_SIZE);

            verify(operationLogDAO).findAll(1, PAGE_SIZE);
        }

        @FastTest
        @DisplayName("第二页应正确计算偏移量")
        void should_calculate_correct_offset_for_second_page() {
            when(operationLogDAO.findAll(2, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            logService.listLogs(null, 2, PAGE_SIZE);

            verify(operationLogDAO).findAll(2, PAGE_SIZE);
        }

        @FastTest
        @DisplayName("最后一页不足一页数据应正常返回")
        void should_handle_partial_last_page() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findAll(2, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(21); // 21条数据，第2页只有1条

            Result result = logService.listLogs(null, 2, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat((List<?>) data.get("list")).hasSize(1);
            assertThat(data.get("total")).isEqualTo(21);
        }
    }

    // ==================== getLogDetail 日志详情 ====================

    @Nested
    @DisplayName("getLogDetail 日志详情")
    class GetLogDetailTests {

        @FastTest
        @DisplayName("获取日志详情成功应返回成功")
        void should_get_log_detail_successfully() {
            OperationLog log = createOperationLog(LOG_ID, USER_ID, "admin",
                OPERATION_LOGIN, MODULE_USER, "用户登录");
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(log));

            Result result = logService.getLogDetail(LOG_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("获取日志详情应包含所有字段")
        void should_return_all_log_fields() {
            OperationLog log = createOperationLog(LOG_ID, USER_ID, "admin",
                OPERATION_LOGIN, MODULE_USER, "用户登录");
            log.setIpAddress("192.168.1.100");
            log.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(log));

            Result result = logService.getLogDetail(LOG_ID);

            assertThat(result.isSuccess()).isTrue();
            OperationLog resultLog = (OperationLog) result.getData();
            assertThat(resultLog.getId()).isEqualTo(LOG_ID);
            assertThat(resultLog.getUserId()).isEqualTo(USER_ID);
            assertThat(resultLog.getUsername()).isEqualTo("admin");
            assertThat(resultLog.getOperation()).isEqualTo(OPERATION_LOGIN);
            assertThat(resultLog.getModule()).isEqualTo(MODULE_USER);
            assertThat(resultLog.getDescription()).isEqualTo("用户登录");
            assertThat(resultLog.getIpAddress()).isEqualTo("192.168.1.100");
            assertThat(resultLog.getUserAgent()).isEqualTo("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        }

        @FastTest
        @DisplayName("日志ID不存在时应返回错误")
        void should_return_error_when_log_not_found() {
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

            Result result = logService.getLogDetail(99999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("不存在");
        }

        @FastTest
        @DisplayName("日志ID为null时应返回错误")
        void should_return_error_when_log_id_is_null() {
            Result result = logService.getLogDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("日志ID为0时应返回错误")
        void should_return_error_when_log_id_is_zero() {
            Result result = logService.getLogDetail(0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("日志ID为负数时应返回错误")
        void should_return_error_when_log_id_is_negative() {
            Result result = logService.getLogDetail(-1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== 异常场景 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @FastTest
        @DisplayName("DAO抛出SQLException时应返回错误")
        void should_return_error_when_dao_throws_sql_exception() {
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database connection failed"));

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("数据库错误");
        }

        @FastTest
        @DisplayName("DAO返回null时应返回空列表")
        void should_return_empty_list_when_dao_returns_null() {
            when(operationLogDAO.findAll(anyInt(), anyInt())).thenReturn(null);
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat((List<?>) data.get("list")).isEmpty();
        }

        @FastTest
        @DisplayName("DAO count返回负数时应正常处理")
        void should_handle_negative_count() {
            when(operationLogDAO.findAll(anyInt(), anyInt())).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(-1);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data.get("total")).isEqualTo(0);
        }
    }

    // ==================== 分页计算验证 ====================

    @Nested
    @DisplayName("分页计算验证")
    class PaginationCalculationTests {

        @FastTest
        @DisplayName("总条数为0时totalPages应为0")
        void should_return_zero_total_pages_when_total_is_zero() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data.get("totalPages")).isEqualTo(0);
        }

        @FastTest
        @DisplayName("总条数正好为一页时totalPages应为1")
        void should_return_one_total_page_when_exact_one_page() {
            List<OperationLog> logs = new ArrayList<>();
            for (int i = 0; i < PAGE_SIZE; i++) {
                logs.add(createOperationLog(i + 1, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "日志" + i));
            }
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(PAGE_SIZE);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data.get("totalPages")).isEqualTo(1);
        }

        @FastTest
        @DisplayName("总条数超过一页时totalPages应大于1")
        void should_return_correct_total_pages() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(21);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data.get("totalPages")).isEqualTo((int) Math.ceil(21.0 / PAGE_SIZE));
        }

        @FastTest
        @DisplayName("返回数据应包含当前页码和每页大小")
        void should_return_current_page_and_page_size() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data.get("page")).isEqualTo(PAGE);
            assertThat(data.get("pageSize")).isEqualTo(PAGE_SIZE);
        }
    }

    // ==================== 操作类型覆盖 ====================

    @Nested
    @DisplayName("操作类型覆盖测试")
    class OperationTypeCoverageTests {

        @FastTest
        @DisplayName("应能查询LOGIN操作的日志")
        void should_query_login_operation_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(null, OPERATION_LOGIN, null, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, OPERATION_LOGIN, null, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, OPERATION_LOGIN, null, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应能查询CREATE操作的日志")
        void should_query_create_operation_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(operationLogDAO.findByConditions(null, OPERATION_CREATE, null, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, OPERATION_CREATE, null, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, OPERATION_CREATE, null, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应能查询UPDATE操作的日志")
        void should_query_update_operation_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_UPDATE, MODULE_ACTIVITY, "更新活动")
            );
            when(operationLogDAO.findByConditions(null, OPERATION_UPDATE, null, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, OPERATION_UPDATE, null, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, OPERATION_UPDATE, null, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应能查询DELETE操作的日志")
        void should_query_delete_operation_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_DELETE, MODULE_PROJECT, "删除项目")
            );
            when(operationLogDAO.findByConditions(null, OPERATION_DELETE, null, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, OPERATION_DELETE, null, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, OPERATION_DELETE, null, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 模块类型覆盖 ====================

    @Nested
    @DisplayName("模块类型覆盖测试")
    class ModuleTypeCoverageTests {

        @FastTest
        @DisplayName("应能查询USER模块的日志")
        void should_query_user_module_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_LOGIN, MODULE_USER, "用户登录")
            );
            when(operationLogDAO.findByConditions(null, null, MODULE_USER, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, null, MODULE_USER, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, null, MODULE_USER, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应能查询ACTIVITY模块的日志")
        void should_query_activity_module_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_ACTIVITY, "创建活动")
            );
            when(operationLogDAO.findByConditions(null, null, MODULE_ACTIVITY, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, null, MODULE_ACTIVITY, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, null, MODULE_ACTIVITY, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应能查询PROJECT模块的日志")
        void should_query_project_module_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_PROJECT, "创建项目")
            );
            when(operationLogDAO.findByConditions(null, null, MODULE_PROJECT, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, null, MODULE_PROJECT, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, null, MODULE_PROJECT, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("应能查询AWARD模块的日志")
        void should_query_award_module_logs() {
            List<OperationLog> logs = Collections.singletonList(
                createOperationLog(LOG_ID, USER_ID, "admin", OPERATION_CREATE, MODULE_AWARD, "创建奖项")
            );
            when(operationLogDAO.findByConditions(null, null, MODULE_AWARD, null, PAGE, PAGE_SIZE))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(null, null, MODULE_AWARD, null))
                .thenReturn(1);

            Result result = logService.listLogs(createLogFilter(null, null, MODULE_AWARD, null), PAGE, PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 结果数据格式验证 ====================

    @Nested
    @DisplayName("结果数据格式验证")
    class ResultDataFormatTests {

        @FastTest
        @DisplayName("成功响应应包含正确的code为0")
        void should_return_success_code_zero() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.getCode()).isEqualTo(0);
        }

        @FastTest
        @DisplayName("错误响应应包含正确的错误码")
        void should_return_error_code_for_invalid_params() {
            Result result = logService.listLogs(null, 0, PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("错误响应应包含错误消息")
        void should_return_error_message_for_invalid_params() {
            Result result = logService.listLogs(null, 0, PAGE_SIZE);

            assertThat(result.getMessage()).isNotNull();
            assertThat(result.getMessage()).isNotEmpty();
        }

        @FastTest
        @DisplayName("成功响应的message应为ok")
        void should_return_ok_message_for_success() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.getMessage()).isEqualTo("ok");
        }

        @FastTest
        @DisplayName("日志列表data应为Map类型")
        void should_return_map_type_for_list_data() {
            when(operationLogDAO.findAll(PAGE, PAGE_SIZE)).thenReturn(Collections.emptyList());
            when(operationLogDAO.countAll()).thenReturn(0);

            Result result = logService.listLogs(null, PAGE, PAGE_SIZE);

            assertThat(result.getData()).isInstanceOf(Map.class);
        }

        @FastTest
        @DisplayName("日志详情data应为OperationLog类型")
        void should_return_operation_log_type_for_detail_data() {
            OperationLog log = createOperationLog(LOG_ID, USER_ID, "admin",
                OPERATION_LOGIN, MODULE_USER, "用户登录");
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(log));

            Result result = logService.getLogDetail(LOG_ID);

            assertThat(result.getData()).isInstanceOf(OperationLog.class);
        }
    }
}
