package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeSkill;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 简历技能特长数据访问实现
 */
@Repository
public class ResumeSkillDAOImpl implements ResumeSkillDAO {

    private final JdbcTemplate jdbcTemplate;

    public ResumeSkillDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ResumeSkill> ROW_MAPPER = BeanPropertyRowMapper.newInstance(ResumeSkill.class);

    @Override
    public boolean save(ResumeSkill skill) {
        String sql = "INSERT INTO resume_skill (resume_id, skill_name, proficiency, proficiency_score, category, description, display_order) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, skill.getResumeId());
            ps.setString(2, skill.getSkillName());
            ps.setString(3, skill.getProficiency());
            ps.setInt(4, skill.getProficiencyScore() != null ? skill.getProficiencyScore() : 50);
            ps.setString(5, skill.getCategory());
            ps.setString(6, skill.getDescription());
            ps.setInt(7, skill.getDisplayOrder() != null ? skill.getDisplayOrder() : 0);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null;
    }

    @Override
    public boolean update(ResumeSkill skill) {
        String sql = "UPDATE resume_skill SET skill_name=?, proficiency=?, proficiency_score=?, category=?, description=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                skill.getSkillName(), skill.getProficiency(),
                skill.getProficiencyScore(), skill.getCategory(),
                skill.getDescription(), skill.getId());
        return rows > 0;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_skill WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public ResumeSkill findById(Integer id) {
        String sql = "SELECT * FROM resume_skill WHERE id=?";
        List<ResumeSkill> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ResumeSkill> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_skill WHERE resume_id=? ORDER BY display_order, id";
        return jdbcTemplate.query(sql, ROW_MAPPER, resumeId);
    }
}
