package dao;

import model.MemberProfile;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 成员档案数据访问层
 */
public class MemberProfileDAO {

    /**
     * 根据用户ID查询档案
     */
    public MemberProfile findByUserId(Integer userId) {
        String sql = "SELECT * FROM member_profile WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToProfile(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * 添加或更新档案
     */
    public boolean saveOrUpdate(MemberProfile profile) {
        MemberProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            return update(profile);
        } else {
            return insert(profile);
        }
    }

    /**
     * 添加档案
     */
    public boolean insert(MemberProfile profile) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return insert(profile, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 添加档案（事务重载版本）
     */
    public boolean insert(MemberProfile profile, Connection conn) {
        if (profile.getUserId() == null) {
            throw new RuntimeException("插入档案失败：用户ID不能为空");
        }
        String sql = "INSERT INTO member_profile (user_id, birthday, student_id, major, grade, avatar_file_id, introduction) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, profile.getUserId());
            pstmt.setDate(2, profile.getBirthday() != null ? new Date(profile.getBirthday().getTime()) : null);
            pstmt.setString(3, profile.getStudentId() != null ? profile.getStudentId() : "");
            pstmt.setString(4, profile.getMajor() != null ? profile.getMajor() : "");
            pstmt.setString(5, profile.getGrade() != null ? profile.getGrade() : "");
            pstmt.setObject(6, profile.getAvatarFileId());
            pstmt.setString(7, profile.getIntroduction());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("插入档案失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 更新档案
     */
    public boolean update(MemberProfile profile) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            return update(profile, conn);
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库连接失败", e);
        } finally {
            closeResources(conn, null, null);
        }
    }

    /**
     * 更新档案（事务重载版本）
     */
    public boolean update(MemberProfile profile, Connection conn) {
        String sql = "UPDATE member_profile SET birthday = ?, student_id = ?, major = ?, grade = ?, avatar_file_id = ?, introduction = ?, github = ?, blog = ? WHERE user_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, profile.getBirthday() != null ? new Date(profile.getBirthday().getTime()) : null);
            pstmt.setString(2, profile.getStudentId());
            pstmt.setString(3, profile.getMajor());
            pstmt.setString(4, profile.getGrade());
            pstmt.setObject(5, profile.getAvatarFileId());
            pstmt.setString(6, profile.getIntroduction());
            pstmt.setString(7, profile.getGithub());
            pstmt.setString(8, profile.getBlog());
            pstmt.setInt(9, profile.getUserId());
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("更新档案失败：档案不存在");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新档案失败", e);
        } finally {
            closeResources(null, pstmt, null);
        }
    }

    /**
     * 添加或更新档案（事务重载版本）
     */
    public boolean saveOrUpdate(MemberProfile profile, Connection conn) {
        MemberProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            return update(profile, conn);
        } else {
            return insert(profile, conn);
        }
    }

    /**
     * 将ResultSet映射为MemberProfile对象
     */
    private MemberProfile mapResultSetToProfile(ResultSet rs) throws SQLException {
        MemberProfile profile = new MemberProfile();
        profile.setId(rs.getInt("id"));
        profile.setUserId(rs.getInt("user_id"));
        Date birthday = rs.getDate("birthday");
        profile.setBirthday(birthday != null ? new java.util.Date(birthday.getTime()) : null);
        profile.setStudentId(rs.getString("student_id"));
        profile.setMajor(rs.getString("major"));
        profile.setGrade(rs.getString("grade"));
        profile.setAvatarFileId((Integer) rs.getObject("avatar_file_id"));
        profile.setIntroduction(rs.getString("introduction"));
        profile.setGithub(rs.getString("github"));
        profile.setBlog(rs.getString("blog"));
        profile.setUpdatedAt(rs.getTimestamp("updated_at"));
        return profile;
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
