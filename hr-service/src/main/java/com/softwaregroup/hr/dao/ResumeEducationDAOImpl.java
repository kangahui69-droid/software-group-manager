package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeEducation;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 简历教育经历数据访问实现
 */
@Repository
public class ResumeEducationDAOImpl implements ResumeEducationDAO {

    private final JdbcTemplate jdbcTemplate;

    public ResumeEducationDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ResumeEducation> ROW_MAPPER = BeanPropertyRowMapper.newInstance(ResumeEducation.class);

    @Override
    public boolean save(ResumeEducation education) {
        String sql = "INSERT INTO resume_education (resume_id, school_name, major, degree, start_date, end_date, is_current, description, display_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, education.getResumeId());
            ps.setString(2, education.getSchoolName());
            ps.setString(3, education.getMajor());
            ps.setString(4, education.getDegree());
            ps.setString(5, education.getStartDate());
            ps.setString(6, education.getEndDate());
            ps.setInt(7, education.getIsCurrent() != null ? education.getIsCurrent() : 0);
            ps.setString(8, education.getDescription());
            ps.setInt(9, education.getDisplayOrder() != null ? education.getDisplayOrder() : 0);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null;
    }

    @Override
    public boolean update(ResumeEducation education) {
        String sql = "UPDATE resume_education SET school_name=?, major=?, degree=?, start_date=?, end_date=?, is_current=?, description=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                education.getSchoolName(), education.getMajor(), education.getDegree(),
                education.getStartDate(), education.getEndDate(),
                education.getIsCurrent(), education.getDescription(), education.getId());
        return rows > 0;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_education WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public ResumeEducation findById(Integer id) {
        String sql = "SELECT * FROM resume_education WHERE id=?";
        List<ResumeEducation> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ResumeEducation> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_education WHERE resume_id=? ORDER BY display_order, id";
        return jdbcTemplate.query(sql, ROW_MAPPER, resumeId);
    }
}
