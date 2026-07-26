package service;

import dao.StudySessionDAO;
import dao.UserDAO;
import model.StudySession;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.sql.SQLException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StudyService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 6.1 StudyService 学习服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 核心方法：
 * - startSession(userId) - 开始学习
 * - endSession(userId) - 结束学习(获取当前进行中)
 * - autoEndSession() - 自动结束超时会话(22:00)
 * - getSessionDetail(id) - 学习记录详情
 * - listSessions(filter, page, pageSize) - 学习记录列表(分页)
 * - getMySessions(userId, page, pageSize) - 我的学习记录
 * - getTodaySession(userId) - 获取今日进行中会话
 * - getStatistics(userId) - 学习统计
 * - getWeekStatistics(userId) - 本周学习统计
 * - getConsecutiveDays(userId) - 连续学习天数
 *
 * 状态枚举：
 * - SessionStatus: ACTIVE-进行中, COMPLETED-已完成
 *
 * 业务规则：
 * - 学习时间段：每日6:00-22:00
 * - 已有进行中学时无法开始新学习
 * - 21:30前签退算早退
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StudyService 学习服务测试")
class StudyServiceTest {

    @Mock
    private StudySessionDAO studySessionDAO;

    @Mock
    private UserDAO userDAO;

    private StudyService studyService;

    @BeforeEach
    void setUp() {
        studyService = new StudyService(studySessionDAO, userDAO);
        // 默认mock：userDAO.findById对任何ID都返回有效用户
        when(userDAO.findById(anyInt())).thenReturn(createUser(1, "testuser", ROLE_MEMBER));
    }

    // ==================== 测试数据常量 ====================

    private static final Integer MEMBER_USER_ID = 1;
    private static final Integer OTHER_USER_ID = 2;
    private static final Integer ADMIN_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer SESSION_ID = 100;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 学习时段状态枚举
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_COMPLETED = "COMPLETED";

    // 分页常量
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // 学习时间段常量
    private static final int LEARNING_START_HOUR = 6;
    private static final int LEARNING_END_HOUR = 22;
    private static final int EARLY_CHECKOUT_HOUR = 21;
    private static final int EARLY_CHECKOUT_MINUTE = 30;

    // ==================== 测试初始化辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private StudySession createSession(Integer id, Integer userId, String status) {
        StudySession session = new StudySession();
        session.setId(id);
        session.setUserId(userId);
        session.setSessionDate(new Date());
        session.setCheckInTime(new Date());
        if (STATUS_COMPLETED.equals(status)) {
            session.setCheckOutTime(new Date());
            session.setDuration(60);
        }
        session.setStatus(status);
        return session;
    }

    private Map<String, Object> createStudyFilter() {
        Map<String, Object> filter = new HashMap<>();
        filter.put("startDate", new Date());
        filter.put("endDate", new Date());
        filter.put("userId", MEMBER_USER_ID);
        return filter;
    }

    private Map<String, Object> createStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", 10);
        stats.put("completedSessions", 8);
        stats.put("activeSessions", 2);
        stats.put("totalDuration", 480);
        stats.put("avgDuration", 60.0);
        return stats;
    }

    private Map<String, Object> createWeekStatistics() {
        Map<String, Object> weekStats = new HashMap<>();
        weekStats.put("weekSessions", 5);
        weekStats.put("weekDuration", 300);
        return weekStats;
    }

    // ==================== startSession 开始学习 ====================

    @Nested
    @DisplayName("startSession 开始学习")
    class StartSessionTests {

        @FastTest
        @DisplayName("正常开始学习应返回成功")
        void should_start_session_successfully() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);
            when(studySessionDAO.startStudy(any(StudySession.class))).thenReturn(SESSION_ID);

            Result result = studyService.startSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).startStudy(any(StudySession.class));
        }

        @FastTest
        @DisplayName("开始学习时应设置ACTIVE状态")
        void should_set_active_status_when_start() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);
            when(studySessionDAO.startStudy(any(StudySession.class))).thenReturn(SESSION_ID);

            studyService.startSession(MEMBER_USER_ID);

            ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
            verify(studySessionDAO).startStudy(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(STATUS_ACTIVE);
        }

        @FastTest
        @DisplayName("开始学习时应设置当前用户ID")
        void should_set_user_id_when_start() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);
            when(studySessionDAO.startStudy(any(StudySession.class))).thenReturn(SESSION_ID);

            studyService.startSession(MEMBER_USER_ID);

            ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
            verify(studySessionDAO).startStudy(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("已有进行中的学习时段应返回错误")
        void should_return_error_when_session_already_active() throws SQLException {
            StudySession activeSession = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(activeSession);

            Result result = studyService.startSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId为null开始学习应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.startSession(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在开始学习应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.startSession(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("开始学习时DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(anyInt())).thenThrow(new SQLException("数据库错误"));

            Result result = studyService.startSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("开始学习时应设置sessionDate为当前日期")
        void should_set_session_date_when_start() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);
            when(studySessionDAO.startStudy(any(StudySession.class))).thenReturn(SESSION_ID);

            studyService.startSession(MEMBER_USER_ID);

            ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
            verify(studySessionDAO).startStudy(captor.capture());
            assertThat(captor.getValue().getSessionDate()).isNotNull();
        }

        @FastTest
        @DisplayName("开始学习时应设置checkInTime为当前时间")
        void should_set_check_in_time_when_start() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);
            when(studySessionDAO.startStudy(any(StudySession.class))).thenReturn(SESSION_ID);

            studyService.startSession(MEMBER_USER_ID);

            ArgumentCaptor<StudySession> captor = ArgumentCaptor.forClass(StudySession.class);
            verify(studySessionDAO).startStudy(captor.capture());
            assertThat(captor.getValue().getCheckInTime()).isNotNull();
        }
    }

    // ==================== endSession 结束学习 ====================

    @Nested
    @DisplayName("endSession 结束学习")
    class EndSessionTests {

        @FastTest
        @DisplayName("正常结束学习应返回成功")
        void should_end_session_successfully() throws SQLException {
            StudySession activeSession = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(activeSession);
            when(studySessionDAO.endStudy(SESSION_ID)).thenReturn(1);
            when(studySessionDAO.getTodayDuration(MEMBER_USER_ID)).thenReturn(60);

            Result result = studyService.endSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).endStudy(SESSION_ID);
        }

        @FastTest
        @DisplayName("没有进行中的学习时段应返回错误")
        void should_return_error_when_no_active_session() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);

            Result result = studyService.endSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId为null结束学习应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.endSession(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在结束学习应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.endSession(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("结束学习时DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(anyInt())).thenThrow(new SQLException("数据库错误"));

            Result result = studyService.endSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("结束学习时应更新状态为COMPLETED")
        void should_set_completed_status_when_end() throws SQLException {
            StudySession activeSession = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(activeSession);
            when(studySessionDAO.endStudy(SESSION_ID)).thenReturn(1);
            when(studySessionDAO.getTodayDuration(MEMBER_USER_ID)).thenReturn(60);

            studyService.endSession(MEMBER_USER_ID);

            verify(studySessionDAO).endStudy(SESSION_ID);
        }

        @FastTest
        @DisplayName("结束学习时应设置checkOutTime")
        void should_set_check_out_time_when_end() throws SQLException {
            StudySession activeSession = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(activeSession);
            when(studySessionDAO.endStudy(SESSION_ID)).thenReturn(1);
            when(studySessionDAO.getTodayDuration(MEMBER_USER_ID)).thenReturn(60);

            studyService.endSession(MEMBER_USER_ID);

            verify(studySessionDAO).endStudy(SESSION_ID);
        }
    }

    // ==================== autoEndSession 自动结束超时会话 ====================

    @Nested
    @DisplayName("autoEndSession 自动结束超时会话(22:00)")
    class AutoEndSessionTests {

        @FastTest
        @DisplayName("自动结束所有进行中的会话应返回成功")
        void should_auto_end_all_active_sessions_successfully() throws SQLException {
            when(studySessionDAO.endAllActiveSessions()).thenReturn(5);

            Result result = studyService.autoEndSession();

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).endAllActiveSessions();
        }

        @FastTest
        @DisplayName("没有进行中的会话时应返回成功")
        void should_return_success_when_no_active_sessions() throws SQLException {
            when(studySessionDAO.endAllActiveSessions()).thenReturn(0);

            Result result = studyService.autoEndSession();

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("自动结束时会结束所有ACTIVE状态的会话")
        void should_end_all_active_sessions() throws SQLException {
            when(studySessionDAO.endAllActiveSessions()).thenReturn(3);

            studyService.autoEndSession();

            verify(studySessionDAO).endAllActiveSessions();
        }

        @FastTest
        @DisplayName("自动结束会话DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.endAllActiveSessions()).thenThrow(new SQLException("数据库错误"));

            Result result = studyService.autoEndSession();

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getSessionDetail 学习记录详情 ====================

    @Nested
    @DisplayName("getSessionDetail 学习记录详情")
    class GetSessionDetailTests {

        @FastTest
        @DisplayName("获取学习记录详情成功应返回成功")
        void should_get_session_detail_successfully() throws SQLException {
            StudySession session = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_COMPLETED);
            when(studySessionDAO.findById(SESSION_ID)).thenReturn(session);

            Result result = studyService.getSessionDetail(SESSION_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).findById(SESSION_ID);
        }

        @FastTest
        @DisplayName("学习记录不存在时应返回错误")
        void should_return_error_when_session_not_exists() throws SQLException {
            when(studySessionDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.getSessionDetail(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("id为null时应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = studyService.getSessionDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("获取ACTIVE状态的详情应成功")
        void should_get_active_session_detail() throws SQLException {
            StudySession session = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.findById(SESSION_ID)).thenReturn(session);

            Result result = studyService.getSessionDetail(SESSION_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取COMPLETED状态的详情应成功")
        void should_get_completed_session_detail() throws SQLException {
            StudySession session = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_COMPLETED);
            when(studySessionDAO.findById(SESSION_ID)).thenReturn(session);

            Result result = studyService.getSessionDetail(SESSION_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("获取详情时应包含用户信息")
        void should_include_user_info_in_detail() throws SQLException {
            StudySession session = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_COMPLETED);
            session.setUserName("测试用户");
            when(studySessionDAO.findById(SESSION_ID)).thenReturn(session);

            Result result = studyService.getSessionDetail(SESSION_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(session);
        }

        @FastTest
        @DisplayName("获取详情DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.findById(anyInt())).thenThrow(new SQLException("数据库错误"));

            Result result = studyService.getSessionDetail(SESSION_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== listSessions 学习记录列表(分页) ====================

    @Nested
    @DisplayName("listSessions 学习记录列表(分页)")
    class ListSessionsTests {

        @FastTest
        @DisplayName("获取学习记录列表成功应返回成功")
        void should_list_sessions_successfully() throws SQLException {
            List<StudySession> sessions = Arrays.asList(
                createSession(1, MEMBER_USER_ID, STATUS_COMPLETED),
                createSession(2, MEMBER_USER_ID, STATUS_COMPLETED)
            );
            when(studySessionDAO.getSessionList(isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(sessions);
            when(studySessionDAO.getTotalCount(isNull(), isNull(), isNull())).thenReturn(2);

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("学习记录列表为空时应返回空列表")
        void should_return_empty_list_when_no_sessions() throws SQLException {
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(any(), any(), any())).thenReturn(0);

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("filter为null时应返回所有记录")
        void should_return_all_records_when_filter_null() throws SQLException {
            when(studySessionDAO.getSessionList(isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(isNull(), isNull(), isNull())).thenReturn(0);

            Result result = studyService.listSessions(null, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数page为1应正常返回")
        void should_handle_page_1() throws SQLException {
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(any(), any(), any())).thenReturn(0);

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, 1, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, 0, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, -1, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize为0时应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize超过最大值时应返回错误")
        void should_return_error_when_page_size_exceeds_max() {
            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, MAX_PAGE_SIZE + 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("带筛选条件应正确传递参数")
        void should_pass_filter_correctly() throws SQLException {
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(any(), any(), any())).thenReturn(0);

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("学习记录列表应包含分页信息")
        void should_include_pagination_info() throws SQLException {
            List<StudySession> sessions = Arrays.asList(
                createSession(1, MEMBER_USER_ID, STATUS_COMPLETED)
            );
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt())).thenReturn(sessions);
            when(studySessionDAO.getTotalCount(any(), any(), any())).thenReturn(10);

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("学习记录DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new SQLException("数据库错误"));

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getMySessions 我的学习记录 ====================

    @Nested
    @DisplayName("getMySessions 我的学习记录")
    class GetMySessionsTests {

        @FastTest
        @DisplayName("获取我的学习记录成功应返回成功")
        void should_get_my_sessions_successfully() throws SQLException {
            List<StudySession> sessions = Arrays.asList(
                createSession(1, MEMBER_USER_ID, STATUS_COMPLETED)
            );
            when(studySessionDAO.getSessionList(eq(MEMBER_USER_ID), any(), any(), anyInt(), anyInt()))
                .thenReturn(sessions);
            when(studySessionDAO.getTotalCount(eq(MEMBER_USER_ID), any(), any())).thenReturn(1);

            Result result = studyService.getMySessions(MEMBER_USER_ID, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).getSessionList(eq(MEMBER_USER_ID), any(), any(), anyInt(), anyInt());
        }

        @FastTest
        @DisplayName("无学习记录应返回空列表")
        void should_return_empty_list_when_no_sessions() throws SQLException {
            when(studySessionDAO.getSessionList(eq(MEMBER_USER_ID), any(), any(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(eq(MEMBER_USER_ID), any(), any())).thenReturn(0);

            Result result = studyService.getMySessions(MEMBER_USER_ID, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null获取记录应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.getMySessions(null, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取记录应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.getMySessions(NONEXISTENT_USER_ID, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = studyService.getMySessions(MEMBER_USER_ID, 0, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = studyService.getMySessions(MEMBER_USER_ID, -1, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize为0时应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = studyService.getMySessions(MEMBER_USER_ID, DEFAULT_PAGE, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("应只返回指定用户的记录")
        void should_only_return_user_sessions() throws SQLException {
            when(studySessionDAO.getSessionList(eq(MEMBER_USER_ID), any(), any(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(eq(MEMBER_USER_ID), any(), any())).thenReturn(0);

            studyService.getMySessions(MEMBER_USER_ID, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            verify(studySessionDAO).getSessionList(eq(MEMBER_USER_ID), any(), any(), anyInt(), anyInt());
        }
    }

    // ==================== getTodaySession 获取今日进行中会话 ====================

    @Nested
    @DisplayName("getTodaySession 获取今日进行中会话")
    class GetTodaySessionTests {

        @FastTest
        @DisplayName("获取今日进行中会话成功应返回成功")
        void should_get_today_session_successfully() throws SQLException {
            StudySession session = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(session);

            Result result = studyService.getTodaySession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).getTodayActiveSession(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("今日无进行中会话应返回成功但data为null")
        void should_return_success_when_no_today_session() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(null);

            Result result = studyService.getTodaySession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNull();
        }

        @FastTest
        @DisplayName("userId为null获取会话应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.getTodaySession(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取会话应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.getTodaySession(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("获取今日会话DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getTodayActiveSession(anyInt()))
                .thenThrow(new SQLException("数据库错误"));

            Result result = studyService.getTodaySession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getStatistics 学习统计 ====================

    @Nested
    @DisplayName("getStatistics 学习统计")
    class GetStatisticsTests {

        @FastTest
        @DisplayName("获取学习统计成功应返回成功")
        void should_get_statistics_successfully() throws SQLException {
            Map<String, Object> stats = createStatistics();
            when(studySessionDAO.getStatistics(eq(MEMBER_USER_ID), any(), any())).thenReturn(stats);

            Result result = studyService.getStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).getStatistics(eq(MEMBER_USER_ID), any(), any());
        }

        @FastTest
        @DisplayName("userId为null获取统计应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.getStatistics(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取统计应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.getStatistics(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("无学习记录时应返回零值统计")
        void should_return_zero_stats_when_no_sessions() throws SQLException {
            Map<String, Object> zeroStats = new HashMap<>();
            zeroStats.put("totalSessions", 0);
            zeroStats.put("completedSessions", 0);
            zeroStats.put("activeSessions", 0);
            zeroStats.put("totalDuration", 0);
            zeroStats.put("avgDuration", 0.0);
            when(studySessionDAO.getStatistics(anyInt(), any(), any())).thenReturn(zeroStats);

            Result result = studyService.getStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("统计数据应包含所有统计项")
        void should_contain_all_stats_fields() throws SQLException {
            Map<String, Object> stats = createStatistics();
            when(studySessionDAO.getStatistics(anyInt(), any(), any())).thenReturn(stats);

            Result result = studyService.getStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsKeys("totalSessions", "completedSessions", "activeSessions", "totalDuration", "avgDuration");
        }

        @FastTest
        @DisplayName("获取统计DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getStatistics(anyInt(), any(), any()))
                .thenThrow(new SQLException("数据库错误"));

            Result result = studyService.getStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getWeekStatistics 本周学习统计 ====================

    @Nested
    @DisplayName("getWeekStatistics 本周学习统计")
    class GetWeekStatisticsTests {

        @FastTest
        @DisplayName("获取本周学习统计成功应返回成功")
        void should_get_week_statistics_successfully() throws SQLException {
            Map<String, Object> weekStats = createWeekStatistics();
            when(studySessionDAO.getWeekStatistics(MEMBER_USER_ID)).thenReturn(weekStats);

            Result result = studyService.getWeekStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).getWeekStatistics(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("userId为null获取本周统计应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.getWeekStatistics(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取本周统计应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.getWeekStatistics(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("本周无学习应返回零值统计")
        void should_return_zero_stats_when_no_week_sessions() throws SQLException {
            Map<String, Object> zeroStats = new HashMap<>();
            zeroStats.put("weekSessions", 0);
            zeroStats.put("weekDuration", 0);
            when(studySessionDAO.getWeekStatistics(anyInt())).thenReturn(zeroStats);

            Result result = studyService.getWeekStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("本周统计数据应包含学习次数和时长")
        void should_contain_week_sessions_and_duration() throws SQLException {
            Map<String, Object> weekStats = createWeekStatistics();
            when(studySessionDAO.getWeekStatistics(anyInt())).thenReturn(weekStats);

            Result result = studyService.getWeekStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsKeys("weekSessions", "weekDuration");
        }

        @FastTest
        @DisplayName("获取本周统计DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getWeekStatistics(anyInt()))
                .thenThrow(new SQLException("数据库错误"));

            Result result = studyService.getWeekStatistics(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== getConsecutiveDays 连续学习天数 ====================

    @Nested
    @DisplayName("getConsecutiveDays 连续学习天数")
    class GetConsecutiveDaysTests {

        @FastTest
        @DisplayName("获取连续学习天数成功应返回成功")
        void should_get_consecutive_days_successfully() throws SQLException {
            when(studySessionDAO.getConsecutiveDays(MEMBER_USER_ID)).thenReturn(5);

            Result result = studyService.getConsecutiveDays(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO).getConsecutiveDays(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("userId为null获取连续天数应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = studyService.getConsecutiveDays(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取连续天数应返回错误")
        void should_return_error_when_user_not_exists() {
            when(userDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = studyService.getConsecutiveDays(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("无连续学习应返回0天")
        void should_return_zero_when_no_consecutive_days() throws SQLException {
            when(studySessionDAO.getConsecutiveDays(MEMBER_USER_ID)).thenReturn(0);

            Result result = studyService.getConsecutiveDays(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("返回数据应为整数天数")
        void should_return_integer_days() throws SQLException {
            when(studySessionDAO.getConsecutiveDays(MEMBER_USER_ID)).thenReturn(7);

            Result result = studyService.getConsecutiveDays(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo(7);
        }

        @FastTest
        @DisplayName("获取连续天数DAO异常应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            when(studySessionDAO.getConsecutiveDays(anyInt()))
                .thenThrow(new SQLException("数据库错误"));

            Result result = studyService.getConsecutiveDays(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性")
    class StatusEnumTests {

        @FastTest
        @DisplayName("学习时段状态枚举应完整")
        void session_status_enum_should_be_complete() {
            assertThat(STATUS_ACTIVE).isEqualTo("ACTIVE");
            assertThat(STATUS_COMPLETED).isEqualTo("COMPLETED");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @FastTest
        @DisplayName("学习记录列表最大分页应正常返回")
        void should_handle_max_page_size() throws SQLException {
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(any(), any(), any())).thenReturn(0);

            Map<String, Object> filter = createStudyFilter();
            Result result = studyService.listSessions(filter, DEFAULT_PAGE, Integer.MAX_VALUE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("用户DAO异常时应返回错误")
        void should_return_error_when_user_dao_throws_exception() {
            when(userDAO.findById(anyInt())).thenThrow(new RuntimeException("数据库错误"));

            Result result = studyService.startSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("学习时长计算正确应返回成功")
        void should_handle_duration_calculation() throws SQLException {
            StudySession activeSession = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(activeSession);
            when(studySessionDAO.endStudy(SESSION_ID)).thenReturn(1);
            when(studySessionDAO.getTodayDuration(MEMBER_USER_ID)).thenReturn(120);

            Result result = studyService.endSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("多个进行中的会话应正确处理")
        void should_handle_multiple_active_sessions() throws SQLException {
            StudySession activeSession = createSession(SESSION_ID, MEMBER_USER_ID, STATUS_ACTIVE);
            when(studySessionDAO.getTodayActiveSession(MEMBER_USER_ID)).thenReturn(activeSession);
            when(studySessionDAO.endStudy(SESSION_ID)).thenReturn(1);
            when(studySessionDAO.getTodayDuration(MEMBER_USER_ID)).thenReturn(60);

            Result result = studyService.endSession(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(studySessionDAO, times(1)).endStudy(SESSION_ID);
        }

        @FastTest
        @DisplayName("空filter地图应正常处理")
        void should_handle_empty_filter() throws SQLException {
            when(studySessionDAO.getSessionList(any(), any(), any(), anyInt(), anyInt())).thenReturn(Arrays.asList());
            when(studySessionDAO.getTotalCount(any(), any(), any())).thenReturn(0);

            Map<String, Object> emptyFilter = new HashMap<>();
            Result result = studyService.listSessions(emptyFilter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }
    }
}
