package com.softwaregroup.project.model;

import java.util.Date;

/**
 * 奖项实体类
 */
public class Award {
    private Integer id;
    private String name;
    private String competition;
    private String competitionTime;
    private String awardStatus;
    private Integer awardLevel;
    private Integer awardType;
    private Integer createdBy;
    private Date createdAt;
    private Date updatedAt;
    private Integer year;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCompetition() { return competition; }
    public void setCompetition(String competition) { this.competition = competition; }

    public String getCompetitionTime() { return competitionTime; }
    public void setCompetitionTime(String competitionTime) { this.competitionTime = competitionTime; }

    public String getAwardStatus() { return awardStatus; }
    public void setAwardStatus(String awardStatus) { this.awardStatus = awardStatus; }

    public Integer getAwardLevel() { return awardLevel; }
    public void setAwardLevel(Integer awardLevel) { this.awardLevel = awardLevel; }

    public Integer getAwardType() { return awardType; }
    public void setAwardType(Integer awardType) { this.awardType = awardType; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
