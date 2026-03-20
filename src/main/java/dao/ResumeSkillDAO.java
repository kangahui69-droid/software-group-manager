package dao;

import model.ResumeSkill;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 简历-技能特长数据访问对象
 * 对应数据库表: resume_skills
 */
public class ResumeSkillDAO {

    /**
     * 保存技能
     * @param skill 技能对象
     * @return 是否成功
     */
    public boolean save(ResumeSkill skill) {
        String sql = "INSERT INTO resume_skills (resume_id, skill_name, proficiency, proficiency_score, " +
                "category, description, display_order, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, skill.getResumeId());
            pstmt.setString(2, skill.getSkillName());
            pstmt.setString(3, skill.getProficiency());
            pstmt.setInt(4, skill.getProficiencyScore() != null ? skill.getProficiencyScore() : 50);
            pstmt.setString(5, skill.getCategory());
            pstmt.setString(6, skill.getDescription());
            pstmt.setInt(7, skill.getDisplayOrder() != null ? skill.getDisplayOrder() : 0);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    skill.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ResumeSkillDAO.save] 保存技能失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新技能
     * @param skill 技能对象
     * @return 是否成功
     */
    public boolean update(ResumeSkill skill) {
        String sql = "UPDATE resume_skills SET skill_name = ?, proficiency = ?, proficiency_score = ?, " +
                "category = ?, description = ?, display_order = ?, updated_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, skill.getSkillName());
            pstmt.setString(2, skill.getProficiency());
            pstmt.setInt(3, skill.getProficiencyScore() != null ? skill.getProficiencyScore() : 50);
            pstmt.setString(4, skill.getCategory());
            pstmt.setString(5, skill.getDescription());
            pstmt.setInt(6, skill.getDisplayOrder() != null ? skill.getDisplayOrder() : 0);
            pstmt.setInt(7, skill.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeSkillDAO.update] 更新技能失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据ID查询技能
     * @param id 技能ID
     * @return 技能对象
     */
    public ResumeSkill findById(Integer id) {
        String sql = "SELECT * FROM resume_skills WHERE id = ?";
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
            System.err.println("[ResumeSkillDAO.findById] 查询技能失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询简历的所有技能
     * @param resumeId 简历ID
     * @return 技能列表
     */
    public List<ResumeSkill> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_skills WHERE resume_id = ? ORDER BY display_order ASC, id ASC";
        List<ResumeSkill> list = new ArrayList<>();
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
            System.err.println("[ResumeSkillDAO.findByResumeId] 查询技能列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 按分类查询技能
     * @param resumeId 简历ID
     * @param category 分类
     * @return 技能列表
     */
    public List<ResumeSkill> findByCategory(Integer resumeId, String category) {
        String sql = "SELECT * FROM resume_skills WHERE resume_id = ? AND category = ? ORDER BY display_order ASC";
        List<ResumeSkill> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, resumeId);
            pstmt.setString(2, category);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ResumeSkillDAO.findByCategory] 查询技能列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除技能
     * @param id 技能ID
     * @return 是否成功
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_skills WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeSkillDAO.delete] 删除技能失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 删除简历的所有技能
     * @param resumeId 简历ID
     * @return 是否成功
     */
    public boolean deleteByResumeId(Integer resumeId) {
        String sql = "DELETE FROM resume_skills WHERE resume_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, resumeId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[ResumeSkillDAO.deleteByResumeId] 删除技能失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * ResultSet映射为实体
     */
    private ResumeSkill mapResultSetToEntity(ResultSet rs) throws SQLException {
        ResumeSkill skill = new ResumeSkill();
        skill.setId(rs.getInt("id"));
        skill.setResumeId(rs.getInt("resume_id"));
        skill.setSkillName(rs.getString("skill_name"));
        skill.setProficiency(rs.getString("proficiency"));

        // 修复：正确处理 proficiency_score 可能为 NULL 的情况
        int proficiencyScore = rs.getInt("proficiency_score");
        if (rs.wasNull()) {
            skill.setProficiencyScore(null);
        } else {
            skill.setProficiencyScore(proficiencyScore);
        }

        skill.setCategory(rs.getString("category"));
        skill.setDescription(rs.getString("description"));
        skill.setDisplayOrder(rs.getInt("display_order"));
        skill.setCreatedAt(rs.getTimestamp("created_at"));
        skill.setUpdatedAt(rs.getTimestamp("updated_at"));
        return skill;
    }
}
