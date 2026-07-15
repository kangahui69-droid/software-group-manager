package dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 奖项数据传输对象
 */
public class AwardDTO {
    private String competition;
    private String competitionTime;
    private Integer year;
    private Integer awardLevel;
    private Integer awardType;
    private Integer awardCategory;
    private Integer competitionLevel;
    private String competitionLocation;
    private String teamName;
    private String description;

    public AwardDTO() {
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public String getCompetitionTime() {
        return competitionTime;
    }

    public void setCompetitionTime(String competitionTime) {
        this.competitionTime = competitionTime;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getAwardLevel() {
        return awardLevel;
    }

    public void setAwardLevel(Integer awardLevel) {
        this.awardLevel = awardLevel;
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

    public String getCompetitionLocation() {
        return competitionLocation;
    }

    public void setCompetitionLocation(String competitionLocation) {
        this.competitionLocation = competitionLocation;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date parseCompetitionTime() {
        if (competitionTime == null || competitionTime.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return sdf.parse(competitionTime);
        } catch (Exception e) {
            return null;
        }
    }

    public int extractYear() {
        if (year != null) {
            return year;
        }
        Date date = parseCompetitionTime();
        if (date != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            return cal.get(java.util.Calendar.YEAR);
        }
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }
}