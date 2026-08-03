package com.softwaregroup.user.dao;

import com.softwaregroup.user.model.entity.MemberProfile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

/**
 * 成员档案数据访问层
 */
@Repository
public class MemberProfileDAO {

    private final JdbcTemplate jdbcTemplate;

    public MemberProfileDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<MemberProfile> PROFILE_ROW_MAPPER = BeanPropertyRowMapper.newInstance(MemberProfile.class);

    /**
     * 根据用户ID查询档案
     */
    public MemberProfile findByUserId(Integer userId) {
        String sql = "SELECT * FROM member_profile WHERE user_id = ?";
        List<MemberProfile> profiles = jdbcTemplate.query(sql, PROFILE_ROW_MAPPER, userId);
        return profiles.isEmpty() ? null : profiles.get(0);
    }

    /**
     * 添加档案
     */
    public Integer insert(MemberProfile profile) {
        String sql = "INSERT INTO member_profile (user_id, birthday, student_id, major, grade, avatar_file_id, introduction, github, blog) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setTimestamp(2, profile.getBirthday() != null ? new Timestamp(profile.getBirthday().getTime()) : null);
            ps.setString(3, profile.getStudentId() != null ? profile.getStudentId() : "");
            ps.setString(4, profile.getMajor() != null ? profile.getMajor() : "");
            ps.setString(5, profile.getGrade() != null ? profile.getGrade() : "");
            ps.setObject(6, profile.getAvatarFileId());
            ps.setString(7, profile.getIntroduction());
            ps.setString(8, profile.getGithub());
            ps.setString(9, profile.getBlog());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    /**
     * 更新档案
     */
    public boolean update(MemberProfile profile) {
        String sql = "UPDATE member_profile SET birthday = ?, student_id = ?, major = ?, grade = ?, avatar_file_id = ?, introduction = ?, github = ?, blog = ? WHERE user_id = ?";
        return jdbcTemplate.update(sql,
                profile.getBirthday() != null ? new Timestamp(profile.getBirthday().getTime()) : null,
                profile.getStudentId(),
                profile.getMajor(),
                profile.getGrade(),
                profile.getAvatarFileId(),
                profile.getIntroduction(),
                profile.getGithub(),
                profile.getBlog(),
                profile.getUserId()) > 0;
    }

    /**
     * 添加或更新档案
     */
    public boolean saveOrUpdate(MemberProfile profile) {
        MemberProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            profile.setId(existing.getId());
            return update(profile);
        } else {
            return insert(profile) != null;
        }
    }
}
