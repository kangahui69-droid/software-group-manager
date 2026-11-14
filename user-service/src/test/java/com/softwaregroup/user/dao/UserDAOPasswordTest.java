package com.softwaregroup.user.dao;

import com.softwaregroup.user.model.entity.User;
import com.softwaregroup.user.util.DESUtil;
import com.softwaregroup.user.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOPasswordTest {
    private JdbcTemplate jdbc;
    private UserDAO dao;
    private PasswordHasher hasher;
    private DESUtil legacyDes;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource source = new DriverManagerDataSource(
                "jdbc:h2:mem:passwords;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER", "sa", "");
        jdbc = new JdbcTemplate(source);
        jdbc.execute("DROP TABLE IF EXISTS user");
        jdbc.execute("CREATE TABLE user (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50), "
                + "password VARCHAR(255) NOT NULL, name VARCHAR(50), email VARCHAR(100), phone VARCHAR(30), "
                + "role VARCHAR(20), status INT, must_change_password INT DEFAULT 0)");
        legacyDes = new DESUtil("(^&%gasie_%^)");
        hasher = new PasswordHasher(legacyDes);
        dao = new UserDAO(jdbc, hasher);
    }

    @Test
    void legacyLoginUpgradesOnceAndWrongPasswordDoesNotUpgrade() {
        String legacy = legacyDes.encrypt("old-password");
        jdbc.update("INSERT INTO user(username,password,role,status) VALUES (?,?,?,1)", "legacy", legacy, "MEMBER");
        assertNull(dao.findByUsernameAndPassword("legacy", "wrong"));
        assertEquals(legacy, stored("legacy"));

        assertNotNull(dao.findByUsernameAndPassword("legacy", "old-password"));
        String upgraded = stored("legacy");
        assertTrue(upgraded.startsWith("$argon2id$"));
        assertTrue(hasher.matches("old-password", upgraded));
        assertNotNull(dao.findByUsernameAndPassword("legacy", "old-password"));
        assertEquals(upgraded, stored("legacy"));
    }

    @Test
    void createChangeAndResetStoreOnlyArgon2id() {
        User user = new User();
        user.setUsername("new-member");
        user.setPassword("initial123");
        user.setRole("MEMBER");
        user.setStatus(1);
        Integer id = dao.insert(user);
        assertNotNull(id);
        assertTrue(stored("new-member").startsWith("$argon2id$"));
        assertTrue(hasher.matches("initial123", stored("new-member")));

        assertTrue(dao.updatePassword(id, "changed123"));
        assertNull(dao.findByUsernameAndPassword("new-member", "initial123"));
        assertNotNull(dao.findByUsernameAndPassword("new-member", "changed123"));

        assertTrue(dao.resetPassword(id, "reset123"));
        assertTrue(stored("new-member").startsWith("$argon2id$"));
        assertEquals(1, jdbc.queryForObject("SELECT must_change_password FROM user WHERE id=?", Integer.class, id));
        assertTrue(hasher.matches("reset123", stored("new-member")));
    }

    @Test
    void legacyUpgradeCannotOverwriteConcurrentPasswordChange() {
        String legacy = legacyDes.encrypt("old-password");
        jdbc.update("INSERT INTO user(username,password,role,status) VALUES (?,?,?,1)", "legacy", legacy, "MEMBER");
        int id = jdbc.queryForObject("SELECT id FROM user WHERE username=?", Integer.class, "legacy");
        String newHash = hasher.hash("new-password");
        jdbc.update("UPDATE user SET password=? WHERE id=?", newHash, id);

        assertFalse(dao.updatePasswordIfCurrent(id, legacy, hasher.hash("old-password")));
        assertEquals(newHash, stored("legacy"));
    }

    private String stored(String username) {
        return jdbc.queryForObject("SELECT password FROM user WHERE username=?", String.class, username);
    }
}
