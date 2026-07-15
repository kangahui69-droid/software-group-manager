package support;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.nio.file.Files;

public class EmbeddedTomcat {

    private Tomcat tomcat;
    private int port;
    private String contextPath = "/software-group";
    private File docBase;
    private File tempDir;

    public int start() throws Exception {
        H2Database.initSchema();
        H2Database.reset();

        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.getConnector().setPort(0);
        tomcat.setHostname("localhost");

        tempDir = Files.createTempDirectory("tomcat-embed-").toFile();
        tempDir.deleteOnExit();
        tomcat.setBaseDir(tempDir.getAbsolutePath());

        docBase = findWebappDir();
        Context ctx = tomcat.addWebapp(contextPath, docBase.getAbsolutePath());

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
