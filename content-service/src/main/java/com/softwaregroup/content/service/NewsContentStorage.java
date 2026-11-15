package com.softwaregroup.content.service;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

/** Stores sanitized news HTML in the volume shared with the legacy WAR. */
@Component
public class NewsContentStorage {
    private final Path root;

    public NewsContentStorage(@Value("${NEWS_CONTENT_ROOT:/usr/local/tomcat/webapps/localstorage}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    public String save(String type, String html) throws IOException {
        if (html == null || html.isBlank() || html.getBytes(StandardCharsets.UTF_8).length > 1024 * 1024) {
            throw new IllegalArgumentException("新闻正文不能为空且不能超过 1 MB");
        }
        String category = type.toLowerCase(Locale.ROOT);
        if (!category.equals("award") && !category.equals("activity") && !category.equals("notice")) {
            throw new IllegalArgumentException("新闻类型无效");
        }
        String safeHtml = Jsoup.clean(html, Safelist.relaxed());
        if (safeHtml.isBlank()) {
            throw new IllegalArgumentException("新闻正文净化后为空");
        }
        String relative = "localstorage/news/" + category + "/" + UUID.randomUUID() + ".html";
        Path file = resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, safeHtml, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return relative;
    }

    public boolean exists(String relative) {
        try {
            return Files.isRegularFile(resolve(relative));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void delete(String relative) throws IOException {
        Files.deleteIfExists(resolve(relative));
    }

    public String read(String relative) throws IOException {
        return Files.readString(resolve(relative), StandardCharsets.UTF_8);
    }

    private Path resolve(String relative) {
        if (relative != null && relative.startsWith("/")) relative = relative.substring(1);
        if (relative == null || !relative.matches("^localstorage/news/(award|activity|notice)/[A-Za-z0-9_.-]+\\.html$")) {
            throw new IllegalArgumentException("新闻内容路径无效");
        }
        Path file = root.resolve(relative.substring("localstorage/".length())).normalize();
        if (!file.startsWith(root.resolve("news"))) {
            throw new IllegalArgumentException("新闻内容路径越界");
        }
        return file;
    }
}
