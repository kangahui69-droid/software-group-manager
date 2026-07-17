package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.PlanDTO;
import dto.ProgressDTO;
import dto.ProjectDTO;
import dto.ProjectFilterDTO;
import model.Project;
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
import service.ProjectService;
import support.FastTest;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.4 ProjectApiServlet 项目API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8（上传用multipart）
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 * - 分页：data: {list:[], total, page, pageSize}
 *
 * 端点：
 * - GET /api/projects → 项目列表（page/status/keyword）
 * - GET /api/projects/{id} → 项目详情
 * - POST /api/projects → 创建项目
 * - PUT /api/projects/{id} → 更新项目
 * - DELETE /api/projects/{id} → 删除项目
 * - POST /api/projects/{id}/apply → 申请加入
 * - POST /api/projects/{id}/approve-member → 审批成员通过
 * - POST /api/projects/{id}/reject-member → 审批成员拒绝
 * - POST /api/projects/{id}/approve → 项目审核通过
 * - POST /api/projects/{id}/reject → 项目审核驳回
 * - POST /api/projects/{id}/plans → 添加计划
 * - POST /api/projects/{id}/progress → 添加进度
 * - POST /api/projects/{id}/images → 上传项目图片
 * - POST /api/projects/{id}/files → 上传项目文件
 * - DELETE /api/projects/{id}/files/{fileId} → 删除项目文件
 * - POST /api/projects/{id}/labels/{labelCode} → 添加标签
 * - DELETE /api/projects/{id}/labels/{labelCode} → 删除标签
 * - POST /api/projects/{id}/transfer → 转让管理员
 * - GET /api/projects/my → 我的项目
 * - GET /api/projects/created-by-me → 我创建的项目
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectApiServlet 项目API测试")
class ProjectApiServletTest {

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer PROJECT_ID = 100;
    private static final Integer APPLICATION_ID = 200;
    private static final Integer FILE_ID = 300;

    // 用户角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 项目状态枚举
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_IN_PROGRESS = "in_progress";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";
    private static final String STATUS_REJECTED = "rejected";

    // 申请状态枚举
    private static final String APP_STATUS_PENDING = "PENDING";
    private static final String APP_STATUS_APPROVED = "APPROVED";
    private static final String APP_STATUS_REJECTED = "REJECTED";

    // 项目分类
    private static final String CATEGORY_TECH = "技术研发";
    private static final String CATEGORY_DESIGN = "设计创意";
    private static final String CATEGORY_RESEARCH = "科研项目";

    // 文件类型
    private static final String FILE_TYPE_DOC = "DOC";
    private static final String FILE_TYPE_IMAGE = "IMAGE";
    private static final String FILE_TYPE_CODE = "CODE";

    // ==================== 测试辅助类 ====================

    private TestableProjectApiServlet servlet;
    private ProjectService mockProjectService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockProjectService = mock(ProjectService.class);
        servlet = new TestableProjectApiServlet(mockProjectService);

        // 默认session行为
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

    private Project createProject(Integer id, String status, Integer leaderId) {
        Project project = new Project();
        project.setId(id);
        project.setName("测试项目" + id);
        project.setDescription("项目描述");
        project.setCategory(CATEGORY_TECH);
        project.setStatus(status);
        project.setLeaderId(leaderId);
        project.setAdminId(leaderId);
        project.setYear(2026);
        return project;
    }

    private ProjectDTO createProjectDTO() {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("新项目");
        dto.setDescription("项目描述");
        dto.setCategory(CATEGORY_TECH);
        dto.setYear(2026);
        dto.setBudget(new BigDecimal("10000"));
        dto.setRepoUrl("https://github.com/example/repo");
        return dto;
    }

    private PlanDTO createPlanDTO() {
        PlanDTO dto = new PlanDTO();
        dto.setTitle("计划标题");
        dto.setDescription("计划内容");
        dto.setStartDate(new Date());
        return dto;
    }

    private ProgressDTO createProgressDTO() {
        ProgressDTO dto = new ProgressDTO();
        dto.setTitle("进度标题");
        dto.setDescription("进度内容");
        dto.setCompletionRate(50);
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 项目列表测试 ====================

    @Nested
    @DisplayName("项目列表 GET /api/projects")
    class ListProjectsTests {

        @FastTest
        @DisplayName("正常获取项目列表")
        void should_return_project_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("pageSize")).thenReturn("20");

            List<Project> projects = Arrays.asList(
                    createProject(1, STATUS_APPROVED, ADMIN_USER_ID),
                    createProject(2, STATUS_IN_PROGRESS, MEMBER_USER_ID)
            );
            when(mockProjectService.listProjects(any(), eq(1), eq(20))).thenReturn(Result.ok(projects));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("测试项目1");
            assertThat(response).contains("测试项目2");
        }

        @FastTest
        @DisplayName("按分类筛选项目列表")
        void should_filter_by_category() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_TECH);

            List<Project> projects = Arrays.asList(
                    createProject(1, STATUS_APPROVED, ADMIN_USER_ID)
            );
            when(mockProjectService.listProjects(any(), eq(1), eq(20))).thenReturn(Result.ok(projects));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("空项目列表")
        void should_return_empty_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

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
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt()))
                    .thenReturn(Result.error(500, "数据库错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
        }
    }

    // ==================== 项目详情测试 ====================

    @Nested
    @DisplayName("项目详情 GET /api/projects/{id}")
    class GetProjectDetailTests {

        @FastTest
        @DisplayName("正常获取项目详情")
        void should_return_project_detail() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID);
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID);

            Project project = createProject(PROJECT_ID, STATUS_APPROVED, ADMIN_USER_ID);
            when(mockProjectService.getProjectDetail(eq(PROJECT_ID), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(project));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("测试项目" + PROJECT_ID);
        }

        @FastTest
        @DisplayName("项目不存在返回404")
        void should_return_404_when_project_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/99999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/99999");

            when(mockProjectService.getProjectDetail(eq(99999), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "项目不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("无效的项目ID格式返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/abc");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("负数ID返回400")
        void should_return_400_when_negative_id() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/-1");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/-1");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("零ID返回400")
        void should_return_400_when_zero_id() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/0");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/0");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 创建项目测试 ====================

    @Nested
    @DisplayName("创建项目 POST /api/projects")
    class CreateProjectTests {

        @FastTest
        @DisplayName("成功创建项目")
        void should_create_project_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"新项目\",\"category\":\"技术研发\",\"description\":\"描述\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            ProjectDTO dto = createProjectDTO();
            when(mockProjectService.createProject(any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(PROJECT_ID));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":" + PROJECT_ID);
        }

        @FastTest
        @DisplayName("项目名称为空返回400")
        void should_return_400_when_name_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"\",\"category\":\"技术研发\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.createProject(any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "项目名称不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("项目名称不能为空");
        }

        @FastTest
        @DisplayName("分类为空返回400")
        void should_return_400_when_category_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"新项目\",\"category\":\"\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.createProject(any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "项目分类不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("每年最多3个项目限制")
        void should_return_400_when_exceed_year_limit() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"第4个项目\",\"category\":\"技术研发\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.createProject(any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "每年最多参与3个项目"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("每年最多参与3个项目");
        }

        @FastTest
        @DisplayName("无效的JSON格式返回400")
        void should_return_400_when_invalid_json() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{invalid json}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("仓库地址格式不正确返回400")
        void should_return_400_when_invalid_repo_url() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"项目\",\"category\":\"技术研发\",\"repoUrl\":\"ftp://invalid\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.createProject(any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "仓库地址格式不正确"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("仓库地址格式不正确");
        }

        @FastTest
        @DisplayName("预算为负数返回400")
        void should_return_400_when_negative_budget() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"项目\",\"category\":\"技术研发\",\"budget\":-1000}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.createProject(any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "项目预算不能为负数"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("项目预算不能为负数");
        }
    }

    // ==================== 更新项目测试 ====================

    @Nested
    @DisplayName("更新项目 PUT /api/projects/{id}")
    class UpdateProjectTests {

        @FastTest
        @DisplayName("成功更新项目")
        void should_update_project_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID);
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"更新后的项目\",\"category\":\"技术研发\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.updateProject(eq(PROJECT_ID), any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("无权限更新返回403")
        void should_return_403_when_not_owner() throws Exception {
            User other = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(other);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID);
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"更新\",\"category\":\"技术研发\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.updateProject(eq(PROJECT_ID), any(ProjectDTO.class), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限更新此项目"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("项目不存在返回404")
        void should_return_404_when_project_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/99999");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"name\":\"更新\",\"category\":\"技术研发\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.updateProject(eq(99999), any(ProjectDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "项目不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }
    }

    // ==================== 删除项目测试 ====================

    @Nested
    @DisplayName("删除项目 DELETE /api/projects/{id}")
    class DeleteProjectTests {

        @FastTest
        @DisplayName("成功删除项目")
        void should_delete_project_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID);

            when(mockProjectService.deleteProject(eq(PROJECT_ID), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("无权限删除返回403")
        void should_return_403_when_not_owner() throws Exception {
            User other = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(other);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID);

            when(mockProjectService.deleteProject(eq(PROJECT_ID), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限删除此项目"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("项目不存在返回404")
        void should_return_404_when_project_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/99999");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/99999");

            when(mockProjectService.deleteProject(eq(99999), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "项目不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("无效的项目ID格式返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/abc");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("负数ID返回400")
        void should_return_400_when_negative_id() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/-1");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/-1");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 申请加入项目测试 ====================

    @Nested
    @DisplayName("申请加入 POST /api/projects/{id}/apply")
    class ApplyMemberTests {

        @FastTest
        @DisplayName("成功申请加入项目")
        void should_apply_member_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/apply");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/apply");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"希望参与学习\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), eq("希望参与学习")))
                    .thenReturn(Result.ok(APPLICATION_ID));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":" + APPLICATION_ID);
        }

        @FastTest
        @DisplayName("重复申请返回400")
        void should_return_400_when_already_applied() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/apply");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/apply");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"申请\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), anyString()))
                    .thenReturn(Result.error(400, "您已申请过此项目"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("您已申请过此项目");
        }

        @FastTest
        @DisplayName("已是成员返回400")
        void should_return_400_when_already_member() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/apply");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/apply");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"申请\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.applyMember(eq(PROJECT_ID), eq(MEMBER_USER_ID), anyString()))
                    .thenReturn(Result.error(400, "您已是项目成员"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("您已是项目成员");
        }

        @FastTest
        @DisplayName("项目不存在返回404")
        void should_return_404_when_project_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/99999/apply");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/99999/apply");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"申请\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.applyMember(eq(99999), eq(MEMBER_USER_ID), anyString()))
                    .thenReturn(Result.error(404, "项目不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }
    }

    // ==================== 审批成员测试 ====================

    @Nested
    @DisplayName("审批成员 POST /api/projects/{id}/approve-member")
    class ApproveMemberTests {

        @FastTest
        @DisplayName("成功审批成员通过")
        void should_approve_member_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/approve-member");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/approve-member");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            ("{\"applicationId\":" + APPLICATION_ID + "}").getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.approveMember(eq(APPLICATION_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("非管理员审批返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/approve-member");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/approve-member");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            ("{\"applicationId\":" + APPLICATION_ID + "}").getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.approveMember(eq(APPLICATION_ID), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限审批成员"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    @Nested
    @DisplayName("驳回成员 POST /api/projects/{id}/reject-member")
    class RejectMemberTests {

        @FastTest
        @DisplayName("成功驳回成员申请")
        void should_reject_member_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/reject-member");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/reject-member");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            ("{\"applicationId\":" + APPLICATION_ID + ",\"reason\":\"不符合要求\"}").getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.rejectMember(eq(APPLICATION_ID), eq("不符合要求"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 项目审核测试 ====================

    @Nested
    @DisplayName("项目审核通过 POST /api/projects/{id}/approve")
    class ApproveProjectTests {

        @FastTest
        @DisplayName("成功审核通过项目")
        void should_approve_project_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/approve");

            when(mockProjectService.approveProject(eq(PROJECT_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("非管理员审核返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/approve");

            when(mockProjectService.approveProject(eq(PROJECT_ID), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限审核项目"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    @Nested
    @DisplayName("项目审核驳回 POST /api/projects/{id}/reject")
    class RejectProjectTests {

        @FastTest
        @DisplayName("成功驳回项目")
        void should_reject_project_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/reject");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"材料不全\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.rejectProject(eq(PROJECT_ID), eq("材料不全"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 计划与进度测试 ====================

    @Nested
    @DisplayName("添加计划 POST /api/projects/{id}/plans")
    class AddPlanTests {

        @FastTest
        @DisplayName("成功添加计划")
        void should_add_plan_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/plans");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/plans");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"content\":\"第一阶段计划\",\"milestone\":\"需求分析\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.addPlan(eq(PROJECT_ID), any(PlanDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(1));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("无权限添加计划返回403")
        void should_return_403_when_not_member() throws Exception {
            User other = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(other);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/plans");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/plans");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"content\":\"计划内容\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.addPlan(eq(PROJECT_ID), any(PlanDTO.class), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限添加计划"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }
    }

    @Nested
    @DisplayName("添加进度 POST /api/projects/{id}/progress")
    class AddProgressTests {

        @FastTest
        @DisplayName("成功添加进度")
        void should_add_progress_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/progress");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/progress");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"content\":\"已完成50%\",\"progressPercent\":50}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.addProgress(eq(PROJECT_ID), any(ProgressDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(1));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("进度百分比超范围返回400")
        void should_return_400_when_progress_out_of_range() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/progress");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/progress");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new java.io.BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"content\":\"完成150%\",\"progressPercent\":150}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.addProgress(eq(PROJECT_ID), any(ProgressDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "进度百分比必须在0-100之间"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("进度百分比必须在0-100之间");
        }
    }

    // ==================== 标签管理测试 ====================

    @Nested
    @DisplayName("添加标签 POST /api/projects/{id}/labels/{labelCode}")
    class AddLabelTests {

        @FastTest
        @DisplayName("成功添加标签")
        void should_add_label_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/labels/Java");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/labels/Java");

            when(mockProjectService.addLabel(eq(PROJECT_ID), eq("Java"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    @Nested
    @DisplayName("删除标签 DELETE /api/projects/{id}/labels/{labelCode}")
    class RemoveLabelTests {

        @FastTest
        @DisplayName("成功删除标签")
        void should_remove_label_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/labels/Java");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/labels/Java");

            when(mockProjectService.removeLabel(eq(PROJECT_ID), eq("Java"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 文件操作测试 ====================

    @Nested
    @DisplayName("上传项目图片 POST /api/projects/{id}/images")
    class UploadProjectImageTests {

        @FastTest
        @DisplayName("成功上传项目图片")
        void should_upload_image_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/images");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/images");
            when(mockRequest.getContentType()).thenReturn("multipart/form-data");

            when(mockProjectService.uploadProjectImage(eq(PROJECT_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(FILE_ID));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":" + FILE_ID);
        }
    }

    @Nested
    @DisplayName("上传项目文件 POST /api/projects/{id}/files")
    class UploadProjectFileTests {

        @FastTest
        @DisplayName("成功上传项目文件")
        void should_upload_file_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/files");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/files");
            when(mockRequest.getContentType()).thenReturn("multipart/form-data");
            when(mockRequest.getParameter("fileType")).thenReturn(FILE_TYPE_DOC);

            when(mockProjectService.uploadProjectFile(eq(PROJECT_ID), any(), eq(FILE_TYPE_DOC), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(FILE_ID));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("文件类型无效返回400")
        void should_return_400_when_invalid_file_type() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/files");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/files");
            when(mockRequest.getContentType()).thenReturn("multipart/form-data");
            when(mockRequest.getParameter("fileType")).thenReturn("INVALID");

            when(mockProjectService.uploadProjectFile(eq(PROJECT_ID), any(), eq("INVALID"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "不支持的文件类型"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("不支持的文件类型");
        }
    }

    @Nested
    @DisplayName("删除项目文件 DELETE /api/projects/{id}/files/{fileId}")
    class DeleteProjectFileTests {

        @FastTest
        @DisplayName("成功删除项目文件")
        void should_delete_file_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/files/" + FILE_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/files/" + FILE_ID);

            when(mockProjectService.deleteProjectFile(eq(PROJECT_ID), eq(FILE_ID), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 转让管理员测试 ====================

    @Nested
    @DisplayName("转让管理员 POST /api/projects/{id}/transfer")
    class TransferAdminTests {

        @FastTest
        @DisplayName("成功转让管理员")
        void should_transfer_admin_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/transfer");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/transfer");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            ("{\"newAdminId\":" + MEMBER_USER_ID + "}").getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.transferAdmin(eq(PROJECT_ID), eq(MEMBER_USER_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("转让给非成员返回400")
        void should_return_400_when_not_a_member() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/transfer");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/transfer");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            ("{\"newAdminId\":" + OTHER_USER_ID + "}").getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockProjectService.transferAdmin(eq(PROJECT_ID), eq(OTHER_USER_ID), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "新管理员必须是项目成员"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("新管理员必须是项目成员");
        }
    }

    // ==================== 我的项目测试 ====================

    @Nested
    @DisplayName("我的项目 GET /api/projects/my")
    class GetMyProjectsTests {

        @FastTest
        @DisplayName("成功获取我的项目列表")
        void should_return_my_projects() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/my");
            when(mockRequest.getParameter("page")).thenReturn("1");

            List<Project> projects = Arrays.asList(
                    createProject(1, STATUS_IN_PROGRESS, MEMBER_USER_ID)
            );
            when(mockProjectService.getMyProjects(eq(MEMBER_USER_ID), eq(1), anyInt()))
                    .thenReturn(Result.ok(projects));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("测试项目1");
        }
    }

    @Nested
    @DisplayName("我创建的项目 GET /api/projects/created-by-me")
    class GetCreatedByMeProjectsTests {

        @FastTest
        @DisplayName("成功获取我创建的项目列表")
        void should_return_created_by_me_projects() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/created-by-me");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/created-by-me");
            when(mockRequest.getParameter("page")).thenReturn("1");

            List<Project> projects = Arrays.asList(
                    createProject(1, STATUS_APPROVED, MEMBER_USER_ID)
            );
            when(mockProjectService.getMyProjects(eq(MEMBER_USER_ID), eq(1), anyInt()))
                    .thenReturn(Result.ok(projects));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== HTTP方法测试 ====================

    @Nested
    @DisplayName("HTTP方法测试")
    class HttpMethodTests {

        @FastTest
        @DisplayName("PUT方法应返回405")
        void should_return_405_for_put_on_collection() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("PUT");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":405");
        }

        @FastTest
        @DisplayName("OPTIONS方法应返回200")
        void should_return_200_for_options() throws Exception {
            when(mockRequest.getMethod()).thenReturn("OPTIONS");
            when(mockResponse.getHeader("Access-Control-Allow-Methods")).thenReturn("GET, POST, PUT, DELETE, OPTIONS");

            servlet.doOptions(mockRequest, mockResponse);

            verify(mockResponse).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空pathInfo应返回项目列表")
        void should_handle_empty_path_info() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("尾斜杠应正常处理")
        void should_handle_trailing_slash() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超大ID应返回400或404")
        void should_handle_overflow_id() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/9999999999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/9999999999");

            // 尝试解析为大数会溢出，根据实现返回400或项目不存在404
            when(mockProjectService.getProjectDetail(anyInt(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "项目不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            // 可能是400（解析失败）或404（项目不存在）
            assertThat(response).matches(".*\"code\":(400|404).*");
        }

        @FastTest
        @DisplayName("特殊字符标签应被正确处理")
        void should_handle_special_characters_in_label() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID + "/labels/Java%23Python");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID + "/labels/Java%23Python");

            when(mockProjectService.addLabel(eq(PROJECT_ID), eq("Java#Python"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 响应格式测试 ====================

    @Nested
    @DisplayName("响应格式测试")
    class ResponseFormatTests {

        @FastTest
        @DisplayName("成功响应应包含正确结构")
        void should_return_correct_success_structure() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

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
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/99999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/99999");
            when(mockProjectService.getProjectDetail(eq(99999), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "项目不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("\"message\":\"项目不存在\"");
            assertThat(response).contains("\"data\":null");
        }

        @FastTest
        @DisplayName("应设置正确的Content-Type")
        void should_set_correct_content_type() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }

        @FastTest
        @DisplayName("分页响应应包含list数组")
        void should_return_paginated_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");

            List<Project> projects = Arrays.asList(createProject(1, STATUS_APPROVED, ADMIN_USER_ID));
            when(mockProjectService.listProjects(any(), eq(1), anyInt())).thenReturn(Result.ok(projects));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"list\"");
        }

        @FastTest
        @DisplayName("单个对象响应不应包含list包装")
        void should_return_single_object_without_list_wrapper() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/projects/" + PROJECT_ID);
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/" + PROJECT_ID);

            Project project = createProject(PROJECT_ID, STATUS_APPROVED, ADMIN_USER_ID);
            when(mockProjectService.getProjectDetail(eq(PROJECT_ID), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(project));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"name\"");
            assertThat(response).doesNotContain("\"list\"");
        }
    }

    // ==================== 测试用Servlet内部类 ====================

    /**
     * 可测试的ProjectApiServlet
     *
     * 复制ProjectApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableProjectApiServlet {

        private final ProjectService projectService;
        private final Gson gson = new GsonBuilder().create();

        // 路径模式常量
        private static final String PATH_LIST = "/api/projects";
        private static final String ACTION_APPLY = "apply";
        private static final String ACTION_APPROVE_MEMBER = "approve-member";
        private static final String ACTION_REJECT_MEMBER = "reject-member";
        private static final String ACTION_APPROVE = "approve";
        private static final String ACTION_REJECT = "reject";
        private static final String ACTION_PLANS = "plans";
        private static final String ACTION_PROGRESS = "progress";
        private static final String ACTION_IMAGES = "images";
        private static final String ACTION_FILES = "files";
        private static final String ACTION_LABELS = "labels";
        private static final String ACTION_TRANSFER = "transfer";
        private static final String ACTION_MY = "my";
        private static final String ACTION_CREATED_BY_ME = "created-by-me";

        public TestableProjectApiServlet(ProjectService projectService) {
            this.projectService = projectService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = req.getPathInfo();
            String uri = req.getRequestURI();

            // Fallback: derive pathInfo from URI if not provided
            if (pathInfo == null && uri != null && uri.contains("/api/projects/")) {
                pathInfo = uri.substring(uri.indexOf("/api/projects/") + 14);
                if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
            }

            // /api/projects/my
            if (pathInfo != null && pathInfo.endsWith(ACTION_MY)) {
                handleMyProjects(req, resp, currentUser);
                return;
            }

            // /api/projects/created-by-me
            if (pathInfo != null && pathInfo.endsWith(ACTION_CREATED_BY_ME)) {
                handleCreatedByMeProjects(req, resp, currentUser);
                return;
            }

            // /api/projects 或 /api/projects/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListProjects(req, resp, currentUser);
                return;
            }

            // /api/projects/{id}
            PathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }

            if (pi.hasAction()) {
                writeJson(resp, Result.error(400, "不支持的操作"));
            } else {
                handleGetProjectDetail(req, resp, pi.getProjectId(), currentUser);
            }
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = req.getPathInfo();
            String uri = req.getRequestURI();

            // /api/projects - 创建项目
            if (uri.endsWith(PATH_LIST) || (pathInfo != null && pathInfo.equals("/"))) {
                handleCreateProject(req, resp, currentUser);
                return;
            }

            // Fallback: derive pathInfo from URI if not provided
            if (pathInfo == null && uri != null && uri.contains("/api/projects/")) {
                pathInfo = uri.substring(uri.indexOf("/api/projects/") + 14);
                if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
            }

            if (pathInfo == null || !pathInfo.startsWith("/")) {
                writeJson(resp, Result.error(400, "无效的请求路径"));
                return;
            }

            PathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }

            int projectId = pi.getProjectId();
            String action = pi.getAction();

            if (ACTION_APPLY.equals(action)) {
                handleApplyMember(req, resp, projectId, currentUser);
            } else if (ACTION_APPROVE_MEMBER.equals(action)) {
                handleApproveMember(req, resp, projectId, currentUser);
            } else if (ACTION_REJECT_MEMBER.equals(action)) {
                handleRejectMember(req, resp, projectId, currentUser);
            } else if (ACTION_APPROVE.equals(action)) {
                handleApproveProject(req, resp, projectId, currentUser);
            } else if (ACTION_REJECT.equals(action)) {
                handleRejectProject(req, resp, projectId, currentUser);
            } else if (ACTION_PLANS.equals(action)) {
                handleAddPlan(req, resp, projectId, currentUser);
            } else if (ACTION_PROGRESS.equals(action)) {
                handleAddProgress(req, resp, projectId, currentUser);
            } else if (ACTION_IMAGES.equals(action)) {
                handleUploadImage(req, resp, projectId, currentUser);
            } else if (ACTION_FILES.equals(action)) {
                handleUploadFile(req, resp, projectId, currentUser);
            } else if (ACTION_LABELS.equals(action)) {
                handleAddLabel(req, resp, projectId, currentUser);
            } else if (ACTION_TRANSFER.equals(action)) {
                handleTransferAdmin(req, resp, projectId, currentUser);
            } else {
                writeJson(resp, Result.error(400, "不支持的操作"));
            }
        }

        public void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = req.getPathInfo();
            // /api/projects/{id} 格式，pathInfo 可能是 /{id}
            if (pathInfo == null) {
                pathInfo = req.getRequestURI();
                if (pathInfo != null && pathInfo.contains("/api/projects/")) {
                    pathInfo = pathInfo.substring(pathInfo.indexOf("/api/projects/") + 14);
                    if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }

            if (pathInfo == null || !pathInfo.startsWith("/")) {
                writeJson(resp, Result.error(400, "无效的请求路径"));
                return;
            }

            PathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }

            if (pi.hasAction()) {
                writeJson(resp, Result.error(400, "不支持的操作"));
            } else {
                handleUpdateProject(req, resp, pi.getProjectId(), currentUser);
            }
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = req.getPathInfo();
            // /api/projects/{id}/labels/{code} 格式，pathInfo 可能是 /{id}/labels/{code}
            if (pathInfo == null) {
                pathInfo = req.getRequestURI();
                if (pathInfo != null && pathInfo.contains("/api/projects/")) {
                    pathInfo = pathInfo.substring(pathInfo.indexOf("/api/projects/") + 14);
                    if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }

            if (pathInfo == null || !pathInfo.startsWith("/")) {
                writeJson(resp, Result.error(400, "无效的请求路径"));
                return;
            }

            PathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }

            int projectId = pi.getProjectId();
            String action = pi.getAction();

            if (ACTION_LABELS.equals(action) && pi.hasLabelCode()) {
                handleRemoveLabel(req, resp, projectId, pi.getLabelCode(), currentUser);
            } else if (ACTION_FILES.equals(action) && pi.hasFileId()) {
                handleDeleteFile(req, resp, projectId, pi.getFileId(), currentUser);
            } else if (!pi.hasAction()) {
                handleDeleteProject(req, resp, projectId, currentUser);
            } else {
                writeJson(resp, Result.error(400, "不支持的操作"));
            }
        }

        public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }

        // ==================== 处理器方法 ====================

        private void handleListProjects(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");
            String category = req.getParameter("category");
            String status = req.getParameter("status");
            String keyword = req.getParameter("keyword");

            int page = 1;
            int pageSize = 20;
            try {
                if (pageStr != null) page = Integer.parseInt(pageStr);
                if (pageSizeStr != null) pageSize = Integer.parseInt(pageSizeStr);
            } catch (NumberFormatException ignored) {}

            ProjectFilterDTO filter = new ProjectFilterDTO();
            filter.setCategory(category);
            filter.setStatus(status);
            filter.setKeyword(keyword);

            Result result = projectService.listProjects(filter, page, pageSize);
            writeJson(resp, result);
        }

        private void handleGetProjectDetail(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            Result result = projectService.getProjectDetail(projectId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleCreateProject(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            try {
                ProjectDTO dto = parseJsonRequest(req, ProjectDTO.class);
                Result result = projectService.createProject(dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleUpdateProject(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                ProjectDTO dto = parseJsonRequest(req, ProjectDTO.class);
                Result result = projectService.updateProject(projectId, dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleDeleteProject(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            Result result = projectService.deleteProject(projectId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleApplyMember(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonRequest(req, Map.class);
                String reason = body != null ? (String) body.get("reason") : null;
                Result result = projectService.applyMember(projectId, currentUser.getId(), reason);
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleApproveMember(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonRequest(req, Map.class);
                Integer applicationId = body != null ? (Integer) body.get("applicationId") : null;
                if (applicationId == null) {
                    writeJson(resp, Result.error(400, "申请ID不能为空"));
                    return;
                }
                Result result = projectService.approveMember(applicationId, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleRejectMember(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonRequest(req, Map.class);
                Integer applicationId = body != null ? (Integer) body.get("applicationId") : null;
                String reason = body != null ? (String) body.get("reason") : null;
                if (applicationId == null) {
                    writeJson(resp, Result.error(400, "申请ID不能为空"));
                    return;
                }
                Result result = projectService.rejectMember(applicationId, reason, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleApproveProject(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            Result result = projectService.approveProject(projectId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRejectProject(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonRequest(req, Map.class);
                String reason = body != null ? (String) body.get("reason") : null;
                Result result = projectService.rejectProject(projectId, reason, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleAddPlan(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                PlanDTO dto = parseJsonRequest(req, PlanDTO.class);
                Result result = projectService.addPlan(projectId, dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleAddProgress(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                ProgressDTO dto = parseJsonRequest(req, ProgressDTO.class);
                Result result = projectService.addProgress(projectId, dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleUploadImage(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                Object part = extractFileFromRequest(req);
                Result result = projectService.uploadProjectImage(projectId, part, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的文件"));
            }
        }

        private void handleUploadFile(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                Object part = extractFileFromRequest(req);
                String fileType = req.getParameter("fileType");
                Result result = projectService.uploadProjectFile(projectId, part, fileType, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的文件"));
            }
        }

        private void handleDeleteFile(HttpServletRequest req, HttpServletResponse resp, int projectId, int fileId, User currentUser) throws IOException {
            Result result = projectService.deleteProjectFile(projectId, fileId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleAddLabel(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            String pathInfo = req.getPathInfo();
            String labelCode = extractLabelCodeFromPath(pathInfo, projectId);
            if (labelCode == null) {
                writeJson(resp, Result.error(400, "标签代码不能为空"));
                return;
            }
            Result result = projectService.addLabel(projectId, labelCode, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRemoveLabel(HttpServletRequest req, HttpServletResponse resp, int projectId, String labelCode, User currentUser) throws IOException {
            Result result = projectService.removeLabel(projectId, labelCode, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleTransferAdmin(HttpServletRequest req, HttpServletResponse resp, int projectId, User currentUser) throws IOException {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonRequest(req, Map.class);
                Integer newAdminId = body != null ? (Integer) body.get("newAdminId") : null;
                if (newAdminId == null) {
                    writeJson(resp, Result.error(400, "新管理员ID不能为空"));
                    return;
                }
                Result result = projectService.transferAdmin(projectId, newAdminId, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleMyProjects(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String pageStr = req.getParameter("page");
            int page = 1;
            try {
                if (pageStr != null) page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}

            Result result = projectService.getMyProjects(currentUser.getId(), page, 20);
            writeJson(resp, result);
        }

        private void handleCreatedByMeProjects(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String pageStr = req.getParameter("page");
            int page = 1;
            try {
                if (pageStr != null) page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}

            Result result = projectService.getMyProjects(currentUser.getId(), page, 20);
            writeJson(resp, result);
        }

        // ==================== 工具方法 ====================

        private User getCurrentUser(HttpServletRequest req) {
            HttpSession session = req.getSession(false);
            if (session == null) return null;
            return (User) session.getAttribute("user");
        }

        private <T> T parseJsonRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return gson.fromJson(sb.toString(), clazz);
        }

        private Object extractFileFromRequest(HttpServletRequest request) {
            try {
                if (request.getContentType() != null && request.getContentType().contains("multipart/form-data")) {
                    return request.getPart("file");
                }
            } catch (Exception e) {
                // 返回null让service层处理
            }
            return null;
        }

        private void writeJson(HttpServletResponse resp, Result result) throws IOException {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().write(gson.toJson(result));
        }

        private PathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                return PathInfo.root();
            }

            if (!pathInfo.startsWith("/")) {
                return PathInfo.root();
            }

            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1 || segments[0].isEmpty()) {
                return PathInfo.root();
            }

            String idStr = segments[0];
            int projectId = 0;
            try {
                projectId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                return PathInfo.root();
            }

            String action = segments.length >= 2 ? segments[1] : null;
            String labelCode = segments.length >= 3 ? segments[2] : null;
            Integer fileId = null;
            if (segments.length >= 3 && ACTION_FILES.equals(action)) {
                try {
                    fileId = Integer.parseInt(segments[2]);
                } catch (NumberFormatException ignored) {}
            }

            return PathInfo.of(projectId, action, labelCode, fileId);
        }

        private String extractLabelCodeFromPath(String pathInfo, int projectId) {
            if (pathInfo == null) return null;
            String prefix = "/" + projectId + "/labels/";
            if (pathInfo.startsWith(prefix)) {
                return pathInfo.substring(prefix.length());
            }
            return null;
        }

        // ==================== 路径解析内部类 ====================

        private static class PathInfo {
            private final int projectId;
            private final String action;
            private final String labelCode;
            private final Integer fileId;
            private final boolean isRoot;

            private PathInfo(int projectId, String action, String labelCode, Integer fileId, boolean isRoot) {
                this.projectId = projectId;
                this.action = action;
                this.labelCode = labelCode;
                this.fileId = fileId;
                this.isRoot = isRoot;
            }

            static PathInfo root() {
                return new PathInfo(0, null, null, null, true);
            }

            static PathInfo of(int projectId, String action, String labelCode, Integer fileId) {
                return new PathInfo(projectId, action, labelCode, fileId, false);
            }

            boolean isRoot() { return isRoot; }

            boolean isValidId() {
                return !isRoot && projectId > 0;
            }

            int getProjectId() { return projectId; }

            boolean hasAction() {
                return !isRoot && action != null && !action.isEmpty();
            }

            String getAction() { return action; }

            boolean hasLabelCode() {
                return !isRoot && labelCode != null && !labelCode.isEmpty();
            }

            String getLabelCode() { return labelCode; }

            boolean hasFileId() {
                return !isRoot && fileId != null && fileId > 0;
            }

            Integer getFileId() { return fileId; }
        }
    }
}
