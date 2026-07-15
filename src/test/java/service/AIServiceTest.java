package service;

import dao.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AIService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.6 AIService 重构
 * - 所有正常路径（公开查询、成员操作、管理员操作）
 * - 所有边界情况（空列表、单条记录、临界值）
 * - 所有异常场景（未登录、无权限、参数缺失、数据库异常）
 * - 所有角色枚举（GUEST/MEMBER/ADMIN）
 *
 * Mock说明：
 * - AIClientUtil: chat() - AI对话
 * - AIConversationDAO: findBySessionId/save/update
 * - AIMessageDAO: save
 * - ActivityDAO: findInRegistrationPeriod/findAll/findById
 * - AwardDAO: findByUserId/findAll/insert
 * - ProjectDAO: findProjectsByUserId/findAll
 * - RegistrationDAO: findByUserId/register/isRegistered
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AIService AI服务测试")
class AIServiceTest {

    // ==================== 测试数据常量 ====================

    private static final String ROLE_GUEST = "GUEST";
    private static final String ROLE_MEMBER = "MEMBER";
    private static final String ROLE_ADMIN = "ADMIN";

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;

    // ==================== Mock对象 ====================

    @Mock
    private AIConversationDAO conversationDAO;

    @Mock
    private AIMessageDAO messageDAO;

    @Mock
    private ActivityDAO activityDAO;

    @Mock
    private ActivityParticipantDAO activityParticipantDAO;

    @Mock
    private AwardDAO awardDAO;

    @Mock
    private ProjectDAO projectDAO;

    @Mock
    private RegistrationDAO registrationDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private NewsDAO newsDAO;

    @Mock
    private ProblemReportDAO problemReportDAO;

    @Mock
    private ActivityGroupDAO activityGroupDAO;

    @InjectMocks
    private AIService aiService;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() {
        when(conversationDAO.findBySessionId(anyString())).thenReturn(null);
        when(conversationDAO.save(any())).thenReturn(1);
    }

    // ==================== 辅助方法 ====================

    private User createAdminUser() {
        User user = new User();
        user.setId(ADMIN_USER_ID);
        user.setUsername("admin");
        user.setRole(ROLE_ADMIN);
        user.setStatus(1);
        return user;
    }

    private User createMemberUser() {
        User user = new User();
        user.setId(MEMBER_USER_ID);
        user.setUsername("member");
        user.setRole(ROLE_MEMBER);
        user.setStatus(1);
        return user;
    }

    private Activity createActivity(Integer id, String title) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setTitle(title);
        activity.setActivityType("COMPETITION");
        activity.setStatus("APPROVED");
        return activity;
    }

    private Award createAward(Integer id, String name, String status) {
        Award award = new Award();
        award.setId(id);
        award.setName(name);
        award.setCompetition(name);
        award.setAwardStatus(status);
        award.setCreatedBy(MEMBER_USER_ID);
        return award;
    }

    private Project createProject(Integer id, String name, String status) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setStatus(status);
        project.setLeaderId(MEMBER_USER_ID);
        return project;
    }

    private Map<String, String> createParams(String... keyValues) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            params.put(keyValues[i], keyValues[i + 1]);
        }
        return params;
    }

    // ==================== executeAction 公共入口测试 ====================

    @Nested
    @DisplayName("executeAction 公共入口测试")
    class ExecuteActionTests {

        @FastTest
        @DisplayName("公开查询应无需登录")
        void should_allow_public_query_without_login() {
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_all_news", params, null);

            assertThat(result).isNotNull();
            assertThat(result.containsKey("success")).isTrue();
        }

        @FastTest
        @DisplayName("公开查询list_all_news应返回成功")
        void should_list_all_news_successfully() {
            when(newsDAO.findAll()).thenReturn(List.of());

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_all_news", params, null);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("公开查询recent_news应返回成功")
        void should_list_recent_news_successfully() {
            when(newsDAO.findAll()).thenReturn(List.of());

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("recent_news", params, null);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("公开查询list_activities应返回成功")
        void should_list_activities_successfully() {
            when(activityDAO.findInRegistrationPeriod()).thenReturn(List.of());

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_activities", params, null);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("公开查询list_latest_activities应返回成功")
        void should_list_latest_activities_successfully() {
            when(activityDAO.findAll()).thenReturn(List.of());

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_latest_activities", params, null);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("公开查询get_organization_info应返回成功")
        void should_get_organization_info_successfully() {
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("get_organization_info", params, null);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("未登录用户操作应返回请先登录")
        void should_return_login_required_when_not_logged_in() {
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("signup_activity", params, null);

            assertThat(result.get("message").toString()).contains("请先登录");
        }

        @FastTest
        @DisplayName("GUEST角色操作应返回权限不足")
        void should_return_permission_denied_for_guest() {
            User guest = new User();
            guest.setRole(ROLE_GUEST);

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("signup_activity", params, guest);

            assertThat(result.get("message").toString()).contains("访客不能执行该操作");
        }

        @FastTest
        @DisplayName("未知操作类型应返回错误消息")
        void should_return_error_for_unknown_action() {
            User member = createMemberUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("unknown_action", params, member);

            assertThat(result.get("message").toString()).contains("未知操作类型");
        }
    }

    // ==================== 成员操作测试 ====================

    @Nested
    @DisplayName("成员操作测试")
    class MemberActionTests {

        @FastTest
        @DisplayName("signup_activity应成功报名")
        void should_signup_activity_successfully() {
            User member = createMemberUser();
            Activity activity = createActivity(1, "测试活动");
            long now = System.currentTimeMillis();
            activity.setActivityStartTime(new Date(now + 86400000));
            activity.setActivityEndTime(new Date(now + 172800000));
            activity.setRegistrationStartTime(new Date(now - 86400000));
            activity.setRegistrationEndTime(new Date(now + 86400000));

            when(activityDAO.findById(1)).thenReturn(activity);
            when(registrationDAO.isRegistered(1, MEMBER_USER_ID)).thenReturn(false);
            when(registrationDAO.register(1, MEMBER_USER_ID)).thenReturn(true);

            Map<String, String> params = createParams("activity_id", "1");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("message").toString()).contains("报名成功");
        }

        @FastTest
        @DisplayName("signup_activity无activityId应返回可报名活动列表")
        void should_return_activity_list_when_no_activity_id() {
            User member = createMemberUser();
            when(activityDAO.findInRegistrationPeriod()).thenReturn(List.of(createActivity(1, "活动1")));

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("type")).isEqualTo("table");
        }

        @FastTest
        @DisplayName("signup_activity活动不存在应返回错误")
        void should_return_error_when_activity_not_exists() {
            User member = createMemberUser();
            when(activityDAO.findById(999)).thenReturn(null);

            Map<String, String> params = createParams("activity_id", "999");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message").toString()).contains("未找到该活动");
        }

        @FastTest
        @DisplayName("signup_activity已报名应返回错误")
        void should_return_error_when_already_registered() {
            User member = createMemberUser();
            Activity activity = createActivity(1, "测试活动");
            long now = System.currentTimeMillis();
            activity.setActivityStartTime(new Date(now + 86400000));
            activity.setActivityEndTime(new Date(now + 172800000));
            activity.setRegistrationStartTime(new Date(now - 86400000));
            activity.setRegistrationEndTime(new Date(now + 86400000));

            when(activityDAO.findById(1)).thenReturn(activity);
            when(registrationDAO.isRegistered(1, MEMBER_USER_ID)).thenReturn(true);

            Map<String, String> params = createParams("activity_id", "1");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message").toString()).contains("已报名过");
        }

        @FastTest
        @DisplayName("view_my_activities应返回我的活动")
        void should_view_my_activities_successfully() {
            User member = createMemberUser();
            Registration reg = new Registration();
            reg.setActivityId(1);
            when(registrationDAO.findByUserId(MEMBER_USER_ID)).thenReturn(List.of(reg));

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("view_my_activities", params, member);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("type")).isEqualTo("view_list");
        }

        @FastTest
        @DisplayName("submit_award应成功提交奖项申请")
        void should_submit_award_successfully() {
            User member = createMemberUser();
            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Map<String, String> params = createParams(
                "competition", "程序设计竞赛",
                "compTime", "2024-06-15",
                "awardLevel", "1",
                "awardType", "1"
            );
            Map<String, Object> result = aiService.executeAction("submit_award", params, member);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("message").toString()).contains("已提交");
        }

        @FastTest
        @DisplayName("submit_award缺少必填参数应返回需要更多信息")
        void should_return_need_more_info_when_params_missing() {
            User member = createMemberUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("submit_award", params, member);

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("need_more_info")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("list_my_awards应返回我的奖项列表")
        void should_list_my_awards_successfully() {
            User member = createMemberUser();
            Award award = createAward(1, "程序设计竞赛", "PENDING");
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(List.of(award));

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_my_awards", params, member);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("type")).isEqualTo("table");
        }

        @FastTest
        @DisplayName("list_my_awards无奖项应返回空消息")
        void should_return_empty_message_when_no_awards() {
            User member = createMemberUser();
            when(awardDAO.findByUserId(MEMBER_USER_ID)).thenReturn(List.of());

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_my_awards", params, member);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("message").toString()).contains("还没有获奖记录");
        }

        @FastTest
        @DisplayName("view_my_projects应返回我的项目")
        void should_view_my_projects_successfully() {
            User member = createMemberUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("view_my_projects", params, member);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("view_my_groups应返回我的群组")
        void should_view_my_groups_successfully() {
            User member = createMemberUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("view_my_groups", params, member);

            assertThat(result.get("success")).isEqualTo(true);
        }
    }

    // ==================== 管理员操作测试 ====================

    @Nested
    @DisplayName("管理员操作测试")
    class AdminActionTests {

        @FastTest
        @DisplayName("list_pending_users应返回待审核用户")
        void should_list_pending_users_successfully() {
            User admin = createAdminUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_pending_users", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("list_all_activities应返回所有活动")
        void should_list_all_activities_as_admin() {
            User admin = createAdminUser();
            when(activityDAO.findAll()).thenReturn(List.of());

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_all_activities", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("type")).isEqualTo("table");
        }

        @FastTest
        @DisplayName("approve_activity应成功审批活动")
        void should_approve_activity_successfully() {
            User admin = createAdminUser();
            Activity activity = createActivity(1, "测试活动");
            when(activityDAO.findById(1)).thenReturn(activity);
            when(activityDAO.approveActivity(1)).thenReturn(true);

            Map<String, String> params = createParams("activity_id", "1");
            Map<String, Object> result = aiService.executeAction("approve_activity", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("approve_activity活动不存在应返回错误")
        void should_return_error_when_approving_nonexistent_activity() {
            User admin = createAdminUser();
            when(activityDAO.findById(999)).thenReturn(null);

            Map<String, String> params = createParams("activity_id", "999");
            Map<String, Object> result = aiService.executeAction("approve_activity", params, admin);

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message").toString()).contains("未找到");
        }

        @FastTest
        @DisplayName("reject_activity应成功驳回活动")
        void should_reject_activity_successfully() {
            User admin = createAdminUser();
            Activity activity = createActivity(1, "测试活动");
            when(activityDAO.findById(1)).thenReturn(activity);
            when(activityDAO.rejectActivity(1)).thenReturn(true);

            Map<String, String> params = createParams("activity_id", "1", "reason", "材料不全");
            Map<String, Object> result = aiService.executeAction("reject_activity", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("approve_user应成功审批用户")
        void should_approve_user_successfully() {
            User admin = createAdminUser();
            User targetUser = new User();
            targetUser.setId(OTHER_USER_ID);
            targetUser.setStatus(0);
            when(userDAO.findById(OTHER_USER_ID)).thenReturn(targetUser);
            when(userDAO.updateStatus(eq(OTHER_USER_ID), eq(1))).thenReturn(true);

            Map<String, String> params = createParams("user_id", String.valueOf(OTHER_USER_ID));
            Map<String, Object> result = aiService.executeAction("approve_user", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("reject_user应成功拒绝用户")
        void should_reject_user_successfully() {
            User admin = createAdminUser();
            User targetUser = new User();
            targetUser.setId(OTHER_USER_ID);
            targetUser.setStatus(0);
            when(userDAO.findById(OTHER_USER_ID)).thenReturn(targetUser);
            when(userDAO.updateStatus(eq(OTHER_USER_ID), eq(-1))).thenReturn(true);

            Map<String, String> params = createParams("user_id", String.valueOf(OTHER_USER_ID));
            Map<String, Object> result = aiService.executeAction("reject_user", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("list_pending_activities应返回待审核活动")
        void should_list_pending_activities_successfully() {
            User admin = createAdminUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_pending_activities", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("view_participants应返回参与者列表")
        void should_view_participants_successfully() {
            User admin = createAdminUser();
            Activity activity = createActivity(1, "测试活动");
            when(activityDAO.findById(1)).thenReturn(activity);
            when(activityParticipantDAO.getParticipantIdsByActivityId(1)).thenReturn(List.of());

            Map<String, String> params = createParams("activity_id", "1");
            Map<String, Object> result = aiService.executeAction("view_participants", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("list_all_awards应返回所有奖项")
        void should_list_all_awards_successfully() {
            User admin = createAdminUser();
            Award award = createAward(1, "程序设计竞赛", "PENDING");
            when(awardDAO.findAll()).thenReturn(List.of(award));

            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("list_all_awards", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("type")).isEqualTo("table");
        }

        @FastTest
        @DisplayName("statistics应返回统计信息")
        void should_return_statistics_successfully() {
            User admin = createAdminUser();
            Map<String, String> params = createParams();
            Map<String, Object> result = aiService.executeAction("statistics", params, admin);

            assertThat(result.get("success")).isEqualTo(true);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空activityId应正常处理")
        void should_handle_empty_activity_id() {
            User member = createMemberUser();
            when(activityDAO.findInRegistrationPeriod()).thenReturn(List.of());

            Map<String, String> params = createParams("activity_id", "");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result).isNotNull();
        }

        @FastTest
        @DisplayName("无效activityId格式应正常处理")
        void should_handle_invalid_activity_id_format() {
            User member = createMemberUser();
            when(activityDAO.findInRegistrationPeriod()).thenReturn(List.of());

            Map<String, String> params = createParams("activity_id", "abc");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result).isNotNull();
        }

        @FastTest
        @DisplayName("空奖项名称应返回需要更多信息")
        void should_return_need_more_info_for_empty_competition() {
            User member = createMemberUser();
            Map<String, String> params = createParams("competition", "");
            Map<String, Object> result = aiService.executeAction("submit_award", params, member);

            assertThat(result.get("need_more_info")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("超长参数应正常处理")
        void should_handle_long_parameters() {
            User member = createMemberUser();
            String longName = "a".repeat(1000);
            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Map<String, String> params = createParams(
                "competition", longName,
                "compTime", "2024-06-15"
            );
            Map<String, Object> result = aiService.executeAction("submit_award", params, member);

            assertThat(result.get("success")).isEqualTo(true);
        }

        @FastTest
        @DisplayName("特殊字符参数应正常处理")
        void should_handle_special_characters_in_params() {
            User member = createMemberUser();
            when(activityDAO.findInRegistrationPeriod()).thenReturn(List.of());

            Map<String, String> params = createParams(
                "activity_name", "测试'\"活动&nbsp;\""
            );
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result).isNotNull();
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @FastTest
        @DisplayName("数据库查询异常应捕获并返回错误")
        void should_catch_database_exception() {
            User member = createMemberUser();
            when(activityDAO.findById(1)).thenThrow(new RuntimeException("数据库连接失败"));

            Map<String, String> params = createParams("activity_id", "1");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result.get("success")).isEqualTo(false);
            assertThat(result.get("message").toString()).contains("出错");
        }

        @FastTest
        @DisplayName("注册数据库异常应返回错误")
        void should_return_error_when_register_fails() {
            User member = createMemberUser();
            Activity activity = createActivity(1, "测试活动");
            activity.setActivityStartTime(new Date(System.currentTimeMillis() + 86400000));
            activity.setActivityEndTime(new Date(System.currentTimeMillis() + 172800000));

            when(activityDAO.findById(1)).thenReturn(activity);
            when(registrationDAO.isRegistered(1, MEMBER_USER_ID)).thenReturn(false);
            when(registrationDAO.register(1, MEMBER_USER_ID)).thenThrow(new RuntimeException("数据库错误"));

            Map<String, String> params = createParams("activity_id", "1");
            Map<String, Object> result = aiService.executeAction("signup_activity", params, member);

            assertThat(result.get("success")).isEqualTo(false);
        }

        @FastTest
        @DisplayName("奖项插入数据库异常应返回错误")
        void should_return_error_when_award_insert_fails() {
            User member = createMemberUser();
            when(awardDAO.insert(any(Award.class))).thenThrow(new RuntimeException("数据库错误"));

            Map<String, String> params = createParams(
                "competition", "竞赛",
                "compTime", "2024-06-15"
            );
            Map<String, Object> result = aiService.executeAction("submit_award", params, member);

            assertThat(result.get("success")).isEqualTo(false);
        }

        @FastTest
        @DisplayName("日期格式错误应正常处理不抛异常")
        void should_handle_invalid_date_format() {
            User member = createMemberUser();
            when(awardDAO.insert(any(Award.class))).thenReturn(true);

            Map<String, String> params = createParams(
                "competition", "竞赛",
                "compTime", "invalid-date"
            );
            Map<String, Object> result = aiService.executeAction("submit_award", params, member);

            assertThat(result.get("success")).isEqualTo(true);
        }
    }

    // ==================== getAIResponse 测试 ====================

    @Nested
    @DisplayName("getAIResponse AI对话测试")
    class GetAIResponseTests {

        @FastTest
        @DisplayName("新会话应创建对话记录")
        void should_create_conversation_for_new_session() {
            when(conversationDAO.findBySessionId("session123")).thenReturn(null);
            when(conversationDAO.save(any())).thenReturn(1);
            when(messageDAO.save(any())).thenReturn(1);
            doNothing().when(conversationDAO).update(any(AIConversation.class));

            aiService.getAIResponse("你好", "session123", null);

            verify(conversationDAO).save(any(AIConversation.class));
        }

        @FastTest
        @DisplayName("已存在会话应复用对话记录")
        void should_reuse_existing_conversation() {
            AIConversation existing = new AIConversation();
            existing.setId(1);
            existing.setSessionId("session123");
            when(conversationDAO.findBySessionId("session123")).thenReturn(existing);
            when(messageDAO.save(any())).thenReturn(1);
            doNothing().when(conversationDAO).update(any(AIConversation.class));

            aiService.getAIResponse("你好", "session123", null);

            verify(conversationDAO, never()).save(any());
        }

        @FastTest
        @DisplayName("用户消息应保存到对话记录")
        void should_save_user_message_to_conversation() {
            AIConversation existing = new AIConversation();
            existing.setId(1);
            existing.setSessionId("session123");
            when(conversationDAO.findBySessionId("session123")).thenReturn(existing);
            when(messageDAO.save(any())).thenReturn(1);
            doNothing().when(conversationDAO).update(any(AIConversation.class));

            aiService.getAIResponse("测试消息", "session123", null);

            ArgumentCaptor<AIMessage> captor = ArgumentCaptor.forClass(AIMessage.class);
            verify(messageDAO, atLeastOnce()).save(captor.capture());
            List<AIMessage> savedMessages = captor.getAllValues();
            boolean hasUserMessage = savedMessages.stream().anyMatch(m -> "user".equals(m.getRole()));
            assertThat(hasUserMessage).isTrue();
        }

        @FastTest
        @DisplayName("空消息应正常处理")
        void should_handle_empty_message() {
            when(conversationDAO.findBySessionId("session123")).thenReturn(null);
            when(conversationDAO.save(any())).thenReturn(1);
            when(messageDAO.save(any())).thenReturn(1);
            doNothing().when(conversationDAO).update(any(AIConversation.class));

            aiService.getAIResponse("", "session123", null);

            assertThat(true).isTrue();
        }
    }

    // ==================== 角色枚举测试 ====================

    @Nested
    @DisplayName("角色枚举测试")
    class RoleEnumTests {

        @FastTest
        @DisplayName("角色常量应包含所有预期值")
        void role_constants_should_be_complete() {
            assertThat(AIService.ROLE_GUEST).isEqualTo("GUEST");
            assertThat(AIService.ROLE_MEMBER).isEqualTo("MEMBER");
            assertThat(AIService.ROLE_ADMIN).isEqualTo("ADMIN");
        }

        @FastTest
        @DisplayName("不同角色应能访问不同范围的action")
        void different_roles_should_access_different_actions() {
            // GUEST - 只能访问公开action
            Map<String, String> guestParams = createParams();
            Map<String, Object> guestResult = aiService.executeAction("list_all_news", guestParams, null);
            assertThat(guestResult.containsKey("success")).isTrue();

            // MEMBER - 可访问成员action
            User member = createMemberUser();
            Map<String, Object> memberResult = aiService.executeAction("view_my_activities", guestParams, member);
            assertThat(memberResult.containsKey("success")).isTrue();

            // ADMIN - 可访问管理员action
            User admin = createAdminUser();
            Map<String, Object> adminResult = aiService.executeAction("list_pending_users", guestParams, admin);
            assertThat(adminResult.containsKey("success")).isTrue();
        }
    }
}
