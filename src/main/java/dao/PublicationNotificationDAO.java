package dao;

import model.PublicationNotification;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Stores publication events and per-user read state. Publication writes share the source transaction. */
public class PublicationNotificationDAO {
    public static final String NEWS = "NEWS";
    public static final String ACTIVITY = "ACTIVITY";

    private static final String VISIBLE =
            " AND ((e.source_type = 'NEWS' AND EXISTS " +
            "(SELECT 1 FROM news n WHERE n.id = e.source_id AND n.status = 1)) " +
            "OR (e.source_type = 'ACTIVITY' AND EXISTS " +
            "(SELECT 1 FROM activity a WHERE a.id = e.source_id AND a.approval_status = 'approved' " +
            "AND a.deleted = 0 AND a.status <> 'canceled')))";

    public void recordPublication(Connection conn, String type, int sourceId, String title) throws SQLException {
        deactivate(conn, type, sourceId);
        long notificationId;
        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO notification_event (source_type, source_id, title) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, type);
            insert.setInt(2, sourceId);
            insert.setString(3, title);
            if (insert.executeUpdate() != 1) throw new SQLException("通知创建失败");
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("通知 ID 获取失败");
                notificationId = keys.getLong(1);
            }
        }
        try (PreparedStatement deliver = conn.prepareStatement(
                "INSERT INTO notification_recipient (notification_id, user_id) " +
                "SELECT ?, id FROM user WHERE status = 1 AND role <> 'ADMIN'")) {
            deliver.setLong(1, notificationId);
            deliver.executeUpdate();
        }
    }

    public void deactivate(Connection conn, String type, int sourceId) throws SQLException {
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE notification_event SET active = 0 WHERE source_type = ? AND source_id = ? AND active = 1")) {
            update.setString(1, type);
            update.setInt(2, sourceId);
            update.executeUpdate();
        }
    }

    public int unreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM notification_recipient r JOIN notification_event e " +
                "ON e.id = r.notification_id WHERE r.user_id = ? AND r.read_at IS NULL AND e.active = 1" + VISIBLE;
        try (Connection conn = DBUtil.getConnection(); PreparedStatement query = conn.prepareStatement(sql)) {
            query.setInt(1, userId);
            try (ResultSet row = query.executeQuery()) {
                return row.next() ? row.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询未读通知失败", e);
        }
    }

    public List<PublicationNotification> list(int userId, int limit) {
        String sql = "SELECT e.id, e.source_type, e.source_id, e.title, e.created_at, r.read_at " +
                "FROM notification_recipient r JOIN notification_event e ON e.id = r.notification_id " +
                "WHERE r.user_id = ? AND e.active = 1" + VISIBLE +
                " ORDER BY e.created_at DESC, e.id DESC LIMIT ?";
        List<PublicationNotification> notifications = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement query = conn.prepareStatement(sql)) {
            query.setInt(1, userId);
            query.setInt(2, Math.min(Math.max(limit, 1), 100));
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) {
                    notifications.add(new PublicationNotification(rows.getLong(1), rows.getString(2),
                            rows.getInt(3), rows.getString(4), rows.getTimestamp(5), rows.getTimestamp(6)));
                }
            }
            return notifications;
        } catch (SQLException e) {
            throw new IllegalStateException("查询通知失败", e);
        }
    }

    public void markSourceRead(int userId, String type, int sourceId) {
        if (!NEWS.equals(type) && !ACTIVITY.equals(type)) throw new IllegalArgumentException("通知类型无效");
        String sql = "UPDATE notification_recipient SET read_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ? AND read_at IS NULL AND notification_id IN " +
                "(SELECT id FROM notification_event WHERE source_type = ? AND source_id = ? AND active = 1)";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement update = conn.prepareStatement(sql)) {
            update.setInt(1, userId);
            update.setString(2, type);
            update.setInt(3, sourceId);
            update.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("更新通知已读状态失败", e);
        }
    }
}
