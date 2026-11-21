package servlet;

import org.junit.jupiter.api.Test;
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

class Func04ProjectStatusPageIT extends TomcatTestBase {

    @Test
    void adminAndMemberProjectPagesRenderCanonicalStatuses() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project (id, name, year, leader_id, status, deleted) VALUES " +
                    "(801, '待审批项目', 2026, 2, 'pending', 0), " +
                    "(802, '已批准项目', 2026, 2, 'approved', 0)");
        }

        Map<String, String> adminCookies = loginWithCsrf("admin");
        HttpRequest.Response admin = HttpRequest.get(baseUrl() + "/project")
                .param("action", "manage").withCookies(adminCookies).execute();
        assertEquals(200, admin.code(), admin.location() + " " + admin.body());
        assertTrue(admin.body().contains("待审核"));
        assertTrue(admin.body().contains("已通过"));

        Map<String, String> memberCookies = loginWithCsrf("member1");
        HttpRequest.Response member = HttpRequest.get(baseUrl() + "/project")
                .param("action", "myProjects").withCookies(memberCookies).execute();
        assertEquals(200, member.code(), member.location() + " " + member.body());
        assertTrue(member.body().contains("待审批项目"));
        assertTrue(member.body().contains("已批准项目"));
    }

    @Test
    void adminEditPageDisplaysCurrentStatusWithoutUnprocessedStatusField() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project (id, name, year, leader_id, status, deleted) " +
                    "VALUES (803, '已批准项目', 2026, 2, 'approved', 0)");
        }
        Map<String, String> adminCookies = loginWithCsrf("admin");
        HttpRequest.Response edit = HttpRequest.get(baseUrl() + "/project")
                .param("action", "edit").param("id", "803")
                .withCookies(adminCookies).execute();

        assertEquals(200, edit.code(), edit.location() + " " + edit.body());
        assertTrue(edit.body().contains("已批准"));
        assertFalse(edit.body().contains("name=\"status\""));
    }

    private Map<String, String> loginWithCsrf(String username) throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        assertEquals(200, loginPage.code(), loginPage.body());
        Matcher token = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"")
                .matcher(loginPage.body());
        assertTrue(token.find(), "登录页未生成 CSRF 令牌");
        assertFalse(loginPage.cookies().isEmpty(), "登录页未返回会话 Cookie");

        HttpRequest.Response auth = HttpRequest.post(baseUrl() + "/login")
                .withCookies(loginPage.cookies())
                .param("username", username).param("password", "admin123")
                .param("_csrf", token.group(1)).execute();
        assertEquals(302, auth.code(), auth.body());
        Map<String, String> cookies = new HashMap<>(loginPage.cookies());
        cookies.putAll(auth.cookies());
        return cookies;
    }
}
