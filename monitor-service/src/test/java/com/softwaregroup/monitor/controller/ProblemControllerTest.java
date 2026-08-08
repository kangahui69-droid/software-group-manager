package com.softwaregroup.monitor.controller;

import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.model.dto.ProblemFilterDTO;
import com.softwaregroup.monitor.service.ProblemService;
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
 * ProblemController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProblemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProblemService problemService;

    @InjectMocks
    private ProblemController problemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(problemController).build();
    }

    @Test
    void submitProblem_withValidData_shouldReturnSuccess() throws Exception {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");
        dto.setContent("测试内容");

        when(problemService.submitProblem(any(ProblemDTO.class), isNull())).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/problems")
                        .contentType("application/json")
                        .content("{\"title\":\"测试问题\",\"content\":\"测试内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void submitProblem_withUserId_shouldPassUserId() throws Exception {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");

        when(problemService.submitProblem(any(ProblemDTO.class), eq(1))).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/problems")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("{\"title\":\"测试问题\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void submitProblem_withInvalidData_shouldReturn400() throws Exception {
        when(problemService.submitProblem(any(ProblemDTO.class), isNull())).thenReturn(Result.error(400, "标题不能为空"));

        mockMvc.perform(post("/api/problems")
                        .contentType("application/json")
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getProblemDetail_withValidId_shouldReturnDetail() throws Exception {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");

        when(problemService.getProblemDetail(eq(1))).thenReturn(Result.ok(dto));

        mockMvc.perform(get("/api/problems/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("测试问题"));
    }

    @Test
    void getProblemDetail_withInvalidId_shouldReturn404() throws Exception {
        when(problemService.getProblemDetail(eq(999))).thenReturn(Result.error(404, "问题不存在"));

        mockMvc.perform(get("/api/problems/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listProblems_shouldReturnList() throws Exception {
        when(problemService.listProblems(any(ProblemFilterDTO.class), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/problems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listProblems_withFilters_shouldPassFilters() throws Exception {
        when(problemService.listProblems(any(ProblemFilterDTO.class), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/problems")
                        .param("category", "技术")
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getMyProblems_shouldReturnList() throws Exception {
        when(problemService.getMyProblems(isNull(), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/problems/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getMyProblems_withUserId_shouldPassUserId() throws Exception {
        when(problemService.getMyProblems(eq(1), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/problems/my")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateProblem_withValidData_shouldReturnSuccess() throws Exception {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("更新后的问题");

        when(problemService.updateProblem(eq(1), any(ProblemDTO.class), isNull())).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/problems/1")
                        .contentType("application/json")
                        .content("{\"title\":\"更新后的问题\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateProblem_withNotFound_shouldReturn404() throws Exception {
        when(problemService.updateProblem(eq(999), any(ProblemDTO.class), isNull())).thenReturn(Result.error(404, "问题不存在"));

        mockMvc.perform(put("/api/problems/999")
                        .contentType("application/json")
                        .content("{\"title\":\"更新后的问题\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void updateStatus_shouldReturnSuccess() throws Exception {
        when(problemService.updateStatus(eq(1), eq("RESOLVED"), isNull(), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/problems/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateStatus_withComment_shouldPassComment() throws Exception {
        when(problemService.updateStatus(eq(1), eq("RESOLVED"), eq("已修复"), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/problems/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"RESOLVED\",\"comment\":\"已修复\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateCategory_shouldReturnSuccess() throws Exception {
        when(problemService.updateCategory(eq(1), eq("功能建议"), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/problems/1/category")
                        .contentType("application/json")
                        .content("{\"category\":\"功能建议\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addComment_shouldReturnSuccess() throws Exception {
        when(problemService.addComment(eq(1), eq("这是评论"), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/problems/1/comment")
                        .contentType("application/json")
                        .content("{\"comment\":\"这是评论\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteProblem_shouldReturnSuccess() throws Exception {
        when(problemService.deleteProblem(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/problems/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteProblem_withNotFound_shouldReturn404() throws Exception {
        when(problemService.deleteProblem(eq(999), isNull())).thenReturn(Result.error(404, "问题不存在"));

        mockMvc.perform(delete("/api/problems/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getStatistics_shouldReturnStats() throws Exception {
        when(problemService.getStatistics()).thenReturn(Result.ok(Map.of("total", 10, "pending", 5)));

        mockMvc.perform(get("/api/problems/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.pending").value(5));
    }

    @Test
    void health_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/problems/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
