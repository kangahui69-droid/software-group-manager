package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.ActivityDTO;
import dto.ActivityFilterDTO;
import model.User;
import service.ActivityService;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * ActivityApiServlet - 活动API端点
 *
 * 路径: /api/activities/*
 * 端点:
 * - GET /api/activities → 活动列表（page/status/keyword）
 * - GET /api/activities/{id} → 活动详情
 * - POST /api/activities → 创建活动（JSON body, admin only）
 * - PUT /api/activities/{id} → 更新活动
 * - DELETE /api/activities/{id} → 删除活动
 * - POST /api/activities/{id}/register → 报名
 * - POST /api/activities/{id}/approve-user → 审批参与者通过
 * - POST /api/activities/{id}/reject-user → 审批参与者拒绝
 * - POST /api/activities/{id}/approve → 活动审核通过
 * - POST /api/activities/{id}/reject → 活动审核驳回
 * - GET /api/activities/my → 我报名的活动
 * - GET /api/activities/created-by-me → 我创建的活动
 */
@WebServlet("/api/activities/*")
public class ActivityApiServlet extends HttpServlet {

    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String ROLE_ADMIN = "ADMIN";

    private final ActivityService activityService = new ActivityService();
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    // ==================== HTTP方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        User currentUser = getCurrentUser(request);

        String pathInfo = request.getPathInfo();
        if (isActivityDetailPath(pathInfo)) {
            handleGetActivityDetail(request, response, currentUser);
        } else if (currentUser == null) {
            sendUnauthorized(response);
        } else if (isRootOrEmptyPath(pathInfo)) {
            handleListActivities(request, response, currentUser);
        } else if (pathInfo.equals("/my")) {
            handleGetMyActivities(request, response, currentUser);
        } else if (pathInfo.equals("/created-by-me")) {
            handleGetMyCreatedActivities(request, response, currentUser);
        } else {
            sendError(response, 400, "无效的请求路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        User currentUser = getCurrentUser(request);

        if (currentUser == null) {
            sendUnauthorized(response);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (isRootOrEmptyPath(pathInfo)) {
            handleCreateActivity(request, response, currentUser);
            return;
        }

        String[] segments = splitPath(pathInfo);
        if (!isValidActivityId(segments[1])) {
            sendError(response, 400, "无效的活动ID");
            return;
        }

        int activityId = Integer.parseInt(segments[1]);
        if (segments.length < 3) {
            sendError(response, 400, "未知的操作");
            return;
        }

        dispatchPostAction(request, response, currentUser, activityId, segments[2]);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        User currentUser = getCurrentUser(request);

        if (currentUser == null) {
            sendUnauthorized(response);
            return;
        }

        Integer activityId = extractActivityIdFromPath(request.getPathInfo());
        if (activityId == null) {
            sendError(response, 400, "无效的活动ID");
            return;
        }

        handleUpdateActivity(request, response, currentUser, activityId);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        User currentUser = getCurrentUser(request);

        if (currentUser == null) {
            sendUnauthorized(response);
            return;
        }

        Integer activityId = extractActivityIdFromPath(request.getPathInfo());
        if (activityId == null) {
            sendError(response, 400, "无效的活动ID");
            return;
        }

        handleDeleteActivity(request, response, currentUser, activityId);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // ==================== POST动作分发 ====================

    private void dispatchPostAction(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId, String action) throws IOException {
        switch (action) {
            case "register":
                handleRegister(request, response, currentUser, activityId);
                break;
            case "approve-user":
                handleApproveUser(request, response, currentUser, activityId);
                break;
            case "reject-user":
                handleRejectUser(request, response, currentUser, activityId);
                break;
            case "approve":
                handleApproveActivity(request, response, currentUser, activityId);
                break;
            case "reject":
                handleRejectActivity(request, response, currentUser, activityId);
                break;
            default:
                sendError(response, 400, "未知的操作");
        }
    }

    // ==================== 处理器方法 ====================

    private void handleListActivities(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        int[] pageParams = parsePageParams(request);
        ActivityFilterDTO filter = new ActivityFilterDTO();
        filter.setStatus(request.getParameter("status"));
        filter.setKeyword(request.getParameter("keyword"));

        Result result = activityService.listActivities(filter, pageParams[0], pageParams[1]);
        writeJson(response, result);
    }

    private void handleGetActivityDetail(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        Integer activityId = extractActivityIdFromPath(request.getPathInfo());
        if (activityId == null) {
            sendError(response, 400, "无效的活动ID");
            return;
        }

        Integer userId = currentUser != null ? currentUser.getId() : null;
        Result result = activityService.getActivityDetail(activityId, userId);
        writeJson(response, result);
    }

    private void handleCreateActivity(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        if (!isAdmin(currentUser)) {
            sendForbidden(response, "需要管理员权限");
            return;
        }

        ActivityDTO dto = parseJsonRequest(request, ActivityDTO.class);
        if (dto == null) {
            sendError(response, 400, "请求体不能为空");
            return;
        }

        Result result = activityService.createActivity(dto, currentUser.getId());
        writeJson(response, result);
    }

    private void handleUpdateActivity(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        ActivityDTO dto = parseJsonRequest(request, ActivityDTO.class);
        if (dto == null) {
            sendError(response, 400, "请求体不能为空");
            return;
        }

        Result result = activityService.updateActivity(activityId, dto, currentUser.getId());
        writeJson(response, result);
    }

    private void handleDeleteActivity(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        Result result = activityService.deleteActivity(activityId, currentUser.getId());
        writeJson(response, result);
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        Result result = activityService.register(activityId, currentUser.getId());
        writeJson(response, result);
    }

    private void handleApproveUser(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        if (!isAdmin(currentUser)) {
            sendForbidden(response, "需要管理员权限");
            return;
        }

        Integer targetUserId = extractUserIdFromRequest(request);
        if (targetUserId == null) {
            sendError(response, 400, "userId不能为空或无效");
            return;
        }

        Result result = activityService.approveParticipant(activityId, targetUserId, currentUser.getId());
        writeJson(response, result);
    }

    private void handleRejectUser(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        if (!isAdmin(currentUser)) {
            sendForbidden(response, "需要管理员权限");
            return;
        }

        Integer targetUserId = extractUserIdFromRequest(request);
        if (targetUserId == null) {
            sendError(response, 400, "userId不能为空或无效");
            return;
        }

        Map<String, Object> body = parseJsonRequest(request, Map.class);
        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : "";

        Result result = activityService.rejectParticipant(activityId, targetUserId, currentUser.getId());
        writeJson(response, result);
    }

    private void handleApproveActivity(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        if (!isAdmin(currentUser)) {
            sendForbidden(response, "需要管理员权限");
            return;
        }

        Result result = activityService.approveActivity(activityId, currentUser.getId());
        writeJson(response, result);
    }

    private void handleRejectActivity(HttpServletRequest request, HttpServletResponse response, User currentUser, int activityId) throws IOException {
        if (!isAdmin(currentUser)) {
            sendForbidden(response, "需要管理员权限");
            return;
        }

        Map<String, Object> body = parseJsonRequest(request, Map.class);
        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : "";

        Result result = activityService.rejectActivity(activityId, reason, currentUser.getId());
        writeJson(response, result);
    }

    private void handleGetMyActivities(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        int[] pageParams = parsePageParams(request);
        Result result = activityService.getMyActivities(currentUser.getId(), pageParams[0], pageParams[1]);
        writeJson(response, result);
    }

    private void handleGetMyCreatedActivities(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        int[] pageParams = parsePageParams(request);
        Result result = activityService.getMyCreatedActivities(currentUser.getId(), pageParams[0], pageParams[1]);
        writeJson(response, result);
    }

    // ==================== 权限检查 ====================

    private boolean isAdmin(User user) {
        return ROLE_ADMIN.equals(user != null ? user.getRole() : null);
    }

    private boolean isActivityDetailPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return false;
        }
        String[] segments = splitPath(pathInfo);
        return segments.length >= 2 && isValidActivityId(segments[1]);
    }

    // ==================== 参数解析工具 ====================

    private int[] parsePageParams(HttpServletRequest request) {
        int page = 1;
        int pageSize = DEFAULT_PAGE_SIZE;

        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");

        try {
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
            if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                pageSize = Integer.parseInt(pageSizeStr);
                if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
                if (pageSize > MAX_PAGE_SIZE) pageSize = MAX_PAGE_SIZE;
            }
        } catch (NumberFormatException ignored) {
        }

        return new int[] { page, pageSize };
    }

    private Integer extractActivityIdFromPath(String pathInfo) {
        if (isRootOrEmptyPath(pathInfo)) {
            return null;
        }
        String[] segments = splitPath(pathInfo);
        if (segments.length < 2 || !isValidActivityId(segments[1])) {
            return null;
        }
        try {
            return Integer.parseInt(segments[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer extractUserIdFromRequest(HttpServletRequest request) throws IOException {
        Map<String, Object> body = parseJsonRequest(request, Map.class);
        if (body == null || !body.containsKey("userId")) {
            return null;
        }
        try {
            return ((Number) body.get("userId")).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 工具方法 ====================

    private User getCurrentUser(HttpServletRequest request) {
        javax.servlet.http.HttpSession session = request.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }

    private <T> T parseJsonRequest(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        if (body.isEmpty() || "null".equals(body)) {
            return null;
        }
        try {
            return gson.fromJson(body, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response, Result result) throws IOException {
        response.setStatus(result.isSuccess() ? HttpServletResponse.SC_OK : result.getCode());
        PrintWriter writer = response.getWriter();
        gson.toJson(result, writer);
        writer.flush();
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        sendError(response, 401, "请先登录");
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        sendError(response, 403, message);
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        writeJson(response, Result.error(code, message));
    }

    private void setJsonContentType(HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE_JSON);
    }

    private boolean isRootOrEmptyPath(String path) {
        return path == null || path.equals("/") || path.isEmpty();
    }

    private String[] splitPath(String pathInfo) {
        return pathInfo.split("/");
    }

    private boolean isValidActivityId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
