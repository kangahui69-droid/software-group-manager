package com.softwaregroup.common.dto;

import java.util.List;

/**
 * 分页响应结果
 *
 * 用于分页查询接口的统一响应格式
 */
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }

    /**
     * 判断是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * 判断是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * 判断是否为第一页
     */
    public boolean isFirst() {
        return page == 1;
    }

    /**
     * 判断是否为最后一页
     */
    public boolean isLast() {
        return page >= totalPages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public String toString() {
        return "PageResult{list=" + list + ", total=" + total + ", page=" + page +
                ", pageSize=" + pageSize + ", totalPages=" + totalPages + '}';
    }
}
