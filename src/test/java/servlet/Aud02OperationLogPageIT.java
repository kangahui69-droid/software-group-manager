package servlet;

import dao.OperationLogDAO;
import model.OperationLog;
import org.junit.jupiter.api.Test;
import support.HttpRequest;
import support.TomcatTestBase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud02OperationLogPageIT extends TomcatTestBase {

    @Test
    void adminCanInspectResultTimingAndCorrelationFields() throws Exception {
        OperationLog log = new OperationLog();
        log.setUsername("aud02-page");
        log.setOperation("POST");
        log.setModule("项目");
        log.setDescription("POST 更新项目");
        log.setResult("FAILED");
        log.setHttpStatus(409);
        log.setDurationMs(18L);
        log.setRequestId("18b8ad79-8353-453b-9665-d55a8206464a");
        log.setErrorCode("HTTP_409");
        log.setTargetType("PROJECT");
        log.setTargetId("42");
        assertTrue(new OperationLogDAO().insert(log));

        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        Matcher csrf = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"")
                .matcher(loginPage.body());
        assertTrue(csrf.find());
        HttpRequest.Response auth = HttpRequest.post(baseUrl() + "/login")
                .withCookies(loginPage.cookies())
                .param("username", "admin").param("password", "admin123")
                .param("_csrf", csrf.group(1)).execute();
        assertEquals(302, auth.code());
        Map<String, String> cookies = new HashMap<>(loginPage.cookies());
        cookies.putAll(auth.cookies());

        HttpRequest.Response page = HttpRequest.get(baseUrl() + "/admin/log")
                .param("keyword", "aud02-page").withCookies(cookies).execute();
        assertEquals(200, page.code(), page.body());
        assertTrue(page.body().contains("FAILED"));
        assertTrue(page.body().contains("HTTP 409"));
        assertTrue(page.body().contains("HTTP_409"));
        assertTrue(page.body().contains("18 ms"));
        assertTrue(page.body().contains(log.getRequestId()));
        assertTrue(page.body().contains("PROJECT"));
        assertTrue(page.body().contains("#42"));
    }
}
