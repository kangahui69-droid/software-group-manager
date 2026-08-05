package com.softwaregroup.content.model;

import java.util.Date;

/**
 * 群组实体类
 */
public class ActivityGroup {
    private Integer id;
    private String groupName;
    private Integer activityId;
    private Integer groupOwnerId;
    private Date createdAt;
    private Date updatedAt;
    private Boolean muted;
    private Date mutedUntil;
    private String muteReason;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getActivityId() { return activityId; }
    public void setActivityId(Integer activityId) { this.activityId = activityId; }

    public Integer getGroupOwnerId() { return groupOwnerId; }
    public void setGroupOwnerId(Integer groupOwnerId) { this.groupOwnerId = groupOwnerId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Boolean isMuted() { return muted; }
    public void setMuted(Boolean muted) { this.muted = muted; }

    public Date getMutedUntil() { return mutedUntil; }
    public void setMutedUntil(Date mutedUntil) { this.mutedUntil = mutedUntil; }

    public String getMuteReason() { return muteReason; }
    public void setMuteReason(String muteReason) { this.muteReason = muteReason; }
}
