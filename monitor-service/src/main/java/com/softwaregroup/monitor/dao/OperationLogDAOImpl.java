package com.softwaregroup.monitor.dao;

import com.softwaregroup.monitor.model.OperationLog;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作日志数据访问实现
 */
@Repository
public class OperationLogDAOImpl implements OperationLogDAO {

    private final JdbcTemplate jdbcTemplate;

    public OperationLogDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<OperationLog> ROW_MAPPER = BeanPropertyRowMapper.newInstance(OperationLog.class);

    @Override
    public List<OperationLog> findAll(int page, int pageSize) {
        String sql = "SELECT * FROM operation_log ORDER BY created_at DESC LIMIT ? OFFSET ?";
        int offset = (page - 1) * pageSize;
        return jdbcTemplate.query(sql, ROW_MAPPER, pageSize, offset);
    }

    @Override
    public List<OperationLog> findByConditions(String keyword, String operation, String module, String dateRange, int page, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM operation_log WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR operation LIKE ? OR module LIKE ? OR description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation=?");
            params.add(operation);
        }
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module=?");
            params.add(module);
        }
        if (dateRange != null && !dateRange.trim().isEmpty()) {
            sql.append(" AND DATE(created_at)=?");
            params.add(dateRange);
        }
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        int offset = (page - 1) * pageSize;
        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM operation_log";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public int countByConditions(String keyword, String operation, String module, String dateRange) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM operation_log WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR operation LIKE ? OR module LIKE ? OR description LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (operation != null && !operation.trim().isEmpty()) {
            sql.append(" AND operation=?");
            params.add(operation);
        }
        if (module != null && !module.trim().isEmpty()) {
            sql.append(" AND module=?");
            params.add(module);
        }
        if (dateRange != null && !dateRange.trim().isEmpty()) {
            sql.append(" AND DATE(created_at)=?");
            params.add(dateRange);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }
}
