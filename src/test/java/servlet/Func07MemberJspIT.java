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

class Func07MemberJspIT extends TomcatTestBase {

    @Test
    void memberPagesRenderThroughServletWhileJspPathsCannotBeOpenedDirectly() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO project (id,name,year,leader_id,admin_id,status,deleted) " +
                    "VALUES (9201,'迁移验证项目',2026,2,2,'approved',0)");
            stmt.execute("INSERT INTO award (id,name,competition,year,created_by,award_status,deleted) VALUES " +
                    "(9301,'迁移验证奖项','比赛甲',2026,2,'APPROVED',0)," +
                    "(9302,'其他用户奖项','比赛乙',2026,1,'APPROVED',0)");
        }
        Map<String, String> cookies = loginWithCsrf("member1");

        assertPage(cookies, "/project", "myProjects", "迁移验证项目");
        assertPage(cookies, "/project", "createForm", "创建新项目");
        assertPage(cookies, "/project", "myApplications", "我的申请记录");
        HttpRequest.Response detail = HttpRequest.get(baseUrl() + "/project")
                .param("action", "detail").param("id", "9201")
                .withCookies(cookies).execute();
        assertEquals(200, detail.code(), detail.location() + " " + detail.body());
        assertTrue(detail.body().contains("迁移验证项目"));

        assertPage(cookies, "/award", "myAwards", "我的奖项");
        assertPage(cookies, "/award", "submit", "提交奖项");
        assertAwardPage(cookies, "detail", "9301", "比赛甲");
        assertAwardPage(cookies, "edit", "9301", "比赛甲");
        assertAwardPage(cookies, "addImage", "9301", "比赛甲");
        HttpRequest.Response forbiddenImageForm = HttpRequest.get(baseUrl() + "/award")
                .param("action", "addImage").param("id", "9302")
                .withCookies(cookies).execute();
        assertEquals(403, forbiddenImageForm.code());
        HttpRequest.Response problems = HttpRequest.get(baseUrl() + "/member/problem")
                .withCookies(cookies).execute();
        assertEquals(200, problems.code(), problems.location() + " " + problems.body());

        String[] removed = {
                "/member/project/create.jsp", "/member/project/detail.jsp",
                "/member/project/list.jsp", "/member/project/myApplications.jsp",
                "/member/award/add-image.jsp", "/member/award/detail.jsp",
                "/member/award/edit.jsp", "/member/award/list.jsp",
                "/member/award/submit.jsp", "/jsp/member/project/list.jsp",
                "/jsp/member/award/list.jsp", "/WEB-INF/jsp/member/project/list.jsp",
                "/WEB-INF/jsp/member/award/list.jsp"
        };
        for (String path : removed) {
            HttpRequest.Response response = HttpRequest.get(baseUrl() + path)
                    .withCookies(cookies).execute();
            assertTrue(response.code() >= 400 && response.code() < 500,
                    path + " must not render directly: " + response.code());
        }
    }

    private void assertPage(Map<String, String> cookies, String path, String action, String text) throws Exception {
        HttpRequest.Response response = HttpRequest.get(baseUrl() + path)
                .param("action", action).withCookies(cookies).execute();
        assertEquals(200, response.code(), action + " " + response.location() + " " + response.body());
        assertTrue(response.body().contains(text), action + " page content missing");
    }

    private void assertAwardPage(Map<String, String> cookies, String action, String id, String text) throws Exception {
        HttpRequest.Response response = HttpRequest.get(baseUrl() + "/award")
                .param("action", action).param("id", id).withCookies(cookies).execute();
        assertEquals(200, response.code(), action + " " + response.location() + " " + response.body());
        assertTrue(response.body().contains(text), action + " award page content missing");
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
