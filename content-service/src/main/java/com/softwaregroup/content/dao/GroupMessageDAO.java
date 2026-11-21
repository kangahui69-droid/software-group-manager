package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.GroupMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.util.List;

@Repository
public class GroupMessageDAO {
    private static final RowMapper<GroupMessage> MAPPER = (rs, row) -> {
        GroupMessage message = new GroupMessage();
        message.setId(rs.getInt("id"));
        message.setGroupId(rs.getInt("group_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setContent(rs.getString("content"));
        message.setMessageType(rs.getString("message_type"));
        message.setSentAt(rs.getTimestamp("sent_at"));
        return message;
    };
    private final JdbcTemplate jdbc;
    public GroupMessageDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<GroupMessage> findByGroupId(Integer groupId, int limit, int offset) {
        return jdbc.query("SELECT id,group_id,sender_id,content,message_type,sent_at FROM group_message " +
                "WHERE group_id=? AND status='normal' ORDER BY sent_at DESC,id DESC LIMIT ? OFFSET ?",
                MAPPER, groupId, limit, offset);
    }
    public GroupMessage findById(Integer messageId) {
        List<GroupMessage> rows = jdbc.query("SELECT id,group_id,sender_id,content,message_type,sent_at " +
                "FROM group_message WHERE id=? AND status='normal'", MAPPER, messageId);
        return rows.isEmpty() ? null : rows.get(0);
    }
    public int insert(GroupMessage message) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int rows = jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO group_message (group_id,sender_id,content,message_type) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, message.getGroupId());
            statement.setInt(2, message.getSenderId());
            statement.setString(3, message.getContent());
            statement.setString(4, message.getMessageType());
            return statement;
        }, keys);
        if (rows != 1 || keys.getKey() == null) throw new IllegalStateException("Message insert returned no ID");
        message.setId(keys.getKey().intValue());
        return message.getId();
    }
    public boolean delete(Integer messageId) {
        return jdbc.update("DELETE FROM group_message WHERE id=?", messageId) == 1;
    }
    public void deleteByGroupId(Integer groupId) {
        jdbc.update("DELETE FROM group_message WHERE group_id=?", groupId);
    }
}
