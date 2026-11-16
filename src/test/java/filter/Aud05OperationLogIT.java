package filter;

import dao.OperationLogDAO;
import model.OperationLog;
import org.junit.jupiter.api.Test;
import support.H2Database;
import support.HttpRequest;
import support.TomcatTestBase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud05OperationLogIT extends TomcatTestBase {
    @Test
    void realHttpRequestDoesNotPersistSecretQueryValues() throws Exception {
        H2Database.reset();
        HttpRequest.post(baseUrl() + "/project?token=bearer-secret&password=top-secret"
                + "&email=a@example.com&phone=13800138000&action=update")
                .execute();

        OperationLog log = new OperationLogDAO().findAll(1, 20).get(0);
        assertTrue(log.getDescription().startsWith("POST "));
        String stored = log.getDescription() + log.getUsername() + log.getUserAgent()
                + log.getTargetId();
        assertFalse(stored.contains("bearer-secret"));
        assertFalse(stored.contains("top-secret"));
        assertFalse(stored.contains("a@example.com"));
        assertFalse(stored.contains("13800138000"));
    }

    @Test
    void loginRequestBodyPasswordIsNotWrittenToAuditRow() throws Exception {
        H2Database.reset();
        HttpRequest.post(baseUrl() + "/login")
                .param("username", "admin")
                .param("password", "super-secret-login-password")
                .execute();

        OperationLog log = new OperationLogDAO().findAll(1, 20).get(0);
        assertFalse(log.getDescription().contains("super-secret-login-password"));
        assertFalse(log.getDescription().contains("admin"));
    }
}
