package com.softwaregroup.activity.dao;

import com.softwaregroup.activity.model.entity.StudySession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 学习时段数据访问层
 */
@Repository
public class StudySessionDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 开始学习（签到）
     */
    public int startStudy(StudySession session) {
        String sql = "INSERT INTO study_session (user_id, session_date, check_in_time, status) " +
                "VALUES (?, ?, ?, 'ACTIVE')";

        return jdbcTemplate.update(sql,
                session.getUserId(),
                new java.sql.Date(session.getSessionDate().getTime()),
                session.getCheckInTime() != null ? new Timestamp(session.getCheckInTime().getTime()) : new Timestamp(System.currentTimeMillis()));
    }

    /**
     * 结束学习（签退）
     */
    public int endStudy(Integer sessionId) {
        String sql = "UPDATE study_session SET check_out_time = NOW(), " +
                "duration = TIMESTAMPDIFF(MINUTE, check_in_time, NOW()), " +
                "status = 'COMPLETED' WHERE id = ? AND status = 'ACTIVE'";
        return jdbcTemplate.update(sql, sessionId);
    }

    /**
     * 结束所有今日进行中的学习时段（用于22:00定时任务）
     */
    public int endAllActiveSessions() {
        String sql = "UPDATE study_session SET check_out_time = NOW(), " +
                "duration = TIMESTAMPDIFF(MINUTE, check_in_time, NOW()), " +
                "status = 'COMPLETED' WHERE session_date = CURDATE() AND status = 'ACTIVE'";
        return jdbcTemplate.update(sql);
    }

    /**
     * 获取今日进行中的学习时段
     */
    public StudySession getTodayActiveSession(Integer userId) {
        String sql = "SELECT * FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'ACTIVE' ORDER BY check_in_time DESC LIMIT 1";
        List<StudySession> results = jdbcTemplate.query(sql, new StudySessionRowMapper(), userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 获取用户学习记录列表
     */
    public List<StudySession> getSessionList(Integer userId, Date startDate, Date endDate, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT s.*, u.name as user_name FROM study_session s " +
                "LEFT JOIN user u ON s.user_id = u.id WHERE 1=1");
        List<Object> args = new java.util.ArrayList<>();

        if (userId != null) {
            sql.append(" AND s.user_id = ?");
            args.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND s.session_date >= ?");
            args.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND s.session_date <= ?");
            args.add(new java.sql.Date(endDate.getTime()));
        }
        sql.append(" ORDER BY s.session_date DESC, s.check_in_time DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), args.toArray(), new StudySessionRowMapper());
    }

    /**
     * 获取学习时长统计
     */
    public Map<String, Object> getStatistics(Integer userId, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "COUNT(*) as total_sessions, " +
                "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_sessions, " +
                "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_sessions, " +
                "SUM(duration) as total_duration, " +
                "AVG(duration) as avg_duration " +
                "FROM study_session WHERE 1=1");

        List<Object> args = new java.util.ArrayList<>();
        if (userId != null) {
            sql.append(" AND user_id = ?");
            args.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND session_date >= ?");
            args.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND session_date <= ?");
            args.add(new java.sql.Date(endDate.getTime()));
        }

        return jdbcTemplate.queryForMap(sql.toString(), args.toArray());
    }

    /**
     * 获取今日学习时长
     */
    public Integer getTodayDuration(Integer userId) {
        String sql = "SELECT SUM(duration) as today_duration FROM study_session WHERE user_id = ? AND session_date = CURDATE() AND status = 'COMPLETED'";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return result != null ? result : 0;
    }

    /**
     * 获取本周学习时长统计
     */
    public Map<String, Object> getWeekStatistics(Integer userId) {
        String sql = "SELECT " +
                "COUNT(*) as week_sessions, " +
                "SUM(duration) as week_duration " +
                "FROM study_session WHERE user_id = ? " +
                "AND session_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) " +
                "AND session_date <= CURDATE() " +
                "AND status = 'COMPLETED'";

        return jdbcTemplate.queryForMap(sql, userId);
    }

    /**
     * 获取连续学习天数
     */
    public int getConsecutiveDays(Integer userId) {
        String sql = "SELECT session_date FROM study_session " +
                "WHERE user_id = ? AND status = 'COMPLETED' " +
                "ORDER BY session_date DESC";

        List<Date> dates = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getDate("session_date"), userId);

        int consecutiveDays = 0;
        Calendar expectedDate = Calendar.getInstance();
        expectedDate.add(Calendar.DAY_OF_MONTH, -1);

        for (Date date : dates) {
            Calendar sessionDate = Calendar.getInstance();
            sessionDate.setTime(date);

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
        return consecutiveDays;
    }

    /**
     * 获取记录总数
     */
    public int getTotalCount(Integer userId, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM study_session WHERE 1=1");
        List<Object> args = new java.util.ArrayList<>();

        if (userId != null) {
            sql.append(" AND user_id = ?");
            args.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND session_date >= ?");
            args.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND session_date <= ?");
            args.add(new java.sql.Date(endDate.getTime()));
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return count != null ? count : 0;
    }

    /**
     * 根据ID查询学习记录
     */
    public StudySession findById(Integer id) {
        String sql = "SELECT * FROM study_session WHERE id = ?";
        List<StudySession> results = jdbcTemplate.query(sql, new StudySessionRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    private static class StudySessionRowMapper implements RowMapper<StudySession> {
        @Override
        public StudySession mapRow(ResultSet rs, int rowNum) throws SQLException {
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

            try {
                session.setUserName(rs.getString("user_name"));
            } catch (Exception ignored) {}

            return session;
        }
    }
}
