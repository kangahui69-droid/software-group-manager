package dao;

import model.AIConversation;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AIConversationDAO {

    public Integer save(AIConversation conversation) {
        String sql = "INSERT INTO ai_conversation (user_id, session_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, conversation.getUserId() != null ? conversation.getUserId() : 0);
            pstmt.setString(2, conversation.getSessionId());
            pstmt.setTimestamp(3, new Timestamp(conversation.getCreatedAt().getTime()));
            pstmt.setTimestamp(4, new Timestamp(conversation.getUpdatedAt().getTime()));
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public AIConversation findBySessionId(String sessionId) {
        String sql = "SELECT * FROM ai_conversation WHERE session_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sessionId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToConversation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public List<AIConversation> findByUserId(Integer userId) {
        String sql = "SELECT * FROM ai_conversation WHERE user_id = ? ORDER BY updated_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIConversation> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public void update(AIConversation conversation) {
        String sql = "UPDATE ai_conversation SET updated_at = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, new Timestamp(conversation.getUpdatedAt().getTime()));
            pstmt.setInt(2, conversation.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM ai_conversation WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    private AIConversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        AIConversation c = new AIConversation();
        c.setId(rs.getInt("id"));
        c.setUserId(rs.getInt("user_id"));
        c.setSessionId(rs.getString("session_id"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        return c;
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (pstmt != null) {
            try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}