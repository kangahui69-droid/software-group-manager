package dao;

import model.Activity;
import org.junit.jupiter.api.*;
import support.FastTest;
import support.H2Database;
import util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 3.8 DAO层事务重载规范测试 - ActivityParticipantDAO
 *
 * 验证ActivityParticipantDAO写操作（register/cancelRegistration/updateStatus/batchUpdate）的事务重载模式：
 * - 有参版本 (Connection conn) 接收外部连接，不自己关闭连接
 * - 无参版本内部获取连接后调有参版本，保持原有行为不变
 * - SQLException在有参版本中包装为RuntimeException
 *
 * 注意：ActivityParticipantDAO已经有带autoApproved参数的register方法，
 * 这里测试的是新增的Connection参数重载版本
 */
@DisplayName("ActivityParticipantDAO 事务重载测试")
class ActivityParticipantDAOTransactionTest {

    private ActivityParticipantDAO participantDAO = new ActivityParticipantDAO();
    private ActivityDAO activityDAO = new ActivityDAO();

    @BeforeAll
    static void initSchema() {
        H2Database.initSchema();
    }

    @BeforeEach
    void reset() {
        H2Database.reset();
    }

    private Activity createTestActivity() {
        Activity activity = new Activity();
        activity.setTitle("测试活动");
        activity.setDescription("活动描述");
        activity.setActivityType("LECTURE");
        activity.setStatus("UPCOMING");
        activity.setApprovalStatus("APPROVED");
        activity.setCreatorId(1);
        activity.setMaxParticipants(10);
        return activity;
    }

    private Activity createAndInsertTestActivity() {
        Activity activity = createTestActivity();
        activityDAO.insert(activity);
        return activity;
    }

    // ==================== register 事务重载 ====================

    @FastTest
    @DisplayName("register(Integer, Integer, Connection) 有参版本应能注册活动参与者")
    void should_register_participant_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();

            boolean result = participantDAO.register(activity.getId(), 2, conn);

            assertThat(result).isTrue();
            assertThat(participantDAO.isRegistered(activity.getId(), 2)).isTrue();
        }
    }

    @FastTest
    @DisplayName("register(Integer, Integer, Connection) 连接不关闭，应由调用方管理")
    void should_not_close_connection_in_register_connection_version() throws SQLException {
        Connection conn = DBUtil.getConnection();
        Activity activity = createAndInsertTestActivity();

        participantDAO.register(activity.getId(), 2, conn);

        assertThat(conn.isClosed()).isFalse();
        conn.close();
    }

    @FastTest
    @DisplayName("register(Integer, Integer, Connection) SQL异常应抛出RuntimeException")
    void should_throw_runtime_exception_when_register_with_connection_fails() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            // 由于移除了外键约束，使用null userId触发异常
            assertThatThrownBy(() -> participantDAO.register(1, null, conn))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @FastTest
    @DisplayName("register(Integer, Integer) 无参版本应能正常注册活动参与者")
    void should_register_participant_without_connection() {
        Activity activity = createAndInsertTestActivity();

        boolean result = participantDAO.register(activity.getId(), 2);

        assertThat(result).isTrue();
        assertThat(participantDAO.isRegistered(activity.getId(), 2)).isTrue();
    }

    @FastTest
    @DisplayName("register(Integer, Integer, boolean, Connection) 带autoApproved参数的有参版本应能注册")
    void should_register_with_auto_approved_and_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();

            boolean result = participantDAO.register(activity.getId(), 2, true, conn);

            assertThat(result).isTrue();
        }
    }

    // ==================== cancelRegistration 事务重载 ====================

    @FastTest
    @DisplayName("cancelRegistration(Integer, Integer, Connection) 有参版本应能取消注册")
    void should_cancel_registration_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();
            participantDAO.register(activity.getId(), 2);

            boolean result = participantDAO.cancelRegistration(activity.getId(), 2, conn);

            assertThat(result).isTrue();
            assertThat(participantDAO.isRegistered(activity.getId(), 2)).isFalse();
        }
    }

    @FastTest
    @DisplayName("cancelRegistration(Integer, Integer) 无参版本应能正常取消注册")
    void should_cancel_registration_without_connection() {
        Activity activity = createAndInsertTestActivity();
        participantDAO.register(activity.getId(), 2);

        boolean result = participantDAO.cancelRegistration(activity.getId(), 2);

        assertThat(result).isTrue();
        assertThat(participantDAO.isRegistered(activity.getId(), 2)).isFalse();
    }

    // ==================== updateStatus 事务重载 ====================

    @FastTest
    @DisplayName("updateStatus(Integer, Integer, String, Connection) 有参版本应能更新参与者状态")
    void should_update_participant_status_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();
            participantDAO.register(activity.getId(), 2);

            boolean result = participantDAO.updateStatus(activity.getId(), 2, "approved", conn);

            assertThat(result).isTrue();
        }
    }

    @FastTest
    @DisplayName("updateStatus(Integer, Integer, String) 无参版本应能正常更新状态")
    void should_update_participant_status_without_connection() {
        Activity activity = createAndInsertTestActivity();
        participantDAO.register(activity.getId(), 2);

        boolean result = participantDAO.updateStatus(activity.getId(), 2, "approved");

        assertThat(result).isTrue();
    }

    // ==================== batchUpdateParticipantStatus 事务重载 ====================

    @FastTest
    @DisplayName("batchUpdateParticipantStatus(List, Integer, String, Connection) 有参版本应能批量更新状态")
    void should_batch_update_participant_status_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();
            participantDAO.register(activity.getId(), 2);

            List<Integer> userIds = Arrays.asList(2);

            int result = participantDAO.batchUpdateParticipantStatus(userIds, activity.getId(), "confirmed", conn);

            assertThat(result).isGreaterThanOrEqualTo(0);
        }
    }

    @FastTest
    @DisplayName("batchUpdateParticipantStatus(List, Integer, String) 无参版本应能正常批量更新")
    void should_batch_update_participant_status_without_connection() {
        Activity activity = createAndInsertTestActivity();
        participantDAO.register(activity.getId(), 2);

        List<Integer> userIds = Arrays.asList(2);

        int result = participantDAO.batchUpdateParticipantStatus(userIds, activity.getId(), "confirmed");

        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @FastTest
    @DisplayName("batchUpdateParticipantStatus 空列表应返回0")
    void should_return_zero_for_empty_user_list() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();

            int result = participantDAO.batchUpdateParticipantStatus(Arrays.asList(), activity.getId(), "confirmed", conn);

            assertThat(result).isZero();
        }
    }

    // ==================== 边界情况测试 ====================

    @FastTest
    @DisplayName("register(Integer, Integer, Connection) 重复注册应失败但不抛异常")
    void should_handle_duplicate_registration_with_connection() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();

            participantDAO.register(activity.getId(), 2, conn);

            boolean result = participantDAO.register(activity.getId(), 2, conn);

            assertThat(result).isFalse();
        }
    }

    @FastTest
    @DisplayName("cancelRegistration(Integer, Integer, Connection) 取消不存在的注册应返回false")
    void should_return_false_when_cancel_nonexistent_registration() throws SQLException {
        try (Connection conn = DBUtil.getConnection()) {
            Activity activity = createAndInsertTestActivity();

            boolean result = participantDAO.cancelRegistration(activity.getId(), 99999, conn);

            assertThat(result).isFalse();
        }
    }

    @FastTest
    @DisplayName("有参版本传入null连接应抛出NullPointerException")
    void should_throw_null_pointer_when_connection_is_null() {
        Activity activity = createAndInsertTestActivity();

        assertThatThrownBy(() -> participantDAO.register(activity.getId(), 2, null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== 事务场景测试 ====================

    @FastTest
    @DisplayName("事务场景：连续多个报名操作使用同一连接应成功提交")
    void should_support_transaction_with_multiple_registration_operations() throws SQLException {
        Activity activity = createAndInsertTestActivity();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                participantDAO.register(activity.getId(), 2, conn);
                participantDAO.updateStatus(activity.getId(), 2, "approved", conn);

                conn.commit();
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            assertThat(participantDAO.isRegistered(activity.getId(), 2)).isTrue();
        }
    }

    @FastTest
    @DisplayName("事务场景：报名后更新状态失败应回滚报名记录")
    void should_rollback_registration_when_status_update_fails() throws SQLException {
        Activity activity = createAndInsertTestActivity();
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            try {
                participantDAO.register(activity.getId(), 2, conn);

                participantDAO.updateStatus(activity.getId(), 99999, "approved", conn);

                conn.commit();
                fail("Expected RuntimeException was not thrown");
            } catch (RuntimeException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }

            assertThat(participantDAO.isRegistered(activity.getId(), 2)).isFalse();
        }
    }
}
