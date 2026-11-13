package servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LegacyGetMutationTest {

    @Test
    void legacyGetActionsCannotApproveOrDelete() throws Exception {
        HttpServletRequest request = request("approveActivity", "ADMIN");
        HttpServletResponse response = mock(HttpServletResponse.class);
        new ActivityServlet().doGet(request, response);
        verify(response).sendError(405);

        request = request("delete", "MEMBER");
        response = mock(HttpServletResponse.class);
        new AwardServlet().doGet(request, response);
        verify(response).sendError(405);

        request = request("deleteGroup", "MEMBER");
        response = mock(HttpServletResponse.class);
        new GroupServlet().doGet(request, response);
        verify(response).sendError(405);

        request = request("delete", "ADMIN");
        response = mock(HttpServletResponse.class);
        new GroupAdminServlet().doGet(request, response);
        verify(response).sendError(405);

        request = request("delete", "MEMBER");
        response = mock(HttpServletResponse.class);
        new ResumeServlet().doGet(request, response);
        verify(response).sendError(405);
    }

    @Test
    void logoutGetCannotInvalidateSession() throws Exception {
        HttpServletRequest request = request(null, "MEMBER");
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = request.getSession(false);
        new LogoutServlet().doGet(request, response);
        verify(response).sendError(405);
        verify(session, never()).invalidate();
    }

    private HttpServletRequest request(String action, String role) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        User user = new User();
        user.setRole(role);
        when(request.getParameter("action")).thenReturn(action);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        return request;
    }
}
