package dao;

import model.Activity;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动数据访问层（新版 - 支持时间窗口报名）
 */
public class ActivityDAO {

    /**
     * 查询所有活动
     */
    public List<Activity> findAll() {
        return findByConditions(null, null, null);
    }

    /**
     * 根据条件搜索活动
     * @param keyword 关键词（标题/描述/地点）
     * @param activityType 活动类型
     * @param status 活动状态
     */
    public List<Activity> findByConditions(String keyword, String activityType, String status) {
        List<Activity> activities = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM activity WHERE deleted = 0");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR description LIKE ? OR location LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (activityType != null && !activityType.trim().isEmpty()) {
            sql.append(" AND activity_type = ?");
            params.add(activityType);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY activity_start_time DESC");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return activities;
    }

    /**
     * 查询报名有效期内的活动（页面A默认视图）
     * 当前时间在 registration_start_time 和 registration_end_time 之间
     */
    public List<Activity> findInRegistrationPeriod() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activity WHERE deleted = 0 AND registration_start_time <= NOW() AND registration_end_time >= NOW() ORDER BY registration_end_time ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return activities;
    }

    /**
     * 根据ID查询活动
     */
    public Activity findById(Integer id) {
        String sql = "SELECT * FROM activity WHERE id = ? AND deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToActivity(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 添加活动
     */
    public boolean insert(Activity activity) {
        String sql = "INSERT INTO activity (name, description, activity_type, activity_start_time, activity_end_time, " +
                    "location, organizers, contact_info, registration_start_time, registration_end_time, " +
                    "max_participants, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, activity.getTitle());
            pstmt.setString(2, activity.getDescription());
            pstmt.setString(3, activity.getActivityType());
            pstmt.setTimestamp(4, activity.getActivityStartTime() != null ? new Timestamp(activity.getActivityStartTime().getTime()) : null);
            pstmt.setTimestamp(5, activity.getActivityEndTime() != null ? new Timestamp(activity.getActivityEndTime().getTime()) : null);
            pstmt.setString(6, activity.getLocation());
            pstmt.setString(7, activity.getOrganizers());
            pstmt.setString(8, activity.getContactInfo());
            pstmt.setTimestamp(9, activity.getRegistrationStartTime() != null ? new Timestamp(activity.getRegistrationStartTime().getTime()) : null);
            pstmt.setTimestamp(10, activity.getRegistrationEndTime() != null ? new Timestamp(activity.getRegistrationEndTime().getTime()) : null);
            pstmt.setInt(11, activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0);
            pstmt.setString(12, activity.getStatus() != null ? activity.getStatus() : "upcoming");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新活动
     */
    public boolean update(Activity activity) {
        String sql = "UPDATE activity SET name=?, description=?, activity_type=?, activity_start_time=?, " +
                    "activity_end_time=?, location=?, organizers=?, contact_info=?, registration_start_time=?, " +
                    "registration_end_time=?, max_participants=?, status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, activity.getTitle());
            pstmt.setString(2, activity.getDescription());
            pstmt.setString(3, activity.getActivityType());
            pstmt.setTimestamp(4, activity.getActivityStartTime() != null ? new Timestamp(activity.getActivityStartTime().getTime()) : null);
            pstmt.setTimestamp(5, activity.getActivityEndTime() != null ? new Timestamp(activity.getActivityEndTime().getTime()) : null);
            pstmt.setString(6, activity.getLocation());
            pstmt.setString(7, activity.getOrganizers());
            pstmt.setString(8, activity.getContactInfo());
            pstmt.setTimestamp(9, activity.getRegistrationStartTime() != null ? new Timestamp(activity.getRegistrationStartTime().getTime()) : null);
            pstmt.setTimestamp(10, activity.getRegistrationEndTime() != null ? new Timestamp(activity.getRegistrationEndTime().getTime()) : null);
            pstmt.setInt(11, activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0);
            pstmt.setString(12, activity.getStatus());
            pstmt.setInt(13, activity.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 删除活动（软删除，同时将状态改为已取消）
     */
    public boolean delete(Integer id) {
        String sql = "UPDATE activity SET deleted = 1, status = 'CANCELED' WHERE id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 将ResultSet映射为Activity对象
     */
    private Activity mapResultSetToActivity(ResultSet rs) throws SQLException {
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
        activity.setCreatedAt(rs.getTimestamp("created_at"));
        activity.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // 动态计算是否开放报名
        activity.setRegistrationOpen(activity.isInRegistrationPeriod());

        return activity;
    }

    /**
     * 检查指定时间段内是否存在同名活动
     * @param title 活动名称
     * @param startTime 活动开始时间
     * @param endTime 活动结束时间
     * @param excludeId 排除的活动ID（编辑时使用）
     * @return true-存在冲突的同名活动，false-不存在
     */
    public boolean existsByTitleAndTimeWindow(String title, java.util.Date startTime, java.util.Date endTime, Integer excludeId) {
        // 两段时间重叠的条件：start1 < end2 且 end1 > start2
        String sql = "SELECT COUNT(*) FROM activity WHERE name = ? AND deleted = 0 " +
                "AND activity_start_time < ? AND activity_end_time > ?";

        if (excludeId != null) {
            sql += " AND id != ?";
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setTimestamp(2, new Timestamp(endTime.getTime()));   // 新活动结束时间 > 现有活动开始时间
            pstmt.setTimestamp(3, new Timestamp(startTime.getTime())); // 新活动开始时间 < 现有活动结束时间

            if (excludeId != null) {
                pstmt.setInt(4, excludeId);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 关闭资源
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
