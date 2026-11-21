package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import util.DBUtil;
import util.RedisUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/** Reports whether the legacy WAR can use its required database and Redis connections. */
public class ReadinessServlet extends HttpServlet {
    @FunctionalInterface
    interface Probe {
        boolean isReady() throws Exception;
    }

    private final Probe probe;

    public ReadinessServlet() {
        this(ReadinessServlet::checkDependencies);
    }

    ReadinessServlet(Probe probe) {
        this.probe = probe;
    }

    private static boolean checkDependencies() throws Exception {
        try (Connection connection = DBUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT 1");
             Jedis jedis = RedisUtil.getJedis()) {
            return result.next() && result.getInt(1) == 1 && "PONG".equals(jedis.ping());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("text/plain; charset=UTF-8");
        boolean ready;
        try {
            ready = probe.isReady();
        } catch (Exception exception) {
            ready = false;
        }
        response.setStatus(ready ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.getWriter().write(ready ? "READY\n" : "NOT_READY\n");
    }
}
