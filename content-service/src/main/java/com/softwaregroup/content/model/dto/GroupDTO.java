package com.softwaregroup.content.model.dto;

/**
 * 群组数据传输对象
 */
public class GroupDTO {
    private Integer id;
    private String groupName;
    private Integer activityId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public Integer getActivityId() { return activityId; }
    public void setActivityId(Integer activityId) { this.activityId = activityId; }
}
