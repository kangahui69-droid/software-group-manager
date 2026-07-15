package service;

import dao.FileStorageDAO;
import dao.MemberProfileDAO;
import dao.UserDAO;
import dto.ProfileDTO;
import model.MemberProfile;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.2 UserService 用户服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 注意：这是Red阶段，UserService尚未实现
 * 所有测试将失败，直至实现对应方法
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - UserDAO: findByUsernameAndPassword(username, password) / findById(id) / findByUsername(username)
 * - UserDAO: update(User) / updatePassword(userId, newPassword) / updatePasswordDirect(userId, encryptedPassword)
 * - UserDAO: findByConditions(keyword, role, status)
 * - MemberProfileDAO: findByUserId(userId) / update(MemberProfile) / insert(MemberProfile)
 * - FileStorageDAO: insert(FileStorage) / findById(id)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService 用户服务测试")
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    @InjectMocks
    private UserService userService;

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;

    // 角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_TEACHER = "TEACHER";

    // 用户状态枚举
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_DISABLED = 0;

    // 用户类型枚举
    private static final String USER_TYPE_STUDENT = "STUDENT";
    private static final String USER_TYPE_TEACHER = "TEACHER";

    // ==================== 工具方法 ====================

    private User createUser(Integer id, String username, String password, String role, int status) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setName("用户" + id);
        user.setEmail(username + "@example.com");
        user.setPhone("1380000000" + id);
        user.setRole(role);
        user.setStatus(status);
        user.setUserType(USER_TYPE_STUDENT);
        return user;
    }

    private MemberProfile createMemberProfile(Integer userId) {
        MemberProfile profile = new MemberProfile();
        profile.setId(userId);
        profile.setUserId(userId);
        profile.setStudentId("2021000" + userId);
        profile.setMajor("计算机科学与技术");
        profile.setGrade("2021");
        profile.setIntroduction("自我介绍");
        profile.setGithub("github.com/user" + userId);
        profile.setBlog("blog.example.com");
        profile.setStatus(STATUS_ACTIVE);
        return profile;
    }

    // ==================== login 登录验证 ====================

    @Nested
    @DisplayName("login 登录验证")
    class LoginTests {

        @FastTest
        @DisplayName("用户名和密码正确应返回成功")
        void should_login_successfully_with_correct_credentials() {
            User user = createUser(MEMBER_USER_ID, "member1", "encryptedPwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findByUsername("member1")).thenReturn(user);
            when(userDAO.findByUsernameAndPassword("member1", "member123")).thenReturn(user);

            Result result = userService.login("member1", "member123");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(user);
        }

        @FastTest
        @DisplayName("用户名正确但密码错误应返回错误")
        void should_return_error_when_password_wrong() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findByUsername("member1")).thenReturn(user);
            when(userDAO.findByUsernameAndPassword("member1", "wrongpwd")).thenReturn(null);

            Result result = userService.login("member1", "wrongpwd");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("密码错误");
        }

        @FastTest
        @DisplayName("用户名不存在应返回错误")
        void should_return_error_when_username_not_exists() {
            when(userDAO.findByUsername("nonexistent")).thenReturn(null);
            when(userDAO.findByUsernameAndPassword("nonexistent", "anypwd")).thenReturn(null);

            Result result = userService.login("nonexistent", "anypwd");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("用户被禁用应返回错误")
        void should_return_error_when_user_disabled() {
            User user = createUser(MEMBER_USER_ID, "disableduser", "pwd", ROLE_MEMBER, STATUS_DISABLED);
            when(userDAO.findByUsername("disableduser")).thenReturn(user);
            when(userDAO.findByUsernameAndPassword("disableduser", "pwd")).thenReturn(user);

            Result result = userService.login("disableduser", "pwd");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("禁用");
        }

        @FastTest
        @DisplayName("用户名为空应返回错误")
        void should_return_error_when_username_empty() {
            Result result = userService.login("", "anypwd");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("密码为空应返回错误")
        void should_return_error_when_password_empty() {
            Result result = userService.login("member1", "");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("用户名和密码都为null应返回错误")
        void should_return_error_when_both_null() {
            Result result = userService.login(null, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("管理员登录应返回成功")
        void should_login_successfully_as_admin() {
            User admin = createUser(ADMIN_USER_ID, "admin", "encryptedPwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findByUsername("admin")).thenReturn(admin);
            when(userDAO.findByUsernameAndPassword("admin", "admin123")).thenReturn(admin);

            Result result = userService.login("admin", "admin123");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(admin);
        }

        @FastTest
        @DisplayName("教师用户登录应返回成功")
        void should_login_successfully_as_teacher() {
            User teacher = createUser(OTHER_USER_ID, "teacher1", "pwd", ROLE_TEACHER, STATUS_ACTIVE);
            when(userDAO.findByUsername("teacher1")).thenReturn(teacher);
            when(userDAO.findByUsernameAndPassword("teacher1", "teacher123")).thenReturn(teacher);

            Result result = userService.login("teacher1", "teacher123");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(teacher);
        }
    }

    // ==================== changePassword 修改密码 ====================

    @Nested
    @DisplayName("changePassword 修改密码")
    class ChangePasswordTests {

        @FastTest
        @DisplayName("旧密码正确且新密码符合要求应修改成功")
        void should_change_password_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", "encryptedOld", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.updatePassword(eq(MEMBER_USER_ID), anyString())).thenReturn(true);

            Result result = userService.changePassword(MEMBER_USER_ID, "oldpwd", "newpwd123");

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).updatePassword(eq(MEMBER_USER_ID), anyString());
        }

        @FastTest
        @DisplayName("用户不存在应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = userService.changePassword(NONEXISTENT_USER_ID, "oldpwd", "newpwd123");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("旧密码为空应返回错误")
        void should_return_error_when_old_password_empty() {
            Result result = userService.changePassword(MEMBER_USER_ID, "", "newpwd123");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("新密码为空应返回错误")
        void should_return_error_when_new_password_empty() {
            Result result = userService.changePassword(MEMBER_USER_ID, "oldpwd", "");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("新密码长度小于6位应返回错误")
        void should_return_error_when_new_password_too_short() {
            Result result = userService.changePassword(MEMBER_USER_ID, "oldpwd", "12345");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("至少6位");
        }

        @FastTest
        @DisplayName("新密码长度大于20位应返回错误")
        void should_return_error_when_new_password_too_long() {
            Result result = userService.changePassword(MEMBER_USER_ID, "oldpwd", "a".repeat(21));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("新密码与旧密码相同应返回错误")
        void should_return_error_when_new_password_same_as_old() {
            Result result = userService.changePassword(MEMBER_USER_ID, "samepwd", "samepwd");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不能与旧密码相同");
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            User user = createUser(MEMBER_USER_ID, "member1", "encryptedOld", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(userDAO.updatePassword(eq(MEMBER_USER_ID), anyString())).thenReturn(false);

            Result result = userService.changePassword(MEMBER_USER_ID, "oldpwd", "newpwd123");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = userService.changePassword(null, "oldpwd", "newpwd123");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateProfile 更新个人档案 ====================

    @Nested
    @DisplayName("updateProfile 更新个人档案")
    class UpdateProfileTests {

        @FastTest
        @DisplayName("正常更新档案应成功")
        void should_update_profile_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            user.setName("用户2"); // Match the name in createProfileDTO
            MemberProfile existingProfile = createMemberProfile(MEMBER_USER_ID);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(existingProfile);
            when(userDAO.update(any(User.class))).thenReturn(true);
            when(memberProfileDAO.update(any(MemberProfile.class))).thenReturn(true);

            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO("用户2", "newemail@example.com", "13800000002", "1990-01-01", "20210002", "2022", "新介绍", "github.com/new", "new.blog.com"));
            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("用户不存在应返回404错误")
        void should_return_not_found_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = userService.updateProfile(NONEXISTENT_USER_ID, createProfileDTO("name", "email@test.com", null, null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("修改不可改字段（姓名）应返回错误")
        void should_return_error_when_trying_to_change_name() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            user.setName("原始姓名");
            MemberProfile existingProfile = createMemberProfile(MEMBER_USER_ID);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(existingProfile);

            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO("新姓名", "email@test.com", null, null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("姓名");
            assertThat(result.getMessage()).contains("不可修改");
        }

        @FastTest
        @DisplayName("修改不可改字段（学号）应返回错误")
        void should_return_error_when_trying_to_change_student_id() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            MemberProfile existingProfile = createMemberProfile(MEMBER_USER_ID);
            existingProfile.setStudentId("123456");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(existingProfile);

            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "email@test.com", null, null, "NEW123", null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("学号");
            assertThat(result.getMessage()).contains("不可修改");
        }

        @FastTest
        @DisplayName("邮箱格式错误应返回错误")
        void should_return_error_when_email_invalid() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "invalid-email", null, null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("邮箱");
        }

        @FastTest
        @DisplayName("邮箱已被其他用户使用应返回错误")
        void should_return_error_when_email_duplicate() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            MemberProfile existingProfile = createMemberProfile(MEMBER_USER_ID);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(existingProfile);
            when(userDAO.update(any(User.class))).thenThrow(new RuntimeException("Duplicate entry"));

            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "duplicate@example.com", null, null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("邮箱");
            assertThat(result.getMessage()).contains("已被使用");
        }

        @FastTest
        @DisplayName("手机号格式错误应返回错误")
        void should_return_error_when_phone_invalid() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "email@test.com", "12345", null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("手机号");
        }

        @FastTest
        @DisplayName("生日格式错误应返回错误")
        void should_return_error_when_birthday_invalid() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "email@test.com", null, "invalid-date", null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("简介超长应返回错误")
        void should_return_error_when_introduction_too_long() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "email@test.com", null, null, null, null, "a".repeat(501), null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("简介");
            assertThat(result.getMessage()).contains("不能超过");
        }

        @FastTest
        @DisplayName("GitHub URL格式错误应返回错误")
        void should_return_error_when_github_url_invalid() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "email@test.com", null, null, null, null, null, "not-a-valid-url", null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("GitHub");
        }

        @FastTest
        @DisplayName("Blog URL格式错误应返回错误")
        void should_return_error_when_blog_url_invalid() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "email@test.com", null, null, null, null, null, null, "not-a-valid-url"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("博客");
        }

        @FastTest
        @DisplayName("userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = userService.updateProfile(null, createProfileDTO("name", "email@test.com", null, null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("ProfileDTO为null应返回错误")
        void should_return_error_when_profile_dto_null() {
            Result result = userService.updateProfile(MEMBER_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("首次创建档案应成功（profile为null）")
        void should_create_profile_when_not_exists() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            user.setName("新用户"); // Match the name in profileDTO
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(null);
            when(userDAO.update(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);

            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO("新用户", "newemail@test.com", "13800000002", "2000-01-01", "20210001", "2022", "新用户简介", "github.com/new", "new.blog.com"));

            assertThat(result.isSuccess()).isTrue();
            verify(memberProfileDAO).insert(any(MemberProfile.class));
        }

        private ProfileDTO createProfileDTO(String name, String email, String phone, String birthday,
                                         String studentId, String grade, String introduction,
                                         String github, String blog) {
            ProfileDTO dto = new ProfileDTO();
            dto.setName(name);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setBirthday(birthday);
            dto.setStudentId(studentId);
            dto.setGrade(grade);
            dto.setIntroduction(introduction);
            dto.setGithub(github);
            dto.setBlog(blog);
            return dto;
        }
    }

    // ==================== uploadAvatar 上传头像 ====================

    @Nested
    @DisplayName("uploadAvatar 上传头像")
    class UploadAvatarTests {

        @FastTest
        @DisplayName("用户不存在应返回404错误")
        void should_return_not_found_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = userService.uploadAvatar(NONEXISTENT_USER_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("文件大小超过500KB应返回错误")
        void should_return_error_when_file_too_large() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(createMemberProfile(MEMBER_USER_ID));

            Result result = userService.uploadAvatar(MEMBER_USER_ID, createMockPart(501 * 1024, "image/png"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("500KB");
        }

        @FastTest
        @DisplayName("文件类型不是图片应返回错误")
        void should_return_error_when_file_not_image() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(createMemberProfile(MEMBER_USER_ID));

            Result result = userService.uploadAvatar(MEMBER_USER_ID, createMockPart(1024, "application/pdf"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("图片");
        }

        @FastTest
        @DisplayName("文件为空应返回错误")
        void should_return_error_when_file_empty() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(createMemberProfile(MEMBER_USER_ID));

            Result result = userService.uploadAvatar(MEMBER_USER_ID, createMockPart(0, "image/png"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = userService.uploadAvatar(null, createMockPart(1024, "image/png"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        private Object createMockPart(long size, String contentType) {
            return new Object() {
                public long getSize() { return size; }
                public String getContentType() { return contentType; }
                public String getSubmittedFileName() { return "avatar.png"; }
            };
        }
    }

    // ==================== getUserDetail 用户详情 ====================

    @Nested
    @DisplayName("getUserDetail 用户详情")
    class GetUserDetailTests {

        @FastTest
        @DisplayName("获取用户详情应成功")
        void should_get_user_detail_successfully() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            MemberProfile profile = createMemberProfile(MEMBER_USER_ID);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(profile);

            Result result = userService.getUserDetail(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("用户不存在应返回404错误")
        void should_return_not_found_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = userService.getUserDetail(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("用户无档案时应返回成功（档案为null）")
        void should_return_success_when_profile_null() {
            User user = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(user);
            when(memberProfileDAO.findByUserId(MEMBER_USER_ID)).thenReturn(null);

            Result result = userService.getUserDetail(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = userService.getUserDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== listMembers 成员列表 ====================

    @Nested
    @DisplayName("listMembers 成员列表")
    class ListMembersTests {

        @FastTest
        @DisplayName("管理员获取成员列表应成功")
        void should_list_members_successfully_as_admin() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            User member1 = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            User member2 = createUser(OTHER_USER_ID, "member2", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(any(), eq(ROLE_MEMBER), any()))
                    .thenReturn(Arrays.asList(member1, member2));

            Result result = userService.listMembers(ADMIN_USER_ID, null, ROLE_MEMBER, 1, 10);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("非管理员获取成员列表应返回403错误")
        void should_return_forbidden_when_not_admin() {
            User member = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = userService.listMembers(MEMBER_USER_ID, null, ROLE_MEMBER, 1, 10);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("按关键词搜索应正确过滤")
        void should_filter_by_keyword() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(eq("张"), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = userService.listMembers(ADMIN_USER_ID, "张", ROLE_MEMBER, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_success_with_empty_list() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = userService.listMembers(ADMIN_USER_ID, null, null, 1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数为负数时应使用默认值")
        void should_use_default_when_page_negative() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = userService.listMembers(ADMIN_USER_ID, null, null, -1, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小超过最大值时应限制")
        void should_limit_page_size_when_exceeds_max() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = userService.listMembers(ADMIN_USER_ID, null, null, 1, 1000);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = userService.listMembers(null, null, null, 1, 10);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateAdminAvatar 管理员头像 ====================

    @Nested
    @DisplayName("updateAdminAvatar 管理员头像")
    class UpdateAdminAvatarTests {

        @FastTest
        @DisplayName("非管理员上传头像应返回403错误")
        void should_return_forbidden_when_not_admin() {
            User member = createUser(MEMBER_USER_ID, "member1", "pwd", ROLE_MEMBER, STATUS_ACTIVE);
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(member);

            Result result = userService.updateAdminAvatar(MEMBER_USER_ID, createMockPart(1024, "image/png"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("管理员上传头像应成功")
        void should_update_avatar_successfully_as_admin() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(memberProfileDAO.findByUserId(ADMIN_USER_ID)).thenReturn(createMemberProfile(ADMIN_USER_ID));
            when(fileStorageDAO.insert(any())).thenReturn(100);
            when(userDAO.update(any(User.class))).thenReturn(true);
            when(memberProfileDAO.update(any(MemberProfile.class))).thenReturn(true);

            Result result = userService.updateAdminAvatar(ADMIN_USER_ID, createMockPart(1024, "image/png"));

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("管理员上传非图片文件应返回错误")
        void should_return_error_when_file_not_image() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(memberProfileDAO.findByUserId(ADMIN_USER_ID)).thenReturn(createMemberProfile(ADMIN_USER_ID));

            Result result = userService.updateAdminAvatar(ADMIN_USER_ID, createMockPart(1024, "text/plain"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("管理员上传超过500KB的头像应返回错误")
        void should_return_error_when_file_too_large() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(memberProfileDAO.findByUserId(ADMIN_USER_ID)).thenReturn(createMemberProfile(ADMIN_USER_ID));

            Result result = userService.updateAdminAvatar(ADMIN_USER_ID, createMockPart(501 * 1024, "image/png"));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        private Object createMockPart(long size, String contentType) {
            return new Object() {
                public long getSize() { return size; }
                public String getContentType() { return contentType; }
                public String getSubmittedFileName() { return "avatar.png"; }
            };
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性测试")
    class StatusEnumTests {

        @Test
        @DisplayName("角色枚举应包含所有预期值")
        void role_enums_should_be_complete() {
            assertThat(ROLE_ADMIN).isEqualTo("ADMIN");
            assertThat(ROLE_MEMBER).isEqualTo("MEMBER");
            assertThat(ROLE_TEACHER).isEqualTo("TEACHER");
        }

        @Test
        @DisplayName("用户状态枚举应包含所有预期值")
        void user_status_enums_should_be_complete() {
            assertThat(STATUS_ACTIVE).isEqualTo(1);
            assertThat(STATUS_DISABLED).isEqualTo(0);
        }

        @Test
        @DisplayName("用户类型枚举应包含所有预期值")
        void user_type_enums_should_be_complete() {
            assertThat(USER_TYPE_STUDENT).isEqualTo("STUDENT");
            assertThat(USER_TYPE_TEACHER).isEqualTo("TEACHER");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("用户ID为0应返回错误")
        void should_return_error_when_user_id_zero() {
            Result result = userService.getUserDetail(0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("负数用户ID应返回错误")
        void should_return_error_when_user_id_negative() {
            Result result = userService.getUserDetail(-1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("分页页码为0时应使用页码1")
        void should_use_page_1_when_page_is_zero() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = userService.listMembers(ADMIN_USER_ID, null, null, 0, 10);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页大小为0时应使用默认值")
        void should_use_default_page_size_when_zero() {
            User admin = createUser(ADMIN_USER_ID, "admin", "pwd", ROLE_ADMIN, STATUS_ACTIVE);
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(admin);
            when(userDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList());

            Result result = userService.listMembers(ADMIN_USER_ID, null, null, 1, 0);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("新密码为空白字符应返回错误")
        void should_return_error_when_new_password_whitespace() {
            Result result = userService.changePassword(MEMBER_USER_ID, "oldpwd", "   ");

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("邮箱为空白字符应返回错误")
        void should_return_error_when_email_whitespace() {
            Result result = userService.updateProfile(MEMBER_USER_ID, createProfileDTO(null, "   ", null, null, null, null, null, null, null));

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        private ProfileDTO createProfileDTO(String name, String email, String phone, String birthday,
                                         String studentId, String grade, String introduction,
                                         String github, String blog) {
            ProfileDTO dto = new ProfileDTO();
            dto.setName(name);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setBirthday(birthday);
            dto.setStudentId(studentId);
            dto.setGrade(grade);
            dto.setIntroduction(introduction);
            dto.setGithub(github);
            dto.setBlog(blog);
            return dto;
        }
    }
}
