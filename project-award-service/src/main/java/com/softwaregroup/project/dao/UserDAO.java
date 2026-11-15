package com.softwaregroup.project.dao;

import com.softwaregroup.project.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDAO {
    private final JdbcTemplate jdbc;

    public UserDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User findById(Integer id) {
        List<User> rows = jdbc.query("SELECT id,username,role FROM user WHERE id=? AND status=1",
                (rs, row) -> {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    return user;
                }, id);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
