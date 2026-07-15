package dao;

import model.MemberProfile;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - MemberProfileDAO
 *
 * 验证MemberProfileDAO写操作（insert/update）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 */
@DisplayName("MemberProfileDAO 事务重载测试")
class MemberProfileDAOTransactionTest {

    private MemberProfileDAO memberProfileDAO = new MemberProfileDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private MemberProfile createTestProfile(int userId) {
        MemberProfile profile = new MemberProfile();
        profile.setUserId(userId);
        profile.setStudentId("2021" + userId);
        profile.setMajor("软件工程");
        profile.setGrade("2021");
        return profile;
    }

    // ==================== insert 事务重载 ====================

    @FastTest
    @DisplayName("insert(MemberProfile, Connection) 有参版本应能插入成员档案")
    void should_insert_profile_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            MemberProfile profile = createTestProfile(1);

            boolean result = memberProfileDAO.insert(profile, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("insert(MemberProfile, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_insert_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();

        MemberProfile profile = createTestProfile(1);
        profile.setStudentId("2021002");

        memberProfileDAO.insert(profile, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("insert(MemberProfile, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_insert_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            MemberProfile profile = createTestProfile(1);
            profile.setUserId(null);

            assertThatThrownBy(() -> memberProfileDAO.insert(profile, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("插入档案");
        }
    }

    @FastTest
    @DisplayName("insert(MemberProfile) 无参版本应能正常插入成员档案")
    void should_insert_profile_without_connection() {
        MemberProfile profile = createTestProfile(1);
        profile.setStudentId("2021003");

        boolean result = memberProfileDAO.insert(profile);

        assertThat(result).isTrue();
    }

    // ==================== update 事务重载 ====================

    @FastTest
    @DisplayName("update(MemberProfile, Connection) 有参版本应能更新成员档案")
    void should_update_profile_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            MemberProfile profile = memberProfileDAO.findByUserId(2);
            profile.setMajor("更新后的专业");
            profile.setGithub("https://github.com/test");

            boolean result = memberProfileDAO.update(profile, conn);

            assertThat(result).isTrue();
            MemberProfile updated = memberProfileDAO.findByUserId(2);
            assertThat(updated.getMajor()).isEqualTo("更新后的专业");
        }
    }

    @FastTest
    @DisplayName("update(MemberProfile, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_update_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            MemberProfile profile = memberProfileDAO.findByUserId(2);
            profile.setUserId(99999);

            assertThatThrownBy(() -> memberProfileDAO.update(profile, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("更新档案");
        }
    }

    @FastTest
    @DisplayName("update(MemberProfile) 无参版本应能正常更新成员档案")
    void should_update_profile_without_connection() {
        MemberProfile profile = memberProfileDAO.findByUserId(2);
        profile.setBlog("https://blog.example.com");

        boolean result = memberProfileDAO.update(profile);

        assertThat(result).isTrue();
        MemberProfile updated = memberProfileDAO.findByUserId(2);
        assertThat(updated.getBlog()).isEqualTo("https://blog.example.com");
    }

    // ==================== saveOrUpdate 事务重载 ====================

    @FastTest
    @DisplayName("saveOrUpdate(MemberProfile, Connection) 有参版本应能插入新档案")
    void should_insert_new_profile_with_save_or_update_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            MemberProfile profile = createTestProfile(1);
            profile.setStudentId("2021999");
            profile.setMajor("新专业");
            profile.setGrade("2021");

            boolean result = memberProfileDAO.saveOrUpdate(profile, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("saveOrUpdate(MemberProfile, Connection) 有参版本应能更新已有档案")
    void should_update_existing_profile_with_save_or_update_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            MemberProfile profile = memberProfileDAO.findByUserId(2);
            profile.setIntroduction("这是我的简介");

            boolean result = memberProfileDAO.saveOrUpdate(profile, conn);

            assertThat(result).isTrue();
            MemberProfile updated = memberProfileDAO.findByUserId(2);
            assertThat(updated.getIntroduction()).isEqualTo("这是我的简介");
        }
    }

    @FastTest
    @DisplayName("saveOrUpdate(MemberProfile) 无参版本应能正常插入或更新档案")
    void should_save_or_update_profile_without_connection() {
        MemberProfile profile = memberProfileDAO.findByUserId(2);
        profile.setGithub("https://github.com/noconn");

        boolean result = memberProfileDAO.saveOrUpdate(profile);

        assertThat(result).isTrue();
    }

    // ==================== 事务边界测试 ====================

    @FastTest
    @DisplayName("有参版本传入null连接应抛出NullPointerException")
    void should_throw_null_pointer_when_connection_is_null() {
        MemberProfile profile = createTestProfile(1);

        assertThatThrownBy(() -> memberProfileDAO.insert(profile, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("事务场景：连续多个写操作使用同一连接应成功提交")
    void should_support_transaction_with_multiple_profile_operations() throws SQLException {
        UserDAO userDAO = new UserDAO();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                model.User user = new model.User();
                user.setUsername("trans_member");
                user.setPassword("password");
                user.setName("事务成员");
                user.setEmail("transmember@example.com");
                user.setRole("MEMBER");
                user.setStatus(1);
                userDAO.insert(user, conn);

                MemberProfile profile = createTestProfile(user.getId());
                profile.setStudentId("2021999");
                memberProfileDAO.insert(profile, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            model.User foundUser = userDAO.findByUsername("trans_member");
            assertThat(foundUser).isNotNull();
            MemberProfile foundProfile = memberProfileDAO.findByUserId(foundUser.getId());
            assertThat(foundProfile).isNotNull();
            assertThat(foundProfile.getStudentId()).isEqualTo("2021999");
        }
    }
}
