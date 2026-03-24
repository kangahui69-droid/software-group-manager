package model;

import java.util.Date;

/**
 * 补签申请实体类
 */
public class AttendanceMakeup {
    private Integer id;
    private Integer userId;
    private Date attendanceDate;
    private String makeUpType;  // CHECK_IN, CHECK_OUT
    private String applyReason;
    private Date applyTime;
    private String status;      // PENDING, APPROVED, REJECTED
    private Integer approveBy;
    private Date approveTime;
    private String approveRemark;

    // 关联用户信息
    private String userName;
    private String userRealName;
    private String approverName;

    public AttendanceMakeup() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(Date attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getMakeUpType() {
        return makeUpType;
    }

    public void setMakeUpType(String makeUpType) {
        this.makeUpType = makeUpType;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getApproveBy() {
        return approveBy;
    }

    public void setApproveBy(Integer approveBy) {
        this.approveBy = approveBy;
    }

    public Date getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(Date approveTime) {
        this.approveTime = approveTime;
    }

    public String getApproveRemark() {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark) {
        this.approveRemark = approveRemark;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }
}
