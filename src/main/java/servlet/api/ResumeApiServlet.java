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
 */
public class ResumeApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== 子资源类型 ====================

    private enum SubResource {
        EDUCATION("education"),
        SKILLS("skills"),
        PROJECTS("projects"),
        AWARDS("awards");

        private final String path;

        SubResource(String path) {
            this.path = path;
        }

        static SubResource fromPath(String path) {
            if (path == null) return null;
            for (SubResource sr : values()) {
                if (sr.path.equals(path)) {
                    return sr;
                }
            }
            return null;
        }
    }

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
        User user = requireAuth(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);

        if (isRecycleBinPath(pathInfo)) {
            writeJson(resp, resumeService.getRecycleBin(user.getId()));
        } else if (isListPath(pathInfo)) {
            handleListResumes(req, resp, user);
        } else {
            handleResumeGet(req, resp, user, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);
        boolean isPutTunnel = "PUT".equalsIgnoreCase(req.getParameter("_method"));

        if (isRecycleBinPath(pathInfo)) {
            sendNotFound(resp);
        } else if (isListPath(pathInfo)) {
            if (isPutTunnel) {
                sendBadRequest(resp, "根路径不支持PUT方法");
            } else {
                handleCreate(req, resp, user);
            }
        } else {
            handleResumePost(req, resp, user, pathInfo, isPutTunnel);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;
        sendMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);

        if (isListPath(pathInfo)) {
            sendNotFound(resp);
        } else {
            handleResumeDelete(req, resp, user, pathInfo);
        }
    }

    // ==================== 简历主资源处理 ====================

    private void handleListResumes(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = parseIntParam(req.getParameter("page"), 1);
        writeJson(resp, resumeService.listResumes(user.getId(), page));
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String body = readBody(req);
        ResumeDTO dto = parseJsonRequest(body, ResumeDTO.class);
        if (dto == null) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }
        writeJson(resp, resumeService.createResume(dto, user.getId()));
    }

    private void handleResumeGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        ResumePathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidResumeId()) {
            sendBadRequest(resp, "无效的简历ID");
            return;
        }

        if (pi.hasSubResource()) {
            writeJson(resp, resumeService.getResumeDetail(pi.getResumeId(), user.getId()));
        } else {
            writeJson(resp, resumeService.getResumeDetail(pi.getResumeId(), user.getId()));
        }
    }

    private void handleResumePost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo, boolean isPutTunnel) throws IOException {
        ResumePathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidResumeId()) {
            sendBadRequest(resp, "无效的简历ID");
            return;
        }

        // 特殊动作
        if (pi.isDefaultAction()) {
            writeJson(resp, resumeService.setDefaultResume(pi.getResumeId(), user.getId()));
        } else if (pi.isRestoreAction()) {
            writeJson(resp, resumeService.restoreResume(pi.getResumeId(), user.getId()));
        } else if (pi.hasSubResource()) {
            handleSubResourcePost(req, resp, user, pi, isPutTunnel);
        } else if (isPutTunnel) {
            handleUpdate(req, resp, user, pi.getResumeId());
        } else {
            sendNotFound(resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int resumeId) throws IOException {
        String body = readBody(req);
        ResumeDTO dto = parseJsonRequest(body, ResumeDTO.class);
        if (dto == null) {
            sendBadRequest(resp, "无效的JSON格式");
            return;
        }
        writeJson(resp, resumeService.updateResume(resumeId, dto, user.getId()));
    }

    private void handleResumeDelete(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        ResumePathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidResumeId()) {
            sendBadRequest(resp, "无效的简历ID");
            return;
        }

        if (pi.isPermanentAction()) {
            writeJson(resp, resumeService.permanentDelete(pi.getResumeId(), user.getId()));
        } else if (pi.hasSubResource()) {
            handleSubResourceDelete(resp, user, pi);
        } else {
            writeJson(resp, resumeService.deleteResume(pi.getResumeId(), user.getId()));
        }
    }

    // ==================== 子资源处理 ====================

    private void handleSubResourcePost(HttpServletRequest req, HttpServletResponse resp, User user, ResumePathInfo pi, boolean isUpdate) throws IOException {
        String body = readBody(req);
        SubResource sr = SubResource.fromPath(pi.getSubResource());

        if (sr == null) {
            sendNotFound(resp);
            return;
        }

        if (isUpdate) {
            if (pi.getItemId() <= 0) {
                sendBadRequest(resp, "无效的项目ID");
                return;
            }
            writeJson(resp, dispatchSubResourceUpdate(sr, pi.getItemId(), body, user.getId()));
        } else {
            writeJson(resp, dispatchSubResourceAdd(sr, pi.getResumeId(), body, user.getId()));
        }
    }

    private Result dispatchSubResourceAdd(SubResource sr, int resumeId, String body, int userId) {
        switch (sr) {
            case EDUCATION:
                ResumeEducationDTO eduDto = parseJsonRequest(body, ResumeEducationDTO.class);
                return eduDto != null ? resumeService.addEducation(resumeId, eduDto, userId)
                        : Result.error(400, "无效的JSON格式");
            case SKILLS:
                ResumeSkillDTO skillDto = parseJsonRequest(body, ResumeSkillDTO.class);
                return skillDto != null ? resumeService.addSkill(resumeId, skillDto, userId)
                        : Result.error(400, "无效的JSON格式");
            case PROJECTS:
                ResumeProjectDTO projDto = parseJsonRequest(body, ResumeProjectDTO.class);
                return projDto != null ? resumeService.addProject(resumeId, projDto, userId)
                        : Result.error(400, "无效的JSON格式");
            case AWARDS:
                ResumeAwardDTO awardDto = parseJsonRequest(body, ResumeAwardDTO.class);
                return awardDto != null ? resumeService.addAward(resumeId, awardDto, userId)
                        : Result.error(400, "无效的JSON格式");
            default:
                return Result.error(404, "路径不存在");
        }
    }

    private Result dispatchSubResourceUpdate(SubResource sr, int itemId, String body, int userId) {
        switch (sr) {
            case EDUCATION:
                ResumeEducationDTO eduDto = parseJsonRequest(body, ResumeEducationDTO.class);
                return eduDto != null ? resumeService.updateEducation(itemId, eduDto, userId)
                        : Result.error(400, "无效的JSON格式");
            case SKILLS:
                ResumeSkillDTO skillDto = parseJsonRequest(body, ResumeSkillDTO.class);
                return skillDto != null ? resumeService.updateSkill(itemId, skillDto, userId)
                        : Result.error(400, "无效的JSON格式");
            case PROJECTS:
                ResumeProjectDTO projDto = parseJsonRequest(body, ResumeProjectDTO.class);
                return projDto != null ? resumeService.updateProject(itemId, projDto, userId)
                        : Result.error(400, "无效的JSON格式");
            case AWARDS:
                ResumeAwardDTO awardDto = parseJsonRequest(body, ResumeAwardDTO.class);
                return awardDto != null ? resumeService.updateAward(itemId, awardDto, userId)
                        : Result.error(400, "无效的JSON格式");
            default:
                return Result.error(404, "路径不存在");
        }
    }

    private void handleSubResourceDelete(HttpServletResponse resp, User user, ResumePathInfo pi) throws IOException {
        if (pi.getItemId() <= 0) {
            sendBadRequest(resp, "无效的项目ID");
            return;
        }

        SubResource sr = SubResource.fromPath(pi.getSubResource());
        if (sr == null) {
            sendNotFound(resp);
            return;
        }

        writeJson(resp, dispatchSubResourceDelete(sr, pi.getItemId(), user.getId()));
    }

    private Result dispatchSubResourceDelete(SubResource sr, int itemId, int userId) {
        switch (sr) {
            case EDUCATION:
                return resumeService.deleteEducation(itemId, userId);
            case SKILLS:
                return resumeService.deleteSkill(itemId, userId);
            case PROJECTS:
                return resumeService.deleteProject(itemId, userId);
            case AWARDS:
                return resumeService.deleteAward(itemId, userId);
            default:
                return Result.error(404, "路径不存在");
        }
    }

    // ==================== 认证与路径验证 ====================

    private User requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp);
            return null;
        }
        return user;
    }

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/");
    }

    private boolean isRecycleBinPath(String pathInfo) {
        return "/recycle-bin".equals(pathInfo);
    }

    // ==================== 路径解析 ====================

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            String uri = req.getRequestURI();
            int idx = uri.indexOf("/api/resumes/");
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

    private ResumePathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return ResumePathInfo.ROOT;
        }

        if (!pathInfo.startsWith("/")) {
            return ResumePathInfo.ROOT;
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length == 0 || segments[0].isEmpty()) {
            return ResumePathInfo.ROOT;
        }

        int resumeId = parseIntOrZero(segments[0]);

        if (segments.length == 1) {
            return ResumePathInfo.forResume(resumeId);
        }

        String segment1 = segments[1];

        // 特殊动作
        switch (segment1) {
            case "default":
                return ResumePathInfo.forAction(resumeId, "default");
            case "restore":
                return ResumePathInfo.forAction(resumeId, "restore");
            case "permanent":
                return ResumePathInfo.forAction(resumeId, "permanent");
        }

        // 子资源
        int itemId = segments.length >= 3 ? parseIntOrZero(segments[2]) : 0;
        return ResumePathInfo.forSubResource(resumeId, segment1, itemId);
    }

    private int parseIntOrZero(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
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

    // ==================== 响应便捷方法 ====================

    private void sendUnauthorized(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(401, "请先登录"));
    }

    private void sendMethodNotAllowed(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(405, "PUT方法不支持，请使用POST with _method=PUT"));
    }

    private void sendNotFound(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(404, "路径不存在"));
    }

    // ==================== 路径解析内部类 ====================

    private static class ResumePathInfo {
        static final ResumePathInfo ROOT = new ResumePathInfo(0, null, 0, null);

        private final int resumeId;
        private final String subResource;
        private final int itemId;
        private final String action;

        private ResumePathInfo(int resumeId, String subResource, int itemId, String action) {
            this.resumeId = resumeId;
            this.subResource = subResource;
            this.itemId = itemId;
            this.action = action;
        }

        static ResumePathInfo forResume(int resumeId) {
            return new ResumePathInfo(resumeId, null, 0, null);
        }

        static ResumePathInfo forAction(int resumeId, String action) {
            return new ResumePathInfo(resumeId, null, 0, action);
        }

        static ResumePathInfo forSubResource(int resumeId, String subResource, int itemId) {
            return new ResumePathInfo(resumeId, subResource, itemId, null);
        }

        boolean isValidResumeId() {
            return resumeId > 0;
        }

        int getResumeId() {
            return resumeId;
        }

        String getSubResource() {
            return subResource;
        }

        int getItemId() {
            return itemId;
        }

        boolean hasSubResource() {
            return subResource != null && !subResource.isEmpty();
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
