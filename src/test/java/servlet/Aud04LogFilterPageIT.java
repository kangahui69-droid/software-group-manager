package servlet;

import org.junit.jupiter.api.Test;
import support.H2Database;
import support.HttpRequest;
import support.TomcatTestBase;
import util.DBUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud04LogFilterPageIT extends TomcatTestBase {
    @Test
    void pageOffersActualWriterValuesAndFiltersByBeijingTime() throws Exception {
        H2Database.reset();
        try (Connection conn = DBUtil.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("INSERT INTO operation_log (username, operation, module, created_at) VALUES "
                    + "('aud04-visible', 'PATCH', '成员', '2026-11-16 10:30:00'), "
                    + "('aud04-hidden', 'POST', '管理员', '2026-11-16 11:00:00')");
        }

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
                .param("startTime", "2026-11-16T10:00")
                .param("endTime", "2026-11-16T11:00")
                .withCookies(cookies).execute();
        assertEquals(200, page.code(), page.body());
        assertTrue(page.body().contains("aud04-visible"));
        assertFalse(page.body().contains("aud04-hidden"));
        assertTrue(page.body().contains("<option value=\"PATCH\""));
        assertTrue(page.body().contains("<option value=\"GET\""));
        assertTrue(page.body().contains("<option value=\"管理员\""));
        assertTrue(page.body().contains("<option value=\"成员\""));
        assertTrue(page.body().contains("<option value=\"招新\""));
        assertFalse(page.body().contains("<option value=\"用户\""));
        assertTrue(page.body().contains("navigateToLogPage"));
    }
}
