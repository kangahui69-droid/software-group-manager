package service;

import dao.ProjectDAO;
import dto.ProjectFilterDTO;
import model.Project;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.Result;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Func01ProjectScopeTest {
    @Mock private ProjectDAO projectDAO;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService();
        projectService.setProjectDAO(projectDAO);
    }

    @Test
    void memberCannotRequestTheManagementDatasetAtServiceLayer() {
        User member = user(7, "MEMBER");

        Result result = projectService.listProjectsForAdmin(new ProjectFilterDTO(), 1, 20, member);

        assertEquals(403, result.getCode());
        verifyNoInteractions(projectDAO);
    }

    @Test
    void adminRequestsTheManagementDataset() {
        User admin = user(1, "ADMIN");
        Project project = new Project();
        project.setId(22);
        when(projectDAO.countByConditions(null, null, null, null)).thenReturn(1);
        when(projectDAO.findPageByConditions(null, null, null, null, 20, 0)).thenReturn(List.of(project));

        Result result = projectService.listProjectsForAdmin(new ProjectFilterDTO(), 1, 20, admin);

        assertTrue(result.isSuccess());
        assertSame(project, ((List<?>) ((Map<?, ?>) result.getData()).get("list")).get(0));
        verify(projectDAO).findPageByConditions(null, null, null, null, 20, 0);
    }

    @Test
    void browseOverridesRequestedStatusWithApproved() {
        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setStatus("pending");
        filter.setKeyword("项目");
        when(projectDAO.countByConditions("项目", "approved", null, null)).thenReturn(0);

        Result result = projectService.browseProjects(filter, 1, 20);

        assertTrue(result.isSuccess());
        verify(projectDAO).countByConditions("项目", "approved", null, null);
    }

    @Test
    void myProjectsUsesOnlyTheAuthenticatedUsersId() {
        Project mine = new Project();
        mine.setId(31);
        when(projectDAO.countProjectsByUserId(7)).thenReturn(1);
        when(projectDAO.findPageByUserId(7, 20, 0)).thenReturn(List.of(mine));

        Result result = projectService.getMyProjects(7, 1, 20);

        assertTrue(result.isSuccess());
        assertEquals(List.of(mine), ((Map<?, ?>) result.getData()).get("list"));
        verify(projectDAO).findPageByUserId(7, 20, 0);
    }

    private User user(int id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
