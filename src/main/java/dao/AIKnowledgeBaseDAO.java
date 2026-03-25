package dao;

import model.AIKnowledgeBase;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AIKnowledgeBaseDAO {

    public Integer save(AIKnowledgeBase kb) {
        String sql = "INSERT INTO ai_knowledge_base (category, question, answer, keywords, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, kb.getCategory());
            pstmt.setString(2, kb.getQuestion());
            pstmt.setString(3, kb.getAnswer());
            pstmt.setString(4, kb.getKeywords());
            pstmt.setInt(5, kb.getStatus() != null ? kb.getStatus() : 1);
            pstmt.setTimestamp(6, new Timestamp(kb.getCreatedAt().getTime()));
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

    public List<AIKnowledgeBase> findAll() {
        String sql = "SELECT * FROM ai_knowledge_base WHERE status = 1 ORDER BY category, id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIKnowledgeBase> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToKB(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public List<AIKnowledgeBase> findByCategory(String category) {
        String sql = "SELECT * FROM ai_knowledge_base WHERE category = ? AND status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIKnowledgeBase> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToKB(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public List<AIKnowledgeBase> searchByKeyword(String keyword) {
        String sql = "SELECT * FROM ai_knowledge_base WHERE keywords LIKE ? AND status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIKnowledgeBase> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToKB(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public void update(AIKnowledgeBase kb) {
        String sql = "UPDATE ai_knowledge_base SET category = ?, question = ?, answer = ?, keywords = ?, status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, kb.getCategory());
            pstmt.setString(2, kb.getQuestion());
            pstmt.setString(3, kb.getAnswer());
            pstmt.setString(4, kb.getKeywords());
            pstmt.setInt(5, kb.getStatus());
            pstmt.setInt(6, kb.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    public void delete(Integer id) {
        String sql = "UPDATE ai_knowledge_base SET status = 0 WHERE id = ?";
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

    private AIKnowledgeBase mapResultSetToKB(ResultSet rs) throws SQLException {
        AIKnowledgeBase kb = new AIKnowledgeBase();
        kb.setId(rs.getInt("id"));
        kb.setCategory(rs.getString("category"));
        kb.setQuestion(rs.getString("question"));
        kb.setAnswer(rs.getString("answer"));
        kb.setKeywords(rs.getString("keywords"));
        kb.setStatus(rs.getInt("status"));
        kb.setCreatedAt(rs.getTimestamp("created_at"));
        return kb;
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