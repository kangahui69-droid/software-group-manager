package dao;

import model.AdminProfile;
import util.DBUtil;

import java.sql.*;

/**
 * 管理员档案数据访问层
 */
public class AdminProfileDAO {

    /**
     * 根据用户ID查询档案
     */
    public AdminProfile findByUserId(Integer userId) {
        String sql = "SELECT * FROM admin_profile WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProfile(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 添加或更新档案
     */
    public boolean saveOrUpdate(AdminProfile profile) {
        AdminProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            return update(profile);
        } else {
            return insert(profile);
        }
    }

    /**
     * 添加档案
     */
    public boolean insert(AdminProfile profile) {
        String sql = "INSERT INTO admin_profile (user_id, title, department, education, research_area, bio, avatar_file_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, profile.getUserId());
            pstmt.setString(2, profile.getTitle());
            pstmt.setString(3, profile.getDepartment());
            pstmt.setString(4, profile.getEducation());
            pstmt.setString(5, profile.getResearchArea());
            pstmt.setString(6, profile.getBio());
            pstmt.setObject(7, profile.getAvatarFileId());
            pstmt.setInt(8, profile.getStatus() != null ? profile.getStatus() : 1);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新档案
     */
    public boolean update(AdminProfile profile) {
        String sql = "UPDATE admin_profile SET title = ?, department = ?, education = ?, research_area = ?, bio = ?, avatar_file_id = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, profile.getTitle());
            pstmt.setString(2, profile.getDepartment());
            pstmt.setString(3, profile.getEducation());
            pstmt.setString(4, profile.getResearchArea());
            pstmt.setString(5, profile.getBio());
            pstmt.setObject(6, profile.getAvatarFileId());
            pstmt.setInt(7, profile.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 将ResultSet映射为AdminProfile对象
     */
    private AdminProfile mapResultSetToProfile(ResultSet rs) throws SQLException {
        AdminProfile profile = new AdminProfile();
        profile.setId(rs.getInt("id"));
        profile.setUserId(rs.getInt("user_id"));
        profile.setTitle(rs.getString("title"));
        profile.setDepartment(rs.getString("department"));
        profile.setEducation(rs.getString("education"));
        profile.setResearchArea(rs.getString("research_area"));
        profile.setBio(rs.getString("bio"));
        profile.setAvatarFileId((Integer) rs.getObject("avatar_file_id"));
        profile.setStatus(rs.getInt("status"));
        profile.setCreatedAt(rs.getTimestamp("created_at"));
        profile.setUpdatedAt(rs.getTimestamp("updated_at"));
        return profile;
    }

    /**
     * 关闭资源
     */
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