package model;

public class NewsAsset {
    private int id;
    private Integer newsId;
    private int uploadedBy;
    private String uploadToken;
    private String kind;
    private String originalName;
    private String storedName;
    private String mimeType;
    private long fileSize;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getNewsId() { return newsId; }
    public void setNewsId(Integer newsId) { this.newsId = newsId; }
    public int getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(int uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getUploadToken() { return uploadToken; }
    public void setUploadToken(String uploadToken) { this.uploadToken = uploadToken; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
}
