package servlet;

import org.junit.jupiter.api.BeforeEach;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
