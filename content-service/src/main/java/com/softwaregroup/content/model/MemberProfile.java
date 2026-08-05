package com.softwaregroup.content.model;

/**
 * 成员资料实体类
 */
public class MemberProfile {
    private Integer id;
    private Integer userId;
    private String name;
    private String studentId;
    private String major;
    private String className;
    private String phone;
    private String email;
    private Integer avatarFileId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAvatarFileId() { return avatarFileId; }
    public void setAvatarFileId(Integer avatarFileId) { this.avatarFileId = avatarFileId; }
}
