package service;

import dao.AIConversationDAO;
import dao.AIMessageDAO;
import dao.AIKnowledgeBaseDAO;
import model.AIConversation;
import model.AIMessage;
import model.AIKnowledgeBase;
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

        String aiResponse = aiClient.chat(fullPrompt, userMessage);

        AIMessage assistantMsg = new AIMessage();
        assistantMsg.setConversationId(conversation.getId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiResponse);
        assistantMsg.setCreatedAt(new Date());
        messageDAO.save(assistantMsg);

        return aiResponse;
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

    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildSystemPrompt(String userRole) {
        String basePrompt = "你是高校软件小组管理系统的AI助手。";

        switch (userRole) {
            case "ADMIN":
                return basePrompt +
                    "当前用户是【管理员】，可使用全部管理功能。\n" +
                    "管理员功能包括：新闻管理、奖项审核、活动管理、项目审批、" +
                    "成员管理、招新审核、操作日志、数据统计等。\n" +
                    "请提供专业的管理操作指导。";
            case "MEMBER":
                return basePrompt +
                    "当前用户是【小组成员】。\n" +
                    "成员功能包括：个人资料管理、奖项申报、活动报名、" +
                    "项目管理、简历编辑、密码修改等。\n" +
                    "请提供简洁的成员操作指导。";
            default:
                return basePrompt +
                    "当前用户是【游客】（未登录）。\n" +
                    "游客可浏览公开新闻、活动，提交招新报名。\n" +
                    "请引导用户了解系统，提示注册登录方式。";
        }
    }

    private String buildContext(String userMessage, String userRole) {
        StringBuilder context = new StringBuilder();
        context.append("参考知识库：\n");

        List<AIKnowledgeBase> allKB = knowledgeBaseDAO.findAll();
        String userMsgLower = userMessage.toLowerCase();

        for (AIKnowledgeBase kb : allKB) {
            if (kb.getKeywords() != null) {
                String[] keywords = kb.getKeywords().toLowerCase().split(",");
                for (String keyword : keywords) {
                    if (userMsgLower.contains(keyword.trim())) {
                        context.append("【").append(kb.getCategory()).append("】\n");
                        context.append("问题：").append(kb.getQuestion()).append("\n");
                        context.append("答案：").append(kb.getAnswer()).append("\n\n");
                        break;
                    }
                }
            }
        }

        context.append("用户问题：").append(userMessage).append("\n");
        context.append("请根据知识库或系统信息回答用户问题。");

        return context.toString();
    }
}