package servlet.api;

import model.User;
import service.StudyService;
import servlet.BaseApiServlet;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * 学习API Servlet
 *
 * 服务分层与API化完整计划.md 6.1 StudyApiServlet 学习服务API化
 *
 * 端点：
 * - GET  /api/study           → 学习记录列表(分页)
 * - GET  /api/study/{id}     → 学习记录详情
 * - GET  /api/study/today    → 今日进行中会话
 * - POST /api/study/start    → 开始学习
 * - POST /api/study/end      → 结束学习
 * - GET  /api/study/my       → 我的学习记录
 * - GET  /api/study/stats    → 学习统计
 * - GET  /api/study/week-stats → 本周学习统计
 */
public class StudyApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== 路径段常量 ====================

    private static final String PATH_TODAY = "/today";
    private static final String PATH_MY = "/my";
    private static final String PATH_STATS = "/stats";
    private static final String PATH_WEEK_STATS = "/week-stats";
    private static final String PATH_START = "/start";
    private static final String PATH_END = "/end";

    private static final Set<String> LIST_PATHS = Set.of(
            PATH_TODAY, PATH_MY, PATH_STATS, PATH_WEEK_STATS, PATH_START, PATH_END
    );

    // ==================== Service ====================

    private transient StudyService studyService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.studyService = new StudyService();
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
            handleSessionGet(req, resp, user, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;

        String pathInfo = derivePathInfo(req);

        if (isPutMethodTunnel(req)) {
            sendBadRequest(resp, "根路径不支持PUT方法");
        } else if (isListPath(pathInfo)) {
            handleActionPost(req, resp, user, pathInfo);
        } else {
            sendNotFound(resp);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;
        sendMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = requireAuthenticatedUser(req, resp);
        if (user == null) return;
        sendNotFound(resp);
    }

    // ==================== GET 请求分发 ====================

    private void dispatchListGetRequest(String pathInfo, HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        if (pathInfo == null) {
            handleListSessions(req, resp, user);
            return;
        }
        switch (pathInfo) {
            case PATH_TODAY:
                handleGetTodaySession(req, resp, user);
                break;
            case PATH_MY:
                handleGetMySessions(req, resp, user);
                break;
            case PATH_STATS:
                handleGetStatistics(req, resp, user);
                break;
            case PATH_WEEK_STATS:
                handleGetWeekStatistics(req, resp, user);
                break;
            default:
                handleListSessions(req, resp, user);
        }
    }

    // ==================== 处理器方法 ====================

    private void handleListSessions(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        PageParams params = extractPageParams(req);
        writeJson(resp, studyService.listSessions(new HashMap<>(), params.page, params.pageSize));
    }

    private void handleGetTodaySession(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        writeJson(resp, studyService.getTodaySession(user.getId()));
    }

    private void handleGetMySessions(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        PageParams params = extractPageParams(req);
        writeJson(resp, studyService.getMySessions(user.getId(), params.page, params.pageSize));
    }

    private void handleGetStatistics(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        writeJson(resp, studyService.getStatistics(user.getId()));
    }

    private void handleGetWeekStatistics(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        writeJson(resp, studyService.getWeekStatistics(user.getId()));
    }

    private void handleSessionGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        StudyPathInfo pi = parsePathInfo(pathInfo);

        if (pi.isInvalid() || pi.hasSubResource()) {
            sendNotFound(resp);
            return;
        }
        if (!pi.isValidSessionId()) {
            sendBadRequest(resp, "无效的学习记录ID");
            return;
        }
        writeJson(resp, studyService.getSessionDetail(pi.getSessionId()));
    }

    private void handleActionPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        if (pathInfo == null) {
            sendNotFound(resp);
            return;
        }
        switch (pathInfo) {
            case PATH_START:
                writeJson(resp, studyService.startSession(user.getId()));
                break;
            case PATH_END:
                writeJson(resp, studyService.endSession(user.getId()));
                break;
            default:
                sendNotFound(resp);
        }
    }

    // ==================== 认证辅助 ====================

    private User requireAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
        }
        return user;
    }

    private boolean isPutMethodTunnel(HttpServletRequest req) {
        return "PUT".equalsIgnoreCase(req.getParameter("_method"));
    }

    // ==================== 分页参数提取 ====================

    private PageParams extractPageParams(HttpServletRequest req) {
        return new PageParams(
                parseIntParam(req.getParameter("page"), 1),
                parseIntParam(req.getParameter("pageSize"), 20)
        );
    }

    private static class PageParams {
        final int page;
        final int pageSize;

        PageParams(int page, int pageSize) {
            this.page = page;
            this.pageSize = pageSize;
        }
    }

    // ==================== 路径判断 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo) || LIST_PATHS.contains(pathInfo);
    }

    // ==================== 路径解析 ====================

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            String uri = req.getRequestURI();
            String prefix = "/api/study/";
            if (uri.startsWith(prefix)) {
                pathInfo = uri.substring(prefix.length() - 1);
                if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
                    pathInfo = null;
                }
            } else if (uri.equals("/api/study") || uri.equals("/api/study/")) {
                pathInfo = null;
            }
        }
        return pathInfo;
    }

    private StudyPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || "/".equals(pathInfo) || pathInfo.isEmpty()) {
            return StudyPathInfo.ROOT;
        }

        if (!pathInfo.startsWith("/")) {
            return StudyPathInfo.ROOT;
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length == 0 || segments[0].isEmpty()) {
            return StudyPathInfo.ROOT;
        }

        int sessionId = parseIntOrZero(segments[0]);

        if (sessionId == 0 && !isAlphanumeric(segments[0])) {
            return StudyPathInfo.INVALID;
        }

        if (segments.length == 1) {
            return StudyPathInfo.forSession(sessionId);
        }

        return StudyPathInfo.forSubResource(sessionId, segments[1]);
    }

    private boolean isAlphanumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isLetterOrDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
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

    // ==================== 响应便捷方法 ====================

    private void sendMethodNotAllowed(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(405, "PUT方法不支持，请使用POST with _method=PUT"));
    }

    private void sendNotFound(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(404, "路径不存在"));
    }

    // ==================== 路径解析内部类 ====================

    private static class StudyPathInfo {
        static final StudyPathInfo ROOT = new StudyPathInfo(0, null, false);
        static final StudyPathInfo INVALID = new StudyPathInfo(0, null, true);

        private final int sessionId;
        private final String subResource;
        private final boolean invalid;

        private StudyPathInfo(int sessionId, String subResource, boolean invalid) {
            this.sessionId = sessionId;
            this.subResource = subResource;
            this.invalid = invalid;
        }

        static StudyPathInfo forSession(int sessionId) {
            return new StudyPathInfo(sessionId, null, false);
        }

        static StudyPathInfo forSubResource(int sessionId, String subResource) {
            return new StudyPathInfo(sessionId, subResource, false);
        }

        int getSessionId() {
            return sessionId;
        }

        boolean isValidSessionId() {
            return sessionId > 0;
        }

        boolean hasSubResource() {
            return subResource != null;
        }

        boolean isInvalid() {
            return invalid;
        }
    }
}
