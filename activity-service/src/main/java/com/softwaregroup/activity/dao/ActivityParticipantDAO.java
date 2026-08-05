package com.softwaregroup.activity.dao;

import com.softwaregroup.activity.model.entity.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 活动参与者数据访问层
 */
@Repository
public class ActivityParticipantDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 报名活动（默认pending状态）
     */
    public boolean register(Integer activityId, Integer userId) {
        return register(activityId, userId, false);
    }

    /**
     * 报名活动（可指定是否自动审批）
     */
    public boolean register(Integer activityId, Integer userId, boolean autoApproved) {
        String status = autoApproved ? "confirmed" : "pending";
        String sql = "INSERT INTO activity_participant (activity_id, user_id, status, created_at) VALUES (?, ?, ?, NOW())";
        try {
            int rows = jdbcTemplate.update(sql, activityId, userId, status);
            return rows > 0;
        } catch (Exception e) {
            // 可能是重复报名
            return false;
        }
    }

    /**
     * 取消报名（只取消pending状态的报名）
     */
    public boolean cancelRegistration(Integer activityId, Integer userId) {
        String sql = "DELETE FROM activity_participant WHERE activity_id = ? AND user_id = ? AND status = 'pending'";
        return jdbcTemplate.update(sql, activityId, userId) > 0;
    }

    /**
     * 检查用户是否已报名某活动
     */
    public boolean isRegistered(Integer activityId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND user_id = ? AND deleted = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, activityId, userId);
        return count != null && count > 0;
    }

    /**
     * 获取活动的报名人数（按状态筛选）
     */
    public int getParticipantCount(Integer activityId, String status) {
        String sql;
        Object[] args;
        if (status == null) {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND deleted = 0";
            args = new Object[]{activityId};
        } else {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND status = ? AND deleted = 0";
            args = new Object[]{activityId, status};
        }
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return count != null ? count : 0;
    }

    /**
     * 获取用户在某活动中的报名状态
     */
    public String getRegistrationStatus(Integer activityId, Integer userId) {
        String sql = "SELECT status FROM activity_participant WHERE activity_id = ? AND user_id = ? AND deleted = 0";
        List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("status"), activityId, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 更新报名状态
     */
    public boolean updateStatus(Integer activityId, Integer userId, String status, String notes) {
        String sql;
        if (notes != null) {
            sql = "UPDATE activity_participant SET status = ?, notes = ?, updated_at = NOW() WHERE activity_id = ? AND user_id = ?";
            return jdbcTemplate.update(sql, status, notes, activityId, userId) > 0;
        } else {
            sql = "UPDATE activity_participant SET status = ?, updated_at = NOW() WHERE activity_id = ? AND user_id = ?";
            return jdbcTemplate.update(sql, status, activityId, userId) > 0;
        }
    }

    /**
     * 批量更新报名状态
     */
    public int batchUpdateStatus(List<Integer> userIds, Integer activityId, String status) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE activity_participant SET status = ?, updated_at = NOW() WHERE activity_id = ? AND user_id IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sql.append("?");
            if (i < userIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        Object[] args = new Object[userIds.size() + 2];
        args[0] = status;
        args[1] = activityId;
        for (int i = 0; i < userIds.size(); i++) {
            args[2 + i] = userIds.get(i);
        }
        return jdbcTemplate.update(sql.toString(), args);
    }

    /**
     * 获取用户的所有报名记录
     */
    public List<Registration> findByUserId(Integer userId) {
        String sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                "a.location, a.registration_end_time, a.status as activity_status " +
                "FROM activity_participant ap " +
                "JOIN activity a ON ap.activity_id = a.id " +
                "WHERE ap.user_id = ? AND a.deleted = 0 AND ap.deleted = 0 " +
                "ORDER BY ap.created_at DESC";
        return jdbcTemplate.query(sql, new RegistrationRowMapper(), userId);
    }

    /**
     * 获取活动的所有报名成员
     */
    public List<Registration> findByActivityId(Integer activityId) {
        String sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                "a.location, a.registration_end_time, " +
                "u.name as user_name, u.email as user_email, u.phone as user_phone, " +
                "mp.student_id, mp.major, mp.grade as grade_class " +
                "FROM activity_participant ap " +
                "JOIN activity a ON ap.activity_id = a.id " +
                "LEFT JOIN user u ON ap.user_id = u.id " +
                "LEFT JOIN member_profile mp ON u.id = mp.user_id " +
                "WHERE ap.activity_id = ? AND ap.status NOT IN ('rejected', 'cancelled') AND ap.deleted = 0 " +
                "ORDER BY ap.created_at ASC";
        return jdbcTemplate.query(sql, new RegistrationRowMapper(), activityId);
    }

    /**
     * 获取活动的指定状态的报名记录
     */
    public List<Registration> findByActivityIdAndStatus(Integer activityId, String status) {
        String sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                "a.location, a.registration_end_time, " +
                "u.name as user_name, u.email as user_email, u.phone as user_phone, " +
                "mp.student_id, mp.major, mp.grade as grade_class " +
                "FROM activity_participant ap " +
                "JOIN activity a ON ap.activity_id = a.id " +
                "LEFT JOIN user u ON ap.user_id = u.id " +
                "LEFT JOIN member_profile mp ON u.id = mp.user_id " +
                "WHERE ap.activity_id = ? AND ap.status = ? AND ap.deleted = 0 " +
                "ORDER BY ap.created_at ASC";
        return jdbcTemplate.query(sql, new RegistrationRowMapper(), activityId, status);
    }

    /**
     * 删除活动的所有报名记录（软删除）
     */
    public int deleteByActivityId(Integer activityId) {
        String sql = "UPDATE activity_participant SET deleted = 1 WHERE activity_id = ?";
        return jdbcTemplate.update(sql, activityId);
    }

    private static class RegistrationRowMapper implements RowMapper<Registration> {
        @Override
        public Registration mapRow(ResultSet rs, int rowNum) throws SQLException {
            Registration reg = new Registration();
            reg.setActivityId(rs.getInt("activity_id"));
            reg.setUserId(rs.getInt("user_id"));
            reg.setStatus(rs.getString("status"));
            reg.setCreatedAt(rs.getTimestamp("created_at"));
            reg.setUpdatedAt(rs.getTimestamp("updated_at"));
            reg.setNotes(rs.getString("notes"));

            // 关联活动信息
            reg.setActivityName(rs.getString("activity_name"));
            Timestamp activityStartTime = rs.getTimestamp("activity_start_time");
            reg.setActivityStartTime(activityStartTime != null ? new java.util.Date(activityStartTime.getTime()) : null);
            Timestamp activityEndTime = rs.getTimestamp("activity_end_time");
            reg.setActivityEndTime(activityEndTime != null ? new java.util.Date(activityEndTime.getTime()) : null);
            reg.setLocation(rs.getString("location"));
            Timestamp registrationEndTime = rs.getTimestamp("registration_end_time");
            reg.setRegistrationEndTime(registrationEndTime != null ? new java.util.Date(registrationEndTime.getTime()) : null);

            try {
                reg.setActivityStatus(rs.getString("activity_status"));
            } catch (Exception ignored) {}

            // 关联用户信息
            try {
                reg.setUserName(rs.getString("user_name"));
            } catch (Exception ignored) {}
            try {
                reg.setStudentId(rs.getString("student_id"));
            } catch (Exception ignored) {}
            try {
                reg.setMajor(rs.getString("major"));
            } catch (Exception ignored) {}
            try {
                reg.setGrade(rs.getString("grade_class"));
            } catch (Exception ignored) {}

            return reg;
        }
    }
}
