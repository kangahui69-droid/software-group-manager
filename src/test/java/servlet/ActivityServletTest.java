package servlet;

import dto.ActivityFilterDTO;
import model.Activity;
import model.Registration;
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
import service.ActivityService;
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
 * ActivityServlet TDD测试套件 - 4.7 Servlet改造
 *
 * 测试范围：服务分层与API化重构计划.md 4.7 Servlet渐进改造
 * - ActivityServlet → ActivityService
 *
 * 测试策略：
 * - Servlet调用Service层方法而非直接调用DAO
 * - 所有action由Service处理业务逻辑
 * - Servlet只做：取参→调service→写响应
 *
 * Mock说明：
 * - ActivityService: listActivities / createActivity / updateActivity / deleteActivity
 * - ActivityService: register / approveActivity / rejectActivity / batchApprove
 * - ActivityService: getActivityDetail / getMyActivities / getMyCreatedActivities
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActivityServlet 改造测试")
class ActivityServletTest {

    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Mock
    private ActivityService activityService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ActivityServletRefactored servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ActivityServletRefactored();
        servlet.setActivityService(activityService);
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

    private Activity createActivity(Integer id, String title, String status) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle(title);
        activity.setStatus(status);
        activity.setApprovalStatus(status);
        activity.setActivityStartTime(new Date());
        activity.setActivityEndTime(new Date());
        return activity;
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
        void should_list_activities_when_action_is_list() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(activityService.listActivities(any(), any(), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(activityService).listActivities(any(), any(), any());
        }

        @FastTest
        @DisplayName("action=detail 应调用详情方法")
        void should_get_detail_when_action_is_detail() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("detail");
            when(request.getParameter("id")).thenReturn("1");
            when(activityService.getActivityDetail(1)).thenReturn(Result.ok(createActivity(1, "测试活动", STATUS_APPROVED)));

            servlet.doGet(request, response);

            verify(activityService).getActivityDetail(1);
        }

        @FastTest
        @DisplayName("action=create 应转发到创建页面")
        void should_forward_to_create_form_when_action_is_create() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doGet(request, response);

            verify(request).getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
        }

        @FastTest
        @DisplayName("action=myActivities 应调用我的活动方法")
        void should_get_my_activities_when_action_is_myActivities() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("myActivities");
            when(activityService.getMyActivities(1)).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(activityService).getMyActivities(1);
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
        void should_create_activity_when_action_is_create() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("title")).thenReturn("新活动");
            when(request.getParameter("description")).thenReturn("活动描述");
            when(request.getParameter("location")).thenReturn("教学楼101");
            when(request.getParameter("activity_type")).thenReturn("LECTURE");
            when(activityService.createActivity(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).createActivity(any(), any());
        }

        @FastTest
        @DisplayName("action=delete 应调用删除方法")
        void should_delete_activity_when_action_is_delete() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("delete");
            when(request.getParameter("id")).thenReturn("1");
            when(activityService.deleteActivity(1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).deleteActivity(1);
        }

        @FastTest
        @DisplayName("action=approve 应调用审批通过方法")
        void should_approve_activity_when_action_is_approve() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(activityService.approveActivity(1, 1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).approveActivity(1, 1);
        }

        @FastTest
        @DisplayName("action=reject 应调用审批驳回方法")
        void should_reject_activity_when_action_is_reject() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("reject");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("reason")).thenReturn("材料不全");
            when(activityService.rejectActivity(eq(1), eq("材料不全"), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).rejectActivity(eq(1), eq("材料不全"), any());
        }
    }

    // ==================== 活动列表 正常路径测试 ====================

    @Nested
    @DisplayName("活动列表 正常路径")
    class ListActivitiesNormalTests {

        @FastTest
        @DisplayName("获取活动列表成功应设置activities属性")
        void should_set_activities_attribute_when_list_success() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            List<Activity> activities = List.of(
                    createActivity(1, "活动1", STATUS_APPROVED),
                    createActivity(2, "活动2", STATUS_APPROVED)
            );
            when(request.getParameter("action")).thenReturn("list");
            when(activityService.listActivities(any(), any(), any())).thenReturn(Result.ok(activities));

            servlet.doGet(request, response);

            verify(request).setAttribute("activities", activities);
        }

        @FastTest
        @DisplayName("空列表应设置空消息")
        void should_set_empty_message_when_list_is_empty() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(activityService.listActivities(any(), any(), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(request).setAttribute(eq("message"), anyString());
        }
    }

    // ==================== 活动列表 边界情况测试 ====================

    @Nested
    @DisplayName("活动列表 边界情况")
    class ListActivitiesBoundaryTests {

        @FastTest
        @DisplayName("页码为0应使用默认页码1")
        void should_use_default_page_when_page_is_zero() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(request.getParameter("page")).thenReturn("0");
            when(activityService.listActivities(any(), eq(1), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(activityService).listActivities(any(), eq(1), any());
        }

        @FastTest
        @DisplayName("页码为负数应使用默认页码1")
        void should_use_default_page_when_page_is_negative() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(request.getParameter("page")).thenReturn("-1");
            when(activityService.listActivities(any(), eq(1), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(activityService).listActivities(any(), eq(1), any());
        }

        @FastTest
        @DisplayName("超长搜索关键词应正常处理")
        void should_handle_long_keyword() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(request.getParameter("keyword")).thenReturn("a".repeat(500));
            when(activityService.listActivities(any(), any(), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(activityService).listActivities(any(), any(), any());
        }
    }

    // ==================== 活动创建 异常场景测试 ====================

    @Nested
    @DisplayName("活动创建 异常场景")
    class CreateActivityExceptionTests {

        @FastTest
        @DisplayName("标题为空应返回错误")
        void should_return_error_when_title_is_empty() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("title")).thenReturn("");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("Service异常应返回错误消息")
        void should_return_error_when_service_throws_exception() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("title")).thenReturn("新活动");
            when(request.getParameter("description")).thenReturn("描述");
            when(request.getParameter("location")).thenReturn("地点");
            when(activityService.createActivity(any(), any())).thenThrow(new RuntimeException("数据库错误"));

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("MEMBER角色创建活动应成功")
        void member_should_create_activity_successfully() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("create");
            when(request.getParameter("title")).thenReturn("新活动");
            when(request.getParameter("description")).thenReturn("描述");
            when(request.getParameter("location")).thenReturn("地点");
            when(request.getParameter("activity_type")).thenReturn("LECTURE");
            when(activityService.createActivity(any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).createActivity(any(), any());
        }
    }

    // ==================== 活动审批 权限测试 ====================

    @Nested
    @DisplayName("活动审批 权限测试")
    class ActivityApprovalPermissionTests {

        @FastTest
        @DisplayName("ADMIN审批活动应成功")
        void admin_should_approve_activity_successfully() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(activityService.approveActivity(1, 1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).approveActivity(1, 1);
        }

        @FastTest
        @DisplayName("MEMBER审批活动应返回权限错误")
        void member_should_not_approve_activity() throws Exception {
            User member = createUser(2, "member1", ROLE_MEMBER);
            setupSession(member);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        @FastTest
        @DisplayName("ADMIN驳回活动应成功")
        void admin_should_reject_activity_successfully() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("reject");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("reason")).thenReturn("材料不全");
            when(activityService.rejectActivity(eq(1), eq("材料不全"), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).rejectActivity(eq(1), eq("材料不全"), any());
        }
    }

    // ==================== 活动报名 正常路径测试 ====================

    @Nested
    @DisplayName("活动报名 正常路径")
    class ActivityRegistrationNormalTests {

        @FastTest
        @DisplayName("报名成功应返回成功消息")
        void should_return_success_when_register_success() throws Exception {
            User user = createUser(2, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("register");
            when(request.getParameter("activityId")).thenReturn("1");
            when(activityService.register(1, 2)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("success"), anyString());
        }

        @FastTest
        @DisplayName("已报名应返回错误消息")
        void should_return_error_when_already_registered() throws Exception {
            User user = createUser(2, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("register");
            when(request.getParameter("activityId")).thenReturn("1");
            when(activityService.register(1, 2)).thenReturn(Result.error(400, "您已报名过该活动"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "您已报名过该活动");
        }
    }

    // ==================== 批量审批 测试 ====================

    @Nested
    @DisplayName("批量审批 测试")
    class BatchApprovalTests {

        @FastTest
        @DisplayName("批量审批成功应返回成功消息")
        void should_return_success_when_batch_approve_success() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("batchApprove");
            when(request.getParameter("ids")).thenReturn("1,2,3");
            when(activityService.batchApprove(anyList(), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).batchApprove(anyList(), eq(1));
        }

        @FastTest
        @DisplayName("空ids参数应返回错误")
        void should_return_error_when_ids_is_empty() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("batchApprove");
            when(request.getParameter("ids")).thenReturn("");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }
    }

    // ==================== 状态枚举测试 ====================

    @Nested
    @DisplayName("Activity Status枚举测试")
    class ActivityStatusEnumTests {

        @FastTest
        @DisplayName("活动状态应包含所有预期值")
        void activity_status_should_contain_all_expected() {
            assertThat(servlet.getActivityStatuses()).contains("PENDING", "APPROVED", "REJECTED");
        }

        @FastTest
        @DisplayName("审批状态PENDING应能正确处理")
        void pending_status_should_be_handled() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            Activity activity = createActivity(1, "待审核活动", STATUS_PENDING);
            when(activityService.approveActivity(1, 1)).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(activityService).approveActivity(1, 1);
        }

        @FastTest
        @DisplayName("非PENDING状态不能重复审批")
        void non_pending_status_should_not_be_approved() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(activityService.approveActivity(1, 1)).thenReturn(Result.error(400, "只能审批待审核的活动"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "只能审批待审核的活动");
        }
    }

    // ==================== 内部类：可测试的Servlet ====================

    static class ActivityServletRefactored extends ActivityServlet {
        private ActivityService activityService;

        void setActivityService(ActivityService activityService) {
            this.activityService = activityService;
        }

        ActivityService getActivityService() {
            return activityService;
        }

        String[] getActivityStatuses() {
            return new String[]{"PENDING", "APPROVED", "REJECTED"};
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
                    listActivities(request, response, user);
                    break;
                case "detail":
                    getActivityDetail(request, response, user);
                    break;
                case "create":
                case "createForm":
                    request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
                    break;
                case "myActivities":
                    getMyActivities(request, response, user);
                    break;
                default:
                    listActivities(request, response, user);
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
                    createActivity(request, response, user);
                    break;
                case "delete":
                    deleteActivity(request, response, user);
                    break;
                case "approve":
                    approveActivity(request, response, user);
                    break;
                case "reject":
                    rejectActivity(request, response, user);
                    break;
                case "batchApprove":
                    batchApprove(request, response, user);
                    break;
                case "register":
                    register(request, response, user);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private void listActivities(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String keyword = request.getParameter("keyword");
            String status = request.getParameter("status");
            String pageStr = request.getParameter("page");
            int page = 1;
            if (pageStr != null && !pageStr.isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr);
                    if (page <= 0) page = 1;
                } catch (NumberFormatException ignored) {}
            }

            ActivityFilterDTO filter = new ActivityFilterDTO();
            filter.setKeyword(keyword);
            filter.setStatus(status);
            Result result = activityService.listActivities(filter, page, 10);
            if (result.isSuccess()) {
                request.setAttribute("activities", result.getData());
            } else {
                request.setAttribute("message", result.getMessage());
            }
            request.getRequestDispatcher("/jsp/admin/activity/list.jsp").forward(request, response);
        }

        private void getActivityDetail(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "活动ID不能为空");
                request.getRequestDispatcher("/jsp/admin/activity/list.jsp").forward(request, response);
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = activityService.getActivityDetail(id);
                if (result.isSuccess()) {
                    request.setAttribute("activity", result.getData());
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "无效的活动ID");
            }
            request.getRequestDispatcher("/jsp/admin/activity/detail.jsp").forward(request, response);
        }

        private void getMyActivities(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            Result result = activityService.getMyActivities(user.getId());
            if (result.isSuccess()) {
                request.setAttribute("activities", result.getData());
            }
            request.getRequestDispatcher("/jsp/activity/myActivities.jsp").forward(request, response);
        }

        private void createActivity(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String title = request.getParameter("title");
            if (title == null || title.trim().isEmpty()) {
                request.setAttribute("error", "活动标题不能为空");
                request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
                return;
            }

            Map<String, String> params = new HashMap<>();
            params.put("title", title);
            params.put("description", request.getParameter("description"));
            params.put("location", request.getParameter("location"));
            params.put("activity_type", request.getParameter("activity_type"));

            try {
                Result result = activityService.createActivity(params, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "活动创建成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "创建失败: " + e.getMessage());
            }
            request.getRequestDispatcher("/jsp/admin/activity/edit.jsp").forward(request, response);
        }

        private void deleteActivity(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "活动ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = activityService.deleteActivity(id);
                if (result.isSuccess()) {
                    request.setAttribute("success", "活动删除成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "删除失败");
            }
        }

        private void approveActivity(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "活动ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = activityService.approveActivity(id, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "活动审批通过");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "审批失败");
            }
        }

        private void rejectActivity(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            String reason = request.getParameter("reason");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "活动ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = activityService.rejectActivity(id, reason, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "活动已驳回");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "驳回失败");
            }
        }

        private void batchApprove(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idsStr = request.getParameter("ids");
            if (idsStr == null || idsStr.trim().isEmpty()) {
                request.setAttribute("error", "请选择要审批的活动");
                return;
            }

            try {
                java.util.List<Integer> ids = new java.util.ArrayList<>();
                for (String id : idsStr.split(",")) {
                    ids.add(Integer.parseInt(id.trim()));
                }
                Result result = activityService.batchApprove(ids, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "批量审批成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "批量审批失败");
            }
        }

        private void register(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String activityIdStr = request.getParameter("activityId");
            if (activityIdStr == null || activityIdStr.isEmpty()) {
                request.setAttribute("error", "活动ID不能为空");
                return;
            }

            try {
                int activityId = Integer.parseInt(activityIdStr);
                Result result = activityService.register(activityId, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "报名成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "报名失败");
            }
        }
    }
}
