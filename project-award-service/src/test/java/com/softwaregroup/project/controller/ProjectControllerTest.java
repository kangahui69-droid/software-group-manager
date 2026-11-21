package com.softwaregroup.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaregroup.project.model.dto.ProjectDTO;
import com.softwaregroup.project.model.dto.ProjectFilterDTO;
import com.softwaregroup.project.service.ProjectService;
import com.softwaregroup.common.util.Result;
import com.softwaregroup.common.util.JwtService;
import com.softwaregroup.common.security.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private MockMvc mockMvc;
    private final JwtService testJwt = new JwtService("controller-test-key-32-bytes-or-longer", "v1", "software-group", "software-group-api");
    private final String testToken = testJwt.generateToken(1, "member", "MEMBER");

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(testJwt))
                .defaultRequest(get("/").header("Authorization", "Bearer " + testToken)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listProjects_shouldReturnProjectList() throws Exception {
        when(projectService.listProjects(any(ProjectFilterDTO.class), anyInt(), anyInt(), isNull()))
            .thenReturn(Result.ok(Map.of("list", List.of(
                    Map.of("id", 1, "name", "测试项目", "status", "approved")),
                    "total", 1, "page", 1, "pageSize", 20, "totalPages", 1)));

        mockMvc.perform(get("/api/projects")
                .param("page", "1")
                .param("pageSize", "20")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.list[0].name").value("测试项目"))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void getProjectDetail_shouldReturnProject() throws Exception {
        when(projectService.getProjectDetail(eq(1), isNull(), eq(false)))
            .thenReturn(Result.ok(Map.of("id", 1, "name", "测试项目", "description", "详情")));

        mockMvc.perform(get("/api/projects/1")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("测试项目"));
    }

    @Test
    void getMyProjects_shouldReturnProjects() throws Exception {
        when(projectService.getMyProjects(any(), anyInt(), anyInt()))
            .thenReturn(Result.ok(Map.of("list", List.of(Map.of("id", 1, "name", "我的项目")),
                    "total", 1, "page", 1, "pageSize", 20, "totalPages", 1)));

        mockMvc.perform(get("/api/projects/my")
                .param("userId", "1")
                .param("page", "1")
                .param("pageSize", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.list[0].name").value("我的项目"))
            .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void createdByMe_shouldUseOwnerOnlyServiceAndAuthenticatedUser() throws Exception {
        when(projectService.getCreatedByMeProjects(eq(1), eq(2), eq(3)))
            .thenReturn(Result.ok(Map.of("list", List.of(Map.of("id", 8)),
                    "total", 4, "page", 2, "pageSize", 3, "totalPages", 2)));

        mockMvc.perform(get("/api/projects/created-by-me")
                .param("userId", "999")
                .param("page", "2")
                .param("pageSize", "3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.list[0].id").value(8))
            .andExpect(jsonPath("$.data.total").value(4));

        org.mockito.Mockito.verify(projectService).getCreatedByMeProjects(1, 2, 3);
        org.mockito.Mockito.verify(projectService, org.mockito.Mockito.never())
                .getMyProjects(any(), anyInt(), anyInt());
    }

    @Test
    void createProject_shouldReturnCreatedProject() throws Exception {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("新项目");
        dto.setDescription("项目描述");

        when(projectService.createProject(any(ProjectDTO.class), any()))
            .thenReturn(Result.ok(Map.of("id", 10, "name", "新项目")));

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("新项目"));
    }

    @Test
    void updateProject_shouldReturnUpdatedProject() throws Exception {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("更新后的项目");

        when(projectService.updateProject(eq(1), any(ProjectDTO.class), any()))
            .thenReturn(Result.ok(Map.of("id", 1, "name", "更新后的项目")));

        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.name").value("更新后的项目"));
    }

    @Test
    void deleteProject_shouldReturnSuccess() throws Exception {
        when(projectService.deleteProject(eq(1), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/projects/1")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void approveProject_shouldReturnSuccess() throws Exception {
        Map<String, Integer> body = new HashMap<>();
        body.put("operatorId", 1);
        when(projectService.approveProject(eq(1), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/projects/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void rejectProject_shouldReturnSuccess() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("reason", "不符合要求");
        body.put("operatorId", 1);
        when(projectService.rejectProject(eq(1), eq("不符合要求"), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/projects/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void applyMember_shouldReturnSuccess() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", 2);
        body.put("reason", "想加入项目");
        when(projectService.applyMember(eq(1), eq(1), eq("想加入项目")))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/projects/1/apply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addPlan_shouldReturnSuccess() throws Exception {
        when(projectService.addPlan(eq(1), any(), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/projects/1/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addProgress_shouldReturnSuccess() throws Exception {
        when(projectService.addProgress(eq(1), any(), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/projects/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
