package com.softwaregroup.hr.model.entity;

/**
 * 简历-技能特长实体类
 */
public class ResumeSkill {
    private Integer id;
    private Integer resumeId;
    private String skillName;
    private String proficiency;
    private Integer proficiencyScore;
    private String category;
    private String description;
    private Integer displayOrder;

    public ResumeSkill() {
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

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getProficiency() {
        return proficiency;
    }

    public void setProficiency(String proficiency) {
        this.proficiency = proficiency;
    }

    public Integer getProficiencyScore() {
        return proficiencyScore;
    }

    public void setProficiencyScore(Integer proficiencyScore) {
        this.proficiencyScore = proficiencyScore;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}
