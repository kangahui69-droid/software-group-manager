package servlet;

import dao.UserDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.Test;
import util.CSRFTokenUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CsrfLifecycleTest {

    @Test
    void tokenEndpointKeepsExistingTokenAndDisallowsPost() throws Exception {
        CSRFTokenServlet servlet = new CSRFTokenServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        StringWriter body = new StringWriter();
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(CSRFTokenUtil.CSRF_TOKEN_SESSION_ATTR)).thenReturn("existing-token");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doGet(request, response);
        assertThat(body.toString()).contains("existing-token");
        verify(response).setHeader("Cache-Control", "no-store");
        verify(session, never()).setAttribute(eq(CSRFTokenUtil.CSRF_TOKEN_SESSION_ATTR), any());

        servlet.doPost(request, response);
        verify(response).sendError(405);
    }

    @Test
    void successfulLoginRotatesSessionIdAndCsrfToken() throws Exception {
        LoginServlet servlet = new LoginServlet();
        UserDAO userDAO = mock(UserDAO.class);
        Field field = LoginServlet.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(servlet, userDAO);
        User user = new User();
        user.setUsername("alice");
        user.setRole("MEMBER");
        when(userDAO.findByUsernameAndPassword("alice", "secret")).thenReturn(user);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getParameter("username")).thenReturn("alice");
        when(request.getParameter("password")).thenReturn("secret");
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("/software-group");

        servlet.doPost(request, response);

        verify(request).changeSessionId();
        verify(session).setAttribute(eq(CSRFTokenUtil.CSRF_TOKEN_SESSION_ATTR), anyString());
        verify(session).setAttribute("user", user);
        verify(response).sendRedirect("/software-group/index.jsp");
    }
}
