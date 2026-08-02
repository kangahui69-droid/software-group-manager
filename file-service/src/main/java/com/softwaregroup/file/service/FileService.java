package com.softwaregroup.file.service;

import com.softwaregroup.file.dao.FileStorageDAO;
import com.softwaregroup.file.model.dto.FileInfo;
import com.softwaregroup.file.model.entity.FileStorage;
import com.softwaregroup.common.util.Result;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件服务层
 */
@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L; // 100MB
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String DEFAULT_CATEGORY = "general";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;

    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    @Autowired
    private FileStorageDAO fileStorageDAO;

    @Autowired
    private MinioClient minioClient;

    @Value("${spring.minio.bucket}")
    private String bucket;

    /**
     * 上传文件到MinIO
     */
    public Result uploadFile(MultipartFile file, String category, Integer userId) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();

        // 验证文件名
        if (isBlank(fileName) || fileName.trim().isEmpty()) {
            return Result.error(400, "文件名不能为空");
        }

        if (size <= 0) {
            return Result.error(400, "文件不能为空");
        }

        if (size > MAX_FILE_SIZE) {
            return Result.error(400, "文件大小不能超过100MB");
        }

        if (fileName.length() > MAX_FILENAME_LENGTH) {
            return Result.error(400, "文件名不能超过255字符");
        }

        // 安全检查
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\") || fileName.contains("\0")) {
            return Result.error(400, "文件名不能包含路径遍历字符");
        }

        // 验证文件类型
        if (!isAllowedContentType(contentType)) {
            return Result.error(400, "不支持的文件类型");
        }

        String normalizedCategory = normalizeCategory(category);
        String storedName = generateStoredName(fileName);
        String objectName = normalizedCategory + "/" + storedName;

        try {
            // 上传到MinIO
            byte[] bytes = file.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(bais, bytes.length, -1)
                            .contentType(contentType != null ? contentType : DEFAULT_CONTENT_TYPE)
                            .build()
            );

            // 保存元数据到数据库
            FileStorage fileStorage = new FileStorage();
            fileStorage.setCreateBy(userId);
            fileStorage.setOriginalName(fileName);
            fileStorage.setStoredName(storedName);
            fileStorage.setFilePath(objectName); // MinIO的路径就是objectName
            fileStorage.setFileType(contentType != null ? contentType : DEFAULT_CONTENT_TYPE);
            fileStorage.setFileSize(size);
            fileStorage.setCategory(normalizedCategory);
            fileStorage.setStatus(STATUS_NORMAL);

            Integer fileId = fileStorageDAO.insert(fileStorage);
            if (fileId == null || fileId <= 0) {
                // 回滚：删除已上传的文件
                try {
                    minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
                } catch (Exception e) {
                    log.error("回滚MinIO文件失败: {}", objectName, e);
                }
                return Result.error(500, "上传失败");
            }

            fileStorage.setId(fileId);
            return Result.ok(fileStorage);

        } catch (Exception e) {
            log.error("上传文件失败", e);
            return Result.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 查看文件（获取文件元信息）
     */
    public Result viewFile(Integer fileId) {
        if (fileId == null || fileId <= 0) {
            return Result.error(400, "文件ID不能为空");
        }

        FileStorage file = fileStorageDAO.findById(fileId);
        if (file == null) {
            return Result.error(404, "文件不存在");
        }

        if (file.getStatus() != null && file.getStatus() == STATUS_DELETED) {
            return Result.error(404, "文件不存在");
        }

        return Result.ok(file);
    }

    /**
     * 获取文件下载URL（预签名URL，有效期1小时）
     */
    public Result getDownloadUrl(Integer fileId) {
        if (fileId == null || fileId <= 0) {
            return Result.error(400, "文件ID不能为空");
        }

        FileStorage file = fileStorageDAO.findById(fileId);
        if (file == null) {
            return Result.error(404, "文件不存在");
        }

        if (file.getStatus() != null && file.getStatus() == STATUS_DELETED) {
            return Result.error(404, "文件不存在");
        }

        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(file.getFilePath())
                            .expiry(3600) // 1小时
                            .build()
            );
            return Result.ok(java.util.Map.of(
                    "url", url,
                    "fileId", fileId,
                    "originalName", file.getOriginalName()
            ));
        } catch (Exception e) {
            log.error("获取下载URL失败", e);
            return Result.error(500, "获取下载URL失败");
        }
    }

    /**
     * 获取文件流
     */
    public Result getFileStream(Integer fileId) {
        if (fileId == null || fileId <= 0) {
            return Result.error(400, "文件ID不能为空");
        }

        FileStorage file = fileStorageDAO.findById(fileId);
        if (file == null) {
            return Result.error(404, "文件不存在");
        }

        if (file.getStatus() != null && file.getStatus() == STATUS_DELETED) {
            return Result.error(404, "文件不存在");
        }

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getFilePath())
                            .build()
            );
            return Result.ok(java.util.Map.of(
                    "stream", stream,
                    "fileType", file.getFileType(),
                    "fileSize", file.getFileSize(),
                    "originalName", file.getOriginalName()
            ));
        } catch (Exception e) {
            log.error("获取文件流失败", e);
            return Result.error(500, "获取文件失败");
        }
    }

    /**
     * 删除文件（软删除 + 从MinIO删除）
     */
    public Result deleteFile(Integer fileId, Integer userId) {
        if (fileId == null) {
            return Result.error(400, "文件ID不能为空");
        }

        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        FileStorage file = fileStorageDAO.findById(fileId);
        if (file == null) {
            return Result.error(404, "文件不存在");
        }

        if (file.getStatus() != null && file.getStatus() == STATUS_DELETED) {
            return Result.error(404, "文件不存在");
        }

        try {
            // 从MinIO删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getFilePath())
                            .build()
            );

            // 软删除数据库记录
            fileStorageDAO.softDelete(fileId);
            return Result.ok();
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.error(500, "删除失败");
        }
    }

    /**
     * 列出用户的文件
     */
    public Result listFiles(String category, Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        try {
            var files = isBlank(category)
                    ? fileStorageDAO.findByCreateBy(userId)
                    : fileStorageDAO.findByCategory(category);

            var activeFiles = files.stream()
                    .filter(f -> f.getStatus() == null || f.getStatus() != STATUS_DELETED)
                    .collect(Collectors.toList());

            return Result.ok(activeFiles);
        } catch (Exception e) {
            log.error("列出文件失败", e);
            return Result.error(500, "系统错误");
        }
    }

    private String normalizeCategory(String category) {
        if (isBlank(category)) {
            return DEFAULT_CATEGORY;
        }
        String trimmed = category.trim();
        return trimmed.isEmpty() ? DEFAULT_CATEGORY : trimmed;
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return true;
        }
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String generateStoredName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        String extension = "";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            extension = fileName.substring(lastDot);
        }
        long timestamp = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return timestamp + "_" + uuid + extension;
    }
}
