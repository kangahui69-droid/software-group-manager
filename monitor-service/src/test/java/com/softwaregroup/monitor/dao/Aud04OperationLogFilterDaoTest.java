package com.softwaregroup.monitor.dao;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Aud04OperationLogFilterDaoTest {
    @Test
    void monitorUsesTheSameRangeBoundariesAndLegacyDateMeaningAsTheWar() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:aud04_monitor;MODE=MySQL;DB_CLOSE_DELAY=-1"));
        jdbc.execute("CREATE TABLE operation_log (id INT PRIMARY KEY, username VARCHAR(50), "
                + "operation VARCHAR(100), module VARCHAR(50), created_at TIMESTAMP)");
        jdbc.update("INSERT INTO operation_log VALUES (1, 'before', 'POST', '项目', '2026-11-16 09:59:59')");
        jdbc.update("INSERT INTO operation_log VALUES (2, 'start', 'POST', '管理员', '2026-11-16 10:00:00')");
        jdbc.update("INSERT INTO operation_log VALUES (3, 'inside', 'PATCH', '成员', '2026-11-16 10:59:59')");
        jdbc.update("INSERT INTO operation_log VALUES (4, 'end', 'DELETE', '招新', '2026-11-16 11:00:00')");

        OperationLogDAO dao = new OperationLogDAOImpl(jdbc);
        LocalDateTime start = LocalDateTime.of(2026, 11, 16, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 16, 11, 0);
        assertEquals(2, dao.findByTimeRange(null, null, null, start, end, 1, 20).size());
        assertEquals(2, dao.countByTimeRange(null, null, null, start, end));
        assertEquals(1, dao.findByTimeRange(null, "PATCH", "成员", start, end, 1, 20).size());
        assertEquals(1, dao.countByTimeRange(null, "PATCH", "成员", start, end));
        assertEquals(4, dao.findByConditions(null, null, null, "2026-11-16", 1, 20).size());
        assertEquals(4, dao.countByConditions(null, null, null, "2026-11-16"));
    }
}
