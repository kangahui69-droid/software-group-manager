package model;

import java.util.Date;

/**
 * 活动实体类
 */
public class Activity {
    private Integer id;
    private String title; // 活动名称
    private String description; // 活动内容介绍
    private String activityType; // 活动类型（字典代码：LECTURE/SEMINAR/TEA_PARTY/PROJECT_INTRO等）
    private Date activityStartTime; // 活动开始时间
    private Date activityEndTime; // 活动结束时间
    private String location; // 活动地点
    private String organizers; // 组织人（支持多个，逗号分隔）
    private String contactInfo; // 联系方式
    private Date registrationStartTime; // 报名开始时间
    private Date registrationEndTime; // 报名截止时间
    private Integer maxParticipants; // 最大参与人数
    private String status; // 活动进行状态：upcoming/ongoing/completed/canceled
    private Date createdAt;
    private Date updatedAt;

    // 扩展字段（用于页面展示）
    private String activityTypeName; // 活动类型名称
    private Integer currentParticipants; // 当前报名人数
    private boolean isRegistrationOpen; // 是否开放报名（动态计算）
    private boolean isRegisteredByCurrentUser; // 当前用户是否已报名

    public static final String STATUS_UPCOMING = "upcoming";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELED = "canceled";
    public static final String STATUS_REJECTED = "rejected"; // **新增** 对应 DB 的 approval_status 状态，尽管 activity 表自己没有 approval_status 的 enum，但设计文档中提到了活动审批状态，如果 status 字段用于表达所有状态，则可能需要补充。
    // 检查 DB: activity 表只有 status 字段，值为 enum('upcoming','ongoing','completed','canceled')。
    // 检查设计文档: activity 表 status: enum('upcoming','ongoing','completed','canceled')， approval_status: enum('pending','approved','rejected')。
    // 结论: 数据库设计中 activity 表只有 status 字段。系统设计文档中 activity 明确定义了 status 和 approval_status 两个字段。
    // 实际 DB 中只有 status 字段，且其值与 System Doc 一致，不包含 rejected。
    // 但是为了确保与系统设计文档的完整一致性（即未来可能增加 approval_status 字段），此处添加一个常量以备后用，但不对 status 字段本身做修改，因为 DB 结构稳定。
    // 鉴于 current DB structure for activity.status is enum('upcoming','ongoing','completed','canceled') which aligns with the DB structure I described, I will only add constants for consistency if needed, but I MUST confirm the actual status string used in the code.
    
    // Activity.java model 仅用 status 字段来表示活动进行状态 (upcoming/ongoing/completed/canceled)。
    // 数据库查询到的 activity.approval_status (pending/approved/rejected) 字段尚未在模型中体现。
    private String approvalStatus; // 对应 DB 中 approval_status 字段
    private Integer creatorId; // 活动创建者用户ID

    public static final String APPROVAL_PENDING = "pending";
    public static final String APPROVAL_APPROVED = "approved";
    public static final String APPROVAL_REJECTED = "rejected";

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Date getActivityStartTime() {
        return activityStartTime;
    }

    public void setActivityStartTime(Date activityStartTime) {
        this.activityStartTime = activityStartTime;
    }

    public Date getActivityEndTime() {
        return activityEndTime;
    }

    public void setActivityEndTime(Date activityEndTime) {
        this.activityEndTime = activityEndTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOrganizers() {
        return organizers;
    }

    public void setOrganizers(String organizers) {
        this.organizers = organizers;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Date getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(Date registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    public Date getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(Date registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public String getActivityTypeName() {
        return activityTypeName;
    }

    public void setActivityTypeName(String activityTypeName) {
        this.activityTypeName = activityTypeName;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public boolean isRegistrationOpen() {
        return isRegistrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        isRegistrationOpen = registrationOpen;
    }

    public boolean isRegisteredByCurrentUser() {
        return isRegisteredByCurrentUser;
    }

    public void setRegisteredByCurrentUser(boolean registeredByCurrentUser) {
        isRegisteredByCurrentUser = registeredByCurrentUser;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * 检查当前是否在报名有效期内
     */
    public boolean isInRegistrationPeriod() {
        if (registrationStartTime == null || registrationEndTime == null) {
            return false;
        }
        Date now = new Date();
        return now.compareTo(registrationStartTime) >= 0 && now.compareTo(registrationEndTime) <= 0;
    }

    /**
     * 检查报名是否已开始
     */
    public boolean isRegistrationStarted() {
        if (registrationStartTime == null) {
            return false;
        }
        Date now = new Date();
        return now.compareTo(registrationStartTime) >= 0;
    }

    /**
     * 检查报名是否已截止
     */
    public boolean isRegistrationEnded() {
        if (registrationEndTime == null) {
            return true;
        }
        Date now = new Date();
        return now.compareTo(registrationEndTime) > 0;
    }

    /**
     * 获取报名状态描述
     */
    public String getRegistrationStatusText() {
        if (STATUS_COMPLETED.equals(status) || STATUS_CANCELED.equals(status) || STATUS_ONGOING.equals(status)) {
            return "报名已结束";
        } else if (isRegistrationEnded()) {
            return "报名已结束";
        } else if (isInRegistrationPeriod()) {
            return "报名进行中";
        } else if (!isRegistrationStarted()) {
            return "报名未开始";
        }
        return "未知";
    }

    /**
     * 判断活动报名是否已关闭（考虑活动状态）
     */
    public boolean isRegistrationClosed() {
        return STATUS_COMPLETED.equals(status) || STATUS_CANCELED.equals(status) || STATUS_ONGOING.equals(status) || isRegistrationEnded();
    }
    
    public boolean getRegistrationClosed() {
        return isRegistrationClosed();
    }
}
