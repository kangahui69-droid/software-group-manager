package servlet;

import dao.MemberProfileDAO;
import dao.FileStorageDAO;
import dao.UserDAO;
import model.MemberProfile;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;
import util.DBUtil;
import util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class MemberProfileSaveIT extends TomcatTestBase {
    private static final Pattern CSRF = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"");

    @BeforeEach void resetDatabase() { H2Database.reset(); }

    @IntegrationTest void savesEveryEditableFieldAndRefreshesCurrentPage() throws Exception {
        String session = loginMember();
        String csrf = editorToken(session);
        Map<String, String> fields = fields(csrf);

        fields.put("blog", "not-a-url");
        HttpResult rejectedBlog = submit(session, fields);
        assertThat(rejectedBlog.status).isEqualTo(200);
        assertThat(rejectedBlog.body).contains("博客链接格式错误", "name=\"studentId\" value=\"20259999\"",
                "name=\"major\" value=\"网络工程\"", "value=\"not-a-url\"");
        assertThat(new MemberProfileDAO().findByUserId(2).getStudentId()).isEqualTo("2021001");
        fields.put("blog", "https://example.com/posts/1");

        HttpResult saved = submit(session, fields);
        assertThat(saved.status).isEqualTo(302);
        assertThat(saved.location).contains("/member/profile.jsp?saved=1");

        User user = new UserDAO().findById(2);
        MemberProfile profile = new MemberProfileDAO().findByUserId(2);
        assertThat(user.getName()).isEqualTo("新名字");
        assertThat(user.getPhone()).isEqualTo("13800138000");
        assertThat(user.getEmail()).isEqualTo("changed@example.com");
        assertThat(profile.getStudentId()).isEqualTo("20259999");
        assertThat(profile.getMajor()).isEqualTo("网络工程");
        assertThat(profile.getGrade()).isEqualTo("2025");
        assertThat(profile.getIntroduction()).isEqualTo("<p>简介</p>");
        assertThat(profile.getGithub()).isEqualTo("newmember");
        assertThat(profile.getBlog()).isEqualTo("https://example.com/posts/1");

        HttpRequest.Response display = HttpRequest.get(baseUrl() + "/member/profile.jsp?saved=1")
                .cookie("JSESSIONID", session).execute();
        assertThat(display.code()).isEqualTo(200);
        assertThat(display.body()).contains("个人资料已保存", "新名字", "changed@example.com", "20259999", "newmember");

        fields.put("name", "");
        HttpResult rejected = submit(session, fields);
        assertThat(rejected.status).isEqualTo(200);
        assertThat(rejected.body).contains("姓名不能为空", "name=\"studentId\" value=\"20259999\"");
        assertThat(new UserDAO().findById(2).getName()).isEqualTo("新名字");
    }

    @IntegrationTest void firstProfileSaveIncludesGithubAndBlog() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM member_profile WHERE user_id = 2");
        }
        String session = loginMember();
        HttpResult saved = submit(session, fields(editorToken(session)));

        assertThat(saved.status).isEqualTo(302);
        MemberProfile profile = new MemberProfileDAO().findByUserId(2);
        assertThat(profile).isNotNull();
        assertThat(profile.getGithub()).isEqualTo("newmember");
        assertThat(profile.getBlog()).isEqualTo("https://example.com/posts/1");
    }

    @IntegrationTest void savesDetailsAndUploadedAvatarTogether() throws Exception {
        String session = loginMember();
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/+fcAAAAASUVORK5CYII=");
        HttpResult saved = submit(session, fields(editorToken(session)), png);
        assertThat(saved.status).isEqualTo(302);

        MemberProfile profile = new MemberProfileDAO().findByUserId(2);
        assertThat(profile.getMajor()).isEqualTo("网络工程");
        assertThat(profile.getAvatarFileId()).isNotNull();
        model.FileStorage avatar = new FileStorageDAO().findById(profile.getAvatarFileId());
        assertThat(avatar).isNotNull();
        Path file = Path.of(FileUtil.resolvePhysicalPath(avatar.getFilePath()));
        try {
            assertThat(Files.isRegularFile(file)).isTrue();
            HttpRequest.Response page = HttpRequest.get(baseUrl() + "/member/profile.jsp")
                    .cookie("JSESSIONID", session).execute();
            assertThat(page.code()).isEqualTo(200);
            assertThat(page.body()).contains("/file?action=view&id=" + profile.getAvatarFileId());
            HttpURLConnection image = (HttpURLConnection) new URL(baseUrl()
                    + "/file?action=view&id=" + profile.getAvatarFileId()).openConnection();
            image.setRequestProperty("Cookie", "JSESSIONID=" + session);
            try {
                assertThat(image.getResponseCode()).isEqualTo(200);
                try (InputStream content = image.getInputStream()) {
                    assertThat(content.readAllBytes()).isEqualTo(png);
                }
            } finally {
                image.disconnect();
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @IntegrationTest void profileAndEditorReloadCurrentDataWhenSessionHasOlderProfile() throws Exception {
        String session = loginMember();
        HttpRequest.Response oldPage = HttpRequest.get(baseUrl() + "/member/profile.jsp")
                .cookie("JSESSIONID", session).execute();
        assertThat(oldPage.code()).isEqualTo(200);
        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE member_profile SET major = '最新专业' WHERE user_id = 2");
        }

        HttpRequest.Response profilePage = HttpRequest.get(baseUrl() + "/member/profile.jsp")
                .cookie("JSESSIONID", session).execute();
        assertThat(profilePage.body()).contains("最新专业");
        HttpRequest.Response editorPage = HttpRequest.get(baseUrl() + "/member/edit-profile.jsp")
                .cookie("JSESSIONID", session).execute();
        assertThat(editorPage.body()).contains("name=\"major\" value=\"最新专业\"");
    }

    private String loginMember() throws Exception {
        HttpRequest.Response page = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        Matcher matcher = CSRF.matcher(page.body());
        assertThat(matcher.find()).isTrue();
        HttpRequest.Response login = HttpRequest.post(baseUrl() + "/login").withCookies(page.cookies())
                .param("_csrf", matcher.group(1)).param("username", "member1")
                .param("password", "admin123").execute();
        assertThat(login.code()).isEqualTo(302);
        return login.sessionId();
    }

    private String editorToken(String session) throws Exception {
        HttpRequest.Response page = HttpRequest.get(baseUrl() + "/member/edit-profile.jsp")
                .cookie("JSESSIONID", session).execute();
        assertThat(page.code()).isEqualTo(200);
        Matcher matcher = CSRF.matcher(page.body());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private Map<String, String> fields(String csrf) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("_csrf", csrf);
        fields.put("name", "新名字");
        fields.put("phone", "13800138000");
        fields.put("email", "changed@example.com");
        fields.put("birthday", "2001-02-03");
        fields.put("studentId", "20259999");
        fields.put("major", "网络工程");
        fields.put("grade", "2025");
        fields.put("bio", "<p>简介</p>");
        fields.put("github", "newmember");
        fields.put("blog", "https://example.com/posts/1");
        return fields;
    }

    private HttpResult submit(String session, Map<String, String> fields) throws Exception {
        return submit(session, fields, null);
    }

    private HttpResult submit(String session, Map<String, String> fields, byte[] avatar) throws Exception {
        String boundary = "profile-" + UUID.randomUUID();
        ByteArrayOutputStream multipart = new ByteArrayOutputStream();
        for (Map.Entry<String, String> field : fields.entrySet()) {
            multipart.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\""
                    + field.getKey() + "\"\r\n\r\n" + field.getValue() + "\r\n")
                    .getBytes(StandardCharsets.UTF_8));
        }
        if (avatar != null) {
            multipart.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"avatar\"; "
                    + "filename=\"avatar.png\"\r\nContent-Type: image/png\r\n\r\n")
                    .getBytes(StandardCharsets.UTF_8));
            multipart.write(avatar);
            multipart.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        multipart.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        byte[] body = multipart.toByteArray();
        HttpURLConnection conn = (HttpURLConnection) new URL(baseUrl() + "/member/profile/update").openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Cookie", "JSESSIONID=" + session);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setFixedLengthStreamingMode(body.length);
        try (java.io.OutputStream out = conn.getOutputStream()) { out.write(body); }
        int status = conn.getResponseCode();
        String location = conn.getHeaderField("Location");
        InputStream stream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String responseBody;
        try (InputStream in = stream) {
            responseBody = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            conn.disconnect();
        }
        return new HttpResult(status, responseBody, location);
    }

    private static class HttpResult {
        final int status;
        final String body;
        final String location;
        HttpResult(int status, String body, String location) {
            this.status = status; this.body = body; this.location = location;
        }
    }
}
