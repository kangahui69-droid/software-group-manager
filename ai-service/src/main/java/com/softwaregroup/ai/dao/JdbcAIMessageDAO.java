package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;

@Repository
@Profile("!test")
public class JdbcAIMessageDAO implements AIMessageDAO {
    private static final RowMapper<AIMessage> MAPPER = (rs, row) -> {
        AIMessage message = new AIMessage();
        message.setId(rs.getInt("id"));
        message.setConversationId(rs.getInt("conversation_id"));
        message.setRole(rs.getString("role"));
        message.setContent(rs.getString("content"));
        message.setCreatedAt(rs.getTimestamp("created_at"));
        return message;
    };
    private final JdbcTemplate jdbc;

    public JdbcAIMessageDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public AIMessage findById(Integer id) {
        List<AIMessage> rows = jdbc.query("SELECT id,conversation_id,role,content,created_at " +
                "FROM ai_message WHERE id=?", MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<AIMessage> findByConversationId(Integer conversationId) {
        return jdbc.query("SELECT id,conversation_id,role,content,created_at FROM ai_message " +
                "WHERE conversation_id=? ORDER BY created_at,id", MAPPER, conversationId);
    }

    @Override
    public Integer save(AIMessage message) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int count = jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO ai_message (conversation_id,role,content) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, message.getConversationId());
            statement.setString(2, message.getRole());
            statement.setString(3, message.getContent());
            return statement;
        }, keys);
        if (count != 1 || keys.getKey() == null) throw new IllegalStateException("AI message insert returned no ID");
        message.setId(keys.getKey().intValue());
        return message.getId();
    }

    @Override
    public void deleteByConversationId(Integer conversationId) {
        jdbc.update("DELETE FROM ai_message WHERE conversation_id=?", conversationId);
    }
}
