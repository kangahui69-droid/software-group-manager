package support;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Opt-in local browser preview. Run with -Dtest=LocalWebPreview; excluded from normal suites.
 * Uses a file-backed H2 database under .local-preview-data; ordinary tests remain in-memory.
 */
public class LocalWebPreview {

    @Test
    void serveUntilStopped() throws Exception {
        int port = Integer.getInteger("preview.port", 18080);
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("preview.port must be between 1024 and 65535");
        }
        EmbeddedTomcat server = new EmbeddedTomcat(port, true);
        try {
            server.start();
            System.out.println("LOCAL_WEB_PREVIEW_READY=http://127.0.0.1:" + port + "/software-group/");
            System.out.println("LOCAL_WEB_PREVIEW_DATA=.local-preview-data/preview-db.mv.db (preserved across restarts)");
            System.out.println("LOCAL_WEB_PREVIEW_INITIAL_USERS=admin,member1 (initial password: admin123)");
            new CountDownLatch(1).await();
        } finally {
            server.stop();
        }
    }
}
