package com.softwaregroup.hr.service;

import com.softwaregroup.hr.dao.MemberProfileDAO;
import com.softwaregroup.hr.dao.RecruitApplicationDAO;
import com.softwaregroup.hr.dao.UserDAO;
import com.softwaregroup.hr.model.entity.MemberProfile;
import com.softwaregroup.hr.model.entity.RecruitApplication;
import com.softwaregroup.hr.model.entity.User;
import com.softwaregroup.hr.model.dto.RecruitApplicationDTO;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RecruitService 单元测试
 *
 * 测试覆盖：
 * - 所有公开业务方法的正常路径
 * - 所有参数验证的边界情况
 * - 所有异常场景
 * - 所有状态枚举流转
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("招新服务测试")
class RecruitServiceTest {

    @Mock
    private RecruitApplicationDAO recruitDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private MemberProfileDAO memberProfileDAO;

    @InjectMocks
    private RecruitService recruitService;

    private RecruitApplicationDTO validDTO;
    private RecruitApplication pendingApplication;

    @BeforeEach
    void setUp() {
        validDTO = new RecruitApplicationDTO();
        validDTO.setName("张三");
        validDTO.setStudentId("2021001234");
        validDTO.setMajor("计算机科学与技术");
        validDTO.setGrade("2021");
        validDTO.setPhone("13800138000");
        validDTO.setEmail("zhangsan@example.com");
        validDTO.setReason("对软件开发有浓厚兴趣");

        pendingApplication = new RecruitApplication();
        pendingApplication.setId(1);
        pendingApplication.setName("张三");
        pendingApplication.setStudentId("2021001234");
        pendingApplication.setMajor("计算机科学与技术");
        pendingApplication.setGrade("2021");
        pendingApplication.setPhone("13800138000");
        pendingApplication.setEmail("zhangsan@example.com");
        pendingApplication.setReason("对软件开发有浓厚兴趣");
        pendingApplication.setStatus(RecruitApplication.STATUS_PENDING);
    }

    // ==================== submitApplication 测试 ====================

    @Nested
    @DisplayName("submitApplication - 提交申请")
    class SubmitApplicationTests {

        @Test
        @DisplayName("提交成功 - 当必填字段有效时")
        void should_submit_success_when_valid_input() {
            // Given
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            // When
            Result result = recruitService.submitApplication(validDTO);

            // Then
            assertTrue(result.isSuccess());
            verify(recruitDAO, times(1)).insert(any(RecruitApplication.class));
        }

        @Test
        @DisplayName("提交失败 - 当姓名为空时")
        void should_fail_when_name_is_empty() {
            // Given
            validDTO.setName("");

            // When
            Result result = recruitService.submitApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("姓名不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 当姓名为null时")
        void should_fail_when_name_is_null() {
            // Given
            validDTO.setName(null);

            // When
            Result result = recruitService.submitApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("姓名不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 当学号为空时")
        void should_fail_when_student_id_is_empty() {
            // Given
            validDTO.setStudentId("");

            // When
            Result result = recruitService.submitApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("学号不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 当专业为空时")
        void should_fail_when_major_is_empty() {
            // Given
            validDTO.setMajor("");

            // When
            Result result = recruitService.submitApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("专业不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 当邮箱为空时")
        void should_fail_when_email_is_empty() {
            // Given
            validDTO.setEmail("");

            // When
            Result result = recruitService.submitApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("邮箱不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 当DTO为null时")
        void should_fail_when_dto_is_null() {
            // When
            Result result = recruitService.submitApplication(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("验证失败", result.getMessage());
        }

        @Test
        @DisplayName("提交成功 - 设置状态为待审核")
        void should_set_status_pending_when_submit_success() {
            // Given
            ArgumentCaptor<RecruitApplication> captor = ArgumentCaptor.forClass(RecruitApplication.class);
            when(recruitDAO.insert(any(RecruitApplication.class))).thenReturn(true);

            // When
            recruitService.submitApplication(validDTO);

            // Then
            verify(recruitDAO).insert(captor.capture());
            assertEquals(RecruitApplication.STATUS_PENDING, captor.getValue().getStatus());
        }
    }

    // ==================== approveApplication 测试 ====================

    @Nested
    @DisplayName("approveApplication - 审批通过")
    class ApproveApplicationTests {

        @Test
        @DisplayName("审批成功 - 新用户创建 - 学号不存在时")
        void should_approve_and_create_user_when_student_id_not_exists() {
            // Given
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);
            when(userDAO.existsByUsername("2021001234")).thenReturn(false);
            when(userDAO.existsByEmail("zhangsan@example.com")).thenReturn(false);
            when(userDAO.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(100);
                return true;
            });
            when(memberProfileDAO.insert(any(MemberProfile.class))).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            // When
            Result result = recruitService.approveApplication(1, 999);

            // Then
            assertTrue(result.isSuccess());
            verify(userDAO).insert(any(User.class));
            verify(memberProfileDAO).insert(any(MemberProfile.class));
            verify(recruitDAO).update(any(RecruitApplication.class));
        }

        @Test
        @DisplayName("审批成功 - 老用户复审 - 学号已存在时")
        void should_approve_existing_user_when_student_id_exists() {
            // Given
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);
            when(userDAO.existsByUsername("2021001234")).thenReturn(true);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            // When
            Result result = recruitService.approveApplication(1, 999);

            // Then
            assertTrue(result.isSuccess());
            verify(userDAO, never()).insert(any(User.class));
            verify(memberProfileDAO, never()).insert(any(MemberProfile.class));
            verify(recruitDAO, times(1)).update(any(RecruitApplication.class));
        }

        @Test
        @DisplayName("审批失败 - 申请不存在时")
        void should_fail_when_application_not_found() {
            // Given
            when(recruitDAO.findById(999)).thenReturn(null);

            // When
            Result result = recruitService.approveApplication(999, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("申请不存在", result.getMessage());
        }

        @Test
        @DisplayName("审批失败 - 已通过的申请再次审批")
        void should_fail_when_application_already_approved() {
            // Given
            pendingApplication.setStatus(RecruitApplication.STATUS_APPROVED);
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);

            // When
            Result result = recruitService.approveApplication(1, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("该申请已被审批，无法重复操作", result.getMessage());
        }

        @Test
        @DisplayName("审批失败 - 申请ID为null时")
        void should_fail_when_application_id_is_null() {
            // When
            Result result = recruitService.approveApplication(null, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("申请ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("审批失败 - 操作者ID为null时")
        void should_fail_when_operator_id_is_null() {
            // When
            Result result = recruitService.approveApplication(1, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("操作者ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("审批失败 - 申请ID小于等于0时")
        void should_fail_when_application_id_invalid() {
            // When
            Result result = recruitService.approveApplication(0, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("申请ID必须大于0", result.getMessage());
        }

        @Test
        @DisplayName("审批失败 - 邮箱已被使用时")
        void should_fail_when_email_already_used() {
            // Given
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);
            when(userDAO.existsByUsername("2021001234")).thenReturn(false);
            when(userDAO.existsByEmail("zhangsan@example.com")).thenReturn(true);

            // When
            Result result = recruitService.approveApplication(1, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("该邮箱已被其他用户使用", result.getMessage());
        }
    }

    // ==================== rejectApplication 测试 ====================

    @Nested
    @DisplayName("rejectApplication - 审批拒绝")
    class RejectApplicationTests {

        @Test
        @DisplayName("拒绝成功 - 待审核状态的申请")
        void should_reject_success_when_pending() {
            // Given
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);
            when(recruitDAO.update(any(RecruitApplication.class))).thenReturn(true);

            // When
            Result result = recruitService.rejectApplication(1, 1);

            // Then
            assertTrue(result.isSuccess());
            ArgumentCaptor<RecruitApplication> captor = ArgumentCaptor.forClass(RecruitApplication.class);
            verify(recruitDAO).update(captor.capture());
            assertEquals(RecruitApplication.STATUS_REJECTED, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("拒绝失败 - 申请不存在")
        void should_fail_when_application_not_found() {
            // Given
            when(recruitDAO.findById(999)).thenReturn(null);

            // When
            Result result = recruitService.rejectApplication(999, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("申请不存在", result.getMessage());
        }

        @Test
        @DisplayName("拒绝失败 - 已通过的申请无法拒绝")
        void should_fail_when_already_approved() {
            // Given
            pendingApplication.setStatus(RecruitApplication.STATUS_APPROVED);
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);

            // When
            Result result = recruitService.rejectApplication(1, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("该申请已被审批，无法重复操作", result.getMessage());
        }

        @Test
        @DisplayName("拒绝失败 - 已拒绝的申请再次拒绝")
        void should_fail_when_already_rejected() {
            // Given
            pendingApplication.setStatus(RecruitApplication.STATUS_REJECTED);
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);

            // When
            Result result = recruitService.rejectApplication(1, 1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("该申请已被审批，无法重复操作", result.getMessage());
        }
    }

    // ==================== listApplications 测试 ====================

    @Nested
    @DisplayName("listApplications - 申请列表查询")
    class ListApplicationsTests {

        @Test
        @DisplayName("查询成功 - 返回申请列表")
        void should_return_application_list() {
            // Given
            List<RecruitApplication> applications = Arrays.asList(pendingApplication);
            when(recruitDAO.findByConditions(any(), any(), any(), any())).thenReturn(applications);

            // When
            Result result = recruitService.listApplications(null, null, null, null);

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals(1, ((List<?>) result.getData()).size());
        }

        @Test
        @DisplayName("查询成功 - 按年份筛选")
        void should_filter_by_year() {
            // Given
            when(recruitDAO.findByConditions(any(), eq(2021), any(), any())).thenReturn(Arrays.asList(pendingApplication));

            // When
            Result result = recruitService.listApplications(2021, null, null, null);

            // Then
            assertTrue(result.isSuccess());
            verify(recruitDAO).findByConditions(null, 2021, null, null);
        }

        @Test
        @DisplayName("查询成功 - 按状态筛选")
        void should_filter_by_status() {
            // Given
            when(recruitDAO.findByConditions(any(), any(), eq("1"), any())).thenReturn(Arrays.asList(pendingApplication));

            // When
            Result result = recruitService.listApplications(null, "1", null, null);

            // Then
            assertTrue(result.isSuccess());
            verify(recruitDAO).findByConditions(null, null, "1", null);
        }

        @Test
        @DisplayName("查询成功 - 按关键字筛选")
        void should_filter_by_keyword() {
            // Given
            when(recruitDAO.findByConditions(eq("张三"), any(), any(), any())).thenReturn(Arrays.asList(pendingApplication));

            // When
            Result result = recruitService.listApplications(null, null, "张三", null);

            // Then
            assertTrue(result.isSuccess());
            verify(recruitDAO).findByConditions("张三", null, null, null);
        }

        @Test
        @DisplayName("查询成功 - 返回空列表")
        void should_return_empty_list_when_no_results() {
            // Given
            when(recruitDAO.findByConditions(any(), any(), any(), any())).thenReturn(Arrays.asList());

            // When
            Result result = recruitService.listApplications(null, null, null, null);

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(((List<?>) result.getData()).isEmpty());
        }
    }

    // ==================== getApplicationDetail 测试 ====================

    @Nested
    @DisplayName("getApplicationDetail - 申请详情查询")
    class GetApplicationDetailTests {

        @Test
        @DisplayName("查询成功 - 申请存在")
        void should_return_detail_when_exists() {
            // Given
            when(recruitDAO.findById(1)).thenReturn(pendingApplication);

            // When
            Result result = recruitService.getApplicationDetail(1);

            // Then
            assertTrue(result.isSuccess());
            RecruitApplication app = (RecruitApplication) result.getData();
            assertEquals(1, app.getId());
            assertEquals("张三", app.getName());
        }

        @Test
        @DisplayName("查询失败 - 申请不存在")
        void should_return_error_when_not_found() {
            // Given
            when(recruitDAO.findById(999)).thenReturn(null);

            // When
            Result result = recruitService.getApplicationDetail(999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("申请不存在", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - ID为null")
        void should_fail_when_id_is_null() {
            // When
            Result result = recruitService.getApplicationDetail(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("申请ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - ID小于等于0")
        void should_fail_when_id_invalid() {
            // When
            Result result = recruitService.getApplicationDetail(-1);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("申请ID必须大于0", result.getMessage());
        }
    }

    // ==================== deleteApplication 测试 ====================

    @Nested
    @DisplayName("deleteApplication - 删除申请")
    class DeleteApplicationTests {

        @Test
        @DisplayName("删除成功 - 申请存在")
        void should_delete_success() {
            // Given
            when(recruitDAO.delete(1)).thenReturn(true);

            // When
            Result result = recruitService.deleteApplication(1);

            // Then
            assertTrue(result.isSuccess());
            verify(recruitDAO).delete(1);
        }

        @Test
        @DisplayName("删除失败 - 申请不存在")
        void should_fail_when_not_found() {
            // Given
            when(recruitDAO.delete(999)).thenReturn(false);

            // When
            Result result = recruitService.deleteApplication(999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("申请不存在", result.getMessage());
        }

        @Test
        @DisplayName("删除失败 - ID为null")
        void should_fail_when_id_is_null() {
            // When
            Result result = recruitService.deleteApplication(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("申请ID不能为空", result.getMessage());
        }
    }

    // ==================== countPending 测试 ====================

    @Nested
    @DisplayName("countPending - 待审核数量统计")
    class CountPendingTests {

        @Test
        @DisplayName("统计成功 - 返回待审核数量")
        void should_return_pending_count() {
            // Given
            when(recruitDAO.countPending()).thenReturn(10);

            // When
            Result result = recruitService.countPending();

            // Then
            assertTrue(result.isSuccess());
            assertEquals(10, result.getData());
        }

        @Test
        @DisplayName("统计成功 - 返回0当没有待审核")
        void should_return_zero_when_no_pending() {
            // Given
            when(recruitDAO.countPending()).thenReturn(0);

            // When
            Result result = recruitService.countPending();

            // Then
            assertTrue(result.isSuccess());
            assertEquals(0, result.getData());
        }
    }

    // ==================== findAllYears 测试 ====================

    @Nested
    @DisplayName("findAllYears - 获取所有招新年份")
    class FindAllYearsTests {

        @Test
        @DisplayName("查询成功 - 返回年份列表")
        void should_return_years_list() {
            // Given
            List<Integer> years = Arrays.asList(2021, 2022, 2023);
            when(recruitDAO.findAllYears()).thenReturn(years);

            // When
            Result result = recruitService.findAllYears();

            // Then
            assertTrue(result.isSuccess());
            assertEquals(years, result.getData());
        }

        @Test
        @DisplayName("查询成功 - 返回空列表当无数据")
        void should_return_empty_list_when_no_data() {
            // Given
            when(recruitDAO.findAllYears()).thenReturn(Arrays.asList());

            // When
            Result result = recruitService.findAllYears();

            // Then
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(((List<?>) result.getData()).isEmpty());
        }
    }

    // ==================== validateApplication 测试 ====================

    @Nested
    @DisplayName("validateApplication - 申请信息验证")
    class ValidateApplicationTests {

        @Test
        @DisplayName("验证通过 - 所有必填字段有效")
        void should_pass_when_all_required_fields_valid() {
            // When
            Result result = recruitService.validateApplication(validDTO);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("验证失败 - DTO为null")
        void should_fail_when_dto_null() {
            // When
            Result result = recruitService.validateApplication(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("申请信息不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证失败 - 姓名空白")
        void should_fail_when_name_blank() {
            // Given
            validDTO.setName("   ");

            // When
            Result result = recruitService.validateApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("姓名不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证失败 - 学号空白")
        void should_fail_when_student_id_blank() {
            // Given
            validDTO.setStudentId("   ");

            // When
            Result result = recruitService.validateApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("学号不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证失败 - 专业空白")
        void should_fail_when_major_blank() {
            // Given
            validDTO.setMajor("   ");

            // When
            Result result = recruitService.validateApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("专业不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证失败 - 邮箱空白")
        void should_fail_when_email_blank() {
            // Given
            validDTO.setEmail("   ");

            // When
            Result result = recruitService.validateApplication(validDTO);

            // Then
            assertFalse(result.isSuccess());
            assertEquals("邮箱不能为空", result.getMessage());
        }

        @Test
        @DisplayName("验证通过 - 可选字段可为空")
        void should_pass_when_optional_fields_empty() {
            // Given
            validDTO.setGrade(null);
            validDTO.setPhone(null);
            validDTO.setReason(null);

            // When
            Result result = recruitService.validateApplication(validDTO);

            // Then
            assertTrue(result.isSuccess());
        }
    }

    // ==================== 状态常量测试 ====================

    @Nested
    @DisplayName("状态常量验证")
    class StatusConstantsTests {

        @Test
        @DisplayName("待审核状态常量值正确")
        void should_have_correct_pending_status_value() {
            assertEquals(1, RecruitApplication.STATUS_PENDING);
        }

        @Test
        @DisplayName("已通过状态常量值正确")
        void should_have_correct_approved_status_value() {
            assertEquals(2, RecruitApplication.STATUS_APPROVED);
        }

        @Test
        @DisplayName("已拒绝状态常量值正确")
        void should_have_correct_rejected_status_value() {
            assertEquals(0, RecruitApplication.STATUS_REJECTED);
        }
    }
}
