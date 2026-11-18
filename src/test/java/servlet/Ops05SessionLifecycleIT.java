package servlet;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import support.EmbeddedTomcat;
import support.HttpRequest;
import util.RedisUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Exercises the actual WAR filter chain and Tomcat's local session manager. */
class Ops05SessionLifecycleIT {
    private static final Pattern CSRF_TOKEN = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"");
    private static final String PROTECTED_PATH = "/admin/ops05-session-check";

    @Test
    void localSessionSurvivesRequestsButNotLogoutExpiryOrInstanceSwitch() throws Exception {
        EmbeddedTomcat first = new EmbeddedTomcat();
        EmbeddedTomcat second = new EmbeddedTomcat();
        try {
            first.start();
            second.start();
            String firstBase = first.getBaseUrl();
            String secondBase = second.getBaseUrl();

            assertRedirectedToLogin(firstBase, Map.of());
            HttpRequest.Response login = login(firstBase);
            String sessionId = login.sessionId();
            assertThat(sessionId).isNotBlank();
            Map<String, String> cookie = Map.of("JSESSIONID", sessionId);

            // An authenticated request reaches the missing resource (404) instead of the login redirect.
            assertThat(HttpRequest.get(firstBase + PROTECTED_PATH).withCookies(cookie).execute().code())
                    .isEqualTo(404);
            assertRedirectedToLogin(secondBase, cookie);
            assertThat(HttpRequest.get(firstBase + PROTECTED_PATH).withCookies(cookie).execute().code())
                    .isEqualTo(404);

            String csrfAfterLogin = csrfToken(firstBase, cookie);
            HttpRequest.Response logout = HttpRequest.post(firstBase + "/logout")
                    .withCookies(cookie).param("_csrf", csrfAfterLogin).execute();
            assertThat(logout.code()).isEqualTo(302);
            assertRedirectedToLogin(firstBase, cookie);

            HttpRequest.Response nextLogin = login(firstBase);
            Map<String, String> expiringCookie = Map.of("JSESSIONID", nextLogin.sessionId());
            first.setSessionTimeout(nextLogin.sessionId(), 1);
            Thread.sleep(1500);
            assertRedirectedToLogin(firstBase, expiringCookie);
        } finally {
            second.stop();
            first.stop();
        }
    }

    @Test
    void loginDoesNotNeedRedisWhenTheRedisConnectionFails() throws Exception {
        Field poolField = RedisUtil.class.getDeclaredField("jedisPool");
        poolField.setAccessible(true);
        JedisPool originalPool = (JedisPool) poolField.get(null);
        JedisPool unavailablePool = new JedisPool("127.0.0.1", 1);
        EmbeddedTomcat tomcat = new EmbeddedTomcat();
        try {
            poolField.set(null, unavailablePool);
            assertThatThrownBy(() -> {
                try (Jedis jedis = RedisUtil.getJedis()) {
                    jedis.ping();
                }
            }).isInstanceOf(redis.clients.jedis.exceptions.JedisConnectionException.class);

            tomcat.start();
            HttpRequest.Response login = login(tomcat.getBaseUrl());
            assertThat(HttpRequest.get(tomcat.getBaseUrl() + PROTECTED_PATH)
                    .cookie("JSESSIONID", login.sessionId()).execute().code()).isEqualTo(404);
        } finally {
            tomcat.stop();
            poolField.set(null, originalPool);
            unavailablePool.close();
        }
    }

    private static HttpRequest.Response login(String base) throws Exception {
        HttpRequest.Response csrf = HttpRequest.get(base + "/login.jsp").execute();
        assertThat(csrf.code()).isEqualTo(200);
        assertThat(csrf.sessionId()).isNotBlank();
        HttpRequest.Response login = HttpRequest.post(base + "/login")
                .cookie("JSESSIONID", csrf.sessionId())
                .param("_csrf", extractToken(csrf.body()))
                .param("username", "admin")
                .param("password", "admin123")
                .execute();
        assertThat(login.code()).isEqualTo(302);
        assertThat(login.location()).contains("/index.jsp");
        return login;
    }

    private static String csrfToken(String base, Map<String, String> cookie) throws Exception {
        HttpRequest.Response response = HttpRequest.get(base + "/login.jsp")
                .withCookies(cookie).execute();
        assertThat(response.code()).isEqualTo(200);
        return extractToken(response.body());
    }

    private static String extractToken(String body) {
        Matcher matcher = CSRF_TOKEN.matcher(body);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private static void assertRedirectedToLogin(String base, Map<String, String> cookie) throws Exception {
        HttpRequest.Response response = HttpRequest.get(base + PROTECTED_PATH)
                .withCookies(cookie).execute();
        assertThat(response.code()).isEqualTo(302);
        assertThat(response.location()).contains("/login.jsp");
    }
}
