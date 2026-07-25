package servlet;

import dto.GroupDTO;
import model.User;
import service.GroupService;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 群聊API Servlet
 *
 * 服务分层与API化重构计划.md 4.1 GroupService 群聊服务
 *
 * 端点：
 * - GET /api/groups → 群聊列表
 * - GET /api/groups/{id} → 群聊详情
 * - POST /api/groups → 创建群聊
 * - PUT /api/groups/{id} → 更新群聊
 * - DELETE /api/groups/{id} → 删除群聊
 * - POST /api/groups/{id}/members → 添加成员
 * - DELETE /api/groups/{id}/members/{userId} → 移除成员
 * - GET /api/groups/{id}/messages → 消息历史
 * - POST /api/groups/{id}/messages → 发送消息
 * - POST /api/groups/{id}/mute → 禁言
 * - POST /api/groups/{id}/unmute → 取消禁言
 * - DELETE /api/groups/{id}/messages/{msgId} → 删除消息
 * - GET /api/groups/my → 我的群聊
 * - GET /api/groups/created-by-me → 我创建的
 */
@WebServlet("/api/groups/*")
public class GroupApiServlet extends BaseApiServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final GroupService groupService;

    public GroupApiServlet() {
        this.groupService = new GroupService();
    }

    public GroupApiServlet(GroupService groupService) {
        this.groupService = groupService;
    }

    // ==================== HTTP Method Handlers ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String path = req.getPathInfo();
        Route route = matchGetRoute(path);

        switch (route) {
            case LIST_GROUPS:
                listGroups(req, resp, currentUser);
                break;
            case MY_GROUPS:
                getMyGroups(req, resp, currentUser);
                break;
            case CREATED_GROUPS:
                getCreatedGroups(req, resp, currentUser);
                break;
            case GROUP_MESSAGES:
                handleGroupMessages(req, resp, currentUser, path);
                break;
            case GROUP_DETAIL:
                handleGroupDetail(req, resp, currentUser, path);
                break;
            case NOT_FOUND:
            default:
                sendError(resp, 404, "未找到请求的路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String path = req.getPathInfo();
        Route route = matchPostRoute(path);

        switch (route) {
            case CREATE_GROUP:
                createGroup(req, resp, currentUser);
                break;
            case SEND_MESSAGE:
                handleSendMessage(req, resp, currentUser, path);
                break;
            case ADD_MEMBER:
                handleAddMember(req, resp, currentUser, path);
                break;
            case MUTE_MEMBER:
                handleMute(req, resp, currentUser, path);
                break;
            case UNMUTE_MEMBER:
                handleUnmute(req, resp, currentUser, path);
                break;
            case NOT_FOUND:
            default:
                sendError(resp, 404, "未找到请求的路径");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String path = req.getPathInfo();
        if (isGroupDetailPath(path)) {
            handleUpdateGroup(req, resp, currentUser, path);
        } else {
            sendError(resp, 404, "未找到请求的路径");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        String path = req.getPathInfo();
        Route route = matchDeleteRoute(path);

        switch (route) {
            case DELETE_GROUP:
                handleDeleteGroup(req, resp, currentUser, path);
                break;
            case REMOVE_MEMBER:
                handleRemoveMember(req, resp, currentUser, path);
                break;
            case DELETE_MESSAGE:
                handleDeleteMessage(req, resp, currentUser, path);
                break;
            case NOT_FOUND:
            default:
                sendError(resp, 404, "未找到请求的路径");
        }
    }

    // ==================== Route Matching ====================

    private enum Route {
        LIST_GROUPS,
        MY_GROUPS,
        CREATED_GROUPS,
        GROUP_DETAIL,
        GROUP_MESSAGES,
        CREATE_GROUP,
        SEND_MESSAGE,
        ADD_MEMBER,
        MUTE_MEMBER,
        UNMUTE_MEMBER,
        DELETE_GROUP,
        REMOVE_MEMBER,
        DELETE_MESSAGE,
        UPDATE_GROUP,
        NOT_FOUND
    }

    private Route matchGetRoute(String path) {
        if (path == null || path.equals("/") || path.equals("")) {
            return Route.LIST_GROUPS;
        }
        if (path.equals("/my")) {
            return Route.MY_GROUPS;
        }
        if (path.equals("/created-by-me")) {
            return Route.CREATED_GROUPS;
        }
        if (path.matches("/\\d+/messages")) {
            return Route.GROUP_MESSAGES;
        }
        if (path.matches("/\\d+")) {
            return Route.GROUP_DETAIL;
        }
        return Route.NOT_FOUND;
    }

    private Route matchPostRoute(String path) {
        if (path == null || path.equals("/") || path.equals("")) {
            return Route.CREATE_GROUP;
        }
        if (path.matches("/\\d+/messages")) {
            return Route.SEND_MESSAGE;
        }
        if (path.matches("/\\d+/members")) {
            return Route.ADD_MEMBER;
        }
        if (path.matches("/\\d+/mute")) {
            return Route.MUTE_MEMBER;
        }
        if (path.matches("/\\d+/unmute")) {
            return Route.UNMUTE_MEMBER;
        }
        return Route.NOT_FOUND;
    }

    private Route matchDeleteRoute(String path) {
        if (path.matches("/\\d+$")) {
            return Route.DELETE_GROUP;
        }
        if (path.matches("/\\d+/members/\\d+")) {
            return Route.REMOVE_MEMBER;
        }
        if (path.matches("/\\d+/messages/\\d+")) {
            return Route.DELETE_MESSAGE;
        }
        return Route.NOT_FOUND;
    }

    private boolean isGroupDetailPath(String path) {
        return path != null && path.matches("/\\d+");
    }

    // ==================== Authentication Helper ====================

    private User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return user;
    }

    // ==================== ID Extraction Helpers ====================

    private Integer extractFirstNumericId(String path) {
        if (path == null || path.isEmpty()) return null;
        for (String part : path.split("/")) {
            if (part.matches("\\d+")) {
                return Integer.parseInt(part);
            }
        }
        return null;
    }

    private Integer extractLastNumericId(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] parts = path.split("/");
        if (parts.length > 0) {
            String last = parts[parts.length - 1];
            if (last.matches("\\d+")) {
                return Integer.parseInt(last);
            }
        }
        return null;
    }

    private void sendInvalidGroupIdError(HttpServletResponse resp) throws IOException {
        sendBadRequest(resp, "无效的群组ID");
    }

    private void sendInvalidParamError(HttpServletResponse resp) throws IOException {
        sendBadRequest(resp, "无效的参数");
    }

    // ==================== Business Methods - Group CRUD ====================

    private void listGroups(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = getPageParam(req, "page", DEFAULT_PAGE);
        int pageSize = getPageParam(req, "pageSize", DEFAULT_PAGE_SIZE);

        Result result = groupService.listGroups(page, pageSize);
        writeJson(resp, result);
    }

    private void handleGroupDetail(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }
        Result result = groupService.getGroupDetail(groupId, user.getId());
        writeJson(resp, result);
    }

    private void handleUpdateGroup(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }

        Map<String, Object> body = parseRequestBody(req);
        if (body == null) {
            sendBadRequest(resp, "无效的请求参数");
            return;
        }

        GroupDTO dto = toGroupDTO(body);
        Result result = groupService.updateGroup(groupId, dto, user.getId());
        writeJson(resp, result);
    }

    private void handleDeleteGroup(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }
        Result result = groupService.deleteGroup(groupId, user.getId());
        writeJson(resp, result);
    }

    // ==================== Business Methods - Create Group ====================

    private void createGroup(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        Map<String, Object> body = parseRequestBody(req);
        if (body == null) {
            sendBadRequest(resp, "无效的请求参数");
            return;
        }

        GroupDTO dto = toGroupDTO(body);
        Result result = groupService.createGroup(dto, user.getId());
        writeJson(resp, result);
    }

    // ==================== Business Methods - Members ====================

    private void handleAddMember(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }

        Map<String, Object> body = parseRequestBody(req);
        if (body == null) {
            sendBadRequest(resp, "无效的请求参数");
            return;
        }

        Integer targetUserId = toInteger(body.get("userId"));
        if (targetUserId == null) {
            sendBadRequest(resp, "成员ID不能为空");
            return;
        }

        Result result = groupService.addMember(groupId, targetUserId, user.getId());
        writeJson(resp, result);
    }

    private void handleRemoveMember(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        Integer targetUserId = extractLastNumericId(path);
        if (groupId == null || targetUserId == null) {
            sendInvalidParamError(resp);
            return;
        }

        Result result = groupService.removeMember(groupId, targetUserId, user.getId());
        writeJson(resp, result);
    }

    // ==================== Business Methods - Messages ====================

    private void handleGroupMessages(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }

        int page = getPageParam(req, "page", DEFAULT_PAGE);
        Result result = groupService.getMessages(groupId, page);
        writeJson(resp, result);
    }

    private void handleSendMessage(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }

        Map<String, Object> body = parseRequestBody(req);
        if (body == null) {
            sendBadRequest(resp, "无效的请求参数");
            return;
        }

        Object contentObj = body.get("content");
        if (contentObj == null) {
            sendBadRequest(resp, "消息内容不能为空");
            return;
        }

        String content = contentObj.toString();
        Result result = groupService.sendMessage(groupId, user.getId(), content);
        writeJson(resp, result);
    }

    private void handleDeleteMessage(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer messageId = extractLastNumericId(path);
        if (messageId == null) {
            sendBadRequest(resp, "无效的消息ID");
            return;
        }

        Result result = groupService.deleteMessage(messageId, user.getId());
        writeJson(resp, result);
    }

    // ==================== Business Methods - Mute ====================

    private void handleMute(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }

        Map<String, Object> body = parseRequestBody(req);
        if (body == null) {
            sendBadRequest(resp, "无效的请求参数");
            return;
        }

        Integer targetUserId = toInteger(body.get("targetUserId"));
        if (targetUserId == null) {
            sendBadRequest(resp, "目标用户ID不能为空");
            return;
        }

        String reason = getString(body.get("reason"));
        Date until = toDate(body.get("until"));

        Result result = groupService.muteMember(groupId, targetUserId, until, reason);
        writeJson(resp, result);
    }

    private void handleUnmute(HttpServletRequest req, HttpServletResponse resp, User user, String path) throws IOException {
        Integer groupId = extractFirstNumericId(path);
        if (groupId == null) {
            sendInvalidGroupIdError(resp);
            return;
        }

        Result result = groupService.unmuteMember(groupId, user.getId());
        writeJson(resp, result);
    }

    // ==================== Business Methods - My Groups ====================

    private void getMyGroups(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = getPageParam(req, "page", DEFAULT_PAGE);
        Result result = groupService.getMyGroups(user.getId(), page);
        writeJson(resp, result);
    }

    private void getCreatedGroups(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = getPageParam(req, "page", DEFAULT_PAGE);
        Result result = groupService.getCreatedGroups(user.getId(), page);
        writeJson(resp, result);
    }

    // ==================== Request Parsing Helpers ====================

    private Map<String, Object> parseRequestBody(HttpServletRequest request) {
        try {
            Object obj = parseJsonRequest(request);
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                return map;
            }
        } catch (IOException e) {
            // Return null on parse error
        }
        return null;
    }

    private int getPageParam(HttpServletRequest req, String paramName, int defaultValue) {
        String value = req.getParameter(paramName);
        return parseIntOrDefault(value, defaultValue);
    }

    private GroupDTO toGroupDTO(Map<String, Object> map) {
        GroupDTO dto = new GroupDTO();
        dto.setGroupName(getString(map.get("groupName")));
        dto.setActivityId(toInteger(map.get("activityId")));
        return dto;
    }

    // ==================== Type Conversion Helpers ====================

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getString(Object value) {
        if (value == null) return null;
        return value.toString();
    }

    private Date toDate(Object value) {
        if (value == null) return null;
        if (value instanceof Date) return (Date) value;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            return sdf.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private int parseIntOrDefault(String str, int defaultValue) {
        if (str == null || str.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
