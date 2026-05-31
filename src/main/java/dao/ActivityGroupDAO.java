package dao;

import model.ActivityGroup;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动群数据访问层
 */
public class ActivityGroupDAO {

    public boolean insert(ActivityGroup group) {
        String sql = "INSERT INTO activity_group (activity_id, group_name, group_owner_id) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, group.getActivityId());
            pstmt.setString(2, group.getGroupName());
            pstmt.setInt(3, group.getGroupOwnerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public ActivityGroup findById(Integer id) {
        String sql = "SELECT ag.*, u.name as owner_name, a.name as activity_name, " +
                     "(SELECT COUNT(*) FROM group_member WHERE group_id = ag.id) as member_count " +
                     "FROM activity_group ag " +
                     "LEFT JOIN user u ON ag.group_owner_id = u.id " +
                     "LEFT JOIN activity a ON ag.activity_id = a.id " +
                     "WHERE ag.id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToGroup(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public ActivityGroup findByActivityId(Integer activityId) {
        String sql = "SELECT ag.*, u.name as owner_name, a.name as activity_name, " +
                     "(SELECT COUNT(*) FROM group_member WHERE group_id = ag.id) as member_count " +
                     "FROM activity_group ag " +
                     "LEFT JOIN user u ON ag.group_owner_id = u.id " +
                     "LEFT JOIN activity a ON ag.activity_id = a.id " +
                     "WHERE ag.activity_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, activityId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToGroup(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    public List<ActivityGroup> findByOwnerId(Integer ownerId) {
        String sql = "SELECT ag.*, u.name as owner_name, a.name as activity_name, " +
                     "(SELECT COUNT(*) FROM group_member WHERE group_id = ag.id) as member_count " +
                     "FROM activity_group ag " +
                     "LEFT JOIN user u ON ag.group_owner_id = u.id " +
                     "LEFT JOIN activity a ON ag.activity_id = a.id " +
                     "WHERE ag.group_owner_id = ? " +
                     "ORDER BY ag.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ActivityGroup> groups = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, ownerId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return groups;
    }

    public List<ActivityGroup> findByUserId(Integer userId) {
        String sql = "SELECT ag.*, u.name as owner_name, a.name as activity_name, " +
                     "(SELECT COUNT(*) FROM group_member WHERE group_id = ag.id) as member_count " +
                     "FROM activity_group ag " +
                     "INNER JOIN group_member gm ON ag.id = gm.group_id " +
                     "LEFT JOIN user u ON ag.group_owner_id = u.id " +
                     "LEFT JOIN activity a ON ag.activity_id = a.id " +
                     "WHERE gm.user_id = ? " +
                     "ORDER BY ag.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ActivityGroup> groups = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return groups;
    }

    public List<ActivityGroup> findAll() {
        String sql = "SELECT ag.*, u.name as owner_name, a.name as activity_name, " +
                     "(SELECT COUNT(*) FROM group_member WHERE group_id = ag.id) as member_count, " +
                     "(SELECT COUNT(*) FROM group_message WHERE group_id = ag.id) as message_count " +
                     "FROM activity_group ag " +
                     "LEFT JOIN user u ON ag.group_owner_id = u.id " +
                     "LEFT JOIN activity a ON ag.activity_id = a.id " +
                     "ORDER BY ag.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ActivityGroup> groups = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return groups;
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM activity_group WHERE id = ?";
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

    public boolean muteGroup(Integer groupId, Date mutedUntil, String reason) {
        String sql = "UPDATE activity_group SET is_muted = 1, muted_until = ?, mute_reason = ?, updated_at = NOW() WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            if (mutedUntil != null) {
                pstmt.setTimestamp(1, new Timestamp(mutedUntil.getTime()));
            } else {
                pstmt.setNull(1, Types.TIMESTAMP);
            }
            pstmt.setString(2, reason);
            pstmt.setInt(3, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public boolean unmuteGroup(Integer groupId) {
        String sql = "UPDATE activity_group SET is_muted = 0, muted_until = NULL, mute_reason = NULL, updated_at = NOW() WHERE id = ?";
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

    public int unmuteExpiredGroups() {
        String sql = "UPDATE activity_group SET is_muted = 0, muted_until = NULL, mute_reason = NULL, updated_at = NOW() " +
                     "WHERE is_muted = 1 AND muted_until IS NOT NULL AND muted_until < NOW()";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return 0;
    }

    private ActivityGroup mapResultSetToGroup(ResultSet rs) throws SQLException {
        ActivityGroup group = new ActivityGroup();
        group.setId(rs.getInt("id"));
        group.setActivityId((Integer) rs.getObject("activity_id"));
        group.setGroupName(rs.getString("group_name"));
        group.setGroupOwnerId(rs.getInt("group_owner_id"));
        group.setCreatedAt(rs.getTimestamp("created_at"));
        group.setUpdatedAt(rs.getTimestamp("updated_at"));
        group.setOwnerName(rs.getString("owner_name"));
        group.setActivityName(rs.getString("activity_name"));
        group.setMemberCount(rs.getInt("member_count"));

        int isMuted = rs.getInt("is_muted");
        group.setIsMuted(isMuted);
        group.setMutedUntil(rs.getTimestamp("muted_until"));
        group.setMuteReason(rs.getString("mute_reason"));

        return group;
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