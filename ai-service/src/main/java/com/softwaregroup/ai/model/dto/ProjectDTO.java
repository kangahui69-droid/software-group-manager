package com.softwaregroup.ai.model.dto;

/**
 * 项目DTO
 */
public class ProjectDTO {
    private Integer id;
    private String name;
    private String description;
    private String status;
    private String category;
    private Integer leaderId;

    public ProjectDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Integer leaderId) {
        this.leaderId = leaderId;
    }
}
