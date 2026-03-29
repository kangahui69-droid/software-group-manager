package service;

import dao.AIConversationDAO;
import dao.AIMessageDAO;
import dao.AIKnowledgeBaseDAO;
import dao.AIFaqStatisticsDAO;
import model.AIConversation;
import model.AIMessage;
import model.AIKnowledgeBase;
import model.AIFaqStatistics;
import model.User;
import util.AIClientUtil;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AIService {

    private AIClientUtil aiClient = AIClientUtil.getInstance();
    private AIConversationDAO conversationDAO = new AIConversationDAO();
    private AIMessageDAO messageDAO = new AIMessageDAO();
    private AIKnowledgeBaseDAO knowledgeBaseDAO = new AIKnowledgeBaseDAO();
    private AIFaqStatisticsDAO faqStatsDAO = new AIFaqStatisticsDAO();

    public String getAIResponse(String userMessage, String sessionId, User user) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        if (conversation == null) {
            conversation = new AIConversation();
            conversation.setUserId(user != null ? user.getId() : 0);
            conversation.setSessionId(sessionId);
            conversation.setCreatedAt(new Date());
            conversation.setUpdatedAt(new Date());
            Integer id = conversationDAO.save(conversation);
            conversation.setId(id);
        }

        AIMessage userMsg = new AIMessage();
        userMsg.setConversationId(conversation.getId());
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        userMsg.setCreatedAt(new Date());
        messageDAO.save(userMsg);

        conversation.setUpdatedAt(new Date());
        conversationDAO.update(conversation);

        String userRole = "GUEST";
        if (user != null) {
            userRole = user.getRole();
        }

        String systemPrompt = buildSystemPrompt(userRole);
        String context = buildContext(userMessage, userRole);
        String fullPrompt = systemPrompt + "\n\n" + context;

        String aiResponse = aiClient.chat(systemPrompt, fullPrompt);

        AIMessage assistantMsg = new AIMessage();
        assistantMsg.setConversationId(conversation.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiResponse);
        assistantMsg.setCreatedAt(new Date());
        messageDAO.save(assistantMsg);

        recordQuestionStatistics(userMessage);

        return aiResponse;
    }

    private void recordQuestionStatistics(String question) {
        try {
            String normalized = question.trim().toLowerCase();
            AIFaqStatistics stats = faqStatsDAO.findByHash(normalized.hashCode() + "");
            if (stats == null) {
                stats = new AIFaqStatistics();
                stats.setQuestionHash(normalized.hashCode() + "");
                stats.setNormalizedQuestion(question.trim());
                stats.setQueryCount(1);
                stats.setLastQueryAt(new Date());
                faqStatsDAO.save(stats);
            } else {
                stats.setQueryCount(stats.getQueryCount() + 1);
                stats.setLastQueryAt(new Date());
                faqStatsDAO.update(stats);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AIMessage> getConversationHistory(String sessionId) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        if (conversation != null) {
            return messageDAO.findByConversationId(conversation.getId());
        }
        return null;
    }

    public void clearConversation(String sessionId) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        if (conversation != null) {
            messageDAO.deleteByConversationId(conversation.getId());
            conversationDAO.delete(conversation.getId());
        }
    }

    public List<AIKnowledgeBase> getKnowledgeBase() {
        return knowledgeBaseDAO.findAll();
    }

    public List<AIFaqStatistics> getFaqStatistics(int limit) {
        return faqStatsDAO.findTopQuestions(limit);
    }

    public List<AIFaqStatistics> getAllFaqStatistics() {
        return faqStatsDAO.findAllOrderByCount();
    }

    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void initConversation(String sessionId, User user) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        if (conversation == null) {
            conversation = new AIConversation();
            conversation.setUserId(user != null ? user.getId() : 0);
            conversation.setSessionId(sessionId);
            conversation.setCreatedAt(new Date());
            conversation.setUpdatedAt(new Date());
            Integer id = conversationDAO.save(conversation);
            conversation.setId(id);
        }
        
        String userRole = user != null ? user.getRole() : "GUEST";
        String systemPrompt = buildSystemPrompt(userRole);
        
        AIMessage systemMsg = new AIMessage();
        systemMsg.setConversationId(conversation.getId());
        systemMsg.setRole("system");
        systemMsg.setContent(systemPrompt);
        systemMsg.setCreatedAt(new Date());
        messageDAO.save(systemMsg);
    }

    public String buildSystemPrompt(String userRole) {
        return "你是黄山学院软件小组管理系统的AI助手。\n" +
            "角色：用户身份为" + userRole + "。\n" +
            "要求：简洁直接回答，不解释思考过程，引导登录查看具体内容。";
    }

    public String buildContext(String userMessage, String userRole) {
        StringBuilder context = new StringBuilder();
        context.append("知识库：\n");

        List<AIKnowledgeBase> allKB = knowledgeBaseDAO.findAll();
        String userMsgLower = userMessage.toLowerCase();

        for (AIKnowledgeBase kb : allKB) {
            if (kb.getKeywords() != null) {
                String[] keywords = kb.getKeywords().toLowerCase().split(",");
                for (String keyword : keywords) {
                    if (userMsgLower.contains(keyword.trim())) {
                        context.append("- ").append(kb.getAnswer()).append("\n");
                        break;
                    }
                }
            }
        }

        return context.toString();
    }
}