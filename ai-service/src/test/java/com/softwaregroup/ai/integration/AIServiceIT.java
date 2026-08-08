package com.softwaregroup.ai.integration;

import com.softwaregroup.ai.dao.*;
import com.softwaregroup.ai.feign.*;
import com.softwaregroup.ai.model.dto.*;
import com.softwaregroup.ai.model.entity.*;
import com.softwaregroup.ai.service.AIService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AIService 集成测试（混合模式）
 *
 * 测试AI服务的核心功能：
 * - AI自身数据（对话、消息、知识库、FAQ统计）直连DB
 * - 业务数据（用户、活动、项目、奖项、群组）通过Feign获取
 */
@ExtendWith(MockitoExtension.class)
class AIServiceIT {

    // AI自身数据DAO（直连DB）
    @Mock
    private AIConversationDAO conversationDAO;

    @Mock
    private AIMessageDAO messageDAO;

    @Mock
    private AIKnowledgeBaseDAO knowledgeBaseDAO;

    @Mock
    private AIFaqStatisticsDAO faqStatsDAO;

    @Mock
    private AIMessageStatusDAO messageStatusDAO;

    // 业务数据Feign客户端
    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private ActivityFeignClient activityFeignClient;

    @Mock
    private ProjectFeignClient projectFeignClient;

    @Mock
    private AwardFeignClient awardFeignClient;

    @Mock
    private GroupFeignClient groupFeignClient;

    @Mock
    private FileFeignClient fileFeignClient;

    private AIService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AIService(
            conversationDAO, messageDAO, knowledgeBaseDAO, faqStatsDAO, messageStatusDAO,
            userFeignClient, activityFeignClient, projectFeignClient, awardFeignClient,
            groupFeignClient, fileFeignClient
        );
    }

    // ==================== 对话管理测试（AI自身数据直连DB） ====================

    @Test
    void createOrGetConversation_withNewSession_shouldCreateConversation() {
        String sessionId = "test-session-123";
        when(conversationDAO.findBySessionId(sessionId)).thenReturn(null);
        when(conversationDAO.save(any(AIConversation.class))).thenReturn(1);

        AIConversation result = aiService.createOrGetConversation(sessionId, 1);

        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(sessionId);
        verify(conversationDAO).save(any(AIConversation.class));
    }

    @Test
    void createOrGetConversation_withExistingSession_shouldReturnExisting() {
        String sessionId = "test-session-123";
        AIConversation existing = new AIConversation(1, sessionId);
        existing.setId(100);

        when(conversationDAO.findBySessionId(sessionId)).thenReturn(existing);

        AIConversation result = aiService.createOrGetConversation(sessionId, 1);

        assertThat(result).isEqualTo(existing);
        assertThat(result.getId()).isEqualTo(100);
        verify(conversationDAO, never()).save(any());
    }

    @Test
    void getConversationHistory_withExistingSession_shouldReturnMessages() {
        String sessionId = "test-session-123";
        AIConversation conversation = new AIConversation(1, sessionId);
        conversation.setId(100);

        AIMessage msg1 = new AIMessage(100, "user", "你好");
        AIMessage msg2 = new AIMessage(100, "assistant", "你好，有什么可以帮助你的？");

        when(conversationDAO.findBySessionId(sessionId)).thenReturn(conversation);
        when(messageDAO.findByConversationId(100)).thenReturn(Arrays.asList(msg1, msg2));

        List<AIMessage> result = aiService.getConversationHistory(sessionId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("你好");
    }

    @Test
    void getConversationHistory_withNonExistingSession_shouldReturnNull() {
        String sessionId = "non-existing-session";
        when(conversationDAO.findBySessionId(sessionId)).thenReturn(null);

        List<AIMessage> result = aiService.getConversationHistory(sessionId);

        assertThat(result).isNull();
    }

    @Test
    void clearConversation_shouldDeleteMessagesAndConversation() {
        String sessionId = "test-session-123";
        AIConversation conversation = new AIConversation(1, sessionId);
        conversation.setId(100);

        when(conversationDAO.findBySessionId(sessionId)).thenReturn(conversation);
        doNothing().when(messageDAO).deleteByConversationId(100);
        doNothing().when(conversationDAO).delete(100);

        aiService.clearConversation(sessionId);

        verify(messageDAO).deleteByConversationId(100);
        verify(conversationDAO).delete(100);
    }

    // ==================== 消息处理测试 ====================

    @Test
    void saveMessage_shouldSaveAndReturnMessage() {
        AIMessage message = new AIMessage(100, "user", "测试消息");

        when(messageDAO.save(any(AIMessage.class))).thenReturn(1);

        AIMessage result = aiService.saveMessage(100, "user", "测试消息");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("测试消息");
        verify(messageDAO).save(any(AIMessage.class));
    }

    // ==================== 知识库测试（AI自身数据直连DB） ====================

    @Test
    void getAllKnowledgeBase_shouldReturnAllEntries() {
        AIKnowledgeBase kb1 = new AIKnowledgeBase();
        kb1.setQuestion("如何加入软件小组？");
        kb1.setAnswer("访问网站注册账号");
        kb1.setKeywords("加入,注册");

        AIKnowledgeBase kb2 = new AIKnowledgeBase();
        kb2.setQuestion("软件小组有哪些活动？");
        kb2.setAnswer("定期技术分享、项目实践");
        kb2.setKeywords("活动,分享");

        when(knowledgeBaseDAO.findAll()).thenReturn(Arrays.asList(kb1, kb2));

        List<AIKnowledgeBase> result = aiService.getAllKnowledgeBase();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestion()).contains("加入");
    }

    @Test
    void searchKnowledgeBase_withMatchingKeyword_shouldReturnResults() {
        AIKnowledgeBase kb1 = new AIKnowledgeBase();
        kb1.setQuestion("如何加入软件小组？");
        kb1.setKeywords("加入,注册");

        AIKnowledgeBase kb2 = new AIKnowledgeBase();
        kb2.setQuestion("有哪些项目？");
        kb2.setKeywords("项目");

        when(knowledgeBaseDAO.findAll()).thenReturn(Arrays.asList(kb1, kb2));

        List<AIKnowledgeBase> result = aiService.searchKnowledgeBase("加入");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestion()).contains("加入");
    }

    @Test
    void searchKnowledgeBase_withNoMatch_shouldReturnEmptyList() {
        when(knowledgeBaseDAO.findAll()).thenReturn(Arrays.asList());

        List<AIKnowledgeBase> result = aiService.searchKnowledgeBase("xyz");

        assertThat(result).isEmpty();
    }

    @Test
    void searchKnowledgeBase_withNullKeyword_shouldReturnEmptyList() {
        List<AIKnowledgeBase> result = aiService.searchKnowledgeBase(null);

        assertThat(result).isEmpty();
    }

    @Test
    void searchKnowledgeBase_withEmptyKeyword_shouldReturnEmptyList() {
        List<AIKnowledgeBase> result = aiService.searchKnowledgeBase("");

        assertThat(result).isEmpty();
    }

    // ==================== FAQ统计测试（AI自身数据直连DB） ====================

    @Test
    void getTopQuestions_shouldReturnTopQuestions() {
        AIFaqStatistics stats1 = new AIFaqStatistics();
        stats1.setNormalizedQuestion("如何加入？");
        stats1.setQueryCount(100);

        AIFaqStatistics stats2 = new AIFaqStatistics();
        stats2.setNormalizedQuestion("有哪些活动？");
        stats2.setQueryCount(50);

        when(faqStatsDAO.findTopQuestions(10)).thenReturn(Arrays.asList(stats1, stats2));

        List<AIFaqStatistics> result = aiService.getTopQuestions(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQueryCount()).isEqualTo(100);
    }

    @Test
    void getTopQuestions_withZeroLimit_shouldReturnEmptyList() {
        List<AIFaqStatistics> result = aiService.getTopQuestions(0);

        assertThat(result).isEmpty();
        verify(faqStatsDAO, never()).findTopQuestions(anyInt());
    }

    @Test
    void recordQuestionStatistics_withNewQuestion_shouldCreateStats() {
        String question = "如何加入小组？";
        when(faqStatsDAO.findByHash(question.toLowerCase())).thenReturn(null);
        when(faqStatsDAO.save(any(AIFaqStatistics.class))).thenReturn(1);

        aiService.recordQuestionStatistics(question);

        verify(faqStatsDAO).save(any(AIFaqStatistics.class));
    }

    @Test
    void recordQuestionStatistics_withExistingQuestion_shouldIncrementCount() {
        String question = "如何加入小组？";
        AIFaqStatistics existing = new AIFaqStatistics();
        existing.setQueryCount(5);
        existing.setNormalizedQuestion("如何加入小组？");

        when(faqStatsDAO.findByHash(question.toLowerCase())).thenReturn(existing);
        doNothing().when(faqStatsDAO).update(any(AIFaqStatistics.class));

        aiService.recordQuestionStatistics(question);

        assertThat(existing.getQueryCount()).isEqualTo(6);
        verify(faqStatsDAO).update(existing);
    }

    @Test
    void recordQuestionStatistics_withNullQuestion_shouldDoNothing() {
        aiService.recordQuestionStatistics(null);

        verify(faqStatsDAO, never()).findByHash(anyString());
        verify(faqStatsDAO, never()).save(any());
    }

    // ==================== Feign调用测试 - 用户服务 ====================

    @Test
    void getUserInfo_withValidUserId_shouldReturnUserDTO() {
        UserDTO user = new UserDTO();
        user.setId(1);
        user.setUsername("admin");
        user.setRole("ADMIN");

        Result result = Result.ok(user);
        when(userFeignClient.getUserById(1)).thenReturn(result);

        UserDTO actual = aiService.getUserInfo(1);

        assertThat(actual).isNotNull();
        assertThat(actual.getUsername()).isEqualTo("admin");
    }

    @Test
    void getUserInfo_withNullUserId_shouldReturnNull() {
        UserDTO result = aiService.getUserInfo(null);

        assertThat(result).isNull();
        verify(userFeignClient, never()).getUserById(anyInt());
    }

    @Test
    void getUserInfo_withNegativeUserId_shouldReturnNull() {
        UserDTO result = aiService.getUserInfo(-1);

        assertThat(result).isNull();
        verify(userFeignClient, never()).getUserById(anyInt());
    }

    @Test
    void getUserInfoCached_withSameUserId_shouldUseCache() {
        UserDTO user = new UserDTO();
        user.setId(1);
        user.setUsername("admin");

        Result result = Result.ok(user);
        when(userFeignClient.getUserById(1)).thenReturn(result);

        UserDTO first = aiService.getUserInfoCached(1);
        UserDTO second = aiService.getUserInfoCached(1);

        assertThat(first).isEqualTo(second);
        verify(userFeignClient, times(1)).getUserById(1); // 只调用一次
    }

    // ==================== Feign调用测试 - 活动服务 ====================

    @Test
    void getActivities_shouldReturnActivityList() {
        ActivityDTO activity = new ActivityDTO();
        activity.setId(1);
        activity.setTitle("技术分享会");

        Map<String, Object> data = new HashMap<>();
        data.put("list", Arrays.asList(activity));

        Result result = Result.ok(Arrays.asList(activity));
        when(activityFeignClient.getActivities()).thenReturn(result);

        List<ActivityDTO> activities = aiService.getActivities();

        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getTitle()).isEqualTo("技术分享会");
    }

    @Test
    void getActivityDetail_withValidId_shouldReturnActivity() {
        ActivityDTO activity = new ActivityDTO();
        activity.setId(1);
        activity.setTitle("技术分享会");

        Result result = Result.ok(activity);
        when(activityFeignClient.getActivityById(1)).thenReturn(result);

        ActivityDTO actual = aiService.getActivityDetail(1);

        assertThat(actual).isNotNull();
        assertThat(actual.getTitle()).isEqualTo("技术分享会");
    }

    @Test
    void getActivityDetail_withNullId_shouldReturnNull() {
        ActivityDTO result = aiService.getActivityDetail(null);

        assertThat(result).isNull();
        verify(activityFeignClient, never()).getActivityById(anyInt());
    }

    @Test
    void getUpcomingActivities_shouldReturnUpcomingList() {
        Result result = Result.ok(Arrays.asList());
        when(activityFeignClient.getUpcomingActivities()).thenReturn(result);

        List<ActivityDTO> activities = aiService.getUpcomingActivities();

        assertThat(activities).isNotNull();
    }

    // ==================== Feign调用测试 - 项目服务 ====================

    @Test
    void getProjects_shouldReturnProjectList() {
        ProjectDTO project = new ProjectDTO();
        project.setId(1);
        project.setName("Web项目");

        Result result = Result.ok(Arrays.asList(project));
        when(projectFeignClient.getProjects()).thenReturn(result);

        List<ProjectDTO> projects = aiService.getProjects();

        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getName()).isEqualTo("Web项目");
    }

    @Test
    void getUserProjects_shouldReturnUserProjectList() {
        ProjectDTO project = new ProjectDTO();
        project.setId(1);
        project.setName("我的项目");

        Result result = Result.ok(Arrays.asList(project));
        when(projectFeignClient.getUserProjects(1)).thenReturn(result);

        List<ProjectDTO> projects = aiService.getUserProjects(1);

        assertThat(projects).hasSize(1);
    }

    @Test
    void getPublicProjects_shouldReturnPublicProjectList() {
        Result result = Result.ok(Arrays.asList());
        when(projectFeignClient.getPublicProjects()).thenReturn(result);

        List<ProjectDTO> projects = aiService.getPublicProjects();

        assertThat(projects).isNotNull();
    }

    // ==================== Feign调用测试 - 奖项服务 ====================

    @Test
    void getUserAwards_shouldReturnUserAwardList() {
        AwardDTO award = new AwardDTO();
        award.setId(1);
        award.setName("一等奖");

        Result result = Result.ok(Arrays.asList(award));
        when(awardFeignClient.getUserAwards(1)).thenReturn(result);

        List<AwardDTO> awards = aiService.getUserAwards(1);

        assertThat(awards).hasSize(1);
        assertThat(awards.get(0).getName()).isEqualTo("一等奖");
    }

    @Test
    void getAllAwards_shouldReturnAllAwards() {
        Result result = Result.ok(Arrays.asList());
        when(awardFeignClient.getAwards()).thenReturn(result);

        List<AwardDTO> awards = aiService.getAllAwards();

        assertThat(awards).isNotNull();
    }

    @Test
    void getPendingAwards_shouldReturnPendingAwards() {
        Result result = Result.ok(Arrays.asList());
        when(awardFeignClient.getPendingAwards()).thenReturn(result);

        List<AwardDTO> awards = aiService.getPendingAwards();

        assertThat(awards).isNotNull();
    }

    // ==================== Feign调用测试 - 群组服务 ====================

    @Test
    void getUserGroups_shouldReturnUserGroupList() {
        GroupDTO group = new GroupDTO();
        group.setId(1);
        group.setName("开发组");

        Result result = Result.ok(Arrays.asList(group));
        when(groupFeignClient.getUserGroups(1)).thenReturn(result);

        List<GroupDTO> groups = aiService.getUserGroups(1);

        assertThat(groups).hasSize(1);
        assertThat(groups.get(0).getName()).isEqualTo("开发组");
    }

    @Test
    void getGroupDetail_shouldReturnGroup() {
        GroupDTO group = new GroupDTO();
        group.setId(1);
        group.setName("开发组");

        Result result = Result.ok(group);
        when(groupFeignClient.getGroupById(1)).thenReturn(result);

        GroupDTO actual = aiService.getGroupDetail(1);

        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo("开发组");
    }

    // ==================== AI对话生成测试 ====================

    @Test
    void chat_shouldSaveMessageAndReturnResponse() {
        String sessionId = "test-session";
        String userMessage = "你好";

        AIConversation conversation = new AIConversation(1, sessionId);
        conversation.setId(100);

        when(conversationDAO.findBySessionId(sessionId)).thenReturn(conversation);
        when(messageDAO.save(any(AIMessage.class))).thenReturn(1);
        lenient().when(userFeignClient.getUserById(1)).thenReturn(Result.ok(new UserDTO()));

        String response = aiService.chat(userMessage, sessionId, 1);

        assertThat(response).isNotNull();
        assertThat(response).contains("AI回复");
        verify(messageDAO).save(any(AIMessage.class));
    }

    @Test
    void generateSessionId_shouldReturnUniqueId() {
        String sessionId1 = aiService.generateSessionId();
        String sessionId2 = aiService.generateSessionId();

        assertThat(sessionId1).isNotNull();
        assertThat(sessionId2).isNotNull();
        assertThat(sessionId1).isNotEqualTo(sessionId2);
        assertThat(sessionId1).hasSize(32); // UUID without dashes
    }

    // ==================== 系统提示词构建测试 ====================

    @Test
    void buildSystemPrompt_withNullRole_shouldReturnGuestPrompt() {
        String prompt = aiService.buildSystemPrompt(null);

        assertThat(prompt).contains("访客");
    }

    @Test
    void buildSystemPrompt_withGuestRole_shouldReturnGuestPrompt() {
        String prompt = aiService.buildSystemPrompt("GUEST");

        assertThat(prompt).contains("访客");
    }

    @Test
    void buildSystemPrompt_withMemberRole_shouldReturnMemberPrompt() {
        String prompt = aiService.buildSystemPrompt("MEMBER");

        assertThat(prompt).contains("正式成员");
    }

    @Test
    void buildSystemPrompt_withAdminRole_shouldReturnAdminPrompt() {
        String prompt = aiService.buildSystemPrompt("ADMIN");

        assertThat(prompt).contains("系统管理员");
    }

    // ==================== 操作执行测试 ====================

    @Test
    void executeAction_withListActivities_shouldReturnActivities() {
        ActivityDTO activity = new ActivityDTO();
        activity.setId(1);
        activity.setTitle("测试活动");

        Result result = Result.ok(Arrays.asList(activity));
        when(activityFeignClient.getActivities()).thenReturn(result);

        Map<String, String> params = new HashMap<>();
        Map<String, Object> actionResult = aiService.executeAction("list_activities", params);

        assertThat((Boolean) actionResult.get("success")).isTrue();
    }

    @Test
    void executeAction_withListPublicProjects_shouldReturnProjects() {
        ProjectDTO project = new ProjectDTO();
        project.setId(1);
        project.setName("公开项目");

        Result result = Result.ok(Arrays.asList(project));
        when(projectFeignClient.getPublicProjects()).thenReturn(result);

        Map<String, String> params = new HashMap<>();
        Map<String, Object> actionResult = aiService.executeAction("list_public_projects", params, null);

        assertThat((Boolean) actionResult.get("success")).isTrue();
    }

    @Test
    void executeAction_withUnknownAction_shouldReturnError() {
        Map<String, String> params = new HashMap<>();
        Map<String, Object> actionResult = aiService.executeAction("unknown_action", params);

        assertThat((Boolean) actionResult.get("success")).isFalse();
        assertThat((String) actionResult.get("message")).contains("未知操作");
    }

    @Test
    void executeAction_withListMyAwards_withoutUser_shouldReturnError() {
        Map<String, String> params = new HashMap<>();
        Map<String, Object> actionResult = aiService.executeAction("list_my_awards", params, null);

        assertThat((Boolean) actionResult.get("success")).isFalse();
    }

    @Test
    void executeAction_withListMyAwards_withUser_shouldReturnAwards() {
        UserDTO user = new UserDTO();
        user.setId(1);

        AwardDTO award = new AwardDTO();
        award.setId(1);

        Result result = Result.ok(Arrays.asList(award));
        when(awardFeignClient.getUserAwards(1)).thenReturn(result);

        Map<String, String> params = new HashMap<>();
        Map<String, Object> actionResult = aiService.executeAction("list_my_awards", params, user);

        assertThat((Boolean) actionResult.get("success")).isTrue();
    }

    // ==================== 消息状态测试 ====================

    @Test
    void createMessageStatus_shouldSaveAndReturnId() {
        AIMessageStatus status = new AIMessageStatus();
        status.setSessionId("test-session");
        status.setUserId(1);
        status.setUserMessage("你好");
        status.setStatus(AIMessageStatus.STATUS_PENDING);

        when(messageStatusDAO.save(any(AIMessageStatus.class))).thenReturn(1);

        Integer statusId = aiService.createMessageStatus("test-session", 1, "你好");

        assertThat(statusId).isEqualTo(1);
        verify(messageStatusDAO).save(any(AIMessageStatus.class));
    }

    @Test
    void updateMessageStatus_withExistingStatus_shouldReturnTrue() {
        AIMessageStatus status = new AIMessageStatus();
        status.setId(1);
        status.setStatus(AIMessageStatus.STATUS_PENDING);

        when(messageStatusDAO.findById(1)).thenReturn(status);
        doNothing().when(messageStatusDAO).update(any(AIMessageStatus.class));

        boolean result = aiService.updateMessageStatus(1, AIMessageStatus.STATUS_COMPLETED);

        assertThat(result).isTrue();
    }

    @Test
    void updateMessageStatus_withNonExistingStatus_shouldReturnFalse() {
        when(messageStatusDAO.findById(9999)).thenReturn(null);

        boolean result = aiService.updateMessageStatus(9999, AIMessageStatus.STATUS_COMPLETED);

        assertThat(result).isFalse();
    }

    // ==================== 参数解析测试 ====================

    @Test
    void parseActionParams_withValidString_shouldReturnParams() {
        String actionString = "key1=value1|key2=value2";

        Map<String, String> params = aiService.parseActionParams(actionString);

        assertThat(params).hasSize(2);
        assertThat(params.get("key1")).isEqualTo("value1");
        assertThat(params.get("key2")).isEqualTo("value2");
    }

    @Test
    void parseActionParams_withEmptyString_shouldReturnEmptyMap() {
        Map<String, String> params = aiService.parseActionParams("");

        assertThat(params).isEmpty();
    }

    @Test
    void parseActionParams_withNull_shouldReturnEmptyMap() {
        Map<String, String> params = aiService.parseActionParams(null);

        assertThat(params).isEmpty();
    }

    @Test
    void parseActionParams_withMixedKeyValue_shouldHandleCorrectly() {
        String actionString = "action=list|userId=1|filter=";

        Map<String, String> params = aiService.parseActionParams(actionString);

        assertThat(params.get("action")).isEqualTo("list");
        assertThat(params.get("userId")).isEqualTo("1");
        assertThat(params.get("filter")).isEqualTo("");
    }
}
