package support;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

public abstract class TomcatTestBase {

    protected static EmbeddedTomcat tomcat;

    @BeforeAll
    static void startServer() throws Exception {
        tomcat = new EmbeddedTomcat();
        tomcat.start();
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (tomcat != null) tomcat.stop();
    }

    protected String baseUrl() { return tomcat.getBaseUrl(); }

    protected Map<String, String> login(String username, String password) throws Exception {
        HttpRequest.Response resp = HttpRequest.post(baseUrl() + "/login")
            .param("username", username).param("password", password).execute();
        return resp.cookies();
    }
}
