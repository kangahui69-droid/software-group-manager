package dto;

import java.util.Date;

/**
 * 问题反馈数据传输对象
 */
public class ProblemDTO {
    private Integer id;
    private String title;
    private String content;
    private String reporterName;
    private String reporterContact;
    private String reporterType;
    private Integer userId;
    private String category;
    private String status;
    private String adminComment;
    private Integer handledBy;
    private Date handledAt;
    private Date createdAt;
    private Date updatedAt;

    // 分类常量
    public static final String CATEGORY_VERIFIED = "VERIFIED";
    public static final String CATEGORY_UNVERIFIED = "UNVERIFIED";
    public static final String CATEGORY_INVALID = "INVALID";

    // 状态常量
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SOLVING = "SOLVING";
    public static final String STATUS_SOLVED = "SOLVED";
    public static final String STATUS_UNSOLVED = "UNSOLVED";

    // 报告者类型常量
    public static final String REPORTER_TYPE_GUEST = "GUEST";
    public static final String REPORTER_TYPE_MEMBER = "MEMBER";
    public static final String REPORTER_TYPE_ADMIN = "ADMIN";

    public ProblemDTO() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterContact() {
        return reporterContact;
    }

    public void setReporterContact(String reporterContact) {
        this.reporterContact = reporterContact;
    }

    public String getReporterType() {
        return reporterType;
    }

    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public Integer getHandledBy() {
        return handledBy;
    }

    public void setHandledBy(Integer handledBy) {
        this.handledBy = handledBy;
    }

    public Date getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(Date handledAt) {
        this.handledAt = handledAt;
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