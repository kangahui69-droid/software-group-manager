package model;

import java.util.Date;

/**
 * 群消息实体类
 */
public class GroupMessage {
    private Integer id;
    private Integer groupId;
    private Integer senderId;
    private String content;
    private Date sentAt;
    
    private String senderName;
    private String senderAvatarFileId;

    public GroupMessage() {
    }

    public GroupMessage(Integer groupId, Integer senderId, String content) {
        this.groupId = groupId;
        this.senderId = senderId;
        this.content = content;
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

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatarFileId() {
        return senderAvatarFileId;
    }

    public void setSenderAvatarFileId(String senderAvatarFileId) {
        this.senderAvatarFileId = senderAvatarFileId;
    }
}
