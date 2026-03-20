package model;

import java.util.Date;
import java.util.List;

/**
 * 简历主表实体类
 * 对应数据库表: resumes
 */
public class Resume {

    // ========== 基本字段 ==========

    private Integer id;                    // 简历ID
    private Integer userId;                // 用户ID（关联users表）
    private String resumeName;             // 简历名称
    private String templateStyle;          // 简历模板风格
    private String summary;                // 个人简介/自我评价
    private String careerObjective;        // 求职意向
    private String phone;                  // 联系电话
    private String email;                  // 联系邮箱
    private String wechat;                 // 微信号
    private String githubUrl;              // GitHub主页
    private String blogUrl;                // 技术博客地址
    private String photoUrl;               // 个人照片URL
    private Integer isDefault;             // 是否为默认简历：0-否，1-是
    private Integer status;                // 简历状态：0-草稿，1-已发布，2-已隐藏
    private Integer viewCount;             // 浏览次数
    private Date createdAt;                // 创建时间
    private Date updatedAt;                // 更新时间
    private Integer deleted;               // 软删除标志：0-正常，1-已删除

    // ========== 关联字段（查询时填充）==========

    private User user;                     // 用户对象
    private List<ResumeEducation> educations;      // 教育经历列表
    private List<ResumeSkill> skills;              // 技能特长列表
    private List<ResumeProject> projects;          // 项目经历列表
    private List<ResumeAward> awards;              // 获奖情况列表

    // ========== 构造方法 ==========

    public Resume() {
    }

    public Resume(Integer userId, String resumeName) {
        this.userId = userId;
        this.resumeName = resumeName;
    }

    // ========== Getter/Setter ==========

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    // ========== 便捷方法 ==========

    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return deleted != null && deleted == 1;
    }

    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        this.deleted = 1;
    }

    /**
     * 是否为默认简历
     */
    public boolean isDefaultResume() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    // ========== toString ==========

    @Override
    public String toString() {
        return "Resume{" +
                "id=" + id +
                ", userId=" + userId +
                ", resumeName='" + resumeName + '\'' +
                ", templateStyle='" + templateStyle + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", deleted=" + deleted +
                '}';
    }
}
