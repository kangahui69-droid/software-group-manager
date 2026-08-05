package com.softwaregroup.monitor.model;

/**
 * 问题反馈实体
 */
public class ProblemReport {

    public static final String CATEGORY_VERIFIED = "VERIFIED";
    public static final String CATEGORY_UNVERIFIED = "UNVERIFIED";
    public static final String CATEGORY_INVALID = "INVALID";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SOLVING = "SOLVING";
    public static final String STATUS_SOLVED = "SOLVED";
    public static final String STATUS_UNSOLVED = "UNSOLVED";

    public static final String REPORTER_TYPE_GUEST = "GUEST";
    public static final String REPORTER_TYPE_MEMBER = "MEMBER";
    public static final String REPORTER_TYPE_ADMIN = "ADMIN";

    private Integer id;
    private String title;
    private String content;
    private String category;
    private String status;
    private String reporterType;
    private Integer userId;
    private String reporterName;
    private String reporterContact;
    private String adminComment;
    private String createdAt;
    private String updatedAt;

    public ProblemReport() {
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

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
