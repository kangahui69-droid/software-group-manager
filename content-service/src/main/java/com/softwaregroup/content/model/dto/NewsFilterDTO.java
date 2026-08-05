package com.softwaregroup.content.model.dto;

/**
 * 新闻过滤数据传输对象
 */
public class NewsFilterDTO {
    private String keyword;
    private String type;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
