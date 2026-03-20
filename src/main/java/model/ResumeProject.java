package model;

import java.util.Date;

/**
 * 简历-项目经历实体类
 * 对应数据库表: resume_projects
 */
public class ResumeProject {

    private Integer id;                    // 项目ID
    private Integer resumeId;              // 简历ID
    private String projectName;            // 项目名称
    private String role;                   // 担任角色
    private Integer teamSize;              // 团队规模
    private java.sql.Date startDate;       // 开始时间
    private java.sql.Date endDate;         // 结束时间
    private Integer isCurrent;             // 是否进行中
    private String description;            // 项目描述
    private String responsibilities;         // 个人职责
    private String technologies;             // 使用技术
    private String projectUrl;               // 项目链接
    private String achievements;           // 项目成果
    private Integer displayOrder;          // 显示顺序
    private Integer isFromSystem;          // 是否来自系统项目
    private Integer systemProjectId;       // 关联的系统项目ID
    private Date createdAt;                // 创建时间
    private Date updatedAt;                // 更新时间

    // 关联对象
    private Resume resume;
    private Project project;               // 关联的系统项目

    // 构造方法
    public ResumeProject() {
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public java.sql.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.sql.Date startDate) {
        this.startDate = startDate;
    }

    public java.sql.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.sql.Date endDate) {
        this.endDate = endDate;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
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

    public Integer getSystemProjectId() {
        return systemProjectId;
    }

    public void setSystemProjectId(Integer systemProjectId) {
        this.systemProjectId = systemProjectId;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // 便捷方法
    public boolean isCurrentProject() {
        return isCurrent != null && isCurrent == 1;
    }

    public boolean isFromSystemProject() {
        return isFromSystem != null && isFromSystem == 1;
    }

    @Override
    public String toString() {
        return "ResumeProject{" +
                "id=" + id +
                ", resumeId=" + resumeId +
                ", projectName='" + projectName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
