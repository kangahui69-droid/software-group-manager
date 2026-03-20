package model;

import java.util.Date;

/**
 * 简历-获奖情况实体类
 * 对应数据库表: resume_awards
 */
public class ResumeAward {

    private Integer id;                    // 记录ID
    private Integer resumeId;              // 简历ID
    private Integer awardId;               // 关联的系统奖项ID
    private String awardName;              // 奖项名称
    private String competitionName;        // 比赛/活动名称
    private String awardLevel;             // 奖项等级
    private java.sql.Date awardDate;       // 获奖时间
    private String awardOrg;               // 颁奖机构
    private String description;            // 获奖描述
    private Integer isFromSystem;          // 是否来自系统奖项
    private Integer displayOrder;          // 显示顺序
    private Date createdAt;                // 创建时间
    private Date updatedAt;                // 更新时间

    // 关联对象
    private Resume resume;
    private Award award;                   // 关联的系统奖项

    // 构造方法
    public ResumeAward() {
    }

    // Getter/Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getResumeId() {
        return resumeId;
    }

    public void setResumeId(Integer resumeId) {
        this.resumeId = resumeId;
    }

    public Integer getAwardId() {
        return awardId;
    }

    public void setAwardId(Integer awardId) {
        this.awardId = awardId;
    }

    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getAwardLevel() {
        return awardLevel;
    }

    public void setAwardLevel(String awardLevel) {
        this.awardLevel = awardLevel;
    }

    public java.sql.Date getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(java.sql.Date awardDate) {
        this.awardDate = awardDate;
    }

    public String getAwardOrg() {
        return awardOrg;
    }

    public void setAwardOrg(String awardOrg) {
        this.awardOrg = awardOrg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIsFromSystem() {
        return isFromSystem;
    }

    public void setIsFromSystem(Integer isFromSystem) {
        this.isFromSystem = isFromSystem;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    // 便捷方法
    public boolean isFromSystemAward() {
        return isFromSystem != null && isFromSystem == 1;
    }

    @Override
    public String toString() {
        return "ResumeAward{" +
                "id=" + id +
                ", resumeId=" + resumeId +
                ", awardName='" + awardName + '\'' +
                ", awardLevel='" + awardLevel + '\'' +
                '}';
    }
}
