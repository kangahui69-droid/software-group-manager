package filter;

import dao.OperationLogDAO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.OperationLog;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoggingFilterTest {
    private OperationLogDAO dao;
    private LoggingFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        dao = mock(OperationLogDAO.class);
        filter = new LoggingFilter(dao);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/software-group/project");
        when(response.getStatus()).thenReturn(200);
        when(dao.insert(any())).thenReturn(true);
    }

    @Test
    void successfulRedirectIsLoggedOnlyAfterBusinessCompletes() throws Exception {
        User user = new User();
        user.setId(7);
        user.setUsername("project-owner");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);
        when(request.getParameter("id")).thenReturn("42");

        filter.doFilter(request, response, (req, resp) -> {
            verify(dao, never()).insert(any());
            ((HttpServletResponse) resp).sendRedirect("/software-group/project?success=created");
        });

        OperationLog log = capturedLog();
        assertEquals(7, log.getUserId());
        assertEquals("project-owner", log.getUsername());
        assertTrue(log.getDescription().startsWith("POST"));
        assertEquals("SUCCESS", log.getResult());
        assertEquals(302, log.getHttpStatus());
        assertTrue(log.getDurationMs() >= 0);
        assertEquals("PROJECT", log.getTargetType());
        assertEquals("42", log.getTargetId());
        assertNull(log.getErrorCode());
        UUID.fromString(log.getRequestId());
        verify(response).setHeader("X-Request-ID", log.getRequestId());
    }

    @Test
    void permissionAndCsrfRejectionAreNotRecordedAsSuccessfulOperations() throws Exception {
        filter.doFilter(request, response,
                (req, resp) -> ((HttpServletResponse) resp).sendError(403));
        OperationLog log = capturedLog();
        assertEquals("DENIED", log.getResult());
        assertEquals(403, log.getHttpStatus());
        assertEquals("HTTP_403", log.getErrorCode());
    }

    @Test
    void serverFailureAndBusinessErrorRedirectAreMarkedFailed() throws Exception {
        filter.doFilter(request, response,
                (req, resp) -> ((HttpServletResponse) resp).setStatus(500));
        OperationLog log = capturedLog();
        assertEquals("FAILED", log.getResult());
        assertEquals(500, log.getHttpStatus());
        assertEquals("HTTP_500", log.getErrorCode());

        OperationLogDAO otherDao = mock(OperationLogDAO.class);
        when(otherDao.insert(any())).thenReturn(true);
        LoggingFilter otherFilter = new LoggingFilter(otherDao);
        otherFilter.doFilter(request, response,
                (req, resp) -> ((HttpServletResponse) resp)
                        .sendRedirect("/software-group/project?action=create&error=failed"));
        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(otherDao).insert(captor.capture());
        assertEquals("FAILED", captor.getValue().getResult());
        assertEquals("BUSINESS_ERROR", captor.getValue().getErrorCode());
    }

    @Test
    void thrownBusinessExceptionIsLoggedAsFailureAndRethrown() throws Exception {
        FilterChain chain = (req, resp) -> { throw new ServletException("business failed"); };
        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
        OperationLog log = capturedLog();
        assertEquals("FAILED", log.getResult());
        assertEquals(500, log.getHttpStatus());
        assertEquals("UNHANDLED_EXCEPTION", log.getErrorCode());
    }

    @Test
    void forwardedBusinessErrorWithHttp200IsNotMarkedSuccessful() throws Exception {
        filter.doFilter(request, response, (req, resp) ->
                when(request.getAttribute("error")).thenReturn("project was not updated"));
        OperationLog log = capturedLog();
        assertEquals("FAILED", log.getResult());
        assertEquals(200, log.getHttpStatus());
        assertEquals("BUSINESS_ERROR", log.getErrorCode());
    }

    @Test
    void failedLoginForwardIsMarkedDenied() throws Exception {
        when(request.getRequestURI()).thenReturn("/software-group/login");
        filter.doFilter(request, response, (req, resp) ->
                when(request.getAttribute("error")).thenReturn("invalid credentials"));
        OperationLog log = capturedLog();
        assertEquals("DENIED", log.getResult());
        assertEquals("AUTH_FAILED", log.getErrorCode());
    }

    @Test
    void anonymousRedirectToLoginIsDeniedAndGetIsNotLogged() throws Exception {
        filter.doFilter(request, response,
                (req, resp) -> ((HttpServletResponse) resp).sendRedirect("/software-group/login.jsp"));
        assertEquals("DENIED", capturedLog().getResult());

        OperationLogDAO getDao = mock(OperationLogDAO.class);
        when(request.getMethod()).thenReturn("GET");
        new LoggingFilter(getDao).doFilter(request, response, (req, resp) -> { });
        verify(getDao, never()).insert(any());
    }

    @Test
    void sensitiveMemberReadIsLoggedWithoutItsQueryString() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/software-group/admin/member/view");
        when(request.getQueryString()).thenReturn("id=42&keyword=private-data");
        when(request.getParameter("id")).thenReturn("42");

        filter.doFilter(request, response, (req, resp) -> { });

        OperationLog log = capturedLog();
        assertEquals("GET", log.getOperation());
        assertEquals("管理员", log.getModule());
        assertEquals("GET 查看成员资料", log.getDescription());
        assertEquals("42", log.getTargetId());
    }

    @Test
    void auditRecordKeepsBothClientAndTrustedProxyAddresses() throws Exception {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.7");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.66, 203.0.113.7");

        new LoggingFilter(dao, new ClientIpResolver("127.0.0.1"))
                .doFilter(request, response, (req, resp) -> { });

        OperationLog log = capturedLog();
        assertEquals("203.0.113.7", log.getIpAddress());
        assertEquals("127.0.0.1", log.getProxyIp());
    }

    private OperationLog capturedLog() {
        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(dao).insert(captor.capture());
        return captor.getValue();
    }
}
