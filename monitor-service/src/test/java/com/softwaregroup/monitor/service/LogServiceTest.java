package com.softwaregroup.monitor.service;

import com.softwaregroup.monitor.dao.OperationLogDAO;
import com.softwaregroup.monitor.model.OperationLog;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LogService 单元测试
 *
 * 测试覆盖：
 * - 日志列表查询（分页、筛选）
 * - 日志详情查询
 * - 所有边界情况
 * - 所有异常场景
 * - 所有筛选条件组合
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志服务测试")
class LogServiceTest {

    @Mock
    private OperationLogDAO operationLogDAO;

    @InjectMocks
    private LogService logService;

    private OperationLog sampleLog;
    private Map<String, Object> emptyFilter;

    @BeforeEach
    void setUp() {
        sampleLog = new OperationLog();
        sampleLog.setId(1);
        sampleLog.setUserId(1);
        sampleLog.setUsername("admin");
        sampleLog.setOperation("LOGIN");
        sampleLog.setModule("AUTH");
        sampleLog.setDescription("用户登录");
        sampleLog.setIpAddress("127.0.0.1");
        sampleLog.setUserAgent("Mozilla/5.0");
        sampleLog.setCreatedAt(new Date());

        emptyFilter = new HashMap<>();
    }

    // ==================== listLogs 测试 ====================

    @Nested
    @DisplayName("listLogs - 日志列表查询")
    class ListLogsTests {

        @Test
        @DisplayName("查询成功 - 无筛选条件，返回分页数据")
        void should_return_paginated_data_when_no_filter() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findAll(1, 20)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(1);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data.get("list"));
            assertEquals(1, data.get("total"));
            assertEquals(1, data.get("page"));
            assertEquals(20, data.get("pageSize"));
            assertEquals(1, data.get("totalPages"));
        }

        @Test
        @DisplayName("查询成功 - 空Filter对象")
        void should_work_with_empty_filter() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findAll(1, 20)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(1);

            // When
            Result result = logService.listLogs(emptyFilter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询成功 - 有筛选条件")
        void should_filter_with_conditions() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findByConditions(
                eq("admin"), eq("LOGIN"), eq("AUTH"), isNull(), eq(1), eq(20)))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(
                eq("admin"), eq("LOGIN"), eq("AUTH"), isNull()))
                .thenReturn(1);

            Map<String, Object> filter = new HashMap<>();
            filter.put("keyword", "admin");
            filter.put("operation", "LOGIN");
            filter.put("module", "AUTH");

            // When
            Result result = logService.listLogs(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            verify(operationLogDAO).findByConditions("admin", "LOGIN", "AUTH", null, 1, 20);
        }

        @Test
        @DisplayName("查询成功 - 只按keyword筛选")
        void should_filter_by_keyword_only() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findByConditions(
                eq("张三"), isNull(), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(
                eq("张三"), isNull(), isNull(), isNull()))
                .thenReturn(1);

            Map<String, Object> filter = new HashMap<>();
            filter.put("keyword", "张三");

            // When
            Result result = logService.listLogs(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询成功 - 只按operation筛选")
        void should_filter_by_operation_only() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findByConditions(
                isNull(), eq("LOGIN"), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(
                isNull(), eq("LOGIN"), isNull(), isNull()))
                .thenReturn(1);

            Map<String, Object> filter = new HashMap<>();
            filter.put("operation", "LOGIN");

            // When
            Result result = logService.listLogs(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询成功 - 只按module筛选")
        void should_filter_by_module_only() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findByConditions(
                isNull(), isNull(), eq("USER"), isNull(), eq(1), eq(20)))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(
                isNull(), isNull(), eq("USER"), isNull()))
                .thenReturn(1);

            Map<String, Object> filter = new HashMap<>();
            filter.put("module", "USER");

            // When
            Result result = logService.listLogs(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询成功 - 只按dateRange筛选")
        void should_filter_by_dateRange_only() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findByConditions(
                isNull(), isNull(), isNull(), eq("2024-01-01"), eq(1), eq(20)))
                .thenReturn(logs);
            when(operationLogDAO.countByConditions(
                isNull(), isNull(), isNull(), eq("2024-01-01")))
                .thenReturn(1);

            Map<String, Object> filter = new HashMap<>();
            filter.put("dateRange", "2024-01-01");

            // When
            Result result = logService.listLogs(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询成功 - 返回空列表")
        void should_return_empty_list() {
            // Given
            when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList());
            when(operationLogDAO.countAll()).thenReturn(0);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertTrue(((List<?>) data.get("list")).isEmpty());
            assertEquals(0, data.get("total"));
            assertEquals(0, data.get("totalPages"));
        }

        @Test
        @DisplayName("查询成功 - 总页数计算正确")
        void should_calculate_total_pages_correctly() {
            // Given - 95条记录，每页20条
            List<OperationLog> logs = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                logs.add(sampleLog);
            }
            when(operationLogDAO.findAll(1, 20)).thenReturn(logs);
            when(operationLogDAO.countAll()).thenReturn(95);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(95, data.get("total"));
            assertEquals(5, data.get("totalPages")); // ceil(95/20) = 5
        }

        @Test
        @DisplayName("查询成功 - 总页数为0当无数据")
        void should_return_zero_pages_when_no_data() {
            // Given
            when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList());
            when(operationLogDAO.countAll()).thenReturn(0);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(0, data.get("totalPages"));
        }

        @Test
        @DisplayName("查询成功 - page为2")
        void should_handle_page_2() {
            // Given
            when(operationLogDAO.findAll(2, 20)).thenReturn(Arrays.asList());
            when(operationLogDAO.countAll()).thenReturn(30);

            // When
            Result result = logService.listLogs(null, 2, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(2, data.get("page"));
        }

        // ==================== 分页参数验证 ====================

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("查询失败 - 页码必须大于0")
        void should_fail_when_page_invalid(int page) {
            // When
            Result result = logService.listLogs(null, page, 20);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("页码必须大于0", result.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 101})
        @DisplayName("查询失败 - pageSize必须大于0且不超过100")
        void should_fail_when_pageSize_invalid(int pageSize) {
            // When
            Result result = logService.listLogs(null, 1, pageSize);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("每页数量必须大于0且不超过100", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - pageSize正好为100")
        void should_pass_when_pageSize_100() {
            // Given
            when(operationLogDAO.findAll(1, 100)).thenReturn(Arrays.asList());
            when(operationLogDAO.countAll()).thenReturn(0);

            // When
            Result result = logService.listLogs(null, 1, 100);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询成功 - pageSize正好为1")
        void should_pass_when_pageSize_1() {
            // Given
            when(operationLogDAO.findAll(1, 1)).thenReturn(Arrays.asList(sampleLog));
            when(operationLogDAO.countAll()).thenReturn(1);

            // When
            Result result = logService.listLogs(null, 1, 1);

            // Then
            assertTrue(result.isSuccess());
        }

        // ==================== 数据库异常处理 ====================

        @Test
        @DisplayName("查询失败 - 数据库异常")
        void should_handle_database_error() {
            // Given
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database connection error"));

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(500, result.getCode());
            assertEquals("数据库错误", result.getMessage());
        }
    }

    // ==================== getLogDetail 测试 ====================

    @Nested
    @DisplayName("getLogDetail - 日志详情查询")
    class GetLogDetailTests {

        @Test
        @DisplayName("查询成功 - 日志存在")
        void should_return_log_when_exists() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findAll(1, 100)).thenReturn(logs);

            // When
            Result result = logService.getLogDetail(1);

            // Then
            assertTrue(result.isSuccess());
            OperationLog log = (OperationLog) result.getData();
            assertEquals(1, log.getId());
            assertEquals("admin", log.getUsername());
        }

        @Test
        @DisplayName("查询失败 - 日志不存在")
        void should_return_error_when_not_found() {
            // Given
            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findAll(1, 100)).thenReturn(logs);

            // When
            Result result = logService.getLogDetail(999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("日志不存在", result.getMessage());
        }

        @Test
        @DisplayName("查询成功 - 找到多条记录中匹配的那条")
        void should_find_correct_log_in_multiple_results() {
            // Given
            OperationLog log1 = new OperationLog();
            log1.setId(1);
            log1.setUsername("user1");

            OperationLog log2 = new OperationLog();
            log2.setId(2);
            log2.setUsername("user2");

            OperationLog log3 = new OperationLog();
            log3.setId(3);
            log3.setUsername("user3");

            when(operationLogDAO.findAll(1, 100)).thenReturn(Arrays.asList(log1, log2, log3));

            // When
            Result result = logService.getLogDetail(2);

            // Then
            assertTrue(result.isSuccess());
            OperationLog log = (OperationLog) result.getData();
            assertEquals(2, log.getId());
            assertEquals("user2", log.getUsername());
        }

        // ==================== ID验证 ====================

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("查询失败 - ID必须大于0")
        void should_fail_when_id_invalid(int id) {
            // When
            Result result = logService.getLogDetail(id);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("日志ID无效", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - ID为null")
        void should_fail_when_id_null() {
            // When
            Result result = logService.getLogDetail(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("日志ID无效", result.getMessage());
        }

        // ==================== 数据库异常处理 ====================

        @Test
        @DisplayName("查询失败 - 数据库异常")
        void should_handle_database_error() {
            // Given
            when(operationLogDAO.findAll(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

            // When
            Result result = logService.getLogDetail(1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(500, result.getCode());
            assertEquals("数据库错误", result.getMessage());
        }
    }

    // ==================== 边界情况测试 ====================

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("查询成功 - 返回结果中list为null时保护")
        void should_handle_null_list_from_dao() {
            // Given
            when(operationLogDAO.findAll(1, 20)).thenReturn(null);
            when(operationLogDAO.countAll()).thenReturn(0);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertNotNull(data.get("list"));
            assertTrue(((List<?>) data.get("list")).isEmpty());
        }

        @Test
        @DisplayName("查询成功 - total为负数时修正为0")
        void should_normalize_negative_total() {
            // Given
            when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList());
            when(operationLogDAO.countAll()).thenReturn(-5);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertEquals(0, data.get("total"));
        }

        @Test
        @DisplayName("getLogDetail - 日志ID为null时不抛异常")
        void should_handle_null_id_gracefully() {
            // When
            Result result = logService.getLogDetail(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
        }

        @Test
        @DisplayName("getLogDetail - 日志ID为0时不抛异常")
        void should_handle_zero_id_gracefully() {
            // When
            Result result = logService.getLogDetail(0);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
        }

        @Test
        @DisplayName("Filter中包含非字符串值时不崩溃")
        void should_handle_non_string_filter_values() {
            // Given
            Map<String, Object> filter = new HashMap<>();
            filter.put("keyword", 123); // Integer instead of String
            filter.put("page", 1); // Additional key

            when(operationLogDAO.findByConditions(
                eq("123"), isNull(), isNull(), isNull(), eq(1), eq(20)))
                .thenReturn(Arrays.asList());
            when(operationLogDAO.countByConditions(
                eq("123"), isNull(), isNull(), isNull()))
                .thenReturn(0);

            // When
            Result result = logService.listLogs(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }
    }

    // ==================== 数据完整性测试 ====================

    @Nested
    @DisplayName("数据完整性测试")
    class DataIntegrityTests {

        @Test
        @DisplayName("listLogs返回的分页数据结构完整")
        void should_return_complete_pagination_structure() {
            // Given
            when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList(sampleLog));
            when(operationLogDAO.countAll()).thenReturn(50);

            // When
            Result result = logService.listLogs(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();

            // 验证所有必要字段都存在
            assertNotNull(data.get("list"));
            assertNotNull(data.get("total"));
            assertNotNull(data.get("page"));
            assertNotNull(data.get("pageSize"));
            assertNotNull(data.get("totalPages"));

            // 验证类型
            assertTrue(data.get("list") instanceof List);
            assertTrue(data.get("total") instanceof Integer);
            assertTrue(data.get("page") instanceof Integer);
            assertTrue(data.get("pageSize") instanceof Integer);
            assertTrue(data.get("totalPages") instanceof Integer);
        }

        @Test
        @DisplayName("返回的日志对象包含所有字段")
        void should_return_log_with_all_fields() {
            // Given
            sampleLog.setOperation("DELETE");
            sampleLog.setModule("USER");
            sampleLog.setDescription("删除用户");
            sampleLog.setIpAddress("192.168.1.1");
            sampleLog.setUserAgent("TestAgent/1.0");

            List<OperationLog> logs = Arrays.asList(sampleLog);
            when(operationLogDAO.findAll(1, 100)).thenReturn(logs);

            // When
            Result result = logService.getLogDetail(1);

            // Then
            assertTrue(result.isSuccess());
            OperationLog log = (OperationLog) result.getData();
            assertEquals("DELETE", log.getOperation());
            assertEquals("USER", log.getModule());
            assertEquals("删除用户", log.getDescription());
            assertEquals("192.168.1.1", log.getIpAddress());
            assertEquals("TestAgent/1.0", log.getUserAgent());
        }
    }
}
