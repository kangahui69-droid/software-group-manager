package com.softwaregroup.content.controller;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.content.model.dto.GroupDTO;
import com.softwaregroup.content.service.GroupService;
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
 * GroupController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @Test
    void listGroups_shouldReturnGroupList() throws Exception {
        GroupDTO group = new GroupDTO();
        group.setId(1);
        group.setGroupName("测试群组");

        when(groupService.listGroups(eq(1), eq(20))).thenReturn(Result.ok(List.of(group)));

        mockMvc.perform(get("/api/groups")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].groupName").value("测试群组"));
    }

    @Test
    void listGroups_withDefaultPagination_shouldUseDefaults() throws Exception {
        when(groupService.listGroups(eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getGroupDetail_withValidParams_shouldReturnGroup() throws Exception {
        GroupDTO group = new GroupDTO();
        group.setId(1);
        group.setGroupName("测试群组");

        when(groupService.getGroupDetail(eq(1), eq(1))).thenReturn(Result.ok(group));

        mockMvc.perform(get("/api/groups/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.groupName").value("测试群组"));
    }

    @Test
    void getGroupDetail_withoutPermission_shouldReturn403() throws Exception {
        when(groupService.getGroupDetail(eq(1), eq(2))).thenReturn(Result.error(403, "无权限查看群组详情"));

        mockMvc.perform(get("/api/groups/1")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void getGroupDetail_groupNotFound_shouldReturn404() throws Exception {
        when(groupService.getGroupDetail(eq(999), eq(1))).thenReturn(Result.error(404, "群组不存在"));

        mockMvc.perform(get("/api/groups/999")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getCreatedGroups_shouldReturnGroups() throws Exception {
        when(groupService.getCreatedGroups(eq(1), eq(1))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/groups/user/1/created")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getMyGroups_shouldReturnGroups() throws Exception {
        when(groupService.getMyGroups(eq(1), eq(1))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/groups/user/1/my-groups")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createGroup_withValidData_shouldReturnSuccess() throws Exception {
        GroupDTO inputDto = new GroupDTO();
        inputDto.setGroupName("新群组");

        when(groupService.createGroup(any(GroupDTO.class), eq(1))).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/groups")
                        .param("userId", "1")
                        .contentType("application/json")
                        .content("{\"groupName\":\"新群组\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createGroup_withInvalidData_shouldReturn400() throws Exception {
        when(groupService.createGroup(any(GroupDTO.class), eq(1))).thenReturn(Result.error(400, "群组名称不能为空"));

        mockMvc.perform(post("/api/groups")
                        .param("userId", "1")
                        .contentType("application/json")
                        .content("{\"groupName\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updateGroup_withValidData_shouldReturnSuccess() throws Exception {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName("更新后的群组");

        when(groupService.updateGroup(eq(1), any(GroupDTO.class), eq(1))).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/groups/1")
                        .param("userId", "1")
                        .contentType("application/json")
                        .content("{\"groupName\":\"更新后的群组\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteGroup_shouldReturnSuccess() throws Exception {
        when(groupService.deleteGroup(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/groups/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void addMember_shouldReturnSuccess() throws Exception {
        when(groupService.addMember(eq(1), eq(2), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/groups/1/members")
                        .param("operatorId", "1")
                        .contentType("application/json")
                        .content("{\"userId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void removeMember_shouldReturnSuccess() throws Exception {
        when(groupService.removeMember(eq(1), eq(2), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/groups/1/members/2")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getMessages_shouldReturnMessages() throws Exception {
        when(groupService.getMessages(eq(1), eq(1))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/groups/1/messages")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void sendMessage_shouldReturnSuccess() throws Exception {
        when(groupService.sendMessage(eq(1), eq(1), eq("测试消息"))).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/groups/1/messages")
                        .contentType("application/json")
                        .content("{\"userId\":1,\"content\":\"测试消息\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteMessage_shouldReturnSuccess() throws Exception {
        when(groupService.deleteMessage(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/groups/messages/1")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void muteMember_shouldReturnSuccess() throws Exception {
        when(groupService.muteMember(eq(1), eq(2), isNull(), eq("违反规定"))).thenReturn(Result.ok(null));

        mockMvc.perform(post("/api/groups/1/mute")
                        .contentType("application/json")
                        .content("{\"targetUserId\":2,\"reason\":\"违反规定\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void unmuteMember_shouldReturnSuccess() throws Exception {
        when(groupService.unmuteMember(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/groups/1/mute")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
