package com.softwaregroup.monitor.dao;

import com.softwaregroup.monitor.model.OperationLog;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Aud02OperationLogMappingTest {

    @Test
    void monitorServiceReadsStructuredAuditColumns() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:aud02_monitor;MODE=MySQL;DB_CLOSE_DELAY=-1"));
        jdbc.execute("CREATE TABLE operation_log (id INT PRIMARY KEY, username VARCHAR(50), "
                + "operation VARCHAR(100), module VARCHAR(50), description VARCHAR(255), result VARCHAR(16), "
                + "http_status SMALLINT, duration_ms BIGINT, request_id CHAR(36), "
                + "error_code VARCHAR(64), target_type VARCHAR(32), target_id VARCHAR(64), proxy_ip VARCHAR(45), "
                + "created_at TIMESTAMP)");
        jdbc.update("INSERT INTO operation_log "
                        + "(id, result, http_status, duration_ms, request_id, error_code, target_type, target_id, proxy_ip, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                1, "DENIED", 403, 27L, "36d619da-5dae-463a-b6a8-d8cb4680142e",
                "HTTP_403", "PROJECT", "42", "172.20.0.5");

        List<OperationLog> logs = new OperationLogDAOImpl(jdbc).findAll(1, 20);
        assertEquals(1, logs.size());
        OperationLog log = logs.get(0);
        assertEquals("DENIED", log.getResult());
        assertEquals(403, log.getHttpStatus());
        assertEquals(27L, log.getDurationMs());
        assertEquals("36d619da-5dae-463a-b6a8-d8cb4680142e", log.getRequestId());
        assertEquals("HTTP_403", log.getErrorCode());
        assertEquals("PROJECT", log.getTargetType());
        assertEquals("42", log.getTargetId());
        assertEquals("172.20.0.5", log.getProxyIp());
        assertEquals(1, new OperationLogDAOImpl(jdbc)
                .findByConditions("36d619da-5dae", null, null, null, 1, 20).size());
    }
}
