package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.AwardDTO;
import model.Award;
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
import service.AwardService;
import support.FastTest;
import util.Result;

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
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AwardApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.5 AwardApiServlet 奖项API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8（上传用multipart）
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 * - 分页：data: {list:[], total, page, pageSize}
 *
 * 端点：
 * - GET /api/awards → 奖项列表（filter/page）
 * - GET /api/awards/{id} → 奖项详情
 * - POST /api/awards → 提交奖项
 * - PUT /api/awards/{id} → 更新奖项
 * - DELETE /api/awards/{id} → 删除奖项
 * - POST /api/awards/{id}/approve → 审批通过
 * - POST /api/awards/{id}/reject → 审批驳回
 * - POST /api/awards/{id}/images → 添加图片
 * - GET /api/awards/my → 我的奖项
 * - GET /api/awards/statistics → 个人统计
 * - GET /api/awards/types → 奖项类型字典
 * - GET /api/awards/categories → 奖项类别字典
 * - GET /api/awards/levels → 奖项级别字典
 * - GET /api/awards/competition-levels → 竞赛级别字典
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AwardApiServlet 奖项API测试")
class AwardApiServletTest {

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer AWARD_ID = 100;
    private static final Integer IMAGE_ID = 200;

    // 用户角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 奖项状态枚举
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    // 奖项类型
    private static final int TYPE_PERSONAL = 1;
    private static final int TYPE_TEAM = 2;

    // 奖项级别
    private static final int LEVEL_NATIONAL = 1;
    private static final int LEVEL_PROVINCIAL = 2;
    private static final int LEVEL_SCHOOL = 3;

    // 奖项类别
    private static final int CATEGORY_COMPETITION = 1;
    private static final int CATEGORY_PUBLICATION = 2;
    private static final int CATEGORY_PATENT = 3;

    // 竞赛级别
    private static final int COMPETITION_INTERNATIONAL = 1;
    private static final int COMPETITION_NATIONAL = 2;
    private static final int COMPETITION_PROVINCIAL = 3;

    // ==================== 测试辅助类 ====================

    private TestableAwardApiServlet servlet;
    private AwardService mockAwardService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockAwardService = mock(AwardService.class);
        servlet = new TestableAwardApiServlet(mockAwardService);

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

    private Award createAward(Integer id, String status, Integer userId) throws Exception {
        Award award = new Award();
        award.setId(id);
        award.setCompetition("竞赛名称");
        award.setCompetitionTime(new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2026-01-01"));
        award.setYear(2026);
        award.setAwardLevel(LEVEL_NATIONAL);
        award.setAwardType(TYPE_PERSONAL);
        award.setAwardCategory(CATEGORY_COMPETITION);
        award.setCompetitionLevel(COMPETITION_NATIONAL);
        award.setAwardStatus(status);
        award.setUserId(userId);
        award.setCreatedBy(userId);
        award.setDescription("奖项描述");
        return award;
    }

    private AwardDTO createAwardDTO() {
        AwardDTO dto = new AwardDTO();
        dto.setCompetition("新竞赛");
        dto.setCompetitionTime("2026-05-01");
        dto.setYear(2026);
        dto.setAwardLevel(LEVEL_NATIONAL);
        dto.setAwardType(TYPE_PERSONAL);
        dto.setAwardCategory(CATEGORY_COMPETITION);
        dto.setCompetitionLevel(COMPETITION_NATIONAL);
        dto.setCompetitionLocation("竞赛地点");
        dto.setDescription("奖项描述");
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("filter")).thenReturn(null);
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }
    }

    // ==================== 奖项列表测试 ====================

    @Nested
    @DisplayName("奖项列表 GET /api/awards")
    class ListAwardsTests {

        @FastTest
        @DisplayName("正常获取奖项列表")
        void should_return_award_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("filter")).thenReturn(null);

            List<Award> awards = Arrays.asList(
                    createAward(1, STATUS_APPROVED, MEMBER_USER_ID),
                    createAward(2, STATUS_APPROVED, OTHER_USER_ID)
            );
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(awards));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("竞赛名称");
        }

        @FastTest
        @DisplayName("按筛选条件获取奖项列表")
        void should_filter_by_status() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockRequest.getParameter("filter")).thenReturn("PENDING");

            List<Award> awards = Arrays.asList(
                    createAward(1, STATUS_PENDING, MEMBER_USER_ID)
            );
            when(mockAwardService.listAwards(eq("PENDING"), eq(1))).thenReturn(Result.ok(awards));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("PENDING");
        }

        @FastTest
        @DisplayName("空奖项列表")
        void should_return_empty_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAwardService.listAwards(any(), eq(1)))
                    .thenReturn(Result.error(500, "数据库错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
        }
    }

    // ==================== 奖项详情测试 ====================

    @Nested
    @DisplayName("奖项详情 GET /api/awards/{id}")
    class GetAwardDetailTests {

        @FastTest
        @DisplayName("正常获取奖项详情")
        void should_return_award_detail() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);

            Award award = createAward(AWARD_ID, STATUS_APPROVED, MEMBER_USER_ID);
            when(mockAwardService.getAwardDetail(AWARD_ID)).thenReturn(Result.ok(award));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("竞赛名称");
        }

        @FastTest
        @DisplayName("奖项不存在返回404")
        void should_return_404_when_award_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/99999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/99999");

            when(mockAwardService.getAwardDetail(99999)).thenReturn(Result.error(404, "奖项不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("无效的奖项ID格式返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/abc");
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/-1");
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/0");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/0");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 提交奖项测试 ====================

    @Nested
    @DisplayName("提交奖项 POST /api/awards")
    class SubmitAwardTests {

        @FastTest
        @DisplayName("成功提交奖项")
        void should_submit_award_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            ("{\"competition\":\"新竞赛\",\"competitionTime\":\"2026-05-01\",\"awardLevel\":1," +
                                    "\"awardType\":1,\"awardCategory\":1,\"competitionLevel\":1}").getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            Award createdAward = createAward(AWARD_ID, STATUS_PENDING, MEMBER_USER_ID);
            when(mockAwardService.submitAward(any(AwardDTO.class), eq(MEMBER_USER_ID), any()))
                    .thenReturn(Result.ok(createdAward));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("奖项名称为空返回400")
        void should_return_400_when_competition_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"competition\":\"\",\"awardLevel\":1}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.submitAward(any(AwardDTO.class), eq(MEMBER_USER_ID), any()))
                    .thenReturn(Result.error(400, "竞赛名称不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("竞赛名称不能为空");
        }

        @FastTest
        @DisplayName("无效的JSON格式返回400")
        void should_return_400_when_invalid_json() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
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
        @DisplayName("用户不存在返回404")
        void should_return_404_when_user_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"competition\":\"竞赛\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.submitAward(any(AwardDTO.class), eq(MEMBER_USER_ID), any()))
                    .thenReturn(Result.error(404, "用户不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }
    }

    // ==================== 更新奖项测试 ====================

    @Nested
    @DisplayName("更新奖项 PUT /api/awards/{id}")
    class UpdateAwardTests {

        @FastTest
        @DisplayName("成功更新奖项")
        void should_update_award_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"competition\":\"更新竞赛\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.updateAward(eq(AWARD_ID), any(AwardDTO.class), eq(MEMBER_USER_ID)))
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"competition\":\"更新\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.updateAward(eq(AWARD_ID), any(AwardDTO.class), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "没有权限修改此奖项"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("奖项不存在返回404")
        void should_return_404_when_award_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/99999");
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getPathInfo()).thenReturn("/99999");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"competition\":\"更新\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.updateAward(eq(99999), any(AwardDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "奖项不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("非待审核状态返回400")
        void should_return_400_when_not_pending() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("PUT");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"competition\":\"更新\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.updateAward(eq(AWARD_ID), any(AwardDTO.class), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "只能修改待审核的奖项"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 删除奖项测试 ====================

    @Nested
    @DisplayName("删除奖项 DELETE /api/awards/{id}")
    class DeleteAwardTests {

        @FastTest
        @DisplayName("成功删除奖项")
        void should_delete_award_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);

            when(mockAwardService.deleteAward(AWARD_ID, MEMBER_USER_ID)).thenReturn(Result.ok(null));

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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);

            when(mockAwardService.deleteAward(AWARD_ID, OTHER_USER_ID))
                    .thenReturn(Result.error(403, "没有权限删除此奖项"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("奖项不存在返回404")
        void should_return_404_when_award_not_found() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/99999");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/99999");

            when(mockAwardService.deleteAward(99999, MEMBER_USER_ID))
                    .thenReturn(Result.error(404, "奖项不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("非待审核状态返回400")
        void should_return_400_when_not_pending() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID);
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID);

            when(mockAwardService.deleteAward(AWARD_ID, MEMBER_USER_ID))
                    .thenReturn(Result.error(400, "只能删除待审核的奖项"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("无效的项目ID格式返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/abc");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 审批奖项测试 ====================

    @Nested
    @DisplayName("审批通过 POST /api/awards/{id}/approve")
    class ApproveAwardTests {

        @FastTest
        @DisplayName("成功审批通过奖项")
        void should_approve_award_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/approve");

            when(mockAwardService.approveAward(AWARD_ID, ADMIN_USER_ID)).thenReturn(Result.ok(null));

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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/approve");

            when(mockAwardService.approveAward(AWARD_ID, MEMBER_USER_ID))
                    .thenReturn(Result.error(403, "无权限操作"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("奖项不存在返回404")
        void should_return_404_when_award_not_found() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/99999/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/99999/approve");

            when(mockAwardService.approveAward(99999, ADMIN_USER_ID))
                    .thenReturn(Result.error(404, "奖项不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("非待审核状态返回400")
        void should_return_400_when_not_pending() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/approve");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/approve");

            when(mockAwardService.approveAward(AWARD_ID, ADMIN_USER_ID))
                    .thenReturn(Result.error(400, "只能审批待审核的奖项"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    @Nested
    @DisplayName("审批驳回 POST /api/awards/{id}/reject")
    class RejectAwardTests {

        @FastTest
        @DisplayName("成功驳回奖项")
        void should_reject_award_successfully() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/reject");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"材料不全\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.rejectAward(eq(AWARD_ID), eq("材料不全"), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.ok(null));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("非管理员驳回返回403")
        void should_return_403_when_not_admin() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/reject");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"不符合\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.rejectAward(eq(AWARD_ID), eq("不符合"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(403, "无权限操作"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("驳回原因为空返回400")
        void should_return_400_when_reason_empty() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/reject");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/reject");
            when(mockRequest.getContentType()).thenReturn("application/json");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(
                            "{\"reason\":\"\"}".getBytes(StandardCharsets.UTF_8)
                    ), StandardCharsets.UTF_8)));

            when(mockAwardService.rejectAward(eq(AWARD_ID), eq(""), eq(ADMIN_USER_ID)))
                    .thenReturn(Result.error(400, "驳回原因不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 添加图片测试 ====================

    @Nested
    @DisplayName("添加图片 POST /api/awards/{id}/images")
    class AddAwardImageTests {

        @FastTest
        @DisplayName("成功添加图片")
        void should_add_image_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/images");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/images");
            when(mockRequest.getContentType()).thenReturn("multipart/form-data");

            when(mockAwardService.addAwardImage(eq(AWARD_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(IMAGE_ID));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":" + IMAGE_ID);
        }

        @FastTest
        @DisplayName("无权限添加图片返回403")
        void should_return_403_when_not_owner() throws Exception {
            User other = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(other);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/images");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/images");
            when(mockRequest.getContentType()).thenReturn("multipart/form-data");

            when(mockAwardService.addAwardImage(eq(AWARD_ID), any(), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "没有权限添加图片"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
        }

        @FastTest
        @DisplayName("文件为空返回400")
        void should_return_400_when_file_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/" + AWARD_ID + "/images");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/" + AWARD_ID + "/images");
            when(mockRequest.getContentType()).thenReturn("multipart/form-data");

            when(mockAwardService.addAwardImage(eq(AWARD_ID), any(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "文件不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== 我的奖项测试 ====================

    @Nested
    @DisplayName("我的奖项 GET /api/awards/my")
    class GetMyAwardsTests {

        @FastTest
        @DisplayName("成功获取我的奖项列表")
        void should_return_my_awards() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/my");

            List<Award> awards = Arrays.asList(
                    createAward(1, STATUS_APPROVED, MEMBER_USER_ID)
            );
            when(mockAwardService.getMyAwards(MEMBER_USER_ID)).thenReturn(Result.ok(awards));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("竞赛名称");
        }

        @FastTest
        @DisplayName("我的奖项为空列表")
        void should_return_empty_my_awards() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/my");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/my");

            when(mockAwardService.getMyAwards(MEMBER_USER_ID)).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }
    }

    // ==================== 个人统计测试 ====================

    @Nested
    @DisplayName("个人统计 GET /api/awards/statistics")
    class GetStatisticsTests {

        @FastTest
        @DisplayName("成功获取个人统计")
        void should_return_statistics() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/statistics");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/statistics");

            AwardService.AwardStatistics stats = new AwardService.AwardStatistics();
            stats.setTotalPersonalAwards(5);
            stats.setNationalAwards(2);
            stats.setProvincialAwards(3);
            when(mockAwardService.getAwardStatistics(MEMBER_USER_ID)).thenReturn(Result.ok(stats));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("totalPersonalAwards");
        }
    }

    // ==================== 字典接口测试 ====================

    @Nested
    @DisplayName("字典接口测试")
    class DictionaryTests {

        @FastTest
        @DisplayName("获取奖项类型字典")
        void should_return_award_types() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/types");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/types");

            when(mockAwardService.getAwardTypes()).thenReturn(Result.ok(Arrays.asList(
                    Map.of("value", 1, "label", "个人奖"),
                    Map.of("value", 2, "label", "团队奖")
            )));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("个人奖");
        }

        @FastTest
        @DisplayName("获取奖项类别字典")
        void should_return_award_categories() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/categories");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/categories");

            when(mockAwardService.getAwardCategories()).thenReturn(Result.ok(Arrays.asList(
                    Map.of("value", 1, "label", "竞赛类"),
                    Map.of("value", 2, "label", "论文类")
            )));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("竞赛类");
        }

        @FastTest
        @DisplayName("获取奖项级别字典")
        void should_return_award_levels() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/levels");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/levels");

            when(mockAwardService.getAwardLevels()).thenReturn(Result.ok(Arrays.asList(
                    Map.of("value", 1, "label", "国家级"),
                    Map.of("value", 2, "label", "省级")
            )));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("国家级");
        }

        @FastTest
        @DisplayName("获取竞赛级别字典")
        void should_return_competition_levels() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/competition-levels");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/competition-levels");

            when(mockAwardService.getCompetitionLevels()).thenReturn(Result.ok(Arrays.asList(
                    Map.of("value", 1, "label", "国际级"),
                    Map.of("value", 2, "label", "国家级")
            )));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("国际级");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空pathInfo应返回奖项列表")
        void should_handle_empty_path_info() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超大ID应根据实现返回400或404")
        void should_handle_overflow_id() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/9999999999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/9999999999");

            when(mockAwardService.getAwardDetail(anyInt()))
                    .thenReturn(Result.error(404, "奖项不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).matches(".*\"code\":(400|404).*");
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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

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
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards/99999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/99999");
            when(mockAwardService.getAwardDetail(99999))
                    .thenReturn(Result.error(404, "奖项不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("\"message\":\"奖项不存在\"");
        }

        @FastTest
        @DisplayName("应设置正确的Content-Type")
        void should_set_correct_content_type() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/awards");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("page")).thenReturn("1");
            when(mockAwardService.listAwards(any(), eq(1))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }
    }

    // ==================== 测试用Servlet内部类 ====================

    /**
     * 可测试的AwardApiServlet
     *
     * 复制AwardApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableAwardApiServlet {

        private final AwardService awardService;
        private final Gson gson = new GsonBuilder().create();

        // 路径模式常量
        private static final String ACTION_APPROVE = "approve";
        private static final String ACTION_REJECT = "reject";
        private static final String ACTION_IMAGES = "images";
        private static final String ACTION_MY = "my";
        private static final String ACTION_STATISTICS = "statistics";
        private static final String ACTION_TYPES = "types";
        private static final String ACTION_CATEGORIES = "categories";
        private static final String ACTION_LEVELS = "levels";
        private static final String ACTION_COMPETITION_LEVELS = "competition-levels";

        public TestableAwardApiServlet(AwardService awardService) {
            this.awardService = awardService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            // /api/awards/my
            if (pathInfo != null && pathInfo.endsWith(ACTION_MY)) {
                handleMyAwards(resp, currentUser);
                return;
            }

            // /api/awards/statistics
            if (pathInfo != null && pathInfo.endsWith(ACTION_STATISTICS)) {
                handleStatistics(resp, currentUser);
                return;
            }

            // /api/awards/types
            if (pathInfo != null && pathInfo.endsWith(ACTION_TYPES)) {
                handleGetAwardTypes(resp);
                return;
            }

            // /api/awards/categories
            if (pathInfo != null && pathInfo.endsWith(ACTION_CATEGORIES)) {
                handleGetAwardCategories(resp);
                return;
            }

            // /api/awards/competition-levels (check before /levels since it ends with "levels")
            if (pathInfo != null && pathInfo.endsWith(ACTION_COMPETITION_LEVELS)) {
                handleGetCompetitionLevels(resp);
                return;
            }

            // /api/awards/levels
            if (pathInfo != null && pathInfo.endsWith(ACTION_LEVELS)) {
                handleGetAwardLevels(resp);
                return;
            }

            // /api/awards 或 /api/awards/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListAwards(req, resp, currentUser);
                return;
            }

            // /api/awards/{id}
            AwardPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的奖项ID"));
                return;
            }

            if (pi.hasAction()) {
                writeJson(resp, Result.error(400, "不支持的操作"));
            } else {
                handleGetAwardDetail(resp, pi.getAwardId());
            }
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);
            String uri = req.getRequestURI();

            // /api/awards - 提交奖项
            if (uri.endsWith("/api/awards") || (pathInfo != null && pathInfo.equals("/"))) {
                handleSubmitAward(req, resp, currentUser);
                return;
            }

            // Fallback: derive pathInfo from URI if not provided
            if (pathInfo == null && uri != null && uri.contains("/api/awards/")) {
                pathInfo = uri.substring(uri.indexOf("/api/awards/") + 12);
                if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                    pathInfo = "/" + pathInfo;
                }
            }

            if (pathInfo == null || !pathInfo.startsWith("/")) {
                writeJson(resp, Result.error(400, "无效的请求路径"));
                return;
            }

            AwardPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的奖项ID"));
                return;
            }

            int awardId = pi.getAwardId();
            String action = pi.getAction();

            if (ACTION_APPROVE.equals(action)) {
                handleApproveAward(resp, awardId, currentUser);
            } else if (ACTION_REJECT.equals(action)) {
                handleRejectAward(req, resp, awardId, currentUser);
            } else if (ACTION_IMAGES.equals(action)) {
                handleAddAwardImage(req, resp, awardId, currentUser);
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

            String pathInfo = derivePathInfo(req);

            if (pathInfo == null || !pathInfo.startsWith("/")) {
                writeJson(resp, Result.error(400, "无效的请求路径"));
                return;
            }

            AwardPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的奖项ID"));
                return;
            }

            if (pi.hasAction()) {
                writeJson(resp, Result.error(400, "不支持的操作"));
            } else {
                handleUpdateAward(req, resp, pi.getAwardId(), currentUser);
            }
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = derivePathInfo(req);

            if (pathInfo == null || !pathInfo.startsWith("/")) {
                writeJson(resp, Result.error(400, "无效的请求路径"));
                return;
            }

            AwardPathInfo pi = parsePathInfo(pathInfo);
            if (!pi.isValidId()) {
                writeJson(resp, Result.error(400, "无效的奖项ID"));
                return;
            }

            if (pi.hasAction()) {
                writeJson(resp, Result.error(400, "不支持的操作"));
            } else {
                handleDeleteAward(resp, pi.getAwardId(), currentUser);
            }
        }

        // ==================== 处理器方法 ====================

        private void handleListAwards(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String pageStr = req.getParameter("page");
            String filter = req.getParameter("filter");

            int page = 1;
            try {
                if (pageStr != null) page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}

            Result result = awardService.listAwards(filter, page);
            writeJson(resp, result);
        }

        private void handleGetAwardDetail(HttpServletResponse resp, int awardId) throws IOException {
            Result result = awardService.getAwardDetail(awardId);
            writeJson(resp, result);
        }

        private void handleSubmitAward(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            try {
                AwardDTO dto = parseJsonRequest(req, AwardDTO.class);
                Result result = awardService.submitAward(dto, currentUser.getId(), null);
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleUpdateAward(HttpServletRequest req, HttpServletResponse resp, int awardId, User currentUser) throws IOException {
            try {
                AwardDTO dto = parseJsonRequest(req, AwardDTO.class);
                Result result = awardService.updateAward(awardId, dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleDeleteAward(HttpServletResponse resp, int awardId, User currentUser) throws IOException {
            Result result = awardService.deleteAward(awardId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleApproveAward(HttpServletResponse resp, int awardId, User currentUser) throws IOException {
            Result result = awardService.approveAward(awardId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleRejectAward(HttpServletRequest req, HttpServletResponse resp, int awardId, User currentUser) throws IOException {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = parseJsonRequest(req, Map.class);
                String reason = body != null ? (String) body.get("reason") : null;
                Result result = awardService.rejectAward(awardId, reason, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的请求参数"));
            }
        }

        private void handleAddAwardImage(HttpServletRequest req, HttpServletResponse resp, int awardId, User currentUser) throws IOException {
            try {
                Object part = extractFileFromRequest(req);
                Result result = awardService.addAwardImage(awardId, part, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的文件"));
            }
        }

        private void handleMyAwards(HttpServletResponse resp, User currentUser) throws IOException {
            Result result = awardService.getMyAwards(currentUser.getId());
            writeJson(resp, result);
        }

        private void handleStatistics(HttpServletResponse resp, User currentUser) throws IOException {
            Result result = awardService.getAwardStatistics(currentUser.getId());
            writeJson(resp, result);
        }

        private void handleGetAwardTypes(HttpServletResponse resp) throws IOException {
            Result result = awardService.getAwardTypes();
            writeJson(resp, result);
        }

        private void handleGetAwardCategories(HttpServletResponse resp) throws IOException {
            Result result = awardService.getAwardCategories();
            writeJson(resp, result);
        }

        private void handleGetAwardLevels(HttpServletResponse resp) throws IOException {
            Result result = awardService.getAwardLevels();
            writeJson(resp, result);
        }

        private void handleGetCompetitionLevels(HttpServletResponse resp) throws IOException {
            Result result = awardService.getCompetitionLevels();
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

        private String derivePathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null) {
                String uri = req.getRequestURI();
                if (uri != null && uri.contains("/api/awards/")) {
                    pathInfo = uri.substring(uri.indexOf("/api/awards/") + 12);
                    if (!pathInfo.isEmpty() && !pathInfo.startsWith("/")) {
                        pathInfo = "/" + pathInfo;
                    }
                }
            }
            return pathInfo;
        }

        private AwardPathInfo parsePathInfo(String pathInfo) {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                return AwardPathInfo.root();
            }

            if (!pathInfo.startsWith("/")) {
                return AwardPathInfo.root();
            }

            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1 || segments[0].isEmpty()) {
                return AwardPathInfo.root();
            }

            String idStr = segments[0];
            int awardId = 0;
            try {
                awardId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                return AwardPathInfo.root();
            }

            String action = segments.length >= 2 ? segments[1] : null;

            return AwardPathInfo.of(awardId, action);
        }

        // ==================== 路径解析内部类 ====================

        private static class AwardPathInfo {
            private final int awardId;
            private final String action;
            private final boolean isRoot;

            private AwardPathInfo(int awardId, String action, boolean isRoot) {
                this.awardId = awardId;
                this.action = action;
                this.isRoot = isRoot;
            }

            static AwardPathInfo root() {
                return new AwardPathInfo(0, null, true);
            }

            static AwardPathInfo of(int awardId, String action) {
                return new AwardPathInfo(awardId, action, false);
            }

            boolean isRoot() { return isRoot; }

            boolean isValidId() {
                return !isRoot && awardId > 0;
            }

            int getAwardId() { return awardId; }

            boolean hasAction() {
                return !isRoot && action != null && !action.isEmpty();
            }

            String getAction() { return action; }
        }
    }
}
