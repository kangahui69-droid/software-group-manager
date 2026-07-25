package service;

import dao.ActivityGroupDAO;
import dao.FileStorageDAO;
import dao.GroupMemberDAO;
import dao.GroupMessageDAO;
import dao.MemberProfileDAO;
import dao.UserDAO;
import dao.UserGroupDAO;
import dto.GroupDTO;
import model.ActivityGroup;
import model.GroupMember;
import model.GroupMessage;
import model.User;
import model.UserGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GroupService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.1 GroupService 群聊服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - ActivityGroupDAO: findById(id) / insert(ActivityGroup) / update(ActivityGroup) / delete(id)
 * - ActivityGroupDAO: findByOwnerId(userId) / findByUserId(userId) / findAll()
 * - ActivityGroupDAO: muteGroup(groupId, mutedUntil, reason) / unmuteGroup(groupId)
 * - GroupMemberDAO: insert(GroupMember) / insertOwner(groupId, userId) / insertMember(groupId, userId)
 * - GroupMemberDAO: delete(groupId, userId) / deleteByGroupId(groupId)
 * - GroupMemberDAO: isMember(groupId, userId) / isOwner(groupId, userId)
 * - GroupMemberDAO: findByGroupId(groupId) / countByGroupId(groupId)
 * - GroupMessageDAO: insert(GroupMessage) / findByGroupId(groupId, limit, offset)
 * - GroupMessageDAO: delete(id) / deleteByGroupId(groupId) / countByGroupId(groupId)
 * - UserGroupDAO: insertUserToGroup(userId, groupId) / delete(userId, groupId)
 * - UserGroupDAO: deleteByGroupId(groupId) / exists(userId, groupId)
 * - UserGroupDAO: findByUserId(userId)
 * - UserDAO: findById(id)
 * - MemberProfileDAO: findByUserId(userId)
 * - FileStorageDAO: insert(FileStorage)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GroupService 群聊服务测试")
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
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(
            activityGroupDAO,
            groupMemberDAO,
            groupMessageDAO,
            userGroupDAO,
            fileStorageDAO,
            userDAO,
            memberProfileDAO
        );
        // 默认mock：userDAO.findById对任何ID都返回有效用户
        when(userDAO.findById(anyInt())).thenReturn(createUser(1, "admin", ROLE_ADMIN));
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer GROUP_ID = 100;
    private static final Integer MESSAGE_ID = 200;
    private static final Integer ACTIVITY_ID = 300;

    // 角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 群成员角色枚举
    private static final String MEMBER_ROLE_OWNER = "OWNER";
    private static final String MEMBER_ROLE_MEMBER = "MEMBER";

    // 消息类型枚举
    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    private static final String MESSAGE_TYPE_FILE = "FILE";

    // ==================== 测试初始化辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private ActivityGroup createGroup(Integer id, String name, Integer ownerId) {
        ActivityGroup group = new ActivityGroup();
        group.setId(id);
        group.setGroupName(name);
        group.setGroupOwnerId(ownerId);
        group.setActivityId(ACTIVITY_ID);
        group.setCreatedAt(new Date());
        group.setIsMuted(0);
        return group;
    }

    private GroupMember createMember(Integer groupId, Integer userId, String role) {
        GroupMember member = new GroupMember();
        member.setId(userId * 1000 + groupId);
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinedAt(new Date());
        return member;
    }

    private GroupMessage createMessage(Integer id, Integer groupId, Integer senderId, String content) {
        GroupMessage message = new GroupMessage();
        message.setId(id);
        message.setGroupId(groupId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setSentAt(new Date());
        message.setMessageType(MESSAGE_TYPE_TEXT);
        return message;
    }

    private GroupDTO createGroupDTO(String name) {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName(name);
        dto.setActivityId(ACTIVITY_ID);
        return dto;
    }

    private Date createFutureDate(int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
        return cal.getTime();
    }

    // ==================== listGroups 群聊列表 ====================

    @Nested
    @DisplayName("listGroups 群聊列表")
    class ListGroupsTests {

        @FastTest
        @DisplayName("获取群聊列表成功应返回成功")
        void should_list_groups_successfully() {
            List<ActivityGroup> groups = Arrays.asList(
                createGroup(1, "群组1", ADMIN_USER_ID),
                createGroup(2, "群组2", MEMBER_USER_ID)
            );
            when(activityGroupDAO.findAll()).thenReturn(groups);

            Result result = groupService.listGroups(1, 20);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).findAll();
        }

        @FastTest
        @DisplayName("群聊列表为空时应返回空列表")
        void should_return_empty_list_when_no_groups() {
            when(activityGroupDAO.findAll()).thenReturn(Arrays.asList());

            Result result = groupService.listGroups(1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数page为1应正常返回")
        void should_handle_page_1() {
            when(activityGroupDAO.findAll()).thenReturn(Arrays.asList());

            Result result = groupService.listGroups(1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数pageSize为最大值应正常返回")
        void should_handle_large_page_size() {
            when(activityGroupDAO.findAll()).thenReturn(Arrays.asList());

            Result result = groupService.listGroups(1, 100);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = groupService.listGroups(0, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize为0时应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = groupService.listGroups(1, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize超过最大值时应返回错误")
        void should_return_error_when_page_size_exceeds_max() {
            Result result = groupService.listGroups(1, 1000);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = groupService.listGroups(-1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getGroupDetail 群聊详情 ====================

    @Nested
    @DisplayName("getGroupDetail 群聊详情")
    class GetGroupDetailTests {

        @FastTest
        @DisplayName("获取群聊详情成功应返回成功")
        void should_get_group_detail_successfully() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.getGroupDetail(GROUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).findById(GROUP_ID);
        }

        @FastTest
        @DisplayName("群聊不存在时应返回错误")
        void should_return_error_when_group_not_exists() {
            when(activityGroupDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = groupService.getGroupDetail(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("用户不是群成员时应返回错误")
        void should_return_error_when_user_not_member() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isMember(GROUP_ID, OTHER_USER_ID)).thenReturn(false);

            Result result = groupService.getGroupDetail(GROUP_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("群主获取详情应成功")
        void should_success_when_owner_gets_detail() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.getGroupDetail(GROUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null获取详情应返回错误")
        void should_return_error_when_user_id_null() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);

            Result result = groupService.getGroupDetail(GROUP_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("groupId为null获取详情应返回错误")
        void should_return_error_when_group_id_null() {
            Result result = groupService.getGroupDetail(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== createGroup 创建群聊 ====================

    @Nested
    @DisplayName("createGroup 创建群聊")
    class CreateGroupTests {

        @FastTest
        @DisplayName("创建群聊成功应返回成功")
        void should_create_group_successfully() {
            GroupDTO dto = createGroupDTO("新群组");
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenAnswer(invocation -> {
                ActivityGroup g = invocation.getArgument(0);
                g.setId(100);
                return 100;
            });
            when(groupMemberDAO.insertOwner(anyInt(), anyInt())).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(anyInt(), anyInt())).thenReturn(true);

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).insert(any(ActivityGroup.class));
            verify(groupMemberDAO).insertOwner(eq(100), eq(ADMIN_USER_ID));
        }

        @FastTest
        @DisplayName("创建群聊时应设置创建者为群主")
        void should_set_creator_as_owner() {
            GroupDTO dto = createGroupDTO("新群组");
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenAnswer(invocation -> {
                ActivityGroup g = invocation.getArgument(0);
                g.setId(100);
                return 100;
            });
            when(groupMemberDAO.insertOwner(anyInt(), anyInt())).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(anyInt(), anyInt())).thenReturn(true);

            groupService.createGroup(dto, ADMIN_USER_ID);

            ArgumentCaptor<ActivityGroup> captor = ArgumentCaptor.forClass(ActivityGroup.class);
            verify(activityGroupDAO).insert(captor.capture());
            assertThat(captor.getValue().getGroupOwnerId()).isEqualTo(ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("创建群聊时应自动添加创建者为成员")
        void should_add_creator_as_member() {
            GroupDTO dto = createGroupDTO("新群组");
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenAnswer(invocation -> {
                ActivityGroup g = invocation.getArgument(0);
                g.setId(100);
                return 100;
            });
            when(groupMemberDAO.insertOwner(anyInt(), anyInt())).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(anyInt(), anyInt())).thenReturn(true);

            groupService.createGroup(dto, ADMIN_USER_ID);

            verify(groupMemberDAO).insertOwner(eq(100), eq(ADMIN_USER_ID));
        }

        @FastTest
        @DisplayName("创建群聊时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = groupService.createGroup(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建群聊时群组名称为空应返回错误")
        void should_return_error_when_group_name_empty() {
            GroupDTO dto = createGroupDTO("");
            dto.setGroupName("");

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建群聊时群组名称为null应返回错误")
        void should_return_error_when_group_name_null() {
            GroupDTO dto = createGroupDTO("新群组");
            dto.setGroupName(null);

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建群聊时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            GroupDTO dto = createGroupDTO("新群组");

            Result result = groupService.createGroup(dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建群聊时用户不存在应返回错误")
        void should_return_error_when_user_not_exists() {
            GroupDTO dto = createGroupDTO("新群组");
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(null);

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("创建群聊时数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            GroupDTO dto = createGroupDTO("新群组");
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenReturn(0);

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("创建带活动ID的群聊应成功")
        void should_create_group_with_activity_id() {
            GroupDTO dto = createGroupDTO("活动群组");
            dto.setActivityId(ACTIVITY_ID);
            when(activityGroupDAO.insert(any(ActivityGroup.class))).thenReturn(1);
            when(groupMemberDAO.insertOwner(anyInt(), anyInt())).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(anyInt(), anyInt())).thenReturn(true);

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("群组名称过长应返回错误")
        void should_return_error_when_group_name_too_long() {
            String longName = new String(new char[256]).replace('\0', 'A');
            GroupDTO dto = createGroupDTO(longName);

            Result result = groupService.createGroup(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateGroup 更新群聊 ====================

    @Nested
    @DisplayName("updateGroup 更新群聊")
    class UpdateGroupTests {

        @FastTest
        @DisplayName("更新群聊成功应返回成功")
        void should_update_group_successfully() {
            GroupDTO dto = createGroupDTO("更新后的群组");
            ActivityGroup existingGroup = createGroup(GROUP_ID, "原群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(existingGroup);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.updateGroup(GROUP_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).findById(GROUP_ID);
        }

        @FastTest
        @DisplayName("非群主更新群聊应返回错误")
        void should_return_error_when_not_owner() {
            GroupDTO dto = createGroupDTO("更新后的群组");
            ActivityGroup existingGroup = createGroup(GROUP_ID, "原群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(existingGroup);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = groupService.updateGroup(GROUP_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("更新不存在的群聊应返回错误")
        void should_return_error_when_group_not_exists() {
            GroupDTO dto = createGroupDTO("更新后的群组");
            when(activityGroupDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = groupService.updateGroup(NONEXISTENT_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新群聊时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = groupService.updateGroup(GROUP_ID, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新群聊时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            GroupDTO dto = createGroupDTO("更新后的群组");

            Result result = groupService.updateGroup(GROUP_ID, dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新群聊时groupId为null应返回错误")
        void should_return_error_when_group_id_null() {
            GroupDTO dto = createGroupDTO("更新后的群组");

            Result result = groupService.updateGroup(null, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新群聊名称为空应返回错误")
        void should_return_error_when_group_name_empty() {
            GroupDTO dto = createGroupDTO("");
            ActivityGroup existingGroup = createGroup(GROUP_ID, "原群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(existingGroup);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.updateGroup(GROUP_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== deleteGroup 删除群聊 ====================

    @Nested
    @DisplayName("deleteGroup 删除群聊")
    class DeleteGroupTests {

        @FastTest
        @DisplayName("删除群聊成功应返回成功")
        void should_delete_group_successfully() {
            ActivityGroup existingGroup = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(existingGroup);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(activityGroupDAO.delete(GROUP_ID)).thenReturn(true);

            Result result = groupService.deleteGroup(GROUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(groupMessageDAO).deleteByGroupId(GROUP_ID);
            verify(groupMemberDAO).deleteByGroupId(GROUP_ID);
            verify(userGroupDAO).deleteByGroupId(GROUP_ID);
            verify(activityGroupDAO).delete(GROUP_ID);
        }

        @FastTest
        @DisplayName("非群主删除群聊应返回错误")
        void should_return_error_when_not_owner() {
            ActivityGroup existingGroup = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(existingGroup);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = groupService.deleteGroup(GROUP_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的群聊应返回错误")
        void should_return_error_when_group_not_exists() {
            when(activityGroupDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = groupService.deleteGroup(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除群聊时应级联删除相关数据")
        void should_cascade_delete_related_data() {
            ActivityGroup existingGroup = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(existingGroup);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(activityGroupDAO.delete(GROUP_ID)).thenReturn(true);

            groupService.deleteGroup(GROUP_ID, ADMIN_USER_ID);

            verify(groupMessageDAO).deleteByGroupId(GROUP_ID);
            verify(groupMemberDAO).deleteByGroupId(GROUP_ID);
            verify(userGroupDAO).deleteByGroupId(GROUP_ID);
        }

        @FastTest
        @DisplayName("删除群聊时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.deleteGroup(GROUP_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除群聊时groupId为null应返回错误")
        void should_return_error_when_group_id_null() {
            Result result = groupService.deleteGroup(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== addMember 添加成员 ====================

    @Nested
    @DisplayName("addMember 添加成员")
    class AddMemberTests {

        @FastTest
        @DisplayName("添加成员成功应返回成功")
        void should_add_member_successfully() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMemberDAO.isMember(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);
            when(groupMemberDAO.insertMember(GROUP_ID, MEMBER_USER_ID)).thenReturn(true);
            when(userGroupDAO.insertUserToGroup(MEMBER_USER_ID, GROUP_ID)).thenReturn(true);

            Result result = groupService.addMember(GROUP_ID, MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(groupMemberDAO).insertMember(GROUP_ID, MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("添加已存在的成员应返回错误")
        void should_return_error_when_member_already_exists() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMemberDAO.isMember(GROUP_ID, MEMBER_USER_ID)).thenReturn(true);

            Result result = groupService.addMember(GROUP_ID, MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("非群主添加成员应返回错误")
        void should_return_error_when_not_owner() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = groupService.addMember(GROUP_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("添加不存在的用户应返回错误")
        void should_return_error_when_user_not_exists() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = groupService.addMember(GROUP_ID, NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("添加到不存在的群聊应返回错误")
        void should_return_error_when_group_not_exists() {
            when(activityGroupDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = groupService.addMember(NONEXISTENT_USER_ID, MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("群主添加自己应返回错误")
        void should_return_error_when_adding_self() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.addMember(GROUP_ID, ADMIN_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加成员时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.addMember(GROUP_ID, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("添加成员时operatorId为null应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = groupService.addMember(GROUP_ID, MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== removeMember 移除成员 ====================

    @Nested
    @DisplayName("removeMember 移除成员")
    class RemoveMemberTests {

        @FastTest
        @DisplayName("移除成员成功应返回成功")
        void should_remove_member_successfully() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMemberDAO.isMember(GROUP_ID, MEMBER_USER_ID)).thenReturn(true);
            when(groupMemberDAO.delete(GROUP_ID, MEMBER_USER_ID)).thenReturn(true);
            when(userGroupDAO.delete(MEMBER_USER_ID, GROUP_ID)).thenReturn(true);

            Result result = groupService.removeMember(GROUP_ID, MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(groupMemberDAO).delete(GROUP_ID, MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("非群主移除成员应返回错误")
        void should_return_error_when_not_owner() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = groupService.removeMember(GROUP_ID, OTHER_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("移除不存在的成员应返回错误")
        void should_return_error_when_member_not_exists() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMemberDAO.isMember(GROUP_ID, NONEXISTENT_USER_ID)).thenReturn(false);

            Result result = groupService.removeMember(GROUP_ID, NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("群主移除自己应返回错误")
        void should_return_error_when_removing_self() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.removeMember(GROUP_ID, ADMIN_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("群主不能被其他管理员移除")
        void should_return_error_when_removing_owner() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.removeMember(GROUP_ID, ADMIN_USER_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("移除成员时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.removeMember(GROUP_ID, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getMessages 消息历史 ====================

    @Nested
    @DisplayName("getMessages 消息历史")
    class GetMessagesTests {

        @FastTest
        @DisplayName("获取消息历史成功应返回成功")
        void should_get_messages_successfully() {
            List<GroupMessage> messages = Arrays.asList(
                createMessage(1, GROUP_ID, ADMIN_USER_ID, "消息1"),
                createMessage(2, GROUP_ID, MEMBER_USER_ID, "消息2")
            );
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.findByGroupId(GROUP_ID, 20, 0)).thenReturn(messages);

            Result result = groupService.getMessages(GROUP_ID, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(groupMessageDAO).findByGroupId(GROUP_ID, 20, 0);
        }

        @FastTest
        @DisplayName("消息历史为空时应返回空列表")
        void should_return_empty_list_when_no_messages() {
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.findByGroupId(GROUP_ID, 20, 0)).thenReturn(Arrays.asList());

            Result result = groupService.getMessages(GROUP_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("非成员获取消息应返回错误")
        void should_return_error_when_not_member() {
            when(groupMemberDAO.isMember(GROUP_ID, OTHER_USER_ID)).thenReturn(false);

            Result result = groupService.getMessages(GROUP_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("获取第2页消息应正确计算offset")
        void should_calculate_offset_correctly_for_page_2() {
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.findByGroupId(GROUP_ID, 20, 20)).thenReturn(Arrays.asList());

            groupService.getMessages(GROUP_ID, 2);

            verify(groupMessageDAO).findByGroupId(GROUP_ID, 20, 20);
        }

        @FastTest
        @DisplayName("消息历史分页大小应为20")
        void should_use_default_page_size() {
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.findByGroupId(eq(GROUP_ID), eq(20), anyInt())).thenReturn(Arrays.asList());

            groupService.getMessages(GROUP_ID, 1);

            verify(groupMessageDAO).findByGroupId(GROUP_ID, 20, 0);
        }

        @FastTest
        @DisplayName("groupId为null获取消息应返回错误")
        void should_return_error_when_group_id_null() {
            Result result = groupService.getMessages(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== sendMessage 发送消息 ====================

    @Nested
    @DisplayName("sendMessage 发送消息")
    class SendMessageTests {

        @FastTest
        @DisplayName("发送消息成功应返回成功")
        void should_send_message_successfully() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(1);

            Result result = groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, "测试消息");

            assertThat(result.isSuccess()).isTrue();
            verify(groupMessageDAO).insert(any(GroupMessage.class));
        }

        @FastTest
        @DisplayName("发送消息时应设置正确的发送者ID")
        void should_set_correct_sender_id() {
            when(groupMemberDAO.isMember(GROUP_ID, MEMBER_USER_ID)).thenReturn(true);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(1);

            groupService.sendMessage(GROUP_ID, MEMBER_USER_ID, "消息内容");

            ArgumentCaptor<GroupMessage> captor = ArgumentCaptor.forClass(GroupMessage.class);
            verify(groupMessageDAO).insert(captor.capture());
            assertThat(captor.getValue().getSenderId()).isEqualTo(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("发送消息时应设置消息类型为TEXT")
        void should_set_message_type_to_text() {
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(1);

            groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, "测试消息");

            ArgumentCaptor<GroupMessage> captor = ArgumentCaptor.forClass(GroupMessage.class);
            verify(groupMessageDAO).insert(captor.capture());
            assertThat(captor.getValue().getMessageType()).isEqualTo(MESSAGE_TYPE_TEXT);
        }

        @FastTest
        @DisplayName("群聊被禁言时发送消息应返回错误")
        void should_return_error_when_group_is_muted() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            group.setIsMuted(1);
            group.setMutedUntil(createFutureDate(1));
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, "测试消息");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("非成员发送消息应返回错误")
        void should_return_error_when_not_member() {
            when(groupMemberDAO.isMember(GROUP_ID, OTHER_USER_ID)).thenReturn(false);

            Result result = groupService.sendMessage(GROUP_ID, OTHER_USER_ID, "测试消息");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("发送空消息应返回错误")
        void should_return_error_when_content_empty() {
            Result result = groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, "");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("发送null消息应返回错误")
        void should_return_error_when_content_null() {
            Result result = groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("发送消息时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.sendMessage(GROUP_ID, null, "测试消息");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("发送消息时groupId为null应返回错误")
        void should_return_error_when_group_id_null() {
            Result result = groupService.sendMessage(null, ADMIN_USER_ID, "测试消息");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("消息内容过长应返回错误")
        void should_return_error_when_content_too_long() {
            String longContent = new String(new char[5001]).replace('\0', 'A');
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Result result = groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, longContent);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            when(groupMemberDAO.isMember(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(groupMessageDAO.insert(any(GroupMessage.class))).thenReturn(0);

            Result result = groupService.sendMessage(GROUP_ID, ADMIN_USER_ID, "测试消息");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== muteMember 禁言 ====================

    @Nested
    @DisplayName("muteMember 禁言")
    class MuteMemberTests {

        @FastTest
        @DisplayName("禁言成功应返回成功")
        void should_mute_member_successfully() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(activityGroupDAO.muteGroup(eq(GROUP_ID), any(java.sql.Date.class), anyString())).thenReturn(true);

            Date until = createFutureDate(1);
            Result result = groupService.muteMember(GROUP_ID, MEMBER_USER_ID, until, "违反群规");

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).muteGroup(eq(GROUP_ID), any(java.sql.Date.class), eq("违反群规"));
        }

        @FastTest
        @DisplayName("禁言时reason过长应返回错误")
        void should_return_error_when_reason_too_long() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            String longReason = new String(new char[501]).replace('\0', 'A');
            Date until = createFutureDate(1);

            Result result = groupService.muteMember(GROUP_ID, MEMBER_USER_ID, until, longReason);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("非群主禁言应返回错误")
        void should_return_error_when_not_owner() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);

            Date until = createFutureDate(1);

            Result result = groupService.muteMember(GROUP_ID, OTHER_USER_ID, until, "违反群规");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("禁言不存在的群聊应返回错误")
        void should_return_error_when_group_not_exists() {
            when(activityGroupDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Date until = createFutureDate(1);

            Result result = groupService.muteMember(NONEXISTENT_USER_ID, MEMBER_USER_ID, until, "违反群规");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("禁言自己应返回错误")
        void should_return_error_when_muting_self() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);

            Date until = createFutureDate(1);

            Result result = groupService.muteMember(GROUP_ID, ADMIN_USER_ID, until, "违反群规");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("禁言时targetUserId为null应返回错误")
        void should_return_error_when_target_user_id_null() {
            Date until = createFutureDate(1);

            Result result = groupService.muteMember(GROUP_ID, null, until, "违反群规");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("禁言时无法获取群组信息应返回错误")
        void should_return_error_when_group_not_found() {
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(null);

            Date until = createFutureDate(1);
            Result result = groupService.muteMember(GROUP_ID, MEMBER_USER_ID, until, "违反群规");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("永久禁言until为null应成功")
        void should_mute_permanently_with_null_until() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(activityGroupDAO.muteGroup(eq(GROUP_ID), isNull(), eq("永久禁言"))).thenReturn(true);

            Result result = groupService.muteMember(GROUP_ID, MEMBER_USER_ID, null, "永久禁言");

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== unmuteMember 取消禁言 ====================

    @Nested
    @DisplayName("unmuteMember 取消禁言")
    class UnmuteMemberTests {

        @FastTest
        @DisplayName("取消禁言成功应返回成功")
        void should_unmute_member_successfully() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, ADMIN_USER_ID)).thenReturn(true);
            when(activityGroupDAO.unmuteGroup(GROUP_ID)).thenReturn(true);

            Result result = groupService.unmuteMember(GROUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).unmuteGroup(GROUP_ID);
        }

        @FastTest
        @DisplayName("非群主取消禁言应返回错误")
        void should_return_error_when_not_owner() {
            ActivityGroup group = createGroup(GROUP_ID, "测试群组", ADMIN_USER_ID);
            when(activityGroupDAO.findById(GROUP_ID)).thenReturn(group);
            when(groupMemberDAO.isOwner(GROUP_ID, MEMBER_USER_ID)).thenReturn(false);

            Result result = groupService.unmuteMember(GROUP_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("取消不存在的群聊禁言应返回错误")
        void should_return_error_when_group_not_exists() {
            when(activityGroupDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = groupService.unmuteMember(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("取消禁言时operatorId为null应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = groupService.unmuteMember(GROUP_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== deleteMessage 删除消息 ====================

    @Nested
    @DisplayName("deleteMessage 删除消息")
    class DeleteMessageTests {

        @FastTest
        @DisplayName("删除消息成功应返回成功")
        void should_delete_message_successfully() {
            GroupMessage message = createMessage(MESSAGE_ID, GROUP_ID, ADMIN_USER_ID, "测试消息");
            when(groupMessageDAO.findByGroupId(GROUP_ID, 1, 0)).thenReturn(Arrays.asList(message));
            when(groupMessageDAO.delete(MESSAGE_ID)).thenReturn(true);

            Result result = groupService.deleteMessage(MESSAGE_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(groupMessageDAO).delete(MESSAGE_ID);
        }

        @FastTest
        @DisplayName("非发送者删除消息应返回错误")
        void should_return_error_when_not_sender() {
            GroupMessage message = createMessage(MESSAGE_ID, GROUP_ID, ADMIN_USER_ID, "测试消息");
            when(groupMessageDAO.findByGroupId(GROUP_ID, 1, 0)).thenReturn(Arrays.asList(message));

            Result result = groupService.deleteMessage(MESSAGE_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("删除不存在的消息应返回错误")
        void should_return_error_when_message_not_exists() {
            when(groupMessageDAO.findByGroupId(GROUP_ID, 1, 0)).thenReturn(Arrays.asList());

            Result result = groupService.deleteMessage(MESSAGE_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除消息时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.deleteMessage(MESSAGE_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除消息时messageId为null应返回错误")
        void should_return_error_when_message_id_null() {
            Result result = groupService.deleteMessage(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getMyGroups 我的群聊 ====================

    @Nested
    @DisplayName("getMyGroups 我的群聊")
    class GetMyGroupsTests {

        @FastTest
        @DisplayName("获取我的群聊成功应返回成功")
        void should_get_my_groups_successfully() {
            List<UserGroup> userGroups = Arrays.asList(
                createUserGroup(MEMBER_USER_ID, 1),
                createUserGroup(MEMBER_USER_ID, 2)
            );
            when(userGroupDAO.findByUserId(MEMBER_USER_ID)).thenReturn(userGroups);

            Result result = groupService.getMyGroups(MEMBER_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(userGroupDAO).findByUserId(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("没有加入任何群聊应返回空列表")
        void should_return_empty_list_when_no_groups() {
            when(userGroupDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList());

            Result result = groupService.getMyGroups(MEMBER_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取我的群聊时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.getMyGroups(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getCreatedGroups 我创建的 ====================

    @Nested
    @DisplayName("getCreatedGroups 我创建的")
    class GetCreatedGroupsTests {

        @FastTest
        @DisplayName("获取我创建的群聊成功应返回成功")
        void should_get_created_groups_successfully() {
            List<ActivityGroup> groups = Arrays.asList(
                createGroup(1, "群组1", ADMIN_USER_ID),
                createGroup(2, "群组2", ADMIN_USER_ID)
            );
            when(activityGroupDAO.findByOwnerId(ADMIN_USER_ID)).thenReturn(groups);

            Result result = groupService.getCreatedGroups(ADMIN_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(activityGroupDAO).findByOwnerId(ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("没有创建任何群聊应返回空列表")
        void should_return_empty_list_when_no_groups() {
            when(activityGroupDAO.findByOwnerId(ADMIN_USER_ID)).thenReturn(Arrays.asList());

            Result result = groupService.getCreatedGroups(ADMIN_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取我创建的群聊时userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = groupService.getCreatedGroups(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== 辅助方法 ====================

    private UserGroup createUserGroup(Integer userId, Integer groupId) {
        UserGroup ug = new UserGroup();
        ug.setUserId(userId);
        ug.setGroupId(groupId);
        ug.setGroupName("群组" + groupId);
        ug.setMemberCount(5);
        return ug;
    }
}
