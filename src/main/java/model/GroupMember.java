package model;

import java.util.Date;

/**
 * 群成员实体类
 */
public class GroupMember {
    private Integer id;
    private Integer groupId;
    private Integer userId;
    private String role;
    private Date joinedAt;
    
    private String username;
    private String name;
    private String avatarFileId;

    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_MEMBER = "MEMBER";

    public GroupMember() {
    }

    public GroupMember(Integer groupId, Integer userId, String role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarFileId() {
        return avatarFileId;
    }

    public void setAvatarFileId(String avatarFileId) {
        this.avatarFileId = avatarFileId;
    }
    
    public boolean isOwner() {
        return ROLE_OWNER.equals(role);
    }
}
