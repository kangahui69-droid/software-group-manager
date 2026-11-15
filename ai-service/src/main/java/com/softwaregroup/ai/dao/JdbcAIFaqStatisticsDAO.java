package com.softwaregroup.ai.dao;

import com.softwaregroup.ai.model.entity.AIFaqStatistics;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
@Profile("!test")
public class JdbcAIFaqStatisticsDAO implements AIFaqStatisticsDAO {
    private static final RowMapper<AIFaqStatistics> MAPPER = (rs, row) -> {
        AIFaqStatistics item = new AIFaqStatistics();
        item.setId(rs.getInt("id"));
        item.setQuestionHash(rs.getString("question_hash"));
        item.setNormalizedQuestion(rs.getString("normalized_question"));
        item.setQueryCount((Integer) rs.getObject("query_count"));
        BigDecimal rating = rs.getBigDecimal("avg_rating");
        item.setAvgRating(rating == null ? null : rating.doubleValue());
        item.setLastQueryAt(rs.getTimestamp("last_query_at"));
        return item;
    };
    private final JdbcTemplate jdbc;

    public JdbcAIFaqStatisticsDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public AIFaqStatistics findByHash(String hash) {
        if (hash == null) return null;
        List<AIFaqStatistics> rows = jdbc.query("SELECT id,question_hash,normalized_question,query_count," +
                "avg_rating,last_query_at FROM ai_faq_statistics WHERE question_hash=?", MAPPER, hash);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<AIFaqStatistics> findTopQuestions(int limit) {
        if (limit <= 0) return List.of();
        return jdbc.query("SELECT id,question_hash,normalized_question,query_count,avg_rating,last_query_at " +
                "FROM ai_faq_statistics ORDER BY query_count DESC,id LIMIT ?", MAPPER, limit);
    }

    @Override
    public List<AIFaqStatistics> findAllOrderByCount() {
        return jdbc.query("SELECT id,question_hash,normalized_question,query_count,avg_rating,last_query_at " +
                "FROM ai_faq_statistics ORDER BY query_count DESC,id", MAPPER);
    }

    @Override
    public Integer save(AIFaqStatistics item) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        try {
            int rows = jdbc.update(connection -> {
                var statement = connection.prepareStatement("INSERT INTO ai_faq_statistics " +
                        "(question_hash,normalized_question,query_count,avg_rating,last_query_at) " +
                        "VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, item.getQuestionHash());
                statement.setString(2, item.getNormalizedQuestion());
                statement.setInt(3, item.getQueryCount() == null ? 1 : item.getQueryCount());
                statement.setObject(4, item.getAvgRating());
                statement.setTimestamp(5, timestamp(item));
                return statement;
            }, keys);
            if (rows != 1 || keys.getKey() == null) throw new IllegalStateException("FAQ insert failed");
            item.setId(keys.getKey().intValue());
            return item.getId();
        } catch (DuplicateKeyException duplicate) {
            jdbc.update("UPDATE ai_faq_statistics SET query_count=query_count+1,last_query_at=? " +
                    "WHERE question_hash=?", timestamp(item), item.getQuestionHash());
            AIFaqStatistics existing = findByHash(item.getQuestionHash());
            if (existing == null) throw duplicate;
            item.setId(existing.getId());
            return item.getId();
        }
    }

    @Override
    public void update(AIFaqStatistics item) {
        if (item.getId() == null || jdbc.update("UPDATE ai_faq_statistics SET query_count=query_count+1," +
                        "avg_rating=?,last_query_at=? WHERE id=?", item.getAvgRating(), timestamp(item),
                item.getId()) != 1) {
            throw new IllegalStateException("FAQ update failed");
        }
    }

    private static Timestamp timestamp(AIFaqStatistics item) {
        return item.getLastQueryAt() == null ? null : new Timestamp(item.getLastQueryAt().getTime());
    }
}
