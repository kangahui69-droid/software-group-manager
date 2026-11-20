package servlet;

import dao.StudySessionDAO;
import model.StudySession;
import org.junit.jupiter.api.BeforeEach;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class StudySessionServletIT extends TomcatTestBase {
    private static final Pattern CSRF_TOKEN = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"");

    @BeforeEach
    void resetData() {
        H2Database.reset();
    }

    @IntegrationTest
    void memberCanOpenStudyCenterAndRecords() throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        Matcher token = CSRF_TOKEN.matcher(loginPage.body());
        assertThat(token.find()).isTrue();
        HttpRequest.Response login = HttpRequest.post(baseUrl() + "/login")
                .withCookies(loginPage.cookies())
                .param("_csrf", token.group(1))
                .param("username", "member1")
                .param("password", "admin123")
                .execute();
        assertThat(login.code()).isEqualTo(302);

        Map<String, String> cookies = new HashMap<>(loginPage.cookies());
        cookies.putAll(login.cookies());
        HttpRequest.Response center = HttpRequest.get(baseUrl() + "/study")
                .withCookies(cookies).execute();
        assertThat(center.code()).isEqualTo(200);
        assertThat(center.body()).contains("学习中心").doesNotContain("获取学习状态失败");

        HttpRequest.Response records = HttpRequest.get(baseUrl() + "/study/list")
                .withCookies(cookies).execute();
        assertThat(records.code()).isEqualTo(200);
        assertThat(records.body()).contains("学习记录").doesNotContain("获取学习记录失败");
    }

    @IntegrationTest
    void studyTimerAndDurationAreConsistentForMemberAndAdmin() throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        Matcher token = CSRF_TOKEN.matcher(loginPage.body());
        assertThat(token.find()).isTrue();
        String csrf = token.group(1);
        HttpRequest.Response login = HttpRequest.post(baseUrl() + "/login")
                .withCookies(loginPage.cookies()).param("_csrf", csrf)
                .param("username", "member1").param("password", "admin123").execute();
        assertThat(login.code()).isEqualTo(302);
        Map<String, String> cookies = new HashMap<>(loginPage.cookies());
        cookies.putAll(login.cookies());
        HttpRequest.Response centerBeforeStart = HttpRequest.get(baseUrl() + "/study")
                .withCookies(cookies).execute();
        Matcher currentToken = CSRF_TOKEN.matcher(centerBeforeStart.body());
        assertThat(currentToken.find()).isTrue();
        csrf = currentToken.group(1);

        int hour = LocalTime.now(ZoneId.of("Asia/Shanghai")).getHour();
        if (hour >= 6 && hour < 22) {
            HttpRequest.Response started = HttpRequest.post(baseUrl() + "/study").withCookies(cookies)
                    .param("_csrf", csrf).param("action", "start").execute();
            assertThat(started.code()).isEqualTo(200);
            assertThat(started.body()).contains("\"success\":true");
        } else {
            StudySession session = new StudySession();
            session.setUserId(2);
            new StudySessionDAO().startStudy(session);
        }

        HttpRequest.Response activePage = HttpRequest.get(baseUrl() + "/study").withCookies(cookies).execute();
        assertThat(activePage.code()).isEqualTo(200);
        assertThat(activePage.body()).contains("initialElapsedSeconds", "结束学习")
                .doesNotContain("new Date('");

        HttpRequest.Response ended = HttpRequest.post(baseUrl() + "/study").withCookies(cookies)
                .param("_csrf", csrf).param("action", "end").execute();
        assertThat(ended.code()).isEqualTo(200);
        assertThat(ended.body()).contains("\"success\":true", "\"todayDuration\":")
                .doesNotContain("\"duration\":-");
        HttpRequest.Response records = HttpRequest.get(baseUrl() + "/study/list").withCookies(cookies).execute();
        assertThat(records.code()).isEqualTo(200);
        assertThat(records.body()).contains("已完成").doesNotContain("-958 分钟");

        HttpRequest.Response adminLoginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        Matcher adminToken = CSRF_TOKEN.matcher(adminLoginPage.body());
        assertThat(adminToken.find()).isTrue();
        HttpRequest.Response adminLogin = HttpRequest.post(baseUrl() + "/login")
                .withCookies(adminLoginPage.cookies()).param("_csrf", adminToken.group(1))
                .param("username", "admin").param("password", "admin123").execute();
        assertThat(adminLogin.code()).isEqualTo(302);
        HttpRequest.Response adminPage = HttpRequest.get(baseUrl() + "/attendance/manage")
                .withCookies(adminLogin.cookies()).execute();
        assertThat(adminPage.code()).isEqualTo(200);
        assertThat(adminPage.body()).contains("测试成员", "已完成");
        HttpRequest.Response legacyAdminPath = HttpRequest.get(baseUrl() + "/study/manage")
                .withCookies(adminLogin.cookies()).execute();
        assertThat(legacyAdminPath.code()).isEqualTo(302);
        assertThat(legacyAdminPath.location()).endsWith("/attendance/manage");
    }
}
