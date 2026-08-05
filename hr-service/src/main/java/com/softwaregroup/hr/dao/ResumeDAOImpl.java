package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.Resume;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 简历主表数据访问实现
 */
@Repository
public class ResumeDAOImpl implements ResumeDAO {

    private final JdbcTemplate jdbcTemplate;

    public ResumeDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<Resume> ROW_MAPPER = BeanPropertyRowMapper.newInstance(Resume.class);

    @Override
    public boolean save(Resume resume) {
        String sql = "INSERT INTO resume (user_id, resume_name, template_style, summary, career_objective, phone, email, wechat, github_url, blog_url, is_default, status, deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, resume.getUserId());
            ps.setString(2, resume.getResumeName());
            ps.setString(3, resume.getTemplateStyle());
            ps.setString(4, resume.getSummary());
            ps.setString(5, resume.getCareerObjective());
            ps.setString(6, resume.getPhone());
            ps.setString(7, resume.getEmail());
            ps.setString(8, resume.getWechat());
            ps.setString(9, resume.getGithubUrl());
            ps.setString(10, resume.getBlogUrl());
            ps.setInt(11, resume.getIsDefault() != null ? resume.getIsDefault() : 0);
            ps.setInt(12, resume.getStatus() != null ? resume.getStatus() : 1);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null;
    }

    @Override
    public boolean update(Resume resume) {
        String sql = "UPDATE resume SET resume_name=?, template_style=?, summary=?, career_objective=?, phone=?, email=?, wechat=?, github_url=?, blog_url=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                resume.getResumeName(), resume.getTemplateStyle(), resume.getSummary(),
                resume.getCareerObjective(), resume.getPhone(), resume.getEmail(),
                resume.getWechat(), resume.getGithubUrl(), resume.getBlogUrl(), resume.getId());
        return rows > 0;
    }

    @Override
    public boolean softDelete(Integer id) {
        String sql = "UPDATE resume SET deleted=1 WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public boolean setDefaultResume(Integer resumeId, Integer userId) {
        jdbcTemplate.update("UPDATE resume SET is_default=0 WHERE user_id=?", userId);
        String sql = "UPDATE resume SET is_default=1 WHERE id=? AND user_id=?";
        return jdbcTemplate.update(sql, resumeId, userId) > 0;
    }

    @Override
    public boolean restore(Integer id) {
        String sql = "UPDATE resume SET deleted=0 WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public boolean hardDelete(Integer id) {
        String sql = "DELETE FROM resume WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Resume findById(Integer id) {
        return findById(id, false);
    }

    @Override
    public Resume findById(Integer id, boolean includeDeleted) {
        String sql = includeDeleted
                ? "SELECT * FROM resume WHERE id=?"
                : "SELECT * FROM resume WHERE id=? AND deleted=0";
        List<Resume> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Resume> findByUserId(Integer userId) {
        String sql = "SELECT * FROM resume WHERE user_id=? AND deleted=0 ORDER BY is_default DESC, id DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    @Override
    public List<Resume> findDeletedByUserId(Integer userId) {
        String sql = "SELECT * FROM resume WHERE user_id=? AND deleted=1 ORDER BY id DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }
}
