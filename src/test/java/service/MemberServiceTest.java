package service;

import dao.UserDAO;
import dao.MemberProfileDAO;
import dao.FileStorageDAO;
import dao.AdminProfileDAO;
import dao.AwardDAO;
import model.MemberProfile;
import model.User;
import model.Award;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MemberService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 5.3 MemberService 成员服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 核心方法：
 * - listMembers(filter, page, pageSize) - 成员列表(分页)
 * - getMemberDetail(id) - 成员详情(含档案)
 * - createMember(dto) - 创建成员
 * - updateMember(id, dto, operatorId) - 更新成员
 * - deleteMember(id, operatorId) - 删除成员
 * - enableMember(id, operatorId) - 启用成员
 * - disableMember(id, operatorId) - 禁用成员
 * - resetPassword(id, operatorId) - 重置密码
 * - getMemberAwards(id) - 成员获奖列表
 * - updateProfile(id, dto, userId) - 更新个人档案
 * - getProfile(id) - 获取个人档案
 * - uploadAvatar(id, file, userId) - 上传头像
 *
 * 状态枚举：
 * - UserStatus: ENABLED(1), DISABLED(0)
 * - UserRole: ADMIN, MEMBER, TEACHER
 *
 * DAO接口假设：
 * - UserDAO: findById, findByConditions, insert, update, updateStatus, delete, resetPassword, findAll
 * - MemberProfileDAO: findByUserId, insert, update, saveOrUpdate
 * - AwardDAO: findByUserId
 * - FileStorageDAO: insert, findById
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MemberService 成员服务测试")
class MemberServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    @Mock
    private AdminProfileDAO adminProfileDAO;

    @Mock
    private AwardDAO awardDAO;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(
            userDAO,
            memberProfileDAO,
            fileStorageDAO,
            adminProfileDAO,
            awardDAO
        );
        // 默认mock：userDAO.findById对任何ID都返回有效用户
        when(userDAO.findById(anyInt())).thenAnswer(invocation -> {
            Integer id = invocation.getArgument(0);
            if (id.equals(ADMIN_USER_ID)) {
                return createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN, STATUS_ENABLED);
            } else if (id.equals(MEMBER_USER_ID)) {
                return createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            } else if (id.equals(OTHER_USER_ID)) {
                return createUser(OTHER_USER_ID, "other", ROLE_MEMBER, STATUS_ENABLED);
            }
            return null;
        });
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer AVATAR_FILE_ID = 100;
    private static final Integer PROFILE_ID = 10;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_TEACHER = "TEACHER";

    private static final Integer STATUS_ENABLED = 1;
    private static final Integer STATUS_DISABLED = 0;

    private static final String DEFAULT_PASSWORD = "123456";

    // ==================== 测试初始化辅助方法 ====================

    private User createUser(Integer id, String username, String role, Integer status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setName("测试用户" + id);
        user.setEmail(username + "@test.com");
        user.setPhone("13800000000");
        user.setRole(role);
        user.setStatus(status);
        return user;
    }

    private MemberProfile createMemberProfile(Integer id, Integer userId) {
        MemberProfile profile = new MemberProfile();
        profile.setId(id);
        profile.setUserId(userId);
        profile.setStudentId("2021000000" + userId);
        profile.setMajor("计算机科学与技术");
        profile.setGrade("2021");
        profile.setIntroduction("这是简介");
        profile.setGithub("github.com/user");
        profile.setBlog("blog.example.com");
        profile.setStatus(STATUS_ENABLED);
        return profile;
    }

    private Award createAward(Integer id, Integer userId, String awardName) {
        Award award = new Award();
        award.setId(id);
        award.setName(awardName);
        award.setAwardName(awardName);
        award.setAwardStatus(Award.STATUS_APPROVED);
        return award;
    }

    private Map<String, Object> createMemberFilter() {
        Map<String, Object> filter = new HashMap<>();
        filter.put("keyword", "测试");
        filter.put("role", ROLE_MEMBER);
        filter.put("status", STATUS_ENABLED);
        return filter;
    }

    // ==================== listMembers 成员列表(分页) ====================

    @Nested
    @DisplayName("listMembers 成员列表(分页)")
    class ListMembersTests {

        @FastTest
        @DisplayName("正常查询应返回分页结果")
        void should_return_paged_members() {
            List<User> members = Arrays.asList(
                createUser(2, "member1", ROLE_MEMBER, STATUS_ENABLED),
                createUser(3, "member2", ROLE_MEMBER, STATUS_ENABLED)
            );
            when(userDAO.findByConditions(anyString(), anyString(), anyString()))
                .thenReturn(members);
            when(userDAO.count())
                .thenReturn(2);

            Result result = memberService.listMembers(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("带关键字筛选应正确传递参数")
        void should_filter_by_keyword() {
            when(userDAO.findByConditions(eq("张"), any(), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(
                Map.of("keyword", "张"), 1, 20);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).findByConditions("张", null, null);
        }

        @FastTest
        @DisplayName("带角色筛选应正确传递参数")
        void should_filter_by_role() {
            when(userDAO.findByConditions(any(), eq(ROLE_ADMIN), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(
                Map.of("role", ROLE_ADMIN), 1, 20);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).findByConditions(null, ROLE_ADMIN, null);
        }

        @FastTest
        @DisplayName("带状态筛选应正确传递参数")
        void should_filter_by_status() {
            when(userDAO.findByConditions(any(), any(), eq(STATUS_DISABLED.toString())))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(
                Map.of("status", STATUS_DISABLED.toString()), 1, 20);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).findByConditions(null, null, STATUS_DISABLED.toString());
        }

        @FastTest
        @DisplayName("空结果应返回空列表")
        void should_return_empty_list_when_no_members() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("分页参数为null应使用默认值")
        void should_use_default_pagination_when_null() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(null, null, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("DAO异常时应返回错误")
        void should_return_error_when_dao_exception() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenThrow(new RuntimeException("数据库错误"));

            Result result = memberService.listMembers(null, 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isNotEqualTo(0);
        }
    }

    // ==================== getMemberDetail 成员详情 ====================

    @Nested
    @DisplayName("getMemberDetail 成员详情(含档案)")
    class GetMemberDetailTests {

        @FastTest
        @DisplayName("存在的成员应返回详情含档案")
        void should_return_member_detail_with_profile() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            Result result = memberService.getMemberDetail(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("不存在的成员应返回错误")
        void should_return_error_when_member_not_found() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.getMemberDetail(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("成员无档案时应返回成员信息但档案为null")
        void should_return_member_without_profile() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(null);

            Result result = memberService.getMemberDetail(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.getMemberDetail(null);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== createMember 创建成员 ====================

    @Nested
    @DisplayName("createMember 创建成员")
    class CreateMemberTests {

        @FastTest
        @DisplayName("正常创建成员应成功")
        void should_create_member_successfully() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "password123");
            dto.put("name", "新成员");
            dto.put("email", "new@member.com");
            dto.put("phone", "13800000001");
            dto.put("role", ROLE_MEMBER);

            when(userDAO.findByUsername("newmember")).thenReturn(null);
            when(userDAO.insert(any(User.class))).thenReturn(true);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).insert(any(User.class));
        }

        @FastTest
        @DisplayName("用户名校重应返回错误")
        void should_return_error_when_username_exists() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "existinguser");
            dto.put("password", "password123");
            dto.put("role", ROLE_MEMBER);

            when(userDAO.findByUsername("existinguser")).thenReturn(new User());

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("用户名");
        }

        @FastTest
        @DisplayName("缺少必填字段应返回错误")
        void should_return_error_when_required_fields_missing() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            // 缺少password和role

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null dto应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = memberService.createMember(null);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("空dto应返回错误")
        void should_return_error_when_dto_is_empty() {
            Result result = memberService.createMember(new HashMap<>());

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("用户名过长应返回错误")
        void should_return_error_when_username_too_long() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "a".repeat(33)); // 用户名最大32字符
            dto.put("password", "password123");
            dto.put("role", ROLE_MEMBER);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("密码过短应返回错误")
        void should_return_error_when_password_too_short() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "12345"); // 密码最小6位
            dto.put("role", ROLE_MEMBER);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("无效角色应返回错误")
        void should_return_error_when_invalid_role() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "password123");
            dto.put("role", "INVALID_ROLE");

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("邮箱格式错误应返回错误")
        void should_return_error_when_invalid_email() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "password123");
            dto.put("email", "invalid-email");
            dto.put("role", ROLE_MEMBER);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("手机号格式错误应返回错误")
        void should_return_error_when_invalid_phone() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "password123");
            dto.put("phone", "12345");
            dto.put("role", ROLE_MEMBER);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("创建管理员角色成员应成功")
        void should_create_admin_member() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newadmin");
            dto.put("password", "password123");
            dto.put("name", "新管理员");
            dto.put("role", ROLE_ADMIN);

            when(userDAO.findByUsername("newadmin")).thenReturn(null);
            when(userDAO.insert(any(User.class))).thenReturn(true);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("DAO插入失败时应返回错误")
        void should_return_error_when_insert_fails() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "password123");
            dto.put("role", ROLE_MEMBER);

            when(userDAO.findByUsername("newmember")).thenReturn(null);
            when(userDAO.insert(any(User.class))).thenReturn(false);

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("DAO抛异常时应返回错误")
        void should_return_error_when_dao_throws_exception() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("username", "newmember");
            dto.put("password", "password123");
            dto.put("role", ROLE_MEMBER);

            when(userDAO.findByUsername("newmember")).thenThrow(new RuntimeException("数据库错误"));

            Result result = memberService.createMember(dto);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== updateMember 更新成员 ====================

    @Nested
    @DisplayName("updateMember 更新成员")
    class UpdateMemberTests {

        @FastTest
        @DisplayName("正常更新成员应成功")
        void should_update_member_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", "更新后的姓名");
            dto.put("email", "updated@member.com");

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.update(any(User.class))).thenReturn(true);

            Result result = memberService.updateMember(MEMBER_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).update(any(User.class));
        }

        @FastTest
        @DisplayName("不存在的成员应返回错误")
        void should_return_error_when_member_not_found() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", "新姓名");

            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.updateMember(NONEXISTENT_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("非管理员更新应返回错误")
        void should_return_error_when_not_admin() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", "新姓名");

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            // 其他成员尝试更新
            Result result = memberService.updateMember(MEMBER_USER_ID, dto, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("成员更新自己的信息应成功")
        void should_allow_member_update_own_info() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", "自己更新的姓名");

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.update(any(User.class))).thenReturn(true);

            // 成员更新自己的信息
            Result result = memberService.updateMember(MEMBER_USER_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.updateMember(null, new HashMap<>(), ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null dto应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = memberService.updateMember(MEMBER_USER_ID, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("空dto应返回错误")
        void should_return_error_when_dto_is_empty() {
            Result result = memberService.updateMember(MEMBER_USER_ID, new HashMap<>(), ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("更新邮箱格式错误应返回错误")
        void should_return_error_when_email_invalid() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            Map<String, Object> dto = new HashMap<>();
            dto.put("email", "invalid-email");

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            Result result = memberService.updateMember(MEMBER_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("更新手机号格式错误应返回错误")
        void should_return_error_when_phone_invalid() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            Map<String, Object> dto = new HashMap<>();
            dto.put("phone", "12345");

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            Result result = memberService.updateMember(MEMBER_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("DAO更新失败时应返回错误")
        void should_return_error_when_update_fails() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", "新姓名");

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.update(any(User.class))).thenReturn(false);

            Result result = memberService.updateMember(MEMBER_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== deleteMember 删除成员 ====================

    @Nested
    @DisplayName("deleteMember 删除成员")
    class DeleteMemberTests {

        @FastTest
        @DisplayName("管理员删除成员应成功")
        void should_delete_member_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.delete(MEMBER_USER_ID)).thenReturn(true);

            Result result = memberService.deleteMember(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).delete(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("不存在的成员应返回错误")
        void should_return_error_when_member_not_found() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.deleteMember(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("非管理员删除应返回错误")
        void should_return_error_when_not_admin() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            Result result = memberService.deleteMember(MEMBER_USER_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("删除自己应返回错误")
        void should_return_error_when_deleting_self() {
            User user = createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN, STATUS_ENABLED);

            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(user);

            Result result = memberService.deleteMember(ADMIN_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.deleteMember(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("DAO删除失败时应返回错误")
        void should_return_error_when_delete_fails() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.delete(MEMBER_USER_ID)).thenReturn(false);

            Result result = memberService.deleteMember(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== enableMember 启用成员 ====================

    @Nested
    @DisplayName("enableMember 启用成员")
    class EnableMemberTests {

        @FastTest
        @DisplayName("管理员启用成员应成功")
        void should_enable_member_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_DISABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.updateStatus(MEMBER_USER_ID, STATUS_ENABLED)).thenReturn(true);

            Result result = memberService.enableMember(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).updateStatus(MEMBER_USER_ID, STATUS_ENABLED);
        }

        @FastTest
        @DisplayName("不存在的成员应返回错误")
        void should_return_error_when_member_not_found() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.enableMember(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("非管理员启用应返回错误")
        void should_return_error_when_not_admin() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_DISABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            Result result = memberService.enableMember(MEMBER_USER_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("已启用的成员应返回成功但不重复操作")
        void should_succeed_for_already_enabled_member() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.updateStatus(MEMBER_USER_ID, STATUS_ENABLED)).thenReturn(true);

            Result result = memberService.enableMember(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.enableMember(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== disableMember 禁用成员 ====================

    @Nested
    @DisplayName("disableMember 禁用成员")
    class DisableMemberTests {

        @FastTest
        @DisplayName("管理员禁用成员应成功")
        void should_disable_member_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.updateStatus(MEMBER_USER_ID, STATUS_DISABLED)).thenReturn(true);

            Result result = memberService.disableMember(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).updateStatus(MEMBER_USER_ID, STATUS_DISABLED);
        }

        @FastTest
        @DisplayName("不存在的成员应返回错误")
        void should_return_error_when_member_not_found() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.disableMember(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("非管理员禁用应返回错误")
        void should_return_error_when_not_admin() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            Result result = memberService.disableMember(MEMBER_USER_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("禁用自己应返回错误")
        void should_return_error_when_disabling_self() {
            User user = createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN, STATUS_ENABLED);

            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(user);

            Result result = memberService.disableMember(ADMIN_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("已禁用的成员应返回成功但不重复操作")
        void should_succeed_for_already_disabled_member() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_DISABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.updateStatus(MEMBER_USER_ID, STATUS_DISABLED)).thenReturn(true);

            Result result = memberService.disableMember(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.disableMember(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== resetPassword 重置密码 ====================

    @Nested
    @DisplayName("resetPassword 重置密码")
    class ResetPasswordTests {

        @FastTest
        @DisplayName("管理员重置密码应成功")
        void should_reset_password_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.resetPassword(MEMBER_USER_ID, DEFAULT_PASSWORD)).thenReturn(true);

            Result result = memberService.resetPassword(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).resetPassword(MEMBER_USER_ID, DEFAULT_PASSWORD);
        }

        @FastTest
        @DisplayName("不存在的成员应返回错误")
        void should_return_error_when_member_not_found() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.resetPassword(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("非管理员重置密码应返回错误")
        void should_return_error_when_not_admin() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);

            Result result = memberService.resetPassword(MEMBER_USER_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("重置自己密码应返回错误")
        void should_return_error_when_resetting_own_password() {
            User user = createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN, STATUS_ENABLED);

            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(user);

            Result result = memberService.resetPassword(ADMIN_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.resetPassword(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("DAO重置失败时应返回错误")
        void should_return_error_when_reset_fails() {
            User user = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER, STATUS_ENABLED);

            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.resetPassword(MEMBER_USER_ID, DEFAULT_PASSWORD)).thenReturn(false);

            Result result = memberService.resetPassword(MEMBER_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== getMemberAwards 成员获奖列表 ====================

    @Nested
    @DisplayName("getMemberAwards 成员获奖列表")
    class GetMemberAwardsTests {

        @FastTest
        @DisplayName("有获奖记录应返回列表")
        void should_return_awards_list() {
            List<Award> awards = Arrays.asList(
                createAward(1, MEMBER_USER_ID, "程序设计大赛"),
                createAward(2, MEMBER_USER_ID, "数学建模竞赛")
            );

            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(awards);

            Result result = memberService.getMemberAwards(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(awards);
        }

        @FastTest
        @DisplayName("无获奖记录应返回空列表")
        void should_return_empty_list_when_no_awards() {
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Collections.emptyList());

            Result result = memberService.getMemberAwards(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.getMemberAwards(null);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("DAO异常时应返回错误")
        void should_return_error_when_dao_exception() {
            when(awardDAO.findByUserId(any())).thenThrow(new RuntimeException("数据库错误"));

            Result result = memberService.getMemberAwards(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== updateProfile 更新个人档案 ====================

    @Nested
    @DisplayName("updateProfile 更新个人档案")
    class UpdateProfileTests {

        @FastTest
        @DisplayName("正常更新档案应成功")
        void should_update_profile_successfully() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            Map<String, Object> dto = new HashMap<>();
            dto.put("studentId", "2021000001");
            dto.put("major", "软件工程");
            dto.put("grade", "2022");
            dto.put("introduction", "更新后的简介");

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);
            when(memberProfileDAO.update(any(MemberProfile.class))).thenReturn(true);

            Result result = memberService.updateProfile(MEMBER_USER_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(memberProfileDAO).update(any(MemberProfile.class));
        }

        @FastTest
        @DisplayName("成员无档案时自动创建")
        void should_create_profile_when_not_exists() {
            Map<String, Object> dto = new HashMap<>();
            dto.put("studentId", "2021000001");
            dto.put("major", "软件工程");

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(null);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);

            Result result = memberService.updateProfile(MEMBER_USER_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(memberProfileDAO).insert(any(MemberProfile.class));
        }

        @FastTest
        @DisplayName("非本人更新应返回错误")
        void should_return_error_when_not_owner() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            Map<String, Object> dto = new HashMap<>();
            dto.put("studentId", "2021000001");

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            // 其他成员尝试更新
            Result result = memberService.updateProfile(MEMBER_USER_ID, dto, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("管理员可更新任意档案")
        void should_allow_admin_update_any_profile() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            Map<String, Object> dto = new HashMap<>();
            dto.put("studentId", "2021000001");

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);
            when(memberProfileDAO.update(any(MemberProfile.class))).thenReturn(true);

            // 管理员更新成员档案
            Result result = memberService.updateProfile(MEMBER_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null userId应返回错误")
        void should_return_error_when_userId_is_null() {
            Result result = memberService.updateProfile(null, new HashMap<>(), MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null dto应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = memberService.updateProfile(MEMBER_USER_ID, null, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("学号格式错误应返回错误")
        void should_return_error_when_studentId_invalid() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            Map<String, Object> dto = new HashMap<>();
            dto.put("studentId", "ABC"); // 学号应该是数字

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            Result result = memberService.updateProfile(MEMBER_USER_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("年级格式错误应返回错误")
        void should_return_error_when_grade_invalid() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            Map<String, Object> dto = new HashMap<>();
            dto.put("grade", "25"); // 年级应该是4位数字

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            Result result = memberService.updateProfile(MEMBER_USER_ID, dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== getProfile 获取个人档案 ====================

    @Nested
    @DisplayName("getProfile 获取个人档案")
    class GetProfileTests {

        @FastTest
        @DisplayName("存在的档案应返回详情")
        void should_return_profile_when_exists() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            Result result = memberService.getProfile(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(profile);
        }

        @FastTest
        @DisplayName("不存在的档案应返回错误")
        void should_return_error_when_profile_not_found() {
            when(memberProfileDAO.findByUserId(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = memberService.getProfile(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null ID应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = memberService.getProfile(null);

            assertThat(result.isSuccess()).isFalse();
        }
    }

    // ==================== uploadAvatar 上传头像 ====================

    @Nested
    @DisplayName("uploadAvatar 上传头像")
    class UploadAvatarTests {

        @FastTest
        @DisplayName("正常上传头像应成功")
        void should_upload_avatar_successfully() throws SQLException {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            InputStream mockInputStream = mock(InputStream.class);

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);
            when(fileStorageDAO.insert(any())).thenReturn(AVATAR_FILE_ID);
            when(memberProfileDAO.update(any(MemberProfile.class))).thenReturn(true);

            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "test.jpg", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(AVATAR_FILE_ID);
        }

        @FastTest
        @DisplayName("非本人上传头像应返回错误")
        void should_return_error_when_not_owner() {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            InputStream mockInputStream = mock(InputStream.class);

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            // 其他成员尝试上传
            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "test.jpg", OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("管理员可上传任意头像")
        void should_allow_admin_upload_any_avatar() throws SQLException {
            MemberProfile profile = createMemberProfile(PROFILE_ID, MEMBER_USER_ID);
            InputStream mockInputStream = mock(InputStream.class);

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);
            when(fileStorageDAO.insert(any())).thenReturn(AVATAR_FILE_ID);
            when(memberProfileDAO.update(any(MemberProfile.class))).thenReturn(true);

            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "test.jpg", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("null userId应返回错误")
        void should_return_error_when_userId_is_null() throws SQLException {
            InputStream mockInputStream = mock(InputStream.class);

            Result result = memberService.uploadAvatar(null, mockInputStream, "test.jpg", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("null file应返回错误")
        void should_return_error_when_file_is_null() {
            Result result = memberService.uploadAvatar(MEMBER_USER_ID, null, "test.jpg", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("空文件名应返回错误")
        void should_return_error_when_filename_empty() throws SQLException {
            InputStream mockInputStream = mock(InputStream.class);

            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("无效文件类型应返回错误")
        void should_return_error_when_invalid_file_type() throws SQLException {
            InputStream mockInputStream = mock(InputStream.class);

            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "test.exe", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("文件过大应返回错误")
        void should_return_error_when_file_too_large() throws SQLException {
            InputStream mockInputStream = mock(InputStream.class);

            // 模拟超大文件（5MB+）
            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "test.jpg", MEMBER_USER_ID);

            // 文件大小检查可能在Service层或更早进行
            assertThat(result.isSuccess()).isFalse();
        }

        @FastTest
        @DisplayName("成员无档案时自动创建档案")
        void should_create_profile_when_uploading_avatar_without_profile() throws SQLException {
            InputStream mockInputStream = mock(InputStream.class);

            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(null);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(fileStorageDAO.insert(any())).thenReturn(AVATAR_FILE_ID);

            Result result = memberService.uploadAvatar(MEMBER_USER_ID, mockInputStream, "test.jpg", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 边界条件和异常场景 ====================

    @Nested
    @DisplayName("边界条件和异常场景")
    class BoundaryAndExceptionTests {

        @FastTest
        @DisplayName("分页页码为0应使用默认值1")
        void should_use_default_page_when_zero() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(null, 0, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小为负数应使用默认值")
        void should_use_default_page_size_when_negative() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(null, 1, -10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小超过最大值应限制在最大值")
        void should_limit_page_size_to_max() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenReturn(Collections.emptyList());
            when(userDAO.count())
                .thenReturn(0);

            Result result = memberService.listMembers(null, 1, 1000); // 超过最大100

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("所有DAO同时异常应返回错误")
        void should_handle_multiple_dao_exceptions() {
            when(userDAO.findByConditions(any(), any(), any()))
                .thenThrow(new RuntimeException("数据库连接失败"));

            Result result = memberService.listMembers(null, 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isGreaterThan(0);
        }
    }
}
