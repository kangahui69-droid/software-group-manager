package com.softwaregroup.project.model.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 项目数据传输对象
 */
public class ProjectDTO {
    private Integer id;
    private String name;
    private String description;
    private String category;
    private Integer year;
    private BigDecimal budget;
    private String repoUrl;
    private Date expectedStartDate;
    private Date expectedEndDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public Date getExpectedStartDate() { return expectedStartDate; }
    public void setExpectedStartDate(Date expectedStartDate) { this.expectedStartDate = expectedStartDate; }

    public Date getExpectedEndDate() { return expectedEndDate; }
    public void setExpectedEndDate(Date expectedEndDate) { this.expectedEndDate = expectedEndDate; }
}
