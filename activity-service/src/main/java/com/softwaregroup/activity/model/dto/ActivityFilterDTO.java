package com.softwaregroup.activity.model.dto;

/**
 * 活动筛选条件数据传输对象
 */
public class ActivityFilterDTO {
    private String keyword;
    private String activityType;
    private String status;
    private String approvalStatus;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
}
