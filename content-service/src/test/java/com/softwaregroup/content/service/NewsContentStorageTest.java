package com.softwaregroup.content.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class NewsContentStorageTest {
    @TempDir Path root;

    @Test
    void storesSanitizedHtmlAndRejectsUntrustedPaths() throws Exception {
        NewsContentStorage storage = new NewsContentStorage(root.toString());
        String path = storage.save("NOTICE", "<p>正文</p><script>alert(1)</script>");

        assertThat(path).startsWith("localstorage/news/notice/");
        assertThat(storage.exists(path)).isTrue();
        assertThat(storage.read(path)).contains("正文").doesNotContain("<script>");
        assertThat(storage.exists("localstorage/news/../private.html")).isFalse();

        storage.delete(path);
        assertThat(storage.exists(path)).isFalse();
    }
}
