package model;

import java.util.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志实体类
 */
public class OperationLog {
    private static final DateTimeFormatter BEIJING_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Shanghai"));
    private Integer id;
    private Integer userId;
    private String username;
    private String operation;
    private String module;
    private String description;
    private String ipAddress;
    private String proxyIp;
    private String userAgent;
    private String result;
    private Integer httpStatus;
    private Long durationMs;
    private String requestId;
    private String errorCode;
    private String targetType;
    private String targetId;
    private Date createdAt;

    public OperationLog() {
    }

    // Getters and Setters
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getProxyIp() { return proxyIp; }
    public void setProxyIp(String proxyIp) { this.proxyIp = proxyIp; }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCreatedAtBeijing() {
        return createdAt == null ? "" : BEIJING_DATE_TIME.format(createdAt.toInstant());
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}

