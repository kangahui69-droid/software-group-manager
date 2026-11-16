package servlet;

import dto.ProjectFilterDTO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Project;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.ProjectService;
import util.Result;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServletAuthorizationTest {
    @Mock private ProjectService projectService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    private ProjectServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProjectServlet();
        Field serviceField = ProjectServlet.class.getDeclaredField("projectService");
        serviceField.setAccessible(true);
        serviceField.set(servlet, projectService);
    }

    @Test
    void anonymousListIsRejected() throws Exception {
        when(request.getParameter("action")).thenReturn("list");
        servlet.doGet(request, response);
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        verifyNoInteractions(projectService);
    }

    @Test
    void memberProjectsAreScopedToCurrentUser() throws Exception {
        loginAs(17, "MEMBER");
        when(request.getParameter("action")).thenReturn("myProjects");
        when(projectService.getMyProjects(17, 1, 20)).thenReturn(emptyProjectPage());
        when(request.getRequestDispatcher("/WEB-INF/jsp/member/project/list.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(projectService).getMyProjects(17, 1, 20);
        verify(projectService, never()).listProjectsForAdmin(any(ProjectFilterDTO.class), eq(1), eq(20), any(User.class));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void legacyListRedirectsMemberToMyProjects() throws Exception {
        loginAs(17, "MEMBER");
        when(request.getParameter("action")).thenReturn("list");
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/project?action=myProjects");
        verifyNoInteractions(projectService);
    }

    @Test
    void legacyListRedirectsAdminToManagement() throws Exception {
        loginAs(2, "ADMIN");
        when(request.getParameter("action")).thenReturn("list");
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/project?action=manage");
        verifyNoInteractions(projectService);
    }

    @Test
    void memberCannotOpenManagementList() throws Exception {
        loginAs(17, "MEMBER");
        when(request.getParameter("action")).thenReturn("manage");
        servlet.doGet(request, response);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(projectService);
    }

    @Test
    void adminManagementListUsesAllProjects() throws Exception {
        loginAs(2, "ADMIN");
        when(request.getParameter("action")).thenReturn("manage");
        when(projectService.listProjectsForAdmin(any(ProjectFilterDTO.class), eq(1), eq(20), any(User.class)))
                .thenReturn(emptyProjectPage());
        when(request.getRequestDispatcher("/jsp/admin/project/list.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(projectService).listProjectsForAdmin(any(ProjectFilterDTO.class), eq(1), eq(20), any(User.class));
        verify(projectService, never()).getMyProjects(anyInt(), anyInt(), anyInt());
        verify(dispatcher).forward(request, response);
    }

    @Test
    void browseUsesApprovedProjectScope() throws Exception {
        loginAs(17, "MEMBER");
        when(request.getParameter("action")).thenReturn("browse");
        when(projectService.browseProjects(any(ProjectFilterDTO.class), eq(1), eq(20)))
                .thenReturn(emptyProjectPage());
        when(request.getRequestDispatcher("/WEB-INF/jsp/member/project/list.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(projectService).browseProjects(any(ProjectFilterDTO.class), eq(1), eq(20));
        verify(request).setAttribute("browseMode", true);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void adminEditPageReceivesProjectEntityWithItsStatus() throws Exception {
        loginAs(2, "ADMIN");
        when(request.getParameter("action")).thenReturn("edit");
        when(request.getParameter("id")).thenReturn("42");
        Project project = new Project();
        project.setId(42);
        project.setStatus(Project.STATUS_APPROVED);
        when(projectService.getProjectDetail(42, 2)).thenReturn(Result.ok(Map.of("project", project)));
        when(request.getRequestDispatcher("/jsp/admin/project/edit.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(request).setAttribute("project", project);
        verify(dispatcher).forward(request, response);
    }

    private void loginAs(int userId, String role) {
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }

    private Result emptyProjectPage() {
        return Result.ok(Map.of("list", List.of(), "total", 0,
                "page", 1, "pageSize", 20, "totalPages", 0));
    }
}
