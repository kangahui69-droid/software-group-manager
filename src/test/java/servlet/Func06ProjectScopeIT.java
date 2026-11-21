package servlet;

import com.google.gson.JsonArray;
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

class Func06ProjectScopeIT extends TomcatTestBase {

    @Test
    void myProjectsIncludesParticipationButCreatedByMeOnlyIncludesLeaderProjects() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project (id,name,year,leader_id,admin_id,status,created_at,deleted) VALUES " +
                    "(9101,'本人负责',2026,2,2,'approved','2026-02-01 10:00:00',0)," +
                    "(9102,'仅任管理员',2026,1,2,'approved','2026-02-02 10:00:00',0)," +
                    "(9103,'仅参与成员',2026,1,1,'approved','2026-02-03 10:00:00',0)," +
                    "(9104,'本人负责已删除',2026,2,2,'approved','2026-02-04 10:00:00',1)");
            stmt.execute("INSERT INTO project_member (project_id,user_id,role) VALUES (9103,2,'MEMBER')");
        }

        Map<String, String> cookies = loginWithCsrf("member1");
        JsonObject mine = getPage("/api/projects/my", cookies, 1, 2);
        assertEquals(3, mine.get("total").getAsInt());
        assertEquals(2, mine.get("totalPages").getAsInt());
        assertEquals(2, mine.getAsJsonArray("list").size());
        assertEquals(9103, mine.getAsJsonArray("list").get(0).getAsJsonObject().get("id").getAsInt());

        JsonObject mineLastPage = getPage("/api/projects/my", cookies, 2, 2);
        assertEquals(9101, mineLastPage.getAsJsonArray("list").get(0).getAsJsonObject().get("id").getAsInt());

        JsonObject created = getPage("/api/projects/created-by-me", cookies, 1, 2);
        JsonArray createdList = created.getAsJsonArray("list");
        assertEquals(1, created.get("total").getAsInt());
        assertEquals(1, created.get("totalPages").getAsInt());
        assertEquals(1, createdList.size());
        assertEquals(9101, createdList.get(0).getAsJsonObject().get("id").getAsInt());
    }

    private JsonObject getPage(String path, Map<String, String> cookies, int page, int pageSize) throws Exception {
        HttpRequest.Response response = HttpRequest.get(baseUrl() + path)
                .param("page", String.valueOf(page))
                .param("pageSize", String.valueOf(pageSize))
                .withCookies(cookies).execute();
        assertEquals(200, response.code(), response.body());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        assertEquals(0, json.get("code").getAsInt(), response.body());
        return json.getAsJsonObject("data");
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
