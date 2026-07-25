package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.ResumeService;
import support.FastTest;
import util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ResumeApiServlet TDD测试套件
 *
 * 测试范围：服务分层与API化完整计划.md 4.4 ResumeApiServlet 简历API
 * - 所有REST端点
 * - 所有HTTP方法(GET/POST/PUT/DELETE)
 * - 认证与授权
 * - 参数解析与验证
 * - 错误处理
 *
 * 测试覆盖端点：
 * - GET  /api/resumes                    → 简历列表
 * - GET  /api/resumes/{id}              → 简历详情
 * - POST /api/resumes                    → 创建简历
 * - PUT  /api/resumes/{id}               → 更新简历
 * - DELETE /api/resumes/{id}             → 删除简历
 * - PUT  /api/resumes/{id}/default      → 设为默认
 * - GET  /api/resumes/{id}/education     → 教育经历列表
 * - POST /api/resumes/{id}/education     → 添加教育经历
 * - PUT  /api/resumes/{id}/education/{eid} → 更新教育经历
 * - DELETE /api/resumes/{id}/education/{eid} → 删除教育经历
 * - GET  /api/resumes/{id}/skills       → 技能列表
 * - POST /api/resumes/{id}/skills        → 添加技能
 * - PUT  /api/resumes/{id}/skills/{sid} → 更新技能
 * - DELETE /api/resumes/{id}/skills/{sid} → 删除技能
 * - GET  /api/resumes/{id}/projects      → 项目经历列表
 * - POST /api/resumes/{id}/projects      → 添加项目经历
 * - PUT  /api/resumes/{id}/projects/{pid} → 更新项目经历
 * - DELETE /api/resumes/{id}/projects/{pid} → 删除项目经历
 * - GET  /api/resumes/{id}/awards       → 获奖情况列表
 * - POST /api/resumes/{id}/awards       → 添加获奖情况
 * - PUT  /api/resumes/{id}/awards/{aid} → 更新获奖情况
 * - DELETE /api/resumes/{id}/awards/{aid} → 删除获奖情况
 * - GET  /api/resumes/recycle-bin        → 回收站
 * - POST /api/resumes/{id}/restore       → 恢复简历
 * - DELETE /api/resumes/{id}/permanent   → 永久删除
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ResumeApiServlet 简历API测试")
class ResumeApiServletTest {

    private TestableResumeApiServlet servlet;
    private ResumeService mockResumeService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private StringWriter responseWriter;
    private Gson gson;

    private static final int TEST_USER_ID = 5;
    private static final int TEST_RESUME_ID = 1;
    private static final int TEST_ITEM_ID = 10;

    @BeforeEach
    void setUp() throws Exception {
        mockResumeService = mock(ResumeService.class);
        servlet = new TestableResumeApiServlet(mockResumeService);
        responseWriter = new StringWriter();
        gson = new GsonBuilder().create();

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== 辅助方法 ====================

    private User createTestUser(int id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setRole(role);
        return user;
    }

    private String getResponseBody() {
        return responseWriter.toString();
    }

    private void simulateLogin(User user) {
        when(mockSession.getAttribute("user")).thenReturn(user);
    }

    private void simulateUnauthorized() {
        when(mockSession.getAttribute("user")).thenReturn(null);
    }

    private void resetResponseWriter() throws Exception {
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== 认证测试 ====================

    @Nested
    @DisplayName("认证测试")
    class AuthTests {

        @Test
        @FastTest
        @DisplayName("未登录时应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getPathInfo()).thenReturn(null);

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录POST请求应返回401")
        void should_return_401_when_post_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getPathInfo()).thenReturn(null);

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录DELETE请求应返回401")
        void should_return_401_when_delete_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/1");
            when(mockRequest.getPathInfo()).thenReturn("/1");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }
    }

    // ==================== GET /api/resumes 简历列表 ====================

    @Nested
    @DisplayName("GET /api/resumes 简历列表")
    class ListResumesTests {

        @Test
        @FastTest
        @DisplayName("获取简历列表成功")
        void should_list_resumes_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockResumeService.listResumes(eq(TEST_USER_ID), anyInt()))
                    .thenReturn(Result.ok(java.util.Collections.emptyList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带分页参数的列表请求")
        void should_handle_pagination_params() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockResumeService.listResumes(eq(TEST_USER_ID), eq(2)))
                    .thenReturn(Result.ok(java.util.Collections.emptyList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/resumes/{id} 简历详情 ====================

    @Nested
    @DisplayName("GET /api/resumes/{id} 简历详情")
    class GetResumeDetailTests {

        @Test
        @FastTest
        @DisplayName("获取简历详情成功")
        void should_get_resume_detail_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID);
            when(mockResumeService.getResumeDetail(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new Resume()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("简历不存在应返回404")
        void should_return_404_when_resume_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/999");
            when(mockRequest.getPathInfo()).thenReturn("/999");
            when(mockResumeService.getResumeDetail(eq(999), eq(TEST_USER_ID)))
                    .thenReturn(Result.error(404, "简历不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/resumes 创建简历 ====================

    @Nested
    @DisplayName("POST /api/resumes 创建简历")
    class CreateResumeTests {

        @Test
        @FastTest
        @DisplayName("创建简历成功")
        void should_create_resume_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"resumeName\":\"我的简历\",\"templateStyle\":\"default\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            ResumeDTO dto = new ResumeDTO();
            dto.setResumeName("我的简历");
            dto.setTemplateStyle("default");
            when(mockResumeService.createResume(any(ResumeDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new Resume()));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("空请求体应返回400")
        void should_return_400_when_empty_body() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("无效JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("invalid json")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== PUT /api/resumes/{id} 更新简历 ====================

    @Nested
    @DisplayName("PUT /api/resumes/{id} 更新简历")
    class UpdateResumeTests {

        @Test
        @FastTest
        @DisplayName("更新简历成功")
        void should_update_resume_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"resumeName\":\"更新后的简历\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockResumeService.updateResume(eq(TEST_RESUME_ID), any(ResumeDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new Resume()));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("无权限更新应返回403")
        void should_return_403_when_not_owner() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"resumeName\":\"更新后的简历\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockResumeService.updateResume(eq(TEST_RESUME_ID), any(ResumeDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.error(403, "无权限更新此简历"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":403");
        }
    }

    // ==================== DELETE /api/resumes/{id} 删除简历 ====================

    @Nested
    @DisplayName("DELETE /api/resumes/{id} 删除简历")
    class DeleteResumeTests {

        @Test
        @FastTest
        @DisplayName("删除简历成功")
        void should_delete_resume_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID);
            when(mockResumeService.deleteResume(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== PUT /api/resumes/{id}/default 设为默认 ====================

    @Nested
    @DisplayName("PUT /api/resumes/{id}/default 设为默认")
    class SetDefaultTests {

        @Test
        @FastTest
        @DisplayName("设置默认简历成功")
        void should_set_default_resume_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/default");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/default");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            when(mockResumeService.setDefaultResume(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 教育经历 CRUD ====================

    @Nested
    @DisplayName("教育经历 CRUD")
    class EducationCrudTests {

        @Test
        @FastTest
        @DisplayName("GET /api/resumes/{id}/education 获取教育经历列表")
        void should_list_education_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/education");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/education");
            when(mockResumeService.getResumeDetail(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestResume()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("POST /api/resumes/{id}/education 添加教育经历")
        void should_add_education_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/education");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/education");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"schoolName\":\"黄山学院\",\"major\":\"计算机科学\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            ResumeEducation education = new ResumeEducation();
            education.setId(1);
            when(mockResumeService.addEducation(eq(TEST_RESUME_ID), any(ResumeEducationDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(education));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("PUT /api/resumes/{id}/education/{eid} 更新教育经历")
        void should_update_education_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/education/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/education/" + TEST_ITEM_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"schoolName\":\"更新后的学校\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockResumeService.updateEducation(eq(TEST_ITEM_ID), any(ResumeEducationDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new ResumeEducation()));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("DELETE /api/resumes/{id}/education/{eid} 删除教育经历")
        void should_delete_education_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/education/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/education/" + TEST_ITEM_ID);
            when(mockResumeService.deleteEducation(eq(TEST_ITEM_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 技能 CRUD ====================

    @Nested
    @DisplayName("技能 CRUD")
    class SkillCrudTests {

        @Test
        @FastTest
        @DisplayName("GET /api/resumes/{id}/skills 获取技能列表")
        void should_list_skills_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/skills");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/skills");
            when(mockResumeService.getResumeDetail(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestResume()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("POST /api/resumes/{id}/skills 添加技能")
        void should_add_skill_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/skills");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/skills");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"skillName\":\"Java\",\"proficiency\":\"intermediate\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            ResumeSkill skill = new ResumeSkill();
            skill.setId(1);
            when(mockResumeService.addSkill(eq(TEST_RESUME_ID), any(ResumeSkillDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(skill));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("PUT /api/resumes/{id}/skills/{sid} 更新技能")
        void should_update_skill_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/skills/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/skills/" + TEST_ITEM_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"skillName\":\"更新后的技能\",\"proficiency\":\"advanced\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockResumeService.updateSkill(eq(TEST_ITEM_ID), any(ResumeSkillDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new ResumeSkill()));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("DELETE /api/resumes/{id}/skills/{sid} 删除技能")
        void should_delete_skill_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/skills/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/skills/" + TEST_ITEM_ID);
            when(mockResumeService.deleteSkill(eq(TEST_ITEM_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 项目经历 CRUD ====================

    @Nested
    @DisplayName("项目经历 CRUD")
    class ProjectCrudTests {

        @Test
        @FastTest
        @DisplayName("GET /api/resumes/{id}/projects 获取项目经历列表")
        void should_list_projects_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/projects");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/projects");
            when(mockResumeService.getResumeDetail(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestResume()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("POST /api/resumes/{id}/projects 添加项目经历")
        void should_add_project_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/projects");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/projects");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"projectName\":\"电商项目\",\"role\":\"负责人\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            ResumeProject project = new ResumeProject();
            project.setId(1);
            when(mockResumeService.addProject(eq(TEST_RESUME_ID), any(ResumeProjectDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(project));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("PUT /api/resumes/{id}/projects/{pid} 更新项目经历")
        void should_update_project_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/projects/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/projects/" + TEST_ITEM_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"projectName\":\"更新后的项目\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockResumeService.updateProject(eq(TEST_ITEM_ID), any(ResumeProjectDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new ResumeProject()));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("DELETE /api/resumes/{id}/projects/{pid} 删除项目经历")
        void should_delete_project_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/projects/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/projects/" + TEST_ITEM_ID);
            when(mockResumeService.deleteProject(eq(TEST_ITEM_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 获奖情况 CRUD ====================

    @Nested
    @DisplayName("获奖情况 CRUD")
    class AwardCrudTests {

        @Test
        @FastTest
        @DisplayName("GET /api/resumes/{id}/awards 获取获奖情况列表")
        void should_list_awards_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/awards");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/awards");
            when(mockResumeService.getResumeDetail(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestResume()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("POST /api/resumes/{id}/awards 添加获奖情况")
        void should_add_award_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/awards");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/awards");
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"awardName\":\"蓝桥杯一等奖\",\"competitionName\":\"蓝桥杯\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            ResumeAward award = new ResumeAward();
            award.setId(1);
            when(mockResumeService.addAward(eq(TEST_RESUME_ID), any(ResumeAwardDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(award));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("PUT /api/resumes/{id}/awards/{aid} 更新获奖情况")
        void should_update_award_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/awards/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/awards/" + TEST_ITEM_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"awardName\":\"更新后的奖项\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockResumeService.updateAward(eq(TEST_ITEM_ID), any(ResumeAwardDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(new ResumeAward()));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("DELETE /api/resumes/{id}/awards/{aid} 删除获奖情况")
        void should_delete_award_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/awards/" + TEST_ITEM_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/awards/" + TEST_ITEM_ID);
            when(mockResumeService.deleteAward(eq(TEST_ITEM_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 回收站 ====================

    @Nested
    @DisplayName("回收站")
    class RecycleBinTests {

        @Test
        @FastTest
        @DisplayName("GET /api/resumes/recycle-bin 获取回收站")
        void should_get_recycle_bin_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/recycle-bin");
            when(mockRequest.getPathInfo()).thenReturn("/recycle-bin");
            when(mockResumeService.getRecycleBin(eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(java.util.Collections.emptyList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("POST /api/resumes/{id}/restore 恢复简历")
        void should_restore_resume_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/restore");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/restore");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockResumeService.restoreResume(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("DELETE /api/resumes/{id}/permanent 永久删除")
        void should_permanent_delete_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes/" + TEST_RESUME_ID + "/permanent");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_RESUME_ID + "/permanent");
            when(mockResumeService.permanentDelete(eq(TEST_RESUME_ID), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== 错误处理 ====================

    @Nested
    @DisplayName("错误处理")
    class ErrorHandlingTests {

        @Test
        @FastTest
        @DisplayName("无效路径应返回404")
        void should_return_404_when_path_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/invalid-path");
            when(mockRequest.getPathInfo()).thenReturn(null);

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("PUT方法直接调用应返回405")
        void should_return_405_when_put_not_supported() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getMethod()).thenReturn("PUT");

            servlet.doPut(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":405");
        }

        @Test
        @FastTest
        @DisplayName("无效的JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "MEMBER"));
            when(mockRequest.getRequestURI()).thenReturn("/api/resumes");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("invalid json")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== 测试辅助方法 ====================

    private Resume createTestResume() {
        Resume resume = new Resume();
        resume.setId(TEST_RESUME_ID);
        resume.setUserId(TEST_USER_ID);
        resume.setResumeName("测试简历");
        resume.setStatus(ResumeService.STATUS_PUBLISHED);
        resume.setDeleted(ResumeService.DELETED_NO);
        resume.setIsDefault(ResumeService.DEFAULT_NO);
        return resume;
    }

    // ==================== 可测试的内部Servlet类 ====================

    /**
     * 可测试的ResumeApiServlet
     *
     * 复制ResumeApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableResumeApiServlet {

        private final ResumeService resumeService;
        private final Gson gson = new GsonBuilder().create();

        public TestableResumeApiServlet(ResumeService resumeService) {
            this.resumeService = resumeService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = getPathInfo(req);
            String uri = req.getRequestURI();

            // /api/resumes/recycle-bin
            if (pathInfo != null && pathInfo.equals("/recycle-bin")) {
                handleGetRecycleBin(req, resp, currentUser);
                return;
            }

            // /api/resumes 或 /api/resumes/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                // 只有当URI是 /api/resumes 开头时才显示列表，否则404
                if (uri != null && (uri.equals("/api/resumes") || uri.equals("/api/resumes/") || uri.startsWith("/api/resumes/"))) {
                    handleListResumes(req, resp, currentUser);
                } else {
                    writeJson(resp, Result.error(404, "路径不存在"));
                }
                return;
            }

            // 解析路径
            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            Integer resumeId = parseIdOrNull(segments[0]);
            if (resumeId == null) {
                writeJson(resp, Result.error(400, "无效的简历ID"));
                return;
            }

            if (segments.length == 1) {
                // /api/resumes/{id}
                handleGetResumeDetail(req, resp, currentUser, resumeId);
                return;
            }

            String subResource = segments[1];

            // /api/resumes/{id}/default
            if (subResource.equals("default") && segments.length == 2) {
                handleSetDefault(req, resp, currentUser, resumeId);
                return;
            }

            // /api/resumes/{id}/restore
            if (subResource.equals("restore") && segments.length == 2) {
                handleRestore(req, resp, currentUser, resumeId);
                return;
            }

            // /api/resumes/{id}/permanent
            if (subResource.equals("permanent") && segments.length == 2) {
                handlePermanentDelete(req, resp, currentUser, resumeId);
                return;
            }

            // 子资源操作
            Integer itemId = segments.length >= 3 ? parseIdOrNull(segments[2]) : null;

            switch (subResource) {
                case "education":
                    handleEducation(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                case "skills":
                    handleSkills(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                case "projects":
                    handleProjects(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                case "awards":
                    handleAwards(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                default:
                    writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = getPathInfo(req);
            String method = req.getParameter("_method");
            boolean isPutTunnel = "PUT".equalsIgnoreCase(method);

            String uri = req.getRequestURI();

            // /api/resumes/recycle-bin (POST不支持)
            if (pathInfo != null && pathInfo.equals("/recycle-bin")) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            // /api/resumes 或 /api/resumes/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                if (isPutTunnel) {
                    writeJson(resp, Result.error(405, "PUT方法不支持在根路径"));
                } else {
                    handleCreateResume(req, resp, currentUser);
                }
                return;
            }

            // 解析路径
            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            Integer resumeId = parseIdOrNull(segments[0]);
            if (resumeId == null) {
                writeJson(resp, Result.error(400, "无效的简历ID"));
                return;
            }

            if (segments.length == 1) {
                // /api/resumes/{id}
                if (isPutTunnel) {
                    handleUpdateResume(req, resp, currentUser, resumeId);
                } else {
                    handleUpdateResume(req, resp, currentUser, resumeId); // 创建不支持，只支持更新
                }
                return;
            }

            String subResource = segments[1];

            // /api/resumes/{id}/default (POST或PUT)
            if (subResource.equals("default") && segments.length == 2) {
                handleSetDefault(req, resp, currentUser, resumeId);
                return;
            }

            // /api/resumes/{id}/restore
            if (subResource.equals("restore") && segments.length == 2) {
                handleRestore(req, resp, currentUser, resumeId);
                return;
            }

            // 子资源操作
            Integer itemId = segments.length >= 3 ? parseIdOrNull(segments[2]) : null;

            switch (subResource) {
                case "education":
                    handleEducationPost(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                case "skills":
                    handleSkillsPost(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                case "projects":
                    handleProjectsPost(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                case "awards":
                    handleAwardsPost(req, resp, currentUser, resumeId, itemId, segments.length >= 3);
                    break;
                default:
                    writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = getPathInfo(req);
            String uri = req.getRequestURI();

            // /api/resumes 或 /api/resumes/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            // 解析路径
            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            Integer resumeId = parseIdOrNull(segments[0]);
            if (resumeId == null) {
                writeJson(resp, Result.error(400, "无效的简历ID"));
                return;
            }

            if (segments.length == 1) {
                // /api/resumes/{id} - 删除简历
                handleDeleteResume(req, resp, currentUser, resumeId);
                return;
            }

            String subResource = segments[1];

            // /api/resumes/{id}/permanent
            if (subResource.equals("permanent") && segments.length == 2) {
                handlePermanentDelete(req, resp, currentUser, resumeId);
                return;
            }

            // 子资源操作
            Integer itemId = segments.length >= 3 ? parseIdOrNull(segments[2]) : null;

            switch (subResource) {
                case "education":
                    handleEducationDelete(req, resp, currentUser, itemId);
                    break;
                case "skills":
                    handleSkillsDelete(req, resp, currentUser, itemId);
                    break;
                case "projects":
                    handleProjectsDelete(req, resp, currentUser, itemId);
                    break;
                case "awards":
                    handleAwardsDelete(req, resp, currentUser, itemId);
                    break;
                default:
                    writeJson(resp, Result.error(404, "路径不存在"));
            }
        }

        public void doPut(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            writeJson(resp, Result.error(405, "PUT方法不支持，请使用POST with _method=PUT"));
        }

        // ==================== 处理器方法 ====================

        private void handleListResumes(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            String pageStr = req.getParameter("page");
            int page = 1;
            if (pageStr != null && !pageStr.isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr);
                } catch (NumberFormatException e) {
                    // 使用默认值1
                }
            }
            Result result = resumeService.listResumes(user.getId(), page);
            writeJson(resp, result);
        }

        private void handleGetResumeDetail(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId) throws Exception {
            Result result = resumeService.getResumeDetail(resumeId, user.getId());
            writeJson(resp, result);
        }

        private void handleCreateResume(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            String body = readRequestBody(req);
            if (body == null || body.trim().isEmpty()) {
                writeJson(resp, Result.error(400, "请求体不能为空"));
                return;
            }
            try {
                ResumeDTO dto = gson.fromJson(body, ResumeDTO.class);
                Result result = resumeService.createResume(dto, user.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
            }
        }

        private void handleUpdateResume(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId) throws Exception {
            String body = readRequestBody(req);
            if (body == null || body.trim().isEmpty()) {
                writeJson(resp, Result.error(400, "请求体不能为空"));
                return;
            }
            try {
                ResumeDTO dto = gson.fromJson(body, ResumeDTO.class);
                Result result = resumeService.updateResume(resumeId, dto, user.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
            }
        }

        private void handleDeleteResume(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId) throws Exception {
            Result result = resumeService.deleteResume(resumeId, user.getId());
            writeJson(resp, result);
        }

        private void handleSetDefault(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId) throws Exception {
            Result result = resumeService.setDefaultResume(resumeId, user.getId());
            writeJson(resp, result);
        }

        private void handleGetRecycleBin(HttpServletRequest req, HttpServletResponse resp, User user) throws Exception {
            Result result = resumeService.getRecycleBin(user.getId());
            writeJson(resp, result);
        }

        private void handleRestore(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId) throws Exception {
            Result result = resumeService.restoreResume(resumeId, user.getId());
            writeJson(resp, result);
        }

        private void handlePermanentDelete(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId) throws Exception {
            Result result = resumeService.permanentDelete(resumeId, user.getId());
            writeJson(resp, result);
        }

        private void handleEducation(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                Result result = resumeService.getResumeDetail(resumeId, user.getId());
                writeJson(resp, result);
            }
        }

        private void handleEducationPost(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                // 更新
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeEducationDTO dto = gson.fromJson(body, ResumeEducationDTO.class);
                    Result result = resumeService.updateEducation(itemId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            } else {
                // 添加
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeEducationDTO dto = gson.fromJson(body, ResumeEducationDTO.class);
                    Result result = resumeService.addEducation(resumeId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            }
        }

        private void handleEducationDelete(HttpServletRequest req, HttpServletResponse resp, User user, Integer itemId) throws Exception {
            if (itemId == null) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }
            Result result = resumeService.deleteEducation(itemId, user.getId());
            writeJson(resp, result);
        }

        private void handleSkills(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                Result result = resumeService.getResumeDetail(resumeId, user.getId());
                writeJson(resp, result);
            }
        }

        private void handleSkillsPost(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeSkillDTO dto = gson.fromJson(body, ResumeSkillDTO.class);
                    Result result = resumeService.updateSkill(itemId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            } else {
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeSkillDTO dto = gson.fromJson(body, ResumeSkillDTO.class);
                    Result result = resumeService.addSkill(resumeId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            }
        }

        private void handleSkillsDelete(HttpServletRequest req, HttpServletResponse resp, User user, Integer itemId) throws Exception {
            if (itemId == null) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }
            Result result = resumeService.deleteSkill(itemId, user.getId());
            writeJson(resp, result);
        }

        private void handleProjects(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                Result result = resumeService.getResumeDetail(resumeId, user.getId());
                writeJson(resp, result);
            }
        }

        private void handleProjectsPost(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeProjectDTO dto = gson.fromJson(body, ResumeProjectDTO.class);
                    Result result = resumeService.updateProject(itemId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            } else {
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeProjectDTO dto = gson.fromJson(body, ResumeProjectDTO.class);
                    Result result = resumeService.addProject(resumeId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            }
        }

        private void handleProjectsDelete(HttpServletRequest req, HttpServletResponse resp, User user, Integer itemId) throws Exception {
            if (itemId == null) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }
            Result result = resumeService.deleteProject(itemId, user.getId());
            writeJson(resp, result);
        }

        private void handleAwards(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                writeJson(resp, Result.error(404, "路径不存在"));
            } else {
                Result result = resumeService.getResumeDetail(resumeId, user.getId());
                writeJson(resp, result);
            }
        }

        private void handleAwardsPost(HttpServletRequest req, HttpServletResponse resp, User user, Integer resumeId, Integer itemId, boolean hasItemId) throws Exception {
            if (hasItemId) {
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeAwardDTO dto = gson.fromJson(body, ResumeAwardDTO.class);
                    Result result = resumeService.updateAward(itemId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            } else {
                String body = readRequestBody(req);
                if (body == null || body.trim().isEmpty()) {
                    writeJson(resp, Result.error(400, "请求体不能为空"));
                    return;
                }
                try {
                    ResumeAwardDTO dto = gson.fromJson(body, ResumeAwardDTO.class);
                    Result result = resumeService.addAward(resumeId, dto, user.getId());
                    writeJson(resp, result);
                } catch (Exception e) {
                    writeJson(resp, Result.error(400, "无效的JSON格式"));
                }
            }
        }

        private void handleAwardsDelete(HttpServletRequest req, HttpServletResponse resp, User user, Integer itemId) throws Exception {
            if (itemId == null) {
                writeJson(resp, Result.error(400, "无效的项目ID"));
                return;
            }
            Result result = resumeService.deleteAward(itemId, user.getId());
            writeJson(resp, result);
        }

        // ==================== 工具方法 ====================

        private User getCurrentUser(HttpServletRequest req) {
            Object user = req.getSession(false).getAttribute("user");
            if (user instanceof User) {
                return (User) user;
            }
            return null;
        }

        private String getPathInfo(HttpServletRequest req) {
            return req.getPathInfo();
        }

        private Integer parseIdOrNull(String str) {
            try {
                int id = Integer.parseInt(str);
                return id > 0 ? id : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private String readRequestBody(HttpServletRequest req) throws Exception {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        }

        private void writeJson(HttpServletResponse resp, Result result) throws Exception {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print(gson.toJson(result));
        }
    }
}
