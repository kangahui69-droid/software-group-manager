package com.softwaregroup.activity.model.entity;

import java.util.Date;

/**
 * 补签申请实体类
 */
public class AttendanceMakeup {
    private Integer id;
    private Integer userId;
    private Date attendanceDate;
    private String makeUpType;
    private String applyReason;
    private Date applyTime;
    private String status;
    private Integer approveBy;
    private Date approveTime;
    private String approveRemark;

    // 关联用户信息
    private String userName;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String TYPE_CHECK_IN = "CHECK_IN";
    public static final String TYPE_CHECK_OUT = "CHECK_OUT";

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getMakeUpType() { return makeUpType; }
    public void setMakeUpType(String makeUpType) { this.makeUpType = makeUpType; }

    public String getApplyReason() { return applyReason; }
    public void setApplyReason(String applyReason) { this.applyReason = applyReason; }

    public Date getApplyTime() { return applyTime; }
    public void setApplyTime(Date applyTime) { this.applyTime = applyTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getApproveBy() { return approveBy; }
    public void setApproveBy(Integer approveBy) { this.approveBy = approveBy; }

    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }

    public String getApproveRemark() { return approveRemark; }
    public void setApproveRemark(String approveRemark) { this.approveRemark = approveRemark; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
