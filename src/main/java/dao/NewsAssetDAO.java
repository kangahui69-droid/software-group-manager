package dao;

import model.NewsAsset;
import util.DBUtil;

import java.sql.*;
import java.util.*;
import java.time.Instant;

public class NewsAssetDAO {
    private NewsAsset map(ResultSet rs) throws SQLException {
        NewsAsset asset = new NewsAsset();
        asset.setId(rs.getInt("id"));
        asset.setNewsId((Integer) rs.getObject("news_id"));
        asset.setUploadedBy(rs.getInt("uploaded_by"));
        asset.setUploadToken(rs.getString("upload_token"));
        asset.setKind(rs.getString("kind"));
        asset.setOriginalName(rs.getString("original_name"));
        asset.setStoredName(rs.getString("stored_name"));
        asset.setMimeType(rs.getString("mime_type"));
        asset.setFileSize(rs.getLong("file_size"));
        return asset;
    }

    public NewsAsset find(int id) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("SELECT * FROM news_asset WHERE id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) { return r.next() ? map(r) : null; }
        } catch (SQLException e) { throw new IllegalStateException("读取新闻媒体失败", e); }
    }

    public List<NewsAsset> forNews(int newsId, String kind) {
        List<NewsAsset> result = new ArrayList<>();
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT * FROM news_asset WHERE news_id=? AND kind=? ORDER BY sort_order,id")) {
            p.setInt(1, newsId); p.setString(2, kind);
            try (ResultSet r = p.executeQuery()) { while (r.next()) result.add(map(r)); }
            return result;
        } catch (SQLException e) { throw new IllegalStateException("读取新闻附件失败", e); }
    }

    public int insert(NewsAsset asset) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO news_asset (uploaded_by,upload_token,kind,original_name,stored_name,mime_type,file_size) VALUES (?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, asset.getUploadedBy()); p.setString(2, asset.getUploadToken());
            p.setString(3, asset.getKind()); p.setString(4, asset.getOriginalName());
            p.setString(5, asset.getStoredName()); p.setString(6, asset.getMimeType()); p.setLong(7, asset.getFileSize());
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) { if (r.next()) return r.getInt(1); }
            throw new SQLException("缺少媒体 ID");
        } catch (SQLException e) { throw new IllegalStateException("保存新闻媒体失败", e); }
    }

    public void validate(Integer newsId, int userId, String token, List<Integer> imageIds, List<Integer> attachmentIds) {
        LinkedHashSet<Integer> all = new LinkedHashSet<>(imageIds);
        all.addAll(attachmentIds);
        for (int id : all) {
            NewsAsset a = find(id);
            boolean existing = a != null && newsId != null && Objects.equals(a.getNewsId(), newsId);
            boolean pending = a != null && a.getNewsId() == null && a.getUploadedBy() == userId
                    && token != null && !token.isEmpty() && token.equals(a.getUploadToken());
            if ((!existing && !pending) || !((imageIds.contains(id) && "IMAGE".equals(a.getKind()))
                    || (attachmentIds.contains(id) && "ATTACHMENT".equals(a.getKind())))) {
                throw new IllegalArgumentException("无权关联新闻媒体: " + id);
            }
        }
    }

    public void sync(int newsId, int userId, String token, List<Integer> imageIds, List<Integer> attachmentIds) {
        LinkedHashSet<Integer> all = new LinkedHashSet<>(imageIds);
        all.addAll(attachmentIds);
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                for (int id : all) {
                    try (PreparedStatement p = c.prepareStatement("SELECT * FROM news_asset WHERE id=? FOR UPDATE")) {
                        p.setInt(1, id);
                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) throw new IllegalArgumentException("新闻媒体不存在: " + id);
                            NewsAsset a = map(r);
                            boolean existing = Objects.equals(a.getNewsId(), newsId);
                            boolean pending = a.getNewsId() == null && a.getUploadedBy() == userId && token.equals(a.getUploadToken());
                            if ((!existing && !pending) || !((imageIds.contains(id) && "IMAGE".equals(a.getKind()))
                                    || (attachmentIds.contains(id) && "ATTACHMENT".equals(a.getKind())))) {
                                throw new IllegalArgumentException("无权关联新闻媒体: " + id);
                            }
                        }
                    }
                }
                // Removed assets remain on disk until cleanup; detach immediately so old URLs stop working.
                try (PreparedStatement p = c.prepareStatement("UPDATE news_asset SET news_id=NULL WHERE news_id=?")) {
                    p.setInt(1, newsId); p.executeUpdate();
                }
                int order = 0;
                for (int id : all) {
                    try (PreparedStatement p = c.prepareStatement("UPDATE news_asset SET news_id=?,sort_order=? WHERE id=?")) {
                        p.setInt(1, newsId); p.setInt(2, order++); p.setInt(3, id); p.executeUpdate();
                    }
                }
                c.commit();
            } catch (RuntimeException | SQLException e) {
                c.rollback();
                if (e instanceof SQLException) throw new IllegalStateException("关联新闻媒体失败", e);
                throw e;
            }
        } catch (SQLException e) { throw new IllegalStateException("关联新闻媒体失败", e); }
    }

    public List<String> pruneUnlinkedBefore(Instant cutoff) {
        List<String> names = new ArrayList<>();
        Timestamp before = Timestamp.from(cutoff);
        try (Connection c = DBUtil.getConnection(); PreparedStatement find = c.prepareStatement(
                "SELECT id,stored_name FROM news_asset WHERE news_id IS NULL AND created_at<?")) {
            find.setTimestamp(1, before);
            try (ResultSet rows = find.executeQuery()) {
                while (rows.next()) {
                    try (PreparedStatement delete = c.prepareStatement(
                            "DELETE FROM news_asset WHERE id=? AND news_id IS NULL AND created_at<?")) {
                        delete.setInt(1, rows.getInt("id")); delete.setTimestamp(2, before);
                        if (delete.executeUpdate() == 1) names.add(rows.getString("stored_name"));
                    }
                }
            }
            return names;
        } catch (SQLException e) { throw new IllegalStateException("清理过期新闻媒体失败", e); }
    }
}
