package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.CSRFTokenUtil;

import static org.mockito.Mockito.*;

class SecurityFilterCsrfTest {
    private SecurityFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/software-group");
        when(request.getRequestURI()).thenReturn("/software-group/project");
    }

    @Test
    void rejectsUnsafeMethodsWithoutValidToken() throws Exception {
        when(session.getAttribute(CSRFTokenUtil.CSRF_TOKEN_SESSION_ATTR)).thenReturn("expected");
        for (String method : new String[]{"POST", "PUT", "PATCH", "DELETE"}) {
            when(request.getMethod()).thenReturn(method);
            filter.doFilter(request, response, chain);
        }
        verify(response, times(4)).sendError(eq(403), anyString());
        verifyNoInteractions(chain);
    }

    @Test
    void acceptsMatchingHeaderOrFormField() throws Exception {
        when(session.getAttribute(CSRFTokenUtil.CSRF_TOKEN_SESSION_ATTR)).thenReturn("expected");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(CSRFTokenUtil.CSRF_TOKEN_HEADER)).thenReturn("expected");
        filter.doFilter(request, response, chain);
        when(request.getHeader(CSRFTokenUtil.CSRF_TOKEN_HEADER)).thenReturn(null);
        when(request.getParameter(CSRFTokenUtil.CSRF_TOKEN_NAME)).thenReturn("expected");
        filter.doFilter(request, response, chain);
        verify(chain, times(2)).doFilter(any(), same(response));
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    void rejectsMissingSessionEvenWhenRequestSuppliesToken() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        when(request.getHeader(CSRFTokenUtil.CSRF_TOKEN_HEADER)).thenReturn("forged");
        filter.doFilter(request, response, chain);
        verify(response).sendError(eq(403), anyString());
        verifyNoInteractions(chain);
    }

    @Test
    void blocksGetAndHeadMutationActionsButAllowsRead() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("delete");
        filter.doFilter(request, response, chain);
        when(request.getMethod()).thenReturn("HEAD");
        filter.doFilter(request, response, chain);
        verify(response, times(2)).sendError(405);
        verifyNoInteractions(chain);

        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("list");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(any(), same(response));
    }

    @Test
    void logoutRequiresPost() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/software-group/logout");
        filter.doFilter(request, response, chain);
        verify(response).sendError(405);
        verifyNoInteractions(chain);
    }
}
