package filter;

import dao.OperationLogDAO;
import model.OperationLog;
import org.junit.jupiter.api.Test;
import support.HttpRequest;
import support.TomcatTestBase;

import java.net.InetAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Aud03ClientIpIT extends TomcatTestBase {

    @Test
    void directRequestCannotForgeAuditIpWithProxyHeaders() throws Exception {
        HttpRequest.Response response = HttpRequest.post(baseUrl() + "/project")
                .header("X-Forwarded-For", "203.0.113.91, 198.51.100.8")
                .header("X-Real-IP", "203.0.113.92")
                .header("Proxy-Client-IP", "203.0.113.93")
                .header("WL-Proxy-Client-IP", "203.0.113.94")
                .param("action", "delete").param("id", "42").execute();
        assertEquals(302, response.code());

        List<OperationLog> logs = new OperationLogDAO()
                .findByConditions(null, "POST", "项目", null, 1, 20);
        assertEquals(1, logs.size());
        OperationLog log = logs.get(0);
        assertTrue(InetAddress.getByName(log.getIpAddress()).isLoopbackAddress());
        assertNull(log.getProxyIp());
        assertEquals("DENIED", log.getResult());
    }
}
