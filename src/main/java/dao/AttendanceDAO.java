package dao;

import model.Attendance;
import util.DBUtil;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 *
 *
 * 签到数据访问层
 */
public class AttendanceDAO {

    /**
     * 签到
     */
    public int checkIn(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (user_id, attendance_date, check_in_time, check_in_status, location, device_info) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE check_in_time = VALUES(check_in_time), " +
                     "check_in_status = VALUES(check_in_status), location = VALUES(location), device_info = VALUES(device_info)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, attendance.getUserId());
            ps.setDate(2, new java.sql.Date(attendance.getAttendanceDate().getTime()));
            ps.setTimestamp(3, attendance.getCheckInTime() != null ?
                new Timestamp(attendance.getCheckInTime().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(4, attendance.getCheckInStatus());
            ps.setString(5, attendance.getLocation());
            ps.setString(6, attendance.getDeviceInfo());

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
     * 签退
     */
    public int checkOut(Attendance attendance) throws SQLException {
        String sql = "UPDATE attendance SET check_out_time = ?, check_out_status = ?, " +
                     "work_duration = TIMESTAMPDIFF(MINUTE, check_in_time, ?) " +
                     "WHERE user_id = ? AND attendance_date = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            Timestamp checkoutTime = attendance.getCheckOutTime() != null ?
                new Timestamp(attendance.getCheckOutTime().getTime()) : new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(1, checkoutTime);
            ps.setString(2, attendance.getCheckOutStatus());
            ps.setTimestamp(3, checkoutTime);
            ps.setInt(4, attendance.getUserId());
            ps.setDate(5, new java.sql.Date(attendance.getAttendanceDate().getTime()));

            return ps.executeUpdate();
        }
    }

    /**
     * 获取今日签到记录
     */
    public Attendance getTodayAttendance(Integer userId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND attendance_date = CURDATE()";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractAttendance(rs);
            }
            return null;
        }
    }

    /**
     * 获取昨日签到记录（检查未签退）
     */
    public Attendance getYesterdayAttendance(Integer userId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND attendance_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractAttendance(rs);
            }
            return null;
        }
    }

    /**
     * 获取用户签到记录列表
     */
    public List<Attendance> getAttendanceList(Integer userId, java.util.Date startDate, java.util.Date endDate, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT a.*, u.name as user_name FROM attendance a " +
                     "LEFT JOIN user u ON a.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND a.user_id = ?");
            params.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND a.attendance_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND a.attendance_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        sql.append(" ORDER BY a.attendance_date DESC, a.check_in_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance att = extractAttendance(rs);
                att.setUserName(rs.getString("user_name"));
                list.add(att);
            }
        }
        return list;
    }

    /**
     * 管理员获取所有签到记录
     */
    public List<Attendance> getAllAttendance(java.util.Date startDate, java.util.Date endDate, Integer userId, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT a.*, u.name as user_name FROM attendance a " +
                     "LEFT JOIN user u ON a.user_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (userId != null) {
            sql.append(" AND a.user_id = ?");
            params.add(userId);
        }
        if (startDate != null) {
            sql.append(" AND a.attendance_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND a.attendance_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        sql.append(" ORDER BY a.attendance_date DESC, a.check_in_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance att = extractAttendance(rs);
                att.setUserName(rs.getString("user_name"));
                list.add(att);
            }
        }
        return list;
    }

    /**
     * 获取考勤统计数据
     */
    public Map<String, Object> getStatistics(Integer userId, java.util.Date startDate, java.util.Date endDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " +
                     "COUNT(*) as total_days, " +
                     "SUM(CASE WHEN check_in_status = 'NORMAL' THEN 1 ELSE 0 END) as normal_days, " +
                     "SUM(CASE WHEN check_in_status = 'LATE' THEN 1 ELSE 0 END) as late_days, " +
                     "SUM(CASE WHEN check_in_status = 'LEAVE' THEN 1 ELSE 0 END) as leave_days, " +
                     "SUM(CASE WHEN check_in_status = 'NONE' OR check_in_status IS NULL THEN 1 ELSE 0 END) as absent_days, " +
                     "SUM(work_duration) as total_work_duration " +
                     "FROM attendance WHERE user_id = ?");

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startDate != null) {
            sql.append(" AND attendance_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND attendance_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("totalDays", rs.getInt("total_days"));
                stats.put("normalDays", rs.getInt("normal_days"));
                stats.put("lateDays", rs.getInt("late_days"));
                stats.put("leaveDays", rs.getInt("leave_days"));
                stats.put("absentDays", rs.getInt("absent_days"));
                stats.put("totalWorkDuration", rs.getInt("total_work_duration"));
            }
        }
        return stats;
    }

    /**
     * 标记昨日未签退记录
     */
    public int markAbsent(Integer userId, Date date) throws SQLException {
        String sql = "UPDATE attendance SET check_out_status = 'MISSING', remark = CONCAT(IFNULL(remark, ''), ' | 系统自动标记：未签退') " +
                     "WHERE user_id = ? AND attendance_date = ? AND check_out_time IS NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, new java.sql.Date(date.getTime()));

            return ps.executeUpdate();
        }
    }

    /**
     * 获取记录总数
     */
    public int getTotalCount(Integer userId, java.util.Date startDate, java.util.Date endDate) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM attendance WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (startDate != null) {
            sql.append(" AND attendance_date >= ?");
            params.add(new java.sql.Date(startDate.getTime()));
        }
        if (endDate != null) {
            sql.append(" AND attendance_date <= ?");
            params.add(new java.sql.Date(endDate.getTime()));
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Attendance extractAttendance(ResultSet rs) throws SQLException {
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
        return att;
    }
}
