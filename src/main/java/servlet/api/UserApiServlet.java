package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.FileStorageDAO;
import dao.MemberProfileDAO;
import dao.UserDAO;
import dto.ProfileDTO;
import model.User;
import service.UserService;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * UserApiServlet - 用户/认证API端点
 *
 * 路径: /api/auth/*, /api/users/*
 * 端点:
 * - POST /api/auth/login → 登录
 * - POST /api/auth/logout → 登出
 * - POST /api/auth/change-password → 改密码
 * - GET /api/users/me → 当前用户信息
 * - PUT /api/users/me → 更新个人档案
 * - POST /api/users/me/avatar → 上传头像
 * - GET /api/users/{id} → 用户详情
 * - GET /api/users → 成员列表（admin）
 */
@WebServlet("/api/*")
public class UserApiServlet extends HttpServlet {

    // ==================== 常量 ====================

    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String AUTH_PREFIX = "/auth";
    private static final String USERS_PREFIX = "/users";

    // ==================== 依赖 ====================

    private UserService userService;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    public void init() throws ServletException {
        UserDAO userDAO = new UserDAO();
        MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
        FileStorageDAO fileStorageDAO = new FileStorageDAO();
        this.userService = new UserService(userDAO, memberProfileDAO, fileStorageDAO);
    }

    // ==================== HTTP方法分发 ====================

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        String path = strippedPath(request);

        if (isMePath(path)) {
            handleGetCurrentUser(request, response, getCurrentUser(request));
        } else if (isUsersPath(path)) {
            handleListMembers(request, response, getCurrentUser(request));
        } else if (isUserDetailPath(path)) {
            dispatchGetUserDetail(request, response, path);
        } else {
            sendError(response, 400, "无效的请求路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        String path = strippedPath(request);

        if (isLoginPath(path)) {
            handleLogin(request, response);
        } else if (isLogoutPath(path)) {
            handleLogout(request, response);
        } else if (isChangePasswordPath(path)) {
            User currentUser = requireAuth(request, response);
            if (currentUser != null) {
                handleChangePassword(request, response, currentUser);
            }
        } else if (isAvatarPath(path)) {
            User currentUser = requireAuth(request, response);
            if (currentUser != null) {
                handleUploadAvatar(request, response, currentUser);
            }
        } else {
            sendError(response, 400, "无效的请求路径");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        User currentUser = requireAuth(request, response);
        if (currentUser == null) return;

        String path = strippedPath(request);
        if (isUpdateProfilePath(path)) {
            handleUpdateProfile(request, response, currentUser);
        } else {
            sendError(response, 400, "无效的请求路径");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setJsonContentType(response);
        sendError(response, 405, "不支持的HTTP方法");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // ==================== 路径判断 ====================

    private String strippedPath(HttpServletRequest request) {
        return stripPrefix(request.getPathInfo());
    }

    private boolean isMePath(String path) {
        return path == null || path.equals("/me") || path.equals("/me/");
    }

    private boolean isUsersPath(String path) {
        return path == null || path.equals("/") || path.isEmpty() || path.equals("/users");
    }

    private boolean isUserDetailPath(String path) {
        if (path == null || !path.startsWith("/")) return false;
        String[] segments = path.split("/");
        return segments.length >= 2 && isNumeric(segments[1]);
    }

    private boolean isLoginPath(String path) {
        return "/login".equals(path);
    }

    private boolean isLogoutPath(String path) {
        return "/logout".equals(path);
    }

    private boolean isChangePasswordPath(String path) {
        return "/change-password".equals(path);
    }

    private boolean isAvatarPath(String path) {
        return path != null && (path.equals("/me/avatar") || path.startsWith("/me/avatar"));
    }

    private boolean isUpdateProfilePath(String path) {
        return path != null && (path.equals("/me") || path.startsWith("/me"));
    }

    // ==================== 处理器方法 ====================

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> body = parseJsonRequest(request, Map.class);
        if (body == null) {
            sendError(response, 400, "请求体不能为空");
            return;
        }
        Result result = userService.login(getStringValue(body, "username"), getStringValue(body, "password"));
        writeJson(response, result);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        writeJson(response, Result.ok());
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        Map<String, Object> body = parseJsonRequest(request, Map.class);
        if (body == null) {
            sendError(response, 400, "请求体不能为空");
            return;
        }
        Result result = userService.changePassword(
                currentUser.getId(),
                getStringValue(body, "oldPassword"),
                getStringValue(body, "newPassword"));
        writeJson(response, result);
    }

    private void handleGetCurrentUser(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        if (currentUser == null) {
            sendUnauthorized(response, "请先登录");
            return;
        }
        Result result = userService.getUserDetail(currentUser.getId());
        writeJson(response, result);
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        Map<String, Object> body = parseJsonRequest(request, Map.class);
        if (body == null) {
            sendError(response, 400, "请求体不能为空");
            return;
        }
        ProfileDTO profileDTO = buildProfileDTO(body);
        Result result = userService.updateProfile(currentUser.getId(), profileDTO);
        writeJson(response, result);
    }

    private void handleUploadAvatar(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException, ServletException {
        Object filePart = request.getPart("avatar");
        Result result = userService.uploadAvatar(currentUser.getId(), filePart);
        writeJson(response, result);
    }

    private void dispatchGetUserDetail(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        int userId = Integer.parseInt(path.split("/")[1]);
        handleGetUserDetail(request, response, getCurrentUser(request), userId);
    }

    private void handleGetUserDetail(HttpServletRequest request, HttpServletResponse response, User currentUser, int userId) throws IOException {
        Result result = userService.getUserDetail(userId);
        writeJson(response, result);
    }

    private void handleListMembers(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        User admin = requireAdmin(request, response, currentUser);
        if (admin == null) return;

        int[] page = parsePageParams(request);
        Result result = userService.listMembers(admin.getId(),
                request.getParameter("keyword"),
                request.getParameter("role"),
                page[0], page[1]);
        writeJson(response, result);
    }

    // ==================== 认证辅助 ====================

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }

    /**
     * 要求用户已登录，未登录返回401
     * @return 登录用户，或null（已发送响应）
     */
    private User requireAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            sendUnauthorized(response, "请先登录");
        }
        return currentUser;
    }

    /**
     * 要求用户是管理员，不是则返回403
     * @return 管理员用户，或null（已发送响应）
     */
    private User requireAdmin(HttpServletRequest request, HttpServletResponse response, User currentUser) throws IOException {
        if (currentUser == null || !ROLE_ADMIN.equals(currentUser.getRole())) {
            sendForbidden(response, "需要管理员权限");
            return null;
        }
        return currentUser;
    }

    // ==================== 数据转换辅助 ====================

    private ProfileDTO buildProfileDTO(Map<String, Object> body) {
        ProfileDTO dto = new ProfileDTO();
        dto.setName(getStringValue(body, "name"));
        dto.setPhone(getStringValue(body, "phone"));
        dto.setEmail(getStringValue(body, "email"));
        dto.setBirthday(getStringValue(body, "birthday"));
        dto.setStudentId(getStringValue(body, "studentId"));
        dto.setMajor(getStringValue(body, "major"));
        dto.setGrade(getStringValue(body, "grade"));
        dto.setIntroduction(getStringValue(body, "bio"));
        return dto;
    }

    /**
     * 从Map中安全获取String值，null返回空字符串
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    // ==================== 工具方法 ====================

    private <T> T parseJsonRequest(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
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

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(401, message));
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        writeJson(response, Result.error(403, message));
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        writeJson(response, Result.error(code, message));
    }

    private void setJsonContentType(HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE_JSON);
    }

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

    /**
     * 去除路径前缀：/auth/login → /login, /users/me → /me
     */
    private String stripPrefix(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty()) {
            return pathInfo;
        }
        if (pathInfo.startsWith(AUTH_PREFIX + "/")) {
            return pathInfo.substring(AUTH_PREFIX.length() + 1);
        }
        if (pathInfo.startsWith(USERS_PREFIX + "/")) {
            return pathInfo.substring(USERS_PREFIX.length() + 1);
        }
        return pathInfo;
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
}
