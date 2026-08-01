package dao;

import model.AIMessageStatus;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AI消息状态数据访问层
 */
public class AIMessageStatusDAO {

    /**
     * 插入消息状态记录
     */
    public boolean insert(AIMessageStatus status) {
        String sql = "INSERT INTO ai_message_status (message_id, user_id, session_id, user_message, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, status.getMessageId());
            if (status.getUserId() != null) {
                pstmt.setInt(2, status.getUserId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, status.getSessionId());
            pstmt.setString(4, status.getUserMessage());
            pstmt.setString(5, status.getStatus());

            boolean result = pstmt.executeUpdate() > 0;
            if (result) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    status.setId(rs.getInt(1));
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新消息状态和响应
     */
    public boolean updateStatus(String messageId, String status, String aiResponse, String errorMessage) {
        String sql = "UPDATE ai_message_status SET status = ?, ai_response = ?, error_message = ?, updated_at = NOW()";
        if (AIMessageStatus.STATUS_COMPLETED.equals(status) || AIMessageStatus.STATUS_FAILED.equals(status)) {
            sql += ", completed_at = NOW()";
        }
        sql += " WHERE message_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, aiResponse);
            pstmt.setString(3, errorMessage);
            pstmt.setString(4, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据 messageId 查询
     */
    public AIMessageStatus findByMessageId(String messageId) {
        String sql = "SELECT * FROM ai_message_status WHERE message_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, messageId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStatus(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询用户的消息状态列表
     */
    public List<AIMessageStatus> findByUserId(Integer userId, int limit) {
        String sql = "SELECT * FROM ai_message_status WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<AIMessageStatus> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToStatus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 查询会话的消息状态列表
     */
    public List<AIMessageStatus> findBySessionId(String sessionId, int limit) {
        String sql = "SELECT * FROM ai_message_status WHERE session_id = ? ORDER BY created_at DESC LIMIT ?";
        List<AIMessageStatus> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sessionId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToStatus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除指定时间之前的记录
     */
    public int deleteBefore(Date date) {
        String sql = "DELETE FROM ai_message_status WHERE created_at < ? AND status IN (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, new Timestamp(date.getTime()));
            pstmt.setString(2, AIMessageStatus.STATUS_COMPLETED);
            pstmt.setString(3, AIMessageStatus.STATUS_FAILED);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return 0;
    }

    private AIMessageStatus mapResultSetToStatus(ResultSet rs) throws SQLException {
        AIMessageStatus status = new AIMessageStatus();
        status.setId(rs.getInt("id"));
        status.setMessageId(rs.getString("message_id"));
        Integer userId = rs.getInt("user_id");
        status.setUserId(rs.wasNull() ? null : userId);
        status.setSessionId(rs.getString("session_id"));
        status.setUserMessage(rs.getString("user_message"));
        status.setAiResponse(rs.getString("ai_response"));
        status.setStatus(rs.getString("status"));
        status.setErrorMessage(rs.getString("error_message"));
        status.setCreatedAt(rs.getTimestamp("created_at"));
        status.setUpdatedAt(rs.getTimestamp("updated_at"));
        status.setCompletedAt(rs.getTimestamp("completed_at"));
        return status;
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
