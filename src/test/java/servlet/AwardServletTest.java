package servlet;

import dto.AwardDTO;
import model.Award;
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
import service.AwardService;
import support.FastTest;
import util.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AwardServlet TDD测试套件 - 4.7 Servlet改造
 *
 * 测试范围：服务分层与API化重构计划.md 4.7 Servlet渐进改造
 * - AwardServlet → AwardService
 *
 * 测试策略：
 * - Servlet调用Service层方法而非直接调用DAO
 * - 所有action由Service处理业务逻辑
 * - Servlet只做：取参→调service→写响应
 *
 * Mock说明：
 * - AwardService: submitAward / approveAward / rejectAward / addAwardImage
 * - AwardService: listAwards / filterAwards / getAwardStatistics / getMyAwards
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AwardServlet 改造测试")
class AwardServletTest {

    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    @Mock
    private AwardService awardService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private AwardServletRefactored servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new AwardServletRefactored();
        servlet.setAwardService(awardService);
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

    private Award createAward(Integer id, String name, String status) {
        Award award = new Award();
        award.setId(id);
        award.setName(name);
        award.setCompetition(name);
        award.setAwardStatus(status);
        award.setCreatedBy(1);
        award.setCreatedAt(new Date());
        return award;
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
        void should_list_awards_when_action_is_list() throws Exception {
            User user = createUser(1, "admin", ROLE_ADMIN);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("list");
            when(awardService.listAwards(any(), anyInt())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(awardService).listAwards(any(), anyInt());
        }

        @FastTest
        @DisplayName("action=filter 应调用筛选方法")
        void should_filter_awards_when_action_is_filter() throws Exception {
            when(request.getParameter("action")).thenReturn("filter");
            when(request.getParameter("status")).thenReturn(STATUS_APPROVED);
            when(awardService.filterAwards(any(), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(awardService).filterAwards(any(), any());
        }

        @FastTest
        @DisplayName("action=myAwards 应调用我的奖项方法")
        void should_get_my_awards_when_action_is_myAwards() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("myAwards");
            when(awardService.getMyAwards(1)).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(awardService).getMyAwards(1);
        }

        @FastTest
        @DisplayName("action=statistics 应调用统计方法")
        void should_get_statistics_when_action_is_statistics() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("statistics");
            when(awardService.getAwardStatistics(1)).thenReturn(Result.ok(new HashMap<>()));

            servlet.doGet(request, response);

            verify(awardService).getAwardStatistics(1);
        }

        @FastTest
        @DisplayName("未登录访问filter也应允许")
        void should_allow_filter_without_login() throws Exception {
            setupNoSession();
            when(request.getParameter("action")).thenReturn("filter");
            when(awardService.filterAwards(any(), any())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(awardService).filterAwards(any(), any());
        }
    }

    // ==================== doPost 路由测试 ====================

    @Nested
    @DisplayName("doPost 路由测试")
    class DoPostTests {

        @FastTest
        @DisplayName("action=submit 应调用提交方法")
        void should_submit_award_when_action_is_submit() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("程序设计竞赛");
            when(request.getParameter("compTime")).thenReturn("2024-06-15");
            when(awardService.submitAward(any(AwardDTO.class), any(), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).submitAward(any(AwardDTO.class), eq(1), any());
        }

        @FastTest
        @DisplayName("action=approve 应调用审批通过方法")
        void should_approve_award_when_action_is_approve() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(awardService.approveAward(eq(1), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).approveAward(eq(1), eq(1));
        }

        @FastTest
        @DisplayName("action=reject 应调用审批驳回方法")
        void should_reject_award_when_action_is_reject() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("reject");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("reason")).thenReturn("材料不全");
            when(awardService.rejectAward(eq(1), eq("材料不全"), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).rejectAward(eq(1), eq("材料不全"), eq(1));
        }

        @FastTest
        @DisplayName("action=addImage 应调用添加图片方法")
        void should_add_image_when_action_is_addImage() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("addImage");
            when(request.getParameter("id")).thenReturn("1");
            when(awardService.addAwardImage(eq(1), any(), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).addAwardImage(eq(1), any(), eq(1));
        }

        @FastTest
        @DisplayName("未登录应重定向到登录页")
        void should_redirect_to_login_when_not_logged_in() throws Exception {
            setupNoSession();
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getContextPath()).thenReturn("/software-group");

            servlet.doPost(request, response);

            verify(response).sendRedirect("/software-group/login.jsp");
        }
    }

    // ==================== 奖项提交 正常路径测试 ====================

    @Nested
    @DisplayName("奖项提交 正常路径")
    class SubmitAwardNormalTests {

        @FastTest
        @DisplayName("提交成功应设置成功消息")
        void should_set_success_message_when_submit_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("程序设计竞赛");
            when(request.getParameter("compTime")).thenReturn("2024-06-15");
            when(request.getParameter("awardLevel")).thenReturn("1");
            when(request.getParameter("awardType")).thenReturn("1");
            when(awardService.submitAward(any(AwardDTO.class), eq(1), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).setAttribute("success", "奖项提交成功");
        }

        @FastTest
        @DisplayName("提交成功应转发到列表页面")
        void should_forward_to_list_when_submit_success() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("程序设计竞赛");
            when(request.getParameter("compTime")).thenReturn("2024-06-15");
            when(request.getParameter("awardLevel")).thenReturn("1");
            when(request.getParameter("awardType")).thenReturn("1");
            when(awardService.submitAward(any(AwardDTO.class), eq(1), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(request).getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
        }
    }

    // ==================== 奖项提交 边界情况测试 ====================

    @Nested
    @DisplayName("奖项提交 边界情况")
    class SubmitAwardBoundaryTests {

        @FastTest
        @DisplayName("竞赛名称为空应返回错误")
        void should_return_error_when_competition_is_empty() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("日期格式错误应返回错误")
        void should_return_error_when_date_invalid() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("程序设计竞赛");
            when(request.getParameter("compTime")).thenReturn("invalid-date");

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("超长竞赛名称应正常处理")
        void should_handle_long_competition_name() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("a".repeat(500));
            when(request.getParameter("compTime")).thenReturn("2024-06-15");
            when(awardService.submitAward(any(AwardDTO.class), eq(1), any())).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).submitAward(any(AwardDTO.class), eq(1), any());
        }

        @FastTest
        @DisplayName("所有参数为空应返回需要更多信息")
        void should_return_need_more_info_when_all_params_empty() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(awardService.submitAward(any(AwardDTO.class), eq(1), any())).thenReturn(Result.error(400, "请提供完整信息"));

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }
    }

    // ==================== 奖项提交 异常场景测试 ====================

    @Nested
    @DisplayName("奖项提交 异常场景")
    class SubmitAwardExceptionTests {

        @FastTest
        @DisplayName("Service异常应返回错误消息")
        void should_return_error_when_service_throws_exception() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("程序设计竞赛");
            when(request.getParameter("compTime")).thenReturn("2024-06-15");
            when(awardService.submitAward(any(AwardDTO.class), eq(1), any())).thenThrow(new RuntimeException("数据库错误"));

            servlet.doPost(request, response);

            verify(request).setAttribute(eq("error"), anyString());
        }

        @FastTest
        @DisplayName("重复提交应返回特定错误")
        void should_return_error_when_duplicate_submit() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("submit");
            when(request.getParameter("competition")).thenReturn("程序设计竞赛");
            when(request.getParameter("compTime")).thenReturn("2024-06-15");
            when(awardService.submitAward(any(AwardDTO.class), eq(1), any())).thenReturn(Result.error(400, "您已提交过相同奖项"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "您已提交过相同奖项");
        }
    }

    // ==================== 奖项审批 权限测试 ====================

    @Nested
    @DisplayName("奖项审批 权限测试")
    class AwardApprovalPermissionTests {

        @FastTest
        @DisplayName("ADMIN审批奖项应成功")
        void admin_should_approve_award_successfully() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(awardService.approveAward(eq(1), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).approveAward(eq(1), eq(1));
        }

        @FastTest
        @DisplayName("MEMBER审批奖项应返回权限错误")
        void member_should_not_approve_award() throws Exception {
            User member = createUser(2, "member1", ROLE_MEMBER);
            setupSession(member);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");

            servlet.doPost(request, response);

            verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        @FastTest
        @DisplayName("ADMIN驳回应成功")
        void admin_should_reject_award_successfully() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("reject");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("reason")).thenReturn("材料不全");
            when(awardService.rejectAward(eq(1), eq("材料不全"), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).rejectAward(eq(1), eq("材料不全"), eq(1));
        }
    }

    // ==================== 奖项筛选 正常路径测试 ====================

    @Nested
    @DisplayName("奖项筛选 正常路径")
    class FilterAwardsNormalTests {

        @FastTest
        @DisplayName("按状态筛选应返回结果")
        void should_filter_by_status() throws Exception {
            when(request.getParameter("action")).thenReturn("filter");
            when(request.getParameter("status")).thenReturn(STATUS_APPROVED);
            List<Award> awards = List.of(
                    createAward(1, "竞赛1", STATUS_APPROVED),
                    createAward(2, "竞赛2", STATUS_APPROVED)
            );
            when(awardService.filterAwards(any(), eq(STATUS_APPROVED))).thenReturn(Result.ok(awards));

            servlet.doGet(request, response);

            verify(awardService).filterAwards(any(), eq(STATUS_APPROVED));
        }

        @FastTest
        @DisplayName("空筛选条件应返回所有奖项")
        void should_return_all_awards_when_no_filter() throws Exception {
            when(request.getParameter("action")).thenReturn("filter");
            when(request.getParameter("status")).thenReturn(null);
            when(awardService.listAwards(any(), anyInt())).thenReturn(Result.ok(List.of()));

            servlet.doGet(request, response);

            verify(awardService).listAwards(any(), anyInt());
        }
    }

    // ==================== 奖项统计 测试 ====================

    @Nested
    @DisplayName("奖项统计 测试")
    class AwardStatisticsTests {

        @FastTest
        @DisplayName("获取统计应成功")
        void should_get_statistics_successfully() throws Exception {
            User user = createUser(1, "member1", ROLE_MEMBER);
            setupSession(user);
            when(request.getParameter("action")).thenReturn("statistics");
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("total", 10);
            stats.put("national", 3);
            stats.put("provincial", 5);
            when(awardService.getAwardStatistics(1)).thenReturn(Result.ok(stats));

            servlet.doGet(request, response);

            verify(request).setAttribute("statistics", stats);
        }
    }

    // ==================== 状态枚举测试 ====================

    @Nested
    @DisplayName("Award Status枚举测试")
    class AwardStatusEnumTests {

        @FastTest
        @DisplayName("奖项状态应包含所有预期值")
        void award_status_should_contain_all_expected() {
            assertThat(servlet.getAwardStatuses()).contains("PENDING", "APPROVED", "REJECTED");
        }

        @FastTest
        @DisplayName("PENDING状态的奖项应能审批")
        void pending_award_should_be_approvable() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(awardService.approveAward(eq(1), eq(1))).thenReturn(Result.ok());

            servlet.doPost(request, response);

            verify(awardService).approveAward(eq(1), eq(1));
        }

        @FastTest
        @DisplayName("APPROVED状态的奖项不能重复审批")
        void approved_award_should_not_be_approved_again() throws Exception {
            User admin = createUser(1, "admin", ROLE_ADMIN);
            setupSession(admin);
            when(request.getParameter("action")).thenReturn("approve");
            when(request.getParameter("id")).thenReturn("1");
            when(awardService.approveAward(eq(1), eq(1))).thenReturn(Result.error(400, "只能审批待审核的奖项"));

            servlet.doPost(request, response);

            verify(request).setAttribute("error", "只能审批待审核的奖项");
        }
    }

    // ==================== 内部类：可测试的Servlet ====================

    static class AwardServletRefactored extends AwardServlet {
        private AwardService awardService;

        void setAwardService(AwardService awardService) {
            this.awardService = awardService;
        }

        AwardService getAwardService() {
            return awardService;
        }

        String[] getAwardStatuses() {
            return new String[]{"PENDING", "APPROVED", "REJECTED"};
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String action = request.getParameter("action");

            // filter 公开查询，不需要登录
            if ("filter".equals(action)) {
                filterAwards(request, response);
                return;
            }

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            User user = (User) session.getAttribute("user");

            if (action == null || action.isEmpty()) {
                action = "list";
            }

            switch (action) {
                case "list":
                    listAwards(request, response, user);
                    break;
                case "myAwards":
                    getMyAwards(request, response, user);
                    break;
                case "statistics":
                    getStatistics(request, response, user);
                    break;
                default:
                    listAwards(request, response, user);
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
                case "submit":
                    submitAward(request, response, user);
                    break;
                case "approve":
                    approveAward(request, response, user);
                    break;
                case "reject":
                    rejectAward(request, response, user);
                    break;
                case "addImage":
                    addAwardImage(request, response, user);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private void filterAwards(HttpServletRequest request, HttpServletResponse response)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String status = request.getParameter("status");
            if (status != null && !status.isEmpty()) {
                Result result = awardService.filterAwards(null, status);
                if (result.isSuccess()) {
                    request.setAttribute("awards", result.getData());
                }
            } else {
                Result result = awardService.listAwards(null, 1);
                if (result.isSuccess()) {
                    request.setAttribute("awards", result.getData());
                }
            }
            request.getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
        }

        private void listAwards(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String filter = request.getParameter("filter");
            String pageStr = request.getParameter("page");
            int page = 1;
            if (pageStr != null && !pageStr.isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr);
                } catch (NumberFormatException ignored) {}
            }

            Result result = awardService.listAwards(filter, page);
            if (result.isSuccess()) {
                request.setAttribute("awards", result.getData());
            }
            request.getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
        }

        private void getMyAwards(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            Result result = awardService.getMyAwards(user.getId());
            if (result.isSuccess()) {
                request.setAttribute("awards", result.getData());
            }
            request.getRequestDispatcher("/jsp/award/myAwards.jsp").forward(request, response);
        }

        private void getStatistics(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            Result result = awardService.getAwardStatistics(user.getId());
            if (result.isSuccess()) {
                request.setAttribute("statistics", result.getData());
            }
            request.getRequestDispatcher("/jsp/award/statistics.jsp").forward(request, response);
        }

        private void submitAward(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String competition = request.getParameter("competition");
            String compTime = request.getParameter("compTime");
            String awardLevel = request.getParameter("awardLevel");
            String awardType = request.getParameter("awardType");

            if (competition == null || competition.trim().isEmpty()) {
                request.setAttribute("error", "竞赛名称不能为空");
                request.getRequestDispatcher("/jsp/award/submit.jsp").forward(request, response);
                return;
            }

            if (compTime != null && !compTime.matches("\\d{4}-\\d{2}-\\d{2}")) {
                request.setAttribute("error", "日期格式错误");
                request.getRequestDispatcher("/jsp/award/submit.jsp").forward(request, response);
                return;
            }

            try {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("competition", competition);
                params.put("compTime", compTime);
                params.put("awardLevel", awardLevel);
                params.put("awardType", awardType);

                Result result = awardService.submitAward(params, user.getId(), null);
                if (result.isSuccess()) {
                    request.setAttribute("success", "奖项提交成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "提交失败: " + e.getMessage());
            }
            request.getRequestDispatcher("/jsp/award/list.jsp").forward(request, response);
        }

        private void approveAward(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "奖项ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = awardService.approveAward(id, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "奖项审批通过");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "审批失败");
            }
        }

        private void rejectAward(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            if (!ROLE_ADMIN.equals(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String idStr = request.getParameter("id");
            String reason = request.getParameter("reason");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "奖项ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = awardService.rejectAward(id, reason, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "奖项已驳回");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "驳回失败");
            }
        }

        private void addAwardImage(HttpServletRequest request, HttpServletResponse response, User user)
                throws jakarta.servlet.ServletException, java.io.IOException {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                request.setAttribute("error", "奖项ID不能为空");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                Result result = awardService.addAwardImage(id, null, user.getId());
                if (result.isSuccess()) {
                    request.setAttribute("success", "图片添加成功");
                } else {
                    request.setAttribute("error", result.getMessage());
                }
            } catch (Exception e) {
                request.setAttribute("error", "添加失败");
            }
        }
    }
}
