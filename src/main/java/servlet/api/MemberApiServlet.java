package servlet.api;

import model.User;
import service.MemberService;
import servlet.BaseApiServlet;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 成员管理API Servlet
 *
 * 服务分层与API化完整计划.md 5.3 MemberService API化
 *
 * 端点：
 * - GET  /api/members           → 成员列表(分页)
 * - GET  /api/members/{id}     → 成员详情
 * - POST /api/members           → 创建成员
 * - PUT  /api/members/{id}     → 更新成员
 * - DELETE /api/members/{id}   → 删除成员
 * - POST /api/members/{id}/enable   → 启用成员
 * - POST /api/members/{id}/disable   → 禁用成员
 * - POST /api/members/{id}/reset-password → 重置密码
 * - GET  /api/members/{id}/awards → 成员获奖列表
 * - GET  /api/members/{id}/profile → 获取个人档案
 * - PUT  /api/members/{id}/profile → 更新个人档案
 * - POST /api/members/{id}/avatar → 上传头像
 */
@WebServlet(name = "MemberApiServlet", urlPatterns = {"/api/members/*"})
public class MemberApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== 路径段常量 ====================
    private static final String PATH_ENABLE = "enable";
    private static final String PATH_DISABLE = "disable";
    private static final String PATH_RESET_PASSWORD = "reset-password";
    private static final String PATH_AVATARS = "avatar";
    private static final String PATH_AWARDS = "awards";
    private static final String PATH_PROFILE = "profile";

    private transient MemberService memberService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.memberService = new MemberService();
    }

    // ==================== HTTP Method分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);
        if (isListPath(pathInfo)) {
            handleListMembers(req, resp, user);
        } else {
            handleMemberGet(req, resp, user, pathInfo);
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
            handleMemberPost(req, resp, user, pathInfo, isPutTunnel);
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
            sendError(resp, 404, "路径不存在");
        } else {
            handleMemberDelete(req, resp, user, pathInfo);
        }
    }

    // ==================== 认证辅助 ====================

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

    // ==================== GET 请求处理 ====================

    private void handleListMembers(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = parseIntParam(req.getParameter("page"), 1);
        int pageSize = parseIntParam(req.getParameter("pageSize"), 20);
        String keyword = req.getParameter("keyword");
        String role = req.getParameter("role");
        String status = req.getParameter("status");

        Map<String, Object> filter = new HashMap<>();
        if (keyword != null && !keyword.isEmpty()) filter.put("keyword", keyword);
        if (role != null && !role.isEmpty()) filter.put("role", role);
        if (status != null && !status.isEmpty()) filter.put("status", status);

        writeJson(resp, memberService.listMembers(filter, page, pageSize));
    }

    private void handleMemberGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        MemberPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidMemberId()) {
            sendBadRequest(resp, "无效的成员ID");
            return;
        }

        if (pi.hasSubResource()) {
            dispatchSubResourceGet(pi, req, resp, user);
        } else {
            writeJson(resp, memberService.getMemberDetail(pi.getMemberId()));
        }
    }

    private void dispatchSubResourceGet(MemberPathInfo pi, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int memberId = pi.getMemberId();
        String subResource = pi.getSubResource();

        if (PATH_AWARDS.equals(subResource)) {
            writeJson(resp, memberService.getMemberAwards(memberId));
        } else if (PATH_PROFILE.equals(subResource)) {
            writeJson(resp, memberService.getProfile(memberId));
        } else {
            sendError(resp, 404, "路径不存在");
        }
    }

    // ==================== POST 请求处理 ====================

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> dto = (Map<String, Object>) parseJsonRequest(req);
        if (dto == null) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }
        writeJson(resp, memberService.createMember(dto));
    }

    private void handleMemberPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo, boolean isPutTunnel) throws IOException {
        MemberPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidMemberId()) {
            sendBadRequest(resp, "无效的成员ID");
            return;
        }

        if (pi.isAction()) {
            dispatchActionRequest(pi, req, resp, user);
        } else if (isPutTunnel) {
            dispatchPutRequest(pi, req, resp, user);
        } else {
            sendError(resp, 404, "路径不存在");
        }
    }

    private void dispatchActionRequest(MemberPathInfo pi, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int memberId = pi.getMemberId();
        String action = pi.getAction();

        switch (action) {
            case PATH_ENABLE:
                writeJson(resp, memberService.enableMember(memberId, user.getId()));
                break;
            case PATH_DISABLE:
                writeJson(resp, memberService.disableMember(memberId, user.getId()));
                break;
            case PATH_RESET_PASSWORD:
                writeJson(resp, memberService.resetPassword(memberId, user.getId()));
                break;
            case PATH_AVATARS:
                handleUploadAvatar(req, resp, user, memberId);
                break;
            default:
                sendError(resp, 404, "路径不存在");
        }
    }

    private void dispatchPutRequest(MemberPathInfo pi, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        if (pi.hasSubResource() && PATH_PROFILE.equals(pi.getSubResource())) {
            handleProfileUpdate(req, resp, user, pi.getMemberId());
        } else if (pi.hasSubResource() && PATH_AVATARS.equals(pi.getSubResource())) {
            handleUploadAvatar(req, resp, user, pi.getMemberId());
        } else {
            handleUpdate(req, resp, user, pi.getMemberId());
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int memberId) throws IOException {
        Map<String, Object> dto = parseJsonBody(req, resp);
        if (dto == null) return;
        writeJson(resp, memberService.updateMember(memberId, dto, user.getId()));
    }

    private void handleProfileUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int memberId) throws IOException {
        Map<String, Object> dto = parseJsonBody(req, resp);
        if (dto == null) return;
        writeJson(resp, memberService.updateProfile(memberId, dto, user.getId()));
    }

    private Map<String, Object> parseJsonBody(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> dto = (Map<String, Object>) parseJsonRequest(req);
        if (dto == null) {
            sendBadRequest(resp, "无效的JSON格式");
            return null;
        }
        return dto;
    }

    private void handleUploadAvatar(HttpServletRequest req, HttpServletResponse resp, User user, int memberId) throws IOException {
        String filename = req.getParameter("filename");
        InputStream fileStream = req.getInputStream();
        writeJson(resp, memberService.uploadAvatar(memberId, fileStream, filename, user.getId()));
    }

    // ==================== DELETE 请求处理 ====================

    private void handleMemberDelete(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        MemberPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidMemberId()) {
            sendBadRequest(resp, "无效的成员ID");
            return;
        }

        if (pi.hasSubResource() || pi.isAction()) {
            sendError(resp, 404, "路径不存在");
        } else {
            writeJson(resp, memberService.deleteMember(pi.getMemberId(), user.getId()));
        }
    }

    // ==================== 路径工具方法 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/");
    }

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            String uri = req.getRequestURI();
            int idx = uri.indexOf("/api/members/");
            if (idx >= 0) {
                pathInfo = uri.substring(idx + 13);
                if (pathInfo.isEmpty()) {
                    pathInfo = null;
                } else if (!pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
            }
        }
        return pathInfo;
    }

    private MemberPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return MemberPathInfo.ROOT;
        }

        if (!pathInfo.startsWith("/")) {
            return MemberPathInfo.ROOT;
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length == 0 || segments[0].isEmpty()) {
            return MemberPathInfo.ROOT;
        }

        int memberId = parseIntOrZero(segments[0]);

        if (segments.length == 1) {
            return MemberPathInfo.forMember(memberId);
        }

        String segment1 = segments[1];

        if (PATH_ENABLE.equals(segment1)) {
            return MemberPathInfo.forAction(memberId, PATH_ENABLE);
        }
        if (PATH_DISABLE.equals(segment1)) {
            return MemberPathInfo.forAction(memberId, PATH_DISABLE);
        }
        if (PATH_RESET_PASSWORD.equals(segment1)) {
            return MemberPathInfo.forAction(memberId, PATH_RESET_PASSWORD);
        }
        if (PATH_AWARDS.equals(segment1)) {
            return MemberPathInfo.forSubResource(memberId, PATH_AWARDS);
        }
        if (PATH_PROFILE.equals(segment1)) {
            return MemberPathInfo.forSubResource(memberId, PATH_PROFILE);
        }
        if (PATH_AVATARS.equals(segment1)) {
            return MemberPathInfo.forAction(memberId, PATH_AVATARS);
        }

        return MemberPathInfo.forSubResource(memberId, segment1);
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

    // ==================== 路径解析内部类 ====================

    private static class MemberPathInfo {
        static final MemberPathInfo ROOT = new MemberPathInfo(0, null, null);

        private final int memberId;
        private final String subResource;
        private final String action;

        private MemberPathInfo(int memberId, String subResource, String action) {
            this.memberId = memberId;
            this.subResource = subResource;
            this.action = action;
        }

        static MemberPathInfo forMember(int memberId) {
            return new MemberPathInfo(memberId, null, null);
        }

        static MemberPathInfo forAction(int memberId, String action) {
            return new MemberPathInfo(memberId, null, action);
        }

        static MemberPathInfo forSubResource(int memberId, String subResource) {
            return new MemberPathInfo(memberId, subResource, null);
        }

        boolean isValidMemberId() {
            return memberId > 0;
        }

        int getMemberId() {
            return memberId;
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
    }
}
