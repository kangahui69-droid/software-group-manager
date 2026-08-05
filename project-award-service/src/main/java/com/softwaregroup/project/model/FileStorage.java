package com.softwaregroup.project.model;

/**
 * 文件存储实体类
 */
public class FileStorage {
    private Integer id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Integer uploadedBy;
    private String category;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Integer uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
