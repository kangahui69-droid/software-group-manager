package service;

import dao.AttendanceDAO;
import dao.AttendanceMakeupDAO;
import dao.UserDAO;
import model.Attendance;
import model.AttendanceMakeup;
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
 * AttendanceService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 4.2 AttendanceService 考勤服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 核心方法：
 * - checkIn(userId) - 签到
 * - checkOut(userId) - 签退
 * - listAttendance(filter, page) - 考勤列表
 * - getAttendanceStats(userId) - 考勤统计
 * - approveMakeup(id, operatorId) - 审批补签
 * - rejectMakeup(id, operatorId) - 拒绝补签
 * - getMyAttendance(userId, page) - 我的考勤
 * - getMyStats(userId) - 我的统计
 * - applyMakeup(date, reason, userId) - 申请补签
 *
 * 状态枚举：
 * - CheckInStatus: NONE, NORMAL, LATE, LEAVE
 * - CheckOutStatus: NONE, NORMAL, EARLY, LEAVE, MISSING
 * - MakeupType: CHECK_IN, CHECK_OUT
 * - MakeupStatus: PENDING, APPROVED, REJECTED
 *
 * DAO接口假设（待实现确认）：
 * - AttendanceMakeupDAO需新增findById(id)方法用于查询单条补签记录
 * - 或使用getPendingList + 过滤的方式实现
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceService 考勤服务测试")
class AttendanceServiceTest {

    @Mock
    private AttendanceDAO attendanceDAO;

    @Mock
    private AttendanceMakeupDAO attendanceMakeupDAO;

    @Mock
    private UserDAO userDAO;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
            attendanceDAO,
            attendanceMakeupDAO,
            userDAO
        );
        // 默认mock：userDAO.findById对任何ID都返回有效用户
        doReturn(createUser(1, "admin", ROLE_ADMIN)).when(userDAO).findById(anyInt());
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer ATTENDANCE_ID = 100;
    private static final Integer MAKEUP_ID = 200;

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 签到状态枚举
    private static final String CHECK_IN_STATUS_NONE = "NONE";
    private static final String CHECK_IN_STATUS_NORMAL = "NORMAL";
    private static final String CHECK_IN_STATUS_LATE = "LATE";
    private static final String CHECK_IN_STATUS_LEAVE = "LEAVE";

    // 签退状态枚举
    private static final String CHECK_OUT_STATUS_NONE = "NONE";
    private static final String CHECK_OUT_STATUS_NORMAL = "NORMAL";
    private static final String CHECK_OUT_STATUS_EARLY = "EARLY";
    private static final String CHECK_OUT_STATUS_LEAVE = "LEAVE";
    private static final String CHECK_OUT_STATUS_MISSING = "MISSING";

    // 补签类型枚举
    private static final String MAKEUP_TYPE_CHECK_IN = "CHECK_IN";
    private static final String MAKEUP_TYPE_CHECK_OUT = "CHECK_OUT";

    // 补签状态枚举
    private static final String MAKEUP_STATUS_PENDING = "PENDING";
    private static final String MAKEUP_STATUS_APPROVED = "APPROVED";
    private static final String MAKEUP_STATUS_REJECTED = "REJECTED";

    // ==================== 测试初始化辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private Attendance createAttendance(Integer id, Integer userId, String checkInStatus, String checkOutStatus) {
        Attendance attendance = new Attendance();
        attendance.setId(id);
        attendance.setUserId(userId);
        attendance.setAttendanceDate(new Date());
        attendance.setCheckInTime(checkInStatus != CHECK_IN_STATUS_NONE ? new Date() : null);
        attendance.setCheckOutTime(checkOutStatus != CHECK_OUT_STATUS_NONE && checkOutStatus != CHECK_OUT_STATUS_MISSING ? new Date() : null);
        attendance.setCheckInStatus(checkInStatus);
        attendance.setCheckOutStatus(checkOutStatus);
        attendance.setWorkDuration(480);
        attendance.setLocation("测试地点");
        attendance.setDeviceInfo("Test Device");
        return attendance;
    }

    private Attendance createAttendanceToday(Integer id, Integer userId, boolean withCheckIn, boolean withCheckOut) {
        Attendance attendance = new Attendance();
        attendance.setId(id);
        attendance.setUserId(userId);
        attendance.setAttendanceDate(new Date());
        attendance.setCheckInTime(withCheckIn ? new Date() : null);
        attendance.setCheckOutTime(withCheckOut ? new Date() : null);
        attendance.setCheckInStatus(withCheckIn ? CHECK_IN_STATUS_NORMAL : CHECK_IN_STATUS_NONE);
        attendance.setCheckOutStatus(withCheckOut ? CHECK_OUT_STATUS_NORMAL : CHECK_OUT_STATUS_NONE);
        attendance.setWorkDuration(withCheckOut ? 480 : null);
        return attendance;
    }

    private AttendanceMakeup createMakeup(Integer id, Integer userId, String type, String status) {
        AttendanceMakeup makeup = new AttendanceMakeup();
        makeup.setId(id);
        makeup.setUserId(userId);
        makeup.setAttendanceDate(new Date());
        makeup.setMakeUpType(type);
        makeup.setApplyReason("测试原因");
        makeup.setApplyTime(new Date());
        makeup.setStatus(status);
        return makeup;
    }

    private Map<String, Object> createAttendanceFilter() {
        Map<String, Object> filter = new HashMap<>();
        filter.put("startDate", new Date());
        filter.put("endDate", new Date());
        filter.put("userId", MEMBER_USER_ID);
        return filter;
    }

    private Map<String, Object> createAttendanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDays", 20);
        stats.put("normalDays", 15);
        stats.put("lateDays", 3);
        stats.put("leaveDays", 1);
        stats.put("absentDays", 1);
        stats.put("totalWorkDuration", 9600);
        return stats;
    }

    // ==================== checkIn 签到 ====================

    @Nested
    @DisplayName("checkIn 签到")
    class CheckInTests {

        @FastTest
        @DisplayName("正常签到成功应返回成功")
        void should_check_in_successfully() throws SQLException {
            doReturn(null).when(attendanceDAO).getTodayAttendance(anyInt());
            doReturn(ATTENDANCE_ID).when(attendanceDAO).checkIn(any(Attendance.class));

            Result result = attendanceService.checkIn(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceDAO).checkIn(any(Attendance.class));
        }

        @FastTest
        @DisplayName("已签到再次签到应返回错误")
        void should_return_error_when_already_checked_in() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, false);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());

            Result result = attendanceService.checkIn(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId为null签到应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = attendanceService.checkIn(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在签到应返回错误")
        void should_return_error_when_user_not_exists() {
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.checkIn(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("用户已签退后再次签到应返回错误")
        void should_return_error_when_already_checked_out() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, true);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());

            Result result = attendanceService.checkIn(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("签到时应设置正确的签到状态")
        void should_set_correct_check_in_status() throws SQLException {
            doReturn(null).when(attendanceDAO).getTodayAttendance(anyInt());
            doReturn(ATTENDANCE_ID).when(attendanceDAO).checkIn(any(Attendance.class));

            attendanceService.checkIn(MEMBER_USER_ID);

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceDAO).checkIn(captor.capture());
            assertThat(captor.getValue().getCheckInStatus()).isIn(CHECK_IN_STATUS_NORMAL, CHECK_IN_STATUS_LATE);
        }
    }

    // ==================== checkOut 签退 ====================

    @Nested
    @DisplayName("checkOut 签退")
    class CheckOutTests {

        @FastTest
        @DisplayName("正常签退成功应返回成功")
        void should_check_out_successfully() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, false);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());
            doReturn(1).when(attendanceDAO).checkOut(any(Attendance.class));

            Result result = attendanceService.checkOut(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceDAO).checkOut(any(Attendance.class));
        }

        @FastTest
        @DisplayName("未签到就签退应返回错误")
        void should_return_error_when_not_checked_in() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, false, false);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());

            Result result = attendanceService.checkOut(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("已签退再次签退应返回错误")
        void should_return_error_when_already_checked_out() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, true);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());

            Result result = attendanceService.checkOut(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId为null签退应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = attendanceService.checkOut(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在签退应返回错误")
        void should_return_error_when_user_not_exists() {
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.checkOut(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("签退时应设置正确的签退状态")
        void should_set_correct_check_out_status() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, false);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());
            doReturn(1).when(attendanceDAO).checkOut(any(Attendance.class));

            attendanceService.checkOut(MEMBER_USER_ID);

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceDAO).checkOut(captor.capture());
            assertThat(captor.getValue().getCheckOutStatus()).isIn(CHECK_OUT_STATUS_NORMAL, CHECK_OUT_STATUS_EARLY);
        }
    }

    // ==================== listAttendance 考勤列表 ====================

    @Nested
    @DisplayName("listAttendance 考勤列表")
    class ListAttendanceTests {

        @FastTest
        @DisplayName("获取考勤列表成功应返回成功")
        void should_list_attendance_successfully() throws SQLException {
            List<Attendance> list = Arrays.asList(
                createAttendance(1, MEMBER_USER_ID, CHECK_IN_STATUS_NORMAL, CHECK_OUT_STATUS_NORMAL),
                createAttendance(2, MEMBER_USER_ID, CHECK_IN_STATUS_LATE, CHECK_OUT_STATUS_EARLY)
            );
            doReturn(list).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(2).when(attendanceDAO).getTotalCount(any(), any(), any());

            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("考勤列表为空时应返回空列表")
        void should_return_empty_list_when_no_attendance() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数page为1应正常返回")
        void should_handle_page_1() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数pageSize为大值应正常返回")
        void should_handle_large_page_size() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, 100);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("filter为null时应返回所有记录")
        void should_return_all_records_when_filter_null() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Result result = attendanceService.listAttendance(null, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, -1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("考勤列表应包含分页信息")
        void should_include_pagination_info() throws SQLException {
            List<Attendance> list = Arrays.asList(
                createAttendance(1, MEMBER_USER_ID, CHECK_IN_STATUS_NORMAL, CHECK_OUT_STATUS_NORMAL)
            );
            doReturn(list).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(10).when(attendanceDAO).getTotalCount(any(), any(), any());

            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, 1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }
    }

    // ==================== getAttendanceStats 考勤统计 ====================

    @Nested
    @DisplayName("getAttendanceStats 考勤统计")
    class GetAttendanceStatsTests {

        @FastTest
        @DisplayName("获取考勤统计成功应返回成功")
        void should_get_stats_successfully() throws SQLException {
            Map<String, Object> stats = createAttendanceStats();
            doReturn(stats).when(attendanceDAO).getStatistics(anyInt(), any(), any());

            Result result = attendanceService.getAttendanceStats(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceDAO).getStatistics(eq(MEMBER_USER_ID), any(), any());
        }

        @FastTest
        @DisplayName("userId为null获取统计应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = attendanceService.getAttendanceStats(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取统计应返回错误")
        void should_return_error_when_user_not_exists() {
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.getAttendanceStats(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("无考勤记录时应返回空统计数据")
        void should_return_empty_stats_when_no_attendance() throws SQLException {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalDays", 0);
            emptyStats.put("normalDays", 0);
            emptyStats.put("lateDays", 0);
            emptyStats.put("leaveDays", 0);
            emptyStats.put("absentDays", 0);
            emptyStats.put("totalWorkDuration", 0);
            doReturn(emptyStats).when(attendanceDAO).getStatistics(anyInt(), any(), any());

            Result result = attendanceService.getAttendanceStats(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("考勤统计数据应包含所有统计项")
        void should_contain_all_stats_fields() throws SQLException {
            Map<String, Object> stats = createAttendanceStats();
            doReturn(stats).when(attendanceDAO).getStatistics(anyInt(), any(), any());

            Result result = attendanceService.getAttendanceStats(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsKeys("totalDays", "normalDays", "lateDays", "leaveDays", "absentDays", "totalWorkDuration");
        }
    }

    // ==================== approveMakeup 审批补签 ====================

    @Nested
    @DisplayName("approveMakeup 审批补签")
    class ApproveMakeupTests {

        @FastTest
        @DisplayName("审批补签成功应返回成功")
        void should_approve_makeup_successfully() throws SQLException {
            List<AttendanceMakeup> pendingList = Arrays.asList(
                createMakeup(MAKEUP_ID, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_PENDING)
            );
            doReturn(pendingList).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());
            doReturn(1).when(attendanceMakeupDAO).approve(anyInt(), anyInt(), anyString(), anyString());

            Result result = attendanceService.approveMakeup(MAKEUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceMakeupDAO).approve(eq(MAKEUP_ID), eq(ADMIN_USER_ID), eq(MAKEUP_STATUS_APPROVED), anyString());
        }

        @FastTest
        @DisplayName("补签申请不存在审批应返回错误")
        void should_return_error_when_makeup_not_exists() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());

            Result result = attendanceService.approveMakeup(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("补签已审批再次审批应返回错误")
        void should_return_error_when_already_approved() throws SQLException {
            List<AttendanceMakeup> allList = Arrays.asList(
                createMakeup(MAKEUP_ID, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_APPROVED)
            );
            doReturn(Arrays.asList()).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());
            doReturn(allList).when(attendanceMakeupDAO).getListByUser(any(), anyInt(), anyInt());

            Result result = attendanceService.approveMakeup(MAKEUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("operatorId为null审批应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = attendanceService.approveMakeup(MAKEUP_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("operatorId不存在审批应返回错误")
        void should_return_error_when_operator_not_exists() {
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.approveMakeup(MAKEUP_ID, NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("operatorId不是管理员审批应返回错误")
        void should_return_error_when_operator_not_admin() {
            doReturn(createUser(MEMBER_USER_ID, "member", ROLE_MEMBER)).when(userDAO).findById(MEMBER_USER_ID);

            Result result = attendanceService.approveMakeup(MAKEUP_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }

        @FastTest
        @DisplayName("审批时应设置审批人为当前操作员")
        void should_set_approver_as_operator() throws SQLException {
            List<AttendanceMakeup> pendingList = Arrays.asList(
                createMakeup(MAKEUP_ID, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_PENDING)
            );
            doReturn(pendingList).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());
            doReturn(1).when(attendanceMakeupDAO).approve(anyInt(), anyInt(), anyString(), anyString());

            attendanceService.approveMakeup(MAKEUP_ID, ADMIN_USER_ID);

            verify(attendanceMakeupDAO).approve(eq(MAKEUP_ID), eq(ADMIN_USER_ID), anyString(), anyString());
        }
    }

    // ==================== rejectMakeup 拒绝补签 ====================

    @Nested
    @DisplayName("rejectMakeup 拒绝补签")
    class RejectMakeupTests {

        @FastTest
        @DisplayName("拒绝补签成功应返回成功")
        void should_reject_makeup_successfully() throws SQLException {
            List<AttendanceMakeup> pendingList = Arrays.asList(
                createMakeup(MAKEUP_ID, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_PENDING)
            );
            doReturn(pendingList).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());
            doReturn(1).when(attendanceMakeupDAO).approve(anyInt(), anyInt(), anyString(), anyString());

            Result result = attendanceService.rejectMakeup(MAKEUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceMakeupDAO).approve(eq(MAKEUP_ID), eq(ADMIN_USER_ID), eq(MAKEUP_STATUS_REJECTED), anyString());
        }

        @FastTest
        @DisplayName("补签申请不存在拒绝应返回错误")
        void should_return_error_when_makeup_not_exists() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());

            Result result = attendanceService.rejectMakeup(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("补签已审批拒绝应返回错误")
        void should_return_error_when_already_approved() throws SQLException {
            List<AttendanceMakeup> allList = Arrays.asList(
                createMakeup(MAKEUP_ID, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_APPROVED)
            );
            doReturn(Arrays.asList()).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());
            doReturn(allList).when(attendanceMakeupDAO).getListByUser(any(), anyInt(), anyInt());

            Result result = attendanceService.rejectMakeup(MAKEUP_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("operatorId为null拒绝应返回错误")
        void should_return_error_when_operator_id_null() {
            Result result = attendanceService.rejectMakeup(MAKEUP_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("拒绝时应设置审批状态为REJECTED")
        void should_set_rejected_status() throws SQLException {
            List<AttendanceMakeup> pendingList = Arrays.asList(
                createMakeup(MAKEUP_ID, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_PENDING)
            );
            doReturn(pendingList).when(attendanceMakeupDAO).getPendingList(anyInt(), anyInt());
            doReturn(1).when(attendanceMakeupDAO).approve(anyInt(), anyInt(), anyString(), anyString());

            attendanceService.rejectMakeup(MAKEUP_ID, ADMIN_USER_ID);

            verify(attendanceMakeupDAO).approve(eq(MAKEUP_ID), eq(ADMIN_USER_ID), eq(MAKEUP_STATUS_REJECTED), anyString());
        }
    }

    // ==================== getMyAttendance 我的考勤 ====================

    @Nested
    @DisplayName("getMyAttendance 我的考勤")
    class GetMyAttendanceTests {

        @FastTest
        @DisplayName("获取我的考勤成功应返回成功")
        void should_get_my_attendance_successfully() throws SQLException {
            List<Attendance> list = Arrays.asList(
                createAttendance(1, MEMBER_USER_ID, CHECK_IN_STATUS_NORMAL, CHECK_OUT_STATUS_NORMAL)
            );
            doReturn(list).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(1).when(attendanceDAO).getTotalCount(any(), any(), any());

            Result result = attendanceService.getMyAttendance(MEMBER_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceDAO).getAttendanceList(eq(MEMBER_USER_ID), any(), any(), anyInt(), anyInt());
        }

        @FastTest
        @DisplayName("无考勤记录应返回空列表")
        void should_return_empty_list_when_no_attendance() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Result result = attendanceService.getMyAttendance(MEMBER_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数page为1应正常返回")
        void should_handle_page_1() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Result result = attendanceService.getMyAttendance(MEMBER_USER_ID, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null获取考勤应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = attendanceService.getMyAttendance(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取考勤应返回错误")
        void should_return_error_when_user_not_exists() {
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.getMyAttendance(NONEXISTENT_USER_ID, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = attendanceService.getMyAttendance(MEMBER_USER_ID, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = attendanceService.getMyAttendance(MEMBER_USER_ID, -1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getMyStats 我的统计 ====================

    @Nested
    @DisplayName("getMyStats 我的统计")
    class GetMyStatsTests {

        @FastTest
        @DisplayName("获取我的统计成功应返回成功")
        void should_get_my_stats_successfully() throws SQLException {
            Map<String, Object> stats = createAttendanceStats();
            doReturn(stats).when(attendanceDAO).getStatistics(anyInt(), any(), any());

            Result result = attendanceService.getMyStats(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("userId为null获取统计应返回错误")
        void should_return_error_when_user_id_null() {
            Result result = attendanceService.getMyStats(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在获取统计应返回错误")
        void should_return_error_when_user_not_exists() {
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.getMyStats(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("无考勤记录时应返回零值统计")
        void should_return_zero_stats_when_no_attendance() throws SQLException {
            Map<String, Object> zeroStats = new HashMap<>();
            zeroStats.put("totalDays", 0);
            zeroStats.put("normalDays", 0);
            zeroStats.put("lateDays", 0);
            zeroStats.put("leaveDays", 0);
            zeroStats.put("absentDays", 0);
            zeroStats.put("totalWorkDuration", 0);
            doReturn(zeroStats).when(attendanceDAO).getStatistics(anyInt(), any(), any());

            Result result = attendanceService.getMyStats(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("统计数据应包含所有必要字段")
        void should_contain_all_required_fields() throws SQLException {
            Map<String, Object> stats = createAttendanceStats();
            doReturn(stats).when(attendanceDAO).getStatistics(anyInt(), any(), any());

            Result result = attendanceService.getMyStats(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsKeys("totalDays", "normalDays", "lateDays", "leaveDays", "absentDays", "totalWorkDuration");
        }
    }

    // ==================== applyMakeup 申请补签 ====================

    @Nested
    @DisplayName("applyMakeup 申请补签")
    class ApplyMakeupTests {

        @FastTest
        @DisplayName("申请补签成功应返回成功")
        void should_apply_makeup_successfully() throws SQLException {
            Date applyDate = new Date();
            doReturn(false).when(attendanceMakeupDAO).hasPendingApplication(anyInt(), any(), anyString());
            doReturn(MAKEUP_ID).when(attendanceMakeupDAO).apply(any(AttendanceMakeup.class));

            Result result = attendanceService.applyMakeup(applyDate, "测试原因", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(attendanceMakeupDAO).apply(any(AttendanceMakeup.class));
        }

        @FastTest
        @DisplayName("重复申请补签应返回错误")
        void should_return_error_when_duplicate_application() throws SQLException {
            Date applyDate = new Date();
            doReturn(true).when(attendanceMakeupDAO).hasPendingApplication(anyInt(), any(), anyString());

            Result result = attendanceService.applyMakeup(applyDate, "测试原因", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("date为null申请补签应返回错误")
        void should_return_error_when_date_null() {
            Result result = attendanceService.applyMakeup(null, "测试原因", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("reason为空申请补签应返回错误")
        void should_return_error_when_reason_empty() {
            Date applyDate = new Date();

            Result result = attendanceService.applyMakeup(applyDate, "", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("reason为null申请补签应返回错误")
        void should_return_error_when_reason_null() {
            Date applyDate = new Date();

            Result result = attendanceService.applyMakeup(applyDate, null, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId为null申请补签应返回错误")
        void should_return_error_when_user_id_null() {
            Date applyDate = new Date();

            Result result = attendanceService.applyMakeup(applyDate, "测试原因", null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("userId不存在申请补签应返回错误")
        void should_return_error_when_user_not_exists() {
            Date applyDate = new Date();
            doReturn(null).when(userDAO).findById(NONEXISTENT_USER_ID);

            Result result = attendanceService.applyMakeup(applyDate, "测试原因", NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("申请补签类型为CHECK_IN应正常保存")
        void should_save_check_in_makeup_type() throws SQLException {
            Date applyDate = new Date();
            doReturn(false).when(attendanceMakeupDAO).hasPendingApplication(anyInt(), any(), eq(MAKEUP_TYPE_CHECK_IN));
            doReturn(MAKEUP_ID).when(attendanceMakeupDAO).apply(any(AttendanceMakeup.class));

            Result result = attendanceService.applyMakeup(applyDate, "签到补签", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<AttendanceMakeup> captor = ArgumentCaptor.forClass(AttendanceMakeup.class);
            verify(attendanceMakeupDAO).apply(captor.capture());
            assertThat(captor.getValue().getMakeUpType()).isEqualTo(MAKEUP_TYPE_CHECK_IN);
        }

        @FastTest
        @DisplayName("申请补签类型为CHECK_OUT应正常保存")
        void should_save_check_out_makeup_type() throws SQLException {
            Date applyDate = new Date();
            doReturn(false).when(attendanceMakeupDAO).hasPendingApplication(anyInt(), any(), eq(MAKEUP_TYPE_CHECK_OUT));
            doReturn(MAKEUP_ID).when(attendanceMakeupDAO).apply(any(AttendanceMakeup.class));

            Result result = attendanceService.applyMakeup(applyDate, "签退补签", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<AttendanceMakeup> captor = ArgumentCaptor.forClass(AttendanceMakeup.class);
            verify(attendanceMakeupDAO).apply(captor.capture());
            assertThat(captor.getValue().getMakeUpType()).isEqualTo(MAKEUP_TYPE_CHECK_OUT);
        }

        @FastTest
        @DisplayName("申请时应设置申请原因为指定值")
        void should_save_apply_reason() throws SQLException {
            Date applyDate = new Date();
            String reason = "测试原因";
            doReturn(false).when(attendanceMakeupDAO).hasPendingApplication(anyInt(), any(), anyString());
            doReturn(MAKEUP_ID).when(attendanceMakeupDAO).apply(any(AttendanceMakeup.class));

            attendanceService.applyMakeup(applyDate, reason, MEMBER_USER_ID);

            ArgumentCaptor<AttendanceMakeup> captor = ArgumentCaptor.forClass(AttendanceMakeup.class);
            verify(attendanceMakeupDAO).apply(captor.capture());
            assertThat(captor.getValue().getApplyReason()).isEqualTo(reason);
        }

        @FastTest
        @DisplayName("申请时应设置申请状态为PENDING")
        void should_set_pending_status() throws SQLException {
            Date applyDate = new Date();
            doReturn(false).when(attendanceMakeupDAO).hasPendingApplication(anyInt(), any(), anyString());
            doReturn(MAKEUP_ID).when(attendanceMakeupDAO).apply(any(AttendanceMakeup.class));

            attendanceService.applyMakeup(applyDate, "测试原因", MEMBER_USER_ID);

            ArgumentCaptor<AttendanceMakeup> captor = ArgumentCaptor.forClass(AttendanceMakeup.class);
            verify(attendanceMakeupDAO).apply(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MAKEUP_STATUS_PENDING);
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性")
    class StatusEnumTests {

        @FastTest
        @DisplayName("签到状态枚举应完整")
        void check_in_status_enum_should_be_complete() {
            assertThat(CHECK_IN_STATUS_NONE).isEqualTo("NONE");
            assertThat(CHECK_IN_STATUS_NORMAL).isEqualTo("NORMAL");
            assertThat(CHECK_IN_STATUS_LATE).isEqualTo("LATE");
            assertThat(CHECK_IN_STATUS_LEAVE).isEqualTo("LEAVE");
        }

        @FastTest
        @DisplayName("签退状态枚举应完整")
        void check_out_status_enum_should_be_complete() {
            assertThat(CHECK_OUT_STATUS_NONE).isEqualTo("NONE");
            assertThat(CHECK_OUT_STATUS_NORMAL).isEqualTo("NORMAL");
            assertThat(CHECK_OUT_STATUS_EARLY).isEqualTo("EARLY");
            assertThat(CHECK_OUT_STATUS_LEAVE).isEqualTo("LEAVE");
            assertThat(CHECK_OUT_STATUS_MISSING).isEqualTo("MISSING");
        }

        @FastTest
        @DisplayName("补签类型枚举应完整")
        void makeup_type_enum_should_be_complete() {
            assertThat(MAKEUP_TYPE_CHECK_IN).isEqualTo("CHECK_IN");
            assertThat(MAKEUP_TYPE_CHECK_OUT).isEqualTo("CHECK_OUT");
        }

        @FastTest
        @DisplayName("补签状态枚举应完整")
        void makeup_status_enum_should_be_complete() {
            assertThat(MAKEUP_STATUS_PENDING).isEqualTo("PENDING");
            assertThat(MAKEUP_STATUS_APPROVED).isEqualTo("APPROVED");
            assertThat(MAKEUP_STATUS_REJECTED).isEqualTo("REJECTED");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @FastTest
        @DisplayName("考勤列表最大分页应正常返回")
        void should_handle_max_page_size() throws SQLException {
            doReturn(Arrays.asList()).when(attendanceDAO).getAttendanceList(any(), any(), any(), anyInt(), anyInt());
            doReturn(0).when(attendanceDAO).getTotalCount(any(), any(), any());

            Map<String, Object> filter = createAttendanceFilter();
            Result result = attendanceService.listAttendance(filter, Integer.MAX_VALUE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("考勤DAO异常时应返回错误")
        void should_return_error_when_dao_throws_exception() throws SQLException {
            doThrow(new RuntimeException("数据库错误")).when(attendanceDAO).getTodayAttendance(anyInt());

            Result result = attendanceService.checkIn(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("补签DAO异常时应返回错误")
        void should_return_error_when_makeup_dao_throws_exception() throws SQLException {
            doThrow(new RuntimeException("数据库错误")).when(attendanceMakeupDAO).hasPendingApplication(any(), any(), anyString());

            Result result = attendanceService.applyMakeup(new Date(), "测试原因", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("用户DAO异常时应返回错误")
        void should_return_error_when_user_dao_throws_exception() {
            doThrow(new RuntimeException("数据库错误")).when(userDAO).findById(anyInt());

            Result result = attendanceService.checkIn(OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("未来日期申请补签应返回错误")
        void should_return_error_when_future_date() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 5);
            Date futureDate = cal.getTime();

            Result result = attendanceService.applyMakeup(futureDate, "测试原因", MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("工作时长超过正常值应正常处理")
        void should_handle_long_work_duration() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, false);
            today.setWorkDuration(720);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());
            doReturn(1).when(attendanceDAO).checkOut(any(Attendance.class));

            Result result = attendanceService.checkOut(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("迟到签到后签退早退应正常处理")
        void should_handle_late_checkin_early_checkout() throws SQLException {
            Attendance today = createAttendanceToday(ATTENDANCE_ID, MEMBER_USER_ID, true, false);
            today.setCheckInStatus(CHECK_IN_STATUS_LATE);
            doReturn(today).when(attendanceDAO).getTodayAttendance(anyInt());
            doReturn(1).when(attendanceDAO).checkOut(any(Attendance.class));

            Result result = attendanceService.checkOut(MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }
}
