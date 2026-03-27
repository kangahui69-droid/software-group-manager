package dao;

import model.ProblemReport;
import model.User;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题反馈数据访问层
 */
public class ProblemReportDAO {

    /**
     * 插入问题反馈
     */
    public int insert(ProblemReport report) {
        String sql = "INSERT INTO problem_report (title, content, reporter_name, reporter_contact, reporter_type, user_id, category, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, report.getTitle());
            pstmt.setString(2, report.getContent());
            pstmt.setString(3, report.getReporterName());
            pstmt.setString(4, report.getReporterContact());
            pstmt.setString(5, report.getReporterType());
            if (report.getUserId() != null) {
                pstmt.setInt(6, report.getUserId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, report.getCategory());
            pstmt.setString(8, report.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return -1;
    }

    /**
     * 根据ID查询
     */
    public ProblemReport findById(Integer id) {
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
            pstmt.setInt(1, id);
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

    /**
     * 查询所有问题反馈（按创建时间倒序）
     */
    public List<ProblemReport> findAll() {
        String sql = "SELECT pr.*, u.name as user_name, h.name as handler_name " +
                     "FROM problem_report pr " +
                     "LEFT JOIN user u ON pr.user_id = u.id " +
                     "LEFT JOIN user h ON pr.handled_by = h.id " +
                     "ORDER BY pr.created_at DESC";
        return findBySql(sql);
    }

    /**
     * 根据分类查询
     */
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

    /**
     * 根据状态查询（仅属实问题有状态）
     */
    public List<ProblemReport> findByStatus(String status) {
        String sql = "SELECT pr.*, u.name as user_name, h.name as handler_name " +
                     "FROM problem_report pr " +
                     "LEFT JOIN user u ON pr.user_id = u.id " +
                     "LEFT JOIN user h ON pr.handled_by = h.id " +
                     "WHERE pr.status = ? " +
                     "ORDER BY pr.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
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

    /**
     * 根据用户ID查询问题反馈（成员查看自己的反馈）
     */
    public List<ProblemReport> findByUserId(Integer userId) {
        String sql = "SELECT pr.*, u.name as user_name, h.name as handler_name " +
                     "FROM problem_report pr " +
                     "LEFT JOIN user u ON pr.user_id = u.id " +
                     "LEFT JOIN user h ON pr.handled_by = h.id " +
                     "WHERE pr.user_id = ? " +
                     "ORDER BY pr.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
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

    /**
     * 更新分类和状态
     */
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

    /**
     * 更新管理员备注
     */
    public boolean updateAdminComment(Integer id, String adminComment) {
        String sql = "UPDATE problem_report SET admin_comment = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, adminComment);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 删除问题反馈
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM problem_report WHERE id = ?";
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
     * 统计各类别数量
     */
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

    /**
     * 统计各状态数量
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM problem_report WHERE status = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
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

    private List<ProblemReport> findBySql(String sql) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
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
            if (rs != null)
                rs.close();
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}