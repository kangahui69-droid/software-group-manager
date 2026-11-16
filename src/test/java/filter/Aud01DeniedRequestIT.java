package filter;

import dao.OperationLogDAO;
import model.OperationLog;
import org.junit.jupiter.api.Test;
import support.HttpRequest;
import support.TomcatTestBase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud01DeniedRequestIT extends TomcatTestBase {

    @Test
    void authenticationFilterRejectionIsLoggedAsDenied() throws Exception {
        HttpRequest.Response response = HttpRequest.post(baseUrl() + "/project")
                .param("action", "delete").param("id", "42").execute();
        assertEquals(302, response.code());
        assertTrue(response.location().endsWith("/login.jsp"));

        List<OperationLog> logs = new OperationLogDAO()
                .findByConditions(null, "POST", "项目", null, 1, 20);
        assertEquals(1, logs.size());
        OperationLog log = logs.get(0);
        assertEquals("DENIED", log.getResult());
        assertTrue(log.getDescription().startsWith("POST 删除项目"));
        assertEquals(302, log.getHttpStatus());
        assertEquals("AUTH_REQUIRED", log.getErrorCode());
        assertEquals("PROJECT", log.getTargetType());
        assertEquals("42", log.getTargetId());
        assertEquals(response.header("X-Request-ID"), log.getRequestId());
    }
}
