package servlet;

import dto.ProjectFilterDTO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
    void memberListIsScopedToCurrentUser() throws Exception {
        loginAs(17, "MEMBER");
        when(request.getParameter("action")).thenReturn("list");
        when(projectService.getMyProjects(17, 1, 20)).thenReturn(Result.ok(List.of()));
        when(request.getRequestDispatcher("/jsp/member/project/list.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(projectService).getMyProjects(17, 1, 20);
        verify(projectService, never()).listProjects(any(ProjectFilterDTO.class), eq(1), eq(20));
        verify(dispatcher).forward(request, response);
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
        when(projectService.listProjects(any(ProjectFilterDTO.class), eq(1), eq(20)))
                .thenReturn(Result.ok(List.of()));
        when(request.getRequestDispatcher("/jsp/admin/project/list.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(projectService).listProjects(any(ProjectFilterDTO.class), eq(1), eq(20));
        verify(projectService, never()).getMyProjects(anyInt(), anyInt(), anyInt());
        verify(dispatcher).forward(request, response);
    }

    private void loginAs(int userId, String role) {
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }
}
