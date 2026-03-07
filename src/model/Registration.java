package model;

import java.util.Date;

/**
 * 活动报名实体类
 */
public class Registration {
    private Integer activityId;
    private Integer userId;
    private String status; // pending-待审核, confirmed-已确认, rejected-已驳回, expired-已过期(动态计算)
    private Date createdAt; // 报名时间
    private Date updatedAt; // 状态更新时间
    private String notes; // 备注（驳回原因等）

    // 关联信息
    private Activity activity;
    private String userName;
    private String studentId;
    private String major;
    
    // 扩展字段（用于页面展示）
    private String activityName;
    private Date activityStartTime;
    private Date activityEndTime;
    private Date registrationEndTime;
    private String location;

    public Registration() {
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
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

    public Date getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(Date registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 检查报名是否已过期（动态计算）
     */
    public boolean isExpired() {
        if (registrationEndTime == null) {
            return false;
        }
        return new Date().after(registrationEndTime);
    }

    /**
     * 获取报名状态显示文本
     * 如果状态为pending且活动报名已截止，返回"已过期"
     */
    public String getDisplayStatus() {
        if ("pending".equals(status) && registrationEndTime != null && new Date().after(registrationEndTime)) {
            return "expired";
        }
        return status;
    }

    /**
     * 获取报名状态显示名称
     */
    public String getStatusName() {
        String displayStatus = getDisplayStatus();
        switch (displayStatus) {
            case "pending":
                return "申请审核中";
            case "confirmed":
                return "申请已确认";
            case "rejected":
                return "申请已驳回";
            case "expired":
                return "申请已过期";
            default:
                return "未知";
        }
    }
}
