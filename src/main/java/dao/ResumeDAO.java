package dao;

import model.Resume;
import model.User;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 简历数据访问对象
 * 对应数据库表: resumes
 */
public class ResumeDAO {

    /**
     * 保存简历
     * @param resume 简历对象
     * @return 是否成功
     */
    public boolean save(Resume resume) {
        String sql = "INSERT INTO resumes (user_id, resume_name, template_style, summary, career_objective, " +
                "phone, email, wechat, github_url, blog_url, photo_url, is_default, status, view_count, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, resume.getUserId());
            pstmt.setString(2, resume.getResumeName());
            pstmt.setString(3, resume.getTemplateStyle());
            pstmt.setString(4, resume.getSummary());
            pstmt.setString(5, resume.getCareerObjective());
            pstmt.setString(6, resume.getPhone());
            pstmt.setString(7, resume.getEmail());
            pstmt.setString(8, resume.getWechat());
            pstmt.setString(9, resume.getGithubUrl());
            pstmt.setString(10, resume.getBlogUrl());
            pstmt.setString(11, resume.getPhotoUrl());
            pstmt.setInt(12, resume.getIsDefault() != null ? resume.getIsDefault() : 0);
            pstmt.setInt(13, resume.getStatus() != null ? resume.getStatus() : 1);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    resume.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.save] 保存简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新简历
     * @param resume 简历对象
     * @return 是否成功
     */
    public boolean update(Resume resume) {
        String sql = "UPDATE resumes SET resume_name = ?, template_style = ?, summary = ?, career_objective = ?, " +
                "phone = ?, email = ?, wechat = ?, github_url = ?, blog_url = ?, photo_url = ?, " +
                "is_default = ?, status = ?, updated_at = NOW() WHERE id = ? AND deleted = 0";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, resume.getResumeName());
            pstmt.setString(2, resume.getTemplateStyle());
            pstmt.setString(3, resume.getSummary());
            pstmt.setString(4, resume.getCareerObjective());
            pstmt.setString(5, resume.getPhone());
            pstmt.setString(6, resume.getEmail());
            pstmt.setString(7, resume.getWechat());
            pstmt.setString(8, resume.getGithubUrl());
            pstmt.setString(9, resume.getBlogUrl());
            pstmt.setString(10, resume.getPhotoUrl());
            pstmt.setInt(11, resume.getIsDefault() != null ? resume.getIsDefault() : 0);
            pstmt.setInt(12, resume.getStatus() != null ? resume.getStatus() : 1);
            pstmt.setInt(13, resume.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.update] 更新简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据ID查询简历（不包含软删除的）
     * @param id 简历ID
     * @return 简历对象
     */
    public Resume findById(Integer id) {
        return findById(id, false);
    }

    /**
     * 根据ID查询简历（可选择是否包含软删除的）
     * @param id 简历ID
     * @param includeDeleted 是否包含软删除的简历
     * @return 简历对象
     */
    public Resume findById(Integer id, boolean includeDeleted) {
        String sql;
        if (includeDeleted) {
            sql = "SELECT r.*, u.username, u.name as name FROM resumes r " +
                    "LEFT JOIN user u ON r.user_id = u.id " +
                    "WHERE r.id = ?";
        } else {
            sql = "SELECT r.*, u.username, u.name as name FROM resumes r " +
                    "LEFT JOIN user u ON r.user_id = u.id " +
                    "WHERE r.id = ? AND r.deleted = 0";
        }
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
            System.err.println("[ResumeDAO.findById] 查询简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询用户的所有简历（不包含软删除的）
     * @param userId 用户ID
     * @return 简历列表
     */
    public List<Resume> findByUserId(Integer userId) {
        String sql = "SELECT r.*, u.username, u.name FROM resumes r " +
                "LEFT JOIN user u ON r.user_id = u.id " +
                "WHERE r.user_id = ? AND r.deleted = 0 ORDER BY r.is_default DESC, r.created_at DESC";
        return findByUserIdWithSql(userId, sql);
    }

    /**
     * 查询用户的所有简历（包含软删除的）- 用于回收站功能
     * @param userId 用户ID
     * @return 简历列表
     */
    public List<Resume> findAllByUserIdIncludingDeleted(Integer userId) {
        String sql = "SELECT r.*, u.username, u.name FROM resumes r " +
                "LEFT JOIN user u ON r.user_id = u.id " +
                "WHERE r.user_id = ? ORDER BY r.deleted ASC, r.is_default DESC, r.created_at DESC";
        return findByUserIdWithSql(userId, sql);
    }

    /**
     * 查询用户被软删除的简历（回收站）
     * @param userId 用户ID
     * @return 简历列表
     */
    public List<Resume> findDeletedByUserId(Integer userId) {
        String sql = "SELECT r.*, u.username, u.name FROM resumes r " +
                "LEFT JOIN user u ON r.user_id = u.id " +
                "WHERE r.user_id = ? AND r.deleted = 1 ORDER BY r.updated_at DESC";
        return findByUserIdWithSql(userId, sql);
    }

    /**
     * 恢复软删除的简历
     * @param id 简历ID
     * @return 是否成功
     */
    public boolean restore(Integer id) {
        String sql = "UPDATE resumes SET deleted = 0, updated_at = NOW() WHERE id = ? AND deleted = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.restore] 恢复简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 永久删除简历（硬删除）
     * @param id 简历ID
     * @return 是否成功
     */
    public boolean hardDelete(Integer id) {
        String sql = "DELETE FROM resumes WHERE id = ? AND deleted = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.hardDelete] 永久删除简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 检查用户是否有被软删除的简历
     * @param userId 用户ID
     * @return 是否有被软删除的简历
     */
    public boolean hasDeletedResumes(Integer userId) {
        String sql = "SELECT COUNT(*) FROM resumes WHERE user_id = ? AND deleted = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.hasDeletedResumes] 查询失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 私有方法：执行SQL查询并映射结果
     */
    private List<Resume> findByUserIdWithSql(Integer userId, String sql) {
        List<Resume> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.findByUserId] 查询简历列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 查询用户的默认简历
     * @param userId 用户ID
     * @return 简历对象
     */
    public Resume findDefaultByUserId(Integer userId) {
        String sql = "SELECT r.*, u.username, u.name FROM resumes r " +
                "LEFT JOIN user u ON r.user_id = u.id " +
                "WHERE r.user_id = ? AND r.is_default = 1 AND r.deleted = 0 LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.findDefaultByUserId] 查询默认简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 软删除简历
     * @param id 简历ID
     * @return 是否成功
     */
    public boolean softDelete(Integer id) {
        String sql = "UPDATE resumes SET deleted = 1, updated_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.softDelete] 删除简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 增加浏览次数
     * @param id 简历ID
     * @return 是否成功
     */
    public boolean incrementViewCount(Integer id) {
        String sql = "UPDATE resumes SET view_count = view_count + 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeDAO.incrementViewCount] 更新浏览次数失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 设置默认简历（同时取消其他默认）
     * @param resumeId 简历ID
     * @param userId 用户ID
     * @return 是否成功
     */
    public boolean setDefaultResume(Integer resumeId, Integer userId) {
        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 取消该用户的所有默认简历
            String sql1 = "UPDATE resumes SET is_default = 0 WHERE user_id = ?";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setInt(1, userId);
            pstmt1.executeUpdate();

            // 2. 设置指定简历为默认
            String sql2 = "UPDATE resumes SET is_default = 1 WHERE id = ? AND user_id = ?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, resumeId);
            pstmt2.setInt(2, userId);
            int affected = pstmt2.executeUpdate();

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("[ResumeDAO.setDefaultResume] 设置默认简历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeResources(null, pstmt1, null);
            DBUtil.closeResources(conn, pstmt2, null);
        }
        return false;
    }

    // ========== 私有方法 ==========

    /**
     * ResultSet映射为实体
     */
    private Resume mapResultSetToEntity(ResultSet rs) throws SQLException {
        Resume resume = new Resume();
        resume.setId(rs.getInt("id"));
        resume.setUserId(rs.getInt("user_id"));
        resume.setResumeName(rs.getString("resume_name"));
        resume.setTemplateStyle(rs.getString("template_style"));
        resume.setSummary(rs.getString("summary"));
        resume.setCareerObjective(rs.getString("career_objective"));
        resume.setPhone(rs.getString("phone"));
        resume.setEmail(rs.getString("email"));
        resume.setWechat(rs.getString("wechat"));
        resume.setGithubUrl(rs.getString("github_url"));
        resume.setBlogUrl(rs.getString("blog_url"));
        resume.setPhotoUrl(rs.getString("photo_url"));
        resume.setIsDefault(rs.getInt("is_default"));
        resume.setStatus(rs.getInt("status"));
        resume.setViewCount(rs.getInt("view_count"));
        resume.setCreatedAt(rs.getTimestamp("created_at"));
        resume.setUpdatedAt(rs.getTimestamp("updated_at"));
        resume.setDeleted(rs.getInt("deleted"));

        // 关联用户信息
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        // 注意：User类使用name字段，数据库可能使用real_name或name
        String realName = rs.getString("name");
        if (realName == null || realName.isEmpty()) {
            realName = rs.getString("name");
        }
        user.setName(realName);
        resume.setUser(user);

        return resume;
    }
}
