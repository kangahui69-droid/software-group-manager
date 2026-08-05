package com.softwaregroup.hr.model.entity;

/**
 * 简历-获奖情况实体类
 */
public class ResumeAward {
    private Integer id;
    private Integer resumeId;
    private String awardName;
    private String competitionName;
    private String awardLevel;
    private String awardDate;
    private String awardOrg;
    private String description;
    private Integer displayOrder;
    private Integer isFromSystem;

    public ResumeAward() {
    }

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

    public String getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(String awardDate) {
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getIsFromSystem() {
        return isFromSystem;
    }

    public void setIsFromSystem(Integer isFromSystem) {
        this.isFromSystem = isFromSystem;
    }
}
