package servlet.api;

import dto.RecruitApplicationDTO;
import model.User;
import service.RecruitService;
import servlet.BaseApiServlet;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 招新API Servlet
 *
 * 服务分层与API化重构计划.md 4.3 RecruitApiServlet 招新API
 *
 * 端点：
 * - GET /api/recruit → 申请列表（year/status/keyword/round）
 * - GET /api/recruit/{id} → 申请详情
 * - POST /api/recruit → 提交申请
 * - POST /api/recruit/{id}/approve → 审批通过
 * - POST /api/recruit/{id}/reject → 审批驳回
 * - DELETE /api/recruit/{id} → 删除申请
 * - GET /api/recruit/years → 所有年份
 * - GET /api/recruit/count → 待审核数量
 */
public class RecruitApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    private static final String ACTION_APPROVE = "approve";
    private static final String ACTION_REJECT = "reject";

    private transient RecruitService recruitService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.recruitService = new RecruitService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        String pathInfo = derivePathInfo(req);

        if (isListPath(pathInfo)) {
            handleListApplications(req, resp);
        } else if (isYearsPath(pathInfo)) {
            handleGetYears(resp);
        } else if (isCountPath(pathInfo)) {
            handleGetCount(resp);
        } else {
            dispatchGetById(resp, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        String pathInfo = derivePathInfo(req);
        String uri = req.getRequestURI();

        if (isCreatePath(uri, pathInfo)) {
            handleSubmitApplication(req, resp);
            return;
        }

        try {
            RecruitPathInfo pi = parseAndValidatePathInfo(pathInfo);
            String action = pi.getAction();
            if (ACTION_APPROVE.equals(action)) {
                handleApproveApplication(resp, pi.getId(), currentUser);
            } else if (ACTION_REJECT.equals(action)) {
                handleRejectApplication(resp, pi.getId(), currentUser);
            } else {
                sendNotFound(resp, "未找到对应接口");
            }
        } catch (InvalidPathException e) {
            sendBadRequest(resp, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        String pathInfo = derivePathInfo(req);
        try {
            RecruitPathInfo pi = parseAndValidatePathInfo(pathInfo);
            if (pi.hasAction()) {
                sendBadRequest(resp, "不支持的操作");
            } else {
                handleDeleteApplication(resp, pi.getId());
            }
        } catch (InvalidPathException e) {
            sendBadRequest(resp, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        sendMethodNotAllowed(resp, "不支持的请求方法");
    }

    // ==================== 认证 ====================

    private User requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return currentUser;
    }

    // ==================== 路径验证 ====================

    private RecruitPathInfo parseAndValidatePathInfo(String pathInfo) {
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            throw new InvalidPathException("无效的请求路径");
        }
        RecruitPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            throw new InvalidPathException("无效的申请ID");
        }
        return pi;
    }

    private void dispatchGetById(HttpServletResponse resp, String pathInfo) throws IOException {
        RecruitPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的申请ID");
            return;
        }
        if (pi.hasAction()) {
            sendBadRequest(resp, "不支持的操作");
        } else {
            handleGetApplicationDetail(resp, pi.getId());
        }
    }

    // ==================== 路由辅助方法 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty();
    }

    private boolean isYearsPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith("/years");
    }

    private boolean isCountPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith("/count");
    }

    private boolean isCreatePath(String uri, String pathInfo) {
        return uri.endsWith("/api/recruit") || (pathInfo != null && pathInfo.equals("/"));
    }

    // ==================== 处理器方法 ====================

    private void handleListApplications(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String yearStr = req.getParameter("year");
        String status = req.getParameter("status");
        String keyword = req.getParameter("keyword");
        String roundStr = req.getParameter("round");

        Integer year = parseIntParam(yearStr, null);
        Integer round = parseIntParam(roundStr, null);

        Result result = recruitService.listApplications(year, status, keyword, round);
        writeJson(resp, result);
    }

    private void handleGetApplicationDetail(HttpServletResponse resp, int id) throws IOException {
        Result result = recruitService.getApplicationDetail(id);
        writeJson(resp, result);
    }

    private void handleSubmitApplication(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RecruitApplicationDTO dto = parseJsonRequest(req, RecruitApplicationDTO.class);
            if (dto == null) {
                sendBadRequest(resp, "无效的请求数据");
                return;
            }
            Result result = recruitService.submitApplication(dto);
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的请求参数");
        }
    }

    private void handleApproveApplication(HttpServletResponse resp, int id, User currentUser) throws IOException {
        Result result = recruitService.approveApplication(id, currentUser.getId());
        writeJson(resp, result);
    }

    private void handleRejectApplication(HttpServletResponse resp, int id, User currentUser) throws IOException {
        Result result = recruitService.rejectApplication(id, currentUser.getId());
        writeJson(resp, result);
    }

    private void handleDeleteApplication(HttpServletResponse resp, int id) throws IOException {
        Result result = recruitService.deleteApplication(id);
        writeJson(resp, result);
    }

    private void handleGetYears(HttpServletResponse resp) throws IOException {
        Result result = recruitService.findAllYears();
        writeJson(resp, result);
    }

    private void handleGetCount(HttpServletResponse resp) throws IOException {
        Result result = recruitService.countPending();
        writeJson(resp, result);
    }

    // ==================== 工具方法 ====================

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            pathInfo = extractPathInfoFromUri(req.getRequestURI());
        }
        return pathInfo;
    }

    private String extractPathInfoFromUri(String uri) {
        if (uri == null || !uri.contains("/api/recruit/")) {
            return null;
        }
        String pathInfo = uri.substring(uri.indexOf("/api/recruit/") + 14);
        if (pathInfo.isEmpty()) {
            return null;
        }
        if (!pathInfo.startsWith("/")) {
            pathInfo = "/" + pathInfo;
        }
        return pathInfo;
    }

    private RecruitPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return RecruitPathInfo.root();
        }

        if (!pathInfo.startsWith("/")) {
            return RecruitPathInfo.root();
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length < 1 || segments[0].isEmpty()) {
            return RecruitPathInfo.root();
        }

        String idStr = segments[0];
        int id = 0;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return RecruitPathInfo.root();
        }

        String action = segments.length >= 2 ? segments[1] : null;

        return RecruitPathInfo.of(id, action);
    }

    private Integer parseIntParam(String value, Integer defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private <T> T parseJsonRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return getGson().fromJson(sb.toString(), clazz);
    }

    // ==================== 响应辅助方法 ====================

    private void sendNotFound(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(404, message));
    }

    private void sendMethodNotAllowed(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(405, message));
    }

    // ==================== 路径解析内部类 ====================

    private static class RecruitPathInfo {
        private final int id;
        private final String action;
        private final boolean isRoot;

        private RecruitPathInfo(int id, String action, boolean isRoot) {
            this.id = id;
            this.action = action;
            this.isRoot = isRoot;
        }

        static RecruitPathInfo root() {
            return new RecruitPathInfo(0, null, true);
        }

        static RecruitPathInfo of(int id, String action) {
            return new RecruitPathInfo(id, action, false);
        }

        boolean isRoot() {
            return isRoot;
        }

        boolean isValidId() {
            return !isRoot && id > 0;
        }

        int getId() {
            return id;
        }

        boolean hasAction() {
            return !isRoot && action != null && !action.isEmpty();
        }

        String getAction() {
            return action;
        }
    }

    // ==================== 异常类 ====================

    private static class InvalidPathException extends RuntimeException {
        InvalidPathException(String message) {
            super(message);
        }
    }
}
