package dao;

import model.Project;
import model.ProjectHistory;
import model.ProjectPlan;
import model.ProjectProgress;
import model.ProjectMember;
import model.ProjectMemberApplication;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目数据访问层
 */
public class ProjectDAO {

    /**
     * 根据ID查询项目
     */
    public Project findById(Integer id) {
        String sql = "SELECT * FROM project WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProject(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询所有已批准的项目
     */
    public List<Project> findApprovedProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM project WHERE status = 'APPROVED' ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return projects;
    }

    /**
     * 查询所有项目（管理员用）
     */
    public List<Project> findAll() {
        return findByConditions(null, null, null);
    }

    /**
     * 根据负责人ID查询项目
     */
    public List<Project> findByLeaderId(Integer leaderId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT p.*, u.name as leader_name FROM project p " +
                    "LEFT JOIN user u ON p.leader_id = u.id " +
                    "WHERE p.leader_id = ? AND (p.deleted = 0 OR p.deleted IS NULL) " +
                    "ORDER BY p.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, leaderId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return projects;
    }

    /**
     * 根据条件搜索项目
     */
    public List<Project> findByConditions(String keyword, String status, Integer year) {
        List<Project> projects = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.*, u.name as leader_name FROM project p " +
                    "LEFT JOIN user u ON p.leader_id = u.id " +
                    "WHERE (p.deleted = 0 OR p.deleted IS NULL)");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ? OR u.name LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND p.status = ?");
            params.add(status.toLowerCase());
        }
        if (year != null) {
            sql.append(" AND p.year = ?");
            params.add(year);
        }
        sql.append(" ORDER BY p.year DESC, p.created_at DESC");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return projects;
    }

    /**
     * 查询成员在指定年份的项目数量
     */
    public int countProjectsByMemberAndYear(Integer memberId, Integer year) {
        String sql = "SELECT COUNT(*) FROM project_member pm JOIN project p ON pm.project_id = p.id WHERE pm.user_id = ? AND p.year = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, year);
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
     * 根据用户ID查询项目（用户参与的所有项目）
     */
    public List<Project> findProjectsByUserId(Integer userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM project p LEFT JOIN project_member pm ON p.id = pm.project_id WHERE p.leader_id = ? OR pm.user_id = ? ORDER BY p.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return projects;
    }

    /**
     * 添加项目
     */
    public boolean insert(Project project) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return insert(project, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 添加项目（事务重载版本）
     */
    public boolean insert(Project project, Connection conn) {
        String sql = "INSERT INTO project (name, description, category, leader_id, status, year, expected_start_date, expected_end_date, admin_id, budget, repo_url, doc_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setString(3, project.getCategory());
            pstmt.setInt(4, project.getLeaderId());
            pstmt.setString(5, project.getStatus() != null ? project.getStatus().toLowerCase() : "pending");
            pstmt.setInt(6, project.getYear());

            if (project.getExpectedStartDate() != null) {
                pstmt.setDate(7, new java.sql.Date(project.getExpectedStartDate().getTime()));
            } else {
                pstmt.setNull(7, java.sql.Types.DATE);
            }

            if (project.getExpectedEndDate() != null) {
                pstmt.setDate(8, new java.sql.Date(project.getExpectedEndDate().getTime()));
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
            }

            if (project.getAdminId() != null) {
                pstmt.setInt(9, project.getAdminId());
            } else {
                pstmt.setNull(9, java.sql.Types.INTEGER);
            }
            pstmt.setBigDecimal(10, project.getBudget());
            pstmt.setString(11, project.getRepoUrl());
            pstmt.setString(12, project.getDocUrl());

            int result = pstmt.executeUpdate();
            if (result > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    project.setId(rs.getInt(1));
                }
            }
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("插入项目失败", e);
        } finally {
            closeResources(null, pstmt, rs);
        }
    }

    /**
     * 审批项目
     */
    public boolean approve(Integer projectId, Integer adminId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return approve(projectId, adminId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 审批项目（事务重载版本）
     */
    public boolean approve(Integer projectId, Integer adminId, Connection conn) {
        String sql = "UPDATE project SET status = 'approved', approved_by = ?, approved_at = NOW() WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            pstmt.setInt(2, projectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("审批项目失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 拒绝项目
     */
    public boolean reject(Integer projectId, Integer adminId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return reject(projectId, adminId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 拒绝项目（事务重载版本）
     */
    public boolean reject(Integer projectId, Integer adminId, Connection conn) {
        String sql = "UPDATE project SET status = 'rejected', approved_by = ?, approved_at = NOW() WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            pstmt.setInt(2, projectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("拒绝项目失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 更新项目
     */
    public boolean update(Project project) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return update(project, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 更新项目（事务重载版本）
     */
    public boolean update(Project project, Connection conn) {
        String sql = "UPDATE project SET name=?, description=?, category=?, leader_id=?, status=?, year=?, expected_start_date=?, expected_end_date=?, actual_start_date=?, actual_end_date=?, admin_id=?, budget=?, repo_url=?, doc_url=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setString(3, project.getCategory());
            pstmt.setInt(4, project.getLeaderId());
            pstmt.setString(5, project.getStatus());
            pstmt.setInt(6, project.getYear());

            if (project.getExpectedStartDate() != null) {
                pstmt.setDate(7, new java.sql.Date(project.getExpectedStartDate().getTime()));
            } else {
                pstmt.setNull(7, java.sql.Types.DATE);
            }

            if (project.getExpectedEndDate() != null) {
                pstmt.setDate(8, new java.sql.Date(project.getExpectedEndDate().getTime()));
            } else {
                pstmt.setNull(8, java.sql.Types.DATE);
            }

            if (project.getActualStartDate() != null) {
                pstmt.setDate(9, new java.sql.Date(project.getActualStartDate().getTime()));
            } else {
                pstmt.setNull(9, java.sql.Types.DATE);
            }

            if (project.getActualEndDate() != null) {
                pstmt.setDate(10, new java.sql.Date(project.getActualEndDate().getTime()));
            } else {
                pstmt.setNull(10, java.sql.Types.DATE);
            }

            if (project.getAdminId() != null) {
                pstmt.setInt(11, project.getAdminId());
            } else {
                pstmt.setNull(11, java.sql.Types.INTEGER);
            }

            if (project.getBudget() != null) {
                pstmt.setBigDecimal(12, project.getBudget());
            } else {
                pstmt.setNull(12, java.sql.Types.DECIMAL);
            }

            pstmt.setString(13, project.getRepoUrl());
            pstmt.setString(14, project.getDocUrl());
            pstmt.setInt(15, project.getId());

            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("更新项目失败：项目不存在");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新项目失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 删除项目（软删除）
     */
    public boolean delete(Integer id) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return delete(id, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 删除项目（软删除，事务重载版本）
     */
    public boolean delete(Integer id, Connection conn) {
        String sql = "UPDATE project SET deleted = 1 WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("删除项目失败：项目不存在");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("删除项目失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 添加项目成员
     */
    public boolean addMember(Integer projectId, Integer memberId, String role) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return addMember(projectId, memberId, role, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 添加项目成员（事务重载版本）
     */
    public boolean addMember(Integer projectId, Integer memberId, String role, Connection conn) {
        String sql = "INSERT INTO project_member (project_id, user_id, role) VALUES (?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, memberId);
            pstmt.setString(3, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("添加项目成员失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 将ResultSet映射为Project对象
     */
    private Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setCategory(rs.getString("category"));
        project.setLeaderId(rs.getInt("leader_id"));
        
        try {
            project.setLeaderName(rs.getString("leader_name"));
        } catch (SQLException e) { }
        
        project.setStatus(rs.getString("status"));
        project.setYear(rs.getInt("year"));
        
        try {
            java.sql.Date expectedStartDate = rs.getDate("expected_start_date");
            project.setExpectedStartDate(expectedStartDate != null ? new java.util.Date(expectedStartDate.getTime()) : null);
        } catch (SQLException e) { }
        
        try {
            java.sql.Date expectedEndDate = rs.getDate("expected_end_date");
            project.setExpectedEndDate(expectedEndDate != null ? new java.util.Date(expectedEndDate.getTime()) : null);
        } catch (SQLException e) { }
        
        try {
            java.sql.Date actualStartDate = rs.getDate("actual_start_date");
            project.setActualStartDate(actualStartDate != null ? new java.util.Date(actualStartDate.getTime()) : null);
        } catch (SQLException e) { }
        
        try {
            java.sql.Date actualEndDate = rs.getDate("actual_end_date");
            project.setActualEndDate(actualEndDate != null ? new java.util.Date(actualEndDate.getTime()) : null);
        } catch (SQLException e) { }
        
        try {
            project.setAdminId(rs.getInt("admin_id"));
        } catch (SQLException e) { }
        
        try {
            project.setBudget(rs.getBigDecimal("budget"));
        } catch (SQLException e) { }
        
        try {
            project.setRepoUrl(rs.getString("repo_url"));
        } catch (SQLException e) { }
        
        try {
            project.setDocUrl(rs.getString("doc_url"));
        } catch (SQLException e) { }
        
        try {
            project.setDeleted(rs.getInt("deleted"));
        } catch (SQLException e) { }
        
        project.setCreatedAt(rs.getTimestamp("created_at"));
        project.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        try {
            project.setApprovedBy(rs.getInt("approved_by"));
            Timestamp approvedAt = rs.getTimestamp("approved_at");
            project.setApprovedAt(approvedAt != null ? new java.util.Date(approvedAt.getTime()) : null);
        } catch (SQLException e) {
            // 忽略不存在的字段
        }
        return project;
    }

    public boolean addLabel(Integer projectId, String labelCode) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return addLabel(projectId, labelCode, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean addLabel(Integer projectId, String labelCode, Connection conn) {
        String sql = "INSERT INTO project_label (project_id, label_code) VALUES (?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setString(2, labelCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("添加项目标签失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public boolean removeLabel(Integer projectId, String labelCode) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return removeLabel(projectId, labelCode, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean removeLabel(Integer projectId, String labelCode, Connection conn) {
        String sql = "DELETE FROM project_label WHERE project_id = ? AND label_code = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setString(2, labelCode);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("移除项目标签失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public List<String> getLabels(Integer projectId) {
        List<String> labels = new ArrayList<>();
        String sql = "SELECT label_code FROM project_label WHERE project_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                labels.add(rs.getString("label_code"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return labels;
    }

    public boolean addHistory(Integer projectId, String operationType, Integer operatorId, String operatorName, String description, String oldValue, String newValue) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return addHistory(projectId, operationType, operatorId, operatorName, description, oldValue, newValue, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean addHistory(Integer projectId, String operationType, Integer operatorId, String operatorName, String description, String oldValue, String newValue, Connection conn) {
        String sql = "INSERT INTO project_history (project_id, operation_type, operator_id, operator_name, description, old_value, new_value) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setString(2, operationType);
            pstmt.setInt(3, operatorId);
            pstmt.setString(4, operatorName);
            pstmt.setString(5, description);
            pstmt.setString(6, oldValue);
            pstmt.setString(7, newValue);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("添加项目历史失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public List<ProjectHistory> getHistory(Integer projectId) {
        List<ProjectHistory> historyList = new ArrayList<>();
        String sql = "SELECT * FROM project_history WHERE project_id = ? ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectHistory history = new ProjectHistory();
                history.setId(rs.getInt("id"));
                history.setProjectId(rs.getInt("project_id"));
                history.setOperationType(rs.getString("operation_type"));
                history.setOperatorId(rs.getInt("operator_id"));
                history.setOperatorName(rs.getString("operator_name"));
                history.setDescription(rs.getString("description"));
                history.setOldValue(rs.getString("old_value"));
                history.setNewValue(rs.getString("new_value"));
                history.setCreatedAt(rs.getTimestamp("created_at"));
                historyList.add(history);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return historyList;
    }

    public boolean addPlan(ProjectPlan plan) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return addPlan(plan, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean addPlan(ProjectPlan plan, Connection conn) {
        String sql = "INSERT INTO project_plan (project_id, title, description, start_date, end_date, order_index) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, plan.getProjectId());
            pstmt.setString(2, plan.getTitle());
            pstmt.setString(3, plan.getDescription());
            pstmt.setDate(4, new java.sql.Date(plan.getStartDate().getTime()));
            pstmt.setDate(5, new java.sql.Date(plan.getEndDate().getTime()));
            pstmt.setInt(6, plan.getOrderIndex() != null ? plan.getOrderIndex() : 0);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    plan.setId(rs.getInt(1));
                }
            }
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("添加项目计划失败", e);
        } finally {
            closeResources(null, pstmt, rs);
        }
    }

    public boolean updatePlan(ProjectPlan plan) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return updatePlan(plan, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean updatePlan(ProjectPlan plan, Connection conn) {
        String sql = "UPDATE project_plan SET title=?, description=?, start_date=?, end_date=?, order_index=? WHERE id=?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plan.getTitle());
            pstmt.setString(2, plan.getDescription());
            pstmt.setDate(3, new java.sql.Date(plan.getStartDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(plan.getEndDate().getTime()));
            pstmt.setInt(5, plan.getOrderIndex() != null ? plan.getOrderIndex() : 0);
            pstmt.setInt(6, plan.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新项目计划失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public boolean deletePlan(Integer planId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return deletePlan(planId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean deletePlan(Integer planId, Connection conn) {
        String sql = "DELETE FROM project_plan WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, planId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("删除项目计划失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public List<ProjectPlan> getPlans(Integer projectId) {
        List<ProjectPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM project_plan WHERE project_id = ? ORDER BY order_index, start_date";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectPlan plan = new ProjectPlan();
                plan.setId(rs.getInt("id"));
                plan.setProjectId(rs.getInt("project_id"));
                plan.setTitle(rs.getString("title"));
                plan.setDescription(rs.getString("description"));
                plan.setStartDate(rs.getDate("start_date"));
                plan.setEndDate(rs.getDate("end_date"));
                plan.setOrderIndex(rs.getInt("order_index"));
                plan.setCreatedAt(rs.getTimestamp("created_at"));
                plan.setUpdatedAt(rs.getTimestamp("updated_at"));
                plans.add(plan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return plans;
    }

    public boolean addProgress(ProjectProgress progress) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return addProgress(progress, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean addProgress(ProjectProgress progress, Connection conn) {
        String sql = "INSERT INTO project_progress (project_id, plan_id, title, description, completion_rate, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, progress.getProjectId());
            if (progress.getPlanId() != null) {
                pstmt.setInt(2, progress.getPlanId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            pstmt.setString(3, progress.getTitle());
            pstmt.setString(4, progress.getDescription());
            pstmt.setInt(5, progress.getCompletionRate() != null ? progress.getCompletionRate() : 0);
            pstmt.setInt(6, progress.getCreatedBy());
            int result = pstmt.executeUpdate();
            if (result > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    progress.setId(rs.getInt(1));
                }
            }
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("添加项目进度失败", e);
        } finally {
            closeResources(null, pstmt, rs);
        }
    }

    public List<ProjectProgress> getProgressList(Integer projectId) {
        List<ProjectProgress> progressList = new ArrayList<>();
        String sql = "SELECT pp.*, u.name as creator_name, p.title as plan_title FROM project_progress pp LEFT JOIN user u ON pp.created_by = u.id LEFT JOIN project_plan p ON pp.plan_id = p.id WHERE pp.project_id = ? ORDER BY pp.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectProgress progress = new ProjectProgress();
                progress.setId(rs.getInt("id"));
                progress.setProjectId(rs.getInt("project_id"));
                progress.setPlanId(rs.getInt("plan_id"));
                progress.setTitle(rs.getString("title"));
                progress.setDescription(rs.getString("description"));
                progress.setCompletionRate(rs.getInt("completion_rate"));
                progress.setCreatedBy(rs.getInt("created_by"));
                progress.setCreatedAt(rs.getTimestamp("created_at"));
                progress.setUpdatedAt(rs.getTimestamp("updated_at"));
                progress.setCreatorName(rs.getString("creator_name"));
                progress.setPlanTitle(rs.getString("plan_title"));
                progressList.add(progress);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return progressList;
    }

    public boolean applyMember(Integer projectId, Integer userId, String reason) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return applyMember(projectId, userId, reason, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean applyMember(Integer projectId, Integer userId, String reason, Connection conn) {
        String sql = "INSERT INTO project_member_application (project_id, user_id, status, reason) VALUES (?, ?, 'PENDING', ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, reason);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("申请加入项目失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public boolean approveMemberApplication(Integer applicationId, Integer handledBy) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return approveMemberApplication(applicationId, handledBy, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean approveMemberApplication(Integer applicationId, Integer handledBy, Connection conn) {
        String sql = "UPDATE project_member_application SET status = 'CONFIRMED', handled_at = NOW(), handled_by = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, handledBy);
            pstmt.setInt(2, applicationId);
            boolean result = pstmt.executeUpdate() > 0;

            if (result) {
                ProjectMemberApplication app = getMemberApplicationById(applicationId);
                if (app != null) {
                    addMember(app.getProjectId(), app.getUserId(), "MEMBER");
                }
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("审批项目申请失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public boolean rejectMemberApplication(Integer applicationId, Integer handledBy, String reason) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return rejectMemberApplication(applicationId, handledBy, reason, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean rejectMemberApplication(Integer applicationId, Integer handledBy, String reason, Connection conn) {
        String sql = "UPDATE project_member_application SET status = 'REJECTED', handled_at = NOW(), handled_by = ?, reason = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, handledBy);
            pstmt.setString(2, reason);
            pstmt.setInt(3, applicationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("拒绝项目申请失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public ProjectMemberApplication getMemberApplicationById(Integer id) {
        String sql = "SELECT pma.*, p.name as project_name, u.name as user_name, mp.student_id, mp.major FROM project_member_application pma JOIN project p ON pma.project_id = p.id JOIN user u ON pma.user_id = u.id LEFT JOIN member_profile mp ON u.id = mp.user_id WHERE pma.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                ProjectMemberApplication app = new ProjectMemberApplication();
                app.setId(rs.getInt("id"));
                app.setProjectId(rs.getInt("project_id"));
                app.setUserId(rs.getInt("user_id"));
                app.setStatus(rs.getString("status"));
                app.setAppliedAt(rs.getTimestamp("applied_at"));
                app.setHandledAt(rs.getTimestamp("handled_at"));
                app.setHandledBy(rs.getInt("handled_by"));
                app.setReason(rs.getString("reason"));
                app.setProjectName(rs.getString("project_name"));
                app.setUserName(rs.getString("user_name"));
                app.setStudentId(rs.getString("student_id"));
                app.setMajor(rs.getString("major"));
                return app;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public List<ProjectMemberApplication> getMemberApplications(Integer projectId, String status) {
        List<ProjectMemberApplication> applications = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT pma.*, p.name as project_name, u.name as user_name, mp.student_id, mp.major FROM project_member_application pma JOIN project p ON pma.project_id = p.id JOIN user u ON pma.user_id = u.id LEFT JOIN member_profile mp ON u.id = mp.user_id WHERE pma.project_id = ?");
        if (status != null && !status.isEmpty()) {
            sql.append(" AND pma.status = ?");
        }
        sql.append(" ORDER BY pma.applied_at DESC");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            pstmt.setInt(1, projectId);
            if (status != null && !status.isEmpty()) {
                pstmt.setString(2, status);
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectMemberApplication app = new ProjectMemberApplication();
                app.setId(rs.getInt("id"));
                app.setProjectId(rs.getInt("project_id"));
                app.setUserId(rs.getInt("user_id"));
                app.setStatus(rs.getString("status"));
                app.setAppliedAt(rs.getTimestamp("applied_at"));
                app.setHandledAt(rs.getTimestamp("handled_at"));
                app.setHandledBy(rs.getInt("handled_by"));
                app.setReason(rs.getString("reason"));
                app.setProjectName(rs.getString("project_name"));
                app.setUserName(rs.getString("user_name"));
                app.setStudentId(rs.getString("student_id"));
                app.setMajor(rs.getString("major"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return applications;
    }

    public List<ProjectMemberApplication> getMyApplications(Integer userId) {
        List<ProjectMemberApplication> applications = new ArrayList<>();
        String sql = "SELECT pma.*, p.name as project_name, u.name as user_name, mp.student_id, mp.major FROM project_member_application pma JOIN project p ON pma.project_id = p.id JOIN user u ON pma.user_id = u.id LEFT JOIN member_profile mp ON u.id = mp.user_id WHERE pma.user_id = ? ORDER BY pma.applied_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectMemberApplication app = new ProjectMemberApplication();
                app.setId(rs.getInt("id"));
                app.setProjectId(rs.getInt("project_id"));
                app.setUserId(rs.getInt("user_id"));
                app.setStatus(rs.getString("status"));
                app.setAppliedAt(rs.getTimestamp("applied_at"));
                app.setHandledAt(rs.getTimestamp("handled_at"));
                app.setHandledBy(rs.getInt("handled_by"));
                app.setReason(rs.getString("reason"));
                app.setProjectName(rs.getString("project_name"));
                app.setUserName(rs.getString("user_name"));
                app.setStudentId(rs.getString("student_id"));
                app.setMajor(rs.getString("major"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return applications;
    }

    public boolean isMember(Integer projectId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM project_member WHERE project_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    public boolean hasPendingApplication(Integer projectId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM project_member_application WHERE project_id = ? AND user_id = ? AND status = 'PENDING'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    public List<ProjectMember> getProjectMembers(Integer projectId) {
        List<ProjectMember> members = new ArrayList<>();
        String sql = "SELECT pm.*, u.name as user_name, mp.student_id, mp.major FROM project_member pm JOIN user u ON pm.user_id = u.id LEFT JOIN member_profile mp ON u.id = mp.user_id WHERE pm.project_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProjectMember member = new ProjectMember();
                member.setId(rs.getInt("id"));
                member.setProjectId(rs.getInt("project_id"));
                member.setUserId(rs.getInt("user_id"));
                member.setRole(rs.getString("role"));
                member.setJoinedAt(rs.getTimestamp("joined_at"));
                member.setUserName(rs.getString("user_name"));
                member.setStudentId(rs.getString("student_id"));
                member.setMajor(rs.getString("major"));
                members.add(member);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return members;
    }

    public boolean removeMember(Integer projectId, Integer userId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return removeMember(projectId, userId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean removeMember(Integer projectId, Integer userId, Connection conn) {
        String sql = "DELETE FROM project_member WHERE project_id = ? AND user_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("移除项目成员失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    public boolean transferAdmin(Integer projectId, Integer newAdminId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return transferAdmin(projectId, newAdminId, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    public boolean transferAdmin(Integer projectId, Integer newAdminId, Connection conn) {
        String sql = "UPDATE project SET admin_id = ? WHERE id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newAdminId);
            pstmt.setInt(2, projectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("转让项目管理员失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
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

    public List<Object[]> getProjectImages(Integer projectId) {
        List<Object[]> images = new ArrayList<>();
        String sql = "SELECT pi.id, pi.project_id, pi.file_id, fs.stored_name, fs.original_name, fs.file_size, fs.created_at " +
                     "FROM project_image pi JOIN file_storage fs ON pi.file_id = fs.id " +
                     "WHERE pi.project_id = ? AND fs.status = 1 ORDER BY pi.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] img = new Object[7];
                img[0] = rs.getInt("id");
                img[1] = rs.getInt("project_id");
                img[2] = rs.getInt("file_id");
                img[3] = rs.getString("stored_name");
                img[4] = rs.getString("original_name");
                img[5] = rs.getLong("file_size");
                img[6] = rs.getTimestamp("created_at");
                images.add(img);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return images;
    }

    public List<Object[]> getProjectFiles(Integer projectId) {
        List<Object[]> files = new ArrayList<>();
        String sql = "SELECT pf.id, pf.project_id, pf.file_id, pf.file_type, fs.stored_name, fs.original_name, fs.file_size, fs.created_at " +
                     "FROM project_file pf JOIN file_storage fs ON pf.file_id = fs.id " +
                     "WHERE pf.project_id = ? AND fs.status = 1 ORDER BY pf.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] file = new Object[8];
                file[0] = rs.getInt("id");
                file[1] = rs.getInt("project_id");
                file[2] = rs.getInt("file_id");
                file[3] = rs.getString("file_type");
                file[4] = rs.getString("stored_name");
                file[5] = rs.getString("original_name");
                file[6] = rs.getLong("file_size");
                file[7] = rs.getTimestamp("created_at");
                files.add(file);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return files;
    }

    /**
     * 统计项目总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM project WHERE status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
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
}
