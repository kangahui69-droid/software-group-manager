package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.Project;
import com.softwaregroup.project.model.ProjectMemberApplication;
import com.softwaregroup.project.model.dto.PlanDTO;
import com.softwaregroup.project.model.dto.ProgressDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Project persistence against the same tables used by the legacy WAR. */
@Repository
public class ProjectDAO {
    private static final String COLUMNS = "p.id,p.name,p.description,p.category,p.year,p.status,p.leader_id,p.admin_id," +
            "p.budget,p.repo_url,p.expected_start_date,p.expected_end_date,p.created_at,p.updated_at,p.deleted";
    private static final RowMapper<Project> PROJECT_MAPPER = (rs, row) -> {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setCategory(rs.getString("category"));
        project.setYear(rs.getInt("year"));
        project.setStatus(rs.getString("status"));
        project.setLeaderId(rs.getInt("leader_id"));
        project.setAdminId((Integer) rs.getObject("admin_id"));
        project.setBudget(rs.getBigDecimal("budget"));
        project.setRepoUrl(rs.getString("repo_url"));
        project.setExpectedStartDate(rs.getDate("expected_start_date"));
        project.setExpectedEndDate(rs.getDate("expected_end_date"));
        project.setCreatedAt(rs.getTimestamp("created_at"));
        project.setUpdatedAt(rs.getTimestamp("updated_at"));
        project.setDeleted(rs.getInt("deleted"));
        return project;
    };

    private final JdbcTemplate jdbc;

    public ProjectDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Project> findByConditions(String keyword, String status, Integer year) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM project p WHERE p.deleted=0");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ?)");
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status=?");
            args.add(status);
        }
        if (year != null) {
            sql.append(" AND p.year=?");
            args.add(year);
        }
        sql.append(" ORDER BY p.created_at DESC,p.id DESC");
        return jdbc.query(sql.toString(), PROJECT_MAPPER, args.toArray());
    }

    public Project findById(Integer id) {
        List<Project> rows = jdbc.query("SELECT " + COLUMNS + " FROM project p WHERE p.id=? AND p.deleted=0",
                PROJECT_MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Project> findByUserId(Integer userId) {
        return findProjectsByUserId(userId);
    }

    public List<Project> findProjectsByUserId(Integer userId) {
        return jdbc.query("SELECT DISTINCT " + COLUMNS + " FROM project p " +
                "LEFT JOIN project_member pm ON pm.project_id=p.id " +
                "WHERE p.deleted=0 AND (p.leader_id=? OR p.admin_id=? OR pm.user_id=?) " +
                "ORDER BY p.created_at DESC,p.id DESC", PROJECT_MAPPER, userId, userId, userId);
    }

    public List<Project> findByOwnerId(Integer ownerId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM project p WHERE p.deleted=0 AND p.leader_id=? " +
                "ORDER BY p.created_at DESC,p.id DESC", PROJECT_MAPPER, ownerId);
    }

    public int countProjectsByMemberAndYear(Integer userId, Integer year) {
        Integer count = jdbc.queryForObject("SELECT COUNT(DISTINCT p.id) FROM project p " +
                "LEFT JOIN project_member pm ON pm.project_id=p.id " +
                "WHERE p.deleted=0 AND p.year=? AND (p.leader_id=? OR pm.user_id=?)",
                Integer.class, year, userId, userId);
        return count == null ? 0 : count;
    }

    public boolean insert(Project project) {
        KeyHolder keys = new GeneratedKeyHolder();
        int count = jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO project " +
                    "(name,description,category,year,status,leader_id,admin_id,budget,repo_url," +
                    "expected_start_date,expected_end_date,deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,0)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, project.getName());
            ps.setString(2, project.getDescription());
            ps.setString(3, project.getCategory());
            ps.setInt(4, project.getYear());
            ps.setString(5, project.getStatus());
            ps.setInt(6, project.getLeaderId());
            ps.setObject(7, project.getAdminId());
            ps.setBigDecimal(8, project.getBudget());
            ps.setString(9, project.getRepoUrl());
            ps.setDate(10, sqlDate(project.getExpectedStartDate()));
            ps.setDate(11, sqlDate(project.getExpectedEndDate()));
            return ps;
        }, keys);
        if (count == 1) {
            if (keys.getKey() == null) {
                throw new IllegalStateException("Project insert did not return an ID");
            }
            project.setId(keys.getKey().intValue());
            return true;
        }
        return false;
    }

    public boolean update(Project project, Integer operatorId) {
        return jdbc.update("UPDATE project SET name=?,description=?,category=?,budget=?,repo_url=?," +
                        "expected_start_date=?,expected_end_date=? WHERE id=? AND deleted=0 " +
                        "AND status NOT IN ('in_progress','completed')",
                project.getName(), project.getDescription(), project.getCategory(), project.getBudget(),
                project.getRepoUrl(), sqlDate(project.getExpectedStartDate()),
                sqlDate(project.getExpectedEndDate()), project.getId()) == 1;
    }

    public boolean delete(Integer id, Integer operatorId) {
        return jdbc.update("UPDATE project SET deleted=1 WHERE id=? AND deleted=0 " +
                "AND status NOT IN ('in_progress','completed')", id) == 1;
    }

    public boolean approve(Integer id, Integer approverId, Object extra) {
        return jdbc.update("UPDATE project SET status='approved',approved_by=?,approved_at=NOW() " +
                "WHERE id=? AND deleted=0 AND status='pending'", approverId, id) == 1;
    }

    public boolean reject(Integer id, Integer approverId, Object extra) {
        return jdbc.update("UPDATE project SET status='rejected',approved_by=?,approved_at=NOW() " +
                "WHERE id=? AND deleted=0 AND status='pending'", approverId, id) == 1;
    }

    public boolean isMember(Integer projectId, Integer userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM project_member pm JOIN project p ON p.id=pm.project_id " +
                "WHERE pm.project_id=? AND pm.user_id=? AND p.deleted=0", Integer.class, projectId, userId);
        return count != null && count > 0;
    }

    public boolean hasPendingApplication(Integer projectId, Integer userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM project_member_application WHERE project_id=? " +
                "AND user_id=? AND status='PENDING'", Integer.class, projectId, userId);
        return count != null && count > 0;
    }

    public boolean applyMember(Integer projectId, Integer userId, String reason, Integer approverId) {
        return jdbc.update("INSERT INTO project_member_application (project_id,user_id,status,reason) " +
                "VALUES (?,?,'PENDING',?)", projectId, userId, reason) == 1;
    }

    @Transactional
    public boolean approveMemberApplication(Integer applicationId, Integer approverId, Object extra) {
        List<ProjectMemberApplication> rows = jdbc.query("SELECT id,project_id,user_id,status,reason,applied_at " +
                "FROM project_member_application WHERE id=? FOR UPDATE", APPLICATION_MAPPER, applicationId);
        if (rows.isEmpty() || !"PENDING".equals(rows.get(0).getStatus())) {
            return false;
        }
        ProjectMemberApplication app = rows.get(0);
        int updated = jdbc.update("UPDATE project_member_application SET status='CONFIRMED',handled_by=?," +
                "handled_at=NOW() WHERE id=? AND status='PENDING'", approverId, applicationId);
        if (updated != 1) {
            return false;
        }
        jdbc.update("INSERT INTO project_member (project_id,user_id,role) VALUES (?,?,'MEMBER') " +
                "ON DUPLICATE KEY UPDATE role=role", app.getProjectId(), app.getUserId());
        return true;
    }

    public boolean rejectMemberApplication(Integer applicationId, Integer approverId, String reason, Object extra) {
        return jdbc.update("UPDATE project_member_application SET status='REJECTED',handled_by=?," +
                "handled_at=NOW(),reason=? WHERE id=? AND status='PENDING'", approverId, reason, applicationId) == 1;
    }

    private static final RowMapper<ProjectMemberApplication> APPLICATION_MAPPER = (rs, row) -> {
        ProjectMemberApplication app = new ProjectMemberApplication();
        app.setId(rs.getInt("id"));
        app.setProjectId(rs.getInt("project_id"));
        app.setUserId(rs.getInt("user_id"));
        app.setStatus(rs.getString("status"));
        app.setApplyReason(rs.getString("reason"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        return app;
    };

    public ProjectMemberApplication getMemberApplicationById(Integer applicationId) {
        List<ProjectMemberApplication> rows = jdbc.query("SELECT a.id,a.project_id,a.user_id,a.status,a.reason," +
                "a.applied_at,u.name AS user_name FROM project_member_application a JOIN user u ON u.id=a.user_id " +
                "JOIN project p ON p.id=a.project_id WHERE a.id=? AND p.deleted=0",
                (rs, row) -> {
                    ProjectMemberApplication app = APPLICATION_MAPPER.mapRow(rs, row);
                    app.setUserName(rs.getString("user_name"));
                    return app;
                }, applicationId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void addMember(Integer projectId, Integer userId, String role, Integer approverId) {
        jdbc.update("INSERT INTO project_member (project_id,user_id,role) VALUES (?,?,?)", projectId, userId, role);
    }

    public void addHistory(Integer projectId, String action, Integer operatorId, String detail,
                           String reason, Integer targetId, Object extra) {
        jdbc.update("INSERT INTO project_history (project_id,operation_type,operator_id,description,old_value) " +
                "VALUES (?,?,?,?,?)", projectId, action, operatorId, detail, reason);
    }

    public boolean addPlan(Integer projectId, Object plan) {
        if (!(plan instanceof PlanDTO dto)) {
            throw new IllegalArgumentException("PlanDTO required");
        }
        return jdbc.update("INSERT INTO project_plan (project_id,title,description,start_date,end_date) " +
                        "VALUES (?,?,?,?,?)", projectId, dto.getTitle(), dto.getDescription(),
                sqlDate(dto.getStartDate()), sqlDate(dto.getEndDate())) == 1;
    }

    public boolean addProgress(Integer projectId, Object progress, Integer creatorId) {
        if (!(progress instanceof ProgressDTO dto)) {
            throw new IllegalArgumentException("ProgressDTO required");
        }
        return jdbc.update("INSERT INTO project_progress (project_id,title,description,completion_rate,created_by) " +
                        "VALUES (?,?,?,?,?)", projectId, dto.getTitle(), dto.getDescription(),
                dto.getCompletionRate(), creatorId) == 1;
    }

    public List<Object> getProjectMembers(Integer projectId) {
        return new ArrayList<>(jdbc.queryForList("SELECT pm.project_id,pm.user_id,pm.role,u.name AS user_name " +
                "FROM project_member pm JOIN user u ON u.id=pm.user_id WHERE pm.project_id=?", projectId));
    }

    public List<Object> getLabels(Integer projectId) {
        return new ArrayList<>(jdbc.queryForList("SELECT label_code FROM project_label WHERE project_id=?", projectId));
    }

    public List<Object> getPlans(Integer projectId) {
        return new ArrayList<>(jdbc.queryForList("SELECT * FROM project_plan WHERE project_id=? " +
                "ORDER BY order_index,start_date,id", projectId));
    }

    public List<Object> getProgressList(Integer projectId) {
        return new ArrayList<>(jdbc.queryForList("SELECT * FROM project_progress WHERE project_id=? " +
                "ORDER BY created_at DESC,id DESC", projectId));
    }

    public List<Object> getHistory(Integer projectId) {
        return new ArrayList<>(jdbc.queryForList("SELECT * FROM project_history WHERE project_id=? " +
                "ORDER BY created_at DESC,id DESC", projectId));
    }

    private static java.sql.Date sqlDate(java.util.Date date) {
        return date == null ? null : new java.sql.Date(date.getTime());
    }
}
