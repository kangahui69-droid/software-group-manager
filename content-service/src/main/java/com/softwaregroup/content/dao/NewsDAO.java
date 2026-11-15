package com.softwaregroup.content.dao;

import com.softwaregroup.content.model.News;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NewsDAO {
    private static final RowMapper<News> MAPPER = (rs, row) -> {
        News news = new News();
        news.setId(rs.getInt("id"));
        news.setTitle(rs.getString("title"));
        news.setType(rs.getString("type"));
        news.setContentPath(rs.getString("content_path"));
        news.setSummary(rs.getString("summary"));
        news.setAuthorId((Integer) rs.getObject("author_id"));
        news.setStatus(rs.getInt("status"));
        news.setCreatedAt(rs.getTimestamp("created_at"));
        news.setUpdatedAt(rs.getTimestamp("updated_at"));
        return news;
    };
    private final JdbcTemplate jdbc;
    public NewsDAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<News> findByConditions(String keyword, String type, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM news WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (title LIKE ? OR summary LIKE ?)");
            args.add("%" + keyword.trim() + "%");
            args.add("%" + keyword.trim() + "%");
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND type=?");
            args.add(type.toLowerCase(java.util.Locale.ROOT));
        }
        if (status != null) {
            sql.append(" AND status=?");
            args.add(status);
        }
        sql.append(" ORDER BY created_at DESC,id DESC");
        return jdbc.query(sql.toString(), MAPPER, args.toArray());
    }
    public List<News> findByType(String type) { return findByConditions(null, type, News.STATUS_PUBLISHED); }
    public News findById(Integer id) {
        List<News> rows = jdbc.query("SELECT * FROM news WHERE id=?", MAPPER, id);
        return rows.isEmpty() ? null : rows.get(0);
    }
    public boolean insert(News news) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        int rows = jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO news (title,type,content_path,summary,author_id,status) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, news.getTitle());
            statement.setString(2, news.getType());
            statement.setString(3, news.getContentPath());
            statement.setString(4, news.getSummary());
            statement.setObject(5, news.getAuthorId());
            statement.setInt(6, news.getStatus());
            return statement;
        }, keys);
        if (rows == 1) {
            if (keys.getKey() == null) throw new IllegalStateException("News insert returned no ID");
            news.setId(keys.getKey().intValue());
            return true;
        }
        return false;
    }
    public boolean update(News news) {
        return jdbc.update("UPDATE news SET title=?,type=?,content_path=?,summary=? WHERE id=?",
                news.getTitle(), news.getType(), news.getContentPath(), news.getSummary(), news.getId()) == 1;
    }
    public boolean updateStatus(Integer id, Integer status) {
        return jdbc.update("UPDATE news SET status=? WHERE id=?", status, id) == 1;
    }
}
