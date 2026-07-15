package service;

import dao.FileStorageDAO;
import dao.UserDAO;
import dto.FileInfo;
import model.FileStorage;
import model.User;
import util.FileUtil;
import util.Result;
import util.TransactionTemplate;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件服务层
 *
 * 服务分层与API化重构计划.md 4.3 FileService 文件服务
 */
public class FileService {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024L; // 100MB
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final String DEFAULT_CATEGORY = "general";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;
    private static final String ADMIN_ROLE = "ADMIN";

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

    private final FileStorageDAO fileStorageDAO;
    private final UserDAO userDAO;

    public FileService(FileStorageDAO fileStorageDAO) {
        this.fileStorageDAO = fileStorageDAO;
        this.userDAO = new UserDAO();
    }

    public FileService(FileStorageDAO fileStorageDAO, UserDAO userDAO) {
        this.fileStorageDAO = fileStorageDAO;
        this.userDAO = userDAO;
    }

    /**
     * 上传文件
     *
     * @param file 文件信息（文件名、内容类型、大小）
     * @param category 分类路径
     * @param userId 创建者用户ID
     * @return 上传结果
     */
    public Result uploadFile(Object file, String category, Integer userId) {
        if (file == null) {
            return Result.error(400, "文件不能为空");
        }

        // 检查用户是否存在
        if (!isValidUser(userId)) {
            return Result.error(404, "用户不存在");
        }

        FileInfo fileInfo = extractFileInfo(file);
        if (fileInfo == null) {
            return Result.error(400, "文件信息无效");
        }

        String fileName = fileInfo.getFileName();
        String contentType = fileInfo.getContentType();
        long size = fileInfo.getSize();

        if (isBlank(fileName) || fileName.trim().isEmpty()) {
            return Result.error(400, "文件名不能为空");
        }

        if (fileName.trim().isEmpty()) {
            return Result.error(400, "文件名不能为空白");
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

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return Result.error(400, "文件名不能包含路径遍历字符");
        }

        if (fileName.contains("\0")) {
            return Result.error(400, "文件名不能包含特殊字符");
        }

        if (!isAllowedContentType(contentType)) {
            return Result.error(400, "不支持的文件类型");
        }

        String normalizedCategory = normalizeCategory(category);
        String storedName = generateStoredName(fileName);
        String filePath = "/localstorage/files/" + normalizedCategory + "/" + storedName;

        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(userId);
        fileStorage.setOriginalName(fileName);
        fileStorage.setStoredName(storedName);
        fileStorage.setFilePath(filePath);
        fileStorage.setFileType(contentType != null ? contentType : DEFAULT_CONTENT_TYPE);
        fileStorage.setFileSize(size);
        fileStorage.setCategory(normalizedCategory);
        fileStorage.setStatus(STATUS_NORMAL);

        try {
            Integer fileId = fileStorageDAO.insert(fileStorage);
            if (fileId == null || fileId <= 0) {
                return Result.error(500, "上传失败");
            }
            fileStorage.setId(fileId);
            return Result.ok(fileStorage);
        } catch (RuntimeException e) {
            return Result.error(500, "上传失败");
        }
    }

    /**
     * 查看文件（获取文件元信息）
     *
     * @param fileId 文件ID
     * @return 文件信息
     */
    public Result viewFile(Integer fileId) {
        if (fileId == null || fileId <= 0) {
            return Result.error(400, "文件ID不能为空");
        }

        try {
            FileStorage file = fileStorageDAO.findById(fileId);
            if (file == null) {
                return Result.error(404, "文件不存在");
            }

            if (file.getStatus() != null && file.getStatus() == STATUS_DELETED) {
                return Result.error(404, "文件不存在");
            }

            return Result.ok(file);
        } catch (RuntimeException e) {
            throw e; // 异常上抛，由TransactionTemplate处理
        }
    }

    /**
     * 下载文件（获取文件下载信息）
     *
     * @param fileId 文件ID
     * @return 下载信息（包含文件流路径和元数据）
     */
    public Result downloadFile(Integer fileId) {
        if (fileId == null) {
            return Result.error(400, "文件ID不能为空");
        }

        try {
            FileStorage file = fileStorageDAO.findById(fileId);
            if (file == null) {
                return Result.error(404, "文件不存在");
            }

            if (file.getStatus() != null && file.getStatus() == STATUS_DELETED) {
                return Result.error(404, "文件不存在");
            }

            // 检查物理文件是否存在
            String physicalPath = FileUtil.resolvePhysicalPath(file.getFilePath());
            if (physicalPath == null || !new File(physicalPath).exists()) {
                return Result.error(404, "文件不存在");
            }

            return Result.ok(file);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
    }

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 操作者用户ID
     * @return 删除结果
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

        // 检查权限：所有者或管理员可删除
        boolean isOwner = userId.equals(file.getCreateBy());
        boolean isAdmin = isAdminUser(userId);

        if (!isOwner && !isAdmin) {
            return Result.error(403, "没有权限删除该文件");
        }

        try {
            return TransactionTemplate.executeWithConnection(conn -> {
                fileStorageDAO.softDelete(fileId, conn);
                return Result.ok();
            });
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
    }

    /**
     * 检查用户是否为管理员
     */
    private boolean isAdminUser(Integer userId) {
        try {
            User user = userDAO.findById(userId);
            return user != null && ADMIN_ROLE.equals(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 列出文件
     *
     * @param category 分类（null或空表示用户所有文件）
     * @param userId 用户ID
     * @return 文件列表
     */
    public Result listFiles(String category, Integer userId) {
        try {
            List<FileStorage> files;
            if (isBlank(category)) {
                files = fileStorageDAO.findByCreateBy(userId);
            } else {
                files = fileStorageDAO.findByCategory(category);
            }

            if (files == null) {
                files = List.of();
            }

            files = files.stream()
                .filter(f -> f.getStatus() == null || f.getStatus() != STATUS_DELETED)
                .collect(Collectors.toList());

            return Result.ok(files);
        } catch (RuntimeException e) {
            return Result.error(500, "系统错误");
        }
    }

    /**
     * 从Object提取文件信息
     * 支持FileInfo类型、Map类型或具有getFileName/getContentType/getSize方法的类型
     */
    private FileInfo extractFileInfo(Object file) {
        if (file == null) {
            return null;
        }
        if (file instanceof FileInfo) {
            return (FileInfo) file;
        }
        if (file instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) file;
            FileInfo info = new FileInfo();
            info.setFileName(map.get("fileName") != null ? map.get("fileName").toString() : null);
            info.setContentType(map.get("contentType") != null ? map.get("contentType").toString() : null);
            Object size = map.get("size");
            if (size != null) {
                if (size instanceof Long) {
                    info.setSize((Long) size);
                } else if (size instanceof Integer) {
                    info.setSize(((Integer) size).longValue());
                } else if (size instanceof java.math.BigInteger) {
                    info.setSize(((java.math.BigInteger) size).longValue());
                }
            }
            return info;
        }
        // 尝试通过反射获取getter方法
        try {
            java.lang.reflect.Method getFileName = file.getClass().getMethod("getFileName");
            java.lang.reflect.Method getContentType = file.getClass().getMethod("getContentType");
            java.lang.reflect.Method getSize = file.getClass().getMethod("getSize");
            FileInfo info = new FileInfo();
            Object fileName = getFileName.invoke(file);
            Object contentType = getContentType.invoke(file);
            Object size = getSize.invoke(file);
            info.setFileName(fileName != null ? fileName.toString() : null);
            info.setContentType(contentType != null ? contentType.toString() : null);
            if (size != null) {
                if (size instanceof Long) {
                    info.setSize((Long) size);
                } else if (size instanceof Integer) {
                    info.setSize(((Integer) size).longValue());
                } else if (size instanceof java.math.BigInteger) {
                    info.setSize(((java.math.BigInteger) size).longValue());
                }
            }
            return info;
        } catch (Exception e) {
            // 反射失败，返回null
            return null;
        }
    }

    /**
     * 标准化分类路径
     */
    private String normalizeCategory(String category) {
        if (isBlank(category)) {
            return DEFAULT_CATEGORY;
        }
        String trimmed = category.trim();
        if (trimmed.isEmpty()) {
            return DEFAULT_CATEGORY;
        }
        return trimmed;
    }

    /**
     * 检查内容类型是否允许
     */
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

    /**
     * 判断字符串是否为空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查用户是否有效
     */
    private boolean isValidUser(Integer userId) {
        try {
            User user = userDAO.findById(userId);
            return user != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成存储文件名（无扩展名用原文件名）
     */
    private String generateStoredName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return FileUtil.generateStoredFileName(fileName);
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return FileUtil.generateStoredFileName(fileName);
        }
        // 无扩展名，返回原文件名（不带路径遍历字符）
        return fileName;
    }
}
