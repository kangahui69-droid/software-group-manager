package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIConversation;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;

@Repository
@Profile("!test")
public class JdbcAIConversationDAO implements AIConversationDAO {
    private static final RowMapper<AIConversation> MAPPER = (rs, row) -> {
        AIConversation conversation = new AIConversation();
        conversation.setId(rs.getInt("id"));
        conversation.setUserId((Integer) rs.getObject("user_id"));
        conversation.setSessionId(rs.getString("session_id"));
        conversation.setCreatedAt(rs.getTimestamp("created_at"));
        conversation.setUpdatedAt(rs.getTimestamp("updated_at"));
        return conversation;
    };
    private final JdbcTemplate jdbc;

    public JdbcAIConversationDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public AIConversation findBySessionId(String sessionId) {
        if (sessionId == null) return null;
        List<AIConversation> rows = jdbc.query("SELECT id,user_id,session_id,created_at,updated_at " +
                "FROM ai_conversation WHERE session_id=? LIMIT 2", MAPPER, sessionId);
        if (rows.size() > 1) throw new IllegalStateException("Duplicate AI session ID");
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public Integer save(AIConversation conversation) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int count = jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO ai_conversation (user_id,session_id) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, conversation.getUserId());
            statement.setString(2, conversation.getSessionId());
            return statement;
        }, keys);
        if (count != 1 || keys.getKey() == null) throw new IllegalStateException("AI session insert returned no ID");
        conversation.setId(keys.getKey().intValue());
        return conversation.getId();
    }

    @Override
    public void update(AIConversation conversation) {
        if (conversation.getId() == null || conversation.getUserId() == null) {
            throw new IllegalArgumentException("Conversation ID and owner required");
        }
        int count = jdbc.update("UPDATE ai_conversation SET updated_at=NOW() WHERE id=? AND user_id=?",
                conversation.getId(), conversation.getUserId());
        if (count != 1) throw new IllegalStateException("AI session update affected no row");
    }

    @Override
    public void delete(Integer id) {
        jdbc.update("DELETE FROM ai_conversation WHERE id=?", id);
    }
}
