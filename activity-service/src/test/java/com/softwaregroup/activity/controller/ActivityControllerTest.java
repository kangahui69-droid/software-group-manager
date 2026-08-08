package com.softwaregroup.activity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaregroup.activity.model.dto.ActivityDTO;
import com.softwaregroup.activity.model.dto.ActivityFilterDTO;
import com.softwaregroup.activity.service.ActivityService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ActivityController 单元测试
 * 使用 MockMvc 独立设置，不加载 Spring 上下文
 */
@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ActivityController activityController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(activityController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listActivities_shouldReturnActivityList() throws Exception {
        Map<String, Object> mockData = Map.of("id", 1, "title", "测试活动", "status", "upcoming");
        when(activityService.listActivities(any(ActivityFilterDTO.class)))
            .thenReturn(Result.ok(mockData));

        mockMvc.perform(get("/api/activities")
                .param("keyword", "测试"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.title").value("测试活动"));
    }

    @Test
    void getActivityDetail_shouldReturnActivity() throws Exception {
        Map<String, Object> mockData = Map.of("id", 1, "title", "测试活动", "description", "详情");
        when(activityService.getActivityDetail(eq(1), any()))
            .thenReturn(Result.ok(mockData));

        mockMvc.perform(get("/api/activities/1")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.title").value("测试活动"));
    }

    @Test
    void createActivity_shouldReturnCreatedActivity() throws Exception {
        ActivityDTO dto = new ActivityDTO();
        dto.setTitle("新活动");
        dto.setDescription("活动描述");

        Map<String, Object> mockData = Map.of("id", 10, "title", "新活动");
        when(activityService.createActivity(any(ActivityDTO.class), any()))
            .thenReturn(Result.ok(mockData));

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.title").value("新活动"));
    }

    @Test
    void updateActivity_shouldReturnUpdatedActivity() throws Exception {
        ActivityDTO dto = new ActivityDTO();
        dto.setTitle("更新后的活动");

        Map<String, Object> mockData = Map.of("id", 1, "title", "更新后的活动");
        when(activityService.updateActivity(eq(1), any(ActivityDTO.class), any()))
            .thenReturn(Result.ok(mockData));

        mockMvc.perform(put("/api/activities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.title").value("更新后的活动"));
    }

    @Test
    void deleteActivity_shouldReturnSuccess() throws Exception {
        when(activityService.deleteActivity(eq(1), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/activities/1")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void approveActivity_shouldReturnSuccess() throws Exception {
        when(activityService.approveActivity(eq(1), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/approve")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void rejectActivity_shouldReturnSuccess() throws Exception {
        Map<String, String> body = Map.of("reason", "不符合要求");
        when(activityService.rejectActivity(eq(1), eq("不符合要求"), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void register_shouldReturnSuccess() throws Exception {
        when(activityService.register(eq(1), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/register")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void health_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/activities/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void getMyActivities_shouldReturnActivities() throws Exception {
        when(activityService.getMyActivities(any()))
            .thenReturn(Result.ok(Arrays.asList(
                Map.of("id", 1, "title", "我的活动")
            )));

        mockMvc.perform(get("/api/activities/my")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].title").value("我的活动"));
    }

    @Test
    void getMyCreatedActivities_shouldReturnActivities() throws Exception {
        when(activityService.getMyCreatedActivities(any()))
            .thenReturn(Result.ok(Arrays.asList(
                Map.of("id", 2, "title", "我创建的活动")
            )));

        mockMvc.perform(get("/api/activities/created")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].title").value("我创建的活动"));
    }

    @Test
    void getActivityParticipants_shouldReturnList() throws Exception {
        when(activityService.getActivityParticipants(eq(1)))
            .thenReturn(Result.ok(Arrays.asList(
                Map.of("userId", 1, "name", "张三"),
                Map.of("userId", 2, "name", "李四")
            )));

        mockMvc.perform(get("/api/activities/1/participants"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void approveParticipant_shouldReturnSuccess() throws Exception {
        when(activityService.approveParticipant(eq(1), eq(2), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/participants/2/approve")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void rejectParticipant_shouldReturnSuccess() throws Exception {
        when(activityService.rejectParticipant(eq(1), eq(2), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/participants/2/reject")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void batchApprove_shouldReturnSuccess() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("userIds", Arrays.asList(1, 2, 3));
        when(activityService.batchApprove(eq(1), any(), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/participants/batch-approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void batchReject_shouldReturnSuccess() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("userIds", Arrays.asList(1, 2, 3));
        when(activityService.batchReject(eq(1), any(), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/participants/batch-reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void cancelActivity_shouldReturnSuccess() throws Exception {
        when(activityService.cancelActivity(eq(1), any()))
            .thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/activities/1/cancel")
                .header("X-User-Id", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}