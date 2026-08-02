package servlet.api;

import model.User;
import service.AttendanceService;
import servlet.BaseApiServlet;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 考勤API Servlet
 *
 * 服务分层与API化完整计划.md 4.2 AttendanceApiServlet 考勤API
 *
 * 端点：
 * - GET /api/attendance - 考勤列表
 * - GET /api/attendance/stats - 考勤统计
 * - POST /api/attendance/check-in - 签到
 * - POST /api/attendance/check-out - 签退
 * - GET /api/attendance/my - 我的考勤
 * - GET /api/attendance/my/stats - 我的统计
 * - POST /api/attendance/makeup - 补签申请
 * - GET /api/attendance/makeup - 补签列表
 * - POST /api/attendance/{id}/approve - 审批通过
 * - POST /api/attendance/{id}/reject - 审批拒绝
 */
@WebServlet("/api/attendance/*")
public class AttendanceApiServlet extends BaseApiServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final AttendanceService attendanceService;

    public AttendanceApiServlet() {
        this.attendanceService = new AttendanceService();
    }

    public AttendanceApiServlet(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // ==================== HTTP Method Handlers ====================

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        dispatchGetRequest(req, resp, currentUser);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = getAuthenticatedUser(req, resp);
        if (currentUser == null) return;

        dispatchPostRequest(req, resp, currentUser);
    }

    // ==================== GET Request Dispatcher ====================

    private void dispatchGetRequest(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String path = getPathInfo(req);
        Route route = matchGetRoute(path);

        switch (route) {
            case LIST_ATTENDANCE:
                handleListAttendance(req, resp, user);
                break;
            case STATS:
                handleGetStats(resp, user);
                break;
            case MY_ATTENDANCE:
                handleGetMyAttendance(req, resp, user);
                break;
            case MY_STATS:
                handleGetMyStats(resp, user);
                break;
            case MAKEUP_LIST:
                handleListMakeup(req, resp, user);
                break;
            case NOT_FOUND:
                sendNotFound(resp);
                break;
        }
    }

    // ==================== POST Request Dispatcher ====================

    private void dispatchPostRequest(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        String path = getPathInfo(req);
        Route route = matchPostRoute(path);

        switch (route) {
            case CHECK_IN:
                handleCheckIn(resp, user);
                break;
            case CHECK_OUT:
                handleCheckOut(resp, user);
                break;
            case APPLY_MAKEUP:
                handleApplyMakeup(req, resp, user);
                break;
            case APPROVE:
                handleApproveMakeup(req, resp, user);
                break;
            case REJECT:
                handleRejectMakeup(req, resp, user);
                break;
            case NOT_FOUND:
                sendNotFound(resp);
                break;
        }
    }

    // ==================== Route Definitions ====================

    private enum Route {
        LIST_ATTENDANCE,
        STATS,
        MY_ATTENDANCE,
        MY_STATS,
        MAKEUP_LIST,
        CHECK_IN,
        CHECK_OUT,
        APPLY_MAKEUP,
        APPROVE,
        REJECT,
        NOT_FOUND
    }

    private Route matchGetRoute(String path) {
        if (isEmptyPath(path)) return Route.LIST_ATTENDANCE;
        if (isStatsPath(path)) return Route.STATS;
        if (isMyPath(path)) return Route.MY_ATTENDANCE;
        if (isMyStatsPath(path)) return Route.MY_STATS;
        if (isMakeupListPath(path)) return Route.MAKEUP_LIST;
        return Route.NOT_FOUND;
    }

    private Route matchPostRoute(String path) {
        if (isEmptyPath(path)) return Route.NOT_FOUND;
        if (isCheckInPath(path)) return Route.CHECK_IN;
        if (isCheckOutPath(path)) return Route.CHECK_OUT;
        if (isApplyMakeupPath(path)) return Route.APPLY_MAKEUP;
        if (isApprovePath(path)) return Route.APPROVE;
        if (isRejectPath(path)) return Route.REJECT;
        return Route.NOT_FOUND;
    }

    // ==================== Path Matchers ====================

    private boolean isEmptyPath(String path) {
        return path == null || path.equals("/") || path.equals("");
    }

    private boolean isStatsPath(String path) {
        return "/stats".equals(path);
    }

    private boolean isMyPath(String path) {
        return "/my".equals(path);
    }

    private boolean isMyStatsPath(String path) {
        return "/my/stats".equals(path);
    }

    private boolean isMakeupListPath(String path) {
        return "/makeup".equals(path);
    }

    private boolean isCheckInPath(String path) {
        return "/check-in".equals(path);
    }

    private boolean isCheckOutPath(String path) {
        return "/check-out".equals(path);
    }

    private boolean isApplyMakeupPath(String path) {
        return "/makeup".equals(path);
    }

    private boolean isApprovePath(String path) {
        return path != null && path.matches("/\\d+/approve");
    }

    private boolean isRejectPath(String path) {
        return path != null && path.matches("/\\d+/reject");
    }

    // ==================== GET Handlers ====================

    private void handleListAttendance(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = getPageParam(req);
        Result result = attendanceService.listAttendance(null, page);
        writeJson(resp, result);
    }

    private void handleGetStats(HttpServletResponse resp, User user) throws IOException {
        Result result = attendanceService.getAttendanceStats(user.getId());
        writeJson(resp, result);
    }

    private void handleGetMyAttendance(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = getPageParam(req);
        Result result = attendanceService.getMyAttendance(user.getId(), page);
        writeJson(resp, result);
    }

    private void handleGetMyStats(HttpServletResponse resp, User user) throws IOException {
        Result result = attendanceService.getMyStats(user.getId());
        writeJson(resp, result);
    }

    private void handleListMakeup(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        int page = getPageParam(req);
        Result result = attendanceService.listAttendance(null, page);
        writeJson(resp, result);
    }

    // ==================== POST Handlers ====================

    private void handleCheckIn(HttpServletResponse resp, User user) throws IOException {
        Result result = attendanceService.checkIn(user.getId());
        writeJson(resp, result);
    }

    private void handleCheckOut(HttpServletResponse resp, User user) throws IOException {
        Result result = attendanceService.checkOut(user.getId());
        writeJson(resp, result);
    }

    private void handleApplyMakeup(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        Map<String, Object> body = parseRequestBody(req);
        if (body == null) {
            sendBadRequest(resp, "无效的请求参数");
            return;
        }

        String reason = toString(body.get("reason"));
        Date date = toDate(body.get("date"));

        Result result = attendanceService.applyMakeup(date, reason, user.getId());
        writeJson(resp, result);
    }

    private void handleApproveMakeup(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        Integer id = extractIdFromPath(getPathInfo(req));
        if (id == null) {
            sendBadRequest(resp, "无效的申请ID");
            return;
        }
        Result result = attendanceService.approveMakeup(id, user.getId());
        writeJson(resp, result);
    }

    private void handleRejectMakeup(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        Integer id = extractIdFromPath(getPathInfo(req));
        if (id == null) {
            sendBadRequest(resp, "无效的申请ID");
            return;
        }
        Result result = attendanceService.rejectMakeup(id, user.getId());
        writeJson(resp, result);
    }

    // ==================== Authentication ====================

    private User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            sendUnauthorized(resp, "请先登录");
            return null;
        }
        return user;
    }

    // ==================== Request Helpers ====================

    private String getPathInfo(HttpServletRequest req) {
        return req.getPathInfo();
    }

    private int getPageParam(HttpServletRequest req) {
        String value = req.getParameter("page");
        return parseIntOrDefault(value, DEFAULT_PAGE);
    }

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

    // ==================== Type Conversion ====================

    private Integer extractIdFromPath(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return Integer.parseInt(part);
            }
        }
        return null;
    }

    private String toString(Object value) {
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

    // ==================== Response Helpers ====================

    private void sendNotFound(HttpServletResponse resp) throws IOException {
        sendError(resp, 404, "未找到请求的路径");
    }
}
