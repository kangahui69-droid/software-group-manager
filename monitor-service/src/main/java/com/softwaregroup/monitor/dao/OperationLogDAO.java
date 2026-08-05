package com.softwaregroup.monitor.dao;

import com.softwaregroup.monitor.model.OperationLog;

import java.util.List;

/**
 * 操作日志数据访问接口
 */
public interface OperationLogDAO {
    List<OperationLog> findAll(int page, int pageSize);
    List<OperationLog> findByConditions(String keyword, String operation, String module, String dateRange, int page, int pageSize);
    int countAll();
    int countByConditions(String keyword, String operation, String module, String dateRange);
}
