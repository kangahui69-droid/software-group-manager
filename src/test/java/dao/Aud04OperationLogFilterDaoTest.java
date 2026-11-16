package dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Aud04OperationLogFilterDaoTest {
    private final OperationLogDAO dao = new OperationLogDAO();

    @BeforeEach
    void prepareDatabase() throws Exception {
        H2Database.initSchema();
        H2Database.reset();
        try (Connection conn = DBUtil.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("INSERT INTO operation_log (username, operation, module, created_at) VALUES "
                    + "('before', 'POST', '项目', '2026-11-16 09:59:59'), "
                    + "('start', 'POST', '管理员', '2026-11-16 10:00:00'), "
                    + "('inside', 'PATCH', '成员', '2026-11-16 10:59:59'), "
                    + "('end', 'DELETE', '招新', '2026-11-16 11:00:00')");
        }
    }

    @Test
    void listAndCountShareInclusiveStartExclusiveEndAndCategoryFilters() {
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
