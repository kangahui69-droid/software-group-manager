package servlet.api;

import dto.PlanDTO;
import dto.ProgressDTO;
import dto.ProjectDTO;
import dto.ProjectFilterDTO;
import model.User;
import service.ProjectService;
import servlet.BaseApiServlet;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * 项目API Servlet
 *
 * 服务分层与API化重构计划.md 5.4 ProjectApiServlet 项目API
 *
 * 端点：
 * - GET /api/projects → 项目列表（page/status/keyword）
 * - GET /api/projects/{id} → 项目详情
 * - POST /api/projects → 创建项目
 * - PUT /api/projects/{id} → 更新项目
 * - DELETE /api/projects/{id} → 删除项目
 * - POST /api/projects/{id}/apply → 申请加入
 * - POST /api/projects/{id}/approve-member → 审批成员通过
 * - POST /api/projects/{id}/reject-member → 审批成员拒绝
 * - POST /api/projects/{id}/approve → 项目审核通过
 * - POST /api/projects/{id}/reject → 项目审核驳回
 * - POST /api/projects/{id}/plans → 添加计划
 * - POST /api/projects/{id}/progress → 添加进度
 * - POST /api/projects/{id}/images → 上传项目图片
 * - POST /api/projects/{id}/files → 上传项目文件
 * - DELETE /api/projects/{id}/files/{fileId} → 删除项目文件
 * - POST /api/projects/{id}/labels/{labelCode} → 添加标签
 * - DELETE /api/projects/{id}/labels/{labelCode} → 删除标签
 * - POST /api/projects/{id}/transfer → 转让管理员
 * - GET /api/projects/my → 我的项目
 * - GET /api/projects/created-by-me → 我创建的项目
 */
@WebServlet(name = "ProjectApiServlet", urlPatterns = {"/api/projects", "/api/projects/*"})
public class ProjectApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== Action 常量 ====================

    private static final String ACTION_APPLY = "apply";
    private static final String ACTION_APPROVE_MEMBER = "approve-member";
    private static final String ACTION_REJECT_MEMBER = "reject-member";
    private static final String ACTION_APPROVE = "approve";
    private static final String ACTION_REJECT = "reject";
    private static final String ACTION_PLANS = "plans";
    private static final String ACTION_PROGRESS = "progress";
    private static final String ACTION_IMAGES = "images";
    private static final String ACTION_FILES = "files";
    private static final String ACTION_LABELS = "labels";
    private static final String ACTION_TRANSFER = "transfer";
    private static final String ACTION_MY = "my";
    private static final String ACTION_CREATED_BY_ME = "created-by-me";

    // ==================== 分页常量 ====================

    private static final int DEFAULT_PAGE_SIZE = 20;

    // ==================== Service ====================

    private transient ProjectService projectService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.projectService = new ProjectService();
    }

    // ==================== HTTP 方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);

        if (isListPath(pathInfo)) {
            handleListProjects(req, resp, currentUser);
        } else if (isMyProjectsPath(pathInfo)) {
            handleMyProjects(req, resp, currentUser);
        } else if (isCreatedByMePath(pathInfo)) {
            handleCreatedByMeProjects(req, resp, currentUser);
        } else {
            dispatchGetById(req, resp, currentUser, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        String uri = req.getRequestURI();

        if (isCreatePath(uri, pathInfo)) {
            handleCreateProject(req, resp, currentUser);
        } else {
            dispatchPostAction(req, resp, currentUser, pathInfo);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        ProjectPathInfo pi = parsePathInfo(pathInfo);

        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的项目ID");
            return;
        }
        if (pi.hasAction()) {
            sendBadRequest(resp, "不支持的操作");
            return;
        }
        handleUpdateProject(req, resp, pi.getProjectId(), currentUser);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        ProjectPathInfo pi = parsePathInfo(pathInfo);

        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的项目ID");
            return;
        }

        int projectId = pi.getProjectId();
        String action = pi.getAction();

        if (ACTION_LABELS.equals(action) && pi.hasLabelCode()) {
            handleRemoveLabel(resp, projectId, pi.getLabelCode(), currentUser);
        } else if (ACTION_FILES.equals(action) && pi.hasFileId()) {
            handleDeleteFile(resp, projectId, pi.getFileId(), currentUser);
        } else if (!pi.hasAction()) {
            handleDeleteProject(resp, projectId, currentUser);
        } else {
            sendBadRequest(resp, "不支持的操作");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // ==================== 认证方法 ====================

    private User requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = getCurrentUser(req);
        if (currentUser == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return currentUser;
    }

    // ==================== GET 路由分发 ====================

    private void dispatchGetById(HttpServletRequest req, HttpServletResponse resp, User currentUser, String pathInfo) throws IOException {
        ProjectPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的项目ID");
            return;
        }
        if (pi.hasAction()) {
            sendBadRequest(resp, "不支持的操作");
        } else {
            handleGetProjectDetail(resp, pi.getProjectId(), currentUser);
        }
    }

    // ==================== POST 路由分发 ====================

    private void dispatchPostAction(HttpServletRequest req, HttpServletResponse resp, User currentUser, String pathInfo) throws IOException {
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            sendBadRequest(resp, "无效的请求路径");
            return;
        }

        ProjectPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的项目ID");
            return;
        }

        int projectId = pi.getProjectId();
        String action = pi.getAction();

        switch (action) {
            case ACTION_APPLY:
                handleApplyMember(req, resp, projectId, currentUser);
                break;
            case ACTION_APPROVE_MEMBER:
                handleApproveMember(req, resp, projectId, currentUser);
                break;
            case ACTION_REJECT_MEMBER:
                handleRejectMember(req, resp, projectId, currentUser);
                break;
            case ACTION_APPROVE:
                handleApproveProject(resp, projectId, currentUser);
                break;
            case ACTION_REJECT:
                handleRejectProject(req, resp, projectId, currentUser);
                break;
            case ACTION_PLANS:
                handleAddPlan(req, resp, projectId, currentUser);
                break;
            case ACTION_PROGRESS:
                handleAddProgress(req, resp, projectId, currentUser);
                break;
            case ACTION_IMAGES:
                handleUploadImage(req, resp, projectId, currentUser);
                break;
            case ACTION_FILES:
                handleUploadFile(req, resp, projectId, currentUser);
                break;
            case ACTION_LABELS:
                handleAddLabel(req, resp, projectId, currentUser);
                break;
            case ACTION_TRANSFER:
                handleTransferAdmin(req, resp, projectId, currentUser);
                break;
            default:
                sendBadRequest(resp, "不支持的操作");
        }
    }

    // ==================== 路径判断辅助 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty();
    }

    private boolean isMyProjectsPath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_MY);
    }

    private boolean isCreatedByMePath(String pathInfo) {
        return pathInfo != null && pathInfo.endsWith(ACTION_CREATED_BY_ME);
    }

    private boolean isCreatePath(String uri, String pathInfo) {
        return uri.endsWith("/api/projects") || (pathInfo != null && pathInfo.equals("/"));
    }

    // ==================== 路径解析 ====================

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            String uri = req.getRequestURI();
            if (uri != null && uri.contains("/api/projects/")) {
                pathInfo = uri.substring(uri.indexOf("/api/projects/") + 14);
                if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
            }
        }
        return pathInfo;
    }

    private ProjectPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return ProjectPathInfo.root();
        }
        if (!pathInfo.startsWith("/")) {
            return ProjectPathInfo.root();
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length < 1 || segments[0].isEmpty()) {
            return ProjectPathInfo.root();
        }

        String idStr = segments[0];
        int projectId = parseIdOrZero(idStr);
        if (projectId == 0) {
            return ProjectPathInfo.root();
        }

        String action = segments.length >= 2 ? segments[1] : null;
        String labelCode = segments.length >= 3 ? segments[2] : null;
        Integer fileId = parseFileIdIfApplicable(segments, action);

        return ProjectPathInfo.of(projectId, action, labelCode, fileId);
    }

    private int parseIdOrZero(String idStr) {
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer parseFileIdIfApplicable(String[] segments, String action) {
        if (segments.length < 3 || !ACTION_FILES.equals(action)) {
            return null;
        }
        try {
            return Integer.parseInt(segments[2]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 处理器方法 ====================

    private void handleListProjects(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        int page = parseIntParam(req.getParameter("page"), 1);
        int pageSize = parseIntParam(req.getParameter("pageSize"), DEFAULT_PAGE_SIZE);

        ProjectFilterDTO filter = new ProjectFilterDTO();
        filter.setCategory(req.getParameter("category"));
        filter.setStatus(req.getParameter("status"));
        filter.setKeyword(req.getParameter("keyword"));

        writeJson(resp, projectService.listProjects(filter, page, pageSize));
    }

    private void handleGetProjectDetail(HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        writeJson(resp, projectService.getProjectDetail(projectId, currentUser.getId()));
    }

    private void handleCreateProject(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            ProjectDTO dto = getGson().fromJson(body, ProjectDTO.class);
            writeJson(resp, projectService.createProject(dto, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleUpdateProject(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            ProjectDTO dto = getGson().fromJson(body, ProjectDTO.class);
            writeJson(resp, projectService.updateProject(projectId, dto, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleDeleteProject(HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        writeJson(resp, projectService.deleteProject(projectId, currentUser.getId()));
    }

    private void handleApplyMember(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = getGson().fromJson(body, Map.class);
            String reason = data != null ? (String) data.get("reason") : null;
            writeJson(resp, projectService.applyMember(projectId, currentUser.getId(), reason));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleApproveMember(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        Integer applicationId = extractApplicationId(req);
        if (applicationId == null) {
            sendBadRequest(resp, "申请ID不能为空");
            return;
        }
        writeJson(resp, projectService.approveMember(applicationId, currentUser.getId()));
    }

    private void handleRejectMember(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = getGson().fromJson(body, Map.class);
            Integer applicationId = toInteger(data != null ? data.get("applicationId") : null);
            String reason = data != null ? (String) data.get("reason") : null;
            if (applicationId == null) {
                sendBadRequest(resp, "申请ID不能为空");
                return;
            }
            writeJson(resp, projectService.rejectMember(applicationId, reason, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleApproveProject(HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        writeJson(resp, projectService.approveProject(projectId, currentUser.getId()));
    }

    private void handleRejectProject(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = getGson().fromJson(body, Map.class);
            String reason = data != null ? (String) data.get("reason") : null;
            writeJson(resp, projectService.rejectProject(projectId, reason, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleAddPlan(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            PlanDTO dto = getGson().fromJson(body, PlanDTO.class);
            writeJson(resp, projectService.addPlan(projectId, dto, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleAddProgress(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            String body = readRequestBody(req);
            ProgressDTO dto = getGson().fromJson(body, ProgressDTO.class);
            writeJson(resp, projectService.addProgress(projectId, dto, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的请求参数"));
        }
    }

    private void handleUploadImage(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            Object part = extractFilePart(req);
            writeJson(resp, projectService.uploadProjectImage(projectId, part, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的文件"));
        }
    }

    private void handleUploadFile(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        try {
            Object part = extractFilePart(req);
            String fileType = req.getParameter("fileType");
            writeJson(resp, projectService.uploadProjectFile(projectId, part, fileType, currentUser.getId()));
        } catch (Exception e) {
            writeJson(resp, Result.error(400, "无效的文件"));
        }
    }

    private void handleDeleteFile(HttpServletResponse resp, int projectId, int fileId, User currentUser) throws IOException {
        writeJson(resp, projectService.deleteProjectFile(projectId, fileId, currentUser.getId()));
    }

    private void handleAddLabel(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        String labelCode = extractLabelCode(req.getPathInfo(), projectId);
        if (labelCode == null) {
            sendBadRequest(resp, "标签代码不能为空");
            return;
        }
        writeJson(resp, projectService.addLabel(projectId, labelCode, currentUser.getId()));
    }

    private void handleRemoveLabel(HttpServletResponse resp, int projectId, String labelCode, User currentUser) throws IOException {
        writeJson(resp, projectService.removeLabel(projectId, labelCode, currentUser.getId()));
    }

    private void handleTransferAdmin(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
        Integer newAdminId = extractNewAdminId(req);
        if (newAdminId == null) {
            sendBadRequest(resp, "新管理员ID不能为空");
            return;
        }
        writeJson(resp, projectService.transferAdmin(projectId, newAdminId, currentUser.getId()));
    }

    private void handleMyProjects(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        int page = parseIntParam(req.getParameter("page"), 1);
        writeJson(resp, projectService.getMyProjects(currentUser.getId(), page, DEFAULT_PAGE_SIZE));
    }

    private void handleCreatedByMeProjects(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        int page = parseIntParam(req.getParameter("page"), 1);
        writeJson(resp, projectService.getMyProjects(currentUser.getId(), page, DEFAULT_PAGE_SIZE));
    }

    // ==================== 参数提取辅助 ====================

    private Integer extractApplicationId(HttpServletRequest req) {
        try {
            String body = readRequestBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = getGson().fromJson(body, Map.class);
            return toInteger(data != null ? data.get("applicationId") : null);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer extractNewAdminId(HttpServletRequest req) {
        try {
            String body = readRequestBody(req);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = getGson().fromJson(body, Map.class);
            return toInteger(data != null ? data.get("newAdminId") : null);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseIntParam(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 文件处理 ====================

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

    private String extractLabelCode(String pathInfo, int projectId) {
        if (pathInfo == null) return null;
        String prefix = "/" + projectId + "/labels/";
        return pathInfo.startsWith(prefix) ? pathInfo.substring(prefix.length()) : null;
    }

    // ==================== JSON 工具 ====================

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        java.io.BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 路径解析内部类 ====================

    private static class ProjectPathInfo {
        private final int projectId;
        private final String action;
        private final String labelCode;
        private final Integer fileId;
        private final boolean isRoot;

        private ProjectPathInfo(int projectId, String action, String labelCode, Integer fileId, boolean isRoot) {
            this.projectId = projectId;
            this.action = action;
            this.labelCode = labelCode;
            this.fileId = fileId;
            this.isRoot = isRoot;
        }

        static ProjectPathInfo root() {
            return new ProjectPathInfo(0, null, null, null, true);
        }

        static ProjectPathInfo of(int projectId, String action, String labelCode, Integer fileId) {
            return new ProjectPathInfo(projectId, action, labelCode, fileId, false);
        }

        boolean isRoot() { return isRoot; }

        boolean isValidId() { return !isRoot && projectId > 0; }

        int getProjectId() { return projectId; }

        boolean hasAction() { return !isRoot && action != null && !action.isEmpty(); }

        String getAction() { return action; }

        boolean hasLabelCode() { return !isRoot && labelCode != null && !labelCode.isEmpty(); }

        String getLabelCode() { return labelCode; }

        boolean hasFileId() { return !isRoot && fileId != null && fileId > 0; }

        Integer getFileId() { return fileId; }
    }
}
