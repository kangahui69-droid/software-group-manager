package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIKnowledgeBase;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;

@Repository
@Profile("!test")
public class JdbcAIKnowledgeBaseDAO implements AIKnowledgeBaseDAO {
    private static final RowMapper<AIKnowledgeBase> MAPPER = (rs, row) -> {
        AIKnowledgeBase item = new AIKnowledgeBase();
        item.setId(rs.getInt("id"));
        item.setCategory(rs.getString("category"));
        item.setQuestion(rs.getString("question"));
        item.setAnswer(rs.getString("answer"));
        item.setKeywords(rs.getString("keywords"));
        item.setStatus((Integer) rs.getObject("status"));
        return item;
    };
    private final JdbcTemplate jdbc;

    public JdbcAIKnowledgeBaseDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<AIKnowledgeBase> findAll() {
        return jdbc.query("SELECT id,category,question,answer,keywords,status FROM ai_knowledge_base ORDER BY id", MAPPER);
    }

    @Override
    public AIKnowledgeBase findById(Integer id) {
        if (id == null) return null;
        List<AIKnowledgeBase> rows = jdbc.query("SELECT id,category,question,answer,keywords,status " +
                "FROM ai_knowledge_base WHERE id=?", MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public Integer save(AIKnowledgeBase item) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int rows = jdbc.update(connection -> {
            var statement = connection.prepareStatement("INSERT INTO ai_knowledge_base " +
                    "(category,question,answer,keywords,status) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, item.getCategory());
            statement.setString(2, item.getQuestion());
            statement.setString(3, item.getAnswer());
            statement.setString(4, item.getKeywords());
            statement.setObject(5, item.getStatus());
            return statement;
        }, keys);
        if (rows != 1 || keys.getKey() == null) throw new IllegalStateException("Knowledge insert failed");
        item.setId(keys.getKey().intValue());
        return item.getId();
    }

    @Override
    public void update(AIKnowledgeBase item) {
        if (item.getId() == null || jdbc.update("UPDATE ai_knowledge_base SET category=?,question=?," +
                        "answer=?,keywords=?,status=? WHERE id=?", item.getCategory(), item.getQuestion(),
                item.getAnswer(), item.getKeywords(), item.getStatus(), item.getId()) != 1) {
            throw new IllegalStateException("Knowledge update failed");
        }
    }

    @Override
    public void delete(Integer id) {
        if (id == null || jdbc.update("DELETE FROM ai_knowledge_base WHERE id=?", id) != 1) {
            throw new IllegalStateException("Knowledge delete failed");
        }
    }
}
