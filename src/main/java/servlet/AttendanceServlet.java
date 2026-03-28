package servlet;

import dao.AttendanceDAO;
import dao.StudySessionDAO;
import dao.UserDAO;
import model.Attendance;
import model.StudySession;
import model.User;
import util.AuthHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/attendance/*")
public class AttendanceServlet extends HttpServlet {

    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    private StudySessionDAO studyDAO = new StudySessionDAO();
    private UserDAO userDAO = new UserDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // 签到首页
            showIndex(req, resp);
        } else if (pathInfo.equals("/list")) {
            // 获取签到记录列表
            showList(req, resp);
        } else if (pathInfo.equals("/stats")) {
            // 获取考勤统计
            getStatistics(req, resp);
        } else if (pathInfo.equals("/manage")) {
            // 管理后台 - 考勤管理页面
            showManage(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String action = req.getParameter("action");

        // 支持两种方式：1. URL路径 /attendance/checkout  2. 参数 action=checkout
        if (pathInfo != null && pathInfo.equals("/checkout") || "checkout".equals(action)) {
            // 签退
            doCheckOut(req, resp);
        } else {
            // 签到
            doCheckIn(req, resp);
        }
    }

    // 签到首页
    private void showIndex(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            // 检查昨日是否未签退
            Attendance yesterday = attendanceDAO.getYesterdayAttendance(currentUser.getId());
            if (yesterday != null && yesterday.getCheckInTime() != null && yesterday.getCheckOutTime() == null) {
                // 昨日未签退，自动标记
                if (yesterday.getAttendanceDate() != null) {
                    attendanceDAO.markAbsent(currentUser.getId(), new java.sql.Date(yesterday.getAttendanceDate().getTime()));
                }
                req.setAttribute("warning", "您昨日忘记签退，已自动标记");
            }

            // 获取今日签到状态
            Attendance today = attendanceDAO.getTodayAttendance(currentUser.getId());

            req.setAttribute("todayAttendance", today);
            req.setAttribute("currentUser", currentUser);
            req.setAttribute("nowDate", new Date());

            req.getRequestDispatcher("/jsp/attendance/index.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "获取签到状态失败: " + e.getMessage());
            req.getRequestDispatcher("/jsp/attendance/index.jsp").forward(req, resp);
        }
    }

    // 获取签到记录列表
    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");
            String pageStr = req.getParameter("page");

            Date startDate = startDateStr != null && !startDateStr.isEmpty() ? sdf.parse(startDateStr) : null;
            Date endDate = endDateStr != null && !endDateStr.isEmpty() ? sdf.parse(endDateStr) : null;
            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int pageSize = 20;
            int offset = (page - 1) * pageSize;

            List<Attendance> list = attendanceDAO.getAttendanceList(
                currentUser.getId(), startDate, endDate, offset, pageSize);

            Map<String, Object> stats = attendanceDAO.getStatistics(
                currentUser.getId(), startDate, endDate);

            int total = attendanceDAO.getTotalCount(currentUser.getId(), startDate, endDate);
            int totalPages = (int) Math.ceil((double) total / pageSize);

            req.setAttribute("attendanceList", list);
            req.setAttribute("statistics", stats);
            req.setAttribute("startDate", startDateStr);
            req.setAttribute("endDate", endDateStr);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("currentUser", currentUser);

            req.getRequestDispatcher("/jsp/attendance/list.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "获取签到记录失败: " + e.getMessage());
            req.getRequestDispatcher("/jsp/attendance/list.jsp").forward(req, resp);
        }
    }

    // 获取考勤统计（JSON）
    private void getStatistics(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"未登录\"}");
            return;
        }

        try {
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");

            Date startDate = startDateStr != null && !startDateStr.isEmpty() ? sdf.parse(startDateStr) : null;
            Date endDate = endDateStr != null && !endDateStr.isEmpty() ? sdf.parse(endDateStr) : null;

            Map<String, Object> stats = attendanceDAO.getStatistics(
                currentUser.getId(), startDate, endDate);

            resp.setContentType("application/json;charset=UTF-8");
            // 手动构建JSON
            String json = "{";
            json += "\"totalDays\":" + stats.get("totalDays") + ",";
            json += "\"normalDays\":" + stats.get("normalDays") + ",";
            json += "\"lateDays\":" + stats.get("lateDays") + ",";
            json += "\"leaveDays\":" + stats.get("leaveDays") + ",";
            json += "\"absentDays\":" + stats.get("absentDays") + ",";
            json += "\"totalWorkDuration\":" + stats.get("totalWorkDuration");
            json += "}";
            resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    // 管理后台 - 学习管理
    private void showManage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            String startDateStr = req.getParameter("startDate");
            String endDateStr = req.getParameter("endDate");
            String userIdStr = req.getParameter("userId");
            String pageStr = req.getParameter("page");

            Date startDate = startDateStr != null && !startDateStr.isEmpty() ? sdf.parse(startDateStr) : null;
            Date endDate = endDateStr != null && !endDateStr.isEmpty() ? sdf.parse(endDateStr) : null;
            Integer userId = userIdStr != null && !userIdStr.isEmpty() ? Integer.parseInt(userIdStr) : null;
            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int pageSize = 30;
            int offset = (page - 1) * pageSize;

            List<StudySession> sessionList = studyDAO.getAllSessions(startDate, endDate, userId, offset, pageSize);
            Map<String, Object> sessionStats = studyDAO.getStatistics(userId, startDate, endDate);
            List<User> memberList = userDAO.findByConditions(null, "MEMBER", null);

            req.setAttribute("sessionList", sessionList);
            req.setAttribute("sessionStats", sessionStats);
            req.setAttribute("memberList", memberList);
            req.setAttribute("startDate", startDateStr);
            req.setAttribute("endDate", endDateStr);
            req.setAttribute("selectedUserId", userIdStr);
            req.setAttribute("currentPage", page);
            req.setAttribute("currentUser", currentUser);

            req.getRequestDispatcher("/jsp/admin/attendance/manage.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "获取学习记录失败: " + e.getMessage());
            req.getRequestDispatcher("/jsp/admin/attendance/manage.jsp").forward(req, resp);
        }
    }

    // 签到
    private void doCheckIn(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
            return;
        }

        try {
            // 检查今日是否已签到
            Attendance today = attendanceDAO.getTodayAttendance(currentUser.getId());
            if (today != null && today.getCheckInTime() != null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"您今日已签到，无需重复签到\"}");
                return;
            }

            // 判断是否迟到（9:30为阈值）
            Calendar now = Calendar.getInstance();
            Calendar threshold = Calendar.getInstance();
            threshold.set(Calendar.HOUR_OF_DAY, 9);
            threshold.set(Calendar.MINUTE, 30);

            String status = now.after(threshold) ? "LATE" : "NORMAL";

            // 获取客户端信息
            String userAgent = req.getHeader("User-Agent");
            String location = req.getParameter("location");

            Attendance attendance = new Attendance();
            attendance.setUserId(currentUser.getId());
            attendance.setAttendanceDate(new Date());
            attendance.setCheckInTime(new Date());
            attendance.setCheckInStatus(status);
            attendance.setLocation(location);
            attendance.setDeviceInfo(userAgent);

            attendanceDAO.checkIn(attendance);

            resp.setContentType("application/json;charset=UTF-8");
            String message = "签到成功" + ("LATE".equals(status) ? "（迟到）" : "");
            resp.getWriter().write("{\"success\":true,\"message\":\"" + message + "\",\"status\":\"" + status + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"签到失败: " + e.getMessage() + "\"}");
        }
    }

    // 签退
    private void doCheckOut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
            return;
        }

        try {
            // 检查今日是否已签到
            Attendance today = attendanceDAO.getTodayAttendance(currentUser.getId());
            if (today == null || today.getCheckInTime() == null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"请先签到后再签退\"}");
                return;
            }

            // 检查是否已签退
            if (today.getCheckOutTime() != null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"您今日已签退\"}");
                return;
            }

            // 判断是否早退（17:30为阈值）
            Calendar now = Calendar.getInstance();
            Calendar threshold = Calendar.getInstance();
            threshold.set(Calendar.HOUR_OF_DAY, 17);
            threshold.set(Calendar.MINUTE, 30);

            String status = now.before(threshold) ? "EARLY" : "NORMAL";

            Attendance attendance = new Attendance();
            attendance.setUserId(currentUser.getId());
            attendance.setAttendanceDate(new Date());
            attendance.setCheckOutTime(new Date());
            attendance.setCheckOutStatus(status);

            attendanceDAO.checkOut(attendance);

            // 重新获取更新后的记录以计算工作时长
            Attendance updated = attendanceDAO.getTodayAttendance(currentUser.getId());

            resp.setContentType("application/json;charset=UTF-8");
            String message = "签退成功" + ("EARLY".equals(status) ? "（早退）" : "");
            resp.getWriter().write("{\"success\":true,\"message\":\"" + message + "\",\"status\":\"" + status + "\",\"workDuration\":" + (updated.getWorkDuration() != null ? updated.getWorkDuration() : 0) + "}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"签退失败: " + e.getMessage() + "\"}");
        }
    }
}
