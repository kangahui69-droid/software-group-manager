package servlet.api;

import dto.ProblemDTO;
import dto.ProblemFilterDTO;
import model.User;
import service.ProblemService;
import servlet.BaseApiServlet;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 问题反馈API Servlet
 *
 * 服务分层与API化完整计划.md 5.2 ProblemService 问题服务
 *
 * 端点：
 * - GET  /api/problems           → 问题列表(分页)
 * - GET  /api/problems/{id}     → 问题详情
 * - POST /api/problems           → 提交问题
 * - PUT  /api/problems/{id}     → 更新问题
 * - DELETE /api/problems/{id}   → 删除问题
 * - POST /api/problems/{id}/status   → 更新状态
 * - POST /api/problems/{id}/category → 更新分类
 * - POST /api/problems/{id}/comment  → 添加备注
 * - GET  /api/problems/my        → 我的问题
 * - GET  /api/problems/stats     → 问题统计
 */
public class ProblemApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    private transient ProblemService problemService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.problemService = new ProblemService();
    }

    // ==================== HTTP 方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);

        if (isListPath(pathInfo)) {
            dispatchListGetRequest(pathInfo, req, resp, user);
        } else {
            handleProblemGet(req, resp, user, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);
        boolean isPutTunnel = isPutMethodTunnel(req);

        if (isListPath(pathInfo)) {
            if (isPutTunnel) {
                sendBadRequest(resp, "根路径不支持PUT方法");
            } else {
                handleCreate(req, resp, user);
            }
        } else {
            handleProblemPost(req, resp, user, pathInfo, isPutTunnel);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;
        sendError(resp, 405, "PUT方法不支持，请使用POST with _method=PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);
        if (isListPath(pathInfo)) {
            sendNotFound(resp);
        } else {
            handleProblemDelete(req, resp, user, pathInfo);
        }
    }

    // ==================== GET 请求分发 ====================

    private void dispatchListGetRequest(String pathInfo, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        if (pathInfo == null) {
            handleListProblems(req, resp, user);
            return;
        }
        switch (pathInfo) {
            case "/stats":
                handleGetStatistics(resp);
                break;
            case "/my":
                handleGetMyProblems(req, resp, user);
                break;
            default:
                handleListProblems(req, resp, user);
        }
    }

    // ==================== 问题主资源处理 ====================

    private void handleListProblems(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        ProblemFilterDTO filter = buildFilterFromParams(req);
        PageParams pageParams = extractPageParams(req);
        writeJson(resp, problemService.listProblems(filter, pageParams.page, pageParams.pageSize));
    }

    private void handleGetMyProblems(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        PageParams pageParams = extractPageParams(req);
        writeJson(resp, problemService.getMyProblems(user.getId(), pageParams.page, pageParams.pageSize));
    }

    private void handleGetStatistics(HttpServletResponse resp) throws IOException {
        writeJson(resp, problemService.getStatistics());
    }

    private void handleProblemGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        ProblemPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidProblemId()) {
            sendBadRequest(resp, "无效的问题ID");
            return;
        }
        if (pi.hasSubResource()) {
            sendNotFound(resp);
            return;
        }
        writeJson(resp, problemService.getProblemDetail(pi.getProblemId()));
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        ProblemDTO dto = parseDtoFromRequest(req, ProblemDTO.class);
        if (dto == null) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }
        writeJson(resp, problemService.submitProblem(dto, user.getId()));
    }

    private void handleProblemPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo,
                                  boolean isPutTunnel) throws IOException {
        ProblemPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidProblemId()) {
            sendBadRequest(resp, "无效的问题ID");
            return;
        }

        if (pi.isAction()) {
            dispatchActionRequest(pi, req, resp, user);
        } else if (isPutTunnel) {
            handleUpdate(req, resp, user, pi.getProblemId());
        } else {
            sendNotFound(resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int problemId) throws IOException {
        ProblemDTO dto = parseDtoFromRequest(req, ProblemDTO.class);
        if (dto == null) {
            sendBadRequest(resp, "无效的JSON格式");
            return;
        }
        writeJson(resp, problemService.updateProblem(problemId, dto, user.getId()));
    }

    private void handleProblemDelete(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        ProblemPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidProblemId()) {
            sendBadRequest(resp, "无效的问题ID");
            return;
        }
        if (pi.isAction() || pi.hasSubResource()) {
            sendNotFound(resp);
            return;
        }
        writeJson(resp, problemService.deleteProblem(pi.getProblemId(), user.getId()));
    }

    // ==================== 认证与请求辅助 ====================

    private User requireAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return user;
    }

    private boolean isPutMethodTunnel(HttpServletRequest req) {
        return "PUT".equalsIgnoreCase(req.getParameter("_method"));
    }

    // ==================== 路径判断 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")
                || "/my".equals(pathInfo) || "/stats".equals(pathInfo);
    }

    // ==================== 路径解析 ====================

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            String uri = req.getRequestURI();
            int idx = uri.indexOf("/api/problems/");
            if (idx >= 0) {
                pathInfo = uri.substring(idx + 14);
                if (pathInfo.isEmpty()) {
                    pathInfo = null;
                } else if (!pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
            }
        }
        return pathInfo;
    }

    private ProblemPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return ProblemPathInfo.ROOT;
        }

        if (!pathInfo.startsWith("/")) {
            return ProblemPathInfo.ROOT;
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length == 0 || segments[0].isEmpty()) {
            return ProblemPathInfo.ROOT;
        }

        int problemId = parseIntOrZero(segments[0]);

        if (segments.length == 1) {
            return ProblemPathInfo.forProblem(problemId);
        }

        String segment1 = segments[1];

        if ("status".equals(segment1)) {
            return ProblemPathInfo.forAction(problemId, "status");
        }
        if ("category".equals(segment1)) {
            return ProblemPathInfo.forAction(problemId, "category");
        }
        if ("comment".equals(segment1)) {
            return ProblemPathInfo.forAction(problemId, "comment");
        }

        return ProblemPathInfo.forSubResource(problemId, segment1);
    }

    private int parseIntOrZero(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseIntParam(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== JSON解析 ====================

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private <T> T parseJsonRequest(String body, Class<T> clazz) {
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            return getGson().fromJson(body, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 公共辅助方法 ====================

    private ProblemFilterDTO buildFilterFromParams(HttpServletRequest req) {
        ProblemFilterDTO filter = new ProblemFilterDTO();
        filter.setCategory(req.getParameter("category"));
        filter.setStatus(req.getParameter("status"));
        return filter;
    }

    private PageParams extractPageParams(HttpServletRequest req) {
        return new PageParams(
                parseIntParam(req.getParameter("page"), 1),
                parseIntParam(req.getParameter("pageSize"), 20)
        );
    }

    private <T> T parseDtoFromRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
        String body = readBody(req);
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            return getGson().fromJson(body, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private void dispatchActionRequest(ProblemPathInfo pi, HttpServletRequest req,
                                      HttpServletResponse resp, User user) throws IOException {
        int problemId = pi.getProblemId();
        ProblemDTO dto = parseDtoFromRequest(req, ProblemDTO.class);

        switch (pi.getAction()) {
            case "status":
                String status = dto != null ? dto.getStatus() : null;
                String adminComment = dto != null ? dto.getAdminComment() : null;
                writeJson(resp, problemService.updateStatus(problemId, status, adminComment, user.getId()));
                break;
            case "category":
                String category = dto != null ? dto.getCategory() : null;
                writeJson(resp, problemService.updateCategory(problemId, category, user.getId()));
                break;
            case "comment":
                String comment = dto != null ? dto.getAdminComment() : null;
                writeJson(resp, problemService.addComment(problemId, comment, user.getId()));
                break;
            default:
                sendNotFound(resp);
        }
    }

    // ==================== 响应便捷方法 ====================

    private void sendNotFound(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(404, "路径不存在"));
    }

    // ==================== 内部数据类 ====================

    private static class PageParams {
        final int page;
        final int pageSize;

        PageParams(int page, int pageSize) {
            this.page = page;
            this.pageSize = pageSize;
        }
    }

    // ==================== 路径解析内部类 ====================

    private static class ProblemPathInfo {
        static final ProblemPathInfo ROOT = new ProblemPathInfo(0, null, null);

        private final int problemId;
        private final String subResource;
        private final String action;

        private ProblemPathInfo(int problemId, String subResource, String action) {
            this.problemId = problemId;
            this.subResource = subResource;
            this.action = action;
        }

        static ProblemPathInfo forProblem(int problemId) {
            return new ProblemPathInfo(problemId, null, null);
        }

        static ProblemPathInfo forAction(int problemId, String action) {
            return new ProblemPathInfo(problemId, null, action);
        }

        static ProblemPathInfo forSubResource(int problemId, String subResource) {
            return new ProblemPathInfo(problemId, subResource, null);
        }

        boolean isValidProblemId() {
            return problemId > 0;
        }

        int getProblemId() {
            return problemId;
        }

        String getSubResource() {
            return subResource;
        }

        String getAction() {
            return action;
        }

        boolean hasSubResource() {
            return subResource != null && !subResource.isEmpty();
        }

        boolean isAction() {
            return action != null && !action.isEmpty();
        }

        boolean isStatusAction() {
            return "status".equals(action);
        }

        boolean isCategoryAction() {
            return "category".equals(action);
        }

        boolean isCommentAction() {
            return "comment".equals(action);
        }
    }
}
