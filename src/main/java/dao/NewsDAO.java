package dao;

import model.News;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
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
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getType());
            pstmt.setString(3, news.getContentPath());
            pstmt.setString(4, news.getSummary());
            pstmt.setObject(5, news.getAuthorId());
            pstmt.setObject(6, news.getActivityId());
            pstmt.setInt(7, news.getStatus() != null ? news.getStatus() : 1);
            if (pstmt.executeUpdate() <= 0) { conn.rollback(); return false; }
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) news.setId(keys.getInt(1));
            }
            if (news.getId() == null) throw new SQLException("新闻 ID 获取失败");
            if (news.getStatus() == null || news.getStatus() == 1) {
                new PublicationNotificationDAO().recordPublication(conn,
                        PublicationNotificationDAO.NEWS, news.getId(), news.getTitle());
            }
            conn.commit();
            return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库写入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新新闻
     */
    public boolean update(News news) {
        String sql = "UPDATE news SET title = ?, type = ?, content_path = ?, summary = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
            int oldStatus = lockedStatus(conn, news.getId());
            if (oldStatus < 0) { conn.rollback(); return false; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, news.getTitle());
            pstmt.setString(2, news.getType());
            pstmt.setString(3, news.getContentPath());
            pstmt.setString(4, news.getSummary());
            pstmt.setInt(5, news.getStatus());
            pstmt.setInt(6, news.getId());
            if (pstmt.executeUpdate() == 0) { conn.rollback(); return false; }
            }
            applyPublicationTransition(conn, news.getId(), news.getTitle(), oldStatus, news.getStatus());
            conn.commit();
            return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库更新失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新新闻状态
     */
    public boolean updateStatus(Integer id, Integer status) {
        String sql = "UPDATE news SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
            int oldStatus = lockedStatus(conn, id);
            if (oldStatus < 0) { conn.rollback(); return false; }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);
            pstmt.setInt(2, id);
            if (pstmt.executeUpdate() == 0) { conn.rollback(); return false; }
            }
            if (oldStatus != status) {
                if (status == 1) {
                    try (PreparedStatement title = conn.prepareStatement("SELECT title FROM news WHERE id = ?")) {
                        title.setInt(1, id);
                        try (ResultSet row = title.executeQuery()) {
                            if (!row.next()) throw new SQLException("新闻不存在");
                            new PublicationNotificationDAO().recordPublication(conn,
                                    PublicationNotificationDAO.NEWS, id, row.getString(1));
                        }
                    }
                } else {
                    new PublicationNotificationDAO().deactivate(conn, PublicationNotificationDAO.NEWS, id);
                }
            }
            conn.commit();
            return true;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("新闻状态更新失败: " + e.getMessage(), e);
        }
    }

    private int lockedStatus(Connection conn, int id) throws SQLException {
        try (PreparedStatement find = conn.prepareStatement("SELECT status FROM news WHERE id = ? FOR UPDATE")) {
            find.setInt(1, id);
            try (ResultSet row = find.executeQuery()) {
                return row.next() ? row.getInt(1) : -1;
            }
        }
    }

    private void applyPublicationTransition(Connection conn, int id, String title,
                                            int oldStatus, int newStatus) throws SQLException {
        if (oldStatus == newStatus) return;
        PublicationNotificationDAO notifications = new PublicationNotificationDAO();
        if (newStatus == 1) {
            notifications.recordPublication(conn, PublicationNotificationDAO.NEWS, id, title);
        } else {
            notifications.deactivate(conn, PublicationNotificationDAO.NEWS, id);
        }
    }

    /** Only a previously deleted article can be removed permanently. */
    public PurgedNews purgeDeleted(int id) {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String contentPath;
                try (PreparedStatement find = conn.prepareStatement(
                        "SELECT content_path FROM news WHERE id = ? AND status = 0 FOR UPDATE")) {
                    find.setInt(1, id);
                    try (ResultSet row = find.executeQuery()) {
                        if (!row.next()) {
                            conn.rollback();
                            return null;
                        }
                        contentPath = row.getString(1);
                    }
                }

                List<String> storedNames = new ArrayList<>();
                try (PreparedStatement find = conn.prepareStatement(
                        "SELECT stored_name FROM news_asset WHERE news_id = ?")) {
                    find.setInt(1, id);
                    try (ResultSet rows = find.executeQuery()) {
                        while (rows.next()) storedNames.add(rows.getString(1));
                    }
                }
                try (PreparedStatement deleteAssets = conn.prepareStatement(
                        "DELETE FROM news_asset WHERE news_id = ?");
                     PreparedStatement deleteNews = conn.prepareStatement(
                        "DELETE FROM news WHERE id = ? AND status = 0")) {
                    deleteAssets.setInt(1, id);
                    deleteAssets.executeUpdate();
                    deleteNews.setInt(1, id);
                    if (deleteNews.executeUpdate() != 1) {
                        conn.rollback();
                        return null;
                    }
                }
                conn.commit();
                return new PurgedNews(contentPath, storedNames);
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("彻底删除新闻失败", e);
        }
    }

    public static final class PurgedNews {
        private final String contentPath;
        private final List<String> storedNames;

        private PurgedNews(String contentPath, List<String> storedNames) {
            this.contentPath = contentPath;
            this.storedNames = Collections.unmodifiableList(new ArrayList<>(storedNames));
        }

        public String getContentPath() { return contentPath; }
        public List<String> getStoredNames() { return storedNames; }
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

    /**
     * 统计新闻总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM news WHERE status = 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return 0;
    }
}
