package com.softwaregroup.activity.dao;

import com.softwaregroup.activity.model.entity.Attendance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 签到数据访问层
 */
@Repository
public class AttendanceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 签到
     */
    public int checkIn(Attendance attendance) {
        String sql = "INSERT INTO attendance (user_id, attendance_date, check_in_time, check_in_status, location, device_info) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE check_in_time = VALUES(check_in_time), " +
                "check_in_status = VALUES(check_in_status), location = VALUES(location), device_info = VALUES(device_info)";

        return jdbcTemplate.update(sql,
                attendance.getUserId(),
                new java.sql.Date(attendance.getAttendanceDate().getTime()),
                attendance.getCheckInTime() != null ? new Timestamp(attendance.getCheckInTime().getTime()) : new Timestamp(System.currentTimeMillis()),
                attendance.getCheckInStatus(),
                attendance.getLocation(),
                attendance.getDeviceInfo());
    }

    /**
     * 签退
     */
    public int checkOut(Attendance attendance) {
        String sql = "UPDATE attendance SET check_out_time = ?, check_out_status = ?, " +
                "work_duration = TIMESTAMPDIFF(MINUTE, check_in_time, ?) " +
                "WHERE user_id = ? AND attendance_date = ?";

        Timestamp checkoutTime = attendance.getCheckOutTime() != null ?
                new Timestamp(attendance.getCheckOutTime().getTime()) : new Timestamp(System.currentTimeMillis());

        return jdbcTemplate.update(sql,
                checkoutTime,
                attendance.getCheckOutStatus(),
                checkoutTime,
                attendance.getUserId(),
                new java.sql.Date(attendance.getAttendanceDate().getTime()));
    }

    /**
     * 获取今日签到记录
     */
    public Attendance getTodayAttendance(Integer userId) {
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND attendance_date = CURDATE()";
        List<Attendance> results = jdbcTemplate.query(sql, new AttendanceRowMapper(), userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 获取用户签到记录列表
     */
    public List<Attendance> getAttendanceList(Integer userId, Date startDate, Date endDate, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT a.*, u.name as user_name FROM attendance a " +
                "LEFT JOIN user u ON a.user_id = u.id WHERE 1=1");
        List<Object> args = new java.util.ArrayList<>();

        if (userId != null) {
            sql.append(" AND a.user_id = ?");
            args.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND a.attendance_date >= ?");
            args.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND a.attendance_date <= ?");
            args.add(new java.sql.Date(endDate.getTime()));
        }
        sql.append(" ORDER BY a.attendance_date DESC, a.check_in_time DESC LIMIT ? OFFSET ?");
        args.add(limit);
        args.add(offset);

        return jdbcTemplate.query(sql.toString(), args.toArray(), new AttendanceRowMapper());
    }

    /**
     * 获取考勤统计数据
     */
    public Map<String, Object> getStatistics(Integer userId, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT " +
                "COUNT(*) as total_days, " +
                "SUM(CASE WHEN check_in_status = 'NORMAL' THEN 1 ELSE 0 END) as normal_days, " +
                "SUM(CASE WHEN check_in_status = 'LATE' THEN 1 ELSE 0 END) as late_days, " +
                "SUM(CASE WHEN check_in_status = 'LEAVE' THEN 1 ELSE 0 END) as leave_days, " +
                "SUM(CASE WHEN check_in_status = 'NONE' OR check_in_status IS NULL THEN 1 ELSE 0 END) as absent_days, " +
                "SUM(work_duration) as total_work_duration " +
                "FROM attendance WHERE user_id = ?");

        if (startDate != null) {
            sql.append(" AND attendance_date >= ?");
        }
        if (endDate != null) {
            sql.append(" AND attendance_date <= ?");
        }

        List<Object> args = new java.util.ArrayList<>();
        args.add(userId);
        if (startDate != null) args.add(new java.sql.Date(startDate.getTime()));
        if (endDate != null) args.add(new java.sql.Date(endDate.getTime()));

        return jdbcTemplate.queryForMap(sql.toString(), args.toArray());
    }

    /**
     * 获取记录总数
     */
    public int getTotalCount(Integer userId, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM attendance WHERE user_id = ?");
        List<Object> args = new java.util.ArrayList<>();
        args.add(userId);

        if (startDate != null) {
            sql.append(" AND attendance_date >= ?");
            args.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND attendance_date <= ?");
            args.add(new java.sql.Date(endDate.getTime()));
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, args.toArray());
        return count != null ? count : 0;
    }

    private static class AttendanceRowMapper implements RowMapper<Attendance> {
        @Override
        public Attendance mapRow(ResultSet rs, int rowNum) throws SQLException {
            Attendance att = new Attendance();
            att.setId(rs.getInt("id"));
            att.setUserId(rs.getInt("user_id"));

            Date date = rs.getDate("attendance_date");
            att.setAttendanceDate(date);

            Timestamp checkIn = rs.getTimestamp("check_in_time");
            if (checkIn != null) att.setCheckInTime(checkIn);

            Timestamp checkOut = rs.getTimestamp("check_out_time");
            if (checkOut != null) att.setCheckOutTime(checkOut);

            att.setCheckInStatus(rs.getString("check_in_status"));
            att.setCheckOutStatus(rs.getString("check_out_status"));
            att.setWorkDuration(rs.getInt("work_duration"));
            att.setLocation(rs.getString("location"));
            att.setDeviceInfo(rs.getString("device_info"));
            att.setRemark(rs.getString("remark"));
            att.setCreatedAt(rs.getTimestamp("created_at"));
            att.setUpdatedAt(rs.getTimestamp("updated_at"));

            try {
                att.setUserName(rs.getString("user_name"));
            } catch (Exception ignored) {}

            return att;
        }
    }
}
