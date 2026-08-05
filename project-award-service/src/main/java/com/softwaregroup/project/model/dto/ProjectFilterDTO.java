package com.softwaregroup.project.model.dto;

/**
 * 项目过滤数据传输对象
 */
public class ProjectFilterDTO {
    private String keyword;
    private String status;
    private Integer year;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
}
