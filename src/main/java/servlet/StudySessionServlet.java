package servlet;

import dao.StudySessionDAO;
import dao.UserDAO;
import model.StudySession;
import model.User;
import util.AuthHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/study/*")
public class StudySessionServlet extends HttpServlet {

    private StudySessionDAO studyDAO = new StudySessionDAO();
    private UserDAO userDAO = new UserDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            showIndex(req, resp);
        } else if (pathInfo.equals("/list")) {
            showList(req, resp);
        } else if (pathInfo.equals("/manage")) {
            showManage(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String action = req.getParameter("action");

        if ("end".equals(action) || (pathInfo != null && pathInfo.equals("/end"))) {
            endStudy(req, resp);
        } else {
            startStudy(req, resp);
        }
    }

    // 学习首页
    private void showIndex(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            // 获取今日进行中的学习时段
            StudySession activeSession = studyDAO.getTodayActiveSession(currentUser.getId());

            // 检查是否需要自动结束（22:00自动结束，不影响后续签到）
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            if (activeSession != null && hour >= 22) {
                studyDAO.endStudy(activeSession.getId());
                activeSession = null; // 刷新状态
            }

            // 重新获取时段（可能刚才被自动结束）
            activeSession = studyDAO.getTodayActiveSession(currentUser.getId());

            // 获取今日学习时长（只统计已完成的）
            Integer todayDuration = studyDAO.getTodayDuration(currentUser.getId());

            // 获取今日统计
            Map<String, Object> stats = studyDAO.getStatistics(currentUser.getId(), new Date(), new Date());

            // 获取今日已完成的学习次数
            int completedToday = stats.get("completedSessions") != null ? (Integer) stats.get("completedSessions") : 0;

            // 获取本周学习统计
            Map<String, Object> weekStats = studyDAO.getWeekStatistics(currentUser.getId());
            int weekDuration = weekStats.get("weekDuration") != null ? (Integer) weekStats.get("weekDuration") : 0;
            int weekSessions = weekStats.get("weekSessions") != null ? (Integer) weekStats.get("weekSessions") : 0;

            // 获取连续学习天数
            int consecutiveDays = studyDAO.getConsecutiveDays(currentUser.getId());

            req.setAttribute("activeSession", activeSession);
            req.setAttribute("todayDuration", todayDuration != null ? todayDuration : 0);
            req.setAttribute("completedToday", completedToday);
            req.setAttribute("todayStats", stats);
            req.setAttribute("weekDuration", weekDuration);
            req.setAttribute("weekSessions", weekSessions);
            req.setAttribute("consecutiveDays", consecutiveDays);
            req.setAttribute("currentUser", currentUser);
            req.setAttribute("nowDate", new Date());

            req.getRequestDispatcher("/jsp/study/index.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "获取学习状态失败: " + e.getMessage());
            req.getRequestDispatcher("/jsp/study/index.jsp").forward(req, resp);
        }
    }

    // 学习记录列表
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

            List<StudySession> list = studyDAO.getSessionList(currentUser.getId(), startDate, endDate, offset, pageSize);
            Map<String, Object> stats = studyDAO.getStatistics(currentUser.getId(), startDate, endDate);
            int total = studyDAO.getTotalCount(currentUser.getId(), startDate, endDate);
            int totalPages = (int) Math.ceil((double) total / pageSize);

            req.setAttribute("sessionList", list);
            req.setAttribute("statistics", stats);
            req.setAttribute("startDate", startDateStr);
            req.setAttribute("endDate", endDateStr);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("currentUser", currentUser);

            req.getRequestDispatcher("/jsp/study/list.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "获取学习记录失败: " + e.getMessage());
            req.getRequestDispatcher("/jsp/study/list.jsp").forward(req, resp);
        }
    }

    // 管理后台
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

            // 默认显示今日记录
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            Date defaultStart = today.getTime();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            Date defaultEnd = today.getTime();

            Date startDate = startDateStr != null && !startDateStr.isEmpty() ? sdf.parse(startDateStr) : defaultStart;
            Date endDate = endDateStr != null && !endDateStr.isEmpty() ? sdf.parse(endDateStr) : defaultEnd;
            Integer userId = userIdStr != null && !userIdStr.isEmpty() ? Integer.parseInt(userIdStr) : null;
            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int pageSize = 30;
            int offset = (page - 1) * pageSize;

            List<StudySession> list = studyDAO.getAllSessions(startDate, endDate, userId, offset, pageSize);
            int total = studyDAO.getTotalCount(userId, startDate, endDate);
            int totalPages = (int) Math.ceil((double) total / pageSize);

            // 获取统计数据
            Map<String, Object> stats = studyDAO.getStatistics(userId, startDate, endDate);

            // 获取所有成员列表（用于下拉选择）
            List<User> userList = userDAO.findAll();

            // 格式化日期用于前端显示
            String displayStartDate = startDateStr != null ? startDateStr : sdf.format(startDate);
            String displayEndDate = endDateStr != null ? endDateStr : sdf.format(endDate);

            req.setAttribute("sessionList", list);
            req.setAttribute("startDate", displayStartDate);
            req.setAttribute("endDate", displayEndDate);
            req.setAttribute("selectedUserId", userIdStr);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("totalCount", total);
            req.setAttribute("statistics", stats);
            req.setAttribute("userList", userList);
            req.setAttribute("currentUser", currentUser);

            req.getRequestDispatcher("/jsp/admin/study/manage.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "获取学习记录失败: " + e.getMessage());
            req.getRequestDispatcher("/jsp/admin/study/manage.jsp").forward(req, resp);
        }
    }

    // 开始学习
    private void startStudy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
            return;
        }

        try {
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            // 检查是否有进行中的学习时段
            StudySession activeSession = studyDAO.getTodayActiveSession(currentUser.getId());
            if (activeSession != null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"您已有进行中的学习时段，请先结束\"}");
                return;
            }

            // 判断签到状态（根据实际签到时间判定）
            String checkStatus = "NORMAL"; // 默认正常
            if (hour >= 6 && hour < 18) {
                // 6:00-18:00 之间开始算早到
                checkStatus = "EARLY";
            } else if (hour >= 18 && hour < 19) {
                // 18:00-19:00 之间开始算正常
                checkStatus = "NORMAL";
            } else if (hour >= 19) {
                // 19:00之后算迟到
                checkStatus = "LATE";
            }

            // 22:00之后不能开始
            if (hour >= 22) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"今日学习时间已结束，请明天再来\"}");
                return;
            }

            // 创建新的学习时段
            StudySession session = new StudySession();
            session.setUserId(currentUser.getId());
            session.setSessionDate(new Date());
            session.setCheckInTime(new Date());
            session.setStatus("ACTIVE");

            studyDAO.startStudy(session);

            // 根据状态返回不同消息
            String message = "开始学习！";
            if ("EARLY".equals(checkStatus)) {
                message = "开始学习（早到）！";
            } else if ("LATE".equals(checkStatus)) {
                message = "开始学习（迟到）！";
            }

            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":true,\"message\":\"" + message + "\",\"startTime\":\"" + sdfTime.format(new Date()) + "\",\"status\":\"" + checkStatus + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"开始学习失败: " + e.getMessage() + "\"}");
        }
    }

    // 结束学习
    private void endStudy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User currentUser = AuthHelper.getCurrentUser(req);
        if (currentUser == null) {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
            return;
        }

        try {
            // 获取进行中的学习时段
            StudySession activeSession = studyDAO.getTodayActiveSession(currentUser.getId());
            if (activeSession == null) {
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"没有进行中的学习时段\"}");
                return;
            }

            // 判断签退状态
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            String checkStatus = "NORMAL"; // 正常
            if (hour < 21 || (hour == 21 && minute < 30)) {
                // 21:30前算早退
                checkStatus = "EARLY";
            } else {
                // 21:30后正常
                checkStatus = "NORMAL";
            }

            // 结束学习
            studyDAO.endStudy(activeSession.getId());

            // 获取更新后的学习时长
            Integer duration = studyDAO.getTodayDuration(currentUser.getId());

            // 根据状态返回不同消息
            String message = "学习结束！";
            if ("EARLY".equals(checkStatus)) {
                message = "学习结束（早退）！";
            }

            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":true,\"message\":\"" + message + "\",\"duration\":" + (duration != null ? duration : 0) + ",\"status\":\"" + checkStatus + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"success\":false,\"message\":\"结束学习失败: " + e.getMessage() + "\"}");
        }
    }
}
