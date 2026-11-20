package servlet;

import dao.ActivityDAO;
import dao.NewsDAO;
import dao.PublicationNotificationDAO;
import model.News;
import org.junit.jupiter.api.BeforeEach;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class PublicationNotificationIT extends TomcatTestBase {
    private static final Pattern CSRF = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"");
    private final PublicationNotificationDAO notifications = new PublicationNotificationDAO();

    @BeforeEach void resetDatabase() { H2Database.reset(); }

    @IntegrationTest void newsPublicationReadAndRepublishArePerUser() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO user(username,password,name,email,role,status) " +
                    "VALUES ('teacher','x','Teacher','teacher@example.com','TEACHER',1)");
            stmt.executeUpdate("INSERT INTO user(username,password,name,email,role,status) " +
                    "VALUES ('disabled','x','Disabled','disabled@example.com','MEMBER',0)");
            stmt.executeUpdate("INSERT INTO user(username,password,name,email,role,status) " +
                    "VALUES ('guest','x','Guest','guest@example.com','GUEST',1)");
        }
        News article = new News();
        article.setTitle("新的通知公告");
        article.setType("notice");
        article.setContentPath("localstorage/news/notice/test.html");
        article.setAuthorId(1);
        article.setStatus(0);
        NewsDAO news = new NewsDAO();
        assertThat(news.insert(article)).isTrue();
        assertThat(notifications.unreadCount(2)).isZero();

        assertThat(news.updateStatus(article.getId(), 1)).isTrue();
        assertThat(notifications.unreadCount(2)).isEqualTo(1);
        assertThat(notifications.unreadCount(1)).isZero();
        assertThat(notifications.unreadCount(3)).isEqualTo(1);
        assertThat(notifications.unreadCount(4)).isZero();
        assertThat(notifications.unreadCount(5)).isEqualTo(1);
        assertThat(news.updateStatus(article.getId(), 1)).isTrue();
        assertThat(eventCount(article.getId())).isEqualTo(1);

        String session = login("member1");
        HttpRequest.Response list = HttpRequest.get(baseUrl() + "/notifications")
                .cookie("JSESSIONID", session).execute();
        assertThat(list.code()).isEqualTo(200);
        assertThat(list.body()).contains("新的通知公告", "未读", "/news?action=detail&amp;id=" + article.getId());
        String csrf = csrf(list.body());
        HttpRequest.Response read = HttpRequest.post(baseUrl() + "/notifications/read-source")
                .cookie("JSESSIONID", session).param("_csrf", csrf)
                .param("type", "NEWS").param("id", String.valueOf(article.getId())).execute();
        assertThat(read.code()).isEqualTo(204);
        assertThat(notifications.unreadCount(2)).isZero();
        assertThat(notifications.unreadCount(3)).isEqualTo(1);
        assertThat(HttpRequest.get(baseUrl() + "/notifications/count")
                .cookie("JSESSIONID", session).execute().body()).contains("\"unread\":0");

        assertThat(news.updateStatus(article.getId(), 0)).isTrue();
        assertThat(notifications.list(2, 10)).isEmpty();
        assertThat(news.updateStatus(article.getId(), 1)).isTrue();
        assertThat(eventCount(article.getId())).isEqualTo(2);
        assertThat(notifications.unreadCount(2)).isEqualTo(1);
        assertThat(notifications.list(2, 10)).hasSize(1);
    }

    @IntegrationTest void activityApprovalNotifiesOnceAndAdminCannotReadUserInbox() throws Exception {
        int activityId;
        try (Connection conn = DBUtil.getConnection(); PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO activity(name,status,approval_status,deleted,creator_id) " +
                "VALUES ('新活动','upcoming','pending',0,1)", Statement.RETURN_GENERATED_KEYS)) {
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) { keys.next(); activityId = keys.getInt(1); }
        }
        ActivityDAO activities = new ActivityDAO();
        assertThat(activities.approveActivity(activityId)).isTrue();
        assertThat(activities.approveActivity(activityId)).isFalse();
        assertThat(notifications.unreadCount(2)).isEqualTo(1);
        assertThat(notifications.list(2, 10).get(0).getTargetUrl())
                .isEqualTo("/activity?action=detail&id=" + activityId);

        String adminSession = login("admin");
        assertThat(HttpRequest.get(baseUrl() + "/notifications/count")
                .cookie("JSESSIONID", adminSession).execute().code()).isEqualTo(403);
    }

    @IntegrationTest void directPublishAndDraftEditCreateOnlyOneAlertPerPublication() {
        News article = new News();
        article.setTitle("直接发布的新闻");
        article.setType("award");
        article.setContentPath("localstorage/news/award/test.html");
        article.setAuthorId(1);
        article.setStatus(1);
        NewsDAO news = new NewsDAO();
        assertThat(news.insert(article)).isTrue();
        assertThat(notifications.unreadCount(2)).isEqualTo(1);

        article.setTitle("更正后的标题");
        assertThat(news.update(article)).isTrue();
        assertThat(notifications.unreadCount(2)).isEqualTo(1);
        assertThat(notifications.list(2, 10)).hasSize(1);

        article.setStatus(0);
        assertThat(news.update(article)).isTrue();
        assertThat(notifications.unreadCount(2)).isZero();
        article.setStatus(1);
        assertThat(news.update(article)).isTrue();
        assertThat(notifications.unreadCount(2)).isEqualTo(1);
        assertThat(notifications.list(2, 10).get(0).getTitle()).isEqualTo("更正后的标题");
    }

    private int eventCount(int sourceId) throws Exception {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement query = conn.prepareStatement(
                "SELECT COUNT(*) FROM notification_event WHERE source_type='NEWS' AND source_id=?")) {
            query.setInt(1, sourceId);
            try (ResultSet row = query.executeQuery()) { row.next(); return row.getInt(1); }
        }
    }

    private String login(String username) throws Exception {
        HttpRequest.Response page = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        HttpRequest.Response login = HttpRequest.post(baseUrl() + "/login").withCookies(page.cookies())
                .param("_csrf", csrf(page.body())).param("username", username)
                .param("password", "admin123").execute();
        assertThat(login.code()).isEqualTo(302);
        return login.sessionId();
    }

    private String csrf(String page) {
        Matcher token = CSRF.matcher(page);
        assertThat(token.find()).isTrue();
        return token.group(1);
    }
}
