package dao;

import model.GroupMessage;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 群消息数据访问层
 */
public class GroupMessageDAO {

    public int insert(GroupMessage message) {
        String sql = "INSERT INTO group_message (group_id, sender_id, content) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, message.getGroupId());
            pstmt.setInt(2, message.getSenderId());
            pstmt.setString(3, message.getContent());
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
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

    public List<GroupMessage> findByGroupId(Integer groupId, int limit, int offset) {
        String sql = "SELECT gm.*, u.username, u.name as sender_name, mp.avatar_file_id as sender_avatar_file_id " +
                     "FROM group_message gm " +
                     "LEFT JOIN user u ON gm.sender_id = u.id " +
                     "LEFT JOIN member_profile mp ON u.id = mp.user_id " +
                     "WHERE gm.group_id = ? " +
                     "ORDER BY gm.sent_at ASC " +
                     "LIMIT ? OFFSET ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<GroupMessage> messages = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return messages;
    }

    public List<GroupMessage> findRecentByGroupId(Integer groupId, int limit) {
        return findByGroupId(groupId, limit, 0);
    }

    public int countByGroupId(Integer groupId) {
        String sql = "SELECT COUNT(*) FROM group_message WHERE group_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
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

    public boolean delete(Integer id) {
        String sql = "DELETE FROM group_message WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public boolean deleteByGroupId(Integer groupId) {
        String sql = "DELETE FROM group_message WHERE group_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    private GroupMessage mapResultSetToMessage(ResultSet rs) throws SQLException {
        GroupMessage message = new GroupMessage();
        message.setId(rs.getInt("id"));
        message.setGroupId(rs.getInt("group_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setContent(rs.getString("content"));
        message.setSentAt(rs.getTimestamp("sent_at"));
        message.setSenderName(rs.getString("sender_name"));
        message.setSenderAvatarFileId(rs.getString("sender_avatar_file_id"));
        return message;
    }

    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
