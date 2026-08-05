package com.softwaregroup.content.model;

import java.util.Date;

/**
 * 用户群组关联实体类
 */
public class UserGroup {
    private Integer id;
    private Integer userId;
    private Integer groupId;
    private Date joinedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }
}
