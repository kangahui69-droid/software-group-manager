package com.softwaregroup.content.service;

import com.softwaregroup.content.dao.ActivityGroupDAO;
import com.softwaregroup.content.dao.GroupMemberDAO;
import com.softwaregroup.content.dao.GroupMessageDAO;
import com.softwaregroup.content.dao.UserGroupDAO;
import com.softwaregroup.content.model.ActivityGroup;
import com.softwaregroup.content.model.GroupMember;
import com.softwaregroup.content.model.GroupMessage;
import com.softwaregroup.content.model.UserGroup;
import com.softwaregroup.content.model.dto.GroupDTO;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GroupService 单元测试
 * 覆盖所有公开业务方法的正常路径、边界情况和异常场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("群聊服务测试")
class GroupServiceTest {

    @Mock
    private ActivityGroupDAO activityGroupDAO;

    @Mock
    private GroupMemberDAO groupMemberDAO;

    @Mock
    private GroupMessageDAO groupMessageDAO;

    @Mock
    private UserGroupDAO userGroupDAO;

    @Mock
    private com.softwaregroup.content.dao.FileStorageDAO fileStorageDAO;

    @Mock
    private com.softwaregroup.content.dao.UserDAO userDAO;

    @Mock
    private com.softwaregroup.content.dao.MemberProfileDAO memberProfileDAO;

    @InjectMocks
    private GroupService groupService;

    private ActivityGroup testGroup;
    private GroupDTO testGroupDTO;
    private GroupMessage testMessage;

    @BeforeEach
    void setUp() {
        testGroup = new ActivityGroup();
        testGroup.setId(1);
        testGroup.setGroupName("测试群组");
        testGroup.setGroupOwnerId(1);
        testGroup.setActivityId(null);
        testGroup.setCreatedAt(new Date());
        testGroup.setMuted(false);

        testGroupDTO = new GroupDTO();
        testGroupDTO.setGroupName("新群组");

        testMessage = new GroupMessage();
        testMessage.setId(1);
        testMessage.setGroupId(1);
        testMessage.setSenderId(1);
        testMessage.setContent("测试消息");
        testMessage.setMessageType(GroupMessage.MESSAGE_TYPE_TEXT);
        testMessage.setSentAt(new Date());
    }

    @Nested
    @DisplayName("listGroups - 群聊列表")
    class ListGroupsTests {

        @Test
        @DisplayName("正常路径：返回群组列表")
        void should_return_groups_when_valid_params() {
            when(activityGroupDAO.findAll()).thenReturn(Arrays.asList(testGroup));

            Result result = groupService.listGroups(1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
            verify(activityGroupDAO, times(1)).findAll();
        }

        @Test
        @DisplayName("边界情况：页码为0应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = groupService.listGroups(0, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("页码必须大于0");
        }

        @Test
        @DisplayName("边界情况：页码为负数应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = groupService.listGroups(-1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("边界情况：每页数量为0应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = groupService.listGroups(1, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每页数量必须在1-100之间");
        }

        @Test
        @DisplayName("边界情况：每页数量超过100应返回错误")
        void should_return_error_when_page_size_exceeds_max() {
            Result result = groupService.listGroups(1, 101);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每页数量必须在1-100之间");
        }

        @Test
        @DisplayName("正常路径：每页数量为100应正常返回")
        void should_return_success_when_page_size_is_100() {
            when(activityGroupDAO.findAll()).thenReturn(Arrays.asList(testGroup));

            Result result = groupService.listGroups(1, 100);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("getGroupDetail - 群聊详情")
    class GetGroupDetailTests {

        @Test
        @DisplayName("正常路径：成员查看自己的群组详情")
        void should_return_detail_when_member_access_own_group() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(false);

            Result result = groupService.getGroupDetail(1, 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(ActivityGroup.class);
        }

        @Test
        @DisplayName("正常路径：群主查看群组详情")
        void should_return_detail_when_owner_access_group() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isMember(1, 1)).thenReturn(false);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);

            Result result = groupService.getGroupDetail(1, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.getGroupDetail(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组ID不能为空");
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.getGroupDetail(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }

        @Test
        @DisplayName("异常场景：群组不存在应返回404")
        void should_return_404_when_group_not_found() {
            when(activityGroupDAO.findById(999)).thenReturn(null);

            Result result = groupService.getGroupDetail(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("群组不存在");
        }

        @Test
        @DisplayName("异常场景：非成员且非群主查看应返回403")
        void should_return_403_when_not_member_or_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isMember(1, 2)).thenReturn(false);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

            Result result = groupService.getGroupDetail(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("无权限");
        }
    }

    @Nested
    @DisplayName("createGroup - 创建群聊")
    class CreateGroupTests {

        @Test
        @DisplayName("正常路径：成功创建群组")
        void should_create_group_successfully() {
            when(userDAO.findById(1)).thenReturn(new com.softwaregroup.content.model.User());
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenAnswer(invocation -> {
                ActivityGroup group = invocation.getArgument(0);
                group.setId(1);
                return 1;
            });
            when(groupMemberDAO.insertOwner(anyInt(), anyInt())).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(anyInt(), anyInt())).thenReturn(true);

            Result result = groupService.createGroup(testGroupDTO, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO, times(1)).insert(any(ActivityGroup.class));
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = groupService.createGroup(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组信息不能为空");
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.createGroup(testGroupDTO, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }

        @Test
        @DisplayName("异常场景：群组名称为空应返回错误")
        void should_return_error_when_group_name_is_empty() {
            testGroupDTO.setGroupName("");

            Result result = groupService.createGroup(testGroupDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组名称不能为空");
        }

        @Test
        @DisplayName("异常场景：群组名称为null应返回错误")
        void should_return_error_when_group_name_is_null() {
            testGroupDTO.setGroupName(null);

            Result result = groupService.createGroup(testGroupDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组名称不能为空");
        }

        @Test
        @DisplayName("边界情况：群组名称超过255字符应返回错误")
        void should_return_error_when_group_name_too_long() {
            testGroupDTO.setGroupName("a".repeat(256));

            Result result = groupService.createGroup(testGroupDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("255");
        }

        @Test
        @DisplayName("边界情况：群组名称为255字符应正常创建")
        void should_create_group_when_name_is_255_chars() {
            testGroupDTO.setGroupName("a".repeat(255));
            when(userDAO.findById(1)).thenReturn(new com.softwaregroup.content.model.User());
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenReturn(1);
            when(groupMemberDAO.insertOwner(anyInt(), anyInt())).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(anyInt(), anyInt())).thenReturn(true);

            Result result = groupService.createGroup(testGroupDTO, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：用户不存在应返回404")
        void should_return_404_when_user_not_found() {
            when(userDAO.findById(999)).thenReturn(null);

            Result result = groupService.createGroup(testGroupDTO, 999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("用户不存在");
        }

        @Test
        @DisplayName("异常场景：数据库插入失败应返回500")
        void should_return_500_when_insert_fails() {
            when(userDAO.findById(1)).thenReturn(new com.softwaregroup.content.model.User());
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenReturn(0);

            Result result = groupService.createGroup(testGroupDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("创建群组失败");
        }
    }

    @Nested
    @DisplayName("updateGroup - 更新群聊")
    class UpdateGroupTests {

        @Test
        @DisplayName("正常路径：群主成功更新群组")
        void should_update_group_when_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);

            GroupDTO updateDTO = new GroupDTO();
            updateDTO.setGroupName("更新后的群组");

            Result result = groupService.updateGroup(1, updateDTO, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.updateGroup(null, testGroupDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组ID不能为空");
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = groupService.updateGroup(1, null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组信息不能为空");
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.updateGroup(1, testGroupDTO, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }

        @Test
        @DisplayName("异常场景：群组不存在应返回404")
        void should_return_404_when_group_not_found() {
            when(activityGroupDAO.findById(999)).thenReturn(null);

            Result result = groupService.updateGroup(999, testGroupDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("群组不存在");
        }

        @Test
        @DisplayName("异常场景：非群主更新应返回403")
        void should_return_403_when_not_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

            Result result = groupService.updateGroup(1, testGroupDTO, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("只有群主才能更新群组");
        }

        @Test
        @DisplayName("异常场景：新群组名称为空应返回错误")
        void should_return_error_when_new_name_is_empty() {
            GroupDTO updateDTO = new GroupDTO();
            updateDTO.setGroupName("");

            Result result = groupService.updateGroup(1, updateDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组名称不能为空");
        }
    }

    @Nested
    @DisplayName("deleteGroup - 删除群聊")
    class DeleteGroupTests {

        @Test
        @DisplayName("正常路径：群主成功删除群组")
        void should_delete_group_when_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            doNothing().when(groupMessageDAO).deleteByGroupId(1);
            doNothing().when(groupMemberDAO).deleteByGroupId(1);
            doNothing().when(userGroupDAO).deleteByGroupId(1);
            when(activityGroupDAO.delete(1)).thenReturn(true);

            Result result = groupService.deleteGroup(1, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO, times(1)).delete(1);
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.deleteGroup(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.deleteGroup(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：群组不存在应返回404")
        void should_return_404_when_group_not_found() {
            when(activityGroupDAO.findById(999)).thenReturn(null);

            Result result = groupService.deleteGroup(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：非群主删除应返回403")
        void should_return_403_when_not_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

            Result result = groupService.deleteGroup(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("只有群主才能删除群组");
        }
    }

    @Nested
    @DisplayName("addMember - 添加成员")
    class AddMemberTests {

        @Test
        @DisplayName("正常路径：群主成功添加成员")
        void should_add_member_when_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(2)).thenReturn(new com.softwaregroup.content.model.User());
            when(groupMemberDAO.isMember(1, 2)).thenReturn(false);
            when(groupMemberDAO.insertMember(1, 2)).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(2, 1)).thenReturn(true);

            Result result = groupService.addMember(1, 2, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.addMember(null, 2, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.addMember(1, null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = groupService.addMember(1, 2, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：群组不存在应返回404")
        void should_return_404_when_group_not_found() {
            when(activityGroupDAO.findById(999)).thenReturn(null);

            Result result = groupService.addMember(999, 2, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：非群主操作应返回403")
        void should_return_403_when_not_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

            Result result = groupService.addMember(1, 3, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @Test
        @DisplayName("异常场景：目标用户不存在应返回404")
        void should_return_404_when_target_user_not_found() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(999)).thenReturn(null);

            Result result = groupService.addMember(1, 999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("用户不存在");
        }

        @Test
        @DisplayName("异常场景：用户已是成员应返回错误")
        void should_return_error_when_user_already_member() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(2)).thenReturn(new com.softwaregroup.content.model.User());
            when(groupMemberDAO.isMember(1, 2)).thenReturn(true);

            Result result = groupService.addMember(1, 2, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("已是群成员");
        }

        @Test
        @DisplayName("异常场景：添加成员数据库失败应返回500")
        void should_return_500_when_insert_fails() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(2)).thenReturn(new com.softwaregroup.content.model.User());
            when(groupMemberDAO.isMember(1, 2)).thenReturn(false);
            when(groupMemberDAO.insertMember(1, 2)).thenReturn(false);

            Result result = groupService.addMember(1, 2, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("添加成员失败");
        }
    }

    @Nested
    @DisplayName("removeMember - 移除成员")
    class RemoveMemberTests {

        @Test
        @DisplayName("正常路径：群主成功移除成员")
        void should_remove_member_when_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(2)).thenReturn(new com.softwaregroup.content.model.User());
            when(groupMemberDAO.isMember(1, 2)).thenReturn(true);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);
            when(groupMemberDAO.delete(1, 2)).thenReturn(true);
            when(userGroupDAO.delete(2, 1)).thenReturn(true);

            Result result = groupService.removeMember(1, 2, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：不能移除群主")
        void should_return_error_when_removing_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(1)).thenReturn(new com.softwaregroup.content.model.User());
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);

            Result result = groupService.removeMember(1, 1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不能移除群主");
        }

        @Test
        @DisplayName("异常场景：目标用户不是成员应返回403")
        void should_return_403_when_target_not_member() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(userDAO.findById(2)).thenReturn(new com.softwaregroup.content.model.User());
            when(groupMemberDAO.isMember(1, 2)).thenReturn(false);

            Result result = groupService.removeMember(1, 2, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("非成员不能执行此操作");
        }
    }

    @Nested
    @DisplayName("getMessages - 消息历史")
    class GetMessagesTests {

        @Test
        @DisplayName("正常路径：成功获取消息历史")
        void should_return_messages_when_valid() {
            when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
            when(groupMessageDAO.findByGroupId(eq(1), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testMessage));

            Result result = groupService.getMessages(1, 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.getMessages(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("群组ID不能为空");
        }

        @Test
        @DisplayName("异常场景：页码为空应返回错误")
        void should_return_error_when_page_is_null() {
            Result result = groupService.getMessages(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("页码必须大于0");
        }

        @Test
        @DisplayName("异常场景：页码为0应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = groupService.getMessages(1, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：非成员查看消息应返回403")
        void should_return_403_when_not_member() {
            when(groupMemberDAO.isMember(1, 1)).thenReturn(false);

            Result result = groupService.getMessages(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("sendMessage - 发送消息")
    class SendMessageTests {

        @Test
        @DisplayName("正常路径：成员成功发送消息")
        void should_send_message_when_member() {
            when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(1);

            Result result = groupService.sendMessage(1, 1, "测试消息");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(GroupMessage.class);
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.sendMessage(null, 1, "消息内容");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.sendMessage(1, null, "消息内容");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：消息内容为空应返回错误")
        void should_return_error_when_content_is_empty() {
            Result result = groupService.sendMessage(1, 1, "");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("消息内容不能为空");
        }

        @Test
        @DisplayName("异常场景：消息内容为null应返回错误")
        void should_return_error_when_content_is_null() {
            Result result = groupService.sendMessage(1, 1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("边界情况：消息内容超过5000字符应返回错误")
        void should_return_error_when_content_too_long() {
            String longContent = "a".repeat(5001);

            Result result = groupService.sendMessage(1, 1, longContent);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("5000");
        }

        @Test
        @DisplayName("边界情况：消息内容为5000字符应正常发送")
        void should_send_when_content_is_5000_chars() {
            String maxContent = "a".repeat(5000);
            when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(1);

            Result result = groupService.sendMessage(1, 1, maxContent);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：群组已被禁言应返回403")
        void should_return_403_when_group_muted() {
            testGroup.setMuted(true);
            when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);

            Result result = groupService.sendMessage(1, 1, "消息内容");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("禁言");
        }

        @Test
        @DisplayName("异常场景：非成员发送消息应返回403")
        void should_return_403_when_not_member() {
            when(groupMemberDAO.isMember(1, 2)).thenReturn(false);

            Result result = groupService.sendMessage(1, 2, "消息内容");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @Test
        @DisplayName("异常场景：数据库插入失败应返回500")
        void should_return_500_when_insert_fails() {
            when(groupMemberDAO.isMember(1, 1)).thenReturn(true);
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(0);

            Result result = groupService.sendMessage(1, 1, "消息内容");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("发送消息失败");
        }
    }

    @Nested
    @DisplayName("muteMember - 禁言")
    class MuteMemberTests {

        @Test
        @DisplayName("正常路径：群主成功禁言成员")
        void should_mute_member_when_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(activityGroupDAO.muteGroup(eq(1), any(java.sql.Date.class), anyString())).thenReturn(true);

            Date until = new Date(System.currentTimeMillis() + 3600000);
            Result result = groupService.muteMember(1, 2, until, "违规发言");

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.muteMember(null, 2, new Date(), "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：目标用户ID为空应返回错误")
        void should_return_error_when_target_user_id_is_null() {
            Result result = groupService.muteMember(1, null, new Date(), "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：禁言自己应返回错误")
        void should_return_error_when_muting_self() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);

            Result result = groupService.muteMember(1, 1, new Date(), "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不能禁言自己");
        }

        @Test
        @DisplayName("异常场景：非群主禁言应返回403")
        void should_return_403_when_not_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

            Result result = groupService.muteMember(1, 3, new Date(), "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("只有群主才能禁言");
        }

        @Test
        @DisplayName("异常场景：禁言原因超过500字符应返回错误")
        void should_return_error_when_reason_too_long() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);

            String longReason = "a".repeat(501);

            Result result = groupService.muteMember(1, 2, new Date(), longReason);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("500");
        }

        @Test
        @DisplayName("异常场景：群组不存在应返回404")
        void should_return_404_when_group_not_found() {
            when(activityGroupDAO.findById(999)).thenReturn(null);

            Result result = groupService.muteMember(999, 2, new Date(), "原因");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("unmuteMember - 取消禁言")
    class UnmuteMemberTests {

        @Test
        @DisplayName("正常路径：群主成功取消禁言")
        void should_unmute_member_when_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 1)).thenReturn(true);
            when(activityGroupDAO.unmuteGroup(1)).thenReturn(true);

            Result result = groupService.unmuteMember(1, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：群组ID为空应返回错误")
        void should_return_error_when_group_id_is_null() {
            Result result = groupService.unmuteMember(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = groupService.unmuteMember(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：非群主取消禁言应返回403")
        void should_return_403_when_not_owner() {
            when(activityGroupDAO.findById(1)).thenReturn(testGroup);
            when(groupMemberDAO.isOwner(1, 2)).thenReturn(false);

            Result result = groupService.unmuteMember(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("只有群主才能取消禁言");
        }
    }

    @Nested
    @DisplayName("deleteMessage - 删除消息")
    class DeleteMessageTests {

        @Test
        @DisplayName("正常路径：发送者成功删除自己的消息")
        void should_delete_message_when_sender() {
            when(groupMessageDAO.findByGroupId(eq(1), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testMessage));
            when(groupMessageDAO.delete(1)).thenReturn(true);

            Result result = groupService.deleteMessage(1, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：消息ID为空应返回错误")
        void should_return_error_when_message_id_is_null() {
            Result result = groupService.deleteMessage(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.deleteMessage(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：非发送者删除消息应返回403")
        void should_return_403_when_not_sender() {
            when(groupMessageDAO.findByGroupId(eq(1), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testMessage));

            Result result = groupService.deleteMessage(1, 2);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("只能删除自己发送的消息");
        }

        @Test
        @DisplayName("异常场景：消息不存在应返回404")
        void should_return_404_when_message_not_found() {
            when(groupMessageDAO.findByGroupId(eq(1), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList());

            Result result = groupService.deleteMessage(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("消息不存在");
        }
    }

    @Nested
    @DisplayName("getMyGroups - 我的群聊")
    class GetMyGroupsTests {

        @Test
        @DisplayName("正常路径：成功获取用户的群聊列表")
        void should_return_user_groups() {
            UserGroup userGroup = new UserGroup();
            userGroup.setUserId(1);
            userGroup.setGroupId(1);
            when(userGroupDAO.findByUserId(1)).thenReturn(Arrays.asList(userGroup));

            Result result = groupService.getMyGroups(1, 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.getMyGroups(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("用户ID不能为空");
        }
    }

    @Nested
    @DisplayName("getCreatedGroups - 我创建的群聊")
    class GetCreatedGroupsTests {

        @Test
        @DisplayName("正常路径：成功获取用户创建的群聊列表")
        void should_return_created_groups() {
            when(activityGroupDAO.findByOwnerId(1)).thenReturn(Arrays.asList(testGroup));

            Result result = groupService.getCreatedGroups(1, 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：用户ID为空应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = groupService.getCreatedGroups(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }
}
