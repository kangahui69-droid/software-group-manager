package dto;

import java.io.InputStream;

/**
 * 文件信息数据传输对象
 * 用于文件上传时传递数据
 */
public class FileInfo {

    private String fileName;
    private String contentType;
    private long size;
    private InputStream inputStream;

    public FileInfo() {
    }

    public FileInfo(String fileName, String contentType, long size) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
    }

    public FileInfo(String fileName, String contentType, long size, InputStream inputStream) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
