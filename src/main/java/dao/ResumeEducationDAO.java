package dao;

import model.ResumeEducation;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 简历-教育经历数据访问对象
 * 对应数据库表: resume_educations
 */
public class ResumeEducationDAO {

    /**
     * 保存教育经历
     * @param education 教育经历对象
     * @return 是否成功
     */
    public boolean save(ResumeEducation education) {
        String sql = "INSERT INTO resume_educations (resume_id, school_name, major, degree, start_date, " +
                "end_date, is_current, description, display_order, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, education.getResumeId());
            pstmt.setString(2, education.getSchoolName());
            pstmt.setString(3, education.getMajor());
            pstmt.setString(4, education.getDegree());
            pstmt.setDate(5, education.getStartDate());
            pstmt.setDate(6, education.getEndDate());
            pstmt.setInt(7, education.getIsCurrent() != null ? education.getIsCurrent() : 0);
            pstmt.setString(8, education.getDescription());
            pstmt.setInt(9, education.getDisplayOrder() != null ? education.getDisplayOrder() : 0);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    education.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ResumeEducationDAO.save] 保存教育经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新教育经历
     * @param education 教育经历对象
     * @return 是否成功
     */
    public boolean update(ResumeEducation education) {
        String sql = "UPDATE resume_educations SET school_name = ?, major = ?, degree = ?, start_date = ?, " +
                "end_date = ?, is_current = ?, description = ?, display_order = ?, updated_at = NOW() " +
                "WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, education.getSchoolName());
            pstmt.setString(2, education.getMajor());
            pstmt.setString(3, education.getDegree());
            pstmt.setDate(4, education.getStartDate());
            pstmt.setDate(5, education.getEndDate());
            pstmt.setInt(6, education.getIsCurrent() != null ? education.getIsCurrent() : 0);
            pstmt.setString(7, education.getDescription());
            pstmt.setInt(8, education.getDisplayOrder() != null ? education.getDisplayOrder() : 0);
            pstmt.setInt(9, education.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeEducationDAO.update] 更新教育经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据ID查询教育经历
     * @param id 教育经历ID
     * @return 教育经历对象
     */
    public ResumeEducation findById(Integer id) {
        String sql = "SELECT * FROM resume_educations WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ResumeEducationDAO.findById] 查询教育经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询简历的所有教育经历
     * @param resumeId 简历ID
     * @return 教育经历列表
     */
    public List<ResumeEducation> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_educations WHERE resume_id = ? ORDER BY display_order ASC, end_date DESC";
        List<ResumeEducation> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, resumeId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ResumeEducationDAO.findByResumeId] 查询教育经历列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除教育经历
     * @param id 教育经历ID
     * @return 是否成功
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_educations WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeEducationDAO.delete] 删除教育经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * ResultSet映射为实体
     */
    private ResumeEducation mapResultSetToEntity(ResultSet rs) throws SQLException {
        ResumeEducation education = new ResumeEducation();
        education.setId(rs.getInt("id"));
        education.setResumeId(rs.getInt("resume_id"));
        education.setSchoolName(rs.getString("school_name"));
        education.setMajor(rs.getString("major"));
        education.setDegree(rs.getString("degree"));
        education.setStartDate(rs.getDate("start_date"));
        education.setEndDate(rs.getDate("end_date"));

        // 修复：正确处理 is_current 可能为 NULL 的情况
        int isCurrent = rs.getInt("is_current");
        if (rs.wasNull()) {
            education.setIsCurrent(null);
        } else {
            education.setIsCurrent(isCurrent);
        }

        education.setDescription(rs.getString("description"));
        education.setDisplayOrder(rs.getInt("display_order"));
        education.setCreatedAt(rs.getTimestamp("created_at"));
        education.setUpdatedAt(rs.getTimestamp("updated_at"));
        return education;
    }
}
