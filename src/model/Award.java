package model;

import java.util.Date;

/**
 * 奖项实体类
 */
public class Award {
    private Integer id;
    private String name;
    private String awardName;
    private Integer awardLevel;
    private String competition;
    private Integer year;
    private Date competitionTime;
    private String competitionLocation;
    private String competitionSession;
    private Integer awardType;
    private Integer awardCategory;
    private Integer competitionLevel;
    private String teamName;
    private String awardStatus;
    private String description;
    private Integer userId;
    private Integer createdBy;
    private Date createdAt;
    private Date updatedAt;
    private Integer approvedBy;
    private Date approvedAt;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAwardName() {
        return awardName;
    }

    public void setAwardName(String awardName) {
        this.awardName = awardName;
    }

    public Integer getAwardLevel() {
        return awardLevel;
    }

    public void setAwardLevel(Integer awardLevel) {
        this.awardLevel = awardLevel;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
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

    public Date getCompetitionTime() {
        return competitionTime;
    }

    public void setCompetitionTime(Date competitionTime) {
        this.competitionTime = competitionTime;
    }

    public String getCompetitionLocation() {
        return competitionLocation;
    }

    public void setCompetitionLocation(String competitionLocation) {
        this.competitionLocation = competitionLocation;
    }

    public String getCompetitionSession() {
        return competitionSession;
    }

    public void setCompetitionSession(String competitionSession) {
        this.competitionSession = competitionSession;
    }

    public Integer getAwardType() {
        return awardType;
    }

    public void setAwardType(Integer awardType) {
        this.awardType = awardType;
    }

    public Integer getAwardCategory() {
        return awardCategory;
    }

    public void setAwardCategory(Integer awardCategory) {
        this.awardCategory = awardCategory;
    }

    public Integer getCompetitionLevel() {
        return competitionLevel;
    }

    public void setCompetitionLevel(Integer competitionLevel) {
        this.competitionLevel = competitionLevel;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getAwardStatus() {
        return awardStatus;
    }

    public void setAwardStatus(String awardStatus) {
        this.awardStatus = awardStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
    }
}

