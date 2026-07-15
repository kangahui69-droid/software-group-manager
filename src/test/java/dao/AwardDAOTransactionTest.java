package dao;

import model.Award;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - AwardDAO
 *
 * 验证AwardDAO写操作（insert/update/delete）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 */
@DisplayName("AwardDAO 事务重载测试")
class AwardDAOTransactionTest {

    private AwardDAO awardDAO = new AwardDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private Award createTestAward() {
        Award award = new Award();
        award.setName("挑战杯一等奖");
        award.setCompetition("挑战杯");
        award.setYear(2026);
        award.setAwardStatus("PENDING");
        award.setCreatedBy(2);
        return award;
    }

    // ==================== insert 事务重载 ====================

    @FastTest
    @DisplayName("insert(Award, Connection) 有参版本应能插入奖项并设置生成的主键ID")
    void should_insert_award_with_connection_and_set_generated_id() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();

            boolean result = awardDAO.insert(award, conn);

            assertThat(result).isTrue();
            assertThat(award.getId()).isGreaterThan(0);
            Award found = awardDAO.findById(award.getId());
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("挑战杯一等奖");
        }
    }

    @FastTest
    @DisplayName("insert(Award, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_insert_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();

        Award award = createTestAward();
        award.setName("不关闭测试");

        awardDAO.insert(award, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("insert(Award, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_insert_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            award.setName(null); // 导致SQL错误

            assertThatThrownBy(() -> awardDAO.insert(award, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("插入奖项");
        }
    }

    @FastTest
    @DisplayName("insert(Award) 无参版本应能正常插入奖项")
    void should_insert_award_without_connection() {
        Award award = createTestAward();
        award.setName("无连接奖项");

        boolean result = awardDAO.insert(award);

        assertThat(result).isTrue();
        assertThat(award.getId()).isGreaterThan(0);
    }

    // ==================== update 事务重载 ====================

    @FastTest
    @DisplayName("update(Award, Connection) 有参版本应能更新奖项信息")
    void should_update_award_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            awardDAO.insert(award);

            award.setName("更新后的奖项名");
            award.setCompetition("更新后的竞赛名");

            boolean result = awardDAO.update(award, conn);

            assertThat(result).isTrue();
            Award updated = awardDAO.findById(award.getId());
            assertThat(updated.getName()).isEqualTo("更新后的奖项名");
        }
    }

    @FastTest
    @DisplayName("update(Award, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_update_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            award.setId(99999); // 不存在的奖项

            assertThatThrownBy(() -> awardDAO.update(award, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("更新奖项");
        }
    }

    @FastTest
    @DisplayName("update(Award) 无参版本应能正常更新奖项")
    void should_update_award_without_connection() {
        Award award = createTestAward();
        awardDAO.insert(award);

        award.setName("无连接更新");
        boolean result = awardDAO.update(award);

        assertThat(result).isTrue();
        Award updated = awardDAO.findById(award.getId());
        assertThat(updated.getName()).isEqualTo("无连接更新");
    }

    // ==================== updateAwardStatus 事务重载 ====================

    @FastTest
    @DisplayName("updateAwardStatus(Integer, String, Connection) 有参版本应能更新奖项状态")
    void should_update_award_status_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            awardDAO.insert(award);

            boolean result = awardDAO.updateAwardStatus(award.getId(), "APPROVED", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("updateAwardStatus(Integer, String) 无参版本应能正常更新状态")
    void should_update_award_status_without_connection() {
        Award award = createTestAward();
        awardDAO.insert(award);

        boolean result = awardDAO.updateAwardStatus(award.getId(), "APPROVED");

        assertThat(result).isTrue();
    }

    // ==================== approveAward 事务重载 ====================

    @FastTest
    @DisplayName("approveAward(Integer, Integer, Connection) 有参版本应能审批通过奖项")
    void should_approve_award_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            awardDAO.insert(award);

            boolean result = awardDAO.approveAward(award.getId(), 1, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("approveAward(Integer, Integer) 无参版本应能正常审批通过")
    void should_approve_award_without_connection() {
        Award award = createTestAward();
        awardDAO.insert(award);

        boolean result = awardDAO.approveAward(award.getId(), 1);

        assertThat(result).isTrue();
    }

    // ==================== rejectAward 事务重载 ====================

    @FastTest
    @DisplayName("rejectAward(Integer, Integer, Connection) 有参版本应能拒绝奖项")
    void should_reject_award_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            awardDAO.insert(award);

            boolean result = awardDAO.rejectAward(award.getId(), 1, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("rejectAward(Integer, Integer) 无参版本应能正常拒绝奖项")
    void should_reject_award_without_connection() {
        Award award = createTestAward();
        awardDAO.insert(award);

        boolean result = awardDAO.rejectAward(award.getId(), 1);

        assertThat(result).isTrue();
    }

    // ==================== addMember 事务重载 ====================

    @FastTest
    @DisplayName("addMember(Integer, Integer, Connection) 有参版本应能添加奖项成员关联")
    void should_add_award_member_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            award.setAwardStatus("APPROVED");
            awardDAO.insert(award);

            boolean result = awardDAO.addMember(award.getId(), 2, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("addMember(Integer, Integer) 无参版本应能正常添加成员关联")
    void should_add_award_member_without_connection() {
        Award award = createTestAward();
        award.setAwardStatus("APPROVED");
        awardDAO.insert(award);

        boolean result = awardDAO.addMember(award.getId(), 2);

        assertThat(result).isTrue();
    }

    // ==================== removeMember 事务重载 ====================

    @FastTest
    @DisplayName("removeMember(Integer, Integer, Connection) 有参版本应能移除奖项成员关联")
    void should_remove_award_member_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            award.setAwardStatus("APPROVED");
            awardDAO.insert(award);
            awardDAO.addMember(award.getId(), 2);

            boolean result = awardDAO.removeMember(award.getId(), 2, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("removeMember(Integer, Integer) 无参版本应能正常移除成员关联")
    void should_remove_award_member_without_connection() {
        Award award = createTestAward();
        award.setAwardStatus("APPROVED");
        awardDAO.insert(award);
        awardDAO.addMember(award.getId(), 2);

        boolean result = awardDAO.removeMember(award.getId(), 2);

        assertThat(result).isTrue();
    }

    // ==================== delete 事务重载 ====================

    @FastTest
    @DisplayName("delete(Integer, Connection) 有参版本应能软删除奖项")
    void should_delete_award_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createTestAward();
            awardDAO.insert(award);

            boolean result = awardDAO.delete(award.getId(), conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("delete(Integer, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_delete_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertThatThrownBy(() -> awardDAO.delete(99999, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("删除奖项");
        }
    }

    @FastTest
    @DisplayName("delete(Integer) 无参版本应能正常软删除奖项")
    void should_delete_award_without_connection() {
        Award award = createTestAward();
        awardDAO.insert(award);

        boolean result = awardDAO.delete(award.getId());

        assertThat(result).isTrue();
    }

    // ==================== 事务边界测试 ====================

    @FastTest
    @DisplayName("有参版本传入null连接应抛出NullPointerException")
    void should_throw_null_pointer_when_connection_is_null() {
        Award award = createTestAward();

        assertThatThrownBy(() -> awardDAO.insert(award, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("事务场景：连续多个写操作使用同一连接应成功提交")
    void should_support_transaction_with_multiple_award_operations() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Award award = createTestAward();
                award.setName("事务奖项");
                awardDAO.insert(award, conn);

                award.setName("事务奖项更新");
                awardDAO.update(award, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            Award found = awardDAO.findAll().stream()
                    .filter(a -> "事务奖项更新".equals(a.getName()))
                    .findFirst()
                    .orElse(null);
            assertThat(found).isNotNull();
        }
    }
}