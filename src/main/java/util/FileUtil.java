package util;

import config.Config;

import java.io.File;
import java.util.UUID;

/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 生成存储文件名（时间戳_UUID.扩展名）
     */
    public static String generateStoredFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isEmpty()) {
            return null;
        }
        String extension = "";
        int lastDot = originalFileName.lastIndexOf(".");
        if (lastDot > 0) {
            extension = originalFileName.substring(lastDot);
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis();
        return timestamp + "_" + uuid + extension;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 确保目录存在
     */
    public static void ensureDirectoryExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 获取文件存储根目录绝对路径（外部路径，不受重新部署影响）
     */
    public static String getFileStorageBaseDir() {
        return Config.getFileStorageBaseDir();
    }

    /**
     * 根据相对路径解析出绝对物理路径（只读操作，不创建目录）
     * @param relativePath 以"/"开头或以localstorage开头的相对路径，如 "/localstorage/images/avatar/xxx.jpg"
     * @return 绝对物理路径
     */
    public static String resolvePhysicalPath(String relativePath) {
        String base = getFileStorageBaseDir();
        String rel = relativePath;
        if (rel.startsWith("/")) {
            rel = rel.substring(1);
        }
        if (rel.startsWith("localstorage/") || rel.startsWith("localstorage\\")) {
            rel = rel.substring("localstorage/".length());
        }
        File file = new File(base, rel);
        return file.getAbsolutePath();
    }

    /**
     * 获取分类子目录的绝对路径（自动创建目录，用于写入前）
     * @param categoryPath 分类相对路径，如 "images/avatar"、"images/award"、"files"
     * @return 该分类目录的绝对路径
     */
    public static String getCategoryDir(String categoryPath) {
        String base = getFileStorageBaseDir();
        File dir = new File(base, categoryPath);
        ensureDirectoryExists(dir.getAbsolutePath());
        return dir.getAbsolutePath();
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }
}
