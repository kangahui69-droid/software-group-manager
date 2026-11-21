package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIMessageStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
@Profile("!test")
public class JdbcAIMessageStatusDAO implements AIMessageStatusDAO {
    private static final RowMapper<AIMessageStatus> MAPPER = (rs, row) -> {
        AIMessageStatus item = new AIMessageStatus();
        item.setId(rs.getInt("id"));
        item.setMessageId(rs.getString("message_id"));
        item.setUserId((Integer) rs.getObject("user_id"));
        item.setSessionId(rs.getString("session_id"));
        item.setUserMessage(rs.getString("user_message"));
        item.setAiResponse(rs.getString("ai_response"));
        item.setStatus(rs.getString("status"));
        item.setErrorMessage(rs.getString("error_message"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));
        item.setCompletedAt(rs.getTimestamp("completed_at"));
        return item;
    };
    private final JdbcTemplate jdbc;

    public JdbcAIMessageStatusDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public AIMessageStatus findById(Integer id) {
        if (id == null) return null;
        List<AIMessageStatus> rows = jdbc.query("SELECT id,message_id,user_id,session_id,user_message," +
                "ai_response,status,error_message,created_at,updated_at,completed_at " +
                "FROM ai_message_status WHERE id=?", MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public Integer save(AIMessageStatus item) {
        if (item.getMessageId() == null) item.setMessageId(UUID.randomUUID().toString());
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int rows = jdbc.update(connection -> {
            var statement = connection.prepareStatement("INSERT INTO ai_message_status " +
                    "(message_id,user_id,session_id,user_message,ai_response,status,error_message,completed_at) " +
                    "VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, item.getMessageId());
            statement.setObject(2, item.getUserId());
            statement.setString(3, item.getSessionId());
            statement.setString(4, item.getUserMessage());
            statement.setString(5, item.getAiResponse());
            statement.setString(6, item.getStatus());
            statement.setString(7, item.getErrorMessage());
            statement.setTimestamp(8, timestamp(item));
            return statement;
        }, keys);
        if (rows != 1 || keys.getKey() == null) throw new IllegalStateException("Message status insert failed");
        item.setId(keys.getKey().intValue());
        return item.getId();
    }

    @Override
    public void update(AIMessageStatus item) {
        if (item.getId() == null || jdbc.update("UPDATE ai_message_status SET status=?,ai_response=?," +
                        "error_message=?,completed_at=? WHERE id=?", item.getStatus(), item.getAiResponse(),
                item.getErrorMessage(), timestamp(item), item.getId()) != 1) {
            throw new IllegalStateException("Message status update failed");
        }
    }

    private static Timestamp timestamp(AIMessageStatus item) {
        return item.getCompletedAt() == null ? null : new Timestamp(item.getCompletedAt().getTime());
    }
}
