package dao;

import model.StudySession;
import util.DBUtil;

import java.sql.*;
import java.util.*;

/**
 * 学习时段数据访问层
 */
public class StudySessionDAO {

    /**
     * 开始学习（签到）
     */
    public int startStudy(StudySession session) throws SQLException {
        String sql = "INSERT INTO study_session (user_id, session_date, check_in_time, status) " +
                     "VALUES (?, ?, ?, 'ACTIVE')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, session.getUserId());
            ps.setDate(2, new java.sql.Date(session.getSessionDate().getTime()));
            ps.setTimestamp(3, session.getCheckInTime() != null ?
                new Timestamp(session.getCheckInTime().getTime()) : new Timestamp(System.currentTimeMillis()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return rows;
        }
    }

    /**
     * 结束学习（签退）
     */
    public int endStudy(Integer sessionId) throws SQLException {
        String sql = "UPDATE study_session SET check_out_time = NOW(), " +
                     "duration = TIMESTAMPDIFF(MINUTE, check_in_time, NOW()), " +
                     "status = 'COMPLETED' WHERE id = ? AND status = 'ACTIVE'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            return ps.executeUpdate();
        }
    }

    /**
     * 获取今日进行中的学习时段
     */
    public StudySession getTodayActiveSession(Integer userId) throws SQLException {
        String sql = "SELECT * FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'ACTIVE' ORDER BY check_in_time DESC LIMIT 1";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractSession(rs);
            }
            return null;
        }
    }

    /**
     * 获取用户学习记录列表
     */
    public List<StudySession> getSessionList(Integer userId, java.util.Date startDate, java.util.Date endDate, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT s.*, u.name as user_name FROM study_session s " +
                     "LEFT JOIN user u ON s.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND s.user_id = ?");
            params.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND s.session_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND s.session_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        sql.append(" ORDER BY s.session_date DESC, s.check_in_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<StudySession> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StudySession session = extractSession(rs);
                session.setUserName(rs.getString("user_name"));
                list.add(session);
            }
        }
        return list;
    }

    /**
     * 管理员获取所有学习记录
     */
    public List<StudySession> getAllSessions(java.util.Date startDate, java.util.Date endDate, Integer userId, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT s.*, u.name as user_name FROM study_session s " +
                     "LEFT JOIN user u ON s.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND s.user_id = ?");
            params.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND s.session_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND s.session_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        sql.append(" ORDER BY s.session_date DESC, s.check_in_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<StudySession> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StudySession session = extractSession(rs);
                session.setUserName(rs.getString("user_name"));
                list.add(session);
            }
        }
        return list;
    }

    /**
     * 获取学习时长统计
     */
    public Map<String, Object> getStatistics(Integer userId, java.util.Date startDate, java.util.Date endDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " +
                     "COUNT(*) as total_sessions, " +
                     "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_sessions, " +
                     "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_sessions, " +
                     "SUM(duration) as total_duration, " +
                     "AVG(duration) as avg_duration " +
                     "FROM study_session WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND session_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND session_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("totalSessions", rs.getInt("total_sessions"));
                stats.put("completedSessions", rs.getInt("completed_sessions"));
                stats.put("activeSessions", rs.getInt("active_sessions"));
                stats.put("totalDuration", rs.getInt("total_duration"));
                stats.put("avgDuration", rs.getDouble("avg_duration"));
            }
        }
        return stats;
    }

    /**
     * 获取今日学习时长
     */
    public Integer getTodayDuration(Integer userId) throws SQLException {
        String sql = "SELECT SUM(duration) as today_duration FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'COMPLETED'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("today_duration");
            }
        }
        return 0;
    }

    /**
     * 获取本周学习时长统计
     */
    public Map<String, Object> getWeekStatistics(Integer userId) throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) as week_sessions, " +
                     "SUM(duration) as week_duration " +
                     "FROM study_session WHERE user_id = ? " +
                     "AND session_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) " +
                     "AND session_date <= CURDATE() " +
                     "AND status = 'COMPLETED'";

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                stats.put("weekSessions", rs.getInt("week_sessions"));
                stats.put("weekDuration", rs.getInt("week_duration"));
            }
        }
        return stats;
    }

    /**
     * 获取连续学习天数
     */
    public int getConsecutiveDays(Integer userId) throws SQLException {
        String sql = "SELECT session_date FROM study_session " +
                     "WHERE user_id = ? AND status = 'COMPLETED' " +
                     "ORDER BY session_date DESC";

        int consecutiveDays = 0;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            Calendar expectedDate = Calendar.getInstance();
            expectedDate.add(Calendar.DAY_OF_MONTH, -1); // 从昨天开始检查

            while (rs.next()) {
                java.sql.Date date = rs.getDate("session_date");
                Calendar sessionDate = Calendar.getInstance();
                sessionDate.setTime(date);

                // 检查日期是否连续
                Calendar expected = (Calendar) expectedDate.clone();
                expected.add(Calendar.DAY_OF_MONTH, 1);

                if (sessionDate.getTimeInMillis() == expected.getTimeInMillis() ||
                    (consecutiveDays == 0 && sessionDate.getTimeInMillis() >= expectedDate.getTimeInMillis())) {
                    consecutiveDays++;
                    expectedDate = sessionDate;
                } else if (consecutiveDays > 0) {
                    break;
                }
            }
        }
        return consecutiveDays;
    }

    /**
     * 检查今天是否已有签退记录
     */
    public Boolean hasCheckedOutToday(Integer userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'COMPLETED' AND check_out_time IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * 获取记录总数
     */
    public int getTotalCount(Integer userId, java.util.Date startDate, java.util.Date endDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM study_session WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND session_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND session_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private StudySession extractSession(ResultSet rs) throws SQLException {
        StudySession session = new StudySession();
        session.setId(rs.getInt("id"));
        session.setUserId(rs.getInt("user_id"));
        session.setSessionDate(rs.getDate("session_date"));
        session.setCheckInTime(rs.getTimestamp("check_in_time"));
        session.setCheckOutTime(rs.getTimestamp("check_out_time"));
        session.setDuration(rs.getInt("duration"));
        session.setStatus(rs.getString("status"));
        session.setCreatedAt(rs.getTimestamp("created_at"));
        session.setUpdatedAt(rs.getTimestamp("updated_at"));
        return session;
    }
}
