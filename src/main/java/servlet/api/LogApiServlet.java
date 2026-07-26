package servlet.api;

import model.User;
import service.LogService;
import servlet.BaseApiServlet;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志API Servlet
 *
 * 服务分层与API化完整计划.md 6.2 LogApiServlet 日志服务API化
 *
 * 端点：
 * - GET  /api/logs           → 日志列表(分页)
 * - GET  /api/logs/{id}     → 日志详情
 */
public class LogApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private transient LogService logService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.logService = new LogService();
    }

    public LogApiServlet() {
    }

    public LogApiServlet(LogService logService) {
        this.logService = logService;
    }

    // ==================== HTTP 方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);
        if (isListPath(pathInfo)) {
            handleListLogs(req, resp, user);
        } else {
            handleLogDetail(req, resp, user, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;
        sendMethodNotAllowed(resp, "POST方法不支持");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;
        sendMethodNotAllowed(resp, "PUT方法不支持");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;
        sendMethodNotAllowed(resp, "DELETE方法不支持");
    }

    // ==================== 认证处理 ====================

    private User requireAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return user;
    }

    // ==================== GET 请求处理 ====================

    private void handleListLogs(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        PaginationParams params = extractPaginationParams(req);
        if (params.hasInvalidPage()) {
            sendBadRequest(resp, "页码参数无效");
            return;
        }
        if (params.hasInvalidPageSize()) {
            sendBadRequest(resp, "每页数量参数无效");
            return;
        }

        Map<String, Object> filter = extractFilterParams(req);

        try {
            writeJson(resp, logService.listLogs(filter, params.page, params.pageSize));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    private void handleLogDetail(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        LogPathInfo pi = parsePathInfo(pathInfo);

        if (pi.isInvalid() || pi.hasSubResource()) {
            sendNotFound(resp, "路径不存在");
            return;
        }
        if (!pi.isValidLogId()) {
            sendBadRequest(resp, "无效的日志ID");
            return;
        }

        try {
            writeJson(resp, logService.getLogDetail(pi.getLogId()));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    // ==================== 参数解析 ====================

    private PaginationParams extractPaginationParams(HttpServletRequest req) {
        String pageParam = req.getParameter("page");
        String pageSizeParam = req.getParameter("pageSize");

        int page = parseIntOrDefault(pageParam, DEFAULT_PAGE);
        int pageSize = parseIntOrDefault(pageSizeParam, DEFAULT_PAGE_SIZE);

        boolean invalidPage = !isValidIntString(pageParam) && pageParam != null && !pageParam.trim().isEmpty();
        boolean invalidPageSize = !isValidIntString(pageSizeParam) && pageSizeParam != null && !pageSizeParam.trim().isEmpty();

        return new PaginationParams(page, pageSize, invalidPage, invalidPageSize);
    }

    private Map<String, Object> extractFilterParams(HttpServletRequest req) {
        Map<String, Object> filter = new HashMap<>();
        addFilterIfPresent(filter, "keyword", req.getParameter("keyword"));
        addFilterIfPresent(filter, "operation", req.getParameter("operation"));
        addFilterIfPresent(filter, "module", req.getParameter("module"));
        addFilterIfPresent(filter, "dateRange", req.getParameter("dateRange"));
        return filter;
    }

    private void addFilterIfPresent(Map<String, Object> filter, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            filter.put(key, value);
        }
    }

    private boolean isValidIntString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 路径处理 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo);
    }

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            return pathInfo;
        }

        String uri = req.getRequestURI();
        int idx = uri.indexOf("/api/logs/");
        if (idx < 0) {
            return null;
        }

        pathInfo = uri.substring(idx + 10);
        if (pathInfo.isEmpty()) {
            return null;
        }
        return pathInfo.startsWith("/") ? pathInfo : "/" + pathInfo;
    }

    private LogPathInfo parsePathInfo(String pathInfo) {
        if (isListPath(pathInfo)) {
            return LogPathInfo.ROOT;
        }

        String segment = extractFirstPathSegment(pathInfo);
        if (segment.isEmpty()) {
            return LogPathInfo.ROOT;
        }

        try {
            int logId = Integer.parseInt(segment);
            boolean hasSubResource = hasAdditionalSegments(pathInfo);
            return new LogPathInfo(logId, hasSubResource);
        } catch (NumberFormatException e) {
            return LogPathInfo.INVALID;
        }
    }

    private String extractFirstPathSegment(String pathInfo) {
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        int slashIdx = pathInfo.indexOf('/');
        return slashIdx > 0 ? pathInfo.substring(0, slashIdx) : pathInfo;
    }

    private boolean hasAdditionalSegments(String pathInfo) {
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        return pathInfo.indexOf('/') > 0;
    }

    // ==================== 响应便捷方法 ====================

    private void sendMethodNotAllowed(HttpServletResponse resp, String message) throws IOException {
        writeJson(resp, Result.error(405, message));
    }

    private void sendNotFound(HttpServletResponse resp, String message) throws IOException {
        writeJson(resp, Result.error(404, message));
    }

    // ==================== 内部类 ====================

    private static class PaginationParams {
        final int page;
        final int pageSize;
        final boolean invalidPage;
        final boolean invalidPageSize;

        PaginationParams(int page, int pageSize, boolean invalidPage, boolean invalidPageSize) {
            this.page = page;
            this.pageSize = pageSize;
            this.invalidPage = invalidPage;
            this.invalidPageSize = invalidPageSize;
        }

        boolean hasInvalidPage() {
            return invalidPage;
        }

        boolean hasInvalidPageSize() {
            return invalidPageSize;
        }
    }

    private static class LogPathInfo {
        static final LogPathInfo ROOT = new LogPathInfo(false, false);
        static final LogPathInfo INVALID = new LogPathInfo(true, false);

        private final boolean invalid;
        private final boolean hasSubResource;
        private final Integer logId;

        private LogPathInfo(boolean invalid, boolean hasSubResource) {
            this.invalid = invalid;
            this.hasSubResource = hasSubResource;
            this.logId = null;
        }

        private LogPathInfo(int logId, boolean hasSubResource) {
            this.invalid = false;
            this.hasSubResource = hasSubResource;
            this.logId = logId;
        }

        boolean isInvalid() {
            return invalid;
        }

        boolean isValidLogId() {
            return logId != null && logId > 0;
        }

        boolean hasSubResource() {
            return hasSubResource;
        }

        Integer getLogId() {
            return logId;
        }
    }
}
