package dao;

import model.OperationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud02OperationLogDaoTest {
    private final OperationLogDAO dao = new OperationLogDAO();

    @BeforeEach
    void prepareDatabase() {
        H2Database.initSchema();
        H2Database.reset();
    }

    @Test
    void structuredMetadataSurvivesInsertAndRead() {
        OperationLog log = new OperationLog();
        log.setUsername("aud02-writer");
        log.setOperation("POST");
        log.setModule("项目");
        log.setDescription("POST 更新项目");
        log.setResult("FAILED");
        log.setHttpStatus(409);
        log.setDurationMs(18L);
        log.setRequestId("18b8ad79-8353-453b-9665-d55a8206464a");
        log.setErrorCode("HTTP_409");
        log.setTargetType("PROJECT");
        log.setTargetId("42");

        assertTrue(dao.insert(log));
        List<OperationLog> rows = dao.findByConditions("aud02-writer", null, null, null, 1, 20);
        assertEquals(1, rows.size());
        OperationLog saved = rows.get(0);
        assertEquals("FAILED", saved.getResult());
        assertEquals(409, saved.getHttpStatus());
        assertEquals(18L, saved.getDurationMs());
        assertEquals(log.getRequestId(), saved.getRequestId());
        assertEquals("HTTP_409", saved.getErrorCode());
        assertEquals("PROJECT", saved.getTargetType());
        assertEquals("42", saved.getTargetId());
        assertNull(saved.getUserId());
        assertEquals(1, dao.countByConditions("18b8ad79-8353", null, null, null));
        assertEquals(1, dao.findByConditions("18b8ad79-8353", null, null, null, 1, 20).size());
    }

    @Test
    void historicalRowsWithoutMetadataRemainReadable() throws Exception {
        try (Connection conn = DBUtil.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute("INSERT INTO operation_log (username, operation) "
                    + "VALUES ('aud02-history', 'POST')");
        }
        OperationLog historical = dao.findByConditions("aud02-history", null, null, null, 1, 20).get(0);
        assertNull(historical.getResult());
        assertNull(historical.getHttpStatus());
        assertNull(historical.getDurationMs());
        assertNull(historical.getRequestId());
    }
}
