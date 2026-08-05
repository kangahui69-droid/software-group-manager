package com.softwaregroup.activity.dao;

import com.softwaregroup.activity.model.entity.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动数据访问层
 */
@Repository
public class ActivityDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ActivityParticipantDAO registrationDAO = new ActivityParticipantDAO();

    /**
     * 查询所有活动
     */
    public List<Activity> findAll() {
        return findByConditions(null, null, null, null);
    }

    /**
     * 根据条件搜索活动
     */
    public List<Activity> findByConditions(String keyword, String activityType, String status, String approvalStatus) {
        StringBuilder sql = new StringBuilder("SELECT * FROM activity WHERE deleted = 0");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR location LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (activityType != null && !activityType.trim().isEmpty()) {
            sql.append(" AND activity_type = ?");
            params.add(activityType);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (approvalStatus != null && !approvalStatus.trim().isEmpty()) {
            sql.append(" AND approval_status = ?");
            params.add(approvalStatus);
        }
        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), new ActivityRowMapper());
    }

    /**
     * 根据ID查询活动
     */
    public Activity findById(Integer id) {
        String sql = "SELECT * FROM activity WHERE id = ? AND deleted = 0";
        List<Activity> results = jdbcTemplate.query(sql, new ActivityRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 添加活动
     */
    public Activity insert(Activity activity) {
        String sql = "INSERT INTO activity (name, description, activity_type, activity_start_time, activity_end_time, " +
                "location, organizers, contact_info, registration_start_time, registration_end_time, " +
                "max_participants, status, approval_status, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, activity.getTitle());
            ps.setString(2, activity.getDescription());
            ps.setTimestamp(3, activity.getActivityStartTime() != null ? new Timestamp(activity.getActivityStartTime().getTime()) : null);
            ps.setTimestamp(4, activity.getActivityEndTime() != null ? new Timestamp(activity.getActivityEndTime().getTime()) : null);
            ps.setString(5, activity.getLocation());
            ps.setString(6, activity.getOrganizers());
            ps.setString(7, activity.getContactInfo());
            ps.setTimestamp(8, activity.getRegistrationStartTime() != null ? new Timestamp(activity.getRegistrationStartTime().getTime()) : null);
            ps.setTimestamp(9, activity.getRegistrationEndTime() != null ? new Timestamp(activity.getRegistrationEndTime().getTime()) : null);
            ps.setInt(10, activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0);
            ps.setString(11, activity.getStatus() != null ? activity.getStatus() : "upcoming");
            ps.setString(12, activity.getApprovalStatus() != null ? activity.getApprovalStatus() : "pending");
            ps.setObject(13, activity.getCreatorId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            activity.setId(key.intValue());
        }
        return activity;
    }

    /**
     * 更新活动
     */
    public boolean update(Activity activity) {
        String sql = "UPDATE activity SET name=?, description=?, activity_type=?, activity_start_time=?, " +
                "activity_end_time=?, location=?, organizers=?, contact_info=?, registration_start_time=?, " +
                "registration_end_time=?, max_participants=?, status=?, approval_status=? WHERE id=?";

        int rows = jdbcTemplate.update(sql,
                activity.getTitle(),
                activity.getDescription(),
                activity.getActivityType(),
                activity.getActivityStartTime() != null ? new Timestamp(activity.getActivityStartTime().getTime()) : null,
                activity.getActivityEndTime() != null ? new Timestamp(activity.getActivityEndTime().getTime()) : null,
                activity.getLocation(),
                activity.getOrganizers(),
                activity.getContactInfo(),
                activity.getRegistrationStartTime() != null ? new Timestamp(activity.getRegistrationStartTime().getTime()) : null,
                activity.getRegistrationEndTime() != null ? new Timestamp(activity.getRegistrationEndTime().getTime()) : null,
                activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0,
                activity.getStatus(),
                activity.getApprovalStatus() != null ? activity.getApprovalStatus() : "approved",
                activity.getId());
        return rows > 0;
    }

    /**
     * 删除活动（软删除）
     */
    public boolean delete(Integer id) {
        String sql = "UPDATE activity SET deleted = 1, status = 'canceled' WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    /**
     * 审批通过活动
     */
    public boolean approveActivity(Integer activityId) {
        String sql = "UPDATE activity SET approval_status = 'approved' WHERE id = ?";
        return jdbcTemplate.update(sql, activityId) > 0;
    }

    /**
     * 审批拒绝活动
     */
    public boolean rejectActivity(Integer activityId) {
        String sql = "UPDATE activity SET approval_status = 'rejected' WHERE id = ?";
        return jdbcTemplate.update(sql, activityId) > 0;
    }

    /**
     * 查询用户创建的活动
     */
    public List<Activity> findByCreatorId(Integer creatorId) {
        String sql = "SELECT * FROM activity WHERE creator_id = ? AND deleted = 0 ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ActivityRowMapper(), creatorId);
    }

    /**
     * 统计待审核的活动数量
     */
    public int countPendingReview() {
        String sql = "SELECT COUNT(*) FROM activity WHERE status = 'pending' AND deleted = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    private static class ActivityRowMapper implements RowMapper<Activity> {
        @Override
        public Activity mapRow(ResultSet rs, int rowNum) throws SQLException {
            Activity activity = new Activity();
            activity.setId(rs.getInt("id"));
            activity.setTitle(rs.getString("name"));
            activity.setDescription(rs.getString("description"));
            activity.setActivityType(rs.getString("activity_type"));

            Timestamp activityStartTime = rs.getTimestamp("activity_start_time");
            activity.setActivityStartTime(activityStartTime != null ? new java.util.Date(activityStartTime.getTime()) : null);

            Timestamp activityEndTime = rs.getTimestamp("activity_end_time");
            activity.setActivityEndTime(activityEndTime != null ? new java.util.Date(activityEndTime.getTime()) : null);

            activity.setLocation(rs.getString("location"));
            activity.setOrganizers(rs.getString("organizers"));
            activity.setContactInfo(rs.getString("contact_info"));

            Timestamp registrationStartTime = rs.getTimestamp("registration_start_time");
            activity.setRegistrationStartTime(registrationStartTime != null ? new java.util.Date(registrationStartTime.getTime()) : null);

            Timestamp registrationEndTime = rs.getTimestamp("registration_end_time");
            activity.setRegistrationEndTime(registrationEndTime != null ? new java.util.Date(registrationEndTime.getTime()) : null);

            activity.setMaxParticipants(rs.getInt("max_participants"));
            activity.setStatus(rs.getString("status"));
            activity.setApprovalStatus(rs.getString("approval_status"));
            activity.setCreatorId((Integer) rs.getObject("creator_id"));
            activity.setCreatedAt(rs.getTimestamp("created_at"));
            activity.setUpdatedAt(rs.getTimestamp("updated_at"));

            activity.setRegistrationOpen(activity.isInRegistrationPeriod());
            return activity;
        }
    }
}
