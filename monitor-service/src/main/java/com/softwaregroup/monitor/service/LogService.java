package com.softwaregroup.monitor.service;

import org.springframework.stereotype.Service;
import com.softwaregroup.monitor.dao.OperationLogDAO;
import com.softwaregroup.monitor.model.OperationLog;
import com.softwaregroup.common.util.Result;

import java.util.*;

/**
 * 操作日志服务层
 */
@Service
public class LogService {

    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_INTERNAL_ERROR = 500;

    private static final int MAX_PAGE_SIZE = 100;

    private final OperationLogDAO operationLogDAO;

    public LogService(OperationLogDAO operationLogDAO) {
        this.operationLogDAO = operationLogDAO;
    }

    public Result listLogs(Map<String, Object> filter, Integer page, Integer pageSize) {
        if (page == null || page <= 0) {
            return Result.error(CODE_BAD_REQUEST, "页码必须大于0");
        }
        if (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return Result.error(CODE_BAD_REQUEST, "每页数量必须大于0且不超过100");
        }

        try {
            List<OperationLog> logs;
            int total;

            if (filter == null || filter.isEmpty()) {
                logs = operationLogDAO.findAll(page, pageSize);
                total = operationLogDAO.countAll();
            } else {
                String keyword = getStringValue(filter, "keyword");
                String operation = getStringValue(filter, "operation");
                String module = getStringValue(filter, "module");
                String dateRange = getStringValue(filter, "dateRange");

                logs = operationLogDAO.findByConditions(keyword, operation, module, dateRange, page, pageSize);
                total = operationLogDAO.countByConditions(keyword, operation, module, dateRange);
            }

            if (logs == null) {
                logs = Collections.emptyList();
            }
            if (total < 0) {
                total = 0;
            }

            int totalPages = (int) Math.ceil((double) total / pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("list", logs);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", totalPages);

            return Result.ok(result);
        } catch (Exception e) {
            return Result.error(CODE_INTERNAL_ERROR, "数据库错误");
        }
    }

    public Result getLogDetail(Integer id) {
        if (id == null || id <= 0) {
            return Result.error(CODE_BAD_REQUEST, "日志ID无效");
        }

        try {
            List<OperationLog> logs = operationLogDAO.findAll(1, 100);
            if (logs == null) {
                return Result.error(CODE_NOT_FOUND, "日志不存在");
            }

            for (OperationLog log : logs) {
                if (log.getId() != null && log.getId().equals(id)) {
                    return Result.ok(log);
                }
            }

            return Result.error(CODE_NOT_FOUND, "日志不存在");
        } catch (Exception e) {
            return Result.error(CODE_INTERNAL_ERROR, "数据库错误");
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
