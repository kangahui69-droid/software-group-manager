package com.softwaregroup.user.dao;

import com.softwaregroup.user.model.entity.User;
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
 * 用户数据访问层
 *
 * 使用Spring JDBC代替原来的DBUtil.getConnection()
 */
@Repository
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> USER_ROW_MAPPER = BeanPropertyRowMapper.newInstance(User.class);

    /**
     * 根据用户名和密码查询用户（用于登录）
     */
    public User findByUsernameAndPassword(String username, String encryptedPassword) {
        String sql = "SELECT * FROM user WHERE username = ? AND status = 1";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, username);
        if (users.isEmpty()) {
            return null;
        }
        User user = users.get(0);
        // 密码对比（数据库中存储的是DES加密后的密码）
        if (encryptedPassword.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    /**
     * 根据ID查询用户
     */
    public User findById(Integer id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, id);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * 根据用户名查询用户
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER, username);
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return findByConditions(null, null, null);
    }

    /**
     * 根据条件搜索用户
     */
    public List<User> findByConditions(String keyword, String role, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR name LIKE ? OR email LIKE ? OR phone LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (role != null && !role.trim().isEmpty()) {
            sql.append(" AND role = ?");
            params.add(role);
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY created_at DESC");

        return jdbcTemplate.query(sql.toString(), USER_ROW_MAPPER, params.toArray());
    }

    /**
     * 添加用户
     */
    public Integer insert(User user) {
        String sql = "INSERT INTO user (username, password, name, email, phone, role, status, must_change_password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole());
            ps.setInt(7, user.getStatus() != null ? user.getStatus() : 1);
            ps.setInt(8, Boolean.TRUE.equals(user.getMustChangePassword()) ? 1 : 0);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    /**
     * 更新用户状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE user SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, id) > 0;
    }

    /**
     * 更新用户信息
     */
    public boolean update(User user) {
        String sql = "UPDATE user SET username = ?, name = ?, email = ?, phone = ?, role = ?, status = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus() != null ? user.getStatus() : 1,
                user.getId()) > 0;
    }

    /**
     * 更新用户密码
     */
    public boolean updatePassword(Integer userId, String encryptedPassword) {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        return jdbcTemplate.update(sql, encryptedPassword, userId) > 0;
    }

    /**
     * 重置密码
     */
    public boolean resetPassword(Integer userId, String encryptedPassword) {
        String sql = "UPDATE user SET password = ?, must_change_password = 1 WHERE id = ?";
        return jdbcTemplate.update(sql, encryptedPassword, userId) > 0;
    }

    /**
     * 清除必须修改密码标志
     */
    public boolean clearMustChangePassword(Integer userId) {
        String sql = "UPDATE user SET must_change_password = 0 WHERE id = ?";
        return jdbcTemplate.update(sql, userId) > 0;
    }

    /**
     * 删除用户
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM user WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    /**
     * 检查邮箱是否已存在
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    /**
     * 统计用户总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM user WHERE status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
