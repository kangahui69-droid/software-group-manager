package dao;

import model.UserGroup;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户_群关系数据访问层
 */
public class UserGroupDAO {

    public boolean insert(UserGroup userGroup) {
        String sql = "INSERT INTO user_group (user_id, group_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userGroup.getUserId());
            pstmt.setInt(2, userGroup.getGroupId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public boolean insertUserToGroup(Integer userId, Integer groupId) {
        return insert(new UserGroup(userId, groupId));
    }

    public boolean delete(Integer userId, Integer groupId) {
        String sql = "DELETE FROM user_group WHERE user_id = ? AND group_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    public boolean exists(Integer userId, Integer groupId) {
        String sql = "SELECT COUNT(*) FROM user_group WHERE user_id = ? AND group_id = ?";
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
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return false;
    }

    public List<UserGroup> findByUserId(Integer userId) {
        String sql = "SELECT ug.*, ag.group_name, a.name as activity_name, " +
                     "(SELECT COUNT(*) FROM group_member WHERE group_id = ag.id) as member_count " +
                     "FROM user_group ug " +
                     "INNER JOIN activity_group ag ON ug.group_id = ag.id " +
                     "LEFT JOIN activity a ON ag.activity_id = a.id " +
                     "WHERE ug.user_id = ? " +
                     "ORDER BY ug.joined_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<UserGroup> userGroups = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                userGroups.add(mapResultSetToUserGroup(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return userGroups;
    }

    private UserGroup mapResultSetToUserGroup(ResultSet rs) throws SQLException {
        UserGroup userGroup = new UserGroup();
        userGroup.setId(rs.getInt("id"));
        userGroup.setUserId(rs.getInt("user_id"));
        userGroup.setGroupId(rs.getInt("group_id"));
        userGroup.setJoinedAt(rs.getTimestamp("joined_at"));
        userGroup.setGroupName(rs.getString("group_name"));
        userGroup.setActivityName(rs.getString("activity_name"));
        userGroup.setMemberCount(rs.getInt("member_count"));
        return userGroup;
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
