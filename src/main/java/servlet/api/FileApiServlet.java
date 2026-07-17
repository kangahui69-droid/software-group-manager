package servlet.api;

import dao.FileStorageDAO;
import dao.UserDAO;
import model.User;
import service.FileService;
import servlet.BaseApiServlet;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 文件API Servlet
 *
 * 服务分层与API化重构计划.md 5.3 FileApiServlet 文件API
 *
 * 端点：
 * - POST /api/files/upload → 上传（multipart, category参数）
 * - GET /api/files/{id} → 文件元信息
 * - GET /api/files/{id}/download → 下载
 * - GET /api/files/{id}/view → 查看（inline，图片/头像用）
 * - DELETE /api/files/{id} → 删除
 * - GET /api/files → 文件列表（按category）
 */
@WebServlet(name = "FileApiServlet", urlPatterns = {"/api/files", "/api/files/*"})
public class FileApiServlet extends BaseApiServlet {

    private static final long serialVersionUID = 1L;

    private static final String ACTION_DOWNLOAD = "download";
    private static final String ACTION_VIEW = "view";
    private static final String PATH_UPLOAD = "/upload";

    private transient FileService fileService;

    @Override
    public void init() throws ServletException {
        super.init();
        FileStorageDAO fileStorageDAO = new FileStorageDAO();
        UserDAO userDAO = new UserDAO();
        this.fileService = new FileService(fileStorageDAO, userDAO);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) {
            return;
        }

        FilePathInfo pathInfo = parseFilePathInfo(req.getPathInfo());

        if (pathInfo.isRootOrEmpty()) {
            handleListFiles(req, resp, currentUser);
            return;
        }

        if (!pathInfo.hasValidId()) {
            sendBadRequest(resp, "无效的文件ID");
            return;
        }

        if (pathInfo.hasAction()) {
            dispatchFileAction(resp, pathInfo.getFileId(), pathInfo.getAction());
        } else {
            handleGetFileMeta(resp, pathInfo.getFileId());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        if (PATH_UPLOAD.equals(pathInfo)) {
            handleUploadFile(req, resp, currentUser);
        } else {
            sendBadRequest(resp, "无效的请求路径");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = requireAuth(req, resp);
        if (currentUser == null) {
            return;
        }

        FilePathInfo pathInfo = parseFilePathInfo(req.getPathInfo());

        if (pathInfo.isRootOrEmpty() || !pathInfo.hasValidId()) {
            sendBadRequest(resp, "无效的文件ID");
            return;
        }

        handleDeleteFile(resp, pathInfo.getFileId(), currentUser);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        sendError(resp, 405, "不支持的HTTP方法");
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

    // ==================== 路径解析 ====================

    /**
     * 解析文件路径信息
     */
    private FilePathInfo parseFilePathInfo(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
            return FilePathInfo.root();
        }

        if (!pathInfo.startsWith("/")) {
            return FilePathInfo.root();
        }

        String[] segments = pathInfo.substring(1).split("/");
        if (segments.length < 1 || segments[0].isEmpty()) {
            return FilePathInfo.root();
        }

        String idStr = segments[0];
        if (!isNumeric(idStr)) {
            return FilePathInfo.root();
        }

        int fileId = Integer.parseInt(idStr);
        String action = segments.length >= 2 ? segments[1] : null;

        return FilePathInfo.of(fileId, action);
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ==================== 路由分发 ====================

    private void dispatchFileAction(HttpServletResponse resp, int fileId, String action) throws IOException {
        if (ACTION_DOWNLOAD.equals(action)) {
            handleDownloadFile(resp, fileId);
        } else if (ACTION_VIEW.equals(action)) {
            handleViewFile(resp, fileId);
        } else {
            sendBadRequest(resp, "未知的操作");
        }
    }

    // ==================== 处理器方法 ====================

    private void handleListFiles(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String category = req.getParameter("category");
        Result result = fileService.listFiles(category, currentUser.getId());
        writeJson(resp, result);
    }

    private void handleGetFileMeta(HttpServletResponse resp, int fileId) throws IOException {
        Result result = fileService.viewFile(fileId);
        writeJson(resp, result);
    }

    private void handleDownloadFile(HttpServletResponse resp, int fileId) throws IOException {
        Result result = fileService.downloadFile(fileId);
        writeJson(resp, result);
    }

    private void handleViewFile(HttpServletResponse resp, int fileId) throws IOException {
        Result result = fileService.viewFile(fileId);
        writeJson(resp, result);
    }

    private void handleUploadFile(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
        String category = req.getParameter("category");
        Object file = extractFileFromRequest(req);
        Result result = fileService.uploadFile(file, category, currentUser.getId());
        writeJson(resp, result);
    }

    private void handleDeleteFile(HttpServletResponse resp, int fileId, User currentUser) throws IOException {
        Result result = fileService.deleteFile(fileId, currentUser.getId());
        writeJson(resp, result);
    }

    // ==================== 文件提取 ====================

    private Object extractFileFromRequest(HttpServletRequest request) {
        try {
            if (request.getContentType() != null && request.getContentType().contains("multipart/form-data")) {
                return request.getPart("file");
            }
        } catch (Exception e) {
            // 返回null让service层处理
        }
        return null;
    }

    // ==================== 内部类：路径解析结果 ====================

    /**
     * 文件路径解析结果
     */
    private static class FilePathInfo {
        private final int fileId;
        private final String action;
        private final boolean isRoot;

        private FilePathInfo(int fileId, String action, boolean isRoot) {
            this.fileId = fileId;
            this.action = action;
            this.isRoot = isRoot;
        }

        static FilePathInfo root() {
            return new FilePathInfo(0, null, true);
        }

        static FilePathInfo of(int fileId, String action) {
            return new FilePathInfo(fileId, action, false);
        }

        boolean isRootOrEmpty() {
            return isRoot;
        }

        boolean hasValidId() {
            return !isRoot && fileId > 0;
        }

        boolean hasAction() {
            return !isRoot && action != null && !action.isEmpty();
        }

        int getFileId() {
            return fileId;
        }

        String getAction() {
            return action;
        }
    }
}
