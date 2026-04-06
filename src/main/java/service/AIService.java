package service;

import dao.AIConversationDAO;
import dao.AIMessageDAO;
import dao.AIKnowledgeBaseDAO;
import dao.AIFaqStatisticsDAO;
import dao.ActivityDAO;
import dao.NewsDAO;
import dao.ProblemReportDAO;
import dao.RegistrationDAO;
import dao.ProjectDAO;
import dao.UserGroupDAO;
import model.AIConversation;
import model.AIMessage;
import model.AIKnowledgeBase;
import model.AIFaqStatistics;
import model.Activity;
import model.News;
import model.ProblemReport;
import model.Registration;
import model.User;
import util.AIClientUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AIService {

    public static final String ROLE_GUEST = "GUEST";
    public static final String ROLE_MEMBER = "MEMBER";
    public static final String ROLE_ADMIN = "ADMIN";

    private AIClientUtil aiClient = AIClientUtil.getInstance();
    private AIConversationDAO conversationDAO = new AIConversationDAO();
    private AIMessageDAO messageDAO = new AIMessageDAO();
    private AIKnowledgeBaseDAO knowledgeBaseDAO = new AIKnowledgeBaseDAO();
    private AIFaqStatisticsDAO faqStatsDAO = new AIFaqStatisticsDAO();
    private ActivityDAO activityDAO = new ActivityDAO();
    private NewsDAO newsDAO = new NewsDAO();
    private ProblemReportDAO problemReportDAO = new ProblemReportDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    private ProjectDAO projectDAO = new ProjectDAO();
    private UserGroupDAO userGroupDAO = new UserGroupDAO();

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
        String history = buildConversationHistory(conversation.getId());
        String context = buildContext(userMessage, userRole);
        String userContent = history + "\n\n当前用户消息: " + userMessage + "\n\n" + context;

        String aiResponse = aiClient.chat(systemPrompt, userContent);

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
        switch (userRole) {
            case ROLE_ADMIN:
                return buildAdminPrompt();
            case ROLE_MEMBER:
                return buildMemberPrompt();
            case ROLE_GUEST:
            default:
                return buildGuestPrompt();
        }
    }

    private String buildGuestPrompt() {
        return "你是黄山学院软件小组管理系统的AI助手。\n\n" +
               "用户身份：访客（未登录）\n\n" +
               "你可以提供的服务：\n" +
               "1. 介绍黄山学院软件小组的基本情况\n" +
               "2. 回答关于活动、项目、新闻等信息的查询\n" +
               "3. 帮助用户了解如何注册和加入软件小组\n" +
               "4. 接收问题反馈并引导提交\n\n" +
               "注意事项：\n" +
               "- 不要尝试执行需要登录的操作\n" +
               "- 引导用户登录或注册后再使用完整功能\n" +
               "- 回答简洁直接，不解释思考过程\n" +
               "- 当用户想报名活动、申请项目、提交新闻时，引导其先登录";
    }

    private String buildMemberPrompt() {
        return "你是黄山学院软件小组管理系统的AI助手。\n\n" +
               "用户身份：正式成员\n\n" +
               "你可以提供的服务：\n" +
               "1. 查询和回答关于小组活动的各种问题\n" +
               "2. 帮助用户报名参加活动（活动名称、时间等）\n" +
               "3. 帮助用户申请加入项目\n" +
               "4. 引导用户提交活动新闻投稿\n" +
               "5. 帮助用户提交问题反馈\n" +
               "6. 帮助用户创建活动\n" +
               "7. 查询用户的报名记录、参加的活动等信息\n\n" +
               "重要：用户输入参数后直接生成ACTION\n" +
               "当用户想执行操作时（如创建活动、报名活动等），你应该：\n" +
               "1. 用户提供参数后，直接生成 [ACTION] 格式\n" +
               "2. 格式：[ACTION]action_type|param1=value1|param2=value2\n\n" +
               "可用的操作类型：\n" +
               "- create_activity: 创建活动\n" +
               "- signup_activity: 报名活动\n" +
               "- submit_news: 提交新闻\n" +
               "- submit_feedback: 提交问题反馈\n" +
               "- view_my_activities: 查看我报名的活动\n" +
               "- view_my_projects: 查看我申请的项目\n" +
               "- view_my_groups: 查看我的群聊\n\n" +
               "创建活动参数：\n" +
               "- name: 活动名称\n" +
               "- desc: 活动描述\n" +
               "- type: 活动类型（竞赛/讲座/培训/其他）\n" +
               "- location: 活动地点\n" +
               "- max: 最大参与人数（数字）\n" +
               "- start: 活动开始时间（如2026-04-10 09:00）\n" +
               "- end: 活动结束时间\n" +
               "- reg_start: 报名开始时间\n" +
               "- reg_end: 报名结束时间\n\n" +
               "示例对话（创建活动）：\n" +
               "用户：创建活动\n" +
               "助手：请提供：名称、描述、类型、地点、人数、开始时间、结束时间、报名开始、报名结束\n\n" +
               "用户：测试 描述 竞赛 地点1 50 2026-04-10 09:00 2026-04-10 18:00 2026-04-01 00:00 2026-04-08 23:59\n" +
               "助手：[ACTION]create_activity|name=测试|desc=描述|type=竞赛|location=地点1|max=50|start=2026-04-10 09:00|end=2026-04-10 18:00|reg_start=2026-04-01 00:00|reg_end=2026-04-08 23:59\n\n" +
               "示例对话（查看我的活动）：\n" +
               "用户：查看我的活动\n" +
               "助手：[ACTION]view_my_activities";
    }

    private String buildAdminPrompt() {
        return "你是黄山学院软件小组管理系统的AI助手。\n\n" +
               "用户身份：系统管理员\n\n" +
               "你可以提供的服务：\n" +
               "1. 查询和展示各类待审核数据\n" +
               "2. 一键审核操作（用户、活动、新闻等）\n" +
               "3. 问题处理和一键修复\n" +
               "4. 批量操作支持\n" +
               "5. AI知识库管理\n\n" +
               "管理员操作格式：\n" +
               "当需要执行操作时，回复格式：\n" +
               "[ACTION]action_type|param1=value1|param2=value2\n\n" +
               "可用的操作类型：\n" +
               "【用户管理】\n" +
               "- list_pending_users: 列出待审核用户\n" +
               "- approve_user: 批准用户，需要user_id\n" +
               "- reject_user: 拒绝用户，需要user_id和reason\n" +
               "- list_all_users: 列出所有用户\n\n" +
               "【活动管理】\n" +
               "- list_pending_activities: 列出待审核活动\n" +
               "- approve_activity: 批准活动，需要activity_id\n" +
               "- reject_activity: 拒绝活动，需要activity_id和reason\n" +
               "- view_participants: 查看活动参与者，需要activity_id\n\n" +
               "【新闻管理】\n" +
               "- list_pending_news: 列出待审核新闻\n" +
               "- approve_news: 批准新闻，需要news_id\n" +
               "- reject_news: 拒绝新闻，需要news_id和reason\n\n" +
               "【问题处理】\n" +
               "- list_pending_problems: 列出待处理问题\n" +
               "- classify_problem: 分类问题，需要problem_id和type\n" +
               "- fix_problem: 修复问题，需要problem_id和solution\n" +
               "- resolve_problem: 标记问题为已解决，需要problem_id\n\n" +
               "【数据查询】\n" +
               "- statistics: 查看系统统计数据\n" +
               "- recent_activities: 查看最近的活动\n" +
               "- recent_news: 查看最近的新闻\n\n" +
               "注意事项：\n" +
               "- 执行危险操作前先确认\n" +
               "- 记录所有操作日志\n" +
               "- 回答简洁直接";
    }

    public String buildContext(String userMessage, String userRole) {
        StringBuilder context = new StringBuilder();
        context.append("【知识库匹配】\n");

        List<AIKnowledgeBase> allKB = knowledgeBaseDAO.findAll();
        String userMsgLower = userMessage.toLowerCase();
        boolean found = false;

        for (AIKnowledgeBase kb : allKB) {
            if (kb.getKeywords() != null) {
                String[] keywords = kb.getKeywords().toLowerCase().split(",");
                for (String keyword : keywords) {
                    if (userMsgLower.contains(keyword.trim())) {
                        context.append("- [").append(kb.getQuestion()).append("] ")
                               .append(kb.getAnswer()).append("\n");
                        found = true;
                        break;
                    }
                }
            }
        }

        if (!found) {
            context.append("- 暂无相关知识库条目\n");
        }

        context.append("\n【操作引导】\n");
        context.append(buildOperationGuide(userMessage, userRole));

        return context.toString();
    }

    private String buildOperationGuide(String userMessage, String userRole) {
        StringBuilder guide = new StringBuilder();
        String msgLower = userMessage.toLowerCase();

        if ((msgLower.contains("创建") || msgLower.contains("发起") || msgLower.contains("举办")) && msgLower.contains("活动")) {
            guide.append("- 用户想创建活动，用户提供参数后直接生成ACTION\n");
        } else if (msgLower.contains("报名") && msgLower.contains("活动")) {
            guide.append("- 用户想报名活动，提供activity_id后直接生成ACTION\n");
        }

        if (msgLower.contains("新闻") && (msgLower.contains("发布") || msgLower.contains("投稿") || msgLower.contains("提交"))) {
            guide.append("- 用户想提交新闻，提供参数后直接生成ACTION\n");
        }

        if ((msgLower.contains("问题") || msgLower.contains("反馈") || msgLower.contains("建议")) && !msgLower.contains("处理")) {
            guide.append("- 用户想提交反馈，提供参数后直接生成ACTION\n");
        }

        if (msgLower.contains("我的活动") || msgLower.contains("报名记录")) {
            guide.append("- 查看报名活动，可直接生成 [ACTION]view_my_activities\n");
        }

        if (msgLower.contains("我的项目") || msgLower.contains("项目申请")) {
            guide.append("- 查看申请项目，可直接生成 [ACTION]view_my_projects\n");
        }

        if (msgLower.contains("我的群聊") || msgLower.contains("加入的群")) {
            guide.append("- 查看我的群聊，可直接生成 [ACTION]view_my_groups\n");
        }

        if (msgLower.contains("确认")) {
            guide.append("- 用户确认了信息，根据对话历史确定操作类型并生成ACTION\n");
        }

        if (ROLE_ADMIN.equals(userRole)) {
            if (msgLower.contains("审核用户") || msgLower.contains("新用户") || msgLower.contains("待审核用户")) {
                guide.append("- 管理员想审核用户，可使用 [ACTION]list_pending_users\n");
            }
            if (msgLower.contains("审核活动") || msgLower.contains("待审核活动")) {
                guide.append("- 管理员想审核活动，可使用 [ACTION]list_pending_activities\n");
            }
            if (msgLower.contains("审核新闻") || msgLower.contains("待审核新闻")) {
                guide.append("- 管理员想审核新闻，可使用 [ACTION]list_pending_news\n");
            }
            if (msgLower.contains("处理问题") || msgLower.contains("待处理问题")) {
                guide.append("- 管理员想处理问题，可使用 [ACTION]list_pending_problems\n");
            }
        }

        if (guide.length() == 0) {
            guide.append("- 无匹配的操作引导\n");
        }

        return guide.toString();
    }

    private String buildConversationHistory(Integer conversationId) {
        StringBuilder history = new StringBuilder();
        history.append("【对话历史】\n");
        
        List<AIMessage> messages = messageDAO.findByConversationId(conversationId);
        int count = 0;
        for (AIMessage msg : messages) {
            if (count >= 10) break;
            String role = "user".equals(msg.getRole()) ? "用户" : "助手";
            history.append(role).append("：").append(msg.getContent()).append("\n");
            count++;
        }
        
        return history.toString();
    }

    public String getOperationTips(String userRole) {
        StringBuilder tips = new StringBuilder();
        tips.append("【可用操作提示】\n");

        if (ROLE_GUEST.equals(userRole)) {
            tips.append("你可以告诉我你想做什么，我会引导你完成操作。\n");
            tips.append("例如：我想报名参加活动、我想提交问题反馈 等\n");
        } else if (ROLE_MEMBER.equals(userRole)) {
            tips.append("你可以执行以下操作：\n");
            tips.append("- 报名活动：我帮你报名\n");
            tips.append("- 申请项目：我帮你申请\n");
            tips.append("- 提交新闻：我帮你投稿\n");
            tips.append("- 提交反馈：我帮你反馈\n");
            tips.append("- 查看我的活动/项目/群聊\n");
        } else if (ROLE_ADMIN.equals(userRole)) {
            tips.append("管理员可以执行以下操作：\n");
            tips.append("- 审核用户/活动/新闻/问题\n");
            tips.append("- 一键修复问题\n");
            tips.append("- 查看系统统计\n");
        }

        return tips.toString();
    }

    public Map<String, Object> executeAction(String actionType, Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (user == null) {
            result.put("message", "请先登录后再操作");
            return result;
        }

        String userRole = user.getRole();
        if (ROLE_ADMIN.equals(userRole) || ROLE_GUEST.equals(userRole)) {
            if (ROLE_GUEST.equals(userRole)) {
                result.put("message", "访客不能执行操作，请先登录");
            } else {
                result.put("message", "管理员请使用管理后台进行操作");
            }
            return result;
        }

        try {
            switch (actionType) {
                case "create_activity":
                    return executeCreateActivity(params, user);
                case "submit_news":
                    return executeSubmitNews(params, user);
                case "submit_feedback":
                    return executeSubmitFeedback(params, user);
                case "signup_activity":
                    return executeSignupActivity(params, user);
                case "view_my_activities":
                    return executeViewMyActivities(params, user);
                case "view_my_projects":
                    return executeViewMyProjects(params, user);
                case "view_my_groups":
                    return executeViewMyGroups(params, user);
                default:
                    result.put("message", "未知操作类型: " + actionType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("message", "执行出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeCreateActivity(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String title = params.get("name");
        String description = params.get("desc");
        String activityType = params.get("type");
        String location = params.get("location");
        String activityStartTime = params.get("start");
        String activityEndTime = params.get("end");
        String registrationStartTime = params.get("reg_start");
        String registrationEndTime = params.get("reg_end");
        String maxParticipantsStr = params.get("max");

        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "活动名称不能为空");
            return result;
        }

        Activity activity = new Activity();
        activity.setTitle(title.trim());
        activity.setDescription(description != null ? description.trim() : "");
        activity.setActivityType(activityType != null ? activityType : "其他");
        activity.setLocation(location != null ? location.trim() : "");
        activity.setCreatorId(user.getId());
        activity.setApprovalStatus("pending");
        activity.setStatus("upcoming");
        activity.setMaxParticipants(0);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            if (activityStartTime != null && !activityStartTime.isEmpty()) {
                activity.setActivityStartTime(sdf.parse(activityStartTime));
            }
            if (activityEndTime != null && !activityEndTime.isEmpty()) {
                activity.setActivityEndTime(sdf.parse(activityEndTime));
            }
            if (registrationStartTime != null && !registrationStartTime.isEmpty()) {
                activity.setRegistrationStartTime(sdf.parse(registrationStartTime));
            }
            if (registrationEndTime != null && !registrationEndTime.isEmpty()) {
                activity.setRegistrationEndTime(sdf.parse(registrationEndTime));
            }
            if (maxParticipantsStr != null && !maxParticipantsStr.isEmpty()) {
                activity.setMaxParticipants(Integer.parseInt(maxParticipantsStr));
            }
} catch (Exception e) {
        }

        boolean success = activityDAO.insert(activity);
        result.put("success", success);
        if (success) {
            result.put("message", "活动创建成功，等待管理员审核");
        } else {
            result.put("message", "活动创建失败，请重试");
        }
        return result;
    }

    private Map<String, Object> executeSubmitNews(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String title = params.get("title");
        String summary = params.get("summary");
        String type = params.get("type");
        String contentPath = params.get("content_path");

        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "新闻标题不能为空");
            return result;
        }

        News news = new News();
        news.setTitle(title.trim());
        news.setSummary(summary != null ? summary.trim() : "");
        news.setType(type != null ? type : "activity");
        news.setContentPath(contentPath != null ? contentPath : "");
        news.setAuthorId(user.getId());
        news.setStatus(0);

        try {
            boolean success = newsDAO.insert(news);
            result.put("success", success);
            if (success) {
                result.put("message", "新闻提交成功，等待管理员审核");
            } else {
                result.put("message", "新闻提交失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "提交出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeSubmitFeedback(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String title = params.get("title");
        String content = params.get("content");
        String contact = params.get("contact");

        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "反馈内容不能为空");
            return result;
        }

        ProblemReport report = new ProblemReport();
        report.setTitle(title != null ? title.trim() : "用户反馈");
        report.setContent(content.trim());
        report.setReporterName(user.getName());
        report.setReporterContact(contact != null ? contact.trim() : "");
        report.setReporterType(ProblemReport.REPORTER_TYPE_MEMBER);
        report.setUserId(user.getId());
        report.setCategory(ProblemReport.CATEGORY_UNVERIFIED);
        report.setStatus(ProblemReport.STATUS_PENDING);

        try {
            int id = problemReportDAO.insert(report);
            result.put("success", id > 0);
            if (id > 0) {
                result.put("message", "问题反馈已提交，我们会尽快处理");
            } else {
                result.put("message", "反馈提交失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "提交出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeSignupActivity(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String activityIdStr = params.get("activity_id");
        String activityName = params.get("activity_name");

        if (activityIdStr == null || activityIdStr.trim().isEmpty()) {
            if (activityName == null || activityName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请提供活动ID或活动名称");
                return result;
            }
            result.put("success", false);
            result.put("message", "请告诉我活动ID，或者我帮你搜索活动");
            return result;
        }

        try {
            int activityId = Integer.parseInt(activityIdStr);
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                result.put("success", false);
                result.put("message", "未找到该活动");
                return result;
            }

            if (!activity.isInRegistrationPeriod()) {
                result.put("success", false);
                result.put("message", "该活动当前不在报名时间内");
                return result;
            }

            if (registrationDAO.isRegistered(activityId, user.getId())) {
                result.put("success", false);
                result.put("message", "您已报名过该活动");
                return result;
            }

            boolean success = registrationDAO.register(activityId, user.getId());
            result.put("success", success);
            if (success) {
                result.put("message", "报名成功，等待管理员审核");
            } else {
                result.put("message", "报名失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "报名出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeViewMyActivities(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        List<Registration> registrations = registrationDAO.findByUserId(user.getId());
        result.put("success", true);
        result.put("type", "view_list");
        result.put("data", registrations);
        result.put("message", "您共报名了 " + registrations.size() + " 个活动");
        return result;
    }

    private Map<String, Object> executeViewMyProjects(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("type", "redirect");
        result.put("message", "正在跳转到我的项目页面...");
        result.put("redirectUrl", "/project?action=myApplications");
        return result;
    }

    private Map<String, Object> executeViewMyGroups(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("type", "redirect");
        result.put("message", "正在跳转到我的群聊页面...");
        result.put("redirectUrl", "/group?action=myGroups");
        return result;
    }

    public Map<String, String> parseActionParams(String actionString) {
        Map<String, String> params = new HashMap<>();
        if (actionString == null || actionString.isEmpty()) {
            return params;
        }

        String[] parts = actionString.split("\\|");
        for (String part : parts) {
            int eqIndex = part.indexOf("=");
            if (eqIndex > 0) {
                String key = part.substring(0, eqIndex).trim();
                String value = part.substring(eqIndex + 1).trim();
                params.put(key, value);
            }
        }
        return params;
    }
}