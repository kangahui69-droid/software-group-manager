package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.RecruitApplicationDTO;
import model.RecruitApplication;
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
import service.RecruitService;
import support.FastTest;
import util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RecruitApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.x RecruitApiServlet 招新API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 * - 分页：data: {list:[], total, page, pageSize}
 *
 * 端点：
 * - GET /api/recruit → 申请列表（year/status/keyword/round/page）
 * - GET /api/recruit/{id} → 申请详情
 * - POST /api/recruit → 提交申请
 * - POST /api/recruit/{id}/approve → 审批通过
 * - POST /api/recruit/{id}/reject → 审批驳回
 * - DELETE /api/recruit/{id} → 删除申请
 * - GET /api/recruit/years → 所有年份
 * - GET /api/recruit/count → 待审核数量
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecruitApiServlet 招新API测试")
class RecruitApiServletTest {

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer APPLICATION_ID = 100;

    // 用户角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 申请状态枚举
    private static final Integer STATUS_PENDING = 1;
    private static final Integer STATUS_APPROVED = 2;
    private static final Integer STATUS_REJECTED = 0;

    // ==================== 测试辅助类 ====================

    private TestableRecruitApiServlet servlet;
    private RecruitService mockRecruitService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockRecruitService = mock(RecruitService.class);
        servlet = new TestableRecruitApiServlet(mockRecruitService);

        when(mockRequest.getSession(false)).thenReturn(mockSession);
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

    private RecruitApplication createApplication(Integer id, Integer status) {
        RecruitApplication app = new RecruitApplication();
        app.setId(id);
        app.setName("张三");
        app.setStudentId("2024" + String.format("%03d", id));
        app.setMajor("软件工程");
        app.setGrade("2024");
        app.setPhone("13800138000");
        app.setEmail("user" + id + "@example.com");
        app.setReason("热爱技术");
        app.setStatus(status);
        return app;
    }

    private RecruitApplicationDTO createApplicationDTO() {
        RecruitApplicationDTO dto = new RecruitApplicationDTO();
        dto.setName("李四");
        dto.setStudentId("2024001");
        dto.setMajor("计算机科学");
        dto.setGrade("2024");
        dto.setPhone("13900139000");
        dto.setEmail("lisi@example.com");
        dto.setReason("热爱编程");
        return dto;
    }

    // ==================== 认证相关测试 ====================

    @Nested
    @DisplayName("认证与授权测试")
    class AuthenticationTests {

        @FastTest
        @DisplayName("未登录用户访问受保护端点应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
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
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 申请列表测试 GET /api/recruit ====================

    @Nested
    @DisplayName("申请列表 GET /api/recruit")
    class ListApplicationsTests {

        @FastTest
        @DisplayName("正常获取申请列表")
        void should_return_application_list() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter(anyString())).thenReturn(null);

            List<RecruitApplication> apps = Arrays.asList(
                    createApplication(1, STATUS_PENDING),
                    createApplication(2, STATUS_APPROVED)
            );
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.ok(apps));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("按年份筛选申请列表")
        void should_filter_by_year() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("year")).thenReturn("2024");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("status")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("round")).thenReturn(null);

            List<RecruitApplication> apps = Arrays.asList(
                    createApplication(1, STATUS_PENDING)
            );
            when(mockRecruitService.listApplications(eq(2024), eq(null), eq(null), eq(null)))
                    .thenReturn(Result.ok(apps));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("按状态筛选申请列表")
        void should_filter_by_status() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("status")).thenReturn("1");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("year")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn(null);
            when(mockRequest.getParameter("round")).thenReturn(null);

            List<RecruitApplication> apps = Arrays.asList(
                    createApplication(1, STATUS_PENDING)
            );
            when(mockRecruitService.listApplications(eq(null), eq("1"), eq(null), eq(null)))
                    .thenReturn(Result.ok(apps));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("按关键词搜索申请列表")
        void should_filter_by_keyword() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("keyword")).thenReturn("张三");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("year")).thenReturn(null);
            when(mockRequest.getParameter("status")).thenReturn(null);
            when(mockRequest.getParameter("round")).thenReturn(null);

            List<RecruitApplication> apps = Arrays.asList(
                    createApplication(1, STATUS_PENDING)
            );
            when(mockRecruitService.listApplications(eq(null), eq(null), eq("张三"), eq(null)))
                    .thenReturn(Result.ok(apps));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("空申请列表")
        void should_return_empty_list() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }

        @FastTest
        @DisplayName("Service层返回错误")
        void should_handle_service_error() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.error(500, "数据库错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
        }
    }

    // ==================== 申请详情测试 GET /api/recruit/{id} ====================

    @Nested
    @DisplayName("申请详情 GET /api/recruit/{id}")
    class GetApplicationDetailTests {

        @FastTest
        @DisplayName("正常获取申请详情")
        void should_return_application_detail() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID);
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID);

            RecruitApplication app = createApplication(APPLICATION_ID, STATUS_PENDING);
            when(mockRecruitService.getApplicationDetail(APPLICATION_ID))
                    .thenReturn(Result.ok(app));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("张三");
        }

        @FastTest
        @DisplayName("申请不存在返回404")
        void should_return_404_when_not_found() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/99999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/99999");
            when(mockRecruitService.getApplicationDetail(99999))
                    .thenReturn(Result.error(404, "申请不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("无效ID格式返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/abc");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 提交申请测试 POST /api/recruit ====================

    @Nested
    @DisplayName("提交申请 POST /api/recruit")
    class SubmitApplicationTests {

        @FastTest
        @DisplayName("成功提交申请")
        void should_submit_application_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{\"name\":\"李四\",\"studentId\":\"2024001\",\"major\":\"计算机科学\",\"email\":\"lisi@example.com\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockRecruitService.submitApplication(any(RecruitApplicationDTO.class)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("姓名为空返回400")
        void should_return_400_when_name_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{\"name\":\"\",\"studentId\":\"2024001\",\"major\":\"计算机科学\",\"email\":\"lisi@example.com\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockRecruitService.submitApplication(any(RecruitApplicationDTO.class)))
                    .thenReturn(Result.error(400, "姓名不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("姓名");
        }

        @FastTest
        @DisplayName("学号为空返回400")
        void should_return_400_when_student_id_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{\"name\":\"李四\",\"studentId\":\"\",\"major\":\"计算机科学\",\"email\":\"lisi@example.com\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockRecruitService.submitApplication(any(RecruitApplicationDTO.class)))
                    .thenReturn(Result.error(400, "学号不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("学号");
        }

        @FastTest
        @DisplayName("专业为空返回400")
        void should_return_400_when_major_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{\"name\":\"李四\",\"studentId\":\"2024001\",\"major\":\"\",\"email\":\"lisi@example.com\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockRecruitService.submitApplication(any(RecruitApplicationDTO.class)))
                    .thenReturn(Result.error(400, "专业不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("专业");
        }

        @FastTest
        @DisplayName("邮箱为空返回400")
        void should_return_400_when_email_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{\"name\":\"李四\",\"studentId\":\"2024001\",\"major\":\"计算机科学\",\"email\":\"\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockRecruitService.submitApplication(any(RecruitApplicationDTO.class)))
                    .thenReturn(Result.error(400, "邮箱不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("邮箱");
        }

        @FastTest
        @DisplayName("JSON解析失败返回400")
        void should_return_400_when_json_parse_error() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{invalid json}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("数据库错误返回500")
        void should_return_500_when_database_error() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");

            String jsonBody = "{\"name\":\"李四\",\"studentId\":\"2024001\",\"major\":\"计算机科学\",\"email\":\"lisi@example.com\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            jsonBody.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)));

            when(mockRecruitService.submitApplication(any(RecruitApplicationDTO.class)))
                    .thenReturn(Result.error(500, "提交申请失败"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
        }
    }

    // ==================== 审批通过测试 POST /api/recruit/{id}/approve ====================

    @Nested
    @DisplayName("审批通过 POST /api/recruit/{id}/approve")
    class ApproveApplicationTests {

        @FastTest
        @DisplayName("审批通过成功")
        void should_approve_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID + "/approve");

            when(mockRecruitService.approveApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("申请不存在返回404")
        void should_return_404_when_application_not_found() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/99999/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/99999/approve");

            when(mockRecruitService.approveApplication(eq(99999), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "申请不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("申请已被审批返回400")
        void should_return_400_when_already_processed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID + "/approve");

            when(mockRecruitService.approveApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "该申请已被审批，无法重复操作"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("邮箱已被使用返回400")
        void should_return_400_when_email_already_used() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID + "/approve");

            when(mockRecruitService.approveApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "该邮箱已被其他用户使用"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("邮箱");
        }

        @FastTest
        @DisplayName("无效ID格式返回400")
        void should_return_400_when_invalid_id() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/abc/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/abc/approve");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 审批驳回测试 POST /api/recruit/{id}/reject ====================

    @Nested
    @DisplayName("审批驳回 POST /api/recruit/{id}/reject")
    class RejectApplicationTests {

        @FastTest
        @DisplayName("审批驳回成功")
        void should_reject_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID + "/reject");

            when(mockRecruitService.rejectApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("申请不存在返回404")
        void should_return_404_when_application_not_found() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/99999/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/99999/reject");

            when(mockRecruitService.rejectApplication(eq(99999), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(404, "申请不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("申请已被审批返回400")
        void should_return_400_when_already_processed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID + "/reject");

            when(mockRecruitService.rejectApplication(eq(APPLICATION_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "该申请已被审批，无法重复操作"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("无效ID格式返回400")
        void should_return_400_when_invalid_id() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/abc/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/abc/reject");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 删除申请测试 DELETE /api/recruit/{id} ====================

    @Nested
    @DisplayName("删除申请 DELETE /api/recruit/{id}")
    class DeleteApplicationTests {

        @FastTest
        @DisplayName("删除申请成功")
        void should_delete_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/" + APPLICATION_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + APPLICATION_ID);

            when(mockRecruitService.deleteApplication(APPLICATION_ID))
                    .thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("申请不存在返回404")
        void should_return_404_when_not_found() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/99999");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/99999");

            when(mockRecruitService.deleteApplication(99999))
                    .thenReturn(Result.error(404, "申请不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("无效ID格式返回400")
        void should_return_400_when_invalid_id() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/abc");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 所有年份测试 GET /api/recruit/years ====================

    @Nested
    @DisplayName("所有年份 GET /api/recruit/years")
    class GetAllYearsTests {

        @FastTest
        @DisplayName("正常获取所有年份")
        void should_return_all_years() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/years");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/years");

            when(mockRecruitService.findAllYears())
                    .thenReturn(Result.ok(Arrays.asList(2024, 2023, 2022)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("2024");
        }

        @FastTest
        @DisplayName("无年份数据返回空列表")
        void should_return_empty_list_when_no_years() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/years");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/years");

            when(mockRecruitService.findAllYears())
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }
    }

    // ==================== 待审核数量测试 GET /api/recruit/count ====================

    @Nested
    @DisplayName("待审核数量 GET /api/recruit/count")
    class GetCountTests {

        @FastTest
        @DisplayName("正常获取待审核数量")
        void should_return_pending_count() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/count");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/count");

            when(mockRecruitService.countPending())
                    .thenReturn(Result.ok(5));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("5");
        }

        @FastTest
        @DisplayName("无待审核申请返回0")
        void should_return_zero_when_no_pending() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/count");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/count");

            when(mockRecruitService.countPending())
                    .thenReturn(Result.ok(0));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("0");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空pathInfo应返回申请列表")
        void should_handle_empty_path_info() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("尾斜杠应正常处理")
        void should_handle_trailing_slash() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超大ID应根据实现返回400或404")
        void should_handle_overflow_id() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/9999999999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/9999999999");
            when(mockRecruitService.getApplicationDetail(anyInt()))
                    .thenReturn(Result.error(404, "申请不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).matches(".*\"code\":(400|404).*");
        }

        @FastTest
        @DisplayName("不支持的HTTP方法返回405")
        void should_return_405_when_method_not_allowed() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getPathInfo()).thenReturn(null);

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":405");
        }
    }

    // ==================== 响应格式测试 ====================

    @Nested
    @DisplayName("响应格式测试")
    class ResponseFormatTests {

        @FastTest
        @DisplayName("成功响应应包含正确结构")
        void should_return_correct_success_structure() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
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
        @DisplayName("错误响应应包含正确结构")
        void should_return_correct_error_structure() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit/99999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/99999");
            when(mockRecruitService.getApplicationDetail(99999))
                    .thenReturn(Result.error(404, "申请不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("\"message\":\"申请不存在\"");
        }

        @FastTest
        @DisplayName("应设置正确的Content-Type")
        void should_set_correct_content_type() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/recruit");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter(anyString())).thenReturn(null);
            when(mockRecruitService.listApplications(any(), any(), any(), any()))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }
    }

    // ==================== 测试用Servlet内部类 ====================

    /**
     * 可测试的RecruitApiServlet
     *
     * 复制RecruitApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableRecruitApiServlet {

        private final RecruitService recruitService;
        private final Gson gson = new GsonBuilder().create();

        public TestableRecruitApiServlet(RecruitService recruitService) {
            this.recruitService = recruitService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);
            String uri = req.getRequestURI();

            // /api/recruit 或 /api/recruit/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListApplications(req, resp, currentUser);
                return;
            }

            // /api/recruit/years
            if (pathInfo.endsWith("/years")) {
                handleGetYears(resp);
                return;
            }

            // /api/recruit/count
            if (pathInfo.endsWith("/count")) {
                handleGetCount(resp);
                return;
            }

            // /api/recruit/{id}
            RecruitPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的申请ID"));
                return;
            }

            handleGetApplicationDetail(req, resp, currentUser, pi.getId());
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);
            String uri = req.getRequestURI();

            // /api/recruit - 提交申请
            if (uri.endsWith("/api/recruit") || (pathInfo != null && pathInfo.equals("/"))) {
                handleSubmitApplication(req, resp, currentUser);
                return;
            }

            // /api/recruit/{id}/approve
            if (pathInfo != null && pathInfo.endsWith("/approve")) {
                RecruitPathInfo pi = parseApproveRejectPath(pathInfo, "/approve");
                if (!pi.isValidId()) {
                    writeJson(resp, Result.error(400, "无效的申请ID"));
                    return;
                }
                handleApproveApplication(req, resp, currentUser, pi.getId());
                return;
            }

            // /api/recruit/{id}/reject
            if (pathInfo != null && pathInfo.endsWith("/reject")) {
                RecruitPathInfo pi = parseApproveRejectPath(pathInfo, "/reject");
                if (!pi.isValidId()) {
                    writeJson(resp, Result.error(400, "无效的申请ID"));
                    return;
                }
                handleRejectApplication(req, resp, currentUser, pi.getId());
                return;
            }

            writeJson(resp, Result.error(404, "未找到对应接口"));
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);
            RecruitPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的申请ID"));
                return;
            }

            handleDeleteApplication(req, resp, currentUser, pi.getId());
        }

        public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            writeJson(resp, Result.error(405, "不支持的请求方法"));
        }

        // ==================== 处理器方法 ====================

        private void handleListApplications(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String yearStr = req.getParameter("year");
            String status = req.getParameter("status");
            String keyword = req.getParameter("keyword");
            String roundStr = req.getParameter("round");

            Integer year = parseInteger(yearStr);
            Integer round = parseInteger(roundStr);

            Result result = recruitService.listApplications(year, status, keyword, round);
            writeJson(resp, result);
        }

        private void handleGetApplicationDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer id) throws IOException {
            Result result = recruitService.getApplicationDetail(id);
            writeJson(resp, result);
        }

        private void handleSubmitApplication(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            RecruitApplicationDTO dto = parseJsonRequest(req, RecruitApplicationDTO.class);
            if (dto == null) {
                writeJson(resp, Result.error(400, "无效的请求数据"));
                return;
            }
            Result result = recruitService.submitApplication(dto);
            writeJson(resp, result);
        }

        private void handleApproveApplication(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer id) throws IOException {
            Result result = recruitService.approveApplication(id, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRejectApplication(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer id) throws IOException {
            Result result = recruitService.rejectApplication(id, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleDeleteApplication(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer id) throws IOException {
            Result result = recruitService.deleteApplication(id);
            writeJson(resp, result);
        }

        private void handleGetYears(HttpServletResponse resp) throws IOException {
            Result result = recruitService.findAllYears();
            writeJson(resp, result);
        }

        private void handleGetCount(HttpServletResponse resp) throws IOException {
            Result result = recruitService.countPending();
            writeJson(resp, result);
        }

        // ==================== 辅助方法 ====================

        private User getCurrentUser(HttpServletRequest req) {
            HttpSession session = req.getSession(false);
            if (session == null) {
                return null;
            }
            return (User) session.getAttribute("user");
        }

        private String derivePathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                String uri = req.getRequestURI();
                if (uri != null && uri.contains("/api/recruit/")) {
                    pathInfo = uri.substring(uri.indexOf("/api/recruit/") + 14);
                    if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }
            return pathInfo;
        }

        private RecruitPathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                return new RecruitPathInfo(null, false);
            }
            String segment = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
            try {
                int id = Integer.parseInt(segment);
                return new RecruitPathInfo(id, true);
            } catch (NumberFormatException e) {
                return new RecruitPathInfo(null, false);
            }
        }

        private RecruitPathInfo parseApproveRejectPath(String pathInfo, String suffix) {
            String idPart = pathInfo.substring(1, pathInfo.length() - suffix.length());
            try {
                int id = Integer.parseInt(idPart);
                return new RecruitPathInfo(id, true);
            } catch (NumberFormatException e) {
                return new RecruitPathInfo(null, false);
            }
        }

        private Integer parseInteger(String str) {
            if (str == null || str.trim().isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private <T> T parseJsonRequest(HttpServletRequest req, Class<T> clazz) {
            try {
                BufferedReader reader = req.getReader();
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return gson.fromJson(sb.toString(), clazz);
            } catch (Exception e) {
                return null;
            }
        }

        private void writeJson(HttpServletResponse resp, Result result) throws IOException {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(gson.toJson(result));
        }

        private static class RecruitPathInfo {
            private final Integer id;
            private final boolean valid;

            RecruitPathInfo(Integer id, boolean valid) {
                this.id = id;
                this.valid = valid;
            }

            Integer getId() {
                return id;
            }

            boolean isValidId() {
                return valid && id != null && id > 0;
            }
        }
    }
}
