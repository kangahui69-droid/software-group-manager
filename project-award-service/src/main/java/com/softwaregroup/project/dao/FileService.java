package com.softwaregroup.project.dao;

/**
 * 文件服务接口
 */
public interface FileService {
    String uploadFile(byte[] data, String fileName, String category);
    boolean deleteFile(String filePath);
}
