package servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadinessServletTest {
    @Test
    void returns200OnlyWhenDependenciesAreReady() throws Exception {
        StringWriter body = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        new ReadinessServlet(() -> true).doGet(mock(HttpServletRequest.class), response);

        verify(response).setStatus(200);
        verify(response).setHeader("Cache-Control", "no-store");
        assertEquals("READY\n", body.toString());
    }

    @Test
    void returns503WithoutLeakingDependencyErrors() throws Exception {
        StringWriter body = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        new ReadinessServlet(() -> {
            throw new IllegalStateException("private connection details");
        }).doGet(mock(HttpServletRequest.class), response);

        verify(response).setStatus(503);
        assertEquals("NOT_READY\n", body.toString());
    }

    @Test
    void headProbeUsedByContainerHealthcheckRunsReadinessCheck() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("HEAD");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        new ReadinessServlet(() -> false).service(request, response);

        verify(response).setStatus(503);
    }
}
