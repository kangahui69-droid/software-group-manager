package servlet.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.RecruitService;
import util.Result;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RecruitApiServletSecurityTest {
    private RecruitApiServlet servlet;
    private RecruitService recruitService;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new RecruitApiServlet();
        recruitService = mock(RecruitService.class);
        Field field = RecruitApiServlet.class.getDeclaredField("recruitService");
        field.setAccessible(true);
        field.set(servlet, recruitService);

        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    void unauthenticatedManagementRequestsStopAfterOne401Response() throws Exception {
        servlet.doGet(request("/api/recruit/100", null), response);
        servlet.doPost(request("/api/recruit/100/approve", null), response);
        servlet.doDelete(request("/api/recruit/100", null), response);

        verify(response, times(3)).setStatus(401);
        verifyNoInteractions(recruitService);
        assertThat(responseBody.toString()).doesNotContain("\"code\":0");
    }

    @Test
    void memberCannotReadApplicationsOrManageThem() throws Exception {
        User member = user(2, "MEMBER");
        servlet.doGet(request("/api/recruit", member), response);
        servlet.doGet(request("/api/recruit/100", member), response);
        servlet.doGet(request("/api/recruit/count", member), response);
        servlet.doPost(request("/api/recruit/100/approve", member), response);
        servlet.doPost(request("/api/recruit/100/reject", member), response);
        servlet.doDelete(request("/api/recruit/100", member), response);

        verify(response, times(6)).setStatus(403);
        verifyNoInteractions(recruitService);
    }

    @Test
    void anonymousSubmissionAndYearsRemainPublic() throws Exception {
        when(recruitService.findAllYears()).thenReturn(Result.ok(List.of(2026)));
        HttpServletRequest submit = request("/api/recruit", null);
        when(submit.getReader()).thenReturn(new BufferedReader(new StringReader(
                "{\"name\":\"Alice\",\"studentId\":\"2026001\"}")));
        when(recruitService.submitApplication(any())).thenReturn(Result.ok());

        servlet.doGet(request("/api/recruit/years", null), response);
        servlet.doPost(submit, response);

        verify(response, times(2)).setStatus(200);
        verify(recruitService).findAllYears();
        verify(recruitService).submitApplication(any());
    }

    @Test
    void adminCanReachManagementOperationsThroughUriFallback() throws Exception {
        User admin = user(1, "ADMIN");
        when(recruitService.listApplications(any(), any(), any(), any())).thenReturn(Result.ok(List.of()));
        when(recruitService.approveApplication(100, 1)).thenReturn(Result.ok());
        when(recruitService.deleteApplication(100)).thenReturn(Result.ok());

        servlet.doGet(request("/api/recruit", admin), response);
        servlet.doPost(request("/api/recruit/100/approve", admin), response);
        servlet.doDelete(request("/api/recruit/100", admin), response);

        verify(response, times(3)).setStatus(200);
        verify(recruitService).listApplications(any(), any(), any(), any());
        verify(recruitService).approveApplication(100, 1);
        verify(recruitService).deleteApplication(100);
    }

    private HttpServletRequest request(String path, User user) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/software-group" + path);
        if (user != null) {
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("user")).thenReturn(user);
            when(request.getSession(false)).thenReturn(session);
        }
        return request;
    }

    private User user(int id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
