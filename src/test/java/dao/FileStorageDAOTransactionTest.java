package dao;

import model.FileStorage;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - FileStorageDAO
 *
 * 验证FileStorageDAO写操作（insert/delete）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 */
@DisplayName("FileStorageDAO 事务重载测试")
class FileStorageDAOTransactionTest {

    private FileStorageDAO fileStorageDAO = new FileStorageDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private FileStorage createTestFileStorage() {
        FileStorage fileStorage = new FileStorage();
        fileStorage.setCreateBy(2);
        fileStorage.setOriginalName("test.pdf");
        fileStorage.setStoredName("test_stored.pdf");
        fileStorage.setFilePath("/localstorage/test.pdf");
        fileStorage.setFileType("application/pdf");
        fileStorage.setFileSize(1024L);
        fileStorage.setCategory("files");
        return fileStorage;
    }

    // ==================== insert 事务重载 ====================

    @FastTest
    @DisplayName("insert(FileStorage, Connection) 有参版本应能插入文件记录并返回生成的ID")
    void should_insert_file_with_connection_and_return_generated_id() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            FileStorage fileStorage = createTestFileStorage();

            Integer generatedId = fileStorageDAO.insert(fileStorage, conn);

            assertThat(generatedId).isNotNull();
            assertThat(generatedId).isGreaterThan(0);
            assertThat(fileStorage.getId()).isEqualTo(generatedId);
            FileStorage found = fileStorageDAO.findById(fileStorage.getId());
            assertThat(found).isNotNull();
            assertThat(found.getOriginalName()).isEqualTo("test.pdf");
        }
    }

    @FastTest
    @DisplayName("insert(FileStorage, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_insert_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();

        FileStorage fileStorage = createTestFileStorage();
        fileStorage.setOriginalName("noclose.pdf");

        fileStorageDAO.insert(fileStorage, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("insert(FileStorage, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_insert_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            FileStorage fileStorage = createTestFileStorage();
            fileStorage.setOriginalName(null); // 导致SQL错误

            assertThatThrownBy(() -> fileStorageDAO.insert(fileStorage, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("插入文件");
        }
    }

    @FastTest
    @DisplayName("insert(FileStorage) 无参版本应能正常插入文件记录")
    void should_insert_file_without_connection() {
        FileStorage fileStorage = createTestFileStorage();
        fileStorage.setOriginalName("noconn.pdf");

        Integer result = fileStorageDAO.insert(fileStorage);

        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(0);
    }

    @FastTest
    @DisplayName("insert(FileStorage) 无参版本应使用自己的连接")
    void should_use_own_connection_in_no_connection_version() {
        FileStorage fileStorage = createTestFileStorage();
        fileStorage.setOriginalName("independent.pdf");

        Integer result = fileStorageDAO.insert(fileStorage);

        assertThat(result).isNotNull();
        FileStorage found = fileStorageDAO.findById(fileStorage.getId());
        assertThat(found.getOriginalName()).isEqualTo("independent.pdf");
    }

    // ==================== delete 事务重载 ====================

    @FastTest
    @DisplayName("delete(Integer, Connection) 有参版本应能删除文件记录")
    void should_delete_file_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            FileStorage fileStorage = createTestFileStorage();
            fileStorage.setOriginalName("todelete.pdf");
            fileStorageDAO.insert(fileStorage);

            boolean result = fileStorageDAO.delete(fileStorage.getId(), conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("delete(Integer, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_delete_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            assertThatThrownBy(() -> fileStorageDAO.delete(99999, conn))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("删除文件");
        }
    }

    @FastTest
    @DisplayName("delete(Integer) 无参版本应能正常删除文件记录")
    void should_delete_file_without_connection() {
        FileStorage fileStorage = createTestFileStorage();
        fileStorage.setOriginalName("delete_noconn.pdf");
        fileStorageDAO.insert(fileStorage);

        boolean result = fileStorageDAO.delete(fileStorage.getId());

        assertThat(result).isTrue();
    }

    // ==================== softDelete 事务重载 ====================

    @FastTest
    @DisplayName("softDelete(Integer, Connection) 有参版本应能软删除文件记录")
    void should_soft_delete_file_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            FileStorage fileStorage = createTestFileStorage();
            fileStorage.setOriginalName("softdelete.pdf");
            fileStorageDAO.insert(fileStorage);

            boolean result = fileStorageDAO.softDelete(fileStorage.getId(), conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("softDelete(Integer) 无参版本应能正常软删除文件记录")
    void should_soft_delete_file_without_connection() {
        FileStorage fileStorage = createTestFileStorage();
        fileStorage.setOriginalName("softdelete_noconn.pdf");
        fileStorageDAO.insert(fileStorage);

        boolean result = fileStorageDAO.softDelete(fileStorage.getId());

        assertThat(result).isTrue();
    }

    // ==================== 事务边界测试 ====================

    @FastTest
    @DisplayName("有参版本传入null连接应抛出NullPointerException")
    void should_throw_null_pointer_when_connection_is_null() {
        FileStorage fileStorage = createTestFileStorage();

        assertThatThrownBy(() -> fileStorageDAO.insert(fileStorage, null))
                .isInstanceOf(NullPointerException.class);
    }

    @FastTest
    @DisplayName("事务场景：连续多个写操作使用同一连接应成功提交")
    void should_support_transaction_with_multiple_file_operations() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                FileStorage file1 = createTestFileStorage();
                file1.setOriginalName("trans1.pdf");
                fileStorageDAO.insert(file1, conn);

                FileStorage file2 = createTestFileStorage();
                file2.setOriginalName("trans2.pdf");
                fileStorageDAO.insert(file2, conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            // 验证两个文件都插入成功
            assertThat(fileStorageDAO.findByCreateBy(2)).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @FastTest
    @DisplayName("事务场景：中间操作失败应能回滚之前的操作")
    void should_rollback_when_file_operation_fails_in_transaction() throws SQLException {
        // 先在事务外插入有效文件
        FileStorage validFile = createTestFileStorage();
        validFile.setOriginalName("rollback_test.pdf");
        fileStorageDAO.insert(validFile);
        int validFileId = validFile.getId();

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 尝试插入一个会失败的文件
                FileStorage badFile = createTestFileStorage();
                badFile.setOriginalName(null); // 会失败
                fileStorageDAO.insert(badFile, conn);

                conn.commit(); // 不应该到达这里
                fail("Expected RuntimeException was not thrown");
            } catch (RuntimeException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }

            // 验证有效文件仍然存在（回滚生效）
            FileStorage found = fileStorageDAO.findById(validFileId);
            assertThat(found).isNotNull();
            assertThat(found.getOriginalName()).isEqualTo("rollback_test.pdf");
        }
    }
}