package servlet.api;

import dto.ProjectFilterDTO;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Func01ProjectApiScopeTest {
    @Mock private ProjectService service;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    private ProjectApiServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProjectApiServlet();
        Field field = ProjectApiServlet.class.getDeclaredField("projectService");
        field.setAccessible(true);
        field.set(servlet, service);
        when(request.getSession(false)).thenReturn(session);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void memberListUsesPublicScope() throws Exception {
        loginAs(7, "MEMBER");
        when(request.getRequestURI()).thenReturn("/api/projects");
        when(service.browseProjects(any(ProjectFilterDTO.class), eq(1), eq(20)))
                .thenReturn(Result.ok(List.of()));

        servlet.doGet(request, response);

        verify(service).browseProjects(any(ProjectFilterDTO.class), eq(1), eq(20));
        verify(service, never()).listProjectsForAdmin(any(), anyInt(), anyInt(), any());
    }

    @Test
    void adminListUsesManagementScope() throws Exception {
        loginAs(1, "ADMIN");
        when(request.getRequestURI()).thenReturn("/api/projects");
        when(service.listProjectsForAdmin(any(ProjectFilterDTO.class), eq(1), eq(20), any(User.class)))
                .thenReturn(Result.ok(List.of()));

        servlet.doGet(request, response);

        verify(service).listProjectsForAdmin(any(ProjectFilterDTO.class), eq(1), eq(20), any(User.class));
        verify(service, never()).browseProjects(any(), anyInt(), anyInt());
    }

    @Test
    void myProjectsUsesCurrentUserId() throws Exception {
        loginAs(7, "MEMBER");
        when(request.getPathInfo()).thenReturn("/my");
        when(service.getMyProjects(7, 1, 20)).thenReturn(Result.ok(List.of()));

        servlet.doGet(request, response);

        verify(service).getMyProjects(7, 1, 20);
        verify(service, never()).listProjectsForAdmin(any(), anyInt(), anyInt(), any());
    }

    @Test
    void createdByMeUsesOwnerOnlyServiceAndCurrentUserId() throws Exception {
        loginAs(7, "MEMBER");
        when(request.getPathInfo()).thenReturn("/created-by-me");
        when(request.getParameter("page")).thenReturn("2");
        when(request.getParameter("pageSize")).thenReturn("3");
        when(service.getCreatedByMeProjects(7, 2, 3)).thenReturn(Result.ok(List.of()));

        servlet.doGet(request, response);

        verify(service).getCreatedByMeProjects(7, 2, 3);
        verify(service, never()).getMyProjects(anyInt(), anyInt(), anyInt());
    }

    private void loginAs(int id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        when(session.getAttribute("user")).thenReturn(user);
    }
}
