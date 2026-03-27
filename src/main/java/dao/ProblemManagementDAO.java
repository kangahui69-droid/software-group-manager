package dao;

import model.ProblemReport;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProblemManagementDAO {

    public List<ProblemReport> findByCategory(String category) {
        String sql = "SELECT pr.*, u.name as user_name, h.name as handler_name " +
                     "FROM problem_report pr " +
                     "LEFT JOIN user u ON pr.user_id = u.id " +
                     "LEFT JOIN user h ON pr.handled_by = h.id " +
                     "WHERE pr.category = ? " +
                     "ORDER BY pr.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            rs = pstmt.executeQuery();
            List<ProblemReport> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSetToProblemReport(rs));
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return new ArrayList<>();
    }

    public ProblemReport findByReportId(Integer reportId) {
        String sql = "SELECT pr.*, u.name as user_name, h.name as handler_name " +
                     "FROM problem_report pr " +
                     "LEFT JOIN user u ON pr.user_id = u.id " +
                     "LEFT JOIN user h ON pr.handled_by = h.id " +
                     "WHERE pr.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reportId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProblemReport(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public boolean updateCategoryAndStatus(Integer id, String category, String status, String adminComment, Integer handledBy) {
        String sql = "UPDATE problem_report SET category = ?, status = ?, admin_comment = ?, handled_by = ?, handled_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            pstmt.setString(2, status);
            pstmt.setString(3, adminComment);
            if (handledBy != null) {
                pstmt.setInt(4, handledBy);
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public int countByCategory(String category) {
        String sql = "SELECT COUNT(*) FROM problem_report WHERE category = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
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

    private ProblemReport mapResultSetToProblemReport(ResultSet rs) throws SQLException {
        ProblemReport report = new ProblemReport();
        report.setId(rs.getInt("id"));
        report.setTitle(rs.getString("title"));
        report.setContent(rs.getString("content"));
        report.setReporterName(rs.getString("reporter_name"));
        report.setReporterContact(rs.getString("reporter_contact"));
        report.setReporterType(rs.getString("reporter_type"));
        
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            report.setUserId(userId);
        }
        
        report.setCategory(rs.getString("category"));
        report.setStatus(rs.getString("status"));
        report.setAdminComment(rs.getString("admin_comment"));
        
        int handledBy = rs.getInt("handled_by");
        if (!rs.wasNull()) {
            report.setHandledBy(handledBy);
        }
        
        report.setHandledAt(rs.getTimestamp("handled_at"));
        report.setCreatedAt(rs.getTimestamp("created_at"));
        report.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        report.setUserName(rs.getString("user_name"));
        report.setHandlerName(rs.getString("handler_name"));
        
        return report;
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
