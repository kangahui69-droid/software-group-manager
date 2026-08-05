package com.softwaregroup.activity.dao;

import com.softwaregroup.activity.model.entity.AttendanceMakeup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * 补签申请数据访问层
 */
@Repository
public class AttendanceMakeupDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 申请补签
     */
    public int apply(AttendanceMakeup makeup) {
        String sql = "INSERT INTO attendance_makeup (user_id, attendance_date, make_up_type, apply_reason, status) " +
                "VALUES (?, ?, ?, ?, 'PENDING')";

        return jdbcTemplate.update(sql,
                makeup.getUserId(),
                new java.sql.Date(makeup.getAttendanceDate().getTime()),
                makeup.getMakeUpType(),
                makeup.getApplyReason());
    }

    /**
     * 获取用户的补签申请列表
     */
    public List<AttendanceMakeup> getListByUser(Integer userId, int offset, int limit) {
        String sql = "SELECT m.*, u.name as user_name FROM attendance_makeup m " +
                "LEFT JOIN user u ON m.user_id = u.id " +
                "WHERE m.user_id = ? ORDER BY m.apply_time DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new AttendanceMakeupRowMapper(), userId, limit, offset);
    }

    /**
     * 获取待审核的申请列表（管理员）
     */
    public List<AttendanceMakeup> getPendingList(int offset, int limit) {
        String sql = "SELECT m.*, u.name as user_name FROM attendance_makeup m " +
                "LEFT JOIN user u ON m.user_id = u.id " +
                "WHERE m.status = 'PENDING' ORDER BY m.apply_time ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new AttendanceMakeupRowMapper(), limit, offset);
    }

    /**
     * 审批补签申请
     */
    public int approve(Integer id, Integer approveBy, String status, String remark) {
        String sql = "UPDATE attendance_makeup SET status = ?, approve_by = ?, approve_time = NOW(), approve_remark = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, approveBy, remark, id);
    }

    /**
     * 检查是否已有待处理的申请
     */
    public boolean hasPendingApplication(Integer userId, Date date, String type) {
        String sql = "SELECT COUNT(*) FROM attendance_makeup WHERE user_id = ? AND attendance_date = ? AND make_up_type = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, new java.sql.Date(date.getTime()), type);
        return count != null && count > 0;
    }

    private static class AttendanceMakeupRowMapper implements RowMapper<AttendanceMakeup> {
        @Override
        public AttendanceMakeup mapRow(ResultSet rs, int rowNum) throws SQLException {
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

            try {
                makeup.setUserName(rs.getString("user_name"));
            } catch (Exception ignored) {}

            return makeup;
        }
    }
}
