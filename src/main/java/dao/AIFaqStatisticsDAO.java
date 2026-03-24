package dao;

import model.AIFaqStatistics;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AIFaqStatisticsDAO {

    public Integer save(AIFaqStatistics stats) {
        String sql = "INSERT INTO ai_faq_statistics (question_hash, normalized_question, query_count, avg_rating, last_query_at, suggested_faq, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, stats.getQuestionHash());
            pstmt.setString(2, stats.getNormalizedQuestion());
            pstmt.setInt(3, stats.getQueryCount() != null ? stats.getQueryCount() : 1);
            pstmt.setDouble(4, stats.getAvgRating() != null ? stats.getAvgRating() : 0.0);
            pstmt.setTimestamp(5, stats.getLastQueryAt() != null ? new Timestamp(stats.getLastQueryAt().getTime()) : new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(6, stats.getSuggestedFaq() != null ? stats.getSuggestedFaq() : 0);
            pstmt.setTimestamp(7, stats.getCreatedAt() != null ? new Timestamp(stats.getCreatedAt().getTime()) : new Timestamp(System.currentTimeMillis()));
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

    public AIFaqStatistics findByHash(String hash) {
        String sql = "SELECT * FROM ai_faq_statistics WHERE question_hash = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hash);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToStats(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public List<AIFaqStatistics> findTopQuestions(int limit) {
        String sql = "SELECT * FROM ai_faq_statistics ORDER BY query_count DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIFaqStatistics> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToStats(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public List<AIFaqStatistics> findAllOrderByCount() {
        String sql = "SELECT * FROM ai_faq_statistics ORDER BY query_count DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AIFaqStatistics> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToStats(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return list;
    }

    public void update(AIFaqStatistics stats) {
        String sql = "UPDATE ai_faq_statistics SET query_count = ?, last_query_at = ?, suggested_faq = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, stats.getQueryCount());
            pstmt.setTimestamp(2, stats.getLastQueryAt() != null ? new Timestamp(stats.getLastQueryAt().getTime()) : new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(3, stats.getSuggestedFaq() != null ? stats.getSuggestedFaq() : 0);
            pstmt.setInt(4, stats.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    public void delete(Integer id) {
        String sql = "DELETE FROM ai_faq_statistics WHERE id = ?";
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

    private AIFaqStatistics mapResultSetToStats(ResultSet rs) throws SQLException {
        AIFaqStatistics stats = new AIFaqStatistics();
        stats.setId(rs.getInt("id"));
        stats.setQuestionHash(rs.getString("question_hash"));
        stats.setNormalizedQuestion(rs.getString("normalized_question"));
        
        int queryCount = rs.getInt("query_count");
        stats.setQueryCount(rs.wasNull() ? 0 : queryCount);
        
        double avgRating = rs.getDouble("avg_rating");
        stats.setAvgRating(rs.wasNull() ? 0.0 : avgRating);
        
        stats.setLastQueryAt(rs.getTimestamp("last_query_at"));
        stats.setSuggestedFaq(rs.getInt("suggested_faq"));
        stats.setCreatedAt(rs.getTimestamp("created_at"));
        return stats;
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