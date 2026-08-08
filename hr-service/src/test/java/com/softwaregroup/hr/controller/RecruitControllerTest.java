package com.softwaregroup.hr.controller;

import com.softwaregroup.hr.model.dto.RecruitApplicationDTO;
import com.softwaregroup.hr.service.RecruitService;
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
 * RecruitController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class RecruitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecruitService recruitService;

    @InjectMocks
    private RecruitController recruitController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recruitController).build();
    }

    @Test
    void submitApplication_withValidData_shouldReturnSuccess() throws Exception {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("张三");
        dto.setStudentId("2026001");
        dto.setMajor("软件工程");
        dto.setEmail("zhangsan@example.com");

        when(recruitService.submitApplication(any(RecruitApplicationDTO.class))).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/recruit/apply")
                        .contentType("application/json")
                        .content("{\"name\":\"张三\",\"studentId\":\"2026001\",\"major\":\"软件工程\",\"email\":\"zhangsan@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void submitApplication_withInvalidData_shouldReturn400() throws Exception {
        when(recruitService.submitApplication(any(RecruitApplicationDTO.class))).thenReturn(Result.error(400, "姓名不能为空"));

        mockMvc.perform(post("/api/recruit/apply")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"studentId\":\"2026001\",\"major\":\"软件工程\",\"email\":\"zhangsan@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void approveApplication_shouldReturnSuccess() throws Exception {
        when(recruitService.approveApplication(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/recruit/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void approveApplication_withOperatorId_shouldPassOperatorId() throws Exception {
        when(recruitService.approveApplication(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/recruit/1/approve")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void approveApplication_withNotFound_shouldReturn404() throws Exception {
        when(recruitService.approveApplication(eq(999), isNull())).thenReturn(Result.error(404, "申请不存在"));

        mockMvc.perform(post("/api/recruit/999/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void rejectApplication_shouldReturnSuccess() throws Exception {
        when(recruitService.rejectApplication(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/recruit/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void rejectApplication_withNotFound_shouldReturn404() throws Exception {
        when(recruitService.rejectApplication(eq(999), isNull())).thenReturn(Result.error(404, "申请不存在"));

        mockMvc.perform(post("/api/recruit/999/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listApplications_shouldReturnList() throws Exception {
        when(recruitService.listApplications(isNull(), isNull(), isNull(), isNull())).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/recruit/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listApplications_withFilters_shouldPassFilters() throws Exception {
        when(recruitService.listApplications(eq(2026), eq("PENDING"), isNull(), isNull())).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/recruit/list")
                        .param("year", "2026")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listApplicationsRoot_shouldReturnList() throws Exception {
        when(recruitService.listApplications(isNull(), isNull(), isNull(), isNull())).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/recruit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getApplicationDetail_withValidId_shouldReturnDetail() throws Exception {
        when(recruitService.getApplicationDetail(eq(1))).thenReturn(Result.ok(Map.of("id", 1, "name", "张三")));

        mockMvc.perform(get("/api/recruit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    void getApplicationDetail_withInvalidId_shouldReturn404() throws Exception {
        when(recruitService.getApplicationDetail(eq(999))).thenReturn(Result.error(404, "申请不存在"));

        mockMvc.perform(get("/api/recruit/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteApplication_shouldReturnSuccess() throws Exception {
        when(recruitService.deleteApplication(eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/recruit/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteApplication_withNotFound_shouldReturn404() throws Exception {
        when(recruitService.deleteApplication(eq(999))).thenReturn(Result.error(404, "申请不存在"));

        mockMvc.perform(delete("/api/recruit/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void countPending_shouldReturnCount() throws Exception {
        when(recruitService.countPending()).thenReturn(Result.ok(Map.of("count", 5)));

        mockMvc.perform(get("/api/recruit/pending/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.count").value(5));
    }

    @Test
    void findAllYears_shouldReturnYears() throws Exception {
        when(recruitService.findAllYears()).thenReturn(Result.ok(List.of(2025, 2026)));

        mockMvc.perform(get("/api/recruit/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0]").value(2025))
                .andExpect(jsonPath("$.data[1]").value(2026));
    }

    @Test
    void health_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/recruit/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
