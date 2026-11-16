package service;

import dao.AIFaqStatisticsDAO;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class Func03AiProjectRedirectTest {
    @Mock private AIFaqStatisticsDAO statistics;
    private AIService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new AIService();
        Field field = AIService.class.getDeclaredField("faqStatsDAO");
        field.setAccessible(true);
        field.set(service, statistics);
    }

    @Test
    void memberIsDirectedToOwnProjectsInsteadOfApplications() {
        Map<String, Object> result = service.executeAction("view_my_projects", Map.of(), user("MEMBER"));

        assertEquals(true, result.get("success"));
        assertEquals("redirect", result.get("type"));
        assertEquals("/project?action=myProjects", result.get("redirectUrl"));
    }

    @Test
    void adminCanAlsoOpenOwnProjects() {
        Map<String, Object> result = service.executeAction("view_my_projects", Map.of(), user("ADMIN"));

        assertEquals(true, result.get("success"));
        assertEquals("/project?action=myProjects", result.get("redirectUrl"));
    }

    @Test
    void unauthenticatedRequestDoesNotReceiveAProjectRedirect() {
        Map<String, Object> result = service.executeAction("view_my_projects", Map.of(), null);

        assertFalse((Boolean) result.get("success"));
        assertNull(result.get("redirectUrl"));
    }

    private User user(String role) {
        User user = new User();
        user.setId(7);
        user.setRole(role);
        return user;
    }
}
