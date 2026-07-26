package service;

import dao.OperationLogDAO;
import model.OperationLog;
import util.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志服务 - 业务逻辑层
 *
 * 对应：LogServlet
 * 核心方法：
 * - listLogs(filter, page, pageSize) - 日志列表(分页)
 * - getLogDetail(id) - 日志详情
 */
public class LogService {

    private static final int MAX_PAGE_SIZE = 100;

    private final OperationLogDAO operationLogDAO;

    public LogService() {
        this.operationLogDAO = new OperationLogDAO();
    }

    public LogService(OperationLogDAO operationLogDAO) {
        this.operationLogDAO = operationLogDAO;
    }

    // ==================== 公开业务方法 ====================

    /**
     * 日志列表(分页)
     *
     * @param filter 筛选条件，支持 keyword/operation/module/dateRange，可为 null
     * @param page 页码(从1开始)
     * @param pageSize 每页条数
     * @return 分页结果，包含 list/total/page/pageSize/totalPages
     */
    public Result listLogs(Map<String, Object> filter, int page, int pageSize) {
        Result validationError = validatePaginationParams(page, pageSize);
        if (validationError != null) {
            return validationError;
        }

        try {
            QueryResult queryResult = queryLogs(filter, page, pageSize);
            Map<String, Object> paginationData = buildPaginationData(
                queryResult.logs, queryResult.total, page, pageSize);
            return Result.ok(paginationData);
        } catch (Exception e) {
            return serverError();
        }
    }

    /**
     * 获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    public Result getLogDetail(Integer id) {
        if (!isValidId(id)) {
            return Result.error(400, "日志ID无效");
        }

        try {
            List<OperationLog> logs = operationLogDAO.findAll(1, MAX_PAGE_SIZE);
            OperationLog targetLog = findLogById(ensureListNotNull(logs), id);

            if (targetLog == null) {
                return Result.error(404, "日志不存在");
            }
            return Result.ok(targetLog);
        } catch (Exception e) {
            return serverError();
        }
    }

    // ==================== 查询逻辑 ====================

    private QueryResult queryLogs(Map<String, Object> filter, int page, int pageSize) {
        if (isFilterEmpty(filter)) {
            List<OperationLog> logs = operationLogDAO.findAll(page, pageSize);
            int total = operationLogDAO.countAll();
            return new QueryResult(ensureListNotNull(logs), normalizeTotal(total));
        }

        FilterTerms terms = extractFilterTerms(filter);
        List<OperationLog> logs = operationLogDAO.findByConditions(
            terms.keyword, terms.operation, terms.module, terms.dateRange, page, pageSize);
        int total = operationLogDAO.countByConditions(
            terms.keyword, terms.operation, terms.module, terms.dateRange);

        return new QueryResult(ensureListNotNull(logs), normalizeTotal(total));
    }

    private FilterTerms extractFilterTerms(Map<String, Object> filter) {
        FilterTerms terms = new FilterTerms();
        terms.keyword = getFilterValueAsString(filter, "keyword");
        terms.operation = getFilterValueAsString(filter, "operation");
        terms.module = getFilterValueAsString(filter, "module");
        terms.dateRange = getFilterValueAsString(filter, "dateRange");
        return terms;
    }

    private OperationLog findLogById(List<OperationLog> logs, Integer targetId) {
        for (OperationLog log : logs) {
            if (log.getId() != null && log.getId().equals(targetId)) {
                return log;
            }
        }
        return null;
    }

    // ==================== 分页数据构建 ====================

    private Map<String, Object> buildPaginationData(List<OperationLog> logs, int total, int page, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", logs);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("totalPages", calculateTotalPages(total, pageSize));
        return data;
    }

    private int calculateTotalPages(int total, int pageSize) {
        return total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
    }

    // ==================== 参数校验 ====================

    private Result validatePaginationParams(int page, int pageSize) {
        if (!isValidPage(page)) {
            return Result.error(400, "页码必须大于0");
        }
        if (!isValidPageSize(pageSize)) {
            return Result.error(400, "每页数量必须大于0且不超过100");
        }
        return null;
    }

    private boolean isValidPage(int page) {
        return page > 0;
    }

    private boolean isValidPageSize(int pageSize) {
        return pageSize > 0 && pageSize <= MAX_PAGE_SIZE;
    }

    private boolean isValidId(Integer id) {
        return id != null && id > 0;
    }

    // ==================== 工具方法 ====================

    private int normalizeTotal(int total) {
        return Math.max(0, total);
    }

    private List<OperationLog> ensureListNotNull(List<OperationLog> logs) {
        return logs != null ? logs : new java.util.ArrayList<>();
    }

    private boolean isFilterEmpty(Map<String, Object> filter) {
        return filter == null || filter.isEmpty();
    }

    private String getFilterValueAsString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Result serverError() {
        return Result.error(500, "数据库错误");
    }

    // ==================== 内部类 ====================

    private static class QueryResult {
        final List<OperationLog> logs;
        final int total;

        QueryResult(List<OperationLog> logs, int total) {
            this.logs = logs;
            this.total = total;
        }
    }

    private static class FilterTerms {
        String keyword;
        String operation;
        String module;
        String dateRange;
    }
}
