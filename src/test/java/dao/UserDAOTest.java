package dao;

import model.User;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;

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
    }

    @FastTest
    @DisplayName("findByUsernameAndPassword 错误密码应返回null")
    void should_return_null_with_wrong_password() {
        User user = userDAO.findByUsernameAndPassword("admin", "wrongpass");
        assertThat(user).isNull();
    }
}
