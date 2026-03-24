package dao;

import model.AIMessage;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AIMessageDAO {

    public Integer save(AIMessage message) {
        String sql = "INSERT INTO ai_message (conversation_id, role, content, created_at) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, message.getConversationId());
            pstmt.setString(2, message.getRole());
            pstmt.setString(3, message.getContent());
            pstmt.setTimestamp(4, new Timestamp(message.getCreatedAt().getTime()));
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

    public List<AIMessage> findByConversationId(Integer conversationId) {
        String sql = "SELECT * FROM ai_message WHERE conversation_id = ? ORDER BY created_at ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIMessage> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, conversationId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public void deleteByConversationId(Integer conversationId) {
        String sql = "DELETE FROM ai_message WHERE conversation_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, conversationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    private AIMessage mapResultSetToMessage(ResultSet rs) throws SQLException {
        AIMessage m = new AIMessage();
        m.setId(rs.getInt("id"));
        m.setConversationId(rs.getInt("conversation_id"));
        m.setRole(rs.getString("role"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        return m;
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