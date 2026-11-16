package filter;

import dao.OperationLogDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.OperationLog;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class Aud05AuditSanitizationTest {
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
        when(response.getStatus()).thenReturn(200);
        when(dao.insert(any())).thenReturn(true);
    }

    @Test
    void unknownRouteKeepsOnlyWhitelistedActionAndMasksContactAndUserAgent() throws Exception {
        when(request.getRequestURI()).thenReturn("/software-group/api/opaque-secret-12345678901234567890");
        when(request.getQueryString()).thenReturn("password=top-secret&token=bearer-secret"
                + "&email=a@example.com&phone=13800138000&action=update");
        when(request.getHeader("User-Agent")).thenReturn("curl/8 token=bearer-secret");
        User user = new User();
        user.setUsername("a@example.com");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, (req, resp) -> { });

        OperationLog log = capturedLog();
        assertEquals("POST 其他请求 [action=update]", log.getDescription());
        assertEquals("[邮箱已隐藏]", log.getUsername());
        assertEquals("curl", log.getUserAgent());
        String stored = log.getDescription() + log.getUsername() + log.getUserAgent();
        assertFalse(stored.contains("top-secret"));
        assertFalse(stored.contains("bearer-secret"));
        assertFalse(stored.contains("a@example.com"));
        assertFalse(stored.contains("13800138000"));
    }

    @Test
    void knownActionDoesNotCopyIdentityNumberFromIdParameter() throws Exception {
        when(request.getRequestURI()).thenReturn("/software-group/project");
        when(request.getQueryString()).thenReturn("action=delete&id=110101199003074816");
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("110101199003074816");
        User user = new User();
        user.setUsername("13800138000");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getSession(false)).thenReturn(session);

        filter.doFilter(request, response, (req, resp) -> { });

        OperationLog log = capturedLog();
        assertEquals("POST 删除项目", log.getDescription());
        assertEquals("[手机号已隐藏]", log.getUsername());
        assertNull(log.getTargetId());
        assertFalse(log.getDescription().contains("110101199003074816"));
    }

    @Test
    void sanitizerRetainsOnlyValidDatabaseIdsAndMasksIdentityFields() {
        assertEquals("42", AuditLogSanitizer.safeTargetId("42"));
        assertNull(AuditLogSanitizer.safeTargetId("18b8ad79-8353-453b-9665-d55a8206464a"));
        assertNull(AuditLogSanitizer.safeTargetId("110101199003074816"));
        assertNull(AuditLogSanitizer.safeTargetId("2147483648"));
        assertEquals("[身份证号已隐藏]", AuditLogSanitizer.maskedUsername("110101199003074816"));
        assertEquals("[用户名已隐藏]", AuditLogSanitizer.maskedUsername("token=secret"));
    }

    private OperationLog capturedLog() {
        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(dao).insert(captor.capture());
        return captor.getValue();
    }
}
