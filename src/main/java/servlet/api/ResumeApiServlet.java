package servlet.api;

import dto.*;
import model.User;
import service.ResumeService;
import servlet.BaseApiServlet;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 简历API Servlet
 *
 * 服务分层与API化完整计划.md 4.4 ResumeApiServlet 简历API
 *
 * 端点：
 * - GET /api/resumes → 简历列表
 * - GET /api/resumes/{id} → 简历详情
 * - POST /api/resumes → 创建简历
 * - PUT /api/resumes/{id} → 更新简历（_method=PUT）
 * - DELETE /api/resumes/{id} → 删除简历
 * - PUT /api/resumes/{id}/default → 设为默认
 * - GET /api/resumes/{id}/education → 教育经历列表
 * - POST /api/resumes/{id}/education → 添加教育经历
 * - PUT /api/resumes/{id}/education/{eid} → 更新教育经历
 * - DELETE /api/resumes/{id}/education/{eid} → 删除教育经历
 * - GET /api/resumes/{id}/skills → 技能列表
 * - POST /api/resumes/{id}/skills → 添加技能
 * - PUT /api/resumes/{id}/skills/{sid} → 更新技能
 * - DELETE /api/resumes/{id}/skills/{sid} → 删除技能
 * - GET /api/resumes/{id}/projects → 项目经历列表
 * - POST /api/resumes/{id}/projects → 添加项目经历
 * - PUT /api/resumes/{id}/projects/{pid} → 更新项目经历
 * - DELETE /api/resumes/{id}/projects/{pid} → 删除项目经历
 * - GET /api/resumes/{id}/awards → 获奖情况列表
 * - POST /api/resumes/{id}/awards → 添加获奖情况
 * - PUT /api/resumes/{id}/awards/{aid} → 更新获奖情况
 * - DELETE /api/resumes/{id}/awards/{aid} → 删除获奖情况
 * - GET /api/resumes/recycle-bin → 回收站
 * - POST /api/resumes/{id}/restore → 恢复简历
 * - DELETE /api/resumes/{id}/permanent → 永久删除
 */
public class ResumeApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== Service ====================

    private transient ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.resumeService = new ResumeService();
    }

    // ==================== HTTP 方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);

        // /api/resumes/recycle-bin
        if (isRecycleBinPath(pathInfo)) {
            handleGetRecycleBin(resp, currentUser);
            return;
        }

        // /api/resumes 或 /api/resumes/
        if (isListPath(pathInfo)) {
            handleListResumes(req, resp, currentUser);
            return;
        }

        // /api/resumes/{id}
        ResumePathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的简历ID");
            return;
        }

        if (pi.hasSubResource()) {
            handleSubResourceGet(req, resp, currentUser, pi);
        } else {
            handleGetResumeDetail(resp, currentUser, pi.getResumeId());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);
        String method = req.getParameter("_method");
        boolean isPutTunnel = "PUT".equalsIgnoreCase(method);

        // /api/resumes/recycle-bin (POST不支持)
        if (isRecycleBinPath(pathInfo)) {
            sendNotFound(resp, "路径不存在");
            return;
        }

        // /api/resumes 或 /api/resumes/ - 创建简历
        if (isListPath(pathInfo)) {
            if (isPutTunnel) {
                sendBadRequest(resp, "根路径不支持PUT方法");
            } else {
                handleCreateResume(req, resp, currentUser);
            }
            return;
        }

        // /api/resumes/{id} 或子资源
        ResumePathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的简历ID");
            return;
        }

        // 特殊动作：default, restore
        if (pi.isDefaultAction()) {
            handleSetDefault(resp, currentUser, pi.getResumeId());
            return;
        }
        if (pi.isRestoreAction()) {
            handleRestore(resp, currentUser, pi.getResumeId());
            return;
        }

        // 子资源POST（添加或更新）
        if (pi.hasSubResource()) {
            handleSubResourcePost(req, resp, currentUser, pi, isPutTunnel);
        } else if (isPutTunnel) {
            // /api/resumes/{id} 更新
            handleUpdateResume(req, resp, currentUser, pi.getResumeId());
        } else {
            sendNotFound(resp, "路径不存在");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        sendMethodNotAllowed(resp, "PUT方法不支持，请使用POST with _method=PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) return;

        String pathInfo = derivePathInfo(req);

        // /api/resumes 或 /api/resumes/
        if (isListPath(pathInfo)) {
            sendNotFound(resp, "路径不存在");
            return;
        }

        ResumePathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidId()) {
            sendBadRequest(resp, "无效的简历ID");
            return;
        }

        // 特殊动作：permanent
        if (pi.isPermanentAction()) {
            handlePermanentDelete(resp, currentUser, pi.getResumeId());
            return;
        }

        // 子资源DELETE
        if (pi.hasSubResource()) {
            handleSubResourceDelete(req, resp, currentUser, pi);
        } else {
            // /api/resumes/{id} 删除
            handleDeleteResume(resp, currentUser, pi.getResumeId());
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

    // ==================== 路径判断 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty();
    }

    private boolean isRecycleBinPath(String pathInfo) {
        return pathInfo != null && pathInfo.equals("/recycle-bin");
    }

    // ==================== 处理器方法 ====================

    private void handleListResumes(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String pageStr = req.getParameter("page");
        int page = parseIntParam(pageStr, 1);
        Result result = resumeService.listResumes(user.getId(), page);
        writeJson(resp, result);
    }

    private void handleGetResumeDetail(HttpServletResponse resp, User user, int resumeId) throws IOException {
        Result result = resumeService.getResumeDetail(resumeId, user.getId());
        writeJson(resp, result);
    }

    private void handleCreateResume(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String body = readRequestBody(req);
        if (body == null || body.trim().isEmpty()) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }
        try {
            ResumeDTO dto = parseJson(body, ResumeDTO.class);
            Result result = resumeService.createResume(dto, user.getId());
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的JSON格式");
        }
    }

    private void handleUpdateResume(HttpServletRequest req, HttpServletResponse resp, User user, int resumeId) throws IOException {
        String body = readRequestBody(req);
        if (body == null || body.trim().isEmpty()) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }
        try {
            ResumeDTO dto = parseJson(body, ResumeDTO.class);
            Result result = resumeService.updateResume(resumeId, dto, user.getId());
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的JSON格式");
        }
    }

    private void handleDeleteResume(HttpServletResponse resp, User user, int resumeId) throws IOException {
        Result result = resumeService.deleteResume(resumeId, user.getId());
        writeJson(resp, result);
    }

    private void handleSetDefault(HttpServletResponse resp, User user, int resumeId) throws IOException {
        Result result = resumeService.setDefaultResume(resumeId, user.getId());
        writeJson(resp, result);
    }

    private void handleGetRecycleBin(HttpServletResponse resp, User user) throws IOException {
        Result result = resumeService.getRecycleBin(user.getId());
        writeJson(resp, result);
    }

    private void handleRestore(HttpServletResponse resp, User user, int resumeId) throws IOException {
        Result result = resumeService.restoreResume(resumeId, user.getId());
        writeJson(resp, result);
    }

    private void handlePermanentDelete(HttpServletResponse resp, User user, int resumeId) throws IOException {
        Result result = resumeService.permanentDelete(resumeId, user.getId());
        writeJson(resp, result);
    }

    // ==================== 子资源处理 ====================

    private void handleSubResourceGet(HttpServletRequest req, HttpServletResponse resp, User user, ResumePathInfo pi) throws IOException {
        // 所有子资源列表都通过getResumeDetail获取完整简历
        Result result = resumeService.getResumeDetail(pi.getResumeId(), user.getId());
        writeJson(resp, result);
    }

    private void handleSubResourcePost(HttpServletRequest req, HttpServletResponse resp, User user, ResumePathInfo pi, boolean isUpdate) throws IOException {
        String body = readRequestBody(req);
        if (body == null || body.trim().isEmpty()) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }

        try {
            Result result;
            switch (pi.getSubResource()) {
                case "education":
                    if (isUpdate) {
                        ResumeEducationDTO dto = parseJson(body, ResumeEducationDTO.class);
                        result = resumeService.updateEducation(pi.getItemId(), dto, user.getId());
                    } else {
                        ResumeEducationDTO dto = parseJson(body, ResumeEducationDTO.class);
                        result = resumeService.addEducation(pi.getResumeId(), dto, user.getId());
                    }
                    break;
                case "skills":
                    if (isUpdate) {
                        ResumeSkillDTO dto = parseJson(body, ResumeSkillDTO.class);
                        result = resumeService.updateSkill(pi.getItemId(), dto, user.getId());
                    } else {
                        ResumeSkillDTO dto = parseJson(body, ResumeSkillDTO.class);
                        result = resumeService.addSkill(pi.getResumeId(), dto, user.getId());
                    }
                    break;
                case "projects":
                    if (isUpdate) {
                        ResumeProjectDTO dto = parseJson(body, ResumeProjectDTO.class);
                        result = resumeService.updateProject(pi.getItemId(), dto, user.getId());
                    } else {
                        ResumeProjectDTO dto = parseJson(body, ResumeProjectDTO.class);
                        result = resumeService.addProject(pi.getResumeId(), dto, user.getId());
                    }
                    break;
                case "awards":
                    if (isUpdate) {
                        ResumeAwardDTO dto = parseJson(body, ResumeAwardDTO.class);
                        result = resumeService.updateAward(pi.getItemId(), dto, user.getId());
                    } else {
                        ResumeAwardDTO dto = parseJson(body, ResumeAwardDTO.class);
                        result = resumeService.addAward(pi.getResumeId(), dto, user.getId());
                    }
                    break;
                default:
                    sendNotFound(resp, "路径不存在");
                    return;
            }
            writeJson(resp, result);
        } catch (Exception e) {
            sendBadRequest(resp, "无效的JSON格式");
        }
    }

    private void handleSubResourceDelete(HttpServletRequest req, HttpServletResponse resp, User user, ResumePathInfo pi) throws IOException {
        if (pi.getItemId() <= 0) {
            sendBadRequest(resp, "无效的项目ID");
            return;
        }

        Result result;
        switch (pi.getSubResource()) {
            case "education":
                result = resumeService.deleteEducation(pi.getItemId(), user.getId());
                break;
            case "skills":
                result = resumeService.deleteSkill(pi.getItemId(), user.getId());
                break;
            case "projects":
                result = resumeService.deleteProject(pi.getItemId(), user.getId());
                break;
            case "awards":
                result = resumeService.deleteAward(pi.getItemId(), user.getId());
                break;
            default:
                sendNotFound(resp, "路径不存在");
                return;
        }
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
        if (uri == null || !uri.contains("/api/resumes/")) {
            return null;
        }
        String pathInfo = uri.substring(uri.indexOf("/api/resumes/") + 14);
        if (pathInfo.isEmpty()) {
            return null;
        }
        if (!pathInfo.startsWith("/")) {
            pathInfo = "/" + pathInfo;
        }
        return pathInfo;
    }

    private ResumePathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return ResumePathInfo.root();
        }

        if (!pathInfo.startsWith("/")) {
            return ResumePathInfo.root();
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length < 1 || segments[0].isEmpty()) {
            return ResumePathInfo.root();
        }

        int resumeId = 0;
        try {
            resumeId = Integer.parseInt(segments[0]);
        } catch (NumberFormatException e) {
            return ResumePathInfo.root();
        }

        if (segments.length == 1) {
            return ResumePathInfo.ofResume(resumeId);
        }

        String subResource = segments[1];

        // 特殊动作
        if ("default".equals(subResource)) {
            return ResumePathInfo.ofDefault(resumeId);
        }
        if ("restore".equals(subResource)) {
            return ResumePathInfo.ofRestore(resumeId);
        }
        if ("permanent".equals(subResource)) {
            return ResumePathInfo.ofPermanent(resumeId);
        }

        // 子资源
        if (segments.length >= 3) {
            int itemId = 0;
            try {
                itemId = Integer.parseInt(segments[2]);
            } catch (NumberFormatException e) {
                // ignore
            }
            return ResumePathInfo.ofSubResource(resumeId, subResource, itemId);
        }

        return ResumePathInfo.ofSubResource(resumeId, subResource, 0);
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        return getGson().fromJson(json, clazz);
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

    private String readRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void sendNotFound(HttpServletResponse resp, String message) throws IOException {
        writeJson(resp, Result.error(404, message));
    }

    private void sendMethodNotAllowed(HttpServletResponse resp, String message) throws IOException {
        writeJson(resp, Result.error(405, message));
    }

    // ==================== 路径解析内部类 ====================

    private static class ResumePathInfo {
        private final int resumeId;
        private final String subResource;
        private final int itemId;
        private final String action;
        private final boolean isRoot;

        private ResumePathInfo(int resumeId, String subResource, int itemId, String action, boolean isRoot) {
            this.resumeId = resumeId;
            this.subResource = subResource;
            this.itemId = itemId;
            this.action = action;
            this.isRoot = isRoot;
        }

        static ResumePathInfo root() {
            return new ResumePathInfo(0, null, 0, null, true);
        }

        static ResumePathInfo ofResume(int resumeId) {
            return new ResumePathInfo(resumeId, null, 0, null, false);
        }

        static ResumePathInfo ofDefault(int resumeId) {
            return new ResumePathInfo(resumeId, null, 0, "default", false);
        }

        static ResumePathInfo ofRestore(int resumeId) {
            return new ResumePathInfo(resumeId, null, 0, "restore", false);
        }

        static ResumePathInfo ofPermanent(int resumeId) {
            return new ResumePathInfo(resumeId, null, 0, "permanent", false);
        }

        static ResumePathInfo ofSubResource(int resumeId, String subResource, int itemId) {
            return new ResumePathInfo(resumeId, subResource, itemId, null, false);
        }

        boolean isRoot() {
            return isRoot;
        }

        boolean isValidId() {
            return !isRoot && resumeId > 0;
        }

        int getResumeId() {
            return resumeId;
        }

        boolean hasSubResource() {
            return !isRoot && subResource != null && !subResource.isEmpty();
        }

        String getSubResource() {
            return subResource;
        }

        int getItemId() {
            return itemId;
        }

        boolean isDefaultAction() {
            return "default".equals(action);
        }

        boolean isRestoreAction() {
            return "restore".equals(action);
        }

        boolean isPermanentAction() {
            return "permanent".equals(action);
        }
    }
}
