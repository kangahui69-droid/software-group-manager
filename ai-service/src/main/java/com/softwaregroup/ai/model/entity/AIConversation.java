package com.softwaregroup.ai.model.entity;

import java.util.Date;

/**
 * AI对话会话实体
 */
public class AIConversation {
    private Integer id;
    private Integer userId;
    private String sessionId;
    private Date createdAt;
    private Date updatedAt;

    public AIConversation() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public AIConversation(Integer userId, String sessionId) {
        this();
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
}
