package com.softwaregroup.monitor.model.dto;

/**
 * 问题筛选DTO
 */
public class ProblemFilterDTO {

    private String category;
    private String status;

    public ProblemFilterDTO() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
