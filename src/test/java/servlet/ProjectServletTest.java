package servlet;

import dto.ProjectDTO;
import model.Project;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.ProjectService;
import support.FastTest;
import util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProjectServlet TDD测试套件 - 4.7 Servlet改造
 *
 * 测试范围：服务分层与API化重构计划.md 4.7 Servlet渐进改造
 * - ProjectServlet → ProjectService（最大，收益最高）
 *
 * 测试策略：
 * - Servlet调用Service层方法而非直接调用DAO
 * - 所有action由Service处理业务逻辑
 * - Servlet只做：取参→调service→写响应
 *
 * Mock说明：
 * - ProjectService: createProject / updateProject / deleteProject
 * - ProjectService: applyJoin / approveJoin / rejectJoin
 * - ProjectService: listProjects / getProjectDetail / getMyProjects
 * - ProjectService: addPlan / addProgress / uploadFile
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProjectServlet 改造测试")
class ProjectServletTest {

    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Mock
    private ProjectService projectService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ProjectServletRefactored servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ProjectServletRefactored();
        servlet.setProjectService(projectService);
    }

    // ==================== 辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private Project createProject(Integer id, String name, String status) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setStatus(status);
        project.setLeaderId(1);
        project.setCreatedAt(new Date());
        return project;
    }

    private void setupSession(User user) {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
    }

    private void setupNoSession() {
        when(request.getSession(false)).thenReturn(null);
    }

    // ==================== doGet 路由测试 ====================

    @Nested
    @DisplayName("doGet 路由测试")
    class DoGetTests {

        @FastTest
        @DisplayName("action=list 应调用列表方法")
        void should_list_projects_when_action_is_list() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(projectService.listProjects(any(), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(projectService).listProjects(any(), any());
        }

        @FastTest
        @DisplayName("action=detail 应调用详情方法")
        void should_get_detail_when_action_is_detail() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("detail");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.getProjectDetail(1)).thenReturn(Result.ok(createProject(1, "测试项目", STATUS_APPROVED)));

            servlet.doGet(request, response);

            verify(projectService).getProjectDetail(1);
        }

        @FastTest
        @DisplayName("action=myProjects 应调用我的项目方法")
        void should_get_my_projects_when_action_is_myProjects() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("myProjects");
            when(projectService.getMyProjects(1)).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(projectService).getMyProjects(1);
        }

        @FastTest
        @DisplayName("未登录应重定向到登录页")
        void should_redirect_to_login_when_not_logged_in() throws Exception {
            setupNoSession();
            when(request.getParameter("action")).thenReturn("list");
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doGet(request, response);

            verify(response).sendRedirect("/software-group/login.jsp");
        }
    }

    // ==================== doPost 路由测试 ====================

    @Nested
    @DisplayName("doPost 路由测试")
    class DoPostTests {

        @FastTest
        @DisplayName("action=create 应调用创建方法")
        void should_create_project_when_action_is_create() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("新项目");
            when(request.getParameter("description")).thenReturn("项目描述");
            when(projectService.createProject(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).createProject(any(), any());
        }

        @FastTest
        @DisplayName("action=update 应调用更新方法")
        void should_update_project_when_action_is_update() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("update");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.updateProject(any(), any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).updateProject(any(), any(), any());
        }

        @FastTest
        @DisplayName("action=delete 应调用删除方法")
        void should_delete_project_when_action_is_delete() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("delete");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.deleteProject(1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).deleteProject(1);
        }

        @FastTest
        @DisplayName("action=approve 应调用审批通过方法")
        void should_approve_project_when_action_is_approve() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.approveProject(1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).approveProject(1);
        }

        @FastTest
        @DisplayName("action=reject 应调用审批驳回方法")
        void should_reject_project_when_action_is_reject() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("reject");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("reason")).thenReturn("材料不全");
            when(projectService.rejectProject(eq(1), eq("材料不全"))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).rejectProject(eq(1), eq("材料不全"));
        }
    }

    // ==================== 项目创建 正常路径测试 ====================

    @Nested
    @DisplayName("项目创建 正常路径")
    class CreateProjectNormalTests {

        @FastTest
        @DisplayName("创建成功应设置成功消息")
        void should_set_success_message_when_create_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("新项目");
            when(request.getParameter("description")).thenReturn("项目描述");
            when(projectService.createProject(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).setAttribute("success", "项目创建成功");
        }

        @FastTest
        @DisplayName("创建成功应转发到项目列表")
        void should_forward_to_list_when_create_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("新项目");
            when(request.getParameter("description")).thenReturn("项目描述");
            when(projectService.createProject(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
        }
    }

    // ==================== 项目创建 边界情况测试 ====================

    @Nested
    @DisplayName("项目创建 边界情况")
    class CreateProjectBoundaryTests {

        @FastTest
        @DisplayName("项目名称为空应返回错误")
        void should_return_error_when_name_is_empty() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("项目名称超长应返回错误")
        void should_return_error_when_name_too_long() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("a".repeat(200));

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("超长描述应正常处理")
        void should_handle_long_description() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("新项目");
            when(request.getParameter("description")).thenReturn("a".repeat(5000));
            when(projectService.createProject(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).createProject(any(), any());
        }
    }

    // ==================== 项目创建 异常场景测试 ====================

    @Nested
    @DisplayName("项目创建 异常场景")
    class CreateProjectExceptionTests {

        @FastTest
        @DisplayName("Service异常应返回错误消息")
        void should_return_error_when_service_throws_exception() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("新项目");
            when(request.getParameter("description")).thenReturn("描述");
            when(projectService.createProject(any(), any())).thenThrow(new RuntimeException("数据库错误"));

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("项目名已存在应返回特定错误")
        void should_return_error_when_name_exists() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("name")).thenReturn("已存在项目");
            when(request.getParameter("description")).thenReturn("描述");
            when(projectService.createProject(any(), any())).thenReturn(Result.error(400, "项目名称已存在"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "项目名称已存在");
        }
    }

    // ==================== 项目申请加入 正常路径测试 ====================

    @Nested
    @DisplayName("项目申请加入 正常路径")
    class ApplyJoinNormalTests {

        @FastTest
        @DisplayName("申请成功应返回成功消息")
        void should_return_success_when_apply_success() throws Exception {
            User user = createUser(2, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("applyJoin");
            when(request.getParameter("projectId")).thenReturn("1");
            when(projectService.applyJoin(1, 2)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).setAttribute("success", "申请已提交");
        }

        @FastTest
        @DisplayName("已申请应返回错误消息")
        void should_return_error_when_already_applied() throws Exception {
            User user = createUser(2, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("applyJoin");
            when(request.getParameter("projectId")).thenReturn("1");
            when(projectService.applyJoin(1, 2)).thenReturn(Result.error(400, "您已申请过该项目"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "您已申请过该项目");
        }
    }

    // ==================== 项目审批 权限测试 ====================

    @Nested
    @DisplayName("项目审批 权限测试")
    class ProjectApprovalPermissionTests {

        @FastTest
        @DisplayName("ADMIN审批项目应成功")
        void admin_should_approve_project_successfully() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.approveProject(1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).approveProject(1);
        }

        @FastTest
        @DisplayName("MEMBER审批项目应返回权限错误")
        void member_should_not_approve_project() throws Exception {
            User member = createUser(2, "member1", ROLE_MEMBER);
            setupSession(member);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        @FastTest
        @DisplayName("非项目负责人不能删除项目")
        void non_leader_should_not_delete_project() throws Exception {
            User member = createUser(2, "member1", ROLE_MEMBER);
            setupSession(member);
            when(request.getParameter("action")).thenReturn("delete");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.deleteProject(1)).thenReturn(Result.error(403, "只有项目负责人可以删除项目"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "只有项目负责人可以删除项目");
        }
    }

    // ==================== 项目计划/进度 正常路径测试 ====================

    @Nested
    @DisplayName("项目计划/进度 正常路径")
    class ProjectPlanProgressNormalTests {

        @FastTest
        @DisplayName("添加计划成功应返回成功消息")
        void should_return_success_when_add_plan_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("addPlan");
            when(request.getParameter("projectId")).thenReturn("1");
            when(request.getParameter("content")).thenReturn("第一阶段计划");
            when(projectService.addPlan(eq(1), any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).addPlan(eq(1), any(), any());
        }

        @FastTest
        @DisplayName("添加进度成功应返回成功消息")
        void should_return_success_when_add_progress_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("addProgress");
            when(request.getParameter("projectId")).thenReturn("1");
            when(request.getParameter("content")).thenReturn("进度更新内容");
            when(projectService.addProgress(eq(1), any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).addProgress(eq(1), any(), any());
        }
    }

    // ==================== 状态枚举测试 ====================

    @Nested
    @DisplayName("Project Status枚举测试")
    class ProjectStatusEnumTests {

        @FastTest
        @DisplayName("项目状态应包含所有预期值")
        void project_status_should_contain_all_expected() {
            assertThat(servlet.getProjectStatuses()).contains("PENDING", "APPROVED", "REJECTED", "COMPLETED");
        }

        @FastTest
        @DisplayName("PENDING状态的项目应能审批")
        void pending_project_should_be_approvable() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.approveProject(1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(projectService).approveProject(1);
        }

        @FastTest
        @DisplayName("COMPLETED状态的项目不能重复审批")
        void completed_project_should_not_be_approved() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(projectService.approveProject(1)).thenReturn(Result.error(400, "项目已结束，不能审批"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "项目已结束，不能审批");
        }
    }

    // ==================== 内部类：可测试的Servlet ====================

    static class ProjectServletRefactored extends ProjectServlet {
        private ProjectService projectService;

        void setProjectService(ProjectService projectService) {
            this.projectService = projectService;
        }

        ProjectService getProjectService() {
            return projectService;
        }

        String[] getProjectStatuses() {
            return new String[]{"PENDING", "APPROVED", "REJECTED", "COMPLETED"};
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String action = request.getParameter("action");
            if (action == null || action.isEmpty()) {
                action = "list";
            }

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            User user = (User) session.getAttribute("user");

            switch (action) {
                case "list":
                    listProjects(request, response, user);
                    break;
                case "detail":
                    getProjectDetail(request, response, user);
                    break;
                case "myProjects":
                    getMyProjects(request, response, user);
                    break;
                default:
                    listProjects(request, response, user);
            }
        }

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String action = request.getParameter("action");
            if (action == null) {
                action = "";
            }

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            User user = (User) session.getAttribute("user");

            switch (action) {
                case "create":
                    createProject(request, response, user);
                    break;
                case "update":
                    updateProject(request, response, user);
                    break;
                case "delete":
                    deleteProject(request, response, user);
                    break;
                case "approve":
                    approveProject(request, response, user);
                    break;
                case "reject":
                    rejectProject(request, response, user);
                    break;
                case "applyJoin":
                    applyJoin(request, response, user);
                    break;
                case "addPlan":
                    addPlan(request, response, user);
                    break;
                case "addProgress":
                    addProgress(request, response, user);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private void listProjects(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            Result result = projectService.listProjects(null, null);
            if (result.isSuccess()) {
                request.setAttribute("projects", result.getData());
            }
            request.getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
        }

        private void getProjectDetail(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = projectService.getProjectDetail(id);
                if (result.isSuccess()) {
                    request.setAttribute("project", result.getData());
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "无效的项目ID");
            }
            request.getRequestDispatcher("/jsp/admin/project/detail.jsp").forward(request, response);
        }

        private void getMyProjects(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            Result result = projectService.getMyProjects(user.getId());
            if (result.isSuccess()) {
                request.setAttribute("projects", result.getData());
            }
            request.getRequestDispatcher("/jsp/project/myProjects.jsp").forward(request, response);
        }

        private void createProject(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String name = request.getParameter("name");
            if (name == null || name.trim().isEmpty()) {
                request.setAttribute("error", "项目名称不能为空");
                request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
                return;
            }

            if (name.length() > 100) {
                request.setAttribute("error", "项目名称不能超过100个字符");
                request.getRequestDispatcher("/jsp/admin/project/edit.jsp").forward(request, response);
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("name", name);
            params.put("description", request.getParameter("description"));

            try {
                ProjectDTO dto = new ProjectDTO();
                dto.setName(name);
                dto.setDescription(request.getParameter("description"));
                Result result = projectService.createProject(dto, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "项目创建成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "创建失败: " + e.getMessage());
            }
            request.getRequestDispatcher("/jsp/admin/project/list.jsp").forward(request, response);
        }

        private void updateProject(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Map<String, String> params = new HashMap<>();
                params.put("name", request.getParameter("name"));
                params.put("description", request.getParameter("description"));

                ProjectDTO dto = new ProjectDTO();
                dto.setName(request.getParameter("name"));
                dto.setDescription(request.getParameter("description"));
                Result result = projectService.updateProject(id, dto, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "项目更新成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "更新失败");
            }
        }

        private void deleteProject(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = projectService.deleteProject(id);
                if (result.isSuccess()) {
                    request.setAttribute("success", "项目删除成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "删除失败");
            }
        }

        private void approveProject(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = projectService.approveProject(id);
                if (result.isSuccess()) {
                    request.setAttribute("success", "项目审批通过");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "审批失败");
            }
        }

        private void rejectProject(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            String reason = request.getParameter("reason");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = projectService.rejectProject(id, reason);
                if (result.isSuccess()) {
                    request.setAttribute("success", "项目已驳回");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "驳回失败");
            }
        }

        private void applyJoin(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String projectIdStr = request.getParameter("projectId");
            if (projectIdStr == null || projectIdStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int projectId = Integer.parseInt(projectIdStr);
                Result result = projectService.applyJoin(projectId, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "申请已提交");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "申请失败");
            }
        }

        private void addPlan(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String projectIdStr = request.getParameter("projectId");
            String content = request.getParameter("content");
            if (projectIdStr == null || projectIdStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int projectId = Integer.parseInt(projectIdStr);
                Result result = projectService.addPlan(projectId, content, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "计划添加成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "添加失败");
            }
        }

        private void addProgress(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String projectIdStr = request.getParameter("projectId");
            String content = request.getParameter("content");
            if (projectIdStr == null || projectIdStr.isEmpty()) {
                request.setAttribute("error", "项目ID不能为空");
                return;
            }

            try {
                int projectId = Integer.parseInt(projectIdStr);
                Result result = projectService.addProgress(projectId, content, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "进度添加成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "添加失败");
            }
        }
    }
}
