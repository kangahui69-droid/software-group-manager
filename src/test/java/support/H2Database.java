package support;

import config.Config;
import util.DBUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

public class H2Database {

    private static boolean initialized = false;

    public static synchronized void initSchema() {
        if (initialized) return;
        Config.reloadConfig();
        createSchema(true);
    }

    public static synchronized void initPersistentPreview(Path databaseBase) {
        if (initialized) return;
        if ("production".equalsIgnoreCase(System.getenv("APP_ENV"))) {
            throw new IllegalStateException("The local H2 preview cannot run with APP_ENV=production");
        }
        Path base = databaseBase.toAbsolutePath().normalize();
        try {
            Files.createDirectories(base.getParent());
            boolean newDatabase = !Files.exists(Path.of(base + ".mv.db"));
            Config.reloadConfig();
            String testUrl = Config.getProperty("db.url");
            if (!"org.h2.Driver".equals(Config.getProperty("db.driver"))
                    || testUrl == null || !testUrl.startsWith("jdbc:h2:mem:")) {
                throw new IllegalStateException("The local preview requires the H2 test configuration");
            }
            String options = testUrl.contains(";") ? testUrl.substring(testUrl.indexOf(';')) : "";
            String fileUrl = "jdbc:h2:file:" + base.toString().replace('\\', '/') + options;
            Field field = Config.class.getDeclaredField("properties");
            field.setAccessible(true);
            ((Properties) field.get(null)).setProperty("db.url", fileUrl);
            createSchema(newDatabase);
            System.out.println("[H2Database] 持久化预览数据库: " + base + ".mv.db");
        } catch (Exception e) {
            throw new RuntimeException("H2预览数据库初始化失败", e);
        }
    }

    private static void createSchema(boolean seedData) {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            // 先禁用外键检查，允许无序创建表
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            String sql = readResource("/h2-schema.sql");
            for (String statement : sql.split(";")) {
                String s = statement.trim();
                if (!s.isEmpty()) {
                    stmt.execute(s);
                }
            }
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            if (seedData) {
                insertBaseData(stmt);
            }
            initialized = true;
            System.out.println("[H2Database] 测试库初始化完成");
        } catch (Exception e) {
            throw new RuntimeException("H2数据库初始化失败", e);
        }
    }

    public static void reset() {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            // 全部44张表，SET REFERENTIAL_INTEGRITY=FALSE 避免外键顺序问题
            String[] tables = {
                "user_group", "user", "study_session", "resumes", "resume_submissions",
                "resume_skills", "resume_projects", "resume_educations", "resume_awards",
                "recruit_application", "project_progress", "project_plan",
                "project_member_application", "project_member", "project_label",
                "project_image", "project_history", "project_file", "project",
                "problem_report", "problem_management", "operation_log", "news",
                "member_profile", "group_message", "group_member", "file_storage",
                "dictionary", "award_member", "award_image", "award",
                "attendance_makeup", "attendance_config", "attendance",
                "ai_message", "ai_knowledge_base", "ai_faq_statistics",
                "ai_faq_knowledge", "ai_conversation_log", "ai_conversation",
                "admin_profile", "activity_participant", "activity_group", "activity"
            };
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            for (String table : tables) {
                stmt.execute("TRUNCATE TABLE " + table);
            }
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            insertBaseData(stmt);
        } catch (Exception e) {
            throw new RuntimeException("H2数据库重置失败", e);
        }
    }

    private static void insertBaseData(Statement stmt) throws Exception {
        String encryptedPwd = util.DESUtil.encrypt("admin123");
        // 用户表
        stmt.execute("INSERT INTO user (username, password, name, email, role, status) VALUES " +
            "('admin', '" + encryptedPwd + "', '系统管理员', 'admin@example.com', 'ADMIN', 1)");
        stmt.execute("INSERT INTO user (username, password, name, email, role, status) VALUES " +
            "('member1', '" + encryptedPwd + "', '测试成员', 'member@example.com', 'MEMBER', 1)");

        // 管理员档案（新schema：title/department/education/research_area/bio/status/avatar_file_id）
        stmt.execute("INSERT INTO admin_profile (user_id, title, department, status) VALUES " +
            "(1, '系统管理员', '技术部', 1)");

        // 成员档案（新schema：student_id/major/grade 为NOT NULL）
        stmt.execute("INSERT INTO member_profile (user_id, student_id, major, grade) VALUES " +
            "(2, '2021001', '软件工程', '2021')");
    }

    private static String readResource(String path) throws Exception {
        try (InputStream in = H2Database.class.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
