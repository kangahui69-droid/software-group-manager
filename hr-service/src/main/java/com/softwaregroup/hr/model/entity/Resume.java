package com.softwaregroup.hr.model.entity;

import java.util.Date;
import java.util.List;

/**
 * 简历主表实体类
 */
public class Resume {
    private Integer id;
    private Integer userId;
    private String resumeName;
    private String templateStyle;
    private String summary;
    private String careerObjective;
    private String phone;
    private String email;
    private String wechat;
    private String githubUrl;
    private String blogUrl;
    private Integer isDefault;
    private Integer status;
    private Integer deleted;

    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_HIDDEN = 2;
    public static final int DELETED_NO = 0;
    public static final int DELETED_YES = 1;
    public static final int DEFAULT_YES = 1;
    public static final int DEFAULT_NO = 0;

    public Resume() {
    }

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

    public String getResumeName() {
        return resumeName;
    }

    public void setResumeName(String resumeName) {
        this.resumeName = resumeName;
    }

    public String getTemplateStyle() {
        return templateStyle;
    }

    public void setTemplateStyle(String templateStyle) {
        this.templateStyle = templateStyle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCareerObjective() {
        return careerObjective;
    }

    public void setCareerObjective(String careerObjective) {
        this.careerObjective = careerObjective;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getBlogUrl() {
        return blogUrl;
    }

    public void setBlogUrl(String blogUrl) {
        this.blogUrl = blogUrl;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public List<ResumeEducation> getEducations() {
        return educations;
    }

    public void setEducations(List<ResumeEducation> educations) {
        this.educations = educations;
    }

    public List<ResumeSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<ResumeSkill> skills) {
        this.skills = skills;
    }

    public List<ResumeProject> getProjects() {
        return projects;
    }

    public void setProjects(List<ResumeProject> projects) {
        this.projects = projects;
    }

    public List<ResumeAward> getAwards() {
        return awards;
    }

    public void setAwards(List<ResumeAward> awards) {
        this.awards = awards;
    }

    private List<ResumeEducation> educations;
    private List<ResumeSkill> skills;
    private List<ResumeProject> projects;
    private List<ResumeAward> awards;
}
