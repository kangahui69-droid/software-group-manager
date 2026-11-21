package dao;

import model.OperationLog;
import model.LogTimeRange;
import util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志数据访问层
 */
public class OperationLogDAO {
    private static final ZoneId BEIJING_TIME = ZoneId.of("Asia/Shanghai");

    /**
     * 添加日志
     */
    public boolean insert(OperationLog log) {
        String sql = "INSERT INTO operation_log (user_id, username, operation, module, description, ip_address, user_agent, "
                + "result, http_status, duration_ms, request_id, error_code, target_type, target_id, proxy_ip) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, log.getUserId());
            pstmt.setString(2, log.getUsername());
            pstmt.setString(3, log.getOperation());
            pstmt.setString(4, log.getModule());
            pstmt.setString(5, log.getDescription());
            pstmt.setString(6, log.getIpAddress());
            pstmt.setString(7, log.getUserAgent());
            pstmt.setString(8, log.getResult());
            pstmt.setObject(9, log.getHttpStatus());
            pstmt.setObject(10, log.getDurationMs());
            pstmt.setString(11, log.getRequestId());
            pstmt.setString(12, log.getErrorCode());
            pstmt.setString(13, log.getTargetType());
            pstmt.setString(14, log.getTargetId());
            pstmt.setString(15, log.getProxyIp());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, null);
        }
        return false;
    }

    /**
     * 查询所有日志（分页）
     */
    public List<OperationLog> findAll(int page, int pageSize) {
        return findByConditions(null, null, null, null, page, pageSize);
    }

    /**
     * 根据条件搜索日志
     */
    public List<OperationLog> findByConditions(String keyword, String operation, String module, String dateRange, int page, int pageSize) {
        LogTimeRange range = LogTimeRange.legacy(dateRange);
        return findByTimeRange(keyword, operation, module, range.getStart(), range.getEnd(), page, pageSize);
    }

    public List<OperationLog> findByTimeRange(String keyword, String operation, String module,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               int page, int pageSize) {
        List<OperationLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM operation_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR description LIKE ? OR ip_address LIKE ? OR request_id LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            params.add(operation);
        }
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        appendTimeRange(sql, params, startTime, endTime);
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof LocalDateTime) {
                    pstmt.setObject(paramIndex++, params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(paramIndex++, (Integer) params.get(i));
                } else {
                    pstmt.setString(paramIndex++, (String) params.get(i));
                }
            }
            pstmt.setInt(paramIndex++, pageSize);
            pstmt.setInt(paramIndex++, (page - 1) * pageSize);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, pstmt, rs);
        }
        return logs;
    }

    /**
     * 统计符合条件日志数量
     */
    public int countByConditions(String keyword, String operation, String module, String dateRange) {
        LogTimeRange range = LogTimeRange.legacy(dateRange);
        return countByTimeRange(keyword, operation, module, range.getStart(), range.getEnd());
    }

    public int countByTimeRange(String keyword, String operation, String module,
                                LocalDateTime startTime, LocalDateTime endTime) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM operation_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR description LIKE ? OR ip_address LIKE ? OR request_id LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation = ?");
            params.add(operation);
        }
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        appendTimeRange(sql, params, startTime, endTime);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof LocalDateTime) {
                    pstmt.setObject(i + 1, params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params.get(i));
                } else {
                    pstmt.setString(i + 1, (String) params.get(i));
                }
            }
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

    private void appendTimeRange(StringBuilder sql, List<Object> params,
                                 LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null) {
            sql.append(" AND created_at >= ?");
            params.add(startTime);
        }
        if (endTime != null) {
            sql.append(" AND created_at < ?");
            params.add(endTime);
        }
    }

    /**
     * 查询日志总数
     */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM operation_log";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
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

    /**
     * 将ResultSet映射为OperationLog对象
     */
    private OperationLog mapResultSetToLog(ResultSet rs) throws SQLException {
        OperationLog log = new OperationLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getObject("user_id", Integer.class));
        log.setUsername(rs.getString("username"));
        log.setOperation(rs.getString("operation"));
        log.setModule(rs.getString("module"));
        log.setDescription(rs.getString("description"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setProxyIp(rs.getString("proxy_ip"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setResult(rs.getString("result"));
        log.setHttpStatus(rs.getObject("http_status", Integer.class));
        log.setDurationMs(rs.getObject("duration_ms", Long.class));
        log.setRequestId(rs.getString("request_id"));
        log.setErrorCode(rs.getString("error_code"));
        log.setTargetType(rs.getString("target_type"));
        log.setTargetId(rs.getString("target_id"));
        // operation_log.created_at is DATETIME: interpret its wall time as Beijing time.
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        log.setCreatedAt(createdAt == null ? null : Date.from(createdAt.atZone(BEIJING_TIME).toInstant()));
        return log;
    }

    /**
     * 关闭资源
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 统计日志总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM operation_log";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
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
}

