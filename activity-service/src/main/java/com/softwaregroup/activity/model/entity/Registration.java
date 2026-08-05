package com.softwaregroup.activity.model.entity;

import java.util.Date;

/**
 * 活动报名实体类
 */
public class Registration {
    private Integer activityId;
    private Integer userId;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String notes;

    // 关联活动信息
    private String activityName;
    private Date activityStartTime;
    private Date activityEndTime;
    private String location;
    private Date registrationEndTime;
    private String activityStatus;

    // 关联用户信息
    private String userName;
    private String studentId;
    private String major;
    private String grade;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_REJECTED = "rejected";

    // Getters and Setters
    public Integer getActivityId() { return activityId; }
    public void setActivityId(Integer activityId) { this.activityId = activityId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public Date getActivityStartTime() { return activityStartTime; }
    public void setActivityStartTime(Date activityStartTime) { this.activityStartTime = activityStartTime; }

    public Date getActivityEndTime() { return activityEndTime; }
    public void setActivityEndTime(Date activityEndTime) { this.activityEndTime = activityEndTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Date getRegistrationEndTime() { return registrationEndTime; }
    public void setRegistrationEndTime(Date registrationEndTime) { this.registrationEndTime = registrationEndTime; }

    public String getActivityStatus() { return activityStatus; }
    public void setActivityStatus(String activityStatus) { this.activityStatus = activityStatus; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
