package dao;

import model.Registration;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动报名数据访问层（新版 - 支持时间窗口报名）
 */
public class RegistrationDAO {

    /**
     * 报名活动
     */
    public boolean register(Integer activityId, Integer userId) {
        String sql = "INSERT INTO activity_participant (activity_id, user_id, status, created_at) " +
                    "VALUES (?, ?, 'pending', NOW())";
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
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 报名活动（使用外部传入的连接，支持事务）
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param conn 数据库连接（外部传入，不自动关闭）
     * @return 是否报名成功
     */
    public boolean register(Integer activityId, Integer userId, Connection conn) {
        String sql = "INSERT INTO activity_participant (activity_id, user_id, status, created_at) " +
                    "VALUES (?, ?, 'pending', NOW())";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("报名失败: " + e.getMessage(), e);
        } finally {
            closePstmt(pstmt);
        }
    }

    /**
     * 取消报名
     */
    public boolean cancelRegistration(Integer activityId, Integer userId) {
        String sql = "DELETE FROM activity_participant WHERE activity_id = ? AND user_id = ? AND status = 'pending'";
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
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 检查用户是否已报名某活动
     */
    public boolean isRegistered(Integer activityId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND user_id = ? AND deleted = 0";
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
     * 统计某活动的报名人数
     */
    public int countByActivityId(Integer activityId) {
        String sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
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

    /**
     * 获取用户的所有报名记录（页面B）
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
     * 获取活动的所有已报名成员（状态不是rejected和cancelled的）
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
                    "WHERE ap.activity_id = ? AND ap.status NOT IN ('rejected', 'cancelled') " +
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
                    "WHERE ap.activity_id = ? AND ap.status = ? " +
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

    /**
     * 更新报名状态
     */
    public boolean updateStatus(Integer activityId, Integer userId, String status, String notes) {
        String sql = "UPDATE activity_participant SET status = ?, notes = ?, updated_at = NOW() " +
                    "WHERE activity_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, notes);
            pstmt.setInt(3, activityId);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新报名状态（使用外部传入的连接，支持事务）
     */
    public boolean updateStatus(Integer activityId, Integer userId, String status, String notes, Connection conn) {
        String sql = "UPDATE activity_participant SET status = ?, notes = ?, updated_at = NOW() " +
                    "WHERE activity_id = ? AND user_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, notes);
            pstmt.setInt(3, activityId);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新报名状态失败: " + e.getMessage(), e);
        } finally {
            closePstmt(pstmt);
        }
    }

    /**
     * 批量更新报名状态
     */
    public int batchUpdateStatus(List<Integer> userIds, Integer activityId, String status) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE activity_participant SET status = ?, updated_at = NOW() " +
                                             "WHERE activity_id = ? AND user_id IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sql.append("?");
            if (i < userIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, status);
            pstmt.setInt(2, activityId);
            for (int i = 0; i < userIds.size(); i++) {
                pstmt.setInt(3 + i, userIds.get(i));
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return 0;
    }

    /**
     * 批量更新报名状态（使用外部传入的连接，支持事务）
     */
    public int batchUpdateStatus(List<Integer> userIds, Integer activityId, String status, Connection conn) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE activity_participant SET status = ?, updated_at = NOW() " +
                                             "WHERE activity_id = ? AND user_id IN (");
        for (int i = 0; i < userIds.size(); i++) {
            sql.append("?");
            if (i < userIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, status);
            pstmt.setInt(2, activityId);
            for (int i = 0; i < userIds.size(); i++) {
                pstmt.setInt(3 + i, userIds.get(i));
            }
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("批量更新报名状态失败: " + e.getMessage(), e);
        } finally {
            closePstmt(pstmt);
        }
    }

    /**
     * 获取活动的报名人数
     */
    public int getParticipantCount(Integer activityId) {
        return getParticipantCount(activityId, null);
    }

    /**
     * 获取活动的报名人数（按状态筛选）
     */
    public int getParticipantCount(Integer activityId, String status) {
        String sql;
        if (status == null) {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ?";
        } else {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND status = ?";
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
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
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return 0;
    }

    /**
     * 获取活动的报名人数（按状态筛选，使用外部传入的连接，支持事务）
     * @param activityId 活动ID
     * @param status 状态（null表示全部，pending/confirmed/rejected等）
     * @param conn 数据库连接
     * @return 报名人数
     */
    public int getParticipantCount(Integer activityId, String status, Connection conn) {
        String sql;
        if (status == null) {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ?";
        } else {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND status = ?";
        }
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
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
            throw new RuntimeException("获取报名人数失败: " + e.getMessage(), e);
        } finally {
            closeRs(rs);
            closePstmt(pstmt);
        }
        return 0;
    }

    /**
     * 获取用户在某活动中的报名状态
     */
    public String getRegistrationStatus(Integer activityId, Integer userId) {
        String sql = "SELECT status FROM activity_participant WHERE activity_id = ? AND user_id = ?";
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
     * 获取活动已确认的报名数量
     */
    public int getConfirmedCount(Integer activityId) {
        String sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND status = 'confirmed'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
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
     * 删除活动的所有报名记录（软删除，使用外部传入的连接，支持事务）
     * @param activityId 活动ID
     * @param conn 数据库连接
     * @return 删除的记录数
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
            closePstmt(pstmt);
        }
    }

    /**
     * 关闭ResultSet（不关闭连接，由调用方管理）
     */
    private void closeRs(ResultSet rs) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭PreparedStatement（不关闭连接，由调用方管理）
     */
    private void closePstmt(PreparedStatement pstmt) {
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
