package servlet;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.AIService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Func03AiProjectRedirectServletTest {
    @Mock private AIService service;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    private AIServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AIServlet();
        Field field = AIServlet.class.getDeclaredField("aiService");
        field.setAccessible(true);
        field.set(servlet, service);
    }

    @Test
    void actionResponseIncludesTheRedirectFieldConsumedByChatPage() throws Exception {
        User user = new User();
        user.setId(7);
        user.setRole("MEMBER");
        when(request.getParameter("action")).thenReturn("execute");
        when(request.getParameter("actionType")).thenReturn("view_my_projects");
        when(request.getParameter("actionString")).thenReturn("");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        when(request.getContextPath()).thenReturn("/software-group");
        when(service.parseActionParams("")).thenReturn(Map.of());
        Map<String, Object> actionResult = new HashMap<>();
        actionResult.put("success", true);
        actionResult.put("type", "redirect");
        actionResult.put("redirectUrl", "/project?action=myProjects");
        when(service.executeAction("view_my_projects", Map.of(), user)).thenReturn(actionResult);
        StringWriter json = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(json));

        servlet.doPost(request, response);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = new Gson().fromJson(json.toString(), Map.class);
        assertEquals("/software-group/project?action=myProjects", result.get("redirect"));
        assertEquals("/project?action=myProjects", result.get("redirectUrl"));
    }
}
