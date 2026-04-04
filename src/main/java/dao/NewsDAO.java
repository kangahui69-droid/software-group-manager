package dao;

import model.News;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 新闻数据访问层
 */
public class NewsDAO {

    /**
     * 根据类型查询新闻列表
     */
    public List<News> findByType(String type) {
        List<News> newsList = new ArrayList<>();
        String sql = "SELECT * FROM news WHERE type = ? AND status = 1 ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                newsList.add(mapResultSetToNews(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return newsList;
    }

    /**
     * 根据ID查询新闻
     */
    public News findById(Integer id) {
        String sql = "SELECT * FROM news WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToNews(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 查询所有新闻
     */
    public List<News> findAll() {
        return findByConditions(null, null, null);
    }

    /**
     * 根据条件搜索新闻
     */
    public List<News> findByConditions(String keyword, String type, Integer status) {
        List<News> newsList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM news WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR summary LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (type != null && !type.trim().isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY created_at DESC");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                newsList.add(mapResultSetToNews(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return newsList;
    }

    /**
     * 添加新闻
     */
    public boolean insert(News news) {
        String sql = "INSERT INTO news (title, type, content_path, summary, author_id, activity_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getType());
            pstmt.setString(3, news.getContentPath());
            pstmt.setString(4, news.getSummary());
            pstmt.setObject(5, news.getAuthorId());
            pstmt.setObject(6, news.getActivityId());
            pstmt.setInt(7, news.getStatus() != null ? news.getStatus() : 1);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("数据库写入失败: " + e.getMessage(), e);
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    /**
     * 更新新闻
     */
    public boolean update(News news) {
        String sql = "UPDATE news SET title = ?, type = ?, content_path = ?, summary = ?, status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getType());
            pstmt.setString(3, news.getContentPath());
            pstmt.setString(4, news.getSummary());
            pstmt.setInt(5, news.getStatus());
            pstmt.setInt(6, news.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("数据库更新失败: " + e.getMessage(), e);
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    /**
     * 更新新闻状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE news SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 将ResultSet映射为News对象
     */
    private News mapResultSetToNews(ResultSet rs) throws SQLException {
        News news = new News();
        news.setId(rs.getInt("id"));
        news.setTitle(rs.getString("title"));
        news.setType(rs.getString("type"));
        news.setContentPath(rs.getString("content_path"));
        news.setSummary(rs.getString("summary"));
        news.setAuthorId(rs.getInt("author_id"));
        news.setActivityId((Integer) rs.getObject("activity_id"));
        news.setStatus(rs.getInt("status"));
        news.setCreatedAt(rs.getTimestamp("created_at"));
        news.setUpdatedAt(rs.getTimestamp("updated_at"));
        return news;
    }

    /**
     * 根据活动ID查询新闻
     */
    public News findByActivityId(Integer activityId) {
        String sql = "SELECT * FROM news WHERE activity_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToNews(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 检查指定活动是否已生成新闻
     */
    public boolean existsByActivityId(Integer activityId) {
        String sql = "SELECT COUNT(*) FROM news WHERE activity_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    /**
     * 关闭资源
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
            if (pstmt != null)
                pstmt.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
