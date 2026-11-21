package com.softwaregroup.content.model;

import java.util.Date;

/**
 * 群组成员实体类
 */
public class GroupMember {
    private Integer id;
    private Integer groupId;
    private Integer userId;
    private String role;
    private Date joinedAt;
    private Boolean muted;
    private Date mutedUntil;
    private String muteReason;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }
    public Boolean isMuted() { return muted; }
    public void setMuted(Boolean muted) { this.muted = muted; }
    public Date getMutedUntil() { return mutedUntil; }
    public void setMutedUntil(Date mutedUntil) { this.mutedUntil = mutedUntil; }
    public String getMuteReason() { return muteReason; }
    public void setMuteReason(String muteReason) { this.muteReason = muteReason; }
}
