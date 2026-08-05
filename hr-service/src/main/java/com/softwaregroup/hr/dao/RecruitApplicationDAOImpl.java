package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.RecruitApplication;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 招新申请数据访问实现
 */
@Repository
public class RecruitApplicationDAOImpl implements RecruitApplicationDAO {

    private final JdbcTemplate jdbcTemplate;

    public RecruitApplicationDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<RecruitApplication> ROW_MAPPER = BeanPropertyRowMapper.newInstance(RecruitApplication.class);

    @Override
    public boolean insert(RecruitApplication app) {
        String sql = "INSERT INTO recruit_application (name, student_id, major, grade, phone, email, reason, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        int rows = jdbcTemplate.update(sql,
                app.getName(), app.getStudentId(), app.getMajor(), app.getGrade(),
                app.getPhone(), app.getEmail(), app.getReason(), app.getStatus());
        return rows > 0;
    }

    @Override
    public boolean update(RecruitApplication app) {
        String sql = "UPDATE recruit_application SET name=?, student_id=?, major=?, grade=?, phone=?, email=?, reason=?, status=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                app.getName(), app.getStudentId(), app.getMajor(), app.getGrade(),
                app.getPhone(), app.getEmail(), app.getReason(), app.getStatus(), app.getId());
        return rows > 0;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM recruit_application WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public RecruitApplication findById(Integer id) {
        String sql = "SELECT * FROM recruit_application WHERE id=?";
        List<RecruitApplication> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<RecruitApplication> findByConditions(String keyword, Integer year, String status, Integer round) {
        StringBuilder sql = new StringBuilder("SELECT * FROM recruit_application WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (name LIKE ? OR student_id LIKE ? OR major LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (year != null) {
            sql.append(" AND grade LIKE ?");
            params.add(year + "%");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status=?");
            params.add(status);
        }
        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    @Override
    public int countPending() {
        String sql = "SELECT COUNT(*) FROM recruit_application WHERE status=1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public List<Integer> findAllYears() {
        String sql = "SELECT DISTINCT CAST(grade AS SIGNED) as year FROM recruit_application WHERE grade IS NOT NULL AND grade != '' ORDER BY year DESC";
        return jdbcTemplate.queryForList(sql, Integer.class);
    }
}
