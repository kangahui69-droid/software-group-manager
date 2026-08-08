package com.softwaregroup.hr.controller;

import com.softwaregroup.hr.model.dto.*;
import com.softwaregroup.hr.service.ResumeService;
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
 * ResumeController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ResumeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ResumeService resumeService;

    @InjectMocks
    private ResumeController resumeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(resumeController).build();
    }

    @Test
    void createResume_withValidData_shouldReturnSuccess() throws Exception {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("我的简历");

        when(resumeService.createResume(any(ResumeDTO.class), isNull())).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/resumes")
                        .contentType("application/json")
                        .content("{\"resumeName\":\"我的简历\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createResume_withUserId_shouldReturnSuccess() throws Exception {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("我的简历");

        when(resumeService.createResume(any(ResumeDTO.class), eq(1))).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/resumes")
                        .header("X-User-Id", "1")
                        .contentType("application/json")
                        .content("{\"resumeName\":\"我的简历\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateResume_withValidData_shouldReturnSuccess() throws Exception {
        ResumeDTO dto = new ResumeDTO();
        dto.setResumeName("更新后的简历");

        when(resumeService.updateResume(eq(1), any(ResumeDTO.class), isNull())).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/resumes/1")
                        .contentType("application/json")
                        .content("{\"resumeName\":\"更新后的简历\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getResumeDetail_withValidId_shouldReturnDetail() throws Exception {
        when(resumeService.getResumeDetail(eq(1), isNull())).thenReturn(Result.ok(Map.of("id", 1, "resumeName", "简历")));

        mockMvc.perform(get("/api/resumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.resumeName").value("简历"));
    }

    @Test
    void getResumeDetail_withNotFound_shouldReturn404() throws Exception {
        when(resumeService.getResumeDetail(eq(999), isNull())).thenReturn(Result.error(404, "简历不存在"));

        mockMvc.perform(get("/api/resumes/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteResume_shouldReturnSuccess() throws Exception {
        when(resumeService.deleteResume(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/resumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void setDefaultResume_shouldReturnSuccess() throws Exception {
        when(resumeService.setDefaultResume(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/resumes/1/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listResumes_shouldReturnList() throws Exception {
        when(resumeService.listResumes(eq(1), eq(1))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/resumes/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getRecycleBin_shouldReturnList() throws Exception {
        when(resumeService.getRecycleBin(isNull())).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/resumes/recycle-bin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void restoreResume_shouldReturnSuccess() throws Exception {
        when(resumeService.restoreResume(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/resumes/1/restore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void permanentDelete_shouldReturnSuccess() throws Exception {
        when(resumeService.permanentDelete(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/resumes/1/permanent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addEducation_shouldReturnSuccess() throws Exception {
        ResumeEducationDTO dto = new ResumeEducationDTO();
        dto.setSchoolName("测试大学");
        dto.setMajor("软件工程");

        when(resumeService.addEducation(eq(1), any(ResumeEducationDTO.class), isNull())).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/resumes/1/education")
                        .contentType("application/json")
                        .content("{\"school\":\"测试大学\",\"major\":\"软件工程\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateEducation_shouldReturnSuccess() throws Exception {
        ResumeEducationDTO dto = new ResumeEducationDTO();
        dto.setSchoolName("新大学");

        when(resumeService.updateEducation(eq(1), any(ResumeEducationDTO.class), isNull())).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/resumes/education/1")
                        .contentType("application/json")
                        .content("{\"school\":\"新大学\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteEducation_shouldReturnSuccess() throws Exception {
        when(resumeService.deleteEducation(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/resumes/education/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addSkill_shouldReturnSuccess() throws Exception {
        ResumeSkillDTO dto = new ResumeSkillDTO();
        dto.setSkillName("Java");
        dto.setProficiency("熟练");

        when(resumeService.addSkill(eq(1), any(ResumeSkillDTO.class), isNull())).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/resumes/1/skill")
                        .contentType("application/json")
                        .content("{\"skillName\":\"Java\",\"proficiency\":\"熟练\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateSkill_shouldReturnSuccess() throws Exception {
        ResumeSkillDTO dto = new ResumeSkillDTO();
        dto.setProficiency("精通");

        when(resumeService.updateSkill(eq(1), any(ResumeSkillDTO.class), isNull())).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/resumes/skill/1")
                        .contentType("application/json")
                        .content("{\"proficiency\":\"精通\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteSkill_shouldReturnSuccess() throws Exception {
        when(resumeService.deleteSkill(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/resumes/skill/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addProject_shouldReturnSuccess() throws Exception {
        ResumeProjectDTO dto = new ResumeProjectDTO();
        dto.setProjectName("测试项目");

        when(resumeService.addProject(eq(1), any(ResumeProjectDTO.class), isNull())).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/resumes/1/project")
                        .contentType("application/json")
                        .content("{\"projectName\":\"测试项目\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateProject_shouldReturnSuccess() throws Exception {
        ResumeProjectDTO dto = new ResumeProjectDTO();
        dto.setProjectName("更新项目");

        when(resumeService.updateProject(eq(1), any(ResumeProjectDTO.class), isNull())).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/resumes/project/1")
                        .contentType("application/json")
                        .content("{\"projectName\":\"更新项目\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteProject_shouldReturnSuccess() throws Exception {
        when(resumeService.deleteProject(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/resumes/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addAward_shouldReturnSuccess() throws Exception {
        ResumeAwardDTO dto = new ResumeAwardDTO();
        dto.setAwardName("一等奖");

        when(resumeService.addAward(eq(1), any(ResumeAwardDTO.class), isNull())).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/resumes/1/award")
                        .contentType("application/json")
                        .content("{\"awardName\":\"一等奖\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateAward_shouldReturnSuccess() throws Exception {
        ResumeAwardDTO dto = new ResumeAwardDTO();
        dto.setAwardName("特等奖");

        when(resumeService.updateAward(eq(1), any(ResumeAwardDTO.class), isNull())).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/resumes/award/1")
                        .contentType("application/json")
                        .content("{\"awardName\":\"特等奖\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteAward_shouldReturnSuccess() throws Exception {
        when(resumeService.deleteAward(eq(1), isNull())).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/resumes/award/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void health_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/resumes/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void listResumesRoot_withUserId_shouldReturnList() throws Exception {
        when(resumeService.listResumes(eq(1), eq(1))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/resumes")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listResumesRoot_withoutUserId_shouldReturnList() throws Exception {
        when(resumeService.listResumes(eq(0), eq(1))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/resumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
