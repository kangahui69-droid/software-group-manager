package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeProject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 简历项目经历数据访问实现
 */
@Repository
public class ResumeProjectDAOImpl implements ResumeProjectDAO {

    private final JdbcTemplate jdbcTemplate;

    public ResumeProjectDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ResumeProject> ROW_MAPPER = BeanPropertyRowMapper.newInstance(ResumeProject.class);

    @Override
    public boolean save(ResumeProject project) {
        String sql = "INSERT INTO resume_project (resume_id, project_name, role, team_size, start_date, end_date, is_current, description, responsibilities, technologies, project_url, achievements, display_order, is_from_system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, project.getResumeId());
            ps.setString(2, project.getProjectName());
            ps.setString(3, project.getRole());
            ps.setInt(4, project.getTeamSize() != null ? project.getTeamSize() : 0);
            ps.setString(5, project.getStartDate());
            ps.setString(6, project.getEndDate());
            ps.setInt(7, project.getIsCurrent() != null ? project.getIsCurrent() : 0);
            ps.setString(8, project.getDescription());
            ps.setString(9, project.getResponsibilities());
            ps.setString(10, project.getTechnologies());
            ps.setString(11, project.getProjectUrl());
            ps.setString(12, project.getAchievements());
            ps.setInt(13, project.getDisplayOrder() != null ? project.getDisplayOrder() : 0);
            ps.setInt(14, project.getIsFromSystem() != null ? project.getIsFromSystem() : 0);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null;
    }

    @Override
    public boolean update(ResumeProject project) {
        String sql = "UPDATE resume_project SET project_name=?, role=?, team_size=?, start_date=?, end_date=?, is_current=?, description=?, responsibilities=?, technologies=?, project_url=?, achievements=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                project.getProjectName(), project.getRole(), project.getTeamSize(),
                project.getStartDate(), project.getEndDate(), project.getIsCurrent(),
                project.getDescription(), project.getResponsibilities(),
                project.getTechnologies(), project.getProjectUrl(),
                project.getAchievements(), project.getId());
        return rows > 0;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_project WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public ResumeProject findById(Integer id) {
        String sql = "SELECT * FROM resume_project WHERE id=?";
        List<ResumeProject> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ResumeProject> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_project WHERE resume_id=? ORDER BY display_order, id";
        return jdbcTemplate.query(sql, ROW_MAPPER, resumeId);
    }
}
