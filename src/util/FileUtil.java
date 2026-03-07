package util;

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

