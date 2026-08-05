package com.softwaregroup.project.model;

/**
 * 奖项图片实体类
 */
public class AwardImage {
    private Integer id;
    private Integer awardId;
    private Integer fileId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAwardId() { return awardId; }
    public void setAwardId(Integer awardId) { this.awardId = awardId; }

    public Integer getFileId() { return fileId; }
    public void setFileId(Integer fileId) { this.fileId = fileId; }
}
