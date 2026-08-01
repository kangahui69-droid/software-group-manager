package model;

import java.io.Serializable;
import java.util.Date;

/**
 * AI消息状态实体类
 * 用于AI消息异步处理，记录消息状态和结果
 */
public class AIMessageStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PROCESSING = "processing";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    private Integer id;
    private String messageId;
    private Integer userId;
    private String sessionId;
    private String userMessage;
    private String aiResponse;
    private String status;
    private String errorMessage;
    private Date createdAt;
    private Date updatedAt;
    private Date completedAt;

    public AIMessageStatus() {
        this.status = STATUS_PENDING;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public AIMessageStatus(String messageId, Integer userId, String sessionId, String userMessage) {
        this();
        this.messageId = messageId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.userMessage = userMessage;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isProcessing() {
        return STATUS_PROCESSING.equals(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }
}
