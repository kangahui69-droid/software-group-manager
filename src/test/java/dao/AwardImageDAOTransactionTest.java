package dao;

import model.Award;
import model.AwardImage;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - AwardImageDAO
 *
 * 验证AwardImageDAO写操作（insert/deleteById）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 */
@DisplayName("AwardImageDAO 事务重载测试")
class AwardImageDAOTransactionTest {

    private AwardImageDAO awardImageDAO = new AwardImageDAO();
    private AwardDAO awardDAO = new AwardDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private Award createAndInsertTestAward() {
        Award award = new Award();
        award.setName("测试奖项");
        award.setCompetition("测试竞赛");
        award.setYear(2026);
        award.setAwardStatus("PENDING");
        award.setCreatedBy(2);
        awardDAO.insert(award);
        return award;
    }

    private AwardImage createTestImage(int awardId, int fileStorageId) {
        AwardImage image = new AwardImage();
        image.setAwardId(awardId);
        image.setMemberId(2);
        image.setIsMain(false);
        image.setFileStorageId(fileStorageId);
        return image;
    }

    // ==================== insert 事务重载 ====================

    @FastTest
    @DisplayName("insert(AwardImage, Connection) 有参版本应能插入奖项图片并返回成功")
    void should_insert_image_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createAndInsertTestAward();
            AwardImage image = createTestImage(award.getId(), 1);

            boolean result = awardImageDAO.insert(image, conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("insert(AwardImage, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_insert_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();
        Award award = createAndInsertTestAward();
        AwardImage image = createTestImage(award.getId(), 1);

        awardImageDAO.insert(image, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("insert(AwardImage, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_insert_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            AwardImage image = new AwardImage();
            // 使用null值触发SQL异常
            image.setFileStorageId(1);

            assertThatThrownBy(() -> awardImageDAO.insert(image, conn))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @FastTest
    @DisplayName("insert(AwardImage) 无参版本应能正常插入奖项图片")
    void should_insert_image_without_connection() {
        Award award = createAndInsertTestAward();
        AwardImage image = createTestImage(award.getId(), 1);

        boolean result = awardImageDAO.insert(image);

        assertThat(result).isTrue();
    }

    // ==================== deleteById 事务重载 ====================

    @FastTest
    @DisplayName("deleteById(Integer, Connection) 有参版本应能删除奖项图片")
    void should_delete_image_by_id_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Award award = createAndInsertTestAward();
            AwardImage image = createTestImage(award.getId(), 1);
            awardImageDAO.insert(image);

            boolean result = awardImageDAO.deleteById(image.getId(), conn);

            assertThat(result).isTrue();
            List<AwardImage> remaining = awardImageDAO.findByAwardId(award.getId());
            assertThat(remaining).isEmpty();
        }
    }

    @FastTest
    @DisplayName("deleteById(Integer, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_delete_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertThatThrownBy(() -> awardImageDAO.deleteById(99999, conn))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @FastTest
    @DisplayName("deleteById(Integer) 无参版本应能正常删除奖项图片")
    void should_delete_image_by_id_without_connection() {
        Award award = createAndInsertTestAward();
        AwardImage image = createTestImage(award.getId(), 1);
        awardImageDAO.insert(image);

        boolean result = awardImageDAO.deleteById(image.getId());

        assertThat(result).isTrue();
    }

    // ==================== 读操作测试 ====================

    @FastTest
    @DisplayName("findByAwardId 应能查询奖项的所有图片")
    void should_find_images_by_award_id() {
        Award award = createAndInsertTestAward();
        AwardImage image1 = createTestImage(award.getId(), 1);
        AwardImage image2 = createTestImage(award.getId(), 2);
        awardImageDAO.insert(image1);
        awardImageDAO.insert(image2);

        List<AwardImage> images = awardImageDAO.findByAwardId(award.getId());

        assertThat(images).hasSize(2);
    }

    @FastTest
    @DisplayName("findByAwardIdAndMemberId 应能按奖项和成员查询图片")
    void should_find_images_by_award_id_and_member_id() {
        Award award = createAndInsertTestAward();
        AwardImage image1 = createTestImage(award.getId(), 1);
        image1.setMemberId(2);
        awardImageDAO.insert(image1);

        List<AwardImage> images = awardImageDAO.findByAwardIdAndMemberId(award.getId(), 2);

        assertThat(images).hasSize(1);
    }

    @FastTest
    @DisplayName("findByAwardId 查询不存在的奖项应返回空列表")
    void should_return_empty_list_when_award_not_exist() {
        List<AwardImage> images = awardImageDAO.findByAwardId(99999);
        assertThat(images).isEmpty();
    }

    // ==================== 事务边界测试 ====================

    @FastTest
    @DisplayName("insert with null connection should throw NullPointerException")
    void should_throw_null_pointer_when_insert_with_null_connection() {
        AwardImage image = new AwardImage();
        image.setAwardId(1);
        image.setFileStorageId(1);

        assertThatThrownBy(() -> awardImageDAO.insert(image, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("deleteById with null connection should throw NullPointerException")
    void should_throw_null_pointer_when_delete_by_id_with_null_connection() {
        assertThatThrownBy(() -> awardImageDAO.deleteById(1, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("事务场景：连续多个图片操作使用同一连接应成功提交")
    void should_support_transaction_with_multiple_image_operations() throws SQLException {
        Award award = createAndInsertTestAward();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                AwardImage image1 = createTestImage(award.getId(), 1);
                image1.setIsMain(true);
                awardImageDAO.insert(image1, conn);

                AwardImage image2 = createTestImage(award.getId(), 2);
                image2.setIsMain(false);
                awardImageDAO.insert(image2, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            List<AwardImage> images = awardImageDAO.findByAwardId(award.getId());
            assertThat(images).hasSize(2);
        }
    }

    @FastTest
    @DisplayName("事务场景：中间操作失败应回滚之前的操作")
    void should_rollback_when_image_operation_fails_in_transaction() throws SQLException {
        Award award = createAndInsertTestAward();
        AwardImage validImage = createTestImage(award.getId(), 1);
        awardImageDAO.insert(validImage);

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AwardImage image2 = createTestImage(award.getId(), 2);
                awardImageDAO.insert(image2, conn);

                // 插入会失败的数据（null值触发异常）
                AwardImage badImage = new AwardImage();
                // awardId 不设置，触发异常
                badImage.setFileStorageId(3);
                awardImageDAO.insert(badImage, conn);

                conn.commit();
                fail("Expected RuntimeException was not thrown");
            } catch (RuntimeException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }

            // 验证有效图片仍然存在（回滚生效）
            List<AwardImage> images = awardImageDAO.findByAwardId(award.getId());
            assertThat(images).hasSize(1);
        }
    }

    @FastTest
    @DisplayName("事务场景：连续删除操作应成功")
    void should_support_transaction_with_multiple_delete_operations() throws SQLException {
        Award award = createAndInsertTestAward();
        AwardImage image1 = createTestImage(award.getId(), 1);
        AwardImage image2 = createTestImage(award.getId(), 2);
        awardImageDAO.insert(image1);
        awardImageDAO.insert(image2);

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                awardImageDAO.deleteById(image1.getId(), conn);
                awardImageDAO.deleteById(image2.getId(), conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            List<AwardImage> images = awardImageDAO.findByAwardId(award.getId());
            assertThat(images).isEmpty();
        }
    }

    @FastTest
    @DisplayName("多DAO同一事务：Award和AwardImage使用同一连接应成功")
    void should_support_multi_dao_transaction_with_same_connection() throws SQLException {
        int awardId;
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Award award = new Award();
                award.setName("多DAO事务奖项");
                award.setCompetition("多DAO事务竞赛");
                award.setYear(2026);
                award.setAwardStatus("APPROVED");
                award.setCreatedBy(2);
                awardDAO.insert(award, conn);

                awardId = award.getId();
                AwardImage image = createTestImage(awardId, 1);
                awardImageDAO.insert(image, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        List<AwardImage> images = awardImageDAO.findByAwardId(awardId);
        assertThat(images).hasSize(1);
    }
}
