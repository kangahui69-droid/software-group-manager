package support;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class EmbeddedTomcat {

    private Tomcat tomcat;
    private int port;
    private final int requestedPort;
    private final boolean httpPreview;
    private String contextPath = "/software-group";
    private File docBase;
    private File tempDir;

    public EmbeddedTomcat() {
        this(0);
    }

    public EmbeddedTomcat(int requestedPort) {
        this(requestedPort, false);
    }

    public EmbeddedTomcat(int requestedPort, boolean httpPreview) {
        this.requestedPort = requestedPort;
        this.httpPreview = httpPreview;
    }

    public int start() throws Exception {
        if (httpPreview) {
            System.setProperty("preview.storageBaseDir", ".local-preview-data/localstorage");
            H2Database.initPersistentPreview(Path.of(System.getProperty("preview.databaseBase",
                    ".local-preview-data/preview-db")));
        } else {
            H2Database.initSchema();
            H2Database.reset();
        }

        tomcat = new Tomcat();
        tomcat.setPort(requestedPort);
        tomcat.getConnector().setPort(requestedPort);
        if (httpPreview) {
            tomcat.getConnector().setProperty("address", "127.0.0.1");
        }
        tomcat.setHostname("localhost");

        tempDir = Files.createTempDirectory("tomcat-embed-").toFile();
        tempDir.deleteOnExit();
        tomcat.setBaseDir(tempDir.getAbsolutePath());

        docBase = findWebappDir();
        Context ctx = tomcat.addWebapp(contextPath, docBase.getAbsolutePath());
        if (httpPreview) {
            // The production descriptor requires HTTPS-only cookies. This isolated HTTP
            // preview needs a session cookie for the login form's CSRF validation.
            Path descriptor = new File(docBase, "WEB-INF/web.xml").toPath();
            String webXml = Files.readString(descriptor, StandardCharsets.UTF_8);
            String secureCookie = "<secure>true</secure>";
            if (webXml.indexOf(secureCookie) < 0 || webXml.indexOf(secureCookie) != webXml.lastIndexOf(secureCookie)) {
                throw new IllegalStateException("Expected exactly one secure session-cookie setting in web.xml");
            }
            Path previewDescriptor = new File(tempDir, "preview-web.xml").toPath();
            Files.writeString(previewDescriptor, webXml.replace(secureCookie, "<secure>false</secure>"), StandardCharsets.UTF_8);
            ctx.setAltDDName(previewDescriptor.toAbsolutePath().toString());
        }

        // 设置类加载器为当前classloader，确保能加载target/classes里的类
        ctx.setParentClassLoader(Thread.currentThread().getContextClassLoader());

        tomcat.start();
        port = tomcat.getConnector().getLocalPort();
        System.out.println("[EmbeddedTomcat] 启动成功 http://localhost:" + port + contextPath);
        return port;
    }

    public void stop() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
        if (tempDir != null) deleteDir(tempDir);
    }

    public String getBaseUrl() { return "http://localhost:" + port + contextPath; }

    public void setSessionTimeout(String sessionId, int seconds) throws Exception {
        Context context = (Context) tomcat.getHost().findChild(contextPath);
        org.apache.catalina.Session session = context.getManager().findSession(sessionId);
        if (session == null) throw new IllegalArgumentException("Session not found: " + sessionId);
        session.setMaxInactiveInterval(seconds);
    }

    private File findWebappDir() {
        String[] candidates = {"src/main/webapp", "../src/main/webapp"};
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists() && f.isDirectory() && new File(f, "WEB-INF/web.xml").exists()) {
                try { return f.getCanonicalFile(); } catch (Exception e) { return f.getAbsoluteFile(); }
            }
        }
        throw new RuntimeException("找不到webapp目录，请在项目根目录运行测试");
    }

    private void deleteDir(File dir) {
        File[] children = dir.listFiles();
        if (children != null) { for (File c : children) { if (c.isDirectory()) deleteDir(c); else c.delete(); } }
        dir.delete();
    }
}
