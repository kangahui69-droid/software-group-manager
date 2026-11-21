package servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.NewsDAO;
import dao.NewsAssetDAO;
import model.News;
import service.NewsMediaService;
import org.junit.jupiter.api.BeforeEach;
import support.H2Database;
import support.HttpRequest;
import support.IntegrationTest;
import support.TomcatTestBase;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import static org.assertj.core.api.Assertions.assertThat;

class NewsMediaServletIT extends TomcatTestBase {
    private static final Pattern CSRF = Pattern.compile("<meta name=\"csrf-token\" content=\"([^\"]+)\"");
    private static final Pattern MEDIA_TOKEN = Pattern.compile("name=\"mediaToken\" value=\"([^\"]+)\"");
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach void reset() { H2Database.reset(); }

    @IntegrationTest void memberCannotUploadNewsMedia() throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        String csrf = group(CSRF, loginPage.body());
        HttpRequest.Response member = HttpRequest.post(baseUrl() + "/login").withCookies(loginPage.cookies())
                .param("_csrf", csrf).param("username", "member1").param("password", "admin123").execute();
        assertThat(member.code()).isEqualTo(302);
        HttpRequest.Response memberNews = HttpRequest.get(baseUrl() + "/news")
                .cookie("JSESSIONID", member.sessionId()).execute();
        String memberCsrf = group(CSRF, memberNews.body());
        assertThat(HttpRequest.post(baseUrl() + "/news").cookie("JSESSIONID", member.sessionId())
                .param("_csrf", memberCsrf).param("action", "hardDelete").param("id", "1")
                .execute().code()).isEqualTo(403);
        HttpResult result = upload("picture.png", "IMAGE", new byte[]{1,2,3,4,5,6,7,8},
                member.sessionId(), csrf, UUID.randomUUID().toString());
        assertThat(result.code).isEqualTo(403);
    }

    @IntegrationTest void uploadAttachPublishAndProtectDraft() throws Exception {
        HttpRequest.Response loginPage = HttpRequest.get(baseUrl() + "/login.jsp").execute();
        String csrf = group(CSRF, loginPage.body());
        HttpRequest.Response login = HttpRequest.post(baseUrl() + "/login").withCookies(loginPage.cookies())
                .param("_csrf", csrf).param("username", "admin").param("password", "admin123").execute();
        assertThat(login.code()).isEqualTo(302);
        String session = login.sessionId();
        HttpRequest.Response editor = HttpRequest.get(baseUrl() + "/news?action=edit")
                .cookie("JSESSIONID", session).execute();
        assertThat(editor.code()).isEqualTo(200);
        String token = group(MEDIA_TOKEN, editor.body());
        csrf = group(CSRF, editor.body());

        byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/+fcAAAAASUVORK5CYII=");
        HttpResult bad = upload("fake.png", "IMAGE", new byte[]{1,2,3,4,5,6,7,8}, session, csrf, token);
        assertThat(bad.code).isEqualTo(400);
        JsonNode picture = json.readTree(upload("picture.png", "IMAGE", png, session, csrf, token).body);
        byte[] docx = officeZip("word/document.xml");
        JsonNode document = json.readTree(upload("slides.docx", "ATTACHMENT", docx, session, csrf, token).body);
        String imageUrl = picture.get("url").asText();
        int documentId = document.get("id").asInt();

        assertThat(HttpRequest.get("http://localhost:" + URI.create(baseUrl()).getPort() + imageUrl).execute().code()).isEqualTo(404);
        HttpRequest.Response created = HttpRequest.post(baseUrl() + "/news")
                .cookie("JSESSIONID", session).param("_csrf", csrf).param("action", "create")
                .param("mediaToken", token).param("title", "带附件的新闻").param("type", "notice")
                .param("summary", "摘要").param("status", "1")
                .param("content", "<p>正文<img src=\"" + imageUrl + "\"></p>")
                .param("attachmentIds", Integer.toString(documentId)).execute();
        assertThat(created.code()).isEqualTo(302);
        News article = new NewsDAO().findByConditions("带附件的新闻", null, 1).get(0);
        Path htmlFile = Path.of(util.FileUtil.resolvePhysicalPath("/" + article.getContentPath()));
        Path imageFile = new NewsMediaService().path(new NewsAssetDAO().find(picture.get("id").asInt()));
        assertThat(Files.isRegularFile(htmlFile)).isTrue();
        assertThat(Files.isRegularFile(imageFile)).isTrue();
        HttpRequest.Response detail = HttpRequest.get(baseUrl() + "/news?action=detail&id=" + article.getId()).execute();
        assertThat(detail.code()).isEqualTo(200);
        assertThat(detail.body()).contains(imageUrl, "slides.docx");
        assertThat(HttpRequest.get("http://localhost:" + URI.create(baseUrl()).getPort() + imageUrl).execute().code()).isEqualTo(200);
        HttpRequest.Response download = HttpRequest.get(baseUrl() + "/news/media/" + documentId + "/download").execute();
        assertThat(download.code()).isEqualTo(200);
        assertThat(download.header("Content-Disposition")).contains("attachment");

        HttpRequest.Response edit = HttpRequest.get(baseUrl() + "/news?action=edit&id=" + article.getId())
                .cookie("JSESSIONID", session).execute();
        String editToken = group(MEDIA_TOKEN, edit.body());
        JsonNode slide = json.readTree(upload("slides.pptx", "ATTACHMENT", officeZip("ppt/presentation.xml"),
                session, csrf, editToken).body);
        int slideId = slide.get("id").asInt();
        Path slideFile = new NewsMediaService().path(new NewsAssetDAO().find(slideId));
        HttpRequest.Response updated = HttpRequest.post(baseUrl() + "/news")
                .cookie("JSESSIONID", session).param("_csrf", csrf).param("action", "update")
                .param("id", article.getId().toString()).param("mediaToken", editToken)
                .param("title", "带附件的新闻").param("type", "notice").param("summary", "摘要")
                .param("status", "1").param("content", "<p>正文<img src=\"" + imageUrl + "\"></p>")
                .param("attachmentIds", Integer.toString(slideId)).execute();
        assertThat(updated.code()).isEqualTo(302);
        assertThat(HttpRequest.get(baseUrl() + "/news/media/" + documentId + "/download").execute().code()).isEqualTo(404);
        assertThat(HttpRequest.get(baseUrl() + "/news/media/" + slideId + "/download").execute().code()).isEqualTo(200);

        assertThat(HttpRequest.get(baseUrl() + "/news?action=hardDelete&id=" + article.getId())
                .cookie("JSESSIONID", session).execute().code()).isEqualTo(405);
        assertThat(HttpRequest.post(baseUrl() + "/news").cookie("JSESSIONID", session)
                .param("_csrf", csrf).param("action", "hardDelete").param("id", article.getId().toString())
                .execute().code()).isEqualTo(302);
        assertThat(new NewsDAO().findById(article.getId()).getStatus()).isEqualTo(1);

        HttpRequest.Response hidden = HttpRequest.post(baseUrl() + "/news").cookie("JSESSIONID", session)
                .param("_csrf", csrf).param("action", "delete").param("id", article.getId().toString()).execute();
        assertThat(hidden.code()).isEqualTo(302);
        assertThat(HttpRequest.get(baseUrl() + "/news?action=detail&id=" + article.getId()).execute().code()).isEqualTo(404);
        assertThat(HttpRequest.get("http://localhost:" + URI.create(baseUrl()).getPort() + imageUrl).execute().code()).isEqualTo(404);
        assertThat(HttpRequest.get(baseUrl() + "/news/media/" + slideId + "/download").execute().code()).isEqualTo(404);
        assertThat(Files.isRegularFile(htmlFile)).isTrue();

        HttpRequest.Response manage = HttpRequest.get(baseUrl() + "/news?action=manage&status=0")
                .cookie("JSESSIONID", session).execute();
        assertThat(manage.code()).isEqualTo(200);
        assertThat(manage.body()).contains("彻底删除", "name=\"action\" value=\"hardDelete\"");
        HttpRequest.Response purged = HttpRequest.post(baseUrl() + "/news")
                .cookie("JSESSIONID", session).param("_csrf", csrf).param("action", "hardDelete")
                .param("id", article.getId().toString()).execute();
        assertThat(purged.code()).isEqualTo(302);
        assertThat(purged.header("Location")).contains("purge=done");
        assertThat(new NewsDAO().findById(article.getId())).isNull();
        assertThat(new NewsAssetDAO().find(picture.get("id").asInt())).isNull();
        assertThat(new NewsAssetDAO().find(slideId)).isNull();
        assertThat(Files.exists(htmlFile)).isFalse();
        assertThat(Files.exists(imageFile)).isFalse();
        assertThat(Files.exists(slideFile)).isFalse();
    }

    private static String group(Pattern p, String body) {
        Matcher m = p.matcher(body);
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    private static byte[] officeZip(String main) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (String name : new String[]{"[Content_Types].xml", main}) {
                zip.putNextEntry(new ZipEntry(name)); zip.write("<xml/>".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private HttpResult upload(String name, String kind, byte[] data, String session, String csrf, String token) throws Exception {
        String boundary = "test-" + UUID.randomUUID();
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (String[] field : new String[][]{{"kind", kind}, {"token", token}}) {
            body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field[0] + "\"\r\n\r\n" + field[1] + "\r\n").getBytes(StandardCharsets.UTF_8));
        }
        body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" + name + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(data); body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        HttpURLConnection c = (HttpURLConnection) URI.create(baseUrl() + "/news/media/upload").toURL().openConnection();
        c.setRequestMethod("POST"); c.setDoOutput(true); c.setInstanceFollowRedirects(false);
        c.setRequestProperty("Cookie", "JSESSIONID=" + session); c.setRequestProperty("X-CSRF-TOKEN", csrf);
        c.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        try (OutputStream out = c.getOutputStream()) { out.write(body.toByteArray()); }
        int code = c.getResponseCode();
        try (InputStream in = code < 400 ? c.getInputStream() : c.getErrorStream()) {
            return new HttpResult(code, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } finally { c.disconnect(); }
    }

    private static class HttpResult {
        final int code; final String body;
        HttpResult(int code, String body) { this.code = code; this.body = body; }
    }
}
