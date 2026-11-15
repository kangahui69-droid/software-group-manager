package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.ActivityGroup;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.sql.Statement;
import java.util.List;

@Repository
public class ActivityGroupDAO {
    private static final RowMapper<ActivityGroup> MAPPER = (rs, row) -> {
        ActivityGroup group = new ActivityGroup();
        group.setId(rs.getInt("id"));
        group.setActivityId((Integer) rs.getObject("activity_id"));
        group.setGroupName(rs.getString("group_name"));
        group.setGroupOwnerId(rs.getInt("group_owner_id"));
        group.setCreatedAt(rs.getTimestamp("created_at"));
        group.setUpdatedAt(rs.getTimestamp("updated_at"));
        group.setMuted(rs.getBoolean("is_muted"));
        group.setMutedUntil(rs.getTimestamp("muted_until"));
        group.setMuteReason(rs.getString("mute_reason"));
        return group;
    };
    private final JdbcTemplate jdbc;
    public ActivityGroupDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<ActivityGroup> findAll() {
        return jdbc.query("SELECT * FROM activity_group ORDER BY created_at DESC,id DESC", MAPPER);
    }
    public ActivityGroup findById(Integer id) {
        List<ActivityGroup> rows = jdbc.query("SELECT * FROM activity_group WHERE id=?", MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }
    public List<ActivityGroup> findByOwnerId(Integer ownerId) {
        return jdbc.query("SELECT * FROM activity_group WHERE group_owner_id=? ORDER BY created_at DESC,id DESC",
                MAPPER, ownerId);
    }
    public int insert(ActivityGroup group) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int rows = jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO activity_group (activity_id,group_name,group_owner_id) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setObject(1, group.getActivityId());
            statement.setString(2, group.getGroupName());
            statement.setInt(3, group.getGroupOwnerId());
            return statement;
        }, keys);
        if (rows != 1 || keys.getKey() == null) throw new IllegalStateException("Group insert returned no ID");
        group.setId(keys.getKey().intValue());
        return group.getId();
    }
    public boolean update(ActivityGroup group) {
        return jdbc.update("UPDATE activity_group SET group_name=? WHERE id=?",
                group.getGroupName(), group.getId()) == 1;
    }
    public boolean delete(Integer id) { return jdbc.update("DELETE FROM activity_group WHERE id=?", id) == 1; }
    public boolean muteGroup(Integer groupId, java.util.Date until, String reason) {
        return jdbc.update("UPDATE activity_group SET is_muted=1,muted_until=?,mute_reason=? WHERE id=?",
                until == null ? null : new Timestamp(until.getTime()), reason, groupId) == 1;
    }
    public boolean unmuteGroup(Integer groupId) {
        return jdbc.update("UPDATE activity_group SET is_muted=0,muted_until=NULL,mute_reason=NULL WHERE id=?",
                groupId) == 1;
    }
}
