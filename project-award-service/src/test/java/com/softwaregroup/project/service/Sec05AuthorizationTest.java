package com.softwaregroup.project.service;

import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.project.dao.AwardDAO;
import com.softwaregroup.project.dao.AwardImageDAO;
import com.softwaregroup.project.dao.ProjectDAO;
import com.softwaregroup.project.model.Award;
import com.softwaregroup.project.model.Project;
import com.softwaregroup.project.model.ProjectMemberApplication;
import com.softwaregroup.project.model.dto.ProjectFilterDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock ProjectDAO projects;
    @Mock AwardDAO awards;
    @Mock AwardImageDAO images;
    @InjectMocks ProjectService projectService;
    @InjectMocks AwardService awardService;

    @Test
    void unrelatedMemberCannotApproveProjectApplication() {
        ProjectMemberApplication application = new ProjectMemberApplication();
        application.setProjectId(7);
        application.setStatus("PENDING");
        Project project = new Project();
        project.setLeaderId(2);
        project.setAdminId(3);
        when(projects.getMemberApplicationById(9)).thenReturn(application);
        when(projects.findById(7)).thenReturn(project);

        assertThat(projectService.approveMember(9, 1).getCode()).isEqualTo(403);
        verify(projects, never()).approveMemberApplication(anyInt(), anyInt(), any());
    }

    @Test
    void pendingProjectIsAbsentFromPublicList() {
        when(projects.countByConditions(null, null, null, true)).thenReturn(0);
        assertThat(((Map<?, ?>) projectService.listProjects(new ProjectFilterDTO(), 1, 20, null)
                .getData()).get("list")).isEqualTo(List.of());
        verify(projects).countByConditions(null, null, null, true);
    }

    @Test
    void pendingAwardIsInvisibleToOtherUsers() {
        Award award = new Award();
        award.setAwardStatus("PENDING");
        award.setCreatedBy(2);
        when(awards.findById(7)).thenReturn(award);
        assertThat(awardService.getAwardDetail(7, new CurrentUser(1, "other", "MEMBER")).getCode()).isEqualTo(404);
        assertThat(awardService.getAwardImages(7, null).getCode()).isEqualTo(404);
        verifyNoInteractions(images);
    }

    @Test
    void publicAwardListOnlyQueriesApproved() {
        awardService.listAwards(null, 1, null);
        verify(awards).findByStatus("APPROVED");
        verify(awards, never()).findAll();
    }
}
