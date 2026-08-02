package com.softwaregroup.file.controller;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.file.model.entity.FileStorage;
import com.softwaregroup.file.service.FileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件服务REST控制器
 *
 * 提供文件上传、下载、查看、删除等REST API
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private MinioClient minioClient;

    @Value("${spring.minio.bucket}")
    private String bucket;

    /**
     * 上传文件
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "category", defaultValue = "general") String category,
                         @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        // 如果没有传userId，返回错误
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return fileService.uploadFile(file, category, userId);
    }

    /**
     * 查看文件（元信息）
     * GET /api/files/{id}
     */
    @GetMapping("/{id}")
    public Result viewFile(@PathVariable("id") Integer id) {
        return fileService.viewFile(id);
    }

    /**
     * 下载文件
     * GET /api/files/{id}/download
     */
    @GetMapping("/{id}/download")
    public void downloadFile(@PathVariable("id") Integer id, HttpServletResponse response) {
        try {
            Result result = fileService.viewFile(id);
            if (!result.isSuccess()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            FileStorage file = (FileStorage) result.getData();
            response.setContentType(file.getFileType());
            response.setContentLengthLong(file.getFileSize());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getOriginalName() + "\"");

            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getFilePath())
                            .build()
            );

            try (OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 在线查看文件（流式输出）
     * GET /api/files/{id}/view
     */
    @GetMapping("/{id}/view")
    public void viewFileStream(@PathVariable("id") Integer id, HttpServletResponse response) {
        try {
            Result result = fileService.viewFile(id);
            if (!result.isSuccess()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            FileStorage file = (FileStorage) result.getData();
            response.setContentType(file.getFileType() != null ? file.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setContentLengthLong(file.getFileSize());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline");

            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getFilePath())
                            .build()
            );

            try (OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (Exception e) {
            log.error("查看文件失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取预签名下载URL
     * GET /api/files/{id}/url
     */
    @GetMapping("/{id}/url")
    public Result getDownloadUrl(@PathVariable("id") Integer id) {
        return fileService.getDownloadUrl(id);
    }

    /**
     * 删除文件
     * DELETE /api/files/{id}?userId=xxx
     */
    @DeleteMapping("/{id}")
    public Result deleteFile(@PathVariable("id") Integer id,
                              @RequestParam("userId") Integer userId) {
        return fileService.deleteFile(id, userId);
    }

    /**
     * 列出用户文件
     * GET /api/files?category=xxx&userId=xxx
     */
    @GetMapping
    public Result listFiles(@RequestParam(value = "category", required = false) String category,
                            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return fileService.listFiles(category, userId);
    }

    /**
     * 健康检查
     * GET /api/files/health
     */
    @GetMapping("/health")
    public Result health() {
        return Result.ok(java.util.Map.of("status", "UP", "service", "file-service"));
    }
}
