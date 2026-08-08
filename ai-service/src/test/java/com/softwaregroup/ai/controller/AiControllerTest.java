package com.softwaregroup.ai.controller;

import com.softwaregroup.ai.model.dto.*;
import com.softwaregroup.ai.model.entity.*;
import com.softwaregroup.ai.service.AIService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AIService aiService;

    @InjectMocks
    private AiController aiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
    }

    // ==================== 对话管理测试 ====================

    @Test
    void chat_shouldReturnResponse() throws Exception {
        when(aiService.chat(eq("你好"), isNull(), isNull())).thenReturn("你好，我是AI助手");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"你好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("你好，我是AI助手"));
    }

    @Test
    void chat_withSessionIdAndUserId_shouldPassParams() throws Exception {
        when(aiService.chat(eq("你好"), eq("session123"), eq(1))).thenReturn("你好，用户");

        mockMvc.perform(post("/api/ai/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"你好\",\"sessionId\":\"session123\",\"userId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getHistory_shouldReturnHistory() throws Exception {
        List<AIMessage> history = List.of(
            createAIMessage(1, "你好"),
            createAIMessage(2, "你好，有什么帮助")
        );
        when(aiService.getConversationHistory(eq("session123"))).thenReturn(history);

        mockMvc.perform(get("/api/ai/history")
                        .param("sessionId", "session123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void clearConversation_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/ai/clear")
                        .param("sessionId", "session123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 知识库测试 ====================

    @Test
    void getAllKnowledge_shouldReturnList() throws Exception {
        List<AIKnowledgeBase> knowledgeList = List.of(
            createKnowledge(1, "如何加入社团"),
            createKnowledge(2, "如何报名活动")
        );
        when(aiService.getAllKnowledgeBase()).thenReturn(knowledgeList);

        mockMvc.perform(get("/api/ai/knowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void searchKnowledge_shouldReturnResults() throws Exception {
        List<AIKnowledgeBase> results = List.of(createKnowledge(1, "如何加入社团"));
        when(aiService.searchKnowledgeBase(eq("加入"))).thenReturn(results);

        mockMvc.perform(get("/api/ai/knowledge/search")
                        .param("keyword", "加入"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== FAQ统计测试 ====================

    @Test
    void getTopQuestions_shouldReturnList() throws Exception {
        List<AIFaqStatistics> faqList = List.of(
            createFaqStat(1, "如何报名活动", 100),
            createFaqStat(2, "如何加入社团", 80)
        );
        when(aiService.getTopQuestions(eq(10))).thenReturn(faqList);

        mockMvc.perform(get("/api/ai/faq/top")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTopQuestions_withDefaultLimit_shouldUseDefault() throws Exception {
        when(aiService.getTopQuestions(eq(10))).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/faq/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getAllFaqStatistics_shouldReturnList() throws Exception {
        when(aiService.getAllFaqStatistics()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/faq/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== ACTION执行测试 ====================

    @Test
    void executeAction_shouldReturnResult() throws Exception {
        when(aiService.executeAction(eq("queryActivity"), any(), isNull())).thenReturn(Map.of("id", 1));

        mockMvc.perform(post("/api/ai/execute")
                        .contentType("application/json")
                        .content("{\"actionType\":\"queryActivity\",\"params\":{}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void executeAction_withUser_shouldPassUser() throws Exception {
        Map<String, Object> user = Map.of("id", 1, "username", "admin", "role", "ADMIN");
        when(aiService.executeAction(eq("queryActivity"), any(), any())).thenReturn(Map.of("id", 1));

        mockMvc.perform(post("/api/ai/execute")
                        .contentType("application/json")
                        .content("{\"actionType\":\"queryActivity\",\"params\":{},\"user\":{\"id\":1,\"username\":\"admin\",\"role\":\"ADMIN\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ==================== 系统提示词测试 ====================

    @Test
    void getPrompt_shouldReturnPrompt() throws Exception {
        when(aiService.buildSystemPrompt(isNull())).thenReturn("你是一个AI助手");

        mockMvc.perform(get("/api/ai/prompt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("你是一个AI助手"));
    }

    @Test
    void getPrompt_withRole_shouldPassRole() throws Exception {
        when(aiService.buildSystemPrompt(eq("ADMIN"))).thenReturn("你是一个管理员");

        mockMvc.perform(get("/api/ai/prompt")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("你是一个管理员"));
    }

    // ==================== 消息状态测试 ====================

    @Test
    void createStatus_shouldReturnStatusId() throws Exception {
        when(aiService.createMessageStatus(eq("session123"), isNull(), isNull())).thenReturn(1);

        mockMvc.perform(post("/api/ai/status")
                        .contentType("application/json")
                        .content("{\"sessionId\":\"session123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void createStatus_withUserIdAndMessage_shouldPassParams() throws Exception {
        when(aiService.createMessageStatus(eq("session123"), eq(1), eq("你好"))).thenReturn(1);

        mockMvc.perform(post("/api/ai/status")
                        .contentType("application/json")
                        .content("{\"sessionId\":\"session123\",\"userId\":1,\"userMessage\":\"你好\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateStatus_shouldReturnSuccess() throws Exception {
        when(aiService.updateMessageStatus(eq(1), eq("COMPLETED"))).thenReturn(true);

        mockMvc.perform(put("/api/ai/status/1")
                        .contentType("application/json")
                        .content("{\"newStatus\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== 参数解析测试 ====================

    @Test
    void parseParams_shouldReturnParams() throws Exception {
        when(aiService.parseActionParams(eq("[ACTION]queryActivity|id=1"))).thenReturn(Map.of("id", "1"));

        mockMvc.perform(post("/api/ai/parse-params")
                        .contentType("application/json")
                        .content("{\"actionString\":\"[ACTION]queryActivity|id=1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("1"));
    }

    // ==================== 业务数据查询测试 ====================

    @Test
    void getUserInfo_shouldReturnUser() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1);
        user.setUsername("admin");
        when(aiService.getUserInfo(eq(1))).thenReturn(user);

        mockMvc.perform(get("/api/ai/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("admin"));
    }

    @Test
    void getActivities_shouldReturnActivities() throws Exception {
        when(aiService.getActivities()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getUpcomingActivities_shouldReturnActivities() throws Exception {
        when(aiService.getUpcomingActivities()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/activities/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getProjects_shouldReturnProjects() throws Exception {
        when(aiService.getProjects()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPublicProjects_shouldReturnProjects() throws Exception {
        when(aiService.getPublicProjects()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/projects/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getAllAwards_shouldReturnAwards() throws Exception {
        when(aiService.getAllAwards()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/awards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPendingAwards_shouldReturnAwards() throws Exception {
        when(aiService.getPendingAwards()).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/awards/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getUserGroups_shouldReturnGroups() throws Exception {
        when(aiService.getUserGroups(eq(1))).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== 辅助方法 ====================

    private AIMessage createAIMessage(int id, String content) {
        AIMessage msg = new AIMessage();
        msg.setId(id);
        msg.setContent(content);
        return msg;
    }

    private AIKnowledgeBase createKnowledge(int id, String question) {
        AIKnowledgeBase kb = new AIKnowledgeBase();
        kb.setId(id);
        kb.setQuestion(question);
        return kb;
    }

    private AIFaqStatistics createFaqStat(int id, String question, int count) {
        AIFaqStatistics stat = new AIFaqStatistics();
        stat.setId(id);
        stat.setNormalizedQuestion(question);
        stat.setQueryCount(count);
        return stat;
    }
}
