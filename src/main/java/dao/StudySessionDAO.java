package dao;

import model.StudySession;
import util.DBUtil;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 学习时段数据访问层
 */
public class StudySessionDAO {
    private static final ZoneId STUDY_ZONE = ZoneId.of("Asia/Shanghai");
    // Old deployments could store check_in_time in a different zone. Database-managed
    // created_at/check_out_time provide a consistent clock for those completed rows.
    private static final String EFFECTIVE_DURATION = "CASE WHEN duration >= 0 THEN duration "
            + "WHEN status = 'COMPLETED' AND check_out_time >= created_at "
            + "AND TIMESTAMPDIFF(MINUTE, created_at, check_out_time) BETWEEN 0 AND 960 "
            + "THEN TIMESTAMPDIFF(MINUTE, created_at, check_out_time) ELSE NULL END";

    /**
     * 开始学习（签到）
     */
    public int startStudy(StudySession session) throws SQLException {
        String sql = "INSERT INTO study_session (user_id, session_date, check_in_time, status) " +
                     "VALUES (?, CURDATE(), NOW(), 'ACTIVE')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, session.getUserId());

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
                     "duration = GREATEST(0, TIMESTAMPDIFF(MINUTE, created_at, NOW())), " +
                     "status = 'COMPLETED' WHERE id = ? AND status = 'ACTIVE'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sessionId);
            return ps.executeUpdate();
        }
    }

    /**
     * 结束用户今日进行中的学习时段（用于退出登录时自动签退）
     */
    public int endStudyForUser(Integer userId) throws SQLException {
        String sql = "UPDATE study_session SET check_out_time = NOW(), " +
                     "duration = GREATEST(0, TIMESTAMPDIFF(MINUTE, created_at, NOW())), " +
                     "status = 'COMPLETED' WHERE user_id = ? AND session_date = CURDATE() AND status = 'ACTIVE'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    /**
     * 结束所有今日进行中的学习时段（用于22:00定时任务）
     */
    public int endAllActiveSessions() throws SQLException {
        String sql = "UPDATE study_session SET check_out_time = NOW(), " +
                     "duration = GREATEST(0, TIMESTAMPDIFF(MINUTE, created_at, NOW())), " +
                     "status = 'COMPLETED' WHERE session_date = CURDATE() AND status = 'ACTIVE'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();
        }
    }

    /**
     * 获取今日所有进行中的学习时段
     */
    public List<StudySession> getAllActiveSessions() throws SQLException {
        String sql = "SELECT s.*, u.name as user_name FROM study_session s " +
                     "LEFT JOIN user u ON s.user_id = u.id " +
                     "WHERE s.session_date = CURDATE() AND s.status = 'ACTIVE'";

        List<StudySession> sessions = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sessions.add(extractSession(rs));
            }
        }
        return sessions;
    }

    /**
     * 获取今日进行中的学习时段
     */
    public StudySession getTodayActiveSession(Integer userId) throws SQLException {
        String sql = "SELECT * FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'ACTIVE' ORDER BY created_at DESC LIMIT 1";

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

    /** Elapsed time measured by the same database clock used for starting a session. */
    public int getActiveElapsedSeconds(Integer sessionId) throws SQLException {
        String sql = "SELECT GREATEST(0, TIMESTAMPDIFF(SECOND, created_at, NOW())) AS elapsed_seconds "
                + "FROM study_session WHERE id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("elapsed_seconds") : 0;
            }
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
                     "SUM(" + EFFECTIVE_DURATION + ") as total_duration, " +
                     "AVG(" + EFFECTIVE_DURATION + ") as avg_duration " +
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
        String sql = "SELECT SUM(" + EFFECTIVE_DURATION + ") as today_duration FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'COMPLETED'";

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
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String sql = "SELECT " +
                     "COUNT(*) as week_sessions, " +
                     "SUM(" + EFFECTIVE_DURATION + ") as week_duration " +
                     "FROM study_session WHERE user_id = ? " +
                     "AND session_date >= ? AND session_date <= ? " +
                     "AND status = 'COMPLETED'";

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(monday));
            ps.setDate(3, java.sql.Date.valueOf(today));
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
        java.util.Date createdAt = beijingDateTime(rs, "created_at");
        java.util.Date checkIn = beijingDateTime(rs, "check_in_time");
        java.util.Date checkOut = beijingDateTime(rs, "check_out_time");
        if (checkIn != null && createdAt != null && Math.abs(checkIn.getTime() - createdAt.getTime()) > 5 * 60_000L) {
            checkIn = createdAt;
        }
        session.setCheckInTime(checkIn);
        session.setCheckOutTime(checkOut);
        Integer storedDuration = (Integer) rs.getObject("duration");
        if (storedDuration != null && storedDuration < 0) {
            long minutes = createdAt != null && checkOut != null ? (checkOut.getTime() - createdAt.getTime()) / 60_000L : -1;
            storedDuration = minutes >= 0 && minutes <= 960 ? (int) minutes : null;
        }
        session.setDuration(storedDuration);
        session.setStatus(rs.getString("status"));
        session.setCreatedAt(createdAt);
        session.setUpdatedAt(beijingDateTime(rs, "updated_at"));
        return session;
    }

    private java.util.Date beijingDateTime(ResultSet rs, String column) throws SQLException {
        LocalDateTime value = rs.getObject(column, LocalDateTime.class);
        return value == null ? null : java.util.Date.from(value.atZone(STUDY_ZONE).toInstant());
    }

    /**
     * 根据ID查询学习记录
     */
    public StudySession findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM study_session WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return extractSession(rs);
            }
            return null;
        }
    }
}
