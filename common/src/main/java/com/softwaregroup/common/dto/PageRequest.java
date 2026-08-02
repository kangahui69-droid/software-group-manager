package com.softwaregroup.common.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页请求参数
 *
 * 用于分页查询接口
 */
public class PageRequest {

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小（防止查询过多数据）
     */
    private static final int MAX_PAGE_SIZE = 100;

    private int page = DEFAULT_PAGE;
    private int pageSize = DEFAULT_PAGE_SIZE;

    public PageRequest() {
    }

    public PageRequest(int page, int pageSize) {
        this.page = page > 0 ? page : DEFAULT_PAGE;
        this.pageSize = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
    }

    /**
     * 从请求参数创建
     */
    public static PageRequest fromParams(Map<String, Object> params) {
        int page = DEFAULT_PAGE;
        int pageSize = DEFAULT_PAGE_SIZE;

        if (params != null) {
            Object pageObj = params.get("page");
            Object pageSizeObj = params.get("pageSize");

            if (pageObj != null) {
                page = Integer.parseInt(pageObj.toString());
            }
            if (pageSizeObj != null) {
                pageSize = Integer.parseInt(pageSizeObj.toString());
            }
        }

        return new PageRequest(page, pageSize);
    }

    /**
     * 转换为 Map（用于查询）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", (page - 1) * pageSize);
        map.put("pageSize", pageSize);
        return map;
    }

    /**
     * 获取偏移量（用于 SQL LIMIT）
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page > 0 ? page : DEFAULT_PAGE;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
    }

    @Override
    public String toString() {
        return "PageRequest{page=" + page + ", pageSize=" + pageSize + '}';
    }
}
