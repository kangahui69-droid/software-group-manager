package dao;

import model.OperationLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import support.H2Database;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud03OperationLogIpDaoTest {

    @BeforeEach
    void prepareDatabase() {
        H2Database.initSchema();
        H2Database.reset();
    }

    @Test
    void clientAndProxyAddressesRoundTripSeparately() {
        OperationLog log = new OperationLog();
        log.setUsername("aud03-writer");
        log.setOperation("POST");
        log.setIpAddress("203.0.113.7");
        log.setProxyIp("172.20.0.5");

        OperationLogDAO dao = new OperationLogDAO();
        assertTrue(dao.insert(log));
        List<OperationLog> saved = dao.findByConditions("aud03-writer", null, null, null, 1, 20);
        assertEquals(1, saved.size());
        assertEquals("203.0.113.7", saved.get(0).getIpAddress());
        assertEquals("172.20.0.5", saved.get(0).getProxyIp());
    }

    @Test
    void directRequestsHaveNoProxyAddress() {
        OperationLog log = new OperationLog();
        log.setUsername("aud03-direct");
        log.setOperation("POST");
        log.setIpAddress("127.0.0.1");

        OperationLogDAO dao = new OperationLogDAO();
        assertTrue(dao.insert(log));
        assertNull(dao.findByConditions("aud03-direct", null, null, null, 1, 20)
                .get(0).getProxyIp());
    }
}
