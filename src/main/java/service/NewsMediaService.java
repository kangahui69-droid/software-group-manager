package service;

import config.Config;
import dao.NewsAssetDAO;
import model.NewsAsset;

import jakarta.servlet.http.Part;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.zip.ZipFile;

/** Stores news media outside the public web root. */
public class NewsMediaService {
    private static final long IMAGE_LIMIT = 5L * 1024 * 1024;
    private static final long DOCUMENT_LIMIT = 50L * 1024 * 1024;
    private final NewsAssetDAO dao = new NewsAssetDAO();

    public Path root() throws IOException {
        String configured = Config.getProperty("news.mediaDir", "${user.dir}/.local-preview-data/news-media");
        if ("production".equalsIgnoreCase(System.getenv("APP_ENV")) &&
                (System.getenv("NEWS_MEDIA_DIR") == null || System.getenv("NEWS_MEDIA_DIR").trim().isEmpty())) {
            throw new IllegalStateException("NEWS_MEDIA_DIR is required in production");
        }
        Path root = Paths.get(configured.replace("${user.dir}", System.getProperty("user.dir"))).toAbsolutePath().normalize();
        Files.createDirectories(root);
        return root;
    }

    public Path path(NewsAsset asset) throws IOException {
        if (!asset.getStoredName().matches("[a-f0-9-]{36}\\.(jpg|png|webp|doc|docx|ppt|pptx)")) {
            throw new IOException("Invalid stored filename");
        }
        return root().resolve(asset.getStoredName());
    }

    public NewsAsset upload(Part part, String kind, int userId, String token) throws IOException {
        if (part == null || part.getSize() == 0 || part.getSize() > DOCUMENT_LIMIT ||
                token == null || !token.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("文件或上传令牌无效");
        String original = part.getSubmittedFileName();
        if (original == null) throw new IllegalArgumentException("文件名无效");
        original = original.replace('\\', '/');
        original = original.substring(original.lastIndexOf('/') + 1).trim();
        if (original.isEmpty() || original.length() > 255 || original.chars().anyMatch(c -> c < 32)) {
            throw new IllegalArgumentException("文件名无效");
        }
        String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        String mime = mimeFor(ext, kind);
        if ("IMAGE".equals(kind) && part.getSize() > IMAGE_LIMIT) throw new IllegalArgumentException("图片不能超过 5 MB");
        String stored = UUID.randomUUID() + "." + ext;
        Path target = root().resolve(stored);
        try {
            try (InputStream input = part.getInputStream()) { Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING); }
            verifySignature(target, ext);
            NewsAsset asset = new NewsAsset();
            asset.setUploadedBy(userId); asset.setUploadToken(token); asset.setKind(kind);
            asset.setOriginalName(original); asset.setStoredName(stored); asset.setMimeType(mime);
            asset.setFileSize(Files.size(target)); asset.setId(dao.insert(asset));
            try { cleanupExpired(); }
            catch (Exception e) { System.err.println("[NewsMediaService] 清理过期媒体失败: " + e.getMessage()); }
            return asset;
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(target);
            throw e;
        }
    }

    private void cleanupExpired() throws IOException {
        for (String name : dao.pruneUnlinkedBefore(Instant.now().minus(7, ChronoUnit.DAYS))) {
            NewsAsset old = new NewsAsset(); old.setStoredName(name);
            Files.deleteIfExists(path(old));
        }
    }

    private String mimeFor(String ext, String kind) {
        Map<String,String> images = Map.of("jpg", "image/jpeg", "png", "image/png", "webp", "image/webp");
        Map<String,String> docs = Map.of(
                "doc", "application/msword",
                "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "ppt", "application/vnd.ms-powerpoint",
                "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        String mime = ("IMAGE".equals(kind) ? images : "ATTACHMENT".equals(kind) ? docs : Map.<String,String>of()).get(ext);
        if (mime == null) throw new IllegalArgumentException("只支持 JPG、PNG、WebP 图片及 DOC、DOCX、PPT、PPTX 文档");
        return mime;
    }

    private void verifySignature(Path file, String ext) throws IOException {
        byte[] header = new byte[12];
        try (InputStream in = Files.newInputStream(file)) { if (in.read(header) < 8) throw new IllegalArgumentException("文件内容无效"); }
        boolean valid;
        switch (ext) {
            case "jpg": valid = (header[0] & 255) == 255 && (header[1] & 255) == 216 && (header[2] & 255) == 255; break;
            case "png": valid = Arrays.equals(Arrays.copyOf(header, 8), new byte[]{(byte)137,80,78,71,13,10,26,10}); break;
            case "webp": valid = new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP"); break;
            case "doc": case "ppt": valid = Arrays.equals(Arrays.copyOf(header, 8),
                    new byte[]{(byte)208,(byte)207,17,(byte)224,(byte)161,(byte)177,26,(byte)225}); break;
            case "docx": case "pptx":
                valid = header[0] == 'P' && header[1] == 'K';
                if (valid) {
                    try (ZipFile zip = new ZipFile(file.toFile())) {
                        valid = zip.getEntry("[Content_Types].xml") != null && zip.getEntry(ext.equals("docx") ? "word/document.xml" : "ppt/presentation.xml") != null;
                    } catch (java.util.zip.ZipException e) { valid = false; }
                }
                break;
            default: valid = false;
        }
        if (!valid) throw new IllegalArgumentException("文件格式与扩展名不匹配");
    }
}
