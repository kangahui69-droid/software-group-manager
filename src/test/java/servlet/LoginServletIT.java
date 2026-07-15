package servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("登录流程集成测试")
class LoginServletIT extends TomcatTestBase {

    @BeforeEach
    void resetData() {
        H2Database.reset();
    }

    @IntegrationTest
    @DisplayName("正确的账号密码应登录成功并重定向到首页")
    void should_login_success_with_correct_credentials() throws Exception {
        HttpRequest.Response resp = HttpRequest.post(baseUrl() + "/login")
            .param("username", "admin")
            .param("password", "admin123")
            .execute();

        // 登录成功应该302重定向
        assertThat(resp.code()).isEqualTo(302);
        assertThat(resp.location()).contains("/index.jsp");
        // 应该下发JSESSIONID
        assertThat(resp.sessionId()).isNotNull();
    }

    @IntegrationTest
    @DisplayName("错误密码应返回登录页（转发，200）")
    void should_fail_with_wrong_password() throws Exception {
        HttpRequest.Response resp = HttpRequest.post(baseUrl() + "/login")
            .param("username", "admin")
            .param("password", "wrongpass")
            .execute();

        // 转发到login.jsp返回200（不是302）
        assertThat(resp.code()).isEqualTo(200);
    }

    @IntegrationTest
    @DisplayName("空用户名密码应返回登录页错误")
    void should_reject_empty_credentials() throws Exception {
        HttpRequest.Response resp = HttpRequest.post(baseUrl() + "/login")
            .param("username", "")
            .param("password", "")
            .execute();

        assertThat(resp.code()).isEqualTo(200);
    }
}
