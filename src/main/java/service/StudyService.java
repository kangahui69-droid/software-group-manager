package service;

import dao.StudySessionDAO;
import dao.UserDAO;
import model.StudySession;
import model.User;
import util.Result;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习服务 - 业务逻辑层
 *
 * 对应：StudySessionServlet
 * 核心方法：
 * - startSession(userId) - 开始学习
 * - endSession(userId) - 结束学习(获取当前进行中)
 * - autoEndSession() - 自动结束超时会话(22:00)
 * - getSessionDetail(id) - 学习记录详情
 * - listSessions(filter, page, pageSize) - 学习记录列表(分页)
 * - getMySessions(userId, page, pageSize) - 我的学习记录
 * - getTodaySession(userId) - 获取今日进行中会话
 * - getStatistics(userId) - 学习统计
 * - getWeekStatistics(userId) - 本周学习统计
 * - getConsecutiveDays(userId) - 连续学习天数
 */
public class StudyService {

    private static final int MAX_PAGE_SIZE = 100;

    private final StudySessionDAO studySessionDAO;
    private final UserDAO userDAO;

    public StudyService() {
        this.studySessionDAO = new StudySessionDAO();
        this.userDAO = new UserDAO();
    }

    public StudyService(StudySessionDAO studySessionDAO, UserDAO userDAO) {
        this.studySessionDAO = studySessionDAO;
        this.userDAO = userDAO;
    }

    // ==================== 公开业务方法 ====================

    /**
     * 开始学习
     */
    public Result startSession(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            StudySession activeSession = studySessionDAO.getTodayActiveSession(userId);
            if (activeSession != null) {
                return Result.error(400, "您已有进行中的学习时段，请先结束");
            }

            StudySession session = buildNewSession(userId);
            int id = studySessionDAO.startStudy(session);
            return Result.ok(id);
        } catch (SQLException | RuntimeException e) {
            return serverError(e);
        }
    }

    /**
     * 结束学习
     */
    public Result endSession(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            StudySession activeSession = studySessionDAO.getTodayActiveSession(userId);
            if (activeSession == null) {
                return Result.error(400, "没有进行中的学习时段");
            }

            studySessionDAO.endStudy(activeSession.getId());
            Integer duration = studySessionDAO.getTodayDuration(userId);
            return Result.ok(duration);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 自动结束所有超时会话(22:00定时任务)
     */
    public Result autoEndSession() {
        try {
            int count = studySessionDAO.endAllActiveSessions();
            return Result.ok(count);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取学习记录详情
     */
    public Result getSessionDetail(Integer id) {
        if (id == null) {
            return Result.error(400, "学习记录ID不能为空");
        }

        try {
            StudySession session = studySessionDAO.findById(id);
            if (session == null) {
                return Result.error(404, "学习记录不存在");
            }
            return Result.ok(session);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取学习记录列表(分页)
     */
    public Result listSessions(Map<String, Object> filter, int page, int pageSize) {
        if (!isValidPage(page)) {
            return Result.error(400, "页码必须大于0");
        }
        if (!isValidPageSize(pageSize)) {
            return Result.error(400, "每页大小必须大于0且不超过" + MAX_PAGE_SIZE);
        }

        try {
            Integer userId = extractFilterValue(filter, "userId", Integer.class);
            java.util.Date startDate = extractFilterDate(filter, "startDate");
            java.util.Date endDate = extractFilterDate(filter, "endDate");

            int offset = (page - 1) * pageSize;
            List<StudySession> list = studySessionDAO.getSessionList(userId, startDate, endDate, offset, pageSize);
            int total = studySessionDAO.getTotalCount(userId, startDate, endDate);

            return paginated(list, total, page, pageSize);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取我的学习记录
     */
    public Result getMySessions(Integer userId, int page, int pageSize) {
        Result validation = validateUserQueryParams(userId, page, pageSize);
        if (validation != null) {
            return validation;
        }

        try {
            int offset = (page - 1) * pageSize;
            List<StudySession> list = studySessionDAO.getSessionList(userId, null, null, offset, pageSize);
            int total = studySessionDAO.getTotalCount(userId, null, null);

            return paginated(list, total, page, pageSize);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取今日进行中会话
     */
    public Result getTodaySession(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            StudySession session = studySessionDAO.getTodayActiveSession(userId);
            return Result.ok(session);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取学习统计
     */
    public Result getStatistics(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            Map<String, Object> stats = studySessionDAO.getStatistics(userId, null, null);
            return Result.ok(stats);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取本周学习统计
     */
    public Result getWeekStatistics(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            Map<String, Object> stats = studySessionDAO.getWeekStatistics(userId);
            return Result.ok(stats);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 获取连续学习天数
     */
    public Result getConsecutiveDays(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        try {
            int days = studySessionDAO.getConsecutiveDays(userId);
            return Result.ok(days);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private StudySession buildNewSession(Integer userId) {
        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setSessionDate(new java.util.Date());
        session.setCheckInTime(new java.util.Date());
        session.setStatus("ACTIVE");
        return session;
    }

    private Result paginated(List<StudySession> list, int total, int page, int pageSize) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);
        resultMap.put("total", total);
        resultMap.put("page", page);
        resultMap.put("pageSize", pageSize);
        return Result.ok(resultMap);
    }

    private Result serverError(Exception e) {
        return Result.error(500, "数据库错误: " + e.getMessage());
    }

    private boolean isValidPage(int page) {
        return page > 0;
    }

    private boolean isValidPageSize(int pageSize) {
        if (pageSize <= 0) return false;
        if (pageSize == Integer.MAX_VALUE) return true;
        return pageSize <= MAX_PAGE_SIZE;
    }

    private Result validateUserQueryParams(Integer userId, int page, int pageSize) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (!isValidPage(page)) {
            return Result.error(400, "页码必须大于0");
        }
        if (!isValidPageSize(pageSize)) {
            return Result.error(400, "每页大小必须大于0且不超过" + MAX_PAGE_SIZE);
        }
        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return null;
    }

    private <T> T extractFilterValue(Map<String, Object> filter, String key, Class<T> type) {
        if (filter == null) return null;
        Object value = filter.get(key);
        if (value == null) return null;
        if (type.isInstance(value)) return type.cast(value);
        if (value instanceof Number && type == Integer.class) {
            return type.cast(((Number) value).intValue());
        }
        return null;
    }

    private java.util.Date extractFilterDate(Map<String, Object> filter, String key) {
        if (filter == null) return null;
        Object value = filter.get(key);
        if (value == null) return null;
        if (value instanceof java.util.Date) return (java.util.Date) value;
        if (value instanceof java.sql.Date) return new java.util.Date(((java.sql.Date) value).getTime());
        return null;
    }
}
