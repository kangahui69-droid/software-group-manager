package dao;

import model.GroupMember;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 群成员数据访问层
 */
public class GroupMemberDAO {

    public boolean insert(GroupMember member) {
        String sql = "INSERT INTO group_member (group_id, user_id, role) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, member.getGroupId());
            pstmt.setInt(2, member.getUserId());
            pstmt.setString(3, member.getRole());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public boolean insertMember(Integer groupId, Integer userId) {
        return insert(new GroupMember(groupId, userId, GroupMember.ROLE_MEMBER));
    }

    public boolean insertOwner(Integer groupId, Integer userId) {
        return insert(new GroupMember(groupId, userId, GroupMember.ROLE_OWNER));
    }

    public boolean delete(Integer groupId, Integer userId) {
        String sql = "DELETE FROM group_member WHERE group_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public void deleteByGroupId(Integer groupId) {
        String sql = "DELETE FROM group_member WHERE group_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    public boolean isMember(Integer groupId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM group_member WHERE group_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
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

    public boolean isOwner(Integer groupId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM group_member WHERE group_id = ? AND user_id = ? AND role = 'OWNER'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
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

    public List<GroupMember> findByGroupId(Integer groupId) {
        String sql = "SELECT gm.*, u.username, u.name, mp.avatar_file_id " +
                     "FROM group_member gm " +
                     "LEFT JOIN user u ON gm.user_id = u.id " +
                     "LEFT JOIN member_profile mp ON u.id = mp.user_id " +
                     "WHERE gm.group_id = ? " +
                     "ORDER BY gm.role = 'OWNER' DESC, gm.joined_at ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<GroupMember> members = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return members;
    }

    public List<GroupMember> findByUserId(Integer userId) {
        String sql = "SELECT gm.*, u.username, u.name, mp.avatar_file_id " +
                     "FROM group_member gm " +
                     "LEFT JOIN user u ON gm.user_id = u.id " +
                     "LEFT JOIN member_profile mp ON u.id = mp.user_id " +
                     "WHERE gm.user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<GroupMember> members = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return members;
    }

    public int countByGroupId(Integer groupId) {
        String sql = "SELECT COUNT(*) FROM group_member WHERE group_id = ?";
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

    public int getUnreadCount(Integer groupId, Integer userId) {
        String sql = "SELECT COUNT(*) FROM group_message gm " +
                     "LEFT JOIN group_member gm2 ON gm.group_id = gm2.group_id AND gm2.user_id = ? " +
                     "WHERE gm.group_id = ? AND gm.sent_at > IFNULL(gm2.last_read_at, '1970-01-01')";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
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

    public void updateLastReadAt(Integer groupId, Integer userId) {
        String sql = "UPDATE group_member SET last_read_at = NOW() WHERE group_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
    }

    private GroupMember mapResultSetToMember(ResultSet rs) throws SQLException {
        GroupMember member = new GroupMember();
        member.setId(rs.getInt("id"));
        member.setGroupId(rs.getInt("group_id"));
        member.setUserId(rs.getInt("user_id"));
        member.setRole(rs.getString("role"));
        member.setJoinedAt(rs.getTimestamp("joined_at"));
        member.setUsername(rs.getString("username"));
        member.setName(rs.getString("name"));
        member.setAvatarFileId(rs.getString("avatar_file_id"));
        try {
            member.setLastReadAt(rs.getTimestamp("last_read_at"));
        } catch (SQLException e) { }
        return member;
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
