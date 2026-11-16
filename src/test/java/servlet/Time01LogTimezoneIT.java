package servlet;

import dao.OperationLogDAO;
import model.OperationLog;
import org.junit.jupiter.api.Test;
import support.HttpRequest;
import support.TomcatTestBase;
import util.DBUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Time01LogTimezoneIT extends TomcatTestBase {

    @Test
    void logDatetimeRemainsBeijingTimeWhenJvmDefaultsToUtc() throws Exception {
        TimeZone original = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO operation_log " +
                        "(username, operation, module, created_at) VALUES " +
                        "('time01-marker', 'TEST', 'TIME', '2026-11-16 12:34:56')");
            }

            List<OperationLog> logs = new OperationLogDAO()
                    .findByConditions("time01-marker", null, null, null, 1, 20);
            assertEquals(1, logs.size());
            assertEquals(Instant.parse("2026-11-16T04:34:56Z"), logs.get(0).getCreatedAt().toInstant());

            HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
            Matcher csrf = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"")
                    .matcher(loginPage.body());
            assertTrue(csrf.find(), "Login page should contain a CSRF token");
            HttpRequest.Response auth = HttpRequest.post(baseUrl() + "/login")
                    .withCookies(loginPage.cookies())
                    .param("username", "admin").param("password", "admin123")
                    .param("_csrf", csrf.group(1)).execute();
            assertEquals(302, auth.code());
            Map<String, String> cookies = new HashMap<>(loginPage.cookies());
            cookies.putAll(auth.cookies());

            HttpRequest.Response page = HttpRequest.get(baseUrl() + "/admin/log")
                    .param("keyword", "time01-marker").withCookies(cookies).execute();
            assertEquals(200, page.code(), page.location() + " " + page.body());
            assertTrue(page.body().contains("time01-marker"));
            int marker = page.body().lastIndexOf("time01-marker");
            assertTrue(page.body().contains("2026-11-16 12:34:56"),
                    page.body().substring(marker, Math.min(page.body().length(), marker + 500)));
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
