package com.softwaregroup.monitor.controller;

import com.softwaregroup.monitor.service.LogService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LogController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class LogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LogService logService;

    @InjectMocks
    private LogController logController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logController).build();
    }

    @Test
    void listLogs_shouldReturnLogList() throws Exception {
        when(logService.listLogs(any(), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listLogs_withPagination_shouldPassPaginationParams() throws Exception {
        when(logService.listLogs(any(), eq(2), eq(50))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/logs")
                        .param("page", "2")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listLogs_withFilter_shouldPassFilterParams() throws Exception {
        when(logService.listLogs(any(), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/logs")
                        .param("action", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getLogDetail_withValidId_shouldReturnDetail() throws Exception {
        when(logService.getLogDetail(eq(1))).thenReturn(Result.ok(Map.of("id", 1, "action", "LOGIN")));

        mockMvc.perform(get("/api/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.action").value("LOGIN"));
    }

    @Test
    void getLogDetail_withInvalidId_shouldReturn404() throws Exception {
        when(logService.getLogDetail(eq(999))).thenReturn(Result.error(404, "日志不存在"));

        mockMvc.perform(get("/api/logs/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void health_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/logs/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("monitor-service"));
    }
}
