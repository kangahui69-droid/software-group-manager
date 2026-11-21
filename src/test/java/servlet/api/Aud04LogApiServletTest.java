package servlet.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import service.LogService;
import util.Result;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Aud04LogApiServletTest {
    @Test
    void realServletForwardsExplicitRangeToService() throws Exception {
        LogService service = mock(LogService.class);
        LogApiServlet servlet = new LogApiServlet(service);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        User user = new User();
        user.setRole("ADMIN");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getRequestURI()).thenReturn("/api/logs");
        when(request.getParameter("startTime")).thenReturn("2026-11-16T10:00");
        when(request.getParameter("endTime")).thenReturn("2026-11-16T11:00");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(service.listLogs(any(), eq(1), eq(20))).thenReturn(Result.ok(Map.of()));

        servlet.doGet(request, response);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> filter = ArgumentCaptor.forClass(Map.class);
        verify(service).listLogs(filter.capture(), eq(1), eq(20));
        assertEquals("2026-11-16T10:00", filter.getValue().get("startTime"));
        assertEquals("2026-11-16T11:00", filter.getValue().get("endTime"));
    }
}
