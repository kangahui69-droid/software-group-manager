package com.softwaregroup.monitor.dao;

import com.softwaregroup.monitor.model.ProblemReport;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 问题报告数据访问实现
 */
@Repository
public class ProblemReportDAOImpl implements ProblemReportDAO {

    private final JdbcTemplate jdbcTemplate;

    public ProblemReportDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ProblemReport> ROW_MAPPER = BeanPropertyRowMapper.newInstance(ProblemReport.class);

    @Override
    public int insert(ProblemReport problem) {
        String sql = "INSERT INTO problem_report (title, content, category, status, reporter_type, user_id, reporter_name, reporter_contact, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, problem.getTitle());
            ps.setString(2, problem.getContent());
            ps.setString(3, problem.getCategory());
            ps.setString(4, problem.getStatus());
            ps.setString(5, problem.getReporterType());
            ps.setObject(6, problem.getUserId());
            ps.setString(7, problem.getReporterName());
            ps.setString(8, problem.getReporterContact());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : 0;
    }

    @Override
    public boolean updateCategoryAndStatus(Integer id, String category, String status, String comment, Integer operatorId) {
        String sql = "UPDATE problem_report SET category=?, status=?, admin_comment=?, updated_at=NOW() WHERE id=?";
        return jdbcTemplate.update(sql, category, status, comment, id) > 0;
    }

    @Override
    public boolean updateAdminComment(Integer id, String comment) {
        String sql = "UPDATE problem_report SET admin_comment=?, updated_at=NOW() WHERE id=?";
        return jdbcTemplate.update(sql, comment, id) > 0;
    }

    @Override
    public ProblemReport findById(Integer id) {
        String sql = "SELECT * FROM problem_report WHERE id=?";
        List<ProblemReport> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ProblemReport> findAll() {
        String sql = "SELECT * FROM problem_report ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public List<ProblemReport> findByCategory(String category) {
        String sql = "SELECT * FROM problem_report WHERE category=? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, category);
    }

    @Override
    public List<ProblemReport> findByStatus(String status) {
        String sql = "SELECT * FROM problem_report WHERE status=? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, status);
    }

    @Override
    public List<ProblemReport> findByUserId(Integer userId) {
        String sql = "SELECT * FROM problem_report WHERE user_id=? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ROW_MAPPER, userId);
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM problem_report WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM problem_report WHERE status='PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int countByCategory(String category) {
        String sql = "SELECT COUNT(*) FROM problem_report WHERE category=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, category);
        return count != null ? count : 0;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM problem_report WHERE status=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }
}
