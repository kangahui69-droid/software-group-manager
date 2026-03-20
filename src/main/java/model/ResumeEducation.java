package model;

import java.util.Date;

/**
 * 简历-教育经历实体类
 * 对应数据库表: resume_educations
 */
public class ResumeEducation {

    private Integer id;                    // 教育经历ID
    private Integer resumeId;              // 简历ID
    private String schoolName;             // 学校名称
    private String major;                  // 专业名称
    private String degree;                 // 学历/学位
    private java.sql.Date startDate;       // 入学时间
    private java.sql.Date endDate;         // 毕业时间
    private Integer isCurrent;             // 是否在读：0-已毕业，1-在读
    private String description;            // 在校经历描述
    private Integer displayOrder;          // 显示顺序
    private Date createdAt;                // 创建时间
    private Date updatedAt;                // 更新时间

    // 关联对象
    private Resume resume;

    // 构造方法
    public ResumeEducation() {
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

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
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
    public boolean isCurrentStudy() {
        return isCurrent != null && isCurrent == 1;
    }

    @Override
    public String toString() {
        return "ResumeEducation{" +
                "id=" + id +
                ", resumeId=" + resumeId +
                ", schoolName='" + schoolName + '\'' +
                ", major='" + major + '\'' +
                ", degree='" + degree + '\'' +
                '}';
    }
}
