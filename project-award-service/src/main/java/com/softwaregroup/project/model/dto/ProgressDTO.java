package com.softwaregroup.project.model.dto;

/**
 * 进度数据传输对象
 */
public class ProgressDTO {
    private Integer id;
    private String title;
    private String description;
    private Integer completionRate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCompletionRate() { return completionRate; }
    public void setCompletionRate(Integer completionRate) { this.completionRate = completionRate; }
}
