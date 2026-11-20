package model;

import java.sql.Timestamp;

/** A publication delivered to one user. */
public class PublicationNotification {
    private final long id;
    private final String sourceType;
    private final int sourceId;
    private final String title;
    private final Timestamp createdAt;
    private final Timestamp readAt;

    public PublicationNotification(long id, String sourceType, int sourceId, String title,
                                   Timestamp createdAt, Timestamp readAt) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.title = title;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public long getId() { return id; }
    public String getSourceType() { return sourceType; }
    public int getSourceId() { return sourceId; }
    public String getTitle() { return title; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getReadAt() { return readAt; }
    public boolean isUnread() { return readAt == null; }
    public String getTargetUrl() {
        return "ACTIVITY".equals(sourceType)
                ? "/activity?action=detail&id=" + sourceId
                : "/news?action=detail&id=" + sourceId;
    }
}
