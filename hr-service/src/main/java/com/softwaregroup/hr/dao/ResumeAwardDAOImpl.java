package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.ResumeAward;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 简历获奖情况数据访问实现
 */
@Repository
public class ResumeAwardDAOImpl implements ResumeAwardDAO {

    private final JdbcTemplate jdbcTemplate;

    public ResumeAwardDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<ResumeAward> ROW_MAPPER = BeanPropertyRowMapper.newInstance(ResumeAward.class);

    @Override
    public boolean save(ResumeAward award) {
        String sql = "INSERT INTO resume_award (resume_id, award_name, competition_name, award_level, award_date, award_org, description, display_order, is_from_system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, award.getResumeId());
            ps.setString(2, award.getAwardName());
            ps.setString(3, award.getCompetitionName());
            ps.setString(4, award.getAwardLevel());
            ps.setString(5, award.getAwardDate());
            ps.setString(6, award.getAwardOrg());
            ps.setString(7, award.getDescription());
            ps.setInt(8, award.getDisplayOrder() != null ? award.getDisplayOrder() : 0);
            ps.setInt(9, award.getIsFromSystem() != null ? award.getIsFromSystem() : 0);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null;
    }

    @Override
    public boolean update(ResumeAward award) {
        String sql = "UPDATE resume_award SET award_name=?, competition_name=?, award_level=?, award_date=?, award_org=?, description=? WHERE id=?";
        int rows = jdbcTemplate.update(sql,
                award.getAwardName(), award.getCompetitionName(),
                award.getAwardLevel(), award.getAwardDate(),
                award.getAwardOrg(), award.getDescription(), award.getId());
        return rows > 0;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM resume_award WHERE id=?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public ResumeAward findById(Integer id) {
        String sql = "SELECT * FROM resume_award WHERE id=?";
        List<ResumeAward> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<ResumeAward> findByResumeId(Integer resumeId) {
        String sql = "SELECT * FROM resume_award WHERE resume_id=? ORDER BY display_order, id";
        return jdbcTemplate.query(sql, ROW_MAPPER, resumeId);
    }
}
