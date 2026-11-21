package servlet;

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
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Func02ProjectApplicationsServletTest {
    @Mock private ProjectService service;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    private ProjectServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProjectServlet();
        Field field = ProjectServlet.class.getDeclaredField("projectService");
        field.setAccessible(true);
        field.set(servlet, service);
        when(request.getParameter("action")).thenReturn("myApplications");
    }

    @Test
    void anonymousUserCannotLoadAnotherUsersApplications() throws Exception {
        when(request.getContextPath()).thenReturn("");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/login.jsp");
        verifyNoInteractions(service);
    }

    @Test
    void pageTwoLoadsOnlyCurrentUsersApplications() throws Exception {
        loginAs(7);
        when(request.getParameter("page")).thenReturn("2");
        when(service.getMyApplications(7, 2, 20)).thenReturn(Result.ok(
                Map.of("list", List.of(), "page", 2, "totalPages", 3, "total", 45)));
        when(request.getRequestDispatcher("/WEB-INF/jsp/member/project/myApplications.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(service).getMyApplications(7, 2, 20);
        verify(request).setAttribute("applications", List.of());
        verify(request).setAttribute("currentPage", 2);
        verify(request).setAttribute("totalPages", 3);
        verify(request).setAttribute("totalCount", 45);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void invalidPageFallsBackToFirstPage() throws Exception {
        loginAs(7);
        when(request.getParameter("page")).thenReturn("not-a-number");
        when(service.getMyApplications(7, 1, 20)).thenReturn(Result.ok(
                Map.of("list", List.of(), "page", 1, "totalPages", 0, "total", 0)));
        when(request.getRequestDispatcher("/WEB-INF/jsp/member/project/myApplications.jsp")).thenReturn(dispatcher);

        servlet.doGet(request, response);

        verify(service).getMyApplications(7, 1, 20);
        verify(dispatcher).forward(request, response);
    }

    @Test
    void serviceFailureDoesNotRenderAnEmptyPage() throws Exception {
        loginAs(7);
        when(service.getMyApplications(7, 1, 20)).thenReturn(Result.error(500, "查询失败"));

        servlet.doGet(request, response);

        verify(response).sendError(500);
    }

    private void loginAs(int userId) {
        User user = new User();
        user.setId(userId);
        user.setRole("MEMBER");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }
}
