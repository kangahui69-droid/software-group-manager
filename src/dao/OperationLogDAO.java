package dao;

import model.OperationLog;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志数据访问层
 */
public class OperationLogDAO {

    /**
     * 添加日志
     */
    public boolean insert(OperationLog log) {
        String sql = "INSERT INTO operation_log (user_id, username, operation, module, description, ip_address, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
        List<OperationLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM operation_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR description LIKE ? OR ip_address LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
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
        if (dateRange != null && !dateRange.trim().isEmpty()) {
            sql.append(" AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)");
            params.add(dateRange);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM operation_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR description LIKE ? OR ip_address LIKE ?)");
            String likeKeyword = "%" + keyword.trim() + "%";
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
        if (dateRange != null && !dateRange.trim().isEmpty()) {
            sql.append(" AND created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)");
            params.add(dateRange);
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof Integer) {
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
        log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setOperation(rs.getString("operation"));
        log.setModule(rs.getString("module"));
        log.setDescription(rs.getString("description"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
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
}

