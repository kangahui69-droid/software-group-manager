package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.GroupMember;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class GroupMemberDAO {
    private static final RowMapper<GroupMember> MAPPER = (rs, row) -> {
        GroupMember member = new GroupMember();
        member.setId(rs.getInt("id"));
        member.setGroupId(rs.getInt("group_id"));
        member.setUserId(rs.getInt("user_id"));
        member.setRole(rs.getString("role"));
        member.setJoinedAt(rs.getTimestamp("joined_at"));
        member.setMuted(rs.getBoolean("muted"));
        member.setMutedUntil(rs.getTimestamp("muted_until"));
        member.setMuteReason(rs.getString("mute_reason"));
        return member;
    };
    private final JdbcTemplate jdbc;
    public GroupMemberDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public boolean isMember(Integer groupId, Integer userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM group_member WHERE group_id=? AND user_id=?",
                Integer.class, groupId, userId);
        return count != null && count > 0;
    }
    public boolean isOwner(Integer groupId, Integer userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM group_member WHERE group_id=? AND user_id=? " +
                "AND role='OWNER'", Integer.class, groupId, userId);
        return count != null && count > 0;
    }
    public boolean isMuted(Integer groupId, Integer userId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM group_member WHERE group_id=? AND user_id=? " +
                "AND muted=1 AND (muted_until IS NULL OR muted_until>CURRENT_TIMESTAMP)",
                Integer.class, groupId, userId);
        return count != null && count > 0;
    }
    public boolean muteMember(Integer groupId, Integer userId, java.util.Date until, String reason) {
        return jdbc.update("UPDATE group_member SET muted=1,muted_until=?,mute_reason=? " +
                        "WHERE group_id=? AND user_id=? AND role<>'OWNER'",
                until == null ? null : new java.sql.Timestamp(until.getTime()), reason, groupId, userId) == 1;
    }
    public boolean unmuteMember(Integer groupId, Integer userId) {
        return jdbc.update("UPDATE group_member SET muted=0,muted_until=NULL,mute_reason=NULL " +
                        "WHERE group_id=? AND user_id=? AND role<>'OWNER'",
                groupId, userId) == 1;
    }
    public boolean insertMember(Integer groupId, Integer userId) {
        return jdbc.update("INSERT INTO group_member (group_id,user_id,role) VALUES (?,?,'MEMBER')",
                groupId, userId) == 1;
    }
    public boolean insertOwner(Integer groupId, Integer userId) {
        return jdbc.update("INSERT INTO group_member (group_id,user_id,role) VALUES (?,?,'OWNER')",
                groupId, userId) == 1;
    }
    public boolean delete(Integer groupId, Integer userId) {
        return jdbc.update("DELETE FROM group_member WHERE group_id=? AND user_id=? AND role<>'OWNER'",
                groupId, userId) == 1;
    }
    public void deleteByGroupId(Integer groupId) {
        jdbc.update("DELETE FROM group_member WHERE group_id=?", groupId);
    }
    public List<GroupMember> findByGroupId(Integer groupId) {
        return jdbc.query("SELECT id,group_id,user_id,role,joined_at,muted,muted_until,mute_reason " +
                "FROM group_member WHERE group_id=? " +
                "ORDER BY joined_at,id", MAPPER, groupId);
    }
}
