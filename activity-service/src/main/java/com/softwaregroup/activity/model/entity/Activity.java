package com.softwaregroup.activity.model.entity;

import java.util.Date;

/**
 * 活动实体类
 */
public class Activity {
    private Integer id;
    private String title;
    private String description;
    private String activityType;
    private Date activityStartTime;
    private Date activityEndTime;
    private String location;
    private String organizers;
    private String contactInfo;
    private Date registrationStartTime;
    private Date registrationEndTime;
    private Integer maxParticipants;
    private String status;
    private String approvalStatus;
    private Integer creatorId;
    private Date createdAt;
    private Date updatedAt;

    // 扩展字段（用于页面展示）
    private String activityTypeName;
    private Integer currentParticipants;
    private boolean isRegistrationOpen;
    private boolean isRegisteredByCurrentUser;

    public static final String STATUS_UPCOMING = "upcoming";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELED = "canceled";
    public static final String APPROVAL_PENDING = "pending";
    public static final String APPROVAL_APPROVED = "approved";
    public static final String APPROVAL_REJECTED = "rejected";

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public Date getActivityStartTime() { return activityStartTime; }
    public void setActivityStartTime(Date activityStartTime) { this.activityStartTime = activityStartTime; }

    public Date getActivityEndTime() { return activityEndTime; }
    public void setActivityEndTime(Date activityEndTime) { this.activityEndTime = activityEndTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getOrganizers() { return organizers; }
    public void setOrganizers(String organizers) { this.organizers = organizers; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public Date getRegistrationStartTime() { return registrationStartTime; }
    public void setRegistrationStartTime(Date registrationStartTime) { this.registrationStartTime = registrationStartTime; }

    public Date getRegistrationEndTime() { return registrationEndTime; }
    public void setRegistrationEndTime(Date registrationEndTime) { this.registrationEndTime = registrationEndTime; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getActivityTypeName() { return activityTypeName; }
    public void setActivityTypeName(String activityTypeName) { this.activityTypeName = activityTypeName; }

    public Integer getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(Integer currentParticipants) { this.currentParticipants = currentParticipants; }

    public boolean isRegistrationOpen() { return isRegistrationOpen; }
    public void setRegistrationOpen(boolean registrationOpen) { isRegistrationOpen = registrationOpen; }

    public boolean isRegisteredByCurrentUser() { return isRegisteredByCurrentUser; }
    public void setRegisteredByCurrentUser(boolean registeredByCurrentUser) { isRegisteredByCurrentUser = registeredByCurrentUser; }

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
     * 根据活动时间自动计算活动状态
     */
    public String calculateStatus() {
        if (STATUS_CANCELED.equals(this.status)) {
            return STATUS_CANCELED;
        }
        Date now = new Date();
        if (activityStartTime == null) {
            return STATUS_UPCOMING;
        }
        if (activityEndTime == null) {
            if (now.before(activityStartTime)) {
                return STATUS_UPCOMING;
            }
            return STATUS_ONGOING;
        }
        if (now.before(activityStartTime)) {
            return STATUS_UPCOMING;
        }
        if (now.after(activityEndTime)) {
            return STATUS_COMPLETED;
        }
        return STATUS_ONGOING;
    }
}
