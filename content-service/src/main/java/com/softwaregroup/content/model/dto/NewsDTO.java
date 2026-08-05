package com.softwaregroup.content.model.dto;

/**
 * 新闻数据传输对象
 */
public class NewsDTO {
    private Integer id;
    private String title;
    private String type;
    private String summary;
    private String content;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
