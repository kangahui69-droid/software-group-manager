package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.UserGroup;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserGroupDAO {
    private static final RowMapper<UserGroup> MAPPER = (rs, row) -> {
        UserGroup relation = new UserGroup();
        relation.setId(rs.getInt("id"));
        relation.setUserId(rs.getInt("user_id"));
        relation.setGroupId(rs.getInt("group_id"));
        relation.setJoinedAt(rs.getTimestamp("joined_at"));
        return relation;
    };
    private final JdbcTemplate jdbc;
    public UserGroupDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<UserGroup> findByUserId(Integer userId) {
        return jdbc.query("SELECT ug.* FROM user_group ug JOIN activity_group g ON g.id=ug.group_id " +
                "WHERE ug.user_id=? ORDER BY ug.joined_at DESC,ug.id DESC", MAPPER, userId);
    }
    public List<UserGroup> findByGroupId(Integer groupId) {
        return jdbc.query("SELECT * FROM user_group WHERE group_id=? ORDER BY joined_at,id", MAPPER, groupId);
    }
    public boolean insertUserToGroup(Integer userId, Integer groupId) {
        return jdbc.update("INSERT INTO user_group (user_id,group_id) VALUES (?,?) " +
                "ON DUPLICATE KEY UPDATE id=id", userId, groupId) >= 0;
    }
    public boolean delete(Integer userId, Integer groupId) {
        return jdbc.update("DELETE FROM user_group WHERE user_id=? AND group_id=?", userId, groupId) == 1;
    }
    public void deleteByGroupId(Integer groupId) {
        jdbc.update("DELETE FROM user_group WHERE group_id=?", groupId);
    }
}
