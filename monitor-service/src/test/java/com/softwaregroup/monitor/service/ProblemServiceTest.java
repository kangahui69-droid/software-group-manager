package com.softwaregroup.monitor.service;

import com.softwaregroup.monitor.dao.ProblemReportDAO;
import com.softwaregroup.monitor.dao.UserDAO;
import com.softwaregroup.monitor.model.ProblemReport;
import com.softwaregroup.monitor.model.User;
import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.model.dto.ProblemFilterDTO;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProblemService 单元测试
 *
 * 测试覆盖：
 * - 问题提交、查询、更新、删除
 * - 状态和分类的完整枚举流转
 * - 所有边界情况
 * - 所有异常场景
 * - 权限验证
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("问题反馈服务测试")
class ProblemServiceTest {

    @Mock
    private ProblemReportDAO problemReportDAO;

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private ProblemService problemService;

    private ProblemDTO validProblemDTO;
    private ProblemReport existingProblem;
    private Integer operatorId = 1;
    private Integer problemId = 100;

    @BeforeEach
    void setUp() {
        validProblemDTO = new ProblemDTO();
        validProblemDTO.setTitle("无法登录系统");
        validProblemDTO.setContent("尝试多次登录都失败");
        validProblemDTO.setReporterName("张三");
        validProblemDTO.setReporterContact("13800138000");

        existingProblem = new ProblemReport();
        existingProblem.setId(problemId);
        existingProblem.setTitle("无法登录系统");
        existingProblem.setContent("尝试多次登录都失败");
        existingProblem.setCategory(ProblemReport.CATEGORY_UNVERIFIED);
        existingProblem.setStatus(ProblemReport.STATUS_PENDING);
        existingProblem.setReporterType(ProblemReport.REPORTER_TYPE_GUEST);
    }

    // ==================== submitProblem 测试 ====================

    @Nested
    @DisplayName("submitProblem - 提交问题")
    class SubmitProblemTests {

        @Test
        @DisplayName("提交成功 - 游客提交")
        void should_submit_success_as_guest() {
            // Given
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertTrue(result.isSuccess());
            ArgumentCaptor<ProblemReport> captor = ArgumentCaptor.forClass(ProblemReport.class);
            verify(problemReportDAO).insert(captor.capture());
            assertEquals(ProblemReport.CATEGORY_UNVERIFIED, captor.getValue().getCategory());
            assertEquals(ProblemReport.STATUS_PENDING, captor.getValue().getStatus());
            assertEquals(ProblemReport.REPORTER_TYPE_GUEST, captor.getValue().getReporterType());
        }

        @Test
        @DisplayName("提交成功 - 成员提交")
        void should_submit_success_as_member() {
            // Given
            User member = new User();
            member.setId(10);
            member.setRole("MEMBER");
            when(userDAO.findById(10)).thenReturn(member);
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

            // When
            Result result = problemService.submitProblem(validProblemDTO, 10);

            // Then
            assertTrue(result.isSuccess());
            ArgumentCaptor<ProblemReport> captor = ArgumentCaptor.forClass(ProblemReport.class);
            verify(problemReportDAO).insert(captor.capture());
            assertEquals(ProblemReport.REPORTER_TYPE_MEMBER, captor.getValue().getReporterType());
        }

        @Test
        @DisplayName("提交成功 - 管理员提交")
        void should_submit_success_as_admin() {
            // Given
            User admin = new User();
            admin.setId(1);
            admin.setRole("ADMIN");
            when(userDAO.findById(1)).thenReturn(admin);
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

            // When
            Result result = problemService.submitProblem(validProblemDTO, 1);

            // Then
            assertTrue(result.isSuccess());
            ArgumentCaptor<ProblemReport> captor = ArgumentCaptor.forClass(ProblemReport.class);
            verify(problemReportDAO).insert(captor.capture());
            assertEquals(ProblemReport.REPORTER_TYPE_ADMIN, captor.getValue().getReporterType());
        }

        @Test
        @DisplayName("提交失败 - DTO为null")
        void should_fail_when_dto_is_null() {
            // When
            Result result = problemService.submitProblem(null, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("问题信息不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 标题为空")
        void should_fail_when_title_is_empty() {
            // Given
            validProblemDTO.setTitle("");

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("标题不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 标题为null")
        void should_fail_when_title_is_null() {
            // Given
            validProblemDTO.setTitle(null);

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("标题不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 标题超过256字符")
        void should_fail_when_title_too_long() {
            // Given
            validProblemDTO.setTitle("a".repeat(257));

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("标题不能超过256个字符", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 内容为空")
        void should_fail_when_content_is_empty() {
            // Given
            validProblemDTO.setContent("");

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("内容不能为空", result.getMessage());
        }

        @Test
        @DisplayName("提交失败 - 内容超过5000字符")
        void should_fail_when_content_too_long() {
            // Given
            validProblemDTO.setContent("a".repeat(5001));

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("内容不能超过5000个字符", result.getMessage());
        }

        @Test
        @DisplayName("提交成功 - 标题正好256字符")
        void should_pass_when_title_256_chars() {
            // Given
            validProblemDTO.setTitle("a".repeat(256));
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("提交成功 - 内容正好5000字符")
        void should_pass_when_content_5000_chars() {
            // Given
            validProblemDTO.setContent("a".repeat(5000));
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

            // When
            Result result = problemService.submitProblem(validProblemDTO, null);

            // Then
            assertTrue(result.isSuccess());
        }
    }

    // ==================== getProblemDetail 测试 ====================

    @Nested
    @DisplayName("getProblemDetail - 获取问题详情")
    class GetProblemDetailTests {

        @Test
        @DisplayName("查询成功 - 问题存在")
        void should_return_problem_when_exists() {
            // Given
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);

            // When
            Result result = problemService.getProblemDetail(problemId);

            // Then
            assertTrue(result.isSuccess());
            ProblemReport problem = (ProblemReport) result.getData();
            assertEquals(problemId, problem.getId());
        }

        @Test
        @DisplayName("查询失败 - 问题不存在")
        void should_return_error_when_not_found() {
            // Given
            when(problemReportDAO.findById(999)).thenReturn(null);

            // When
            Result result = problemService.getProblemDetail(999);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("问题不存在", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - ID为null")
        void should_fail_when_id_is_null() {
            // When
            Result result = problemService.getProblemDetail(null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("问题ID不能为空", result.getMessage());
        }
    }

    // ==================== listProblems 测试 ====================

    @Nested
    @DisplayName("listProblems - 问题列表查询")
    class ListProblemsTests {

        @Test
        @DisplayName("查询成功 - 无筛选条件")
        void should_return_all_problems_when_no_filter() {
            // Given
            List<ProblemReport> problems = Arrays.asList(existingProblem);
            when(problemReportDAO.findAll()).thenReturn(problems);

            // When
            Result result = problemService.listProblems(null, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            verify(problemReportDAO).findAll();
        }

        @Test
        @DisplayName("查询成功 - 按分类筛选")
        void should_filter_by_category() {
            // Given
            List<ProblemReport> problems = Arrays.asList(existingProblem);
            when(problemReportDAO.findByCategory(ProblemReport.CATEGORY_VERIFIED)).thenReturn(problems);

            ProblemFilterDTO filter = new ProblemFilterDTO();
            filter.setCategory(ProblemReport.CATEGORY_VERIFIED);

            // When
            Result result = problemService.listProblems(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            verify(problemReportDAO).findByCategory(ProblemReport.CATEGORY_VERIFIED);
        }

        @Test
        @DisplayName("查询成功 - 按状态筛选")
        void should_filter_by_status() {
            // Given
            List<ProblemReport> problems = Arrays.asList(existingProblem);
            when(problemReportDAO.findByStatus(ProblemReport.STATUS_PENDING)).thenReturn(problems);

            ProblemFilterDTO filter = new ProblemFilterDTO();
            filter.setStatus(ProblemReport.STATUS_PENDING);

            // When
            Result result = problemService.listProblems(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            verify(problemReportDAO).findByStatus(ProblemReport.STATUS_PENDING);
        }

        @Test
        @DisplayName("查询成功 - 空Filter对象")
        void should_return_all_when_empty_filter() {
            // Given
            List<ProblemReport> problems = Arrays.asList(existingProblem);
            when(problemReportDAO.findAll()).thenReturn(problems);

            ProblemFilterDTO filter = new ProblemFilterDTO();

            // When
            Result result = problemService.listProblems(filter, 1, 20);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("查询失败 - 页码小于等于0")
        void should_fail_when_page_invalid() {
            // When
            Result result = problemService.listProblems(null, 0, 20);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("页码必须大于0", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - pageSize小于等于0")
        void should_fail_when_pageSize_invalid() {
            // When
            Result result = problemService.listProblems(null, 1, 0);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("每页数量必须在1-100之间", result.getMessage());
        }

        @Test
        @DisplayName("查询失败 - pageSize超过100")
        void should_fail_when_pageSize_exceeds_max() {
            // When
            Result result = problemService.listProblems(null, 1, 101);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("每页数量必须在1-100之间", result.getMessage());
        }
    }

    // ==================== getMyProblems 测试 ====================

    @Nested
    @DisplayName("getMyProblems - 获取我的问题")
    class GetMyProblemsTests {

        @Test
        @DisplayName("查询成功 - 返回用户的问题列表")
        void should_return_user_problems() {
            // Given
            List<ProblemReport> problems = Arrays.asList(existingProblem);
            when(problemReportDAO.findByUserId(10)).thenReturn(problems);

            // When
            Result result = problemService.getMyProblems(10, 1, 20);

            // Then
            assertTrue(result.isSuccess());
            verify(problemReportDAO).findByUserId(10);
        }

        @Test
        @DisplayName("查询失败 - 用户ID为null")
        void should_fail_when_user_id_null() {
            // When
            Result result = problemService.getMyProblems(null, 1, 20);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("用户ID不能为空", result.getMessage());
        }
    }

    // ==================== updateProblem 测试 ====================

    @Nested
    @DisplayName("updateProblem - 更新问题")
    class UpdateProblemTests {

        @Test
        @DisplayName("更新成功")
        void should_update_success() {
            // Given
            ProblemDTO updateDTO = new ProblemDTO();
            updateDTO.setTitle("更新后的标题");
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);
            when(problemReportDAO.updateCategoryAndStatus(
                eq(problemId), any(), any(), any(), any())).thenReturn(true);

            // When
            Result result = problemService.updateProblem(problemId, updateDTO, operatorId);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("更新失败 - 问题不存在")
        void should_fail_when_problem_not_found() {
            // Given
            ProblemDTO updateDTO = new ProblemDTO();
            updateDTO.setTitle("新标题");
            when(problemReportDAO.findById(999)).thenReturn(null);

            // When
            Result result = problemService.updateProblem(999, updateDTO, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("问题不存在", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - ID为null")
        void should_fail_when_id_null() {
            // Given
            ProblemDTO updateDTO = new ProblemDTO();

            // When
            Result result = problemService.updateProblem(null, updateDTO, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
        }

        @Test
        @DisplayName("更新失败 - DTO为null")
        void should_fail_when_dto_null() {
            // When
            Result result = problemService.updateProblem(problemId, null, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("问题信息不能为空", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 操作者ID为null")
        void should_fail_when_operator_id_null() {
            // Given
            ProblemDTO updateDTO = new ProblemDTO();
            updateDTO.setTitle("新标题");

            // When
            Result result = problemService.updateProblem(problemId, updateDTO, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("操作者ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 标题为空")
        void should_fail_when_title_empty() {
            // Given
            ProblemDTO updateDTO = new ProblemDTO();
            updateDTO.setTitle("");

            // When
            Result result = problemService.updateProblem(problemId, updateDTO, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("标题不能为空", result.getMessage());
        }
    }

    // ==================== updateStatus 测试 ====================

    @Nested
    @DisplayName("updateStatus - 更新状态")
    class UpdateStatusTests {

        @Test
        @DisplayName("更新成功 - 属实的分类可更新状态")
        void should_update_status_success() {
            // Given
            existingProblem.setCategory(ProblemReport.CATEGORY_VERIFIED);
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);
            when(problemReportDAO.updateCategoryAndStatus(
                eq(problemId), eq(ProblemReport.CATEGORY_VERIFIED),
                eq(ProblemReport.STATUS_SOLVED), any(), eq(operatorId))).thenReturn(true);

            // When
            Result result = problemService.updateStatus(
                problemId, ProblemReport.STATUS_SOLVED, "已修复", operatorId);

            // Then
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("更新失败 - 非属实的分类不能更新状态")
        void should_fail_when_category_not_verified() {
            // Given
            existingProblem.setCategory(ProblemReport.CATEGORY_UNVERIFIED);
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);

            // When
            Result result = problemService.updateStatus(
                problemId, ProblemReport.STATUS_SOLVED, "已修复", operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("只有属实的分类才能更新状态", result.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"PENDING", "SOLVING", "SOLVED", "UNSOLVED"})
        @DisplayName("更新成功 - 所有有效状态值")
        void should_accept_all_valid_statuses(String status) {
            // Given
            existingProblem.setCategory(ProblemReport.CATEGORY_VERIFIED);
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);
            when(problemReportDAO.updateCategoryAndStatus(
                eq(problemId), any(), eq(status), any(), any())).thenReturn(true);

            // When
            Result result = problemService.updateStatus(problemId, status, null, operatorId);

            // Then
            assertTrue(result.isSuccess());
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "pending", "Solved", ""})
        @DisplayName("更新失败 - 无效状态值")
        void should_reject_invalid_statuses(String status) {
            // When
            Result result = problemService.updateStatus(problemId, status, null, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("无效的状态值", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 问题不存在")
        void should_fail_when_problem_not_found() {
            // Given
            when(problemReportDAO.findById(999)).thenReturn(null);

            // When
            Result result = problemService.updateStatus(
                999, ProblemReport.STATUS_SOLVED, null, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("问题不存在", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - ID为null")
        void should_fail_when_id_null() {
            // When
            Result result = problemService.updateStatus(
                null, ProblemReport.STATUS_SOLVED, null, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("问题ID不能为空", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 操作者ID为null")
        void should_fail_when_operator_id_null() {
            // When
            Result result = problemService.updateStatus(
                problemId, ProblemReport.STATUS_SOLVED, null, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("操作者ID不能为空", result.getMessage());
        }
    }

    // ==================== updateCategory 测试 ====================

    @Nested
    @DisplayName("updateCategory - 更新分类")
    class UpdateCategoryTests {

        @ParameterizedTest
        @ValueSource(strings = {"VERIFIED", "UNVERIFIED", "INVALID"})
        @DisplayName("更新成功 - 所有有效分类值")
        void should_accept_all_valid_categories(String category) {
            // Given
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);
            when(problemReportDAO.updateCategoryAndStatus(
                eq(problemId), eq(category), eq(ProblemReport.STATUS_PENDING), any(), any()))
                .thenReturn(true);

            // When
            Result result = problemService.updateCategory(problemId, category, operatorId);

            // Then
            assertTrue(result.isSuccess());
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", "VERIFIEDD", ""})
        @DisplayName("更新失败 - 无效分类值")
        void should_reject_invalid_categories(String category) {
            // When
            Result result = problemService.updateCategory(problemId, category, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("无效的分类值", result.getMessage());
        }

        @Test
        @DisplayName("更新失败 - 问题不存在")
        void should_fail_when_problem_not_found() {
            // Given
            when(problemReportDAO.findById(999)).thenReturn(null);

            // When
            Result result = problemService.updateCategory(
                999, ProblemReport.CATEGORY_VERIFIED, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("问题不存在", result.getMessage());
        }
    }

    // ==================== addComment 测试 ====================

    @Nested
    @DisplayName("addComment - 添加备注")
    class AddCommentTests {

        @Test
        @DisplayName("添加成功")
        void should_add_comment_success() {
            // Given
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);
            when(problemReportDAO.updateAdminComment(problemId, "处理中")).thenReturn(true);

            // When
            Result result = problemService.addComment(problemId, "处理中", operatorId);

            // Then
            assertTrue(result.isSuccess());
            verify(problemReportDAO).updateAdminComment(problemId, "处理中");
        }

        @Test
        @DisplayName("添加失败 - 问题不存在")
        void should_fail_when_problem_not_found() {
            // Given
            when(problemReportDAO.findById(999)).thenReturn(null);

            // When
            Result result = problemService.addComment(999, "备注", operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("问题不存在", result.getMessage());
        }

        @Test
        @DisplayName("添加失败 - ID为null")
        void should_fail_when_id_null() {
            // When
            Result result = problemService.addComment(null, "备注", operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
        }
    }

    // ==================== deleteProblem 测试 ====================

    @Nested
    @DisplayName("deleteProblem - 删除问题")
    class DeleteProblemTests {

        @Test
        @DisplayName("删除成功")
        void should_delete_success() {
            // Given
            when(problemReportDAO.findById(problemId)).thenReturn(existingProblem);
            when(problemReportDAO.delete(problemId)).thenReturn(true);

            // When
            Result result = problemService.deleteProblem(problemId, operatorId);

            // Then
            assertTrue(result.isSuccess());
            verify(problemReportDAO).delete(problemId);
        }

        @Test
        @DisplayName("删除失败 - 问题不存在")
        void should_fail_when_problem_not_found() {
            // Given
            when(problemReportDAO.findById(999)).thenReturn(null);

            // When
            Result result = problemService.deleteProblem(999, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, result.getCode());
            assertEquals("问题不存在", result.getMessage());
        }

        @Test
        @DisplayName("删除失败 - ID为null")
        void should_fail_when_id_null() {
            // When
            Result result = problemService.deleteProblem(null, operatorId);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
        }

        @Test
        @DisplayName("删除失败 - 操作者ID为null")
        void should_fail_when_operator_id_null() {
            // When
            Result result = problemService.deleteProblem(problemId, null);

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, result.getCode());
            assertEquals("操作者ID不能为空", result.getMessage());
        }
    }

    // ==================== getStatistics 测试 ====================

    @Nested
    @DisplayName("getStatistics - 统计数据")
    class GetStatisticsTests {

        @Test
        @DisplayName("统计成功 - 返回各类数量")
        void should_return_statistics() {
            // Given
            when(problemReportDAO.countPending()).thenReturn(10);
            when(problemReportDAO.countByCategory(ProblemReport.CATEGORY_VERIFIED)).thenReturn(5);
            when(problemReportDAO.countByCategory(ProblemReport.CATEGORY_UNVERIFIED)).thenReturn(3);
            when(problemReportDAO.countByCategory(ProblemReport.CATEGORY_INVALID)).thenReturn(2);
            when(problemReportDAO.countByStatus(ProblemReport.STATUS_SOLVED)).thenReturn(4);

            // When
            Result result = problemService.getStatistics();

            // Then
            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) result.getData();
            assertEquals(10, stats.get("pending"));
            assertEquals(5, stats.get("verified"));
            assertEquals(3, stats.get("unverified"));
            assertEquals(2, stats.get("invalid"));
            assertEquals(4, stats.get("solved"));
        }
    }

    // ==================== 分类常量测试 ====================

    @Nested
    @DisplayName("分类常量验证")
    class CategoryConstantsTests {

        @Test
        @DisplayName("所有分类常量值正确")
        void should_have_correct_category_constants() {
            assertEquals("VERIFIED", ProblemReport.CATEGORY_VERIFIED);
            assertEquals("UNVERIFIED", ProblemReport.CATEGORY_UNVERIFIED);
            assertEquals("INVALID", ProblemReport.CATEGORY_INVALID);
        }
    }

    // ==================== 状态常量测试 ====================

    @Nested
    @DisplayName("状态常量验证")
    class StatusConstantsTests {

        @Test
        @DisplayName("所有状态常量值正确")
        void should_have_correct_status_constants() {
            assertEquals("PENDING", ProblemReport.STATUS_PENDING);
            assertEquals("SOLVING", ProblemReport.STATUS_SOLVING);
            assertEquals("SOLVED", ProblemReport.STATUS_SOLVED);
            assertEquals("UNSOLVED", ProblemReport.STATUS_UNSOLVED);
        }
    }

    // ==================== 报告者类型常量测试 ====================

    @Nested
    @DisplayName("报告者类型常量验证")
    class ReporterTypeConstantsTests {

        @Test
        @DisplayName("所有报告者类型常量值正确")
        void should_have_correct_reporter_type_constants() {
            assertEquals("GUEST", ProblemReport.REPORTER_TYPE_GUEST);
            assertEquals("MEMBER", ProblemReport.REPORTER_TYPE_MEMBER);
            assertEquals("ADMIN", ProblemReport.REPORTER_TYPE_ADMIN);
        }
    }
}
