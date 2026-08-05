package com.softwaregroup.hr.dao;

import com.softwaregroup.hr.model.entity.MemberProfile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * 成员档案数据访问实现
 */
@Repository
public class MemberProfileDAOImpl implements MemberProfileDAO {

    private final JdbcTemplate jdbcTemplate;

    public MemberProfileDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<MemberProfile> ROW_MAPPER = BeanPropertyRowMapper.newInstance(MemberProfile.class);

    @Override
    public boolean insert(MemberProfile profile) {
        String sql = "INSERT INTO member_profile (user_id, student_id, major, grade, phone, email, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getStudentId());
            ps.setString(3, profile.getMajor());
            ps.setString(4, profile.getGrade());
            ps.setString(5, profile.getPhone());
            ps.setString(6, profile.getEmail());
            ps.setInt(7, profile.getStatus() != null ? profile.getStatus() : 1);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null;
    }

    @Override
    public MemberProfile findById(Integer id) {
        String sql = "SELECT * FROM member_profile WHERE id=?";
        List<MemberProfile> list = jdbcTemplate.query(sql, ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public MemberProfile findByUserId(Integer userId) {
        String sql = "SELECT * FROM member_profile WHERE user_id=?";
        List<MemberProfile> list = jdbcTemplate.query(sql, ROW_MAPPER, userId);
        return list.isEmpty() ? null : list.get(0);
    }
}
