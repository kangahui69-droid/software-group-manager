package dto;

import java.sql.Date;

/**
 * 简历-获奖情况数据传输对象
 */
public class ResumeAwardDTO {
    private Integer id;
    private Integer resumeId;
    private String awardName;
    private String competitionName;
    private String awardLevel;
    private Date awardDate;
    private String awardOrg;
    private String description;

    public ResumeAwardDTO() {
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

    public Date getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(Date awardDate) {
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
}