package dto;

import java.util.Date;

/**
 * 新闻数据传输对象
 */
public class NewsDTO {
    private Integer id;
    private String title;
    private String type;
    private String content;
    private String summary;
    private Integer authorId;
    private Integer activityId;
    private Integer status;
    private Date createdAt;
    private Date updatedAt;

    // 枚举常量
    public static final Integer STATUS_PUBLISHED = 1;
    public static final Integer STATUS_DELETED = 0;

    public static final String TYPE_AWARD = "award";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_NOTICE = "notice";

    public NewsDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
