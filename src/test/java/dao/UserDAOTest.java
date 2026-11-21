package dao;

import model.User;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.PasswordHasher;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserDAO 数据访问测试（H2内存库）")
class UserDAOTest {

    private UserDAO userDAO = new UserDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    @FastTest
    @DisplayName("findById 应能查到预置的管理员账号")
    void should_find_admin_by_id() {
        User admin = userDAO.findById(1);
        assertThat(admin).isNotNull();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getRole()).isEqualTo("ADMIN");
    }

    @FastTest
    @DisplayName("findById 查询不存在的ID应返回null")
    void should_return_null_when_id_not_exist() {
        User notFound = userDAO.findById(99999);
        assertThat(notFound).isNull();
    }

    @FastTest
    @DisplayName("findByUsernameAndPassword 正确密码应返回用户")
    void should_find_user_with_correct_password() {
        User user = userDAO.findByUsernameAndPassword("admin", "admin123");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(userDAO.findById(user.getId()).getPassword()).startsWith("$argon2id$");
        assertThat(userDAO.findByUsernameAndPassword("admin", "admin123")).isNotNull();
    }

    @FastTest
    @DisplayName("findByUsernameAndPassword 错误密码应返回null")
    void should_return_null_with_wrong_password() {
        User user = userDAO.findByUsernameAndPassword("admin", "wrongpass");
        assertThat(user).isNull();
        assertThat(userDAO.findById(1).getPassword()).doesNotStartWith("$argon2id$");
    }

    @FastTest
    void newAndChangedPasswordsUseArgon2() {
        User member = new User();
        member.setUsername("new-member");
        member.setPassword("initial123");
        member.setName("Test Member");
        member.setEmail("new-member@example.test");
        member.setRole("MEMBER");
        member.setStatus(1);

        assertThat(userDAO.insert(member)).isTrue();
        String firstHash = userDAO.findById(member.getId()).getPassword();
        assertThat(firstHash).startsWith("$argon2id$");
        assertThat(PasswordHasher.matches("initial123", firstHash)).isTrue();

        assertThat(userDAO.updatePassword(member.getId(), "changed123")).isTrue();
        String secondHash = userDAO.findById(member.getId()).getPassword();
        assertThat(secondHash).startsWith("$argon2id$").isNotEqualTo(firstHash);
        assertThat(userDAO.findByUsernameAndPassword("new-member", "initial123")).isNull();
        assertThat(userDAO.findByUsernameAndPassword("new-member", "changed123")).isNotNull();

        assertThat(userDAO.resetPassword(member.getId(), "reset123")).isTrue();
        User reset = userDAO.findById(member.getId());
        assertThat(reset.getPassword()).startsWith("$argon2id$");
        assertThat(reset.getMustChangePassword()).isTrue();
        assertThat(PasswordHasher.matches("reset123", reset.getPassword())).isTrue();
    }
}
