package servlet;

import org.junit.jupiter.api.BeforeEach;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;
import util.DBUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServletIT extends TomcatTestBase {
    private static final Pattern CSRF_TOKEN = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"");

    @BeforeEach
    void resetData() throws Exception {
        H2Database.reset();
        try (Connection connection = DBUtil.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE user SET must_change_password = 1 WHERE username = 'member1'");
        }
    }

    @IntegrationTest
    void forcedPasswordChangeRequiresCsrfAndAcceptsValidPassword() throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        assertThat(loginPage.code()).isEqualTo(200);
        HttpRequest.Response loginResponse = HttpRequest.post(baseUrl() + "/login")
                .withCookies(loginPage.cookies())
                .param("_csrf", csrfToken(loginPage.body()))
                .param("username", "member1")
                .param("password", "admin123")
                .execute();
        assertThat(loginResponse.code()).isEqualTo(302);
        assertThat(loginResponse.location()).contains("/member/password-change.jsp?mustChange=1");

        Map<String, String> cookies = new HashMap<>(loginPage.cookies());
        cookies.putAll(loginResponse.cookies());
        HttpRequest.Response passwordPage = HttpRequest.get(baseUrl() + "/member/password-change.jsp")
                .withCookies(cookies).execute();
        assertThat(passwordPage.code()).isEqualTo(200);
        String token = csrfToken(passwordPage.body());
        assertThat(passwordPage.body()).contains("name=\"_csrf\" value=\"" + token + "\"");

        HttpRequest.Response missingToken = HttpRequest.post(baseUrl() + "/member/password")
                .withCookies(cookies)
                .param("oldPassword", "admin123")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "newpass123")
                .execute();
        assertThat(missingToken.code()).isEqualTo(403);

        HttpRequest.Response wrongOldPassword = HttpRequest.post(baseUrl() + "/member/password")
                .withCookies(cookies)
                .param("_csrf", token)
                .param("oldPassword", "wrong")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "newpass123")
                .execute();
        assertThat(wrongOldPassword.code()).isEqualTo(200);
        assertThat(wrongOldPassword.body()).contains("原密码错误");

        HttpRequest.Response changed = HttpRequest.post(baseUrl() + "/member/password")
                .withCookies(cookies)
                .param("_csrf", token)
                .param("oldPassword", "admin123")
                .param("newPassword", "newpass123")
                .param("confirmPassword", "newpass123")
                .execute();
        assertThat(changed.code()).isEqualTo(302);
        assertThat(changed.location()).contains("/login.jsp?message=");

        HttpRequest.Response nextLoginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        HttpRequest.Response nextLogin = HttpRequest.post(baseUrl() + "/login")
                .withCookies(nextLoginPage.cookies())
                .param("_csrf", csrfToken(nextLoginPage.body()))
                .param("username", "member1")
                .param("password", "newpass123")
                .execute();
        assertThat(nextLogin.code()).isEqualTo(302);
        assertThat(nextLogin.location()).contains("/index.jsp");
    }

    private static String csrfToken(String html) {
        Matcher matcher = CSRF_TOKEN.matcher(html);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
