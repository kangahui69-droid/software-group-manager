package com.softwaregroup.monitor.integration;

import com.softwaregroup.monitor.dao.ProblemReportDAO;
import com.softwaregroup.monitor.dao.UserDAO;
import com.softwaregroup.monitor.model.ProblemReport;
import com.softwaregroup.monitor.model.User;
import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.service.ProblemService;
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
 * ProblemService 集成测试
 *
 * 测试问题反馈服务的核心功能：问题提交、列表查询、状态更新
 */
@ExtendWith(MockitoExtension.class)
class ProblemServiceIT {

    @Mock
    private ProblemReportDAO problemReportDAO;

    @Mock
    private UserDAO userDAO;

    private ProblemService problemService;

    @BeforeEach
    void setUp() {
        problemService = new ProblemService(problemReportDAO, userDAO);
    }

    @Test
    void submitProblem_withValidData_shouldReturnSuccess() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");
        dto.setContent("这是测试内容");
        dto.setReporterName("张三");
        dto.setReporterContact("13800138000");

        when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

        Result result = problemService.submitProblem(dto, null);

        assertThat(result.isSuccess()).isTrue();
        verify(problemReportDAO).insert(any(ProblemReport.class));
    }

    @Test
    void submitProblem_withEmptyTitle_shouldReturnError() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("");
        dto.setContent("这是测试内容");

        Result result = problemService.submitProblem(dto, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void submitProblem_withNullContent_shouldReturnError() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");
        dto.setContent(null);

        Result result = problemService.submitProblem(dto, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void submitProblem_withTooLongTitle_shouldReturnError() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("a".repeat(300));
        dto.setContent("这是测试内容");

        Result result = problemService.submitProblem(dto, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void submitProblem_withTooLongContent_shouldReturnError() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");
        dto.setContent("a".repeat(6000));

        Result result = problemService.submitProblem(dto, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void submitProblem_withMemberUser_shouldSetReporterTypeMember() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");
        dto.setContent("这是测试内容");
        dto.setReporterName("张三");

        User member = new User();
        member.setId(1);
        member.setRole("MEMBER");

        when(userDAO.findById(1)).thenReturn(member);
        when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

        Result result = problemService.submitProblem(dto, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(problemReportDAO).insert(argThat(problem ->
            ProblemReport.REPORTER_TYPE_MEMBER.equals(problem.getReporterType()) &&
            problem.getUserId() != null && problem.getUserId().equals(1)
        ));
    }

    @Test
    void submitProblem_withAdminUser_shouldSetReporterTypeAdmin() {
        ProblemDTO dto = new ProblemDTO();
        dto.setTitle("测试问题");
        dto.setContent("这是测试内容");

        User admin = new User();
        admin.setId(1);
        admin.setRole("ADMIN");

        when(userDAO.findById(1)).thenReturn(admin);
        when(problemReportDAO.insert(any(ProblemReport.class))).thenReturn(1);

        Result result = problemService.submitProblem(dto, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(problemReportDAO).insert(argThat(problem ->
            ProblemReport.REPORTER_TYPE_ADMIN.equals(problem.getReporterType())
        ));
    }

    @Test
    void getProblemDetail_withValidId_shouldReturnProblem() {
        ProblemReport problem = new ProblemReport();
        problem.setId(1);
        problem.setTitle("测试问题");

        when(problemReportDAO.findById(1)).thenReturn(problem);

        Result result = problemService.getProblemDetail(1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo(problem);
    }

    @Test
    void getProblemDetail_withInvalidId_shouldReturnNotFound() {
        when(problemReportDAO.findById(9999)).thenReturn(null);

        Result result = problemService.getProblemDetail(9999);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void getProblemDetail_withNullId_shouldReturnBadRequest() {
        Result result = problemService.getProblemDetail(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listProblems_withDefaultFilter_shouldReturnAllProblems() {
        ProblemReport problem = new ProblemReport();
        problem.setId(1);
        problem.setTitle("测试问题");

        when(problemReportDAO.findAll()).thenReturn(Arrays.asList(problem));

        Result result = problemService.listProblems(null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(problemReportDAO).findAll();
    }

    @Test
    void listProblems_withInvalidPage_shouldReturnError() {
        Result result = problemService.listProblems(null, 0, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listProblems_withInvalidPageSize_shouldReturnError() {
        Result result = problemService.listProblems(null, 1, 200);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getStatistics_shouldReturnCorrectCounts() {
        when(problemReportDAO.countPending()).thenReturn(5);
        when(problemReportDAO.countByCategory(ProblemReport.CATEGORY_VERIFIED)).thenReturn(3);
        when(problemReportDAO.countByCategory(ProblemReport.CATEGORY_UNVERIFIED)).thenReturn(2);
        when(problemReportDAO.countByCategory(ProblemReport.CATEGORY_INVALID)).thenReturn(1);
        when(problemReportDAO.countByStatus(ProblemReport.STATUS_SOLVED)).thenReturn(4);

        Result result = problemService.getStatistics();

        assertThat(result.isSuccess()).isTrue();
        verify(problemReportDAO).countPending();
        verify(problemReportDAO).countByCategory(ProblemReport.CATEGORY_VERIFIED);
        verify(problemReportDAO).countByStatus(ProblemReport.STATUS_SOLVED);
    }

    @Test
    void updateStatus_withVerifiedProblem_shouldUpdateStatus() {
        ProblemReport problem = new ProblemReport();
        problem.setId(1);
        problem.setCategory(ProblemReport.CATEGORY_VERIFIED);
        problem.setStatus(ProblemReport.STATUS_PENDING);

        when(problemReportDAO.findById(1)).thenReturn(problem);
        when(problemReportDAO.updateCategoryAndStatus(eq(1), anyString(), anyString(), any(), anyInt())).thenReturn(true);

        Result result = problemService.updateStatus(1, ProblemReport.STATUS_SOLVING, null, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void updateStatus_withUnverifiedProblem_shouldReturnError() {
        ProblemReport problem = new ProblemReport();
        problem.setId(1);
        problem.setCategory(ProblemReport.CATEGORY_UNVERIFIED);

        when(problemReportDAO.findById(1)).thenReturn(problem);

        Result result = problemService.updateStatus(1, ProblemReport.STATUS_SOLVING, null, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void deleteProblem_withValidId_shouldReturnSuccess() {
        ProblemReport problem = new ProblemReport();
        problem.setId(1);

        when(problemReportDAO.findById(1)).thenReturn(problem);
        when(problemReportDAO.delete(1)).thenReturn(true);

        Result result = problemService.deleteProblem(1, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(problemReportDAO).delete(1);
    }

    @Test
    void deleteProblem_withNullOperatorId_shouldReturnError() {
        Result result = problemService.deleteProblem(1, null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }
}
