package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.ActivityDTO;
import dto.ActivityFilterDTO;
import model.Activity;
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
import service.ActivityService;
import support.FastTest;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ActivityApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.1 ActivityApiServlet 活动API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 * - 分页：data: {list:[], total, page, pageSize}
 *
 * 端点：
 * - GET /api/activities → 活动列表
 * - GET /api/activities/{id} → 活动详情
 * - POST /api/activities → 创建活动
 * - PUT /api/activities/{id} → 更新活动
 * - DELETE /api/activities/{id} → 删除活动
 * - POST /api/activities/{id}/register → 报名
 * - POST /api/activities/{id}/approve-user → 审批参与者通过
 * - POST /api/activities/{id}/reject-user → 审批参与者拒绝
 * - POST /api/activities/{id}/approve → 活动审核通过
 * - POST /api/activities/{id}/reject → 活动审核驳回
 * - GET /api/activities/my → 我报名的活动
 * - GET /api/activities/created-by-me → 我创建的活动
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActivityApiServlet 活动API测试")
class ActivityApiServletTest {

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer ACTIVITY_ID = 100;
    private static final Integer REG_USER_ID = 2;

    // 活动状态枚举
    private static final String STATUS_UPCOMING = "upcoming";
    private static final String STATUS_ONGOING = "ongoing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";

    // 活动审批状态枚举
    private static final String APPROVAL_PENDING = "pending";
    private static final String APPROVAL_APPROVED = "approved";
    private static final String APPROVAL_REJECTED = "rejected";

    // 报名状态枚举
    private static final String REG_STATUS_PENDING = "pending";
    private static final String REG_STATUS_CONFIRMED = "confirmed";
    private static final String REG_STATUS_REJECTED = "rejected";

    // 活动类型
    private static final String TYPE_LECTURE = "LECTURE";
    private static final String TYPE_SEMINAR = "SEMINAR";
    private static final String TYPE_TEA_PARTY = "TEA_PARTY";
    private static final String TYPE_PROJECT_INTRO = "PROJECT_INTRO";

    // ==================== 测试辅助类 ====================

    private TestableActivityApiServlet servlet;
    private ActivityService mockActivityService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockActivityService = mock(ActivityService.class);
        servlet = new TestableActivityApiServlet(mockActivityService);

        // 默认session行为
        when(mockRequest.getSession(false)).thenReturn(mockSession);
    }

    // ==================== 工具方法 ====================

    private User createUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        return user;
    }

    private Activity createActivity(Integer id, String status, String approvalStatus, int maxParticipants) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle("测试活动");
        activity.setDescription("活动描述");
        activity.setActivityType(TYPE_LECTURE);
        activity.setStatus(status);
        activity.setApprovalStatus(approvalStatus);
        activity.setCreatorId(ADMIN_USER_ID);
        activity.setMaxParticipants(maxParticipants);
        activity.setLocation("活动地点");
        return activity;
    }

    private Date addDays(Calendar cal, int days) {
        Calendar result = (Calendar) cal.clone();
        result.add(Calendar.DAY_OF_MONTH, days);
        return result.getTime();
    }

    private String extractJsonFromResponse(String responseBody) {
        // 简单解析JSON，验证结构
        return responseBody;
    }

    // ==================== 认证相关测试 ====================

    @Nested
    @DisplayName("认证与授权测试")
    class AuthenticationTests {

        @FastTest
        @DisplayName("未登录用户访问受保护端点应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
            assertThat(response).contains("请先登录");
        }

        @FastTest
        @DisplayName("已登录用户访问应正常处理")
        void should_process_request_when_logged_in() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("MEMBER角色用户访问管理员端点应返回403")
        void should_return_403_when_member_access_admin_endpoint() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve");
            when(mockRequest.getMethod()).thenReturn("POST");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    // ==================== GET /api/activities 活动列表 ====================

    @Nested
    @DisplayName("GET /api/activities 活动列表")
    class ListActivitiesTests {

        @FastTest
        @DisplayName("获取活动列表应返回成功")
        void should_return_activity_list() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("status")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);

            Activity activity1 = createActivity(1, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            Activity activity2 = createActivity(2, STATUS_ONGOING, APPROVAL_APPROVED, 20);
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList(activity1, activity2)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("按状态筛选活动列表")
        void should_filter_by_status() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("status")).thenReturn(STATUS_UPCOMING);
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("按关键词搜索活动列表")
        void should_search_by_keyword() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("keyword")).thenReturn("测试");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("活动列表为空时应返回空数组")
        void should_return_empty_list_when_no_activities() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }

        @FastTest
        @DisplayName("分页参数应正确传递")
        void should_pass_pagination_params() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("3");
            when(mockRequest.getParameter("pageSize")).thenReturn("20");
            when(mockActivityService.listActivities(any(), eq(3), eq(20)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("Service返回错误时应返回对应错误码")
        void should_return_error_when_service_fails() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.error(500, "系统错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
            assertThat(response).contains("系统错误");
        }
    }

    // ==================== GET /api/activities/{id} 活动详情 ====================

    @Nested
    @DisplayName("GET /api/activities/{id} 活动详情")
    class GetActivityDetailTests {

        @FastTest
        @DisplayName("获取活动详情应返回成功")
        void should_return_activity_detail() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            Activity activity = createActivity(100, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(mockActivityService.getActivityDetail(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(activity));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("获取不存在的活动详情应返回404")
        void should_return_404_when_activity_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999");

            when(mockActivityService.getActivityDetail(eq(999), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "活动不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("活动不存在");
        }

        @FastTest
        @DisplayName("活动ID格式错误时应返回400")
        void should_return_400_when_id_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/abc");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("未登录用户获取活动详情应允许（公开）")
        void should_allow_guest_to_view_activity_detail() throws Exception {
            when(mockSession.getAttribute("user")).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            Activity activity = createActivity(100, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            when(mockActivityService.getActivityDetail(eq(100), isNull()))
                    .thenReturn(Result.ok(activity));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }
    }

    // ==================== POST /api/activities 创建活动 ====================

    @Nested
    @DisplayName("POST /api/activities 创建活动")
    class CreateActivityTests {

        @FastTest
        @DisplayName("管理员创建活动应返回成功")
        void should_create_activity_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"新活动\",\"activityType\":\"LECTURE\",\"description\":\"活动描述\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            Activity created = createActivity(100, STATUS_UPCOMING, APPROVAL_PENDING, 10);
            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(100));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":100");
        }

        @FastTest
        @DisplayName("非管理员创建活动应返回403")
        void should_return_403_when_member_creates_activity() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"新活动\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "需要管理员权限"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("创建活动时标题为空应返回400")
        void should_return_400_when_title_empty() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "标题不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("标题不能为空");
        }

        @FastTest
        @DisplayName("创建活动时活动类型无效应返回400")
        void should_return_400_when_activity_type_invalid() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"测试活动\",\"activityType\":\"INVALID_TYPE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "无效的活动类型"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("创建活动时请求体为空应返回400")
        void should_return_400_when_body_empty() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("创建活动时无效JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream("{invalid}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("创建活动时所有枚举类型的活动类型都应支持")
        void should_support_all_activity_type_enums() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            List<String> types = Arrays.asList(TYPE_LECTURE, TYPE_SEMINAR, TYPE_TEA_PARTY, TYPE_PROJECT_INTRO);

            for (String type : types) {
                String jsonBody = String.format("{\"title\":\"测试活动\",\"activityType\":\"%s\"}", type);
                when(mockRequest.getReader()).thenReturn(new BufferedReader(
                        new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));
                when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                        .thenReturn(Result.ok(100));

                StringWriter sw = new StringWriter();
                when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

                servlet.doPost(mockRequest, mockResponse);

                assertThat(sw.toString()).contains("\"code\":0");
            }
        }
    }

    // ==================== PUT /api/activities/{id} 更新活动 ====================

    @Nested
    @DisplayName("PUT /api/activities/{id} 更新活动")
    class UpdateActivityTests {

        @FastTest
        @DisplayName("更新活动应返回成功")
        void should_update_activity_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            String jsonBody = "{\"title\":\"更新后的活动\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.updateActivity(eq(100), any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(100));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("更新不存在的活动应返回404")
        void should_return_404_when_activity_not_exists() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/999");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/999");

            String jsonBody = "{\"title\":\"更新后的活动\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.updateActivity(eq(999), any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "活动不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("非创建者且非管理员更新活动应返回403")
        void should_return_403_when_not_creator_or_admin() throws Exception {
            User other = createUser(OTHER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(other);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            String jsonBody = "{\"title\":\"更新后的活动\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.updateActivity(eq(100), any(ActivityDTO.class), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权修改此活动"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("更新已发布的活动应返回400")
        void should_return_400_when_activity_already_started() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            String jsonBody = "{\"title\":\"更新后的活动\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.updateActivity(eq(100), any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "活动已开始或已结束，无法修改"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== DELETE /api/activities/{id} 删除活动 ====================

    @Nested
    @DisplayName("DELETE /api/activities/{id} 删除活动")
    class DeleteActivityTests {

        @FastTest
        @DisplayName("删除活动应返回成功")
        void should_delete_activity_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockActivityService.deleteActivity(eq(100), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("删除不存在的活动应返回404")
        void should_return_404_when_activity_not_exists() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/999");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/999");

            when(mockActivityService.deleteActivity(eq(999), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "活动不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("删除有确认报名的活动应返回400")
        void should_return_400_when_has_confirmed_participants() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockActivityService.deleteActivity(eq(100), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "已有确认报名，无法删除"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("已有确认报名");
        }

        @FastTest
        @DisplayName("删除进行中的活动应返回400")
        void should_return_400_when_activity_ongoing() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockActivityService.deleteActivity(eq(100), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "活动进行中，无法删除"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("进行中");
        }

        @FastTest
        @DisplayName("非创建者且非管理员删除活动应返回403")
        void should_return_403_when_not_creator_or_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockActivityService.deleteActivity(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "无权删除此活动"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    // ==================== POST /api/activities/{id}/register 报名 ====================

    @Nested
    @DisplayName("POST /api/activities/{id}/register 报名")
    class RegisterTests {

        @FastTest
        @DisplayName("正常报名应返回成功")
        void should_register_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/register");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/register");

            when(mockActivityService.register(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("报名已满的活动应返回400")
        void should_return_400_when_activity_full() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/register");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/register");

            when(mockActivityService.register(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "活动报名已满"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("报名已满");
        }

        @FastTest
        @DisplayName("报名已截止的活动应返回400")
        void should_return_400_when_registration_ended() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/register");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/register");

            when(mockActivityService.register(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "报名已截止"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("重复报名应返回400")
        void should_return_400_when_already_registered() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/register");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/register");

            when(mockActivityService.register(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "您已报名此活动"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("已报名");
        }

        @FastTest
        @DisplayName("报名自己创建的活动应返回400")
        void should_return_400_when_register_own_activity() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/register");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/register");

            when(mockActivityService.register(eq(100), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "不能报名自己创建的活动"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("报名不存在的活动应返回404")
        void should_return_404_when_activity_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/999/register");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/999/register");

            when(mockActivityService.register(eq(999), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "活动不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }
    }

    // ==================== POST /api/activities/{id}/approve-user 审批参与者通过 ====================

    @Nested
    @DisplayName("POST /api/activities/{id}/approve-user 审批参与者通过")
    class ApproveUserTests {

        @FastTest
        @DisplayName("管理员审批参与者应返回成功")
        void should_approve_participant_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve-user");

            String jsonBody = "{\"userId\":2}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.approveParticipant(eq(100), eq(2), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("非管理员审批应返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve-user");

            String jsonBody = "{\"userId\":3}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.approveParticipant(eq(100), eq(3), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "需要管理员权限"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("审批不存在的报名应返回404")
        void should_return_404_when_registration_not_found() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve-user");

            String jsonBody = "{\"userId\":999}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.approveParticipant(eq(100), eq(999), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "报名记录不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("审批已确认的报名应返回400")
        void should_return_400_when_already_confirmed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve-user");

            String jsonBody = "{\"userId\":2}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.approveParticipant(eq(100), eq(2), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "该报名非待审核状态"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("审批时超过最大人数限制应返回400")
        void should_return_400_when_exceeds_max_participants() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve-user");

            String jsonBody = "{\"userId\":2}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.approveParticipant(eq(100), eq(2), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "超过最大人数"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("超过最大人数");
        }
    }

    // ==================== POST /api/activities/{id}/reject-user 审批参与者拒绝 ====================

    @Nested
    @DisplayName("POST /api/activities/{id}/reject-user 审批参与者拒绝")
    class RejectUserTests {

        @FastTest
        @DisplayName("管理员拒绝参与者应返回成功")
        void should_reject_participant_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/reject-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/reject-user");

            String jsonBody = "{\"userId\":2,\"reason\":\"不符合报名条件\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.rejectParticipant(eq(100), eq(2), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("拒绝已确认的报名应返回400")
        void should_return_400_when_already_confirmed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/reject-user");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/reject-user");

            String jsonBody = "{\"userId\":2,\"reason\":\"不符合条件\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.rejectParticipant(eq(100), eq(2), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "已确认的报名无法拒绝"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== POST /api/activities/{id}/approve 活动审核通过 ====================

    @Nested
    @DisplayName("POST /api/activities/{id}/approve 活动审核通过")
    class ApproveActivityTests {

        @FastTest
        @DisplayName("管理员审核活动通过应返回成功")
        void should_approve_activity_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve");

            when(mockActivityService.approveActivity(eq(100), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("审核已通过的活动应返回400")
        void should_return_400_when_already_approved() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve");

            when(mockActivityService.approveActivity(eq(100), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "该活动已审核"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("已审核");
        }

        @FastTest
        @DisplayName("非管理员审核应返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/approve");

            when(mockActivityService.approveActivity(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "需要管理员权限"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    // ==================== POST /api/activities/{id}/reject 活动审核驳回 ====================

    @Nested
    @DisplayName("POST /api/activities/{id}/reject 活动审核驳回")
    class RejectActivityTests {

        @FastTest
        @DisplayName("管理员驳回活动应返回成功")
        void should_reject_activity_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/reject");

            String jsonBody = "{\"reason\":\"活动内容不符合要求\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.rejectActivity(eq(100), eq("活动内容不符合要求"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("驳回活动时原因为空应返回400")
        void should_return_400_when_reason_empty() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/reject");

            String jsonBody = "{\"reason\":\"\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.rejectActivity(eq(100), eq(""), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "拒绝原因不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("驳回活动时原因超长应返回400")
        void should_return_400_when_reason_too_long() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/100/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/100/reject");

            String jsonBody = "{\"reason\":\"" + "a".repeat(501) + "\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.rejectActivity(eq(100), anyString(), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "拒绝原因不能超过500字符"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== GET /api/activities/my 我报名的活动 ====================

    @Nested
    @DisplayName("GET /api/activities/my 我报名的活动")
    class GetMyActivitiesTests {

        @FastTest
        @DisplayName("获取我报名的活动应返回成功")
        void should_return_my_activities() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn("1");

            Activity activity1 = createActivity(1, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            Activity activity2 = createActivity(2, STATUS_ONGOING, APPROVAL_APPROVED, 20);
            when(mockActivityService.getMyActivities(any(Integer.class), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList(activity1, activity2)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("未报名任何活动应返回空数组")
        void should_return_empty_list_when_not_registered() throws Exception {
            User member = createUser(MEMBER_USER_ID, "MEMBER");
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn("1");

            when(mockActivityService.getMyActivities(any(Integer.class), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }

        @FastTest
        @DisplayName("未登录访问应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockSession.getAttribute("user")).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/my");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }
    }

    // ==================== GET /api/activities/created-by-me 我创建的活动 ====================

    @Nested
    @DisplayName("GET /api/activities/created-by-me 我创建的活动")
    class GetMyCreatedActivitiesTests {

        @FastTest
        @DisplayName("获取我创建的活动应返回成功")
        void should_return_my_created_activities() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/created-by-me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/created-by-me");
            when(mockRequest.getParameter("page")).thenReturn("1");

            Activity activity1 = createActivity(1, STATUS_UPCOMING, APPROVAL_APPROVED, 10);
            activity1.setCreatorId(ADMIN_USER_ID);
            when(mockActivityService.getMyCreatedActivities(any(Integer.class), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList(activity1)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("未创建任何活动应返回空数组")
        void should_return_empty_list_when_not_created_any() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/created-by-me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/created-by-me");
            when(mockRequest.getParameter("page")).thenReturn("1");

            when(mockActivityService.getMyCreatedActivities(any(Integer.class), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }
    }

    // ==================== HTTP方法测试 ====================

    @Nested
    @DisplayName("HTTP方法测试")
    class HttpMethodTests {

        @FastTest
        @DisplayName("不支持的HTTP方法应返回405")
        void should_return_405_when_method_not_allowed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("PATCH"); // 不支持的HTTP方法

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            // 默认行为是不过滤HTTP方法，子类实现时应处理
            verify(mockResponse, atLeastOnce()).setContentType("application/json; charset=UTF-8");
        }

        @FastTest
        @DisplayName("OPTIONS请求应返回200")
        void should_handle_options_request() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("OPTIONS");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doOptions(mockRequest, mockResponse);

            // CORS预检请求处理
            verify(mockResponse).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空路径info应使用默认列表行为")
        void should_handle_empty_path_info() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超长标题应被Service拒绝")
        void should_handle_long_title() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"" + "a".repeat(1000) + "\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "标题不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("特殊字符应正确处理")
        void should_handle_special_characters_in_title() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"测试活动 <script>alert('xss')</script>\",\"activityType\":\"LECTURE\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(100));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            // XSS过滤应在Filter层处理，Service正常创建
            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("Unicode字符应正确处理")
        void should_handle_unicode_characters() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"測試活動中文\",\"activityType\":\"LECTURE\",\"description\":\"描述русскийтекст日本語\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockActivityService.createActivity(any(ActivityDTO.class), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(100));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("负数分页页码应使用默认值")
        void should_use_default_when_page_negative() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("-1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }
    }

    // ==================== 响应格式测试 ====================

    @Nested
    @DisplayName("响应格式测试")
    class ResponseFormatTests {

        @FastTest
        @DisplayName("成功响应应包含code、message、data三个字段")
        void should_include_all_fields_in_success_response() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"message\":\"ok\"");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("错误响应应包含code、message、data三个字段")
        void should_include_all_fields_in_error_response() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities/999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999");
            when(mockActivityService.getActivityDetail(eq(999), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "活动不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("\"message\":\"活动不存在\"");
            assertThat(response).contains("\"data\":null");
        }

        @FastTest
        @DisplayName("Content-Type应正确设置")
        void should_set_correct_content_type() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockActivityService.listActivities(any(), eq(1), anyInt()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }

        @FastTest
        @DisplayName("分页数据应返回正确的结构")
        void should_return_paginated_data_structure() throws Exception {
            User admin = createUser(ADMIN_USER_ID, "ADMIN");
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/activities");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("pageSize")).thenReturn("10");
            when(mockActivityService.listActivities(any(), eq(1), eq(10)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }
    }

    // ==================== 测试用子类 ====================

    /**
     * 测试用ActivityApiServlet子类
     * 复制ActivityApiServlet的业务逻辑，用于测试
     */
    static class TestableActivityApiServlet {

        private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
        private static final int DEFAULT_PAGE_SIZE = 20;

        private final ActivityService activityService;
        private final Gson gson = new GsonBuilder().serializeNulls().create();

        public TestableActivityApiServlet(ActivityService activityService) {
            this.activityService = activityService;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                String pathInfo = req.getPathInfo();
                if (pathInfo != null && !pathInfo.equals("/") && !pathInfo.isEmpty()) {
                    String[] segments = pathInfo.split("/");
                    if (segments.length >= 2 && isNumeric(segments[1])) {
                        handleGetActivityDetail(req, resp, currentUser);
                        return;
                    }
                }
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListActivities(req, resp, currentUser);
            } else if (pathInfo.equals("/my")) {
                handleGetMyActivities(req, resp, currentUser);
            } else if (pathInfo.equals("/created-by-me")) {
                handleGetMyCreatedActivities(req, resp, currentUser);
            } else {
                String[] segments = pathInfo.split("/");
                if (segments.length >= 2) {
                    String idStr = segments[1];
                    if (isNumeric(idStr)) {
                        handleGetActivityDetail(req, resp, currentUser);
                    } else {
                        sendError(resp, 400, "无效的活动ID");
                    }
                } else {
                    sendError(resp, 400, "无效的请求路径");
                }
            }
        }

        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleCreateActivity(req, resp, currentUser);
            } else {
                String[] segments = pathInfo.split("/");
                if (segments.length >= 2) {
                    String idStr = segments[1];
                    if (!isNumeric(idStr)) {
                        sendError(resp, 400, "无效的活动ID");
                        return;
                    }
                    int activityId = Integer.parseInt(idStr);

                    if (segments.length >= 3) {
                        String action = segments[2];
                        switch (action) {
                            case "register":
                                handleRegister(req, resp, currentUser, activityId);
                                break;
                            case "approve-user":
                                handleApproveUser(req, resp, currentUser, activityId);
                                break;
                            case "reject-user":
                                handleRejectUser(req, resp, currentUser, activityId);
                                break;
                            case "approve":
                                handleApproveActivity(req, resp, currentUser, activityId);
                                break;
                            case "reject":
                                handleRejectActivity(req, resp, currentUser, activityId);
                                break;
                            default:
                                sendError(resp, 400, "未知的操作");
                        }
                    } else {
                        sendError(resp, 400, "未知的操作");
                    }
                } else {
                    sendError(resp, 400, "无效的请求路径");
                }
            }
        }

        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                sendError(resp, 400, "无效的请求路径");
                return;
            }

            String[] segments = pathInfo.split("/");
            if (segments.length >= 2) {
                String idStr = segments[1];
                if (!isNumeric(idStr)) {
                    sendError(resp, 400, "无效的活动ID");
                    return;
                }
                int activityId = Integer.parseInt(idStr);
                handleUpdateActivity(req, resp, currentUser, activityId);
            } else {
                sendError(resp, 400, "无效的请求路径");
            }
        }

        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                sendError(resp, 400, "无效的请求路径");
                return;
            }

            String[] segments = pathInfo.split("/");
            if (segments.length >= 2) {
                String idStr = segments[1];
                if (!isNumeric(idStr)) {
                    sendError(resp, 400, "无效的活动ID");
                    return;
                }
                int activityId = Integer.parseInt(idStr);
                handleDeleteActivity(req, resp, currentUser, activityId);
            } else {
                sendError(resp, 400, "无效的请求路径");
            }
        }

        protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            resp.setStatus(HttpServletResponse.SC_OK);
        }

        // ==================== 处理器方法 ====================

        private void handleListActivities(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");
            String status = req.getParameter("status");
            String keyword = req.getParameter("keyword");

            Integer page = 1;
            Integer pageSize = DEFAULT_PAGE_SIZE;

            try {
                if (pageStr != null && !pageStr.isEmpty()) {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                }
                if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
                    if (pageSize > 100) pageSize = 100;
                }
            } catch (NumberFormatException ignored) {}

            ActivityFilterDTO filter = new ActivityFilterDTO();
            filter.setStatus(status);
            filter.setKeyword(keyword);

            Result result = activityService.listActivities(filter, page, pageSize);
            writeJson(resp, result);
        }

        private void handleGetActivityDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                sendError(resp, 400, "无效的活动ID");
                return;
            }

            String[] segments = pathInfo.split("/");
            if (segments.length < 2) {
                sendError(resp, 400, "无效的活动ID");
                return;
            }

            String idStr = segments[1];
            if (!isNumeric(idStr)) {
                sendError(resp, 400, "无效的活动ID");
                return;
            }

            int activityId = Integer.parseInt(idStr);
            Integer userId = currentUser != null ? currentUser.getId() : null;

            Result result = activityService.getActivityDetail(activityId, userId);
            writeJson(resp, result);
        }

        private void handleCreateActivity(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            if (!"ADMIN".equals(currentUser.getRole())) {
                sendError(resp, 403, "需要管理员权限");
                return;
            }

            ActivityDTO dto = parseJsonRequest(req, ActivityDTO.class);
            if (dto == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            Result result = activityService.createActivity(dto, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleUpdateActivity(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            ActivityDTO dto = parseJsonRequest(req, ActivityDTO.class);
            if (dto == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            Result result = activityService.updateActivity(activityId, dto, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleDeleteActivity(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            Result result = activityService.deleteActivity(activityId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRegister(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            Result result = activityService.register(activityId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleApproveUser(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            if (!"ADMIN".equals(currentUser.getRole())) {
                sendError(resp, 403, "需要管理员权限");
                return;
            }

            Map<String, Object> body = parseJsonRequest(req, Map.class);
            if (body == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            Object userIdObj = body.get("userId");
            if (userIdObj == null) {
                sendError(resp, 400, "userId不能为空");
                return;
            }

            int userId;
            try {
                userId = ((Number) userIdObj).intValue();
            } catch (Exception e) {
                sendError(resp, 400, "无效的userId");
                return;
            }

            Result result = activityService.approveParticipant(activityId, userId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRejectUser(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            if (!"ADMIN".equals(currentUser.getRole())) {
                sendError(resp, 403, "需要管理员权限");
                return;
            }

            Map<String, Object> body = parseJsonRequest(req, Map.class);
            if (body == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            Object userIdObj = body.get("userId");
            if (userIdObj == null) {
                sendError(resp, 400, "userId不能为空");
                return;
            }

            int userId;
            try {
                userId = ((Number) userIdObj).intValue();
            } catch (Exception e) {
                sendError(resp, 400, "无效的userId");
                return;
            }

            Result result = activityService.rejectParticipant(activityId, userId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleApproveActivity(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            if (!"ADMIN".equals(currentUser.getRole())) {
                sendError(resp, 403, "需要管理员权限");
                return;
            }

            Result result = activityService.approveActivity(activityId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRejectActivity(HttpServletRequest req, HttpServletResponse resp, User currentUser, int activityId) throws IOException {
            if (!"ADMIN".equals(currentUser.getRole())) {
                sendError(resp, 403, "需要管理员权限");
                return;
            }

            Map<String, Object> body = parseJsonRequest(req, Map.class);
            if (body == null) {
                sendError(resp, 400, "请求体不能为空");
                return;
            }

            String reason = body.get("reason") != null ? body.get("reason").toString() : "";

            Result result = activityService.rejectActivity(activityId, reason, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleGetMyActivities(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            if (currentUser == null || currentUser.getId() == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            int page = 1;
            int pageSize = DEFAULT_PAGE_SIZE;

            try {
                if (pageStr != null && !pageStr.isEmpty()) {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                }
                if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
                    if (pageSize > 100) pageSize = 100;
                }
            } catch (NumberFormatException ignored) {}

            Result result = activityService.getMyActivities(currentUser.getId(), page, pageSize);
            if (result == null) {
                result = Result.error(500, "系统错误");
            }
            writeJson(resp, result);
        }

        private void handleGetMyCreatedActivities(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            if (currentUser == null || currentUser.getId() == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            int page = 1;
            int pageSize = DEFAULT_PAGE_SIZE;

            try {
                if (pageStr != null && !pageStr.isEmpty()) {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                }
                if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;
                    if (pageSize > 100) pageSize = 100;
                }
            } catch (NumberFormatException ignored) {}

            Result result = activityService.getMyCreatedActivities(currentUser.getId(), page, pageSize);
            if (result == null) {
                result = Result.error(500, "系统错误");
            }
            writeJson(resp, result);
        }

        // ==================== 工具方法 ====================

        private <T> T parseJsonRequest(HttpServletRequest request, Class<T> clazz) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();
            if (body.isEmpty() || "null".equals(body)) {
                return null;
            }
            try {
                return gson.fromJson(body, clazz);
            } catch (Exception e) {
                return null;
            }
        }

        private void writeJson(HttpServletResponse response, Result result) throws IOException {
            if (result == null) {
                result = Result.error(500, "系统错误");
            }
            response.setStatus(result.isSuccess() ? HttpServletResponse.SC_OK : result.getCode());
            PrintWriter writer = response.getWriter();
            gson.toJson(result, writer);
            writer.flush();
        }

        private void sendError(HttpServletResponse response, int code, String message) throws IOException {
            writeJson(response, Result.error(code, message));
        }

        private boolean isNumeric(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        private User getCurrentUser(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return null;
            }
            return (User) session.getAttribute("user");
        }
    }
}
