package servlet.api;

import dto.NewsDTO;
import dto.NewsFilterDTO;
import model.User;
import service.NewsService;
import servlet.BaseApiServlet;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 新闻API Servlet
 *
 * 服务分层与API化完整计划.md 5.1 NewsApiServlet 新闻API
 */
public class NewsApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    // ==================== Service ====================

    private transient NewsService newsService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.newsService = new NewsService();
    }

    // ==================== HTTP 方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return;
        }

        String pathInfo = derivePathInfo(req);

        if (isTypesPath(pathInfo)) {
            handleListTypes(resp);
        } else if (isListPath(pathInfo)) {
            handleListNews(req, resp);
        } else {
            handleNewsGet(req, resp, user, pathInfo);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return;
        }

        String pathInfo = derivePathInfo(req);
        boolean isPutTunnel = "PUT".equalsIgnoreCase(req.getParameter("_method"));
        boolean isDeleteTunnel = "DELETE".equalsIgnoreCase(req.getParameter("_method"));

        if (isListPath(pathInfo)) {
            if (isPutTunnel) {
                sendBadRequest(resp, "根路径不支持PUT方法");
            } else if (isDeleteTunnel) {
                sendBadRequest(resp, "根路径不支持DELETE方法");
            } else {
                handleCreate(req, resp, user);
            }
        } else {
            handleNewsPost(req, resp, user, pathInfo, isPutTunnel, isDeleteTunnel);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return;
        }
        sendMethodNotAllowed(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return;
        }

        String pathInfo = derivePathInfo(req);

        if (isListPath(pathInfo)) {
            sendNotFound(resp);
        } else {
            handleNewsDelete(req, resp, user, pathInfo);
        }
    }

    // ==================== 新闻主资源处理 ====================

    private void handleListNews(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        String type = req.getParameter("type");
        String statusStr = req.getParameter("status");
        int page = parseIntParam(req.getParameter("page"), 1);
        int pageSize = parseIntParam(req.getParameter("pageSize"), 20);

        NewsFilterDTO filter = new NewsFilterDTO();
        filter.setKeyword(keyword);
        filter.setType(type);
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                filter.setStatus(Integer.parseInt(statusStr));
            } catch (NumberFormatException ignored) {
            }
        }

        writeJson(resp, newsService.listNews(filter, page, pageSize));
    }

    private void handleListTypes(HttpServletResponse resp) throws IOException {
        String[] types = {
            NewsFilterDTO.TYPE_AWARD,
            NewsFilterDTO.TYPE_ACTIVITY,
            NewsFilterDTO.TYPE_NOTICE
        };
        writeJson(resp, Result.ok(types));
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String body = readBody(req);
        NewsDTO dto = parseJsonRequest(body, NewsDTO.class);
        if (dto == null) {
            sendBadRequest(resp, "请求体不能为空");
            return;
        }
        writeJson(resp, newsService.createNews(dto, user.getId()));
    }

    private void handleNewsGet(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        NewsPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidNewsId()) {
            sendBadRequest(resp, "无效的新闻ID");
            return;
        }

        if (pi.hasSubResource()) {
            sendNotFound(resp);
        } else {
            handleGetNewsDetail(req, resp, user, pi.getNewsId());
        }
    }

    private void handleGetNewsDetail(HttpServletRequest req, HttpServletResponse resp, User user, int newsId) throws IOException {
        writeJson(resp, newsService.getNewsDetail(newsId));
    }

    private void handleNewsPost(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo,
                               boolean isPutTunnel, boolean isDeleteTunnel) throws IOException {
        NewsPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidNewsId()) {
            sendBadRequest(resp, "无效的新闻ID");
            return;
        }

        if (pi.isPublishAction()) {
            handlePublish(req, resp, user, pi.getNewsId());
        } else if (pi.isUnpublishAction()) {
            handleUnpublish(req, resp, user, pi.getNewsId());
        } else if (isPutTunnel) {
            handleUpdate(req, resp, user, pi.getNewsId());
        } else if (isDeleteTunnel) {
            handleDelete(req, resp, user, pi.getNewsId());
        } else {
            sendNotFound(resp);
        }
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User user, int newsId) throws IOException {
        String body = readBody(req);
        NewsDTO dto = parseJsonRequest(body, NewsDTO.class);
        if (dto == null) {
            sendBadRequest(resp, "无效的JSON格式");
            return;
        }
        writeJson(resp, newsService.updateNews(newsId, dto, user.getId()));
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User user, int newsId) throws IOException {
        writeJson(resp, newsService.deleteNews(newsId, user.getId()));
    }

    private void handlePublish(HttpServletRequest req, HttpServletResponse resp, User user, int newsId) throws IOException {
        writeJson(resp, newsService.publishNews(newsId, user.getId()));
    }

    private void handleUnpublish(HttpServletRequest req, HttpServletResponse resp, User user, int newsId) throws IOException {
        writeJson(resp, newsService.unpublishNews(newsId, user.getId()));
    }

    private void handleNewsDelete(HttpServletRequest req, HttpServletResponse resp, User user, String pathInfo) throws IOException {
        NewsPathInfo pi = parsePathInfo(pathInfo);
        if (!pi.isValidNewsId()) {
            sendBadRequest(resp, "无效的新闻ID");
            return;
        }

        if (pi.hasSubResource()) {
            sendNotFound(resp);
        } else {
            handleDelete(req, resp, user, pi.getNewsId());
        }
    }

    // ==================== 路径判断 ====================

    private boolean isListPath(String pathInfo) {
        return pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/");
    }

    private boolean isTypesPath(String pathInfo) {
        return "/types".equals(pathInfo);
    }

    // ==================== 路径解析 ====================

    private String derivePathInfo(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            String uri = req.getRequestURI();
            String prefix = "/api/news/";
            if (uri.startsWith(prefix)) {
                pathInfo = uri.substring(prefix.length() - 1); // keep leading /
                if (pathInfo.isEmpty() || pathInfo.equals("/")) {
                    pathInfo = null;
                }
            } else if (uri.equals("/api/news") || uri.equals("/api/news/")) {
                pathInfo = null;
            }
        }
        return pathInfo;
    }

    private NewsPathInfo parsePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return NewsPathInfo.ROOT;
        }

        if (!pathInfo.startsWith("/")) {
            return NewsPathInfo.ROOT;
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length == 0 || segments[0].isEmpty()) {
            return NewsPathInfo.ROOT;
        }

        int newsId = parseIntOrZero(segments[0]);

        if (segments.length == 1) {
            return NewsPathInfo.forNews(newsId);
        }

        String segment1 = segments[1];

        if ("publish".equals(segment1)) {
            return NewsPathInfo.forAction(newsId, "publish");
        }
        if ("unpublish".equals(segment1)) {
            return NewsPathInfo.forAction(newsId, "unpublish");
        }

        return NewsPathInfo.forSubResource(newsId, segment1);
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

    // ==================== 响应便捷方法 ====================

    private void sendMethodNotAllowed(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(405, "PUT/DELETE方法不支持，请使用POST with _method=PUT/_method=DELETE"));
    }

    private void sendNotFound(HttpServletResponse resp) throws IOException {
        writeJson(resp, Result.error(404, "路径不存在"));
    }

    // ==================== 路径解析内部类 ====================

    private static class NewsPathInfo {
        static final NewsPathInfo ROOT = new NewsPathInfo(0, null, null);

        private final int newsId;
        private final String subResource;
        private final String action;

        private NewsPathInfo(int newsId, String subResource, String action) {
            this.newsId = newsId;
            this.subResource = subResource;
            this.action = action;
        }

        static NewsPathInfo forNews(int newsId) {
            return new NewsPathInfo(newsId, null, null);
        }

        static NewsPathInfo forAction(int newsId, String action) {
            return new NewsPathInfo(newsId, null, action);
        }

        static NewsPathInfo forSubResource(int newsId, String subResource) {
            return new NewsPathInfo(newsId, subResource, null);
        }

        boolean isValidNewsId() {
            return newsId > 0;
        }

        int getNewsId() {
            return newsId;
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

        boolean isPublishAction() {
            return "publish".equals(action);
        }

        boolean isUnpublishAction() {
            return "unpublish".equals(action);
        }
    }
}