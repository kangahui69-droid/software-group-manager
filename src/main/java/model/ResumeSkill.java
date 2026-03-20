package model;

import java.util.Date;

/**
 * 简历-技能特长实体类
 * 对应数据库表: resume_skills
 */
public class ResumeSkill {

    private Integer id;                    // 技能ID
    private Integer resumeId;              // 简历ID
    private String skillName;              // 技能名称
    private String proficiency;            // 熟练程度：beginner/elementary/intermediate/advanced/expert
    private Integer proficiencyScore;        // 熟练度分数：1-100
    private String category;               // 技能分类：编程语言/开发框架/数据库/工具/语言/其他
    private String description;            // 技能描述
    private Integer displayOrder;          // 显示顺序
    private Date createdAt;                // 创建时间
    private Date updatedAt;                // 更新时间

    // 关联对象
    private Resume resume;

    // 构造方法
    public ResumeSkill() {
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

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    // 便捷方法
    public boolean isAdvanced() {
        return "advanced".equals(proficiency) || "expert".equals(proficiency);
    }

    @Override
    public String toString() {
        return "ResumeSkill{" +
                "id=" + id +
                ", resumeId=" + resumeId +
                ", skillName='" + skillName + '\'' +
                ", proficiency='" + proficiency + '\'' +
                '}';
    }
}
