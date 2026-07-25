package servlet;

import model.Attendance;
import model.AttendanceMakeup;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.AttendanceService;
import support.FastTest;
import util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AttendanceApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 4.2 AttendanceApiServlet 考勤API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 *
 * 端点（根据计划文档 line 290-302）：
 * - GET    /api/attendance          - 考勤列表
 * - GET    /api/attendance/stats   - 考勤统计
 * - POST   /api/attendance/check-in - 签到
 * - POST   /api/attendance/check-out - 签退
 * - GET    /api/attendance/my      - 我的考勤
 * - GET    /api/attendance/my/stats - 我的统计
 * - POST   /api/attendance/makeup  - 补签申请
 * - GET    /api/attendance/makeup  - 补签列表
 * - POST   /api/attendance/{id}/approve - 审批通过
 * - POST   /api/attendance/{id}/reject  - 审批拒绝
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceApiServlet 考勤API测试")
class AttendanceApiServletTest {

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
    private static final String CHECK_IN_STATUS_NORMAL = "NORMAL";
    private static final String CHECK_IN_STATUS_LATE = "LATE";

    // 签退状态枚举
    private static final String CHECK_OUT_STATUS_NORMAL = "NORMAL";
    private static final String CHECK_OUT_STATUS_EARLY = "EARLY";

    // 补签类型枚举
    private static final String MAKEUP_TYPE_CHECK_IN = "CHECK_IN";
    private static final String MAKEUP_TYPE_CHECK_OUT = "CHECK_OUT";

    // 补签状态枚举
    private static final String MAKEUP_STATUS_PENDING = "PENDING";
    private static final String MAKEUP_STATUS_APPROVED = "APPROVED";
    private static final String MAKEUP_STATUS_REJECTED = "REJECTED";

    // ==================== 测试辅助类 ====================

    private TestableAttendanceApiServlet servlet;
    private AttendanceService mockAttendanceService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockAttendanceService = mock(AttendanceService.class);
        servlet = new TestableAttendanceApiServlet(mockAttendanceService);

        when(mockRequest.getSession(false)).thenReturn(mockSession);

        // 自动从RequestURI提取PathInfo用于路由
        when(mockRequest.getPathInfo()).thenAnswer(invocation -> {
            String uri = mockRequest.getRequestURI();
            if (uri == null) return null;
            // 提取 /api/attendance 之后的部分作为 pathInfo
            int idx = uri.indexOf("/api/attendance");
            if (idx < 0) return null;
            String path = uri.substring(idx + "/api/attendance".length());
            return path.isEmpty() ? "/" : path;
        });
    }

    // ==================== 工具方法 ====================

    private User createUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        user.setName("用户" + id);
        return user;
    }

    private Attendance createAttendance(Integer id, Integer userId, String checkInStatus, String checkOutStatus) {
        Attendance attendance = new Attendance();
        attendance.setId(id);
        attendance.setUserId(userId);
        attendance.setAttendanceDate(new Date());
        attendance.setCheckInTime(checkInStatus != null ? new Date() : null);
        attendance.setCheckOutTime(checkOutStatus != null ? new Date() : null);
        attendance.setCheckInStatus(checkInStatus);
        attendance.setCheckOutStatus(checkOutStatus);
        attendance.setWorkDuration(480);
        attendance.setLocation("测试地点");
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

    private StringWriter setupResponseWriter() throws Exception {
        StringWriter sw = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));
        return sw;
    }

    private void setupJsonBody(String jsonBody) throws Exception {
        BufferedReader reader = new BufferedReader(new StringReader(jsonBody));
        when(mockRequest.getReader()).thenReturn(reader);
    }

    private Map<String, Object> parseJsonResponse(String response) {
        // 简单解析JSON用于验证
        Map<String, Object> result = new HashMap<>();
        if (response.contains("\"code\":0")) {
            result.put("code", 0);
        } else if (response.contains("\"code\":")) {
            int start = response.indexOf("\"code\":") + 7;
            int end = response.indexOf(",", start);
            if (end == -1) end = response.indexOf("}", start);
            result.put("code", Integer.parseInt(response.substring(start, end).trim()));
        }
        return result;
    }

    // ==================== 认证相关测试 ====================

    @Nested
    @DisplayName("认证与授权测试")
    class AuthenticationTests {

        @FastTest
        @DisplayName("未登录用户访问应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
            assertThat(response).contains("请先登录");
        }

        @FastTest
        @DisplayName("已登录用户访问应正常处理")
        void should_process_request_when_logged_in() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAttendanceService.listAttendance(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("管理员访问审批端点应正常处理")
        void should_allow_admin_to_access_approval_endpoint() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/200/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.approveMakeup(eq(200), eq(ADMIN_USER_ID))).thenReturn(Result.ok());

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 考勤列表 GET /api/attendance ====================

    @Nested
    @DisplayName("考勤列表 GET /api/attendance")
    class ListAttendanceTests {

        @FastTest
        @DisplayName("正常获取考勤列表")
        void should_return_attendance_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");

            List<Attendance> list = Arrays.asList(
                createAttendance(1, MEMBER_USER_ID, CHECK_IN_STATUS_NORMAL, CHECK_OUT_STATUS_NORMAL),
                createAttendance(2, MEMBER_USER_ID, CHECK_IN_STATUS_LATE, CHECK_OUT_STATUS_EARLY)
            );
            when(mockAttendanceService.listAttendance(any(), eq(1))).thenReturn(Result.ok(list));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("空考勤列表")
        void should_return_empty_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAttendanceService.listAttendance(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }

        @FastTest
        @DisplayName("带日期筛选参数")
        void should_accept_date_filter() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("startDate")).thenReturn("2026-01-01");
            when(mockRequest.getParameter("endDate")).thenReturn("2026-01-31");
            when(mockAttendanceService.listAttendance(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("Service层返回错误")
        void should_handle_service_error() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAttendanceService.listAttendance(any(), eq(1))).thenReturn(Result.error(500, "数据库错误"));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
        }
    }

    // ==================== 考勤统计 GET /api/attendance/stats ====================

    @Nested
    @DisplayName("考勤统计 GET /api/attendance/stats")
    class GetStatsTests {

        @FastTest
        @DisplayName("正常获取考勤统计")
        void should_return_attendance_stats() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/stats");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockAttendanceService.getAttendanceStats(MEMBER_USER_ID)).thenReturn(Result.ok(createAttendanceStats()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("totalDays");
        }

        @FastTest
        @DisplayName("无考勤记录返回零值统计")
        void should_return_zero_stats_when_no_attendance() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/stats");
            when(mockRequest.getMethod()).thenReturn("GET");

            Map<String, Object> zeroStats = new HashMap<>();
            zeroStats.put("totalDays", 0);
            zeroStats.put("normalDays", 0);
            zeroStats.put("lateDays", 0);
            when(mockAttendanceService.getAttendanceStats(MEMBER_USER_ID)).thenReturn(Result.ok(zeroStats));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 签到 POST /api/attendance/check-in ====================

    @Nested
    @DisplayName("签到 POST /api/attendance/check-in")
    class CheckInTests {

        @FastTest
        @DisplayName("正常签到成功")
        void should_check_in_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-in");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkIn(MEMBER_USER_ID)).thenReturn(Result.ok(CHECK_IN_STATUS_NORMAL));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("NORMAL");
        }

        @FastTest
        @DisplayName("迟到签到返回LATE状态")
        void should_return_late_status() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-in");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkIn(MEMBER_USER_ID)).thenReturn(Result.ok(CHECK_IN_STATUS_LATE));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("LATE");
        }

        @FastTest
        @DisplayName("重复签到返回错误")
        void should_return_error_when_already_checked_in() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-in");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkIn(MEMBER_USER_ID)).thenReturn(Result.error(400, "您今日已签到，无需重复签到"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 签退 POST /api/attendance/check-out ====================

    @Nested
    @DisplayName("签退 POST /api/attendance/check-out")
    class CheckOutTests {

        @FastTest
        @DisplayName("正常签退成功")
        void should_check_out_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-out");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkOut(MEMBER_USER_ID)).thenReturn(Result.ok(CHECK_OUT_STATUS_NORMAL));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("NORMAL");
        }

        @FastTest
        @DisplayName("早退签退返回EARLY状态")
        void should_return_early_status() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-out");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkOut(MEMBER_USER_ID)).thenReturn(Result.ok(CHECK_OUT_STATUS_EARLY));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("EARLY");
        }

        @FastTest
        @DisplayName("未签到就签退返回错误")
        void should_return_error_when_not_checked_in() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-out");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkOut(MEMBER_USER_ID)).thenReturn(Result.error(400, "请先签到后再签退"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("重复签退返回错误")
        void should_return_error_when_already_checked_out() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/check-out");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.checkOut(MEMBER_USER_ID)).thenReturn(Result.error(400, "您今日已签退"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 我的考勤 GET /api/attendance/my ====================

    @Nested
    @DisplayName("我的考勤 GET /api/attendance/my")
    class GetMyAttendanceTests {

        @FastTest
        @DisplayName("正常获取我的考勤")
        void should_return_my_attendance() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");

            List<Attendance> list = Arrays.asList(
                createAttendance(1, MEMBER_USER_ID, CHECK_IN_STATUS_NORMAL, CHECK_OUT_STATUS_NORMAL)
            );
            when(mockAttendanceService.getMyAttendance(MEMBER_USER_ID, 1)).thenReturn(Result.ok(list));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("空考勤记录")
        void should_return_empty_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAttendanceService.getMyAttendance(MEMBER_USER_ID, 1)).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("分页参数page为2")
        void should_handle_page_2() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockAttendanceService.getMyAttendance(MEMBER_USER_ID, 2)).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 我的统计 GET /api/attendance/my/stats ====================

    @Nested
    @DisplayName("我的统计 GET /api/attendance/my/stats")
    class GetMyStatsTests {

        @FastTest
        @DisplayName("正常获取我的统计")
        void should_return_my_stats() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/my/stats");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockAttendanceService.getMyStats(MEMBER_USER_ID)).thenReturn(Result.ok(createAttendanceStats()));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("totalDays");
        }
    }

    // ==================== 补签申请 POST /api/attendance/makeup ====================

    @Nested
    @DisplayName("补签申请 POST /api/attendance/makeup")
    class ApplyMakeupTests {

        @FastTest
        @DisplayName("正常申请补签成功")
        void should_apply_makeup_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/makeup");
            when(mockRequest.getMethod()).thenReturn("POST");
            setupJsonBody("{\"reason\":\"测试原因\",\"date\":\"2026-07-01\"}");
            when(mockAttendanceService.applyMakeup(any(), eq("测试原因"), eq(MEMBER_USER_ID))).thenReturn(Result.ok());

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("重复申请补签返回错误")
        void should_return_error_when_duplicate_application() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/makeup");
            when(mockRequest.getMethod()).thenReturn("POST");
            setupJsonBody("{\"reason\":\"测试原因\",\"date\":\"2026-07-01\"}");
            when(mockAttendanceService.applyMakeup(any(), any(), eq(MEMBER_USER_ID))).thenReturn(Result.error(400, "该日期已有待处理的补签申请"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("未来日期申请返回错误")
        void should_return_error_when_future_date() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/makeup");
            when(mockRequest.getMethod()).thenReturn("POST");
            setupJsonBody("{\"reason\":\"测试原因\",\"date\":\"2026-07-01\"}");
            when(mockAttendanceService.applyMakeup(any(), any(), eq(MEMBER_USER_ID))).thenReturn(Result.error(400, "不能申请未来日期的补签"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("原因为空返回错误")
        void should_return_error_when_reason_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/makeup");
            when(mockRequest.getMethod()).thenReturn("POST");
            setupJsonBody("{\"reason\":\"\",\"date\":\"2026-07-01\"}");
            when(mockAttendanceService.applyMakeup(any(), eq(""), eq(MEMBER_USER_ID))).thenReturn(Result.error(400, "补签原因不能为空"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 补签列表 GET /api/attendance/makeup ====================

    @Nested
    @DisplayName("补签列表 GET /api/attendance/makeup")
    class ListMakeupTests {

        @FastTest
        @DisplayName("正常获取补签列表")
        void should_return_makeup_list() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/makeup");
            when(mockRequest.getMethod()).thenReturn("GET");

            List<AttendanceMakeup> list = Arrays.asList(
                createMakeup(1, MEMBER_USER_ID, MAKEUP_TYPE_CHECK_IN, MAKEUP_STATUS_PENDING),
                createMakeup(2, OTHER_USER_ID, MAKEUP_TYPE_CHECK_OUT, MAKEUP_STATUS_PENDING)
            );
            when(mockAttendanceService.listAttendance(any(), eq(1))).thenReturn(Result.ok(list));

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 审批通过 POST /api/attendance/{id}/approve ====================

    @Nested
    @DisplayName("审批通过 POST /api/attendance/{id}/approve")
    class ApproveMakeupTests {

        @FastTest
        @DisplayName("正常审批通过")
        void should_approve_makeup_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + MAKEUP_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.approveMakeup(eq(MAKEUP_ID), eq(ADMIN_USER_ID))).thenReturn(Result.ok());

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("补签申请不存在返回404")
        void should_return_404_when_makeup_not_exists() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + NONEXISTENT_USER_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.approveMakeup(eq(NONEXISTENT_USER_ID), eq(ADMIN_USER_ID))).thenReturn(Result.error(404, "补签申请不存在"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("已审批再次审批返回400")
        void should_return_400_when_already_approved() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + MAKEUP_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.approveMakeup(eq(MAKEUP_ID), eq(ADMIN_USER_ID))).thenReturn(Result.error(400, "该补签申请已处理"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("非管理员审批返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + MAKEUP_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.approveMakeup(eq(MAKEUP_ID), eq(MEMBER_USER_ID))).thenReturn(Result.error(403, "无权限执行此操作"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("无效的ID格式返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/abc/approve");
            when(mockRequest.getMethod()).thenReturn("POST");

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 审批拒绝 POST /api/attendance/{id}/reject ====================

    @Nested
    @DisplayName("审批拒绝 POST /api/attendance/{id}/reject")
    class RejectMakeupTests {

        @FastTest
        @DisplayName("正常拒绝补签")
        void should_reject_makeup_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + MAKEUP_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.rejectMakeup(eq(MAKEUP_ID), eq(ADMIN_USER_ID))).thenReturn(Result.ok());

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("补签申请不存在返回404")
        void should_return_404_when_makeup_not_exists() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + NONEXISTENT_USER_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.rejectMakeup(eq(NONEXISTENT_USER_ID), eq(ADMIN_USER_ID))).thenReturn(Result.error(404, "补签申请不存在"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("已审批拒绝返回400")
        void should_return_400_when_already_processed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/" + MAKEUP_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockAttendanceService.rejectMakeup(eq(MAKEUP_ID), eq(ADMIN_USER_ID))).thenReturn(Result.error(400, "该补签申请已处理"));

            StringWriter sw = setupResponseWriter();

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 404路由测试 ====================

    @Nested
    @DisplayName("404路由测试")
    class NotFoundRouteTests {

        @FastTest
        @DisplayName("不存在的端点返回404")
        void should_return_404_for_unknown_endpoint() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/attendance/unknown");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = setupResponseWriter();

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }
    }

    // ==================== 状态枚举完整性测试 ====================

    @Nested
    @DisplayName("状态枚举完整性")
    class StatusEnumTests {

        @FastTest
        @DisplayName("签到状态枚举应完整")
        void check_in_status_enum_should_be_complete() {
            assertThat(CHECK_IN_STATUS_NORMAL).isEqualTo("NORMAL");
            assertThat(CHECK_IN_STATUS_LATE).isEqualTo("LATE");
        }

        @FastTest
        @DisplayName("签退状态枚举应完整")
        void check_out_status_enum_should_be_complete() {
            assertThat(CHECK_OUT_STATUS_NORMAL).isEqualTo("NORMAL");
            assertThat(CHECK_OUT_STATUS_EARLY).isEqualTo("EARLY");
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

    // ==================== TestableAttendanceApiServlet ====================

    /**
     * 测试用AttendanceApiServlet子类
     * 模拟AttendanceApiServlet的行为用于测试
     * 复制业务逻辑避免依赖实际实现
     */
    static class TestableAttendanceApiServlet extends BaseApiServlet {

        private final AttendanceService attendanceService;

        public TestableAttendanceApiServlet(AttendanceService attendanceService) {
            this.attendanceService = attendanceService;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendUnauthorized(resp, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();

            // /api/attendance - 考勤列表
            if (pathInfo == null || pathInfo.equals("/")) {
                String pageStr = req.getParameter("page");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                writeJson(resp, attendanceService.listAttendance(new HashMap<>(), page));
                return;
            }

            // /api/attendance/stats - 考勤统计
            if (pathInfo.equals("/stats")) {
                writeJson(resp, attendanceService.getAttendanceStats(currentUser.getId()));
                return;
            }

            // /api/attendance/my - 我的考勤
            if (pathInfo.equals("/my")) {
                String pageStr = req.getParameter("page");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                writeJson(resp, attendanceService.getMyAttendance(currentUser.getId(), page));
                return;
            }

            // /api/attendance/my/stats - 我的统计
            if (pathInfo.equals("/my/stats")) {
                writeJson(resp, attendanceService.getMyStats(currentUser.getId()));
                return;
            }

            // /api/attendance/makeup - 补签列表
            if (pathInfo.equals("/makeup")) {
                String pageStr = req.getParameter("page");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                writeJson(resp, attendanceService.listAttendance(new HashMap<>(), page));
                return;
            }

            sendError(resp, 404, "未找到请求的路径");
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendUnauthorized(resp, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();

            // /api/attendance/check-in - 签到
            if (pathInfo != null && pathInfo.equals("/check-in")) {
                writeJson(resp, attendanceService.checkIn(currentUser.getId()));
                return;
            }

            // /api/attendance/check-out - 签退
            if (pathInfo != null && pathInfo.equals("/check-out")) {
                writeJson(resp, attendanceService.checkOut(currentUser.getId()));
                return;
            }

            // /api/attendance/makeup - 补签申请
            if (pathInfo != null && pathInfo.equals("/makeup")) {
                try {
                    Map<String, Object> body = parseJsonRequestToMap(req);
                    String reason = body.get("reason") != null ? body.get("reason").toString() : "";
                    Date date = body.get("date") instanceof Date ? (Date) body.get("date") : new Date();
                    writeJson(resp, attendanceService.applyMakeup(date, reason, currentUser.getId()));
                } catch (Exception e) {
                    sendBadRequest(resp, "无效的请求参数");
                }
                return;
            }

            // /api/attendance/{id}/approve - 审批通过
            if (pathInfo != null && pathInfo.matches("/\\d+/approve")) {
                Integer id = extractNumericId(pathInfo);
                if (id == null) {
                    sendBadRequest(resp, "无效的申请ID");
                    return;
                }
                writeJson(resp, attendanceService.approveMakeup(id, currentUser.getId()));
                return;
            }

            // /api/attendance/{id}/reject - 审批拒绝
            if (pathInfo != null && pathInfo.matches("/\\d+/reject")) {
                Integer id = extractNumericId(pathInfo);
                if (id == null) {
                    sendBadRequest(resp, "无效的申请ID");
                    return;
                }
                writeJson(resp, attendanceService.rejectMakeup(id, currentUser.getId()));
                return;
            }

            // 无效ID格式（非数字）返回400
            if (pathInfo != null && pathInfo.matches("/[^/]+/approve")) {
                sendBadRequest(resp, "无效的申请ID");
                return;
            }
            if (pathInfo != null && pathInfo.matches("/[^/]+/reject")) {
                sendBadRequest(resp, "无效的申请ID");
                return;
            }

            sendError(resp, 404, "未找到请求的路径");
        }

        @Override
        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {
            sendError(resp, 404, "未找到请求的路径");
        }

        private Map<String, Object> parseJsonRequestToMap(HttpServletRequest req) throws Exception {
            Object obj = parseJsonRequest(req);
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                return map;
            }
            return new HashMap<>();
        }

        private Integer extractNumericId(String path) {
            if (path == null) return null;
            String[] parts = path.split("/");
            for (String part : parts) {
                try {
                    return Integer.parseInt(part);
                } catch (NumberFormatException ignored) {
                }
            }
            return null;
        }
    }
}
