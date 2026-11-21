package servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

class Func05ProjectPaginationPageIT extends TomcatTestBase {

    @Test
    void projectPagesAndApisUseRequestedPagesAndExposeMatchingTotals() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            for (int number = 1; number <= 25; number++) {
                int id = 9000 + number;
                stmt.execute("INSERT INTO project (id,name,year,leader_id,status,created_at,deleted) " +
                        "VALUES (" + id + ",'分页项目" + number + "',2026,2,'approved'," +
                        "'2026-01-01 10:00:00',0)");
            }
        }

        Map<String, String> adminCookies = loginWithCsrf("admin");
        HttpRequest.Response adminPage = HttpRequest.get(baseUrl() + "/project")
                .param("action", "manage").param("page", "2").param("pageSize", "10")
                .param("keyword", "分页项目").param("status", "approved")
                .withCookies(adminCookies).execute();
        assertEquals(200, adminPage.code(), adminPage.location() + " " + adminPage.body());
        assertTrue(adminPage.body().contains("分页项目15"));
        assertFalse(adminPage.body().contains("分页项目25"));
        assertTrue(adminPage.body().contains("第 2 / 3 页，共 25 条"));
        assertTrue(adminPage.body().contains("上一页"));
        assertTrue(adminPage.body().contains("下一页"));
        Matcher nextPage = Pattern.compile("href=\"([^\"]*action=manage[^\"]*page=3)\"")
                .matcher(adminPage.body());
        assertTrue(nextPage.find(), "管理端缺少下一页链接");
        assertTrue(nextPage.group(1).contains("keyword="));
        assertTrue(nextPage.group(1).contains("status=approved"));
        assertTrue(nextPage.group(1).contains("pageSize=10"));

        HttpRequest.Response adminApi = HttpRequest.get(baseUrl() + "/api/projects")
                .param("page", "2").param("pageSize", "10")
                .withCookies(adminCookies).execute();
        assertEquals(200, adminApi.code(), adminApi.body());
        JsonObject adminData = JsonParser.parseString(adminApi.body()).getAsJsonObject()
                .getAsJsonObject("data");
        assertEquals(25, adminData.get("total").getAsInt());
        assertEquals(2, adminData.get("page").getAsInt());
        assertEquals(10, adminData.get("pageSize").getAsInt());
        assertEquals(3, adminData.get("totalPages").getAsInt());
        assertEquals(10, adminData.getAsJsonArray("list").size());

        Map<String, String> memberCookies = loginWithCsrf("member1");
        HttpRequest.Response memberPage = HttpRequest.get(baseUrl() + "/project")
                .param("action", "myProjects").param("page", "3").param("pageSize", "10")
                .withCookies(memberCookies).execute();
        assertEquals(200, memberPage.code(), memberPage.location() + " " + memberPage.body());
        assertTrue(memberPage.body().contains("分页项目5"));
        assertFalse(memberPage.body().contains("分页项目15"));
        assertTrue(memberPage.body().contains("第 3 / 3 页，共 25 条"));
        assertTrue(memberPage.body().contains("action=myProjects"));

        HttpRequest.Response myApi = HttpRequest.get(baseUrl() + "/api/projects/my")
                .param("page", "3").param("pageSize", "10")
                .withCookies(memberCookies).execute();
        assertEquals(200, myApi.code(), myApi.body());
        JsonObject myData = JsonParser.parseString(myApi.body()).getAsJsonObject()
                .getAsJsonObject("data");
        assertEquals(25, myData.get("total").getAsInt());
        assertEquals(3, myData.get("page").getAsInt());
        assertEquals(5, myData.getAsJsonArray("list").size());
    }

    private Map<String, String> loginWithCsrf(String username) throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        assertEquals(200, loginPage.code(), loginPage.body());
        Matcher token = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"")
                .matcher(loginPage.body());
        assertTrue(token.find());
        assertFalse(loginPage.cookies().isEmpty());
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
