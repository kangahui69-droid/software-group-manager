package com.softwaregroup.project.model.dto;

import java.util.List;

/**
 * 奖项数据传输对象
 */
public class AwardDTO {
    private Integer id;
    private String competition;
    private String competitionTime;
    private Integer awardLevel;
    private Integer awardType;
    private List<Integer> memberIds;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCompetition() { return competition; }
    public void setCompetition(String competition) { this.competition = competition; }

    public String getCompetitionTime() { return competitionTime; }
    public void setCompetitionTime(String competitionTime) { this.competitionTime = competitionTime; }

    public Integer getAwardLevel() { return awardLevel; }
    public void setAwardLevel(Integer awardLevel) { this.awardLevel = awardLevel; }

    public Integer getAwardType() { return awardType; }
    public void setAwardType(Integer awardType) { this.awardType = awardType; }

    public List<Integer> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Integer> memberIds) { this.memberIds = memberIds; }
}
