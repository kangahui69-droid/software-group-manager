package service;

import dao.MemberProfileDAO;
import dao.RecruitApplicationDAO;
import dao.UserDAO;
import dto.RecruitApplicationDTO;
import model.MemberProfile;
import model.RecruitApplication;
import model.User;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RecruitService TDD测试套件
 *
 * 测试范围：服务分层与API化重构计划.md 4.3 RecruitService 招新服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * Mock依赖：
 * - RecruitApplicationDAO: insert / findById / update / delete / findByConditions / countPending / findAllYears
 * - UserDAO: existsByUsername / existsByEmail / insert / findById
 * - MemberProfileDAO: insert
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecruitService 招新服务测试")
class RecruitServiceTest {

    @Mock
    private RecruitApplicationDAO recruitDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    private RecruitService recruitService;

    @BeforeEach
    void setUp() {
        recruitService = new RecruitService(recruitDAO, userDAO, memberProfileDAO);
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer OPERATOR_ID = 1;

    // 申请状态枚举
    private static final Integer STATUS_PENDING = 1;
    private static final Integer STATUS_APPROVED = 2;
    private static final Integer STATUS_REJECTED = 0;

    // 用户角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 默认密码常量
    private static final String DEFAULT_PASSWORD = "123456";

    // ==================== 测试数据构建 ====================

    private RecruitApplication createApplication(Integer id, String name, String studentId,
                                                  String major, Integer status) {
        RecruitApplication app = new RecruitApplication();
        app.setId(id);
        app.setName(name);
        app.setStudentId(studentId);
        app.setMajor(major);
        app.setGrade("2024");
        app.setPhone("13800138000");
        app.setEmail(studentId + "@example.com");
        app.setReason("热爱技术，希望加入");
        app.setStatus(status);
        return app;
    }

    private RecruitApplicationDTO createApplicationDTO(String name, String studentId,
                                                        String major, String email) {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName(name);
        dto.setStudentId(studentId);
        dto.setMajor(major);
        dto.setGrade("2024");
        dto.setPhone("13800138000");
        dto.setEmail(email);
        dto.setReason("热爱技术，希望加入");
        return dto;
    }

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setName(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private User createUserWithName(Integer id, String username, String name, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性测试")
    class StatusEnumTests {

        @Test
        @DisplayName("申请状态常量应与RecruitApplication模型一致")
        void application_status_constants_should_match() {
            assertThat(RecruitApplication.STATUS_PENDING).isEqualTo(1);
            assertThat(RecruitApplication.STATUS_APPROVED).isEqualTo(2);
            assertThat(RecruitApplication.STATUS_REJECTED).isEqualTo(0);
        }

        @Test
        @DisplayName("Service状态常量应与模型一致")
        void service_status_constants_should_match() {
            assertThat(RecruitService.STATUS_PENDING).isEqualTo(STATUS_PENDING);
            assertThat(RecruitService.STATUS_APPROVED).isEqualTo(STATUS_APPROVED);
            assertThat(RecruitService.STATUS_REJECTED).isEqualTo(STATUS_REJECTED);
        }
    }

    // ==================== submitApplication 提交申请 ====================

    @Nested
    @DisplayName("submitApplication 提交申请")
    class SubmitApplicationTests {

        @FastTest
        @DisplayName("提交申请成功应返回成功")
        void should_submit_application_successfully() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "zhangsan@example.com");

            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<RecruitApplication> captor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).insert(captor.capture());
            RecruitApplication saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("张三");
            assertThat(saved.getStudentId()).isEqualTo("2024001");
            assertThat(saved.getMajor()).isEqualTo("软件工程");
            assertThat(saved.getStatus()).isEqualTo(STATUS_PENDING);
        }

        @FastTest
        @DisplayName("提交申请时应设置状态为PENDING")
        void should_set_status_to_pending_when_submit() {
            RecruitApplicationDTO dto = createApplicationDTO("李四", "2024002", "计算机科学", "lisi@example.com");

            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<RecruitApplication> captor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).insert(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(STATUS_PENDING);
        }

        @FastTest
        @DisplayName("提交申请时姓名为空应返回错误")
        void should_return_error_when_name_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("", "2024001", "软件工程", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("姓名");
        }

        @FastTest
        @DisplayName("提交申请时姓名为null应返回错误")
        void should_return_error_when_name_null() {
            RecruitApplicationDTO dto = createApplicationDTO(null, "2024001", "软件工程", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交申请时学号为空应返回错误")
        void should_return_error_when_student_id_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "", "软件工程", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("学号");
        }

        @FastTest
        @DisplayName("提交申请时学号为null应返回错误")
        void should_return_error_when_student_id_null() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", null, "软件工程", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交申请时专业为空应返回错误")
        void should_return_error_when_major_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("专业");
        }

        @FastTest
        @DisplayName("提交申请时专业为null应返回错误")
        void should_return_error_when_major_null() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", null, "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交申请时邮箱为空应返回错误")
        void should_return_error_when_email_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("邮箱");
        }

        @FastTest
        @DisplayName("提交申请时邮箱为null应返回错误")
        void should_return_error_when_email_null() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", null);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交申请时dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = recruitService.submitApplication(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("提交申请时应保存所有字段")
        void should_save_all_fields_when_submit() {
            RecruitApplicationDTO dto = createApplicationDTO("王五", "2024003", "信息安全", "wangwu@example.com");
            dto.setGrade("2023");
            dto.setPhone("13900139000");
            dto.setReason("对网络安全感兴趣");

            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<RecruitApplication> captor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).insert(captor.capture());
            RecruitApplication saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("王五");
            assertThat(saved.getStudentId()).isEqualTo("2024003");
            assertThat(saved.getMajor()).isEqualTo("信息安全");
            assertThat(saved.getGrade()).isEqualTo("2023");
            assertThat(saved.getPhone()).isEqualTo("13900139000");
            assertThat(saved.getEmail()).isEqualTo("wangwu@example.com");
            assertThat(saved.getReason()).isEqualTo("对网络安全感兴趣");
        }

        @FastTest
        @DisplayName("提交申请时grade为null应正常处理")
        void should_handle_null_grade() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "test@example.com");
            dto.setGrade(null);

            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("提交申请时phone为null应正常处理")
        void should_handle_null_phone() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "test@example.com");
            dto.setPhone(null);

            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("提交申请时reason为null应正常处理")
        void should_handle_null_reason() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "test@example.com");
            dto.setReason(null);

            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(false);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("姓名仅包含空格应返回错误")
        void should_return_error_when_name_only_spaces() {
            RecruitApplicationDTO dto = createApplicationDTO("   ", "2024001", "软件工程", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("学号仅包含空格应返回错误")
        void should_return_error_when_student_id_only_spaces() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "   ", "软件工程", "test@example.com");

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== approveApplication 审批通过 ====================

    @Nested
    @DisplayName("approveApplication 审批通过")
    class ApproveApplicationTests {

        @FastTest
        @DisplayName("审批通过新用户应创建User和MemberProfile")
        void should_create_user_and_profile_when_approve_new_student() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).insert(any(User.class));
            verify(memberProfileDAO).insert(any(MemberProfile.class));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).insert(userCaptor.capture());
            User createdUser = userCaptor.getValue();
            assertThat(createdUser.getUsername()).isEqualTo("2024001");
            assertThat(createdUser.getRole()).isEqualTo(ROLE_MEMBER);
            assertThat(createdUser.getMustChangePassword()).isTrue();
        }

        @FastTest
        @DisplayName("审批通过时应设置默认密码123456")
        void should_set_default_password_when_approve() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(DEFAULT_PASSWORD);
        }

        @FastTest
        @DisplayName("审批通过时应设置用户姓名")
        void should_set_user_name_from_application() {
            RecruitApplication app = createApplication(100, "李四", "2024002", "计算机科学", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024002")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getName()).isEqualTo("李四");
        }

        @FastTest
        @DisplayName("审批通过时应设置用户邮箱")
        void should_set_user_email_from_application() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setEmail("zhangsan@example.com");
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail("zhangsan@example.com")).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("zhangsan@example.com");
        }

        @FastTest
        @DisplayName("审批通过时应设置用户手机")
        void should_set_user_phone_from_application() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setPhone("13900139000");
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).insert(userCaptor.capture());
            assertThat(userCaptor.getValue().getPhone()).isEqualTo("13900139000");
        }

        @FastTest
        @DisplayName("审批通过时应创建MemberProfile")
        void should_create_member_profile_when_approve() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(memberProfileDAO).insert(any(MemberProfile.class));
        }

        @FastTest
        @DisplayName("审批通过时应设置MemberProfile的studentId")
        void should_set_student_id_in_member_profile() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<MemberProfile> profileCaptor = ArgumentCaptor.forClass(MemberProfile.class);
            verify(memberProfileDAO).insert(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getStudentId()).isEqualTo("2024001");
        }

        @FastTest
        @DisplayName("审批通过时应设置MemberProfile的专业和年级")
        void should_set_major_and_grade_in_member_profile() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setMajor("信息安全");
            app.setGrade("2023");
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<MemberProfile> profileCaptor = ArgumentCaptor.forClass(MemberProfile.class);
            verify(memberProfileDAO).insert(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getMajor()).isEqualTo("信息安全");
            assertThat(profileCaptor.getValue().getGrade()).isEqualTo("2023");
        }

        @FastTest
        @DisplayName("审批通过时应更新申请状态为APPROVED")
        void should_update_application_status_to_approved() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<RecruitApplication> appCaptor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).update(appCaptor.capture());
            assertThat(appCaptor.getValue().getStatus()).isEqualTo(STATUS_APPROVED);
        }

        @FastTest
        @DisplayName("学号已存在时只更新申请状态")
        void should_only_update_status_when_user_exists() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO, never()).insert(any(User.class));
            verify(memberProfileDAO, never()).insert(any(MemberProfile.class));
            ArgumentCaptor<RecruitApplication> appCaptor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).update(appCaptor.capture());
            assertThat(appCaptor.getValue().getStatus()).isEqualTo(STATUS_APPROVED);
        }

        @FastTest
        @DisplayName("邮箱已被使用时应返回错误")
        void should_return_error_when_email_already_used() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setEmail("existing@example.com");
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail("existing@example.com")).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("邮箱");
            verify(userDAO, never()).insert(any(User.class));
        }

        @FastTest
        @DisplayName("申请不存在时应返回错误")
        void should_return_error_when_application_not_exists() {
            when(recruitDAO.findById(999)).thenReturn(null);

            Result result = recruitService.approveApplication(999, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("申请id为null时应返回错误")
        void should_return_error_when_id_null() {
            Result result = recruitService.approveApplication(null, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("operatorId为null时应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = recruitService.approveApplication(100, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("申请已处于APPROVED状态时应返回错误")
        void should_return_error_when_already_approved() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_APPROVED);
            when(recruitDAO.findById(100)).thenReturn(app);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("申请已处于REJECTED状态时应返回错误")
        void should_return_error_when_already_rejected() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_REJECTED);
            when(recruitDAO.findById(100)).thenReturn(app);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("用户创建失败时应返回错误")
        void should_return_error_when_user_creation_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(false);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("MemberProfile创建失败时应返回错误")
        void should_return_error_when_profile_creation_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(false);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("申请状态更新失败时应返回错误")
        void should_return_error_when_status_update_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(false);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("申请邮箱为空时应正常处理")
        void should_handle_empty_email_when_approve() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setEmail("");
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(userDAO).existsByEmail("");
            verify(userDAO).existsByEmail("");
        }

        @FastTest
        @DisplayName("申请邮箱为null时应正常处理")
        void should_handle_null_email_when_approve() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setEmail(null);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== rejectApplication 驳回申请 ====================

    @Nested
    @DisplayName("rejectApplication 驳回申请")
    class RejectApplicationTests {

        @FastTest
        @DisplayName("驳回申请成功应更新状态为REJECTED")
        void should_reject_application_successfully() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.rejectApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<RecruitApplication> captor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(STATUS_REJECTED);
        }

        @FastTest
        @DisplayName("驳回申请时id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = recruitService.rejectApplication(null, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回申请时operatorId为null应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = recruitService.rejectApplication(100, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回申请时申请不存在应返回错误")
        void should_return_error_when_application_not_exists() {
            when(recruitDAO.findById(999)).thenReturn(null);

            Result result = recruitService.rejectApplication(999, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("驳回已通过的申请应返回错误")
        void should_return_error_when_already_approved() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_APPROVED);
            when(recruitDAO.findById(100)).thenReturn(app);

            Result result = recruitService.rejectApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("驳回已拒绝的申请应返回错误")
        void should_return_error_when_already_rejected() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_REJECTED);
            when(recruitDAO.findById(100)).thenReturn(app);

            Result result = recruitService.rejectApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(false);

            Result result = recruitService.rejectApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(recruitDAO.update(any(RecruitApplication.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.rejectApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== listApplications 申请列表 ====================

    @Nested
    @DisplayName("listApplications 申请列表")
    class ListApplicationsTests {

        @FastTest
        @DisplayName("获取所有申请列表应成功")
        void should_list_all_applications_successfully() {
            RecruitApplication app1 = createApplication(1, "张三", "2024001", "软件工程", STATUS_PENDING);
            RecruitApplication app2 = createApplication(2, "李四", "2024002", "计算机科学", STATUS_APPROVED);
            when(recruitDAO.findByConditions(null, null, null, null)).thenReturn(Arrays.asList(app1, app2));

            Result result = recruitService.listApplications(null, null, null, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按年份筛选应成功")
        void should_filter_by_year() {
            RecruitApplication app = createApplication(1, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findByConditions(null, 2024, null, null)).thenReturn(Arrays.asList(app));

            Result result = recruitService.listApplications(2024, null, null, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按状态筛选应成功")
        void should_filter_by_status() {
            RecruitApplication app = createApplication(1, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findByConditions(null, null, "1", null)).thenReturn(Arrays.asList(app));

            Result result = recruitService.listApplications(null, "1", null, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按关键词搜索应成功")
        void should_filter_by_keyword() {
            RecruitApplication app = createApplication(1, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findByConditions("张三", null, null, null)).thenReturn(Arrays.asList(app));

            Result result = recruitService.listApplications(null, null, "张三", null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按多条件筛选应成功")
        void should_filter_by_multiple_conditions() {
            RecruitApplication app = createApplication(1, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findByConditions("张三", 2024, "1", null)).thenReturn(Arrays.asList(app));

            Result result = recruitService.listApplications(2024, "1", "张三", null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空列表应返回成功")
        void should_return_success_with_empty_list() {
            when(recruitDAO.findByConditions(null, null, null, null)).thenReturn(Collections.emptyList());

            Result result = recruitService.listApplications(null, null, null, null);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按round筛选应成功")
        void should_filter_by_round() {
            RecruitApplication app = createApplication(1, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findByConditions(null, null, null, 1)).thenReturn(Arrays.asList(app));

            Result result = recruitService.listApplications(null, null, null, 1);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getApplicationDetail 申请详情 ====================

    @Nested
    @DisplayName("getApplicationDetail 申请详情")
    class GetApplicationDetailTests {

        @FastTest
        @DisplayName("获取申请详情应成功")
        void should_get_application_detail_successfully() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);

            Result result = recruitService.getApplicationDetail(100);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("申请不存在应返回错误")
        void should_return_error_when_not_exists() {
            when(recruitDAO.findById(999)).thenReturn(null);

            Result result = recruitService.getApplicationDetail(999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = recruitService.getApplicationDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("应返回申请的所有字段")
        void should_return_all_fields() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            app.setGrade("2023");
            app.setPhone("13900139000");
            app.setEmail("zhangsan@example.com");
            app.setReason("热爱技术");
            when(recruitDAO.findById(100)).thenReturn(app);

            Result result = recruitService.getApplicationDetail(100);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== deleteApplication 删除申请 ====================

    @Nested
    @DisplayName("deleteApplication 删除申请")
    class DeleteApplicationTests {

        @FastTest
        @DisplayName("删除申请成功应返回成功")
        void should_delete_application_successfully() {
            when(recruitDAO.delete(100)).thenReturn(true);

            Result result = recruitService.deleteApplication(100);

            assertThat(result.isSuccess()).isTrue();
            verify(recruitDAO).delete(100);
        }

        @FastTest
        @DisplayName("id为null应返回错误")
        void should_return_error_when_id_null() {
            Result result = recruitService.deleteApplication(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除不存在的申请应返回错误")
        void should_return_error_when_not_exists() {
            when(recruitDAO.delete(999)).thenReturn(false);

            Result result = recruitService.deleteApplication(999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(recruitDAO.delete(100)).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.deleteApplication(100);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== countPending 待审核数量 ====================

    @Nested
    @DisplayName("countPending 待审核数量")
    class CountPendingTests {

        @FastTest
        @DisplayName("获取待审核数量应成功")
        void should_count_pending_successfully() {
            when(recruitDAO.countPending()).thenReturn(5);

            Result result = recruitService.countPending();

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("无待审核申请时应返回0")
        void should_return_zero_when_no_pending() {
            when(recruitDAO.countPending()).thenReturn(0);

            Result result = recruitService.countPending();

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(recruitDAO.countPending()).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.countPending();

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== findAllYears 获取所有年份 ====================

    @Nested
    @DisplayName("findAllYears 获取所有年份")
    class FindAllYearsTests {

        @FastTest
        @DisplayName("获取所有年份应成功")
        void should_find_all_years_successfully() {
            when(recruitDAO.findAllYears()).thenReturn(Arrays.asList(2024, 2023, 2022));

            Result result = recruitService.findAllYears();

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("无申请记录时应返回空列表")
        void should_return_empty_list_when_no_applications() {
            when(recruitDAO.findAllYears()).thenReturn(Collections.emptyList());

            Result result = recruitService.findAllYears();

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(recruitDAO.findAllYears()).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.findAllYears();

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== validateApplication 验证申请 ====================

    @Nested
    @DisplayName("validateApplication 验证申请")
    class ValidateApplicationTests {

        @FastTest
        @DisplayName("验证有效申请应返回成功")
        void should_validate_valid_application() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "zhangsan@example.com");

            Result result = recruitService.validateApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("姓名为空应返回错误")
        void should_return_error_when_name_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("", "2024001", "软件工程", "zhangsan@example.com");

            Result result = recruitService.validateApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("姓名");
        }

        @FastTest
        @DisplayName("学号为空应返回错误")
        void should_return_error_when_student_id_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "", "软件工程", "zhangsan@example.com");

            Result result = recruitService.validateApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("学号");
        }

        @FastTest
        @DisplayName("专业为空应返回错误")
        void should_return_error_when_major_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "", "zhangsan@example.com");

            Result result = recruitService.validateApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("专业");
        }

        @FastTest
        @DisplayName("邮箱为空应返回错误")
        void should_return_error_when_email_empty() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "");

            Result result = recruitService.validateApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("邮箱");
        }

        @FastTest
        @DisplayName("dto为null应返回错误")
        void should_return_error_when_dto_null() {
            Result result = recruitService.validateApplication(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("所有字段都为空应返回姓名错误")
        void should_return_name_error_when_all_fields_empty() {
            RecruitApplicationDTO dto = new RecruitApplicationDTO();

            Result result = recruitService.validateApplication(dto);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).contains("姓名");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("申请ID为0应返回错误")
        void should_return_error_when_id_zero() {
            Result result = recruitService.approveApplication(0, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("申请ID为负数应返回错误")
        void should_return_error_when_id_negative() {
            Result result = recruitService.approveApplication(-1, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("超长姓名应正常处理")
        void should_handle_very_long_name() {
            RecruitApplicationDTO dto = createApplicationDTO("张".repeat(500), "2024001", "软件工程", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("超长学号应正常处理")
        void should_handle_very_long_student_id() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "0".repeat(100), "软件工程", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("特殊字符姓名应正常处理")
        void should_handle_special_characters_in_name() {
            RecruitApplicationDTO dto = createApplicationDTO("张三's \"Test\" <Script>", "2024001", "软件工程", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("中文专业名称应正常处理")
        void should_handle_chinese_major() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "计算机科学与技术", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("带空格的邮箱应正常处理")
        void should_handle_email_with_spaces() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024001", "软件工程", "  test@example.com  ");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("学号包含字母应正常处理")
        void should_handle_student_id_with_letters() {
            RecruitApplicationDTO dto = createApplicationDTO("张三", "2024CS001", "软件工程", "test@example.com");
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            Result result = recruitService.submitApplication(dto);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @FastTest
        @DisplayName("数据库查询申请异常应返回错误")
        void should_return_error_when_find_by_id_fails() {
            when(recruitDAO.findById(100)).thenThrow(new RuntimeException("数据库连接失败"));

            Result result = recruitService.getApplicationDetail(100);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("数据库列表查询异常应返回错误")
        void should_return_error_when_list_query_fails() {
            when(recruitDAO.findByConditions(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.listApplications(null, null, null, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("用户查找异常应返回错误")
        void should_return_error_when_user_exists_check_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("用户插入异常应返回错误")
        void should_return_error_when_user_insert_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("档案插入异常应返回错误")
        void should_return_error_when_profile_insert_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("申请更新异常应返回错误")
        void should_return_error_when_application_update_fails() {
            RecruitApplication app = createApplication(100, "张三", "2024001", "软件工程", STATUS_PENDING);
            when(recruitDAO.findById(100)).thenReturn(app);
            when(userDAO.existsByUsername("2024001")).thenReturn(false);
            when(userDAO.existsByEmail(anyString())).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenReturn(true);
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenThrow(new RuntimeException("数据库错误"));

            Result result = recruitService.approveApplication(100, OPERATOR_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }
}
