package servlet.api;

import dto.AwardDTO;
import model.User;
import service.AwardService;
import servlet.BaseApiServlet;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 奖项API Servlet
 *
 * 服务分层与API化重构计划.md 5.5 AwardApiServlet 奖项API
 *
 * 端点：
 * - GET /api/awards → 奖项列表（filter/page）
 * - GET /api/awards/{id} → 奖项详情
 * - POST /api/awards → 提交奖项
 * - PUT /api/awards/{id} → 更新奖项
 * - DELETE /api/awards/{id} → 删除奖项
 * - POST /api/awards/{id}/approve → 审批通过
 * - POST /api/awards/{id}/reject → 审批驳回
 * - POST /api/awards/{id}/images → 添加图片
 * - GET /api/awards/my → 我的奖项
 * - GET /api/awards/statistics → 个人统计
 * - GET /api/awards/types → 奖项类型字典
 * - GET /api/awards/categories → 奖项类别字典
 * - GET /api/awards/levels → 奖项级别字典
 * - GET /api/awards/competition-levels → 竞赛级别字典
 */
@WebServlet(name = "AwardApiServlet", urlPatterns = {"/api/awards", "/api/awards/*"})
public class AwardApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== Action 常量 ====================

    private static final String ACTION_APPROVE = "approve";
    private static final String ACTION_REJECT = "reject";
    private static final String ACTION_IMAGES = "images";
    private static final String ACTION_MY = "my";
    private static final String ACTION_STATISTICS = "statistics";
    private static final String ACTION_TYPES = "types";
    private static final String ACTION_CATEGORIES = "categories";
    private static final String ACTION_LEVELS = "levels";
    private static final String ACTION_COMPETITION_LEVELS = "competition-levels";

    // ==================== Service ====================

    private transient AwardService awardService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.awardService = new AwardService();
    }

    // ==================== HTTP 方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);

        if (isMyAwardsPath(pathInfo)) {
            handleMyAwards(resp, currentUser);
        } else if (isStatisticsPath(pathInfo)) {
            handleStatistics(resp, currentUser);
        } else if (isTypesPath(pathInfo)) {
            handleDictionary(resp, awardService.getAwardTypes());
        } else if (isCategoriesPath(pathInfo)) {
            handleDictionary(resp, awardService.getAwardCategories());
        } else if (isCompetitionLevelsPath(pathInfo)) {
            handleDictionary(resp, awardService.getCompetitionLevels());
        } else if (isLevelsPath(pathInfo)) {
            handleDictionary(resp, awardService.getAwardLevels());
        } else if (isListPath(pathInfo)) {
            handleListAwards(req, resp, currentUser);
        } else {
            dispatchGetById(resp, currentUser, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        String uri = req.getRequestURI();

        if (isCreatePath(uri, pathInfo)) {
            handleSubmitAward(req, resp, currentUser);
            return;
        }

        if (pathInfo == null) {
            pathInfo = derivePathInfoFromUri(uri);
        }

        AwardPathInfo pi = parseAndValidatePathInfo(pathInfo, resp);
        if (pi == null) return;

        dispatchPostAction(req, resp, currentUser, pi);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        AwardPathInfo pi = parseAndValidatePathInfo(pathInfo, resp);
        if (pi == null) return;

        if (pi.hasAction()) {
            sendBadRequest(resp, "不支持的操作");
        } else {
            handleUpdateAward(req, resp, pi.getAwardId(), currentUser);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        AwardPathInfo pi = parseAndValidatePathInfo(pathInfo, resp);
        if (pi == null) return;

        if (pi.hasAction()) {
            sendBadRequest(resp, "不支持的操作");
        } else {
            handleDeleteAward(resp, pi.getAwardId(), currentUser);
        }
    }

    // ==================== 认证与路径验证 ====================

    private User requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return currentUser;
    }

    private AwardPathInfo parseAndValidatePathInfo(String pathInfo, HttpServletResponse resp) throws IOException {
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            sendBadRequest(resp, "无效的请求路径");
            return null;
        }

        AwardPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的奖项ID");
            return null;
        }
        return pi;
    }

    private void dispatchPostAction(HttpServletRequest req, HttpServletResponse resp, User currentUser, AwardPathInfo pi) throws IOException {
        int awardId = pi.getAwardId();
        String action = pi.getAction();

        if (ACTION_APPROVE.equals(action)) {
            handleApproveAward(resp, awardId, currentUser);
        } else if (ACTION_REJECT.equals(action)) {
            handleRejectAward(req, resp, awardId, currentUser);
        } else if (ACTION_IMAGES.equals(action)) {
            handleAddAwardImage(req, resp, awardId, currentUser);
        } else {
            sendBadRequest(resp, "不支持的操作");
        }
    }

    // ==================== GET 路由辅助 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty();
    }

    private boolean isMyAwardsPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_MY);
    }

    private boolean isStatisticsPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_STATISTICS);
    }

    private boolean isTypesPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_TYPES);
    }

    private boolean isCategoriesPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_CATEGORIES);
    }

    private boolean isCompetitionLevelsPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_COMPETITION_LEVELS);
    }

    private boolean isLevelsPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_LEVELS);
    }

    private boolean isCreatePath(String uri, String pathInfo) {
        return uri.endsWith("/api/awards") || (pathInfo != null && pathInfo.equals("/"));
    }

    private void dispatchGetById(HttpServletResponse resp, User currentUser, String pathInfo) throws IOException {
        AwardPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的奖项ID");
            return;
        }

        if (pi.hasAction()) {
            sendBadRequest(resp, "不支持的操作");
        } else {
            handleGetAwardDetail(resp, pi.getAwardId());
        }
    }

    // ==================== 处理器方法 ====================

    private void handleListAwards(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String pageStr = req.getParameter("page");
        String filter = req.getParameter("filter");

        int page = parseIntParam(pageStr, 1);

        Result result = awardService.listAwards(filter, page);
        writeJson(resp, result);
    }

    private void handleGetAwardDetail(HttpServletResponse resp, int awardId) throws IOException {
        Result result = awardService.getAwardDetail(awardId);
        writeJson(resp, result);
    }

    private void handleSubmitAward(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            AwardDTO dto = parseJsonRequest(req, AwardDTO.class);
            Result result = awardService.submitAward(dto, currentUser.getId(), null);
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的请求参数");
        }
    }

    private void handleUpdateAward(HttpServletRequest req, HttpServletResponse resp, int awardId, User currentUser) throws IOException {
        try {
            AwardDTO dto = parseJsonRequest(req, AwardDTO.class);
            Result result = awardService.updateAward(awardId, dto, currentUser.getId());
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的请求参数");
        }
    }

    private void handleDeleteAward(HttpServletResponse resp, int awardId, User currentUser) throws IOException {
        Result result = awardService.deleteAward(awardId, currentUser.getId());
        writeJson(resp, result);
    }

    private void handleApproveAward(HttpServletResponse resp, int awardId, User currentUser) throws IOException {
        Result result = awardService.approveAward(awardId, currentUser.getId());
        writeJson(resp, result);
    }

    @SuppressWarnings("unchecked")
    private void handleRejectAward(HttpServletRequest req, HttpServletResponse resp, int awardId, User currentUser) throws IOException {
        try {
            Object body = parseJsonRequest(req);
            Map<String, Object> bodyMap = body instanceof Map ? (Map<String, Object>) body : null;
            String reason = bodyMap != null ? (String) bodyMap.get("reason") : null;
            Result result = awardService.rejectAward(awardId, reason, currentUser.getId());
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的请求参数");
        }
    }

    private void handleAddAwardImage(HttpServletRequest req, HttpServletResponse resp, int awardId, User currentUser) throws IOException {
        try {
            Object part = extractFilePart(req);
            Result result = awardService.addAwardImage(awardId, part, currentUser.getId());
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的文件");
        }
    }

    private void handleMyAwards(HttpServletResponse resp, User currentUser) throws IOException {
        Result result = awardService.getMyAwards(currentUser.getId());
        writeJson(resp, result);
    }

    private void handleStatistics(HttpServletResponse resp, User currentUser) throws IOException {
        Result result = awardService.getAwardStatistics(currentUser.getId());
        writeJson(resp, result);
    }

    private void handleDictionary(HttpServletResponse resp, Result result) throws IOException {
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

    private String derivePathInfoFromUri(String uri) {
        return extractPathInfoFromUri(uri);
    }

    private String extractPathInfoFromUri(String uri) {
        if (uri == null || !uri.contains("/api/awards/")) {
            return null;
        }
        String pathInfo = uri.substring(uri.indexOf("/api/awards/") + 12);
        if (pathInfo.isEmpty()) {
            return null;
        }
        if (!pathInfo.startsWith("/")) {
            pathInfo = "/" + pathInfo;
        }
        return pathInfo;
    }

    private AwardPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return AwardPathInfo.root();
        }

        if (!pathInfo.startsWith("/")) {
            return AwardPathInfo.root();
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length < 1 || segments[0].isEmpty()) {
            return AwardPathInfo.root();
        }

        String idStr = segments[0];
        int awardId = 0;
        try {
            awardId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return AwardPathInfo.root();
        }

        String action = segments.length >= 2 ? segments[1] : null;

        return AwardPathInfo.of(awardId, action);
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

    private AwardDTO parseJsonRequest(HttpServletRequest req, Class<AwardDTO> clazz) throws IOException {
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return getGson().fromJson(sb.toString(), clazz);
    }

    private Object extractFilePart(HttpServletRequest request) {
        try {
            if (request.getContentType() != null && request.getContentType().contains("multipart/form-data")) {
                return request.getPart("file");
            }
        } catch (Exception e) {
            // 返回null让service层处理
        }
        return null;
    }

    // ==================== 路径解析内部类 ====================

    private static class AwardPathInfo {
        private final int awardId;
        private final String action;
        private final boolean isRoot;

        private AwardPathInfo(int awardId, String action, boolean isRoot) {
            this.awardId = awardId;
            this.action = action;
            this.isRoot = isRoot;
        }

        static AwardPathInfo root() {
            return new AwardPathInfo(0, null, true);
        }

        static AwardPathInfo of(int awardId, String action) {
            return new AwardPathInfo(awardId, action, false);
        }

        boolean isRoot() {
            return isRoot;
        }

        boolean isValidId() {
            return !isRoot && awardId > 0;
        }

        int getAwardId() {
            return awardId;
        }

        boolean hasAction() {
            return !isRoot && action != null && !action.isEmpty();
        }

        String getAction() {
            return action;
        }
    }
}
