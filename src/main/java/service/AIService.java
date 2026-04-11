package service;

import dao.AIConversationDAO;
import dao.AIMessageDAO;
import dao.AIKnowledgeBaseDAO;
import dao.AIFaqStatisticsDAO;
import dao.ActivityDAO;
import dao.ActivityParticipantDAO;
import dao.NewsDAO;
import dao.ProblemReportDAO;
import dao.RegistrationDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import dao.UserGroupDAO;
import model.AIConversation;
import model.AIMessage;
import model.AIKnowledgeBase;
import model.AIFaqStatistics;
import model.Activity;
import model.News;
import model.Project;
import model.ProblemReport;
import model.Registration;
import model.User;
import util.AIClientUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
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
    private ActivityParticipantDAO activityParticipantDAO = new ActivityParticipantDAO();
    private NewsDAO newsDAO = new NewsDAO();
    private ProblemReportDAO problemReportDAO = new ProblemReportDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    private ProjectDAO projectDAO = new ProjectDAO();
    private UserDAO userDAO = new UserDAO();
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

        String detailResponse = checkAndHandleDetailQuery(userMessage, userRole, user);
        if (detailResponse != null) {
            AIMessage assistantMsg = new AIMessage();
            assistantMsg.setConversationId(conversation.getId());
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(detailResponse);
            assistantMsg.setCreatedAt(new Date());
            messageDAO.save(assistantMsg);
            recordQuestionStatistics(userMessage);
            return detailResponse;
        }

        String systemPrompt = buildSystemPrompt(userRole);
        String history = buildConversationHistory(conversation.getId());
        String userContent = history + "\n\n用户问题: " + userMessage;

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

    private String checkAndHandleDetailQuery(String userMessage, String userRole, User user) {
        String msg = userMessage.toLowerCase();
        
        boolean isAdmin = ROLE_ADMIN.equals(userRole);
        boolean isGuestOrMember = ROLE_GUEST.equals(userRole) || ROLE_MEMBER.equals(userRole);
        
        // 注意：活动查询已移至ACTION系统，不要在这里直接查询
        if (isAdmin || isGuestOrMember) {
            if (msg.contains("项目")) {
                return queryProjectList(userMessage);
            }
            
            if (msg.contains("新闻") || msg.contains("资讯") || msg.contains("动态") || msg.contains("成就")) {
                return queryNewsList(userMessage);
            }
        }
        
        if (isAdmin) {
            if ((msg.contains("活动") && (msg.contains("详情") || msg.contains("具体") || msg.contains("内容"))) || 
                (msg.matches(".*活动\\s*\\d+.*")) ||
                (msg.contains("activity_id"))) {
                Integer activityId = extractId(userMessage, "活动");
                if (activityId != null) {
                    return queryActivityDetail(activityId);
                }
            }
            
            if ((msg.contains("新闻") && (msg.contains("详情") || msg.contains("具体") || msg.contains("内容"))) ||
                (msg.matches(".*新闻\\s*\\d+.*")) ||
                (msg.contains("news_id"))) {
                Integer newsId = extractId(userMessage, "新闻");
                if (newsId != null) {
                    return queryNewsDetail(newsId);
                }
            }
            
            if ((msg.contains("用户") && (msg.contains("详情") || msg.contains("具体") || msg.contains("信息"))) ||
                (msg.matches(".*用户\\s*\\d+.*")) ||
                (msg.contains("user_id")) ||
                (msg.contains("成员") && (msg.contains("详情") || msg.contains("信息")))) {
                Integer userId = extractId(userMessage, "用户");
                if (userId != null) {
                    return queryUserDetail(userId);
                }
            }
        }
        
        return null;
    }

    private Integer extractId(String message, String type) {
        String msg = message.toLowerCase();
        String[] patterns = {
            type + "\\s*(\\d+)",
            type + "[:：]\\s*(\\d+)",
            "id\\s*[=:：]\\s*(\\d+)",
            "第\\s*(\\d+)\\s*" + type,
            "(\\d+)\\s*" + type
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(msg);
            if (m.find()) {
                try {
                    return Integer.parseInt(m.group(1));
                } catch (NumberFormatException e) {
                }
            }
        }
        return null;
    }

    private String queryActivityDetail(int activityId) {
        Activity activity = activityDAO.findById(activityId);
        if (activity == null) {
            return "未找到该活动信息";
        }
        
        int participantCount = activityParticipantDAO.getParticipantIdsByActivityId(activityId).size();
        
        StringBuilder sb = new StringBuilder();
        sb.append(activity.getTitle()).append("\n\n");
        
        if (activity.getActivityStartTime() != null) {
            sb.append("时间：").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(activity.getActivityStartTime()));
            if (activity.getActivityEndTime() != null) {
                sb.append(" - ").append(new SimpleDateFormat("HH:mm").format(activity.getActivityEndTime()));
            }
            sb.append("\n");
        }
        
        if (activity.getLocation() != null && !activity.getLocation().isEmpty()) {
            sb.append("地点：").append(activity.getLocation()).append("\n");
        }
        
        sb.append("类型：").append(activity.getActivityType()).append("\n");
        sb.append("状态：").append(activity.getComputedStatusText());
        sb.append(" | 报名：").append(activity.getRegistrationStatusText()).append("\n");
        
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0) {
            sb.append("人数：").append(participantCount).append("/").append(activity.getMaxParticipants()).append("人\n");
        }
        
        if (activity.getDescription() != null && !activity.getDescription().isEmpty()) {
            sb.append("\n").append(activity.getDescription()).append("\n");
        }
        
        return sb.toString();
    }

    private String queryNewsDetail(int newsId) {
        News news = newsDAO.findById(newsId);
        if (news == null) {
            return "未找到该新闻信息";
        }
        
        User author = userDAO.findById(news.getAuthorId());
        
        StringBuilder sb = new StringBuilder();
        sb.append(news.getTitle()).append("\n\n");
        
        if (author != null) {
            sb.append("作者：").append(author.getName());
        }
        
        if (news.getCreatedAt() != null) {
            sb.append(" | 发布时间：").append(new SimpleDateFormat("yyyy-MM-dd").format(news.getCreatedAt()));
        }
        sb.append("\n");
        
        if (news.getSummary() != null && !news.getSummary().isEmpty()) {
            sb.append("\n").append(news.getSummary()).append("\n");
        }
        
        return sb.toString();
    }

    private String queryUserDetail(int userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            return "未找到该用户信息";
        }
        
        List<Registration> registrations = registrationDAO.findByUserId(userId);
        List<ProblemReport> problems = problemReportDAO.findByUserId(userId);
        
        StringBuilder sb = new StringBuilder();
        sb.append(user.getName()).append(" 的信息\n\n");
        sb.append("用户名：").append(user.getUsername()).append("\n");
        sb.append("角色：").append(user.getRole()).append("\n");
        
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sb.append("邮箱：").append(user.getEmail()).append("\n");
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            sb.append("电话：").append(user.getPhone()).append("\n");
        }
        
        sb.append("账号：").append(user.getStatus() == 1 ? "正常" : "已禁用");
        if (user.getCreatedAt() != null) {
            sb.append(" | 注册于").append(new SimpleDateFormat("yyyy-MM-dd").format(user.getCreatedAt()));
        }
        sb.append("\n");
        
        sb.append("报名活动：").append(registrations.size()).append("个");
        sb.append(" | 反馈问题：").append(problems.size()).append("个\n");
        
        return sb.toString();
    }

    private String queryActivityList(String userMessage) {
        String msg = userMessage.toLowerCase();
        List<Activity> activities;
        
        if (msg.contains("讲座") || msg.contains("技术分享")) {
            activities = activityDAO.findByConditions(null, "LECTURE", null);
        } else if (msg.contains("培训") || msg.contains("工作坊") || msg.contains("workshop")) {
            activities = activityDAO.findByConditions(null, "TRAINING", null);
            if (activities == null || activities.isEmpty()) {
                activities = activityDAO.findByConditions(null, "WORKSHOP", null);
            }
        } else if (msg.contains("比赛") || msg.contains("竞赛") || msg.contains("编程挑战")) {
            activities = activityDAO.findByConditions(null, "COMPETITION", null);
        } else if (msg.contains("黑客")) {
            activities = activityDAO.findByConditions(null, "COMPETITION", null);
        } else if (msg.contains("分享") || msg.contains("讨论") || msg.contains("茶话")) {
            activities = activityDAO.findByConditions(null, "SEMINAR", null);
        } else if (msg.contains("项目")) {
            activities = activityDAO.findByConditions(null, "PROJECT_INTRO", null);
        } else {
            activities = activityDAO.findInRegistrationPeriod();
            if (activities == null || activities.isEmpty()) {
                activities = activityDAO.findAll();
            }
        }
        
        if (activities == null || activities.isEmpty()) {
            return "暂无相关活动信息";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(activities.size(), 10); i++) {
            Activity a = activities.get(i);
            String typeName = getActivityTypeName(a.getActivityType());
            sb.append(i + 1).append(". ").append(a.getTitle());
            if (typeName != null) {
                sb.append(" [").append(typeName).append("]");
            }
            if (a.getActivityStartTime() != null) {
                sb.append(" (").append(new SimpleDateFormat("MM-dd HH:mm").format(a.getActivityStartTime())).append(")");
            }
            if (a.getLocation() != null && !a.getLocation().isEmpty()) {
                sb.append(" @").append(a.getLocation());
            }
            sb.append("\n");
        }
        
        if (activities.size() > 10) {
            sb.append("还有").append(activities.size() - 10).append("个活动...");
        }
        
        return sb.toString();
    }
    
    private String getActivityTypeName(String type) {
        if (type == null) return null;
        switch (type) {
            case "LECTURE": return "讲座";
            case "SEMINAR": return "讨论会";
            case "COMPETITION": return "竞赛";
            case "TRAINING": return "培训";
            case "WORKSHOP": return "工作坊";
            case "TEA_PARTY": return "茶话会";
            case "PROJECT_INTRO": return "项目介绍";
            case "TEAM_BUILDING": return "团建";
            default: return type;
        }
    }

    private String queryProjectList(String userMessage) {
        List<Project> projects = projectDAO.findApprovedProjects();
        
        if (projects == null || projects.isEmpty()) {
            return "暂无相关项目信息";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("正在进行的技术项目：\n\n");
        
        for (int i = 0; i < Math.min(projects.size(), 10); i++) {
            Project p = projects.get(i);
            sb.append(i + 1).append(". ").append(p.getName());
            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                String desc = p.getDescription().length() > 30 ? p.getDescription().substring(0, 30) + "..." : p.getDescription();
                sb.append("\n   ").append(desc);
            }
            if (p.getStatus() != null) {
                sb.append(" [").append(p.getStatus()).append("]");
            }
            sb.append("\n");
        }
        
        if (projects.size() > 10) {
            sb.append("\n还有").append(projects.size() - 10).append("个项目...");
        }
        
        return sb.toString();
    }

    private String queryNewsList(String userMessage) {
        List<News> newsList = newsDAO.findAll();
        
        if (newsList == null || newsList.isEmpty()) {
            return "暂无相关新闻资讯";
        }
        
        String msg = userMessage.toLowerCase();
        String filterType = null;
        if (msg.contains("获奖") || msg.contains("成就") || msg.contains("achievement")) {
            filterType = "award";
        } else if (msg.contains("技术") && msg.contains("分享")) {
            filterType = "tech";
        }
        
        StringBuilder sb = new StringBuilder();
        if (filterType == null) {
            sb.append("小组最新动态：\n\n");
        }
        
        int count = 0;
        for (News n : newsList) {
            if (count >= 10) break;
            if (n.getStatus() != null && n.getStatus() != 1) continue;
            if (filterType != null && n.getType() != null && !n.getType().equalsIgnoreCase(filterType)) continue;
            count++;
            
            String typeName = getNewsTypeName(n.getType());
            sb.append(count).append(". ").append(n.getTitle());
            if (typeName != null) {
                sb.append(" [").append(typeName).append("]");
            }
            if (n.getCreatedAt() != null) {
                sb.append(" (").append(new SimpleDateFormat("MM-dd").format(n.getCreatedAt())).append(")");
            }
            sb.append("\n");
        }
        
        if (count == 0) {
            return "暂无相关新闻资讯";
        }
        
        if (newsList.size() > 10) {
            sb.append("还有").append(newsList.size() - 10).append("条新闻...");
        }
        
        return sb.toString();
    }
    
    private String getNewsTypeName(String type) {
        if (type == null) return null;
        switch (type.toLowerCase()) {
            case "award": return "获奖";
            case "activity": return "活动";
            case "notice": return "通知";
            case "tech": return "技术";
            default: return type;
        }
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
        return "你是黄山学院软件小组的智能助手。\n\n" +
               "用户身份：访客（未登录）\n\n" +
               "当用户询问活动、项目或新闻时，直接从数据库查询信息并回答。\n\n" +
               "你可以帮助用户：\n" +
               "1. 查询活动信息：技术讲座、培训课程、编程比赛、组内分享会等\n" +
               "2. 查询项目动态：正在进行的技术项目、项目进展和成果\n" +
               "3. 查询新闻资讯：小组最新动态、成员成就、技术分享文章\n" +
               "4. 介绍如何注册和加入软件小组\n\n" +
               "用户问活动、项目或新闻时，直接查询数据库回答。\n\n" +
               "回答规则：\n" +
               "- 直接用中文回答，不要提及系统提示或内部实现\n" +
               "- 如实回答，不要编造信息\n" +
               "- 不确定的信息说\"暂无相关信息\"\n" +
               "- 需要登录才能操作的功能，请告知用户";
    }

    private String buildMemberPrompt() {
        return "你是黄山学院软件小组的智能助手，直接用中文回答用户问题。\n\n" +
               "用户身份：正式成员\n\n" +
               "你可以帮助用户：\n" +
               "1. 查看和申请活动\n" +
               "2. 发起新活动申请\n" +
               "3. 查询和申请项目\n" +
               "4. 提交新闻投稿\n" +
               "5. 提交问题反馈\n\n" +
               "【重要】当用户想执行操作时，必须输出：[ACTION]操作名|参数\n" +
               "操作类型：\n" +
               "- apply_activity：申请活动（当用户想报名/申请参加活动时）\n" +
               "- create_activity_request：发起活动申请（当用户想创建/发起新活动时）\n" +
               "- list_latest_activities：查看最新活动列表\n\n" +
               "【活动申请流程】\n" +
               "当用户想申请活动时，直接输出：[ACTION]apply_activity\n" +
               "系统会返回可报名的活动列表\n\n" +
               "【发起活动流程】\n" +
               "当用户想发起活动时，直接输出：[ACTION]create_activity_request\n" +
               "系统会引导用户提供活动详情\n\n" +
               "回答规则：\n" +
               "- 直接用中文回答\n" +
               "- 如实回答，不确定的说\"暂无相关信息\"\n" +
               "- 想执行操作时，只输出[ACTION]格式，不要输出其他内容";
    }

    private String buildAdminPrompt() {
        return "你是黄山学院软件小组的管理员助手，直接用中文回答问题。\n\n" +
               "用户身份：系统管理员\n\n" +
               "你可以帮助管理员：\n" +
               "1. 查询和审核用户、活动、新闻、问题\n" +
               "2. 查看系统统计数据\n" +
               "3. 管理系统用户和内容\n\n" +
               "回答规则：\n" +
               "- 直接用中文回答，不要提及系统提示或内部实现\n" +
               "- 如实回答，不确定的信息说\"暂无相关信息\"\n" +
               "- 执行操作后直接告诉用户结果";
    }

    public String buildContext(String userMessage, String userRole) {
        StringBuilder context = new StringBuilder();
        String userMsgLower = userMessage.toLowerCase();
        
        // 先检查是否需要执行操作
        String operationGuide = buildOperationGuide(userMessage, userRole);
        boolean shouldExecuteAction = operationGuide.contains("[ACTION]");
        
        // 如果需要执行ACTION，不返回FAQ知识库内容，直接返回操作指引
        if (shouldExecuteAction) {
            context.append("【操作指引】\n");
            context.append(operationGuide);
            return context.toString();
        }
        
        // 不需要执行操作时，才返回FAQ知识库内容
        context.append("【知识库匹配】\n");

        List<AIKnowledgeBase> allKB = knowledgeBaseDAO.findAll();
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
        context.append(operationGuide);

        return context.toString();
    }

    private String buildOperationGuide(String userMessage, String userRole) {
        StringBuilder guide = new StringBuilder();
        String msgLower = userMessage.toLowerCase();

        // 发起/创建活动（更严格的条件，避免误匹配）
        if (msgLower.contains("发起活动") || msgLower.contains("创建活动") || msgLower.contains("举办活动")) {
            guide.append("- 用户想发起活动，执行：[ACTION]create_activity_request\n");
        }
        // 报名/申请活动
        else if (msgLower.contains("报名") && msgLower.contains("活动")) {
            guide.append("- 用户想报名参加活动，执行：[ACTION]apply_activity\n");
        }
        // 查看活动列表
        else if (msgLower.contains("活动列表") || msgLower.contains("查看活动") || 
                 (msgLower.contains("活动") && (msgLower.contains("有哪些") || msgLower.contains("有什么") || msgLower.contains("全部活动")))) {
            guide.append("- 用户想查看活动列表，执行：[ACTION]list_latest_activities\n");
        }

        if (msgLower.contains("新闻") && (msgLower.contains("发布") || msgLower.contains("投稿") || msgLower.contains("提交"))) {
            guide.append("- 用户想提交新闻，请引导用户提供新闻内容\n");
        }

        if ((msgLower.contains("问题") || msgLower.contains("反馈") || msgLower.contains("建议")) && !msgLower.contains("处理")) {
            guide.append("- 用户想提交反馈，请引导用户提供反馈内容\n");
        }

        if (msgLower.contains("我的活动") || msgLower.contains("报名记录")) {
            guide.append("- 用户想查看报名的活动\n");
        }

        if (msgLower.contains("我的项目") || msgLower.contains("项目申请")) {
            guide.append("- 用户想查看申请的项目\n");
        }

        if (msgLower.contains("我的群聊") || msgLower.contains("加入的群")) {
            guide.append("- 用户想查看加入的群聊\n");
        }

        if (ROLE_ADMIN.equals(userRole)) {
            if (msgLower.contains("审核用户") || msgLower.contains("新用户") || msgLower.contains("待审核用户")) {
                guide.append("- 管理员想查看待审核用户列表\n");
            }
            if (msgLower.contains("审核活动") || msgLower.contains("待审核活动")) {
                guide.append("- 管理员想查看待审核活动列表\n");
            }
            if (msgLower.contains("审核新闻") || msgLower.contains("待审核新闻")) {
                guide.append("- 管理员想查看待审核新闻列表\n");
            }
            if (msgLower.contains("处理问题") || msgLower.contains("待处理问题")) {
                guide.append("- 管理员想查看待处理问题列表\n");
            }
        }

        if (guide.length() == 0) {
            guide.append("- 请根据用户问题直接回答\n");
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

        if ("list_all_news".equals(actionType) || "recent_news".equals(actionType) || "list_activities".equals(actionType) || "list_latest_activities".equals(actionType)) {
            return executePublicQuery(actionType, params);
        }

        if (user == null) {
            result.put("message", "请先登录后再操作");
            return result;
        }

        String userRole = user.getRole();
        if (ROLE_GUEST.equals(userRole)) {
            result.put("message", "访客不能执行该操作，请先登录");
            return result;
        }

        if (ROLE_ADMIN.equals(userRole)) {
            return executeAdminAction(actionType, params, user);
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
                case "apply_activity":
                    return executeSignupActivity(params, user);
                case "create_activity_request":
                    return executeCreateActivityRequest(params, user);
                default:
                    result.put("message", "未知操作类型: " + actionType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("message", "执行出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executePublicQuery(String actionType, Map<String, String> params) {
        switch (actionType) {
            case "list_all_news":
                return executeListAllNews(params);
            case "recent_news":
                return executeRecentNews(params);
            case "list_activities":
                return executeListActivities(params);
            case "list_latest_activities":
                return executeListLatestActivities(params);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "未知查询类型: " + actionType);
        return result;
    }

    private Map<String, Object> executeListActivities(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        List<Activity> activities = activityDAO.findInRegistrationPeriod();
        
        if (activities.isEmpty()) {
            result.put("success", false);
            result.put("message", "当前没有可报名的活动");
            return result;
        }
        
        result.put("success", true);
        result.put("message", "以下是当前可报名的活动，请告诉我活动ID：");
        result.put("type", "table");
        result.put("columns", new String[]{"ID", "活动名称", "时间", "地点"});
        
        List<Map<String, Object>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        for (Activity a : activities) {
            Map<String, Object> row = new HashMap<>();
            row.put("ID", a.getId());
            row.put("活动名称", a.getTitle());
            row.put("时间", sdf.format(a.getActivityStartTime()));
            row.put("地点", a.getLocation() != null ? a.getLocation() : "-");
            data.add(row);
        }
        result.put("data", data);
        return result;
    }

    private Map<String, Object> executeListLatestActivities(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        List<Activity> activities = activityDAO.findAll();
        
        if (activities.isEmpty()) {
            result.put("success", false);
            result.put("message", "当前没有任何活动");
            return result;
        }
        
        result.put("success", true);
        result.put("message", "以下是最新活动列表：");
        result.put("type", "table");
        result.put("columns", new String[]{"ID", "活动名称", "类型", "时间", "状态"});
        
        List<Map<String, Object>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        int count = 0;
        for (Activity a : activities) {
            if (count >= 10) break;
            Map<String, Object> row = new HashMap<>();
            row.put("ID", a.getId());
            row.put("活动名称", a.getTitle());
            row.put("类型", a.getActivityType() != null ? a.getActivityType() : "-");
            row.put("时间", a.getActivityStartTime() != null ? sdf.format(a.getActivityStartTime()) : "-");
            row.put("状态", getActivityStatusText(a.getStatus()));
            data.add(row);
            count++;
        }
        result.put("data", data);
        return result;
    }

    private java.util.Date parseDateTime(String dateStr, SimpleDateFormat... formats) {
        for (SimpleDateFormat sdf : formats) {
            try {
                return sdf.parse(dateStr);
            } catch (Exception e) {
            }
        }
        return null;
    }

    private Map<String, Object> executeCreateActivityRequest(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String name = params.get("name");
        String description = params.get("description");
        String activityType = params.get("activity_type");
        String location = params.get("location");
        String startTime = params.get("start_time");
        String endTime = params.get("end_time");
        String regStartTime = params.get("reg_start");
        if (regStartTime == null) regStartTime = params.get("registration_start_time");
        String regEndTime = params.get("reg_end");
        if (regEndTime == null) regEndTime = params.get("registration_end_time");
        String maxParticipants = params.get("max_participants");
        
        // 引导用户提供信息
        List<String> missingInfo = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) missingInfo.add("活动名称");
        if (location == null || location.trim().isEmpty()) missingInfo.add("活动地点");
        if (startTime == null || startTime.trim().isEmpty()) missingInfo.add("开始时间（如：2026-04-15 09:00）");
        if (endTime == null || endTime.trim().isEmpty()) missingInfo.add("结束时间（如：2026-04-15 17:00）");
        if (regStartTime == null || regStartTime.trim().isEmpty()) missingInfo.add("报名开始时间（如：2026-04-10 00:00）");
        if (regEndTime == null || regEndTime.trim().isEmpty()) missingInfo.add("报名截止时间（如：2026-04-14 23:59）");
        
        if (!missingInfo.isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供以下信息来完成活动申请：\n" + String.join("、", missingInfo));
            result.put("need_more_info", true);
            return result;
        }
        
        try {
            Activity activity = new Activity();
            activity.setTitle(name.trim());
            activity.setDescription(description != null ? description.trim() : "");
            activity.setActivityType(activityType != null ? activityType : "OTHER");
            activity.setLocation(location.trim());
            activity.setMaxParticipants(maxParticipants != null && !maxParticipants.isEmpty() ? Integer.parseInt(maxParticipants) : null);
            activity.setStatus("upcoming");
            activity.setApprovalStatus("pending");
            activity.setCreatorId(user.getId());
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat sdfAlt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            
            activity.setActivityStartTime(parseDateTime(startTime.trim(), sdf, sdfAlt));
            activity.setActivityEndTime(parseDateTime(endTime.trim(), sdf, sdfAlt));
            if (regStartTime != null && !regStartTime.trim().isEmpty()) {
                activity.setRegistrationStartTime(parseDateTime(regStartTime.trim(), sdf, sdfAlt));
            }
            if (regEndTime != null && !regEndTime.trim().isEmpty()) {
                activity.setRegistrationEndTime(parseDateTime(regEndTime.trim(), sdf, sdfAlt));
            }
            
            boolean success = activityDAO.insert(activity);
            result.put("success", success);
            if (success) {
                result.put("message", "活动「" + name + "」申请已提交，等待管理员审核！");
            } else {
                result.put("message", "活动申请提交失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建活动出错: " + e.getMessage());
        }
        return result;
    }

    private String getActivityStatusText(String status) {
        if (status == null) return "-";
        switch (status) {
            case "upcoming": return "即将开始";
            case "ongoing": return "进行中";
            case "completed": return "已结束";
            case "canceled": return "已取消";
            default: return status;
        }
    }

    private Map<String, Object> executeAdminAction(String actionType, Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            switch (actionType) {
                case "list_pending_users":
                    return executeListPendingUsers();
                case "list_all_users":
                    return executeListAllUsers(params);
                case "approve_user":
                    return executeApproveUser(params);
                case "reject_user":
                    return executeRejectUser(params);
                case "list_pending_activities":
                    return executeListPendingActivities();
                case "list_all_activities":
                    return executeListAllActivities(params);
                case "approve_activity":
                    return executeApproveActivity(params);
                case "reject_activity":
                    return executeRejectActivity(params);
                case "view_participants":
                    return executeViewParticipants(params);
                case "list_pending_news":
                    return executeListPendingNews();
                case "list_all_news":
                    return executeListAllNews(params);
                case "approve_news":
                    return executeApproveNews(params);
                case "reject_news":
                    return executeRejectNews(params);
                case "list_pending_problems":
                    return executeListPendingProblems();
                case "list_all_problems":
                    return executeListAllProblems(params);
                case "classify_problem":
                    return executeClassifyProblem(params);
                case "resolve_problem":
                    return executeResolveProblem(params);
                case "fix_problem":
                    return executeFixProblem(params);
                case "statistics":
                    return executeStatistics();
                case "recent_activities":
                    return executeRecentActivities(params);
                case "recent_news":
                    return executeRecentNews(params);
                case "view_activity_detail":
                    return executeViewActivityDetail(params);
                case "view_news_detail":
                    return executeViewNewsDetail(params);
                case "view_user_detail":
                    return executeViewUserDetail(params);
                default:
                    result.put("message", "未知操作类型: " + actionType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("message", "执行出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeViewActivityDetail(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String activityIdStr = params.get("activity_id");
        if (activityIdStr == null || activityIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供活动ID (activity_id)");
            return result;
        }
        try {
            int activityId = Integer.parseInt(activityIdStr);
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                result.put("success", false);
                result.put("message", "未找到活动 ID " + activityId);
                return result;
            }
            int participantCount = activityParticipantDAO.getParticipantIdsByActivityId(activityId).size();
            activity.setCurrentParticipants(participantCount);
            activity.setRegistrationOpen(activity.isInRegistrationPeriod());
            result.put("success", true);
            result.put("type", "detail");
            result.put("data", activity);
            result.put("message", "活动详情获取成功");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的活动ID");
        }
        return result;
    }

    private Map<String, Object> executeViewNewsDetail(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String newsIdStr = params.get("news_id");
        if (newsIdStr == null || newsIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供新闻ID (news_id)");
            return result;
        }
        try {
            int newsId = Integer.parseInt(newsIdStr);
            News news = newsDAO.findById(newsId);
            if (news == null) {
                result.put("success", false);
                result.put("message", "未找到新闻 ID " + newsId);
                return result;
            }
            User author = userDAO.findById(news.getAuthorId());
            result.put("success", true);
            result.put("type", "detail");
            result.put("data", news);
            result.put("author", author);
            result.put("message", "新闻详情获取成功");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的新闻ID");
        }
        return result;
    }

    private Map<String, Object> executeViewUserDetail(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String userIdStr = params.get("user_id");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供用户ID (user_id)");
            return result;
        }
        try {
            int userId = Integer.parseInt(userIdStr);
            User user = userDAO.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "未找到用户 ID " + userId);
                return result;
            }
            List<Registration> registrations = registrationDAO.findByUserId(userId);
            List<ProblemReport> problems = problemReportDAO.findByUserId(userId);
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("user", user);
            userDetail.put("activityCount", registrations.size());
            userDetail.put("problemCount", problems.size());
            result.put("success", true);
            result.put("type", "detail");
            result.put("data", userDetail);
            result.put("message", "用户详情获取成功");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的用户ID");
        }
        return result;
    }

    private Map<String, Object> executeListPendingUsers() {
        Map<String, Object> result = new HashMap<>();
        List<User> users = userDAO.findByConditions(null, null, "0");
        result.put("success", true);
        result.put("type", "table");
        result.put("data", users);
        result.put("columns", new String[]{"id", "username", "name", "email", "phone", "createdAt"});
        result.put("message", "待审核用户共 " + users.size() + " 人");
        return result;
    }

    private Map<String, Object> executeListAllUsers(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String keyword = params.get("keyword");
        String role = params.get("role");
        String status = params.get("status");
        List<User> users = userDAO.findByConditions(keyword, role, status);
        result.put("success", true);
        result.put("type", "table");
        result.put("data", users);
        result.put("columns", new String[]{"id", "username", "name", "email", "phone", "role", "status", "createdAt"});
        result.put("message", "用户共 " + users.size() + " 人");
        return result;
    }

    private Map<String, Object> executeApproveUser(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String userIdStr = params.get("user_id");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供用户ID (user_id)");
            return result;
        }
        try {
            int userId = Integer.parseInt(userIdStr);
            boolean success = userDAO.updateStatus(userId, 1);
            result.put("success", success);
            result.put("message", success ? "用户 ID " + userId + " 已批准" : "批准失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的用户ID");
        }
        return result;
    }

    private Map<String, Object> executeRejectUser(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String userIdStr = params.get("user_id");
        String reason = params.get("reason");
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供用户ID (user_id)");
            return result;
        }
        try {
            int userId = Integer.parseInt(userIdStr);
            boolean success = userDAO.updateStatus(userId, -1);
            result.put("success", success);
            result.put("message", success ? "用户 ID " + userId + " 已拒绝" + (reason != null ? "，原因：" + reason : "") : "拒绝失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的用户ID");
        }
        return result;
    }

    private Map<String, Object> executeListPendingActivities() {
        Map<String, Object> result = new HashMap<>();
        List<Activity> activities = activityDAO.findPendingApproval();
        result.put("success", true);
        result.put("type", "table");
        result.put("data", activities);
        result.put("columns", new String[]{"id", "title", "activityType", "activityStartTime", "maxParticipants", "approvalStatus", "createdAt"});
        result.put("message", "待审核活动共 " + activities.size() + " 个");
        return result;
    }

    private Map<String, Object> executeListAllActivities(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        List<Activity> activities = activityDAO.findAll();
        result.put("success", true);
        result.put("type", "table");
        result.put("data", activities);
        result.put("columns", new String[]{"id", "title", "activityType", "activityStartTime", "location", "status", "approvalStatus"});
        result.put("message", "活动共 " + activities.size() + " 个");
        return result;
    }

    private Map<String, Object> executeApproveActivity(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String activityIdStr = params.get("activity_id");
        if (activityIdStr == null || activityIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供活动ID (activity_id)");
            return result;
        }
        try {
            int activityId = Integer.parseInt(activityIdStr);
            boolean success = activityDAO.approveActivity(activityId);
            result.put("success", success);
            result.put("message", success ? "活动 ID " + activityId + " 已批准" : "批准失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的活动ID");
        }
        return result;
    }

    private Map<String, Object> executeRejectActivity(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String activityIdStr = params.get("activity_id");
        String reason = params.get("reason");
        if (activityIdStr == null || activityIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供活动ID (activity_id)");
            return result;
        }
        try {
            int activityId = Integer.parseInt(activityIdStr);
            boolean success = activityDAO.rejectActivity(activityId);
            result.put("success", success);
            result.put("message", success ? "活动 ID " + activityId + " 已拒绝" + (reason != null ? "，原因：" + reason : "") : "拒绝失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的活动ID");
        }
        return result;
    }

    private Map<String, Object> executeViewParticipants(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String activityIdStr = params.get("activity_id");
        if (activityIdStr == null || activityIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供活动ID (activity_id)");
            return result;
        }
        try {
            int activityId = Integer.parseInt(activityIdStr);
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                result.put("success", false);
                result.put("message", "未找到活动 ID " + activityId);
                return result;
            }
            List<Integer> participantIds = activityParticipantDAO.getParticipantIdsByActivityId(activityId);
            List<User> participants = new java.util.ArrayList<>();
            for (Integer userId : participantIds) {
                User u = userDAO.findById(userId);
                if (u != null) {
                    participants.add(u);
                }
            }
            result.put("success", true);
            result.put("type", "participants");
            result.put("activity", activity);
            result.put("data", participants);
            result.put("columns", new String[]{"id", "username", "name", "email", "phone"});
            result.put("message", "活动「" + activity.getTitle() + "」共有 " + participants.size() + " 名参与者");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的活动ID");
        }
        return result;
    }

    private Map<String, Object> executeListPendingNews() {
        Map<String, Object> result = new HashMap<>();
        List<News> newsList = newsDAO.findByConditions(null, null, 0);
        result.put("success", true);
        result.put("type", "table");
        result.put("data", newsList);
        result.put("columns", new String[]{"编号", "标题", "类型", "摘要", "申请人", "发布时间"});
        result.put("message", "待审核新闻共 " + newsList.size() + " 条");
        return result;
    }

    private Map<String, Object> executeListAllNews(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String keyword = params.get("keyword");
        String type = params.get("type");
        List<News> newsList = newsDAO.findByConditions(keyword, type, null);
        result.put("success", true);
        result.put("type", "table");
        result.put("data", newsList);
        result.put("columns", new String[]{"编号", "标题", "类型", "摘要", "状态", "发布时间"});
        result.put("message", "新闻共 " + newsList.size() + " 条");
        return result;
    }

    private Map<String, Object> executeApproveNews(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String newsIdStr = params.get("news_id");
        if (newsIdStr == null || newsIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供新闻ID (news_id)");
            return result;
        }
        try {
            int newsId = Integer.parseInt(newsIdStr);
            boolean success = newsDAO.updateStatus(newsId, 1);
            result.put("success", success);
            result.put("message", success ? "新闻 ID " + newsId + " 已批准" : "批准失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的新闻ID");
        }
        return result;
    }

    private Map<String, Object> executeRejectNews(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String newsIdStr = params.get("news_id");
        String reason = params.get("reason");
        if (newsIdStr == null || newsIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供新闻ID (news_id)");
            return result;
        }
        try {
            int newsId = Integer.parseInt(newsIdStr);
            boolean success = newsDAO.updateStatus(newsId, -1);
            result.put("success", success);
            result.put("message", success ? "新闻 ID " + newsId + " 已拒绝" + (reason != null ? "，原因：" + reason : "") : "拒绝失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的新闻ID");
        }
        return result;
    }

    private Map<String, Object> executeListPendingProblems() {
        Map<String, Object> result = new HashMap<>();
        List<ProblemReport> problems = problemReportDAO.findByStatus(ProblemReport.STATUS_PENDING);
        result.put("success", true);
        result.put("type", "table");
        result.put("data", problems);
        result.put("columns", new String[]{"id", "title", "category", "status", "reporterName", "createdAt"});
        result.put("message", "待处理问题共 " + problems.size() + " 个");
        return result;
    }

    private Map<String, Object> executeListAllProblems(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String category = params.get("category");
        String status = params.get("status");
        List<ProblemReport> problems;
        if (status != null && !status.isEmpty()) {
            problems = problemReportDAO.findByStatus(status);
        } else if (category != null && !category.isEmpty()) {
            problems = problemReportDAO.findByCategory(category);
        } else {
            problems = problemReportDAO.findAll();
        }
        result.put("success", true);
        result.put("type", "table");
        result.put("data", problems);
        result.put("columns", new String[]{"id", "title", "category", "status", "reporterName", "createdAt"});
        result.put("message", "问题共 " + problems.size() + " 个");
        return result;
    }

    private Map<String, Object> executeClassifyProblem(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String problemIdStr = params.get("problem_id");
        String category = params.get("category");
        if (problemIdStr == null || problemIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供问题ID (problem_id)");
            return result;
        }
        if (category == null || category.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供分类 (category)");
            return result;
        }
        try {
            int problemId = Integer.parseInt(problemIdStr);
            boolean success = problemReportDAO.updateCategoryAndStatus(problemId, category, ProblemReport.STATUS_SOLVING, null, null);
            result.put("success", success);
            result.put("message", success ? "问题 ID " + problemId + " 已分类为 " + category : "分类失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的问题ID");
        }
        return result;
    }

    private Map<String, Object> executeResolveProblem(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String problemIdStr = params.get("problem_id");
        String solution = params.get("solution");
        if (problemIdStr == null || problemIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供问题ID (problem_id)");
            return result;
        }
        try {
            int problemId = Integer.parseInt(problemIdStr);
            boolean success = problemReportDAO.updateCategoryAndStatus(problemId, null, ProblemReport.STATUS_SOLVED, solution, null);
            result.put("success", success);
            result.put("message", success ? "问题 ID " + problemId + " 已标记为已解决" : "操作失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的问题ID");
        }
        return result;
    }

    private Map<String, Object> executeFixProblem(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String problemIdStr = params.get("problem_id");
        if (problemIdStr == null || problemIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供问题ID (problem_id)");
            return result;
        }
        try {
            int problemId = Integer.parseInt(problemIdStr);
            boolean success = problemReportDAO.updateCategoryAndStatus(problemId, null, ProblemReport.STATUS_SOLVED, "AI助手一键修复", null);
            result.put("success", success);
            result.put("message", success ? "问题 ID " + problemId + " 已一键修复" : "修复失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的问题ID");
        }
        return result;
    }

    private Map<String, Object> executeStatistics() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_users", userDAO.findAll().size());
        stats.put("pending_users", userDAO.findByConditions(null, null, "0").size());
        stats.put("total_activities", activityDAO.findAll().size());
        stats.put("pending_activities", activityDAO.findPendingApproval().size());
        stats.put("total_news", newsDAO.findAll().size());
        stats.put("pending_news", newsDAO.findByConditions(null, null, 0).size());
        stats.put("total_problems", problemReportDAO.findAll().size());
        stats.put("pending_problems", problemReportDAO.findByStatus(ProblemReport.STATUS_PENDING).size());
        result.put("success", true);
        result.put("type", "statistics");
        result.put("data", stats);
        result.put("message", "系统统计数据获取成功");
        return result;
    }

    private Map<String, Object> executeRecentActivities(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String limitStr = params.get("limit");
        int limit = 10;
        if (limitStr != null && !limitStr.isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
            }
        }
        List<Activity> activities = activityDAO.findAll();
        result.put("success", true);
        result.put("type", "table");
        result.put("data", activities.subList(0, Math.min(activities.size(), limit)));
        result.put("columns", new String[]{"id", "title", "activityStartTime", "status", "approvalStatus"});
        result.put("message", "最近 " + Math.min(activities.size(), limit) + " 个活动");
        return result;
    }

    private Map<String, Object> executeRecentNews(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String limitStr = params.get("limit");
        int limit = 10;
        if (limitStr != null && !limitStr.isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
            }
        }
        List<News> newsList = newsDAO.findAll();
        result.put("success", true);
        result.put("type", "table");
        result.put("data", newsList.subList(0, Math.min(newsList.size(), limit)));
        result.put("columns", new String[]{"编号", "标题", "类型", "发布时间"});
        result.put("message", "最近 " + Math.min(newsList.size(), limit) + " 条新闻");
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
                // 没有提供活动名称，返回当前可报名的活动列表
                List<Activity> availableActivities = activityDAO.findInRegistrationPeriod();
                if (availableActivities.isEmpty()) {
                    result.put("success", false);
                    result.put("message", "当前没有可报名的活动");
                    return result;
                }
                
                // 返回表格格式的活动列表
                result.put("success", true);
                result.put("message", "以下是当前可报名的活动，请告诉我活动ID进行报名：");
                result.put("type", "table");
                result.put("columns", new String[]{"ID", "活动名称", "类型", "时间", "地点"});
                
                List<Map<String, Object>> data = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                for (Activity a : availableActivities) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("ID", a.getId());
                    row.put("活动名称", a.getTitle());
                    row.put("类型", getActivityTypeName(a.getActivityType()));
                    row.put("时间", a.getActivityStartTime() != null ? sdf.format(a.getActivityStartTime()) : "-");
                    row.put("地点", a.getLocation() != null ? a.getLocation() : "-");
                    data.add(row);
                }
                result.put("data", data);
                return result;
            }
            // 搜索匹配名称的活动
            List<Activity> matchedActivities = activityDAO.findByConditions(activityName, null, null);
            List<Activity> availableActivities = new ArrayList<>();
            for (Activity a : matchedActivities) {
                if (a.isInRegistrationPeriod()) {
                    availableActivities.add(a);
                }
            }
            if (availableActivities.isEmpty()) {
                result.put("success", false);
                result.put("message", "未找到可报名的活动「" + activityName + "」，请确认活动名称或查看当前可报名活动");
                return result;
            }
            
            // 返回表格格式
            result.put("success", true);
            result.put("message", "找到以下可报名的活动，请告诉我活动ID进行报名：");
            result.put("type", "table");
            result.put("columns", new String[]{"ID", "活动名称", "类型", "时间", "地点"});
            
            List<Map<String, Object>> data = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            for (Activity a : availableActivities) {
                Map<String, Object> row = new HashMap<>();
                row.put("ID", a.getId());
                row.put("活动名称", a.getTitle());
                row.put("类型", getActivityTypeName(a.getActivityType()));
                row.put("时间", a.getActivityStartTime() != null ? sdf.format(a.getActivityStartTime()) : "-");
                row.put("地点", a.getLocation() != null ? a.getLocation() : "-");
                data.add(row);
            }
            result.put("data", data);
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