package model;

import java.util.Date;

/**
 * 活动群实体类
 */
public class ActivityGroup {
    private Integer id;
    private Integer activityId;
    private String groupName;
    private Integer groupOwnerId;
    private Date createdAt;
    private Date updatedAt;
    
    private String activityName;
    private String ownerName;
    private Integer memberCount;
    
    private Integer isMuted;
    private Date mutedUntil;
    private String muteReason;

    public ActivityGroup() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Integer getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(Integer groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
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

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getIsMuted() {
        return isMuted;
    }

    public void setIsMuted(Integer isMuted) {
        this.isMuted = isMuted;
    }

    public Date getMutedUntil() {
        return mutedUntil;
    }

    public void setMutedUntil(Date mutedUntil) {
        this.mutedUntil = mutedUntil;
    }

    public String getMuteReason() {
        return muteReason;
    }

    public void setMuteReason(String muteReason) {
        this.muteReason = muteReason;
    }

    public boolean isMuted() {
        if (isMuted != null && isMuted == 1) {
            if (mutedUntil == null) {
                return true;
            }
            return mutedUntil.after(new Date());
        }
        return false;
    }
}
