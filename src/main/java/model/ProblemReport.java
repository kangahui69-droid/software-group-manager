package model;

import java.util.Date;

/**
 * 问题反馈实体类
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
    
    private String handlerName;
    private String userName;
    
    public ProblemReport() {
    }
    
    public ProblemReport(String title, String content, String reporterType) {
        this.title = title;
        this.content = content;
        this.reporterType = reporterType;
        this.category = CATEGORY_UNVERIFIED;
        this.status = STATUS_PENDING;
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
    
    public String getHandlerName() {
        return handlerName;
    }
    
    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getCategoryDisplayName() {
        switch (category) {
            case CATEGORY_VERIFIED: return "属实";
            case CATEGORY_UNVERIFIED: return "待验证";
            case CATEGORY_INVALID: return "不属实";
            default: return category;
        }
    }
    
    public String getStatusDisplayName() {
        switch (status) {
            case STATUS_PENDING: return "待处理";
            case STATUS_SOLVING: return "正在解决";
            case STATUS_SOLVED: return "已解决";
            case STATUS_UNSOLVED: return "未解决";
            default: return status;
        }
    }
    
    public String getReporterTypeDisplayName() {
        switch (reporterType) {
            case REPORTER_TYPE_GUEST: return "游客";
            case REPORTER_TYPE_MEMBER: return "成员";
            case REPORTER_TYPE_ADMIN: return "管理员";
            default: return reporterType;
        }
    }
}