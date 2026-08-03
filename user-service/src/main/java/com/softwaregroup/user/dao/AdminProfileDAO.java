package com.softwaregroup.user.dao;

import com.softwaregroup.user.model.entity.AdminProfile;
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
 * 管理员档案数据访问层
 */
@Repository
public class AdminProfileDAO {

    private final JdbcTemplate jdbcTemplate;

    public AdminProfileDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<AdminProfile> PROFILE_ROW_MAPPER = BeanPropertyRowMapper.newInstance(AdminProfile.class);

    /**
     * 根据用户ID查询档案
     */
    public AdminProfile findByUserId(Integer userId) {
        String sql = "SELECT * FROM admin_profile WHERE user_id = ?";
        List<AdminProfile> profiles = jdbcTemplate.query(sql, PROFILE_ROW_MAPPER, userId);
        return profiles.isEmpty() ? null : profiles.get(0);
    }

    /**
     * 添加档案
     */
    public Integer insert(AdminProfile profile) {
        String sql = "INSERT INTO admin_profile (user_id, title, department, education, research_area, bio, avatar_file_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getTitle());
            ps.setString(3, profile.getDepartment());
            ps.setString(4, profile.getEducation());
            ps.setString(5, profile.getResearchArea());
            ps.setString(6, profile.getBio());
            ps.setObject(7, profile.getAvatarFileId());
            ps.setInt(8, profile.getStatus() != null ? profile.getStatus() : 1);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    /**
     * 更新档案
     */
    public boolean update(AdminProfile profile) {
        String sql = "UPDATE admin_profile SET title = ?, department = ?, education = ?, research_area = ?, bio = ?, avatar_file_id = ? WHERE user_id = ?";
        return jdbcTemplate.update(sql,
                profile.getTitle(),
                profile.getDepartment(),
                profile.getEducation(),
                profile.getResearchArea(),
                profile.getBio(),
                profile.getAvatarFileId(),
                profile.getUserId()) > 0;
    }

    /**
     * 添加或更新档案
     */
    public boolean saveOrUpdate(AdminProfile profile) {
        AdminProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            profile.setId(existing.getId());
            return update(profile);
        } else {
            return insert(profile) != null;
        }
    }
}
