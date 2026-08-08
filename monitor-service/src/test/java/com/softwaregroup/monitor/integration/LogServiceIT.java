package com.softwaregroup.monitor.integration;

import com.softwaregroup.monitor.dao.OperationLogDAO;
import com.softwaregroup.monitor.model.OperationLog;
import com.softwaregroup.monitor.service.LogService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LogService 集成测试
 *
 * 测试操作日志服务的核心功能：日志列表查询、日志详情
 */
@ExtendWith(MockitoExtension.class)
class LogServiceIT {

    @Mock
    private OperationLogDAO operationLogDAO;

    private LogService logService;

    @BeforeEach
    void setUp() {
        logService = new LogService(operationLogDAO);
    }

    @Test
    void listLogs_withValidParams_shouldReturnLogList() {
        OperationLog log = new OperationLog();
        log.setId(1);
        log.setOperation("用户登录");
        log.setModule("认证模块");

        when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList(log));
        when(operationLogDAO.countAll()).thenReturn(1);

        Result result = logService.listLogs(null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(operationLogDAO).findAll(1, 20);
        verify(operationLogDAO).countAll();
    }

    @Test
    void listLogs_withEmptyFilter_shouldReturnAllLogs() {
        when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList());
        when(operationLogDAO.countAll()).thenReturn(0);

        Map<String, Object> filter = new HashMap<>();

        Result result = logService.listLogs(filter, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(operationLogDAO).findAll(1, 20);
    }

    @Test
    void listLogs_withKeywordFilter_shouldReturnFilteredLogs() {
        when(operationLogDAO.findByConditions(eq("登录"), isNull(), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(Arrays.asList());
        when(operationLogDAO.countByConditions(eq("登录"), isNull(), isNull(), isNull()))
            .thenReturn(0);

        Map<String, Object> filter = new HashMap<>();
        filter.put("keyword", "登录");

        Result result = logService.listLogs(filter, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(operationLogDAO).findByConditions(eq("登录"), isNull(), isNull(), isNull(), eq(1), eq(20));
    }

    @Test
    void listLogs_withOperationFilter_shouldReturnFilteredLogs() {
        when(operationLogDAO.findByConditions(isNull(), eq("登录"), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(Arrays.asList());
        when(operationLogDAO.countByConditions(isNull(), eq("登录"), isNull(), isNull()))
            .thenReturn(0);

        Map<String, Object> filter = new HashMap<>();
        filter.put("operation", "登录");

        Result result = logService.listLogs(filter, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(operationLogDAO).findByConditions(isNull(), eq("登录"), isNull(), isNull(), eq(1), eq(20));
    }

    @Test
    void listLogs_withModuleFilter_shouldReturnFilteredLogs() {
        when(operationLogDAO.findByConditions(isNull(), isNull(), eq("用户管理"), isNull(), eq(1), eq(20)))
            .thenReturn(Arrays.asList());
        when(operationLogDAO.countByConditions(isNull(), isNull(), eq("用户管理"), isNull()))
            .thenReturn(0);

        Map<String, Object> filter = new HashMap<>();
        filter.put("module", "用户管理");

        Result result = logService.listLogs(filter, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(operationLogDAO).findByConditions(isNull(), isNull(), eq("用户管理"), isNull(), eq(1), eq(20));
    }

    @Test
    void listLogs_withCombinedFilters_shouldReturnFilteredLogs() {
        when(operationLogDAO.findByConditions(eq("登录"), eq("登录"), eq("认证"), isNull(), eq(1), eq(20)))
            .thenReturn(Arrays.asList());
        when(operationLogDAO.countByConditions(eq("登录"), eq("登录"), eq("认证"), isNull()))
            .thenReturn(0);

        Map<String, Object> filter = new HashMap<>();
        filter.put("keyword", "登录");
        filter.put("operation", "登录");
        filter.put("module", "认证");

        Result result = logService.listLogs(filter, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(operationLogDAO).findByConditions(eq("登录"), eq("登录"), eq("认证"), isNull(), eq(1), eq(20));
    }

    @Test
    void listLogs_withInvalidPage_shouldReturnError() {
        Result result = logService.listLogs(null, 0, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listLogs_withNegativePage_shouldReturnError() {
        Result result = logService.listLogs(null, -1, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listLogs_withNullPage_shouldReturnError() {
        Result result = logService.listLogs(null, null, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listLogs_withInvalidPageSize_shouldReturnError() {
        Result result = logService.listLogs(null, 1, 0);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listLogs_withTooLargePageSize_shouldReturnError() {
        Result result = logService.listLogs(null, 1, 200);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listLogs_withNullPageSize_shouldReturnError() {
        Result result = logService.listLogs(null, 1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listLogs_withNullResult_shouldReturnEmptyList() {
        when(operationLogDAO.findAll(1, 20)).thenReturn(null);
        when(operationLogDAO.countAll()).thenReturn(-1);

        Result result = logService.listLogs(null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void listLogs_shouldCalculateTotalPagesCorrectly() {
        when(operationLogDAO.findAll(1, 20)).thenReturn(Arrays.asList());
        when(operationLogDAO.countAll()).thenReturn(45);

        Result result = logService.listLogs(null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertThat(data.get("totalPages")).isEqualTo(3); // 45/20 = 2.25, ceil = 3
    }

    @Test
    void getLogDetail_withValidId_shouldReturnLog() {
        OperationLog log = new OperationLog();
        log.setId(1);
        log.setOperation("用户登录");

        when(operationLogDAO.findAll(1, 100)).thenReturn(Arrays.asList(log));

        Result result = logService.getLogDetail(1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(log);
    }

    @Test
    void getLogDetail_withInvalidId_shouldReturnNotFound() {
        when(operationLogDAO.findAll(1, 100)).thenReturn(Arrays.asList());

        Result result = logService.getLogDetail(9999);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void getLogDetail_withNullId_shouldReturnBadRequest() {
        Result result = logService.getLogDetail(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getLogDetail_withZeroId_shouldReturnBadRequest() {
        Result result = logService.getLogDetail(0);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getLogDetail_withNegativeId_shouldReturnBadRequest() {
        Result result = logService.getLogDetail(-1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getLogDetail_withDaoException_shouldReturnError() {
        when(operationLogDAO.findAll(1, 100)).thenThrow(new RuntimeException("DB error"));

        Result result = logService.getLogDetail(1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(500);
    }
}
