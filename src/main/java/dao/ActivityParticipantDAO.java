package dao;

import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityParticipantDAO {

    public boolean register(Integer activityId, Integer userId) {
        return register(activityId, userId, false);
    }

    public boolean register(Integer activityId, Integer userId, boolean autoApproved) {
        String sql = "INSERT INTO activity_participant (activity_id, user_id, status) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, autoApproved ? "approved" : "pending");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public boolean cancelRegistration(Integer activityId, Integer userId) {
        String sql = "DELETE FROM activity_participant WHERE activity_id = ? AND user_id = ?";
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

    public boolean isRegistered(Integer activityId, Integer userId) {
        return isRegistered(activityId, userId, null);
    }

    public boolean isRegistered(Integer activityId, Integer userId, String participantStatus) {
        String sql;
        if (participantStatus == null) {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND user_id = ?";
        } else {
            sql = "SELECT COUNT(*) FROM activity_participant WHERE activity_id = ? AND user_id = ? AND status = ?";
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

    public List<Integer> getParticipantIdsByActivityId(Integer activityId) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM activity_participant WHERE activity_id = ?";
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

    public int getParticipantCount(Integer activityId) {
        return getParticipantCount(activityId, "approved");
    }

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

    public boolean updateStatus(Integer activityId, Integer userId, String status) {
        String sql = "UPDATE activity_participant SET status = ? WHERE activity_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, activityId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public int batchUpdateParticipantStatus(List<Integer> userIds, Integer activityId, String participantStatus) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("UPDATE activity_participant SET status = ? WHERE activity_id = ? AND user_id IN (");
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
            pstmt.setString(1, participantStatus);
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

    public String getParticipantStatus(Integer activityId, Integer userId) {
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
                return rs.getString("participant_status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 统计待审核的活动报名数量（只统计活动未结束的）
     */
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM activity_participant ap " +
                     "JOIN activity a ON ap.activity_id = a.id " +
                     "WHERE ap.status = 'pending' AND a.registration_end_time > NOW()";
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
