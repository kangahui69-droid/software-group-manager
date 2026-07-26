package service;

import dao.ProblemReportDAO;
import dao.UserDAO;
import dto.ProblemDTO;
import dto.ProblemFilterDTO;
import model.ProblemReport;
import model.User;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProblemService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.2 ProblemService 问题服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - ProblemReportDAO: findAll() / findById(id) / findByUserId(userId)
 * - ProblemReportDAO: findByStatus(status) / findByCategory(category)
 * - ProblemReportDAO: insert(ProblemReport) / updateCategoryAndStatus(...)
 * - ProblemReportDAO: updateAdminComment(id, comment) / delete(id)
 * - ProblemReportDAO: countPending() / countByCategory(category) / countByStatus(status)
 * - UserDAO: findById(id)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProblemService 问题服务测试")
class ProblemServiceTest {

    @Mock
    private ProblemReportDAO problemReportDAO;

    @Mock
    private UserDAO userDAO;

    private ProblemService problemService;

    @BeforeEach
    void setUp() {
        problemService = new ProblemService(problemReportDAO, userDAO);
        when(userDAO.findById(anyInt())).thenReturn(createUser(1, "admin", "ADMIN"));
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer GUEST_USER_ID = null;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer PROBLEM_ID = 100;
    private static final Integer NONEXISTENT_PROBLEM_ID = 99999;

    // 分类常量
    private static final String CATEGORY_VERIFIED = ProblemDTO.CATEGORY_VERIFIED;
    private static final String CATEGORY_UNVERIFIED = ProblemDTO.CATEGORY_UNVERIFIED;
    private static final String CATEGORY_INVALID = ProblemDTO.CATEGORY_INVALID;

    // 状态常量
    private static final String STATUS_PENDING = ProblemDTO.STATUS_PENDING;
    private static final String STATUS_SOLVING = ProblemDTO.STATUS_SOLVING;
    private static final String STATUS_SOLVED = ProblemDTO.STATUS_SOLVED;
    private static final String STATUS_UNSOLVED = ProblemDTO.STATUS_UNSOLVED;

    // 报告者类型常量
    private static final String REPORTER_TYPE_ADMIN = ProblemDTO.REPORTER_TYPE_ADMIN;
    private static final String REPORTER_TYPE_MEMBER = ProblemDTO.REPORTER_TYPE_MEMBER;
    private static final String REPORTER_TYPE_GUEST = ProblemDTO.REPORTER_TYPE_GUEST;

    // ==================== 测试初始化辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private ProblemReport createProblem(Integer id, String title, String category, String status) {
        ProblemReport problem = new ProblemReport();
        problem.setId(id);
        problem.setTitle(title);
        problem.setContent("问题内容" + id);
        problem.setReporterName(" reporter");
        problem.setReporterType(REPORTER_TYPE_MEMBER);
        problem.setUserId(MEMBER_USER_ID);
        problem.setCategory(category);
        problem.setStatus(status);
        problem.setCreatedAt(new Date());
        return problem;
    }

    private ProblemDTO createProblemDTO(String title) {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle(title);
        dto.setContent("问题内容");
        dto.setReporterName(" reporter");
        dto.setReporterContact("contact@example.com");
        dto.setCategory(CATEGORY_UNVERIFIED);
        dto.setStatus(STATUS_PENDING);
        return dto;
    }

    // ==================== submitProblem 提交问题 ====================

    @Nested
    @DisplayName("submitProblem 提交问题")
    class SubmitProblemTests {

        @FastTest
        @DisplayName("提交问题成功应返回成功")
        void should_submit_problem_successfully() {
            ProblemDTO dto = createProblemDTO("测试问题");
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(PROBLEM_ID);

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(PROBLEM_ID);
            verify(problemReportDAO).insert(any(ProblemReport.class));
        }

        @FastTest
        @DisplayName("游客提交问题成功应设置reporterType为GUEST")
        void should_submit_problem_as_guest_successfully() {
            ProblemDTO dto = createProblemDTO("游客问题");
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(PROBLEM_ID);

            Result result = problemService.submitProblem(dto, GUEST_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(problemReportDAO).insert(argThat(report ->
                REPORTER_TYPE_GUEST.equals(report.getReporterType()) &&
                report.getUserId() == null
            ));
        }

        @FastTest
        @DisplayName("成员提交问题应设置reporterType为MEMBER")
        void should_submit_problem_as_member() {
            ProblemDTO dto = createProblemDTO("成员问题");
            when(userDAO.findById(MEMBER_USER_ID)).thenReturn(createUser(MEMBER_USER_ID, "member", "MEMBER"));
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(PROBLEM_ID);

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(problemReportDAO).insert(argThat(report ->
                REPORTER_TYPE_MEMBER.equals(report.getReporterType()) &&
                MEMBER_USER_ID.equals(report.getUserId())
            ));
        }

        @FastTest
        @DisplayName("管理员提交问题应设置reporterType为ADMIN")
        void should_submit_problem_as_admin() {
            ProblemDTO dto = createProblemDTO("管理员问题");
            when(userDAO.findById(ADMIN_USER_ID)).thenReturn(createUser(ADMIN_USER_ID, "admin", "ADMIN"));
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(PROBLEM_ID);

            Result result = problemService.submitProblem(dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(problemReportDAO).insert(argThat(report ->
                REPORTER_TYPE_ADMIN.equals(report.getReporterType())
            ));
        }

        @FastTest
        @DisplayName("标题为空应返回错误")
        void should_return_error_when_title_empty() {
            ProblemDTO dto = createProblemDTO("");
            dto.setTitle(null);

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("标题");
        }

        @FastTest
        @DisplayName("内容为空应返回错误")
        void should_return_error_when_content_empty() {
            ProblemDTO dto = createProblemDTO("测试问题");
            dto.setContent(null);

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("内容");
        }

        @FastTest
        @DisplayName("标题过长应返回错误")
        void should_return_error_when_title_too_long() {
            ProblemDTO dto = createProblemDTO("a".repeat(257));

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("标题");
        }

        @FastTest
        @DisplayName("内容过长应返回错误")
        void should_return_error_when_content_too_long() {
            ProblemDTO dto = createProblemDTO("测试问题");
            dto.setContent("a".repeat(5001));

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("内容");
        }

        @FastTest
        @DisplayName("数据库插入失败应返回错误")
        void should_return_error_when_insert_fails() {
            ProblemDTO dto = createProblemDTO("测试问题");
            when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(-1);

            Result result = problemService.submitProblem(dto, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getProblemDetail 问题详情 ====================

    @Nested
    @DisplayName("getProblemDetail 问题详情")
    class GetProblemDetailTests {

        @FastTest
        @DisplayName("获取问题详情成功应返回成功")
        void should_get_problem_detail_successfully() {
            ProblemReport problem = createProblem(PROBLEM_ID, "测试问题", CATEGORY_UNVERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(problem);

            Result result = problemService.getProblemDetail(PROBLEM_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(problem);
        }

        @FastTest
        @DisplayName("问题不存在应返回404")
        void should_return_404_when_problem_not_found() {
            when(problemReportDAO.findById(NONEXISTENT_PROBLEM_ID)).thenReturn(null);

            Result result = problemService.getProblemDetail(NONEXISTENT_PROBLEM_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("ID为空应返回400")
        void should_return_400_when_id_null() {
            Result result = problemService.getProblemDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== listProblems 问题列表 ====================

    @Nested
    @DisplayName("listProblems 问题列表")
    class ListProblemsTests {

        @FastTest
        @DisplayName("获取问题列表成功应返回成功")
        void should_list_problems_successfully() {
            List<ProblemReport> problems = Arrays.asList(
                createProblem(1, "问题1", CATEGORY_UNVERIFIED, STATUS_PENDING),
                createProblem(2, "问题2", CATEGORY_VERIFIED, STATUS_SOLVING)
            );
            when(problemReportDAO.findAll()).thenReturn(problems);

            Result result = problemService.listProblems(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(problems);
        }

        @FastTest
        @DisplayName("按分类筛选应返回对应问题")
        void should_filter_by_category() {
            List<ProblemReport> problems = Arrays.asList(
                createProblem(1, "问题1", CATEGORY_VERIFIED, STATUS_PENDING)
            );
            when(problemReportDAO.findByCategory(CATEGORY_VERIFIED)).thenReturn(problems);

            ProblemFilterDTO filter = new ProblemFilterDTO();
            filter.setCategory(CATEGORY_VERIFIED);
            Result result = problemService.listProblems(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(problems);
        }

        @FastTest
        @DisplayName("按状态筛选应返回对应问题")
        void should_filter_by_status() {
            List<ProblemReport> problems = Arrays.asList(
                createProblem(1, "问题1", CATEGORY_UNVERIFIED, STATUS_PENDING)
            );
            when(problemReportDAO.findByStatus(STATUS_PENDING)).thenReturn(problems);

            ProblemFilterDTO filter = new ProblemFilterDTO();
            filter.setStatus(STATUS_PENDING);
            Result result = problemService.listProblems(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(problems);
        }

        @FastTest
        @DisplayName("空列表应返回空数组")
        void should_return_empty_list() {
            when(problemReportDAO.findAll()).thenReturn(Arrays.asList());

            Result result = problemService.listProblems(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("页码为0应返回错误")
        void should_return_error_when_page_zero() {
            Result result = problemService.listProblems(null, 0, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("页码为负数应返回错误")
        void should_return_error_when_page_negative() {
            Result result = problemService.listProblems(null, -1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("每页数量超限应返回错误")
        void should_return_error_when_page_size_too_large() {
            Result result = problemService.listProblems(null, 1, 101);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getMyProblems 我的问题列表 ====================

    @Nested
    @DisplayName("getMyProblems 我的问题列表")
    class GetMyProblemsTests {

        @FastTest
        @DisplayName("获取我的问题列表成功应返回成功")
        void should_get_my_problems_successfully() {
            List<ProblemReport> problems = Arrays.asList(
                createProblem(1, "我的问题1", CATEGORY_UNVERIFIED, STATUS_PENDING),
                createProblem(2, "我的问题2", CATEGORY_VERIFIED, STATUS_SOLVED)
            );
            when(problemReportDAO.findByUserId(MEMBER_USER_ID)).thenReturn(problems);

            Result result = problemService.getMyProblems(MEMBER_USER_ID, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(problems);
        }

        @FastTest
        @DisplayName("用户没有问题应返回空数组")
        void should_return_empty_when_no_problems() {
            when(problemReportDAO.findByUserId(MEMBER_USER_ID)).thenReturn(Arrays.asList());

            Result result = problemService.getMyProblems(MEMBER_USER_ID, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为空应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = problemService.getMyProblems(null, 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateProblem 更新问题 ====================

    @Nested
    @DisplayName("updateProblem 更新问题")
    class UpdateProblemTests {

        @FastTest
        @DisplayName("更新问题成功应返回成功")
        void should_update_problem_successfully() {
            ProblemReport existing = createProblem(PROBLEM_ID, "原标题", CATEGORY_VERIFIED, STATUS_SOLVING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);
            when(problemReportDAO.updateCategoryAndStatus(eq(PROBLEM_ID), anyString(), anyString(), any(), any())).thenReturn(true);

            ProblemDTO dto = createProblemDTO("新标题");
            dto.setCategory(CATEGORY_VERIFIED);
            dto.setStatus(STATUS_SOLVED);
            Result result = problemService.updateProblem(PROBLEM_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("问题不存在应返回404")
        void should_return_404_when_problem_not_found() {
            when(problemReportDAO.findById(NONEXISTENT_PROBLEM_ID)).thenReturn(null);

            ProblemDTO dto = createProblemDTO("新标题");
            Result result = problemService.updateProblem(NONEXISTENT_PROBLEM_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("标题为空应返回错误")
        void should_return_error_when_title_empty() {
            ProblemReport existing = createProblem(PROBLEM_ID, "原标题", CATEGORY_VERIFIED, STATUS_SOLVING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);

            ProblemDTO dto = createProblemDTO("");
            dto.setTitle(null);
            Result result = problemService.updateProblem(PROBLEM_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("operatorId为空应返回错误")
        void should_return_error_when_operator_id_null() {
            ProblemReport existing = createProblem(PROBLEM_ID, "原标题", CATEGORY_VERIFIED, STATUS_SOLVING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);

            ProblemDTO dto = createProblemDTO("新标题");
            Result result = problemService.updateProblem(PROBLEM_ID, dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateStatus 更新状态 ====================

    @Nested
    @DisplayName("updateStatus 更新状态")
    class UpdateStatusTests {

        @FastTest
        @DisplayName("更新状态成功应返回成功")
        void should_update_status_successfully() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_VERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);
            when(problemReportDAO.updateCategoryAndStatus(eq(PROBLEM_ID), eq(CATEGORY_VERIFIED), eq(STATUS_SOLVING), any(), eq(ADMIN_USER_ID))).thenReturn(true);

            Result result = problemService.updateStatus(PROBLEM_ID, STATUS_SOLVING, "处理中", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("问题不存在应返回404")
        void should_return_404_when_problem_not_found() {
            when(problemReportDAO.findById(NONEXISTENT_PROBLEM_ID)).thenReturn(null);

            Result result = problemService.updateStatus(NONEXISTENT_PROBLEM_ID, STATUS_SOLVED, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("无效状态值应返回错误")
        void should_return_error_when_invalid_status() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_VERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);

            Result result = problemService.updateStatus(PROBLEM_ID, "INVALID_STATUS", null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("只有属实的分类才能更新状态")
        void should_return_error_when_not_verified() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_UNVERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);

            Result result = problemService.updateStatus(PROBLEM_ID, STATUS_SOLVED, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== updateCategory 更新分类 ====================

    @Nested
    @DisplayName("updateCategory 更新分类")
    class UpdateCategoryTests {

        @FastTest
        @DisplayName("更新分类成功应返回成功")
        void should_update_category_successfully() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_UNVERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);
            when(problemReportDAO.updateCategoryAndStatus(eq(PROBLEM_ID), eq(CATEGORY_VERIFIED), anyString(), any(), eq(ADMIN_USER_ID))).thenReturn(true);

            Result result = problemService.updateCategory(PROBLEM_ID, CATEGORY_VERIFIED, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("无效分类值应返回错误")
        void should_return_error_when_invalid_category() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_UNVERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);

            Result result = problemService.updateCategory(PROBLEM_ID, "INVALID_CATEGORY", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== addComment 添加备注 ====================

    @Nested
    @DisplayName("addComment 添加备注")
    class AddCommentTests {

        @FastTest
        @DisplayName("添加备注成功应返回成功")
        void should_add_comment_successfully() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_UNVERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);
            when(problemReportDAO.updateAdminComment(PROBLEM_ID, "处理备注")).thenReturn(true);

            Result result = problemService.addComment(PROBLEM_ID, "处理备注", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("问题不存在应返回404")
        void should_return_404_when_problem_not_found() {
            when(problemReportDAO.findById(NONEXISTENT_PROBLEM_ID)).thenReturn(null);

            Result result = problemService.addComment(NONEXISTENT_PROBLEM_ID, "备注", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("operatorId为空应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = problemService.addComment(PROBLEM_ID, "备注", null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== deleteProblem 删除问题 ====================

    @Nested
    @DisplayName("deleteProblem 删除问题")
    class DeleteProblemTests {

        @FastTest
        @DisplayName("删除问题成功应返回成功")
        void should_delete_problem_successfully() {
            ProblemReport existing = createProblem(PROBLEM_ID, "问题", CATEGORY_UNVERIFIED, STATUS_PENDING);
            when(problemReportDAO.findById(PROBLEM_ID)).thenReturn(existing);
            when(problemReportDAO.delete(PROBLEM_ID)).thenReturn(true);

            Result result = problemService.deleteProblem(PROBLEM_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("问题不存在应返回404")
        void should_return_404_when_problem_not_found() {
            when(problemReportDAO.findById(NONEXISTENT_PROBLEM_ID)).thenReturn(null);

            Result result = problemService.deleteProblem(NONEXISTENT_PROBLEM_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("ID为空应返回错误")
        void should_return_error_when_id_null() {
            Result result = problemService.deleteProblem(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getStatistics 问题统计 ====================

    @Nested
    @DisplayName("getStatistics 问题统计")
    class GetStatisticsTests {

        @FastTest
        @DisplayName("获取统计成功应返回成功")
        void should_get_statistics_successfully() {
            when(problemReportDAO.countPending()).thenReturn(5);
            when(problemReportDAO.countByCategory(CATEGORY_VERIFIED)).thenReturn(10);
            when(problemReportDAO.countByCategory(CATEGORY_UNVERIFIED)).thenReturn(3);
            when(problemReportDAO.countByCategory(CATEGORY_INVALID)).thenReturn(2);
            when(problemReportDAO.countByStatus(STATUS_SOLVED)).thenReturn(8);

            Result result = problemService.getStatistics();

            assertThat(result.isSuccess()).isTrue();
        }
    }
}