package dao;

import model.ResumeProject;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 简历-项目经历数据访问对象
 * 对应数据库表: resume_projects
 */
public class ResumeProjectDAO {

    /**
     * 保存项目经历
     * @param project 项目经历对象
     * @return 是否成功
     */
    public boolean save(ResumeProject project) {
        String sql = "INSERT INTO resume_projects (resume_id, project_name, role, team_size, start_date, end_date, " +
                "is_current, description, responsibilities, technologies, project_url, achievements, display_order, " +
                "is_from_system, system_project_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, project.getResumeId());
            pstmt.setString(2, project.getProjectName());
            pstmt.setString(3, project.getRole());
            pstmt.setObject(4, project.getTeamSize(), Types.INTEGER);
            pstmt.setDate(5, project.getStartDate());
            pstmt.setDate(6, project.getEndDate());
            pstmt.setInt(7, project.getIsCurrent() != null ? project.getIsCurrent() : 0);
            pstmt.setString(8, project.getDescription());
            pstmt.setString(9, project.getResponsibilities());
            pstmt.setString(10, project.getTechnologies());
            pstmt.setString(11, project.getProjectUrl());
            pstmt.setString(12, project.getAchievements());
            pstmt.setInt(13, project.getDisplayOrder() != null ? project.getDisplayOrder() : 0);
            pstmt.setInt(14, project.getIsFromSystem() != null ? project.getIsFromSystem() : 0);
            pstmt.setObject(15, project.getSystemProjectId(), Types.INTEGER);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    project.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ResumeProjectDAO.save] 保存项目经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 更新项目经历
     * @param project 项目经历对象
     * @return 是否成功
     */
    public boolean update(ResumeProject project) {
        String sql = "UPDATE resume_projects SET project_name = ?, role = ?, team_size = ?, start_date = ?, " +
                "end_date = ?, is_current = ?, description = ?, responsibilities = ?, technologies = ?, " +
                "project_url = ?, achievements = ?, display_order = ?, updated_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, project.getProjectName());
            pstmt.setString(2, project.getRole());
            pstmt.setObject(3, project.getTeamSize(), Types.INTEGER);
            pstmt.setDate(4, project.getStartDate());
            pstmt.setDate(5, project.getEndDate());
            pstmt.setInt(6, project.getIsCurrent() != null ? project.getIsCurrent() : 0);
            pstmt.setString(7, project.getDescription());
            pstmt.setString(8, project.getResponsibilities());
            pstmt.setString(9, project.getTechnologies());
            pstmt.setString(10, project.getProjectUrl());
            pstmt.setString(11, project.getAchievements());
            pstmt.setInt(12, project.getDisplayOrder() != null ? project.getDisplayOrder() : 0);
            pstmt.setInt(13, project.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeProjectDAO.update] 更新项目经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 根据ID查询项目经历
     * @param id 项目经历ID
     * @return 项目经历对象
     */
    public ResumeProject findById(Integer id) {
        String sql = "SELECT * FROM resume_projects WHERE id = ?";
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
            System.err.println("[ResumeProjectDAO.findById] 查询项目经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询简历的所有项目经历
     * @param resumeId 简历ID
     * @return 项目经历列表
     */
    public List<ResumeProject> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_projects WHERE resume_id = ? ORDER BY is_current DESC, end_date DESC, display_order ASC";
        List<ResumeProject> list = new ArrayList<>();
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
            System.err.println("[ResumeProjectDAO.findByResumeId] 查询项目经历列表失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 删除项目经历
     * @param id 项目经历ID
     * @return 是否成功
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_projects WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ResumeProjectDAO.delete] 删除项目经历失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * ResultSet映射为实体
     */
    private ResumeProject mapResultSetToEntity(ResultSet rs) throws SQLException {
        ResumeProject project = new ResumeProject();
        project.setId(rs.getInt("id"));
        project.setResumeId(rs.getInt("resume_id"));
        project.setProjectName(rs.getString("project_name"));
        project.setRole(rs.getString("role"));
        project.setTeamSize(rs.getInt("team_size"));
        project.setStartDate(rs.getDate("start_date"));
        project.setEndDate(rs.getDate("end_date"));
        project.setIsCurrent(rs.getInt("is_current"));
        project.setDescription(rs.getString("description"));
        project.setResponsibilities(rs.getString("responsibilities"));
        project.setTechnologies(rs.getString("technologies"));
        project.setProjectUrl(rs.getString("project_url"));
        project.setAchievements(rs.getString("achievements"));
        project.setDisplayOrder(rs.getInt("display_order"));
        project.setIsFromSystem(rs.getInt("is_from_system"));
        project.setSystemProjectId(rs.getInt("system_project_id"));
        project.setCreatedAt(rs.getTimestamp("created_at"));
        project.setUpdatedAt(rs.getTimestamp("updated_at"));
        return project;
    }
}
