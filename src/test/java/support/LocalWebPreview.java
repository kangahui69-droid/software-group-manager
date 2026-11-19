package support;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Opt-in local browser preview. Run with -Dtest=LocalWebPreview; excluded from normal suites.
 * Uses an in-memory H2 database and the test-only accounts seeded by H2Database.
 */
public class LocalWebPreview {

    @Test
    void serveUntilStopped() throws Exception {
        int port = Integer.getInteger("preview.port", 18080);
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("preview.port must be between 1024 and 65535");
        }
        EmbeddedTomcat server = new EmbeddedTomcat(port);
        try {
            server.start();
            System.out.println("LOCAL_WEB_PREVIEW_READY=http://127.0.0.1:" + port + "/software-group/");
            System.out.println("LOCAL_WEB_PREVIEW_TEST_USERS=admin,member1 (password: admin123)");
            new CountDownLatch(1).await();
        } finally {
            server.stop();
        }
    }
}
