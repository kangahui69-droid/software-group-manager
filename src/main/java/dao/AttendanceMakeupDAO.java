package dao;

import model.AttendanceMakeup;
import util.DBUtil;

import java.sql.*;
import java.util.*;

/**
 * 补签申请数据访问层
 */
public class AttendanceMakeupDAO {

    /**
     * 申请补签
     */
    public int apply(AttendanceMakeup makeup) throws SQLException {
        String sql = "INSERT INTO attendance_makeup (user_id, attendance_date, make_up_type, apply_reason, status) " +
                     "VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, makeup.getUserId());
            ps.setDate(2, new java.sql.Date(makeup.getAttendanceDate().getTime()));
            ps.setString(3, makeup.getMakeUpType());
            ps.setString(4, makeup.getApplyReason());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return rows;
        }
    }

    /**
     * 获取用户的补签申请列表
     */
    public List<AttendanceMakeup> getListByUser(Integer userId, int offset, int limit) throws SQLException {
        String sql = "SELECT m.*, u.name as user_name FROM attendance_makeup m " +
                     "LEFT JOIN user u ON m.user_id = u.id " +
                     "WHERE m.user_id = ? ORDER BY m.apply_time DESC LIMIT ? OFFSET ?";

        List<AttendanceMakeup> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AttendanceMakeup makeup = extractMakeup(rs);
                makeup.setUserName(rs.getString("user_name"));
                list.add(makeup);
            }
        }
        return list;
    }

    /**
     * 获取待审核的申请列表（管理员）
     */
    public List<AttendanceMakeup> getPendingList(int offset, int limit) throws SQLException {
        String sql = "SELECT m.*, u.name as user_name FROM attendance_makeup m " +
                     "LEFT JOIN user u ON m.user_id = u.id " +
                     "WHERE m.status = 'PENDING' ORDER BY m.apply_time ASC LIMIT ? OFFSET ?";

        List<AttendanceMakeup> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AttendanceMakeup makeup = extractMakeup(rs);
                makeup.setUserName(rs.getString("user_name"));
                list.add(makeup);
            }
        }
        return list;
    }

    /**
     * 审批补签申请
     */
    public int approve(Integer id, Integer approveBy, String status, String remark) throws SQLException {
        String sql = "UPDATE attendance_makeup SET status = ?, approve_by = ?, approve_time = NOW(), approve_remark = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, approveBy);
            ps.setString(3, remark);
            ps.setInt(4, id);

            return ps.executeUpdate();
        }
    }

    /**
     * 检查是否已有待处理的申请
     */
    public boolean hasPendingApplication(Integer userId, java.util.Date date, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance_makeup WHERE user_id = ? AND attendance_date = ? AND make_up_type = ? AND status = 'PENDING'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setString(3, type);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private AttendanceMakeup extractMakeup(ResultSet rs) throws SQLException {
        AttendanceMakeup makeup = new AttendanceMakeup();
        makeup.setId(rs.getInt("id"));
        makeup.setUserId(rs.getInt("user_id"));
        makeup.setAttendanceDate(rs.getDate("attendance_date"));
        makeup.setMakeUpType(rs.getString("make_up_type"));
        makeup.setApplyReason(rs.getString("apply_reason"));
        makeup.setApplyTime(rs.getTimestamp("apply_time"));
        makeup.setStatus(rs.getString("status"));

        int approveBy = rs.getInt("approve_by");
        if (!rs.wasNull()) {
            makeup.setApproveBy(approveBy);
        }

        makeup.setApproveTime(rs.getTimestamp("approve_time"));
        makeup.setApproveRemark(rs.getString("approve_remark"));
        return makeup;
    }
}
