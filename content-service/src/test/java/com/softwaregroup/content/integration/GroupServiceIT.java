package com.softwaregroup.content.integration;

import com.softwaregroup.content.dao.ActivityGroupDAO;
import com.softwaregroup.content.dao.GroupMemberDAO;
import com.softwaregroup.content.dao.GroupMessageDAO;
import com.softwaregroup.content.dao.UserGroupDAO;
import com.softwaregroup.content.dao.FileStorageDAO;
import com.softwaregroup.content.dao.UserDAO;
import com.softwaregroup.content.dao.MemberProfileDAO;
import com.softwaregroup.content.model.ActivityGroup;
import com.softwaregroup.content.model.GroupMessage;
import com.softwaregroup.content.model.User;
import com.softwaregroup.content.model.dto.GroupDTO;
import com.softwaregroup.content.service.GroupService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GroupService 集成测试
 *
 * 测试群组服务的核心功能：群组CRUD、成员管理、消息发送
 */
@ExtendWith(MockitoExtension.class)
class GroupServiceIT {

    @Mock
    private ActivityGroupDAO activityGroupDAO;

    @Mock
    private GroupMemberDAO groupMemberDAO;

    @Mock
    private GroupMessageDAO groupMessageDAO;

    @Mock
    private UserGroupDAO userGroupDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(
                activityGroupDAO, groupMemberDAO, groupMessageDAO,
                userGroupDAO, fileStorageDAO, userDAO, memberProfileDAO);
    }

    @Test
    void listGroups_withValidParams_shouldReturnGroupList() {
        ActivityGroup group = new ActivityGroup();
        group.setId(1);
        group.setGroupName("测试群组");
        when(activityGroupDAO.findAll()).thenReturn(Arrays.asList(group));

        Result result = groupService.listGroups(1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(activityGroupDAO).findAll();
    }

    @Test
    void listGroups_withInvalidPage_shouldReturnError() {
        Result result = groupService.listGroups(0, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listGroups_withInvalidPageSize_shouldReturnError() {
        Result result = groupService.listGroups(1, 200);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createGroup_withValidData_shouldReturnSuccess() {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName("新群组");

        User user = new User();
        user.setId(1);
        user.setUsername("admin");

        when(userDAO.findById(1)).thenReturn(user);
        when(activityGroupDAO.insert(any(ActivityGroup.class))).thenReturn(1);
        when(groupMemberDAO.insertOwner(eq(1), eq(1))).thenReturn(true);
        when(userGroupDAO.insertUserToGroup(eq(1), eq(1))).thenReturn(true);

        Result result = groupService.createGroup(dto, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(activityGroupDAO).insert(any(ActivityGroup.class));
    }

    @Test
    void createGroup_withEmptyName_shouldReturnError() {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName("");

        Result result = groupService.createGroup(dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createGroup_withTooLongName_shouldReturnError() {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName("a".repeat(300));

        Result result = groupService.createGroup(dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createGroup_withNullUser_shouldReturnError() {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName("测试群组");

        Result result = groupService.createGroup(dto, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void addMember_withValidParams_shouldReturnSuccess() {
        ActivityGroup group = new ActivityGroup();
        group.setId(1);
        group.setGroupName("测试群组");

        User user = new User();
        user.setId(2);

        when(activityGroupDAO.findById(1)).thenReturn(group);
        when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
        when(userDAO.findById(2)).thenReturn(user);
        when(groupMemberDAO.isMember(1, 2)).thenReturn(false);
        when(groupMemberDAO.insertMember(1, 2)).thenReturn(true);
        when(userGroupDAO.insertUserToGroup(2, 1)).thenReturn(true);

        Result result = groupService.addMember(1, 2, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void addMember_withNonOwner_shouldReturnError() {
        ActivityGroup group = new ActivityGroup();
        group.setId(1);

        when(activityGroupDAO.findById(1)).thenReturn(group);
        when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

        Result result = groupService.addMember(1, 3, 2);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void sendMessage_withValidParams_shouldReturnSuccess() {
        when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
        when(activityGroupDAO.findById(1)).thenReturn(null);
        when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(1);

        GroupMessage message = new GroupMessage();
        message.setId(1);
        message.setGroupId(1);
        message.setSenderId(1);
        message.setContent("测试消息");

        Result result = groupService.sendMessage(1, 1, "测试消息");

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void sendMessage_withEmptyContent_shouldReturnError() {
        Result result = groupService.sendMessage(1, 1, "");

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void sendMessage_withTooLongContent_shouldReturnError() {
        String longContent = "a".repeat(6000);
        Result result = groupService.sendMessage(1, 1, longContent);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getMyGroups_withValidUser_shouldReturnGroupList() {
        when(userGroupDAO.findByUserId(1)).thenReturn(Arrays.asList());

        Result result = groupService.getMyGroups(1, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(userGroupDAO).findByUserId(1);
    }

    @Test
    void getMyGroups_withNullUser_shouldReturnError() {
        Result result = groupService.getMyGroups(null, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }
}
