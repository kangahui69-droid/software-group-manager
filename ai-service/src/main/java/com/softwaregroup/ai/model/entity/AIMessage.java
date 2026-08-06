package com.softwaregroup.ai.model.entity;

import java.util.Date;

/**
 * AI消息实体
 */
public class AIMessage {
    private Integer id;
    private Integer conversationId;
    private String role;
    private String content;
    private Date createdAt;

    public AIMessage() {
        this.createdAt = new Date();
    }

    public AIMessage(Integer conversationId, String role, String content) {
        this();
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
