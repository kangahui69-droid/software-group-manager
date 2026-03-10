package model;

import java.util.Date;

/**
 * 奖项图片实体类
 */
public class AwardImage {
    private Integer id;
    private Integer awardId;
    private Integer memberId;
    private Boolean isMain;
    private String imagePath;
    private String originalName;
    private Date createdAt;
    private Integer fileStorageId;

    public AwardImage() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAwardId() {
        return awardId;
    }

    public void setAwardId(Integer awardId) {
        this.awardId = awardId;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    public Boolean getIsMain() {
        return isMain;
    }

    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getFileStorageId() {
        return fileStorageId;
    }

    public void setFileStorageId(Integer fileStorageId) {
        this.fileStorageId = fileStorageId;
    }
}
