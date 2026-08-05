package com.softwaregroup.content.model;

import java.util.Date;

/**
 * 群组消息实体类
 */
public class GroupMessage {
    private Integer id;
    private Integer groupId;
    private Integer senderId;
    private String content;
    private String messageType;
    private Date sentAt;

    public static final String MESSAGE_TYPE_TEXT = "TEXT";

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Date getSentAt() { return sentAt; }
    public void setSentAt(Date sentAt) { this.sentAt = sentAt; }
}
