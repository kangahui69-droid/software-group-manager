package com.softwaregroup.monitor.service;

import com.softwaregroup.monitor.dao.ProblemReportDAO;
import com.softwaregroup.monitor.dao.UserDAO;
import com.softwaregroup.monitor.model.ProblemReport;
import com.softwaregroup.monitor.model.dto.ProblemDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock ProblemReportDAO problems;
    @Mock UserDAO users;
    @InjectMocks ProblemService service;

    @Test
    void anotherUserCannotReadOrUpdateProblem() {
        ProblemReport problem = new ProblemReport();
        problem.setUserId(2);
        when(problems.findById(7)).thenReturn(problem);
        assertThat(service.getProblemDetail(7, 1).getCode()).isEqualTo(403);
        assertThat(service.updateProblem(7, new ProblemDTO(), 1).getCode()).isEqualTo(403);
    }

    @Test
    void ordinaryUserCannotDeleteProblem() {
        when(problems.findById(7)).thenReturn(new ProblemReport());
        assertThat(service.deleteProblem(7, 1).getCode()).isEqualTo(403);
        verify(problems, never()).delete(anyInt());
    }

    @Test
    void ordinaryUserCannotClassifyProblem() {
        when(problems.findById(7)).thenReturn(new ProblemReport());
        assertThat(service.updateCategory(7, "VERIFIED", 1).getCode()).isEqualTo(403);
        verify(problems, never()).updateCategoryAndStatus(anyInt(), any(), any(), any(), anyInt());
    }
}
