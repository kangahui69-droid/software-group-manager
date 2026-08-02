package dao;

import model.Registration;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动参与者数据访问层（合并版 - 整合了RegistrationDAO的功能）
 *
 * 合并说明：
 * - 原ActivityParticipantDAO：基础CRUD操作
 * - 原RegistrationDAO：报名审核流程、多条件查询
 * - 两者操作同一张表activity_participant，功能重叠约60%
 *
 * 本DAO保留所有功能，统一命名规范。
 */
public class ActivityParticipantDAO {

    // ==================== 基础报名操作 ====================

    /**
     * 报名活动（默认pending状态，有创建时间）
     */
    public boolean register(Integer activityId, Integer userId) {
        return register(activityId, userId, false);
    }

    /**
     * 报名活动（可指定是否自动审批）
     */
    public boolean register(Integer activityId, Integer userId, boolean autoApproved) {
        try (Connection conn = DBUtil.getConnection()) {
            return register(activityId, userId, autoApproved, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        }
    }

    /**
     * 报名活动（支持事务）
     */
    public boolean register(Integer activityId, Integer userId, boolean autoApproved, Connection conn) {
        String sql = "INSERT INTO activity_participant (activity_id, user_id, status, created_at) VALUES (?, ?, ?, NOW())";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, autoApproved ? "confirmed" : "pending");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("Duplicate") || msg.contains("Unique index") || msg.contains("primary key"))) {
                return false;
            }
            e.printStackTrace();
            throw new RuntimeException("报名失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 报名活动（默认autoApproved=false）
     */
    public boolean register(Integer activityId, Integer userId, Connection conn) {
        return register(activityId, userId, false, conn);
    }

    /**
     * 取消报名（只取消pending状态的报名）
     */
    public boolean cancelRegistration(Integer activityId, Integer userId) {
        try (Connection conn = DBUtil.getConnection()) {
            return cancelRegistration(activityId, userId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        }
    }

    /**
     * 取消报名（只取消pending状态的报名，支持事务）
     */
    public boolean cancelRegistration(Integer activityId, Integer userId, Connection conn) {
        String sql = "DELETE FROM activity_participant WHERE activity_id = ? AND user_id = ? AND status = 'pending'";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("取消报名失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 检查用户是否已报名某活动
     */
    public boolean isRegistered(Integer activityId, Integer userId) {
        return isRegistered(activityId, userId, null);
    }

    /**
     * 检查用户是否已报名某活动（可指定状态）
     */
    public boolean isRegistered(Integer activityId, Integer userId, String participantStatus) {
        String sql;
        if (participantStatus == null) {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND user_id = ? AND deleted = 0";
        } else {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND user_id = ? AND status = ? AND deleted = 0";
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            if (participantStatus != null) {
                pstmt.setString(3, participantStatus);
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
     * 获取活动的参与者ID列表
     */
    public List<Integer> getParticipantIdsByActivityId(Integer activityId) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM activity_participant WHERE activity_id = ? AND deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return userIds;
    }

    // ==================== 统计操作 ====================

    /**
     * 获取活动的报名人数（默认统计confirmed状态）
     */
    public int getParticipantCount(Integer activityId) {
        return getParticipantCount(activityId, "confirmed");
    }

    /**
     * 获取活动的报名人数（按状态筛选）
     */
    public int getParticipantCount(Integer activityId, String status) {
        return getParticipantCount(activityId, status, null);
    }

    /**
     * 获取活动的报名人数（按状态筛选，支持事务）
     */
    public int getParticipantCount(Integer activityId, String status, Connection conn) {
        String sql;
        if (status == null) {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND deleted = 0";
        } else {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND status = ? AND deleted = 0";
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean ownConnection = (conn == null);
        try {
            if (ownConnection) {
                conn = DBUtil.getConnection();
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            if (status != null) {
                pstmt.setString(2, status);
            }
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                throw new RuntimeException("获取报名人数失败: " + e.getMessage(), e);
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (ownConnection && conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 统计某活动的报名人数（无状态筛选，等同于getParticipantCount(activityId, null)）
     */
    public int countByActivityId(Integer activityId) {
        return getParticipantCount(activityId, null);
    }

    /**
     * 获取活动已确认的报名数量
     */
    public int getConfirmedCount(Integer activityId) {
        return getParticipantCount(activityId, "confirmed");
    }

    /**
     * 统计待审核的活动报名数量（只统计活动未结束的）
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM activity_participant ap " +
                     "JOIN activity a ON ap.activity_id = a.id " +
                     "WHERE ap.status = 'pending' AND a.registration_end_time > NOW() AND ap.deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return 0;
    }

    // ==================== 状态更新操作 ====================

    /**
     * 更新报名状态
     */
    public boolean updateStatus(Integer activityId, Integer userId, String status) {
        return updateStatus(activityId, userId, status, null);
    }

    /**
     * 更新报名状态（支持备注）
     */
    public boolean updateStatus(Integer activityId, Integer userId, String status, String notes) {
        return updateStatus(activityId, userId, status, notes, null);
    }

    /**
     * 更新报名状态（支持事务）
     */
    public boolean updateStatus(Integer activityId, Integer userId, String status, String notes, Connection conn) {
        String sql = notes != null
            ? "UPDATE activity_participant SET status = ?, notes = ?, updated_at = NOW() WHERE activity_id = ? AND user_id = ?"
            : "UPDATE activity_participant SET status = ?, updated_at = NOW() WHERE activity_id = ? AND user_id = ?";
        PreparedStatement pstmt = null;
        boolean ownConnection = (conn == null);
        try {
            if (ownConnection) {
                conn = DBUtil.getConnection();
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            if (notes != null) {
                pstmt.setString(2, notes);
                pstmt.setInt(3, activityId);
                pstmt.setInt(4, userId);
            } else {
                pstmt.setInt(2, activityId);
                pstmt.setInt(3, userId);
            }
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("更新报名状态失败：用户未报名该活动");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新报名状态失败", e);
        } finally {
            closeResources(ownConnection ? null : null, pstmt, null);
            if (ownConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 批量更新报名状态
     */
    public int batchUpdateStatus(List<Integer> userIds, Integer activityId, String status) {
        return batchUpdateStatus(userIds, activityId, status, null);
    }

    /**
     * 批量更新报名状态（支持事务）
     */
    public int batchUpdateStatus(List<Integer> userIds, Integer activityId, String status, Connection conn) {
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

        PreparedStatement pstmt = null;
        boolean ownConnection = (conn == null);
        try {
            if (ownConnection) {
                conn = DBUtil.getConnection();
            }
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, status);
            pstmt.setInt(2, activityId);
            for (int i = 0; i < userIds.size(); i++) {
                pstmt.setInt(3 + i, userIds.get(i));
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("批量更新报名状态失败", e);
        } finally {
            closeResources(ownConnection ? null : null, pstmt, null);
            if (ownConnection && conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取用户在某活动中的报名状态
     */
    public String getRegistrationStatus(Integer activityId, Integer userId) {
        return getParticipantStatus(activityId, userId);
    }

    /**
     * 获取用户在某活动中的报名状态（兼容旧名称）
     */
    public String getParticipantStatus(Integer activityId, Integer userId) {
        String sql = "SELECT status FROM activity_participant WHERE activity_id = ? AND user_id = ? AND deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    // ==================== 查询操作（来自原RegistrationDAO） ====================

    /**
     * 获取用户的所有报名记录（包含活动详情）
     */
    public List<Registration> findByUserId(Integer userId) {
        List<Registration> registrations = new ArrayList<>();
        String sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                    "a.location, a.registration_end_time, a.status as activity_status " +
                    "FROM activity_participant ap " +
                    "JOIN activity a ON ap.activity_id = a.id " +
                    "WHERE ap.user_id = ? AND a.deleted = 0 AND ap.deleted = 0 " +
                    "ORDER BY ap.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return registrations;
    }

    /**
     * 获取活动的所有已报名成员（状态不是rejected和cancelled的，包含用户详情）
     */
    public List<Registration> findByActivityId(Integer activityId) {
        List<Registration> registrations = new ArrayList<>();
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return registrations;
    }

    /**
     * 获取活动的指定状态的报名记录
     */
    public List<Registration> findByActivityIdAndStatus(Integer activityId, String status) {
        List<Registration> registrations = new ArrayList<>();
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
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setString(2, status);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return registrations;
    }

    /**
     * 根据状态和报名时间筛选报名记录
     */
    public List<Registration> findByUserIdAndStatus(Integer userId, String status) {
        List<Registration> registrations = new ArrayList<>();
        String sql;
        List<Object> params = new ArrayList<>();

        if ("expired".equals(status)) {
            // 查询已过期的pending报名（当前时间超过报名截止时间）
            sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                  "a.location, a.registration_end_time, a.status as activity_status " +
                  "FROM activity_participant ap " +
                  "JOIN activity a ON ap.activity_id = a.id " +
                  "WHERE ap.user_id = ? AND ap.status = 'pending' AND a.registration_end_time < NOW() AND a.deleted = 0 AND ap.deleted = 0 " +
                  "ORDER BY ap.created_at DESC";
        } else if ("activityEnded".equals(status)) {
            // 查询活动已结束/已取消/进行中的报名
            sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                  "a.location, a.registration_end_time, a.status as activity_status " +
                  "FROM activity_participant ap " +
                  "JOIN activity a ON ap.activity_id = a.id " +
                  "WHERE ap.user_id = ? AND (a.status IN ('completed', 'canceled', 'ongoing') OR (ap.status = 'pending' AND a.registration_end_time < NOW())) AND a.deleted = 0 AND ap.deleted = 0 " +
                  "ORDER BY ap.created_at DESC";
        } else {
            sql = "SELECT ap.*, a.name as activity_name, a.activity_start_time, a.activity_end_time, " +
                  "a.location, a.registration_end_time, a.status as activity_status " +
                  "FROM activity_participant ap " +
                  "JOIN activity a ON ap.activity_id = a.id " +
                  "WHERE ap.user_id = ? AND ap.status = ? AND a.deleted = 0 AND ap.deleted = 0 " +
                  "ORDER BY ap.created_at DESC";
            params.add(status);
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 2, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                registrations.add(mapResultSetToRegistration(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return registrations;
    }

    // ==================== 软删除操作 ====================

    /**
     * 删除报名记录（软删除）
     */
    public boolean delete(Integer activityId, Integer userId) {
        String sql = "UPDATE activity_participant SET deleted = 1 WHERE activity_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    /**
     * 删除活动的所有报名记录（软删除）
     */
    public int deleteByActivityId(Integer activityId) {
        String sql = "UPDATE activity_participant SET deleted = 1 WHERE activity_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    /**
     * 删除活动的所有报名记录（软删除，支持事务）
     */
    public int deleteByActivityId(Integer activityId, Connection conn) {
        String sql = "UPDATE activity_participant SET deleted = 1 WHERE activity_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("删除活动报名记录失败: " + e.getMessage(), e);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ==================== 结果集映射 ====================

    /**
     * 将ResultSet映射为Registration对象
     */
    private Registration mapResultSetToRegistration(ResultSet rs) throws SQLException {
        Registration registration = new Registration();
        registration.setActivityId(rs.getInt("activity_id"));
        registration.setUserId(rs.getInt("user_id"));
        registration.setStatus(rs.getString("status"));
        registration.setCreatedAt(rs.getTimestamp("created_at"));
        registration.setUpdatedAt(rs.getTimestamp("updated_at"));
        registration.setNotes(rs.getString("notes"));

        // 关联活动信息
        if (hasColumn(rs, "activity_name")) {
            registration.setActivityName(rs.getString("activity_name"));

            Timestamp activityStartTime = rs.getTimestamp("activity_start_time");
            registration.setActivityStartTime(activityStartTime != null ? new java.util.Date(activityStartTime.getTime()) : null);

            Timestamp activityEndTime = rs.getTimestamp("activity_end_time");
            registration.setActivityEndTime(activityEndTime != null ? new java.util.Date(activityEndTime.getTime()) : null);

            registration.setLocation(rs.getString("location"));

            Timestamp registrationEndTime = rs.getTimestamp("registration_end_time");
            registration.setRegistrationEndTime(registrationEndTime != null ? new java.util.Date(registrationEndTime.getTime()) : null);

            if (hasColumn(rs, "activity_status")) {
                registration.setActivityStatus(rs.getString("activity_status"));
            }
        }

        // 关联用户信息
        if (hasColumn(rs, "user_name")) {
            registration.setUserName(rs.getString("user_name"));
        }
        if (hasColumn(rs, "user_email")) {
            // 可在Registration模型中添加email字段，如需显示邮箱可在此扩展
        }
        if (hasColumn(rs, "user_phone")) {
            // 可在Registration模型中添加phone字段，如需显示手机号可在此扩展
        }
        if (hasColumn(rs, "student_id")) {
            registration.setStudentId(rs.getString("student_id"));
        }
        if (hasColumn(rs, "major")) {
            registration.setMajor(rs.getString("major"));
        }
        if (hasColumn(rs, "grade_class")) {
            registration.setGrade(rs.getString("grade_class"));
        }

        return registration;
    }

    /**
     * 检查ResultSet是否包含指定列
     */
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            if (metaData.getColumnLabel(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 资源关闭 ====================

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
