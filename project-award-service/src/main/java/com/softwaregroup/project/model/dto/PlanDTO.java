package com.softwaregroup.project.model.dto;

import java.util.Date;

/**
 * 计划数据传输对象
 */
public class PlanDTO {
    private Integer id;
    private String title;
    private String description;
    private Date startDate;
    private Date endDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
