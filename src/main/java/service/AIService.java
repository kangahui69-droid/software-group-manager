package service;

import dao.AIConversationDAO;
import dao.AIMessageDAO;
import dao.AIKnowledgeBaseDAO;
import dao.AIFaqStatisticsDAO;
import dao.ActivityDAO;
import dao.ActivityGroupDAO;
import dao.GroupMemberDAO;
import dao.ActivityParticipantDAO;
import dao.AwardDAO;
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
import model.ActivityGroup;
import model.GroupMember;
import model.Award;
import model.News;
import model.Project;
import model.ProblemReport;
import model.Registration;
import model.User;
import util.AIClientUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private ActivityGroupDAO groupDAO = new ActivityGroupDAO();
    private GroupMemberDAO groupMemberDAO = new GroupMemberDAO();
    private ActivityParticipantDAO activityParticipantDAO = new ActivityParticipantDAO();
    private NewsDAO newsDAO = new NewsDAO();
    private AwardDAO awardDAO = new AwardDAO();
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

        if (msg.contains("介绍") || msg.contains("加入") || msg.contains("怎么加入") || msg.contains("如何加入")) {
            if (msg.contains("小组") || msg.contains("软件") || msg.contains("团队")) {
                return executeGetOrganizationInfo(new HashMap<>()).get("message").toString();
            }
        }

        if (msg.contains("组织") || msg.contains("机构") || msg.contains("我们是谁")) {
            return executeGetOrganizationInfo(new HashMap<>()).get("message").toString();
        }

        boolean isAdmin = ROLE_ADMIN.equals(userRole);

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
        return "你是黄山学院软件小组的智能助手，直接用中文回答用户问题。\n\n" +
               "用户身份：访客（未登录）\n\n" +
               "【核心原则】\n" +
               "1. 绝对不要编造活动、新闻、项目等信息！\n" +
               "2. 查询必须通过[ACTION]从数据库获取真实数据\n" +
               "3. 遇到不确定的情况，先执行ACTION查询再回答\n" +
               "4. 查询结果要适当说明，帮助用户理解\n\n" +
               "你可以帮助用户：\n" +
               "1. 介绍软件小组和回答如何加入的问题（无需查询数据库，直接回答）\n" +
               "2. 查询活动信息：技术讲座、培训课程、编程比赛、组内分享会等\n" +
               "3. 查询新闻资讯：小组最新动态、成员成就、技术分享文章\n" +
               "4. 介绍如何注册和加入软件小组\n\n" +
               "【关于软件小组的介绍 - 直接回答，无需查询】\n" +
               "黄山学院软件小组是一个由计算机相关专业学生组成的学术组织，致力于软件开发学习与实践。\n" +
               "小组提供以下服务：\n" +
               "1. 学习资源分享\n" +
               "2. 项目实战锻炼\n" +
               "3. 技术交流活动\n" +
               "4. 竞赛指导培训\n" +
               "5. 就业经验分享\n\n" +
               "【加入方式】\n" +
               "访问网站注册账号，管理员审核通过后即可成为正式成员。\n\n" +
               "【操作触发规则】\n" +
               "当用户询问以下内容时，必须先执行ACTION查询：\n" +
               "- 活动相关：'查看活动'、'有哪些活动'、'活动列表'、'最近活动'、'技术讲座'、'培训'、'分享会' → [ACTION]list_activities\n" +
               "- 新闻相关：'查看新闻'、'新闻列表'、'资讯'、'动态'、'公告'、'通知'、'消息' → [ACTION]list_all_news\n" +
               "- 项目相关：'查看项目'、'项目列表'、'有哪些项目'、'项目动态' → [ACTION]list_public_projects\n\n" +
               "【重要】\n" +
               "- 想执行操作时，只输出[ACTION]格式\n" +
               "- 查询结果为\"暂无\"时，要友好地说明：'目前没有可报名的活动'或'暂无相关新闻'\n" +
               "- 无法匹配时，请用户详细描述问题\n" +
               "- 绝对不要编造信息，不确定时说\"暂无相关信息\"";
    }

    private String buildMemberPrompt() {
        return "你是黄山学院软件小组的智能助手，直接用中文回答用户问题。\n\n" +
               "用户身份：正式成员\n\n" +
               "【核心原则】\n" +
               "1. 绝对不要编造活动、新闻、项目、奖项等信息！\n" +
               "2. 所有列表查询必须通过[ACTION]从数据库获取真实数据\n" +
               "3. 遇到不确定的情况，先执行ACTION查询再回答\n" +
               "4. 查询结果要适当说明，帮助用户理解\n\n" +
               "【操作触发规则】\n" +
                "当用户提到以下关键词时，直接输出对应ACTION：\n" +
                "- '查看活动'、'有哪些活动'、'活动列表'、'最近活动'、'技术讲座'、'培训课程'、'分享会'、'比赛' → [ACTION]list_latest_activities\n" +
                "- '报名'、'参加'、'加入' + '活动' → [ACTION]apply_activity\n" +
                "- '发起'、'创建'、'举办' + '活动' → [ACTION]create_activity_request\n" +
                "- '查看新闻'、'新闻列表'、'有哪些新闻'、'资讯'、'动态'、'公告'、'通知'、'消息' → [ACTION]list_all_news\n" +
                "- '发布新闻'、'投稿'、'提交新闻' → [ACTION]submit_news\n" +
                "- '提交反馈'、'问题反馈'、'提建议' → [ACTION]submit_feedback\n" +
                "- '我的活动'、'报名记录'、'我报名的活动'、'已报名活动' → [ACTION]view_my_activities\n" +
                "- '我的项目'、'项目申请'、'我参与的项目'、'个人项目' → [ACTION]view_my_projects\n" +
                "- '我的奖项'、'获奖记录'、'我获得的奖项'、'个人奖项'、'获奖情况' → [ACTION]list_my_awards\n" +
                "- '奖项申请'、'申请奖项'、'申报奖项'、'获奖申请' → [ACTION]submit_award\n" +
                "- '查看项目'、'项目列表'、'有哪些项目'、'项目动态'、'项目进展' → [ACTION]list_public_projects\n" +
                "- '发起项目'、'创建项目'、'新建项目' → [ACTION]create_project_request\n\n" +
                "【重要】\n" +
                "- 想执行操作时，只输出[ACTION]格式，不要输出其他内容\n" +
                "- 查询结果为\"暂无\"时，要友好地说明：'目前没有可报名的活动'或'暂无相关新闻'\n" +
                "- 无法匹配时，反问用户想做什么操作\n" +
                "- 不确定就说\"暂无相关信息\"，不要编造";
    }

    private String buildAdminPrompt() {
        return "你是黄山学院软件小组的管理员助手，直接用中文回答问题。\n\n" +
               "用户身份：系统管理员\n\n" +
               "【核心原则】\n" +
               "1. 绝对不要编造活动、新闻、项目、用户等信息！\n" +
               "2. 所有列表查询必须通过[ACTION]从数据库获取真实数据\n" +
               "3. 遇到不确定的情况，先执行ACTION查询再回答\n" +
               "4. 查询结果要适当说明，帮助理解数据情况\n\n" +
               "你可以帮助管理员：\n" +
               "1. 查询和审核用户、活动、新闻、问题、奖项\n" +
               "2. 查看系统统计数据\n" +
               "3. 管理系统用户和内容\n\n" +
               "【操作触发规则】\n" +
                "当用户提到以下内容时，必须执行对应ACTION：\n" +
                "- '查看活动'、'活动列表'、'待审核活动'、'最新活动' → [ACTION]list_activities / [ACTION]list_latest_activities\n" +
                "- '审核活动'、'待处理活动' → 执行活动审核相关查询\n" +
                "- '审核用户'、'新用户'、'待审核用户'、'招新' → [ACTION]list_pending_users\n" +
                "- '审核新闻'、'待审核新闻'、'待审核资讯' → [ACTION]list_pending_news\n" +
                "- '审核奖项'、'待审核奖项'、'获奖审核' → [ACTION]list_all_awards\n" +
                "- '处理问题'、'待处理问题'、'bug' → [ACTION]list_pending_problems\n" +
                "- '查看新闻'、'新闻列表'、'资讯'、'动态'、'公告' → [ACTION]list_all_news\n" +
                "- '查看项目'、'项目列表'、'项目动态' → [ACTION]list_all_projects\n" +
                "- '查看成员'、'成员列表'、'用户列表' → [ACTION]list_all_users\n" +
                "- '数据统计'、'系统统计'、'统计概览' → [ACTION]statistics\n\n" +
                "【重要】\n" +
                "- 想执行操作时，只输出[ACTION]格式\n" +
                "- 查询结果为\"暂无\"时，要说明：'暂无待审核活动'或'暂无相关数据'\n" +
                "- 无法匹配时，请管理员详细描述想做什么\n" +
                "- 绝对不要编造信息，不确定时说\"暂无相关信息\"";
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

        // ============================================
        // 活动相关操作 - 扩展近义词
        // ============================================
        
        // 发起/创建活动 - 近义词
        boolean isCreateActivity = containsAny(msgLower, new String[]{
            "发起活动", "创建活动", "举办活动", "组织活动", "建立活动",
            "发起个活动", "创建个活动", "举办个活动", "组织个活动", "开个活动",
            "我想办活动", "要办活动", "想搞活动", "想创建一个", "想发起一个",
            "怎样发起活动", "如何举办活动", "怎么创建活动", "活动怎么办", "活动怎么做",
            "帮我创建活动", "帮我发起活动", "帮我举办活动", "帮我组织活动",
            "我要创建活动", "我要发起活动", "我想创建活动", "我想发起活动",
            "创建一个活动", "发起一个活动", "举办一个活动", "新建一个活动"
        }) && !containsAny(msgLower, new String[]{"查看", "查询", "看", "找", "列表"});
        
        if (isCreateActivity) {
            guide.append("- 用户想发起新活动，执行：[ACTION]create_activity_request\n");
        }
        
        // 报名/申请活动 - 近义词
        boolean isApplyActivity = containsAny(msgLower, new String[]{
            "报名活动", "申请活动", "参加活动", "参与活动", "加入活动",
            "活动报名", "活动申请", "活动参加", "活动参与", "活动加入",
            "报名参加活动", "申请参加活动", "参加报名活动", "加入报名活动",
            "我要报名活动", "我要参加活动", "我想报名活动", "我想参加活动",
            "帮我报名", "帮我参加", "帮我申请活动", "帮我报名活动",
            "想报名活动", "想参加活动", "想加入活动", "想参与活动",
            "要报名活动", "要去参加活动", "想报个活动",
            "怎么报名活动", "如何报名活动", "怎么参加活动", "如何参加活动"
        }) && !containsAny(msgLower, new String[]{"查看", "查询", "看", "找", "列表", "有什么", "有哪些", "所有"});
        
        if (isApplyActivity) {
            guide.append("- 用户想报名参加活动，执行：[ACTION]apply_activity\n");
        }
        
        // 查看活动列表 - 近义词
        boolean isListActivities = containsAny(msgLower, new String[]{
            "活动列表", "查看活动", "查询活动", "所有活动", "全部活动",
            "活动有哪些", "活动有什么", "有些什么活动", "有什么活动", "有哪些活动",
            "活动信息", "活动详情", "查看活动列表", "查询活动列表",
            "我想看活动", "想查看活动", "找活动", "看看活动",
            "最近的活動", "最新活动", "近期活动", "即将开始的活动",
            "有没有活动", "有没有活动可以参加", "现在有什么活动",
            "显示活动列表", "浏览活动列表", "获取活动列表"
        }) && !containsAny(msgLower, new String[]{"发起", "创建", "举办", "组织", "申请", "报名", "参加", "加入"});
        
        if (isListActivities) {
            guide.append("- 用户想查看活动列表，执行：[ACTION]list_latest_activities\n");
        }

        // ============================================
        // 新闻相关操作
        // ============================================
        boolean isSubmitNews = containsAny(msgLower, new String[]{
            "发布新闻", "提交新闻", "投稿新闻", "发表新闻", "上传新闻",
            "我想发布新闻", "想发布新闻", "要发布新闻", "想投稿",
            "新闻发布", "新闻投稿", "新闻提交", "发布个新闻",
            "怎么发布新闻", "如何发布新闻",
            "发布", "投稿", "提交", "发表"
        }) && !containsAny(msgLower, new String[]{"查看", "查询", "看", "审核"});
        
        if (isSubmitNews) {
            guide.append("- 用户想发布新闻，执行：[ACTION]submit_news\n");
        }
        
        boolean isListNews = containsAny(msgLower, new String[]{
            "新闻列表", "查看新闻", "查询新闻", "所有新闻", "全部新闻",
            "新闻有哪些", "新闻有什么", "有些什么新闻", "有什么新闻", "有哪些新闻",
            "新闻资讯", "新闻动态", "最新新闻", "最近新闻",
            "看看新闻", "查看新闻", "找新闻"
        });
        
        if (isListNews && guide.length() == 0) {
            guide.append("- 用户想查看新闻列表，执行：[ACTION]list_all_news\n");
        }

        // ============================================
        // 反馈/问题相关
        // ============================================
        boolean isSubmitFeedback = containsAny(msgLower, new String[]{
            "提交反馈", "问题反馈", "提交问题", "提交建议", "意见反馈",
            "我要反馈", "想反馈", "要反馈", "有建议", "提个建议",
            "反馈问题", "反馈意见", "问题建议", "bug", "报错", "错误",
            "遇到问题", "有问题", "提问题", "提意见",
            "怎么反馈", "如何反馈", "如何建议"
        }) && !containsAny(msgLower, new String[]{"查看", "查询", "看", "审核", "处理"});
        
        if (isSubmitFeedback) {
            guide.append("- 用户想提交反馈，执行：[ACTION]submit_feedback\n");
        }

        // ============================================
        // 我的相关查询
        // ============================================
        if (containsAny(msgLower, new String[]{
            "我的活动", "我的报名", "报名记录", "已报名活动", 
            "我报名的活动", "我参加的活动", "我的活动记录",
            "查看我的活动"
        })) {
            guide.append("- 用户想查看报名的活动，执行：[ACTION]view_my_activities\n");
        }

        if (containsAny(msgLower, new String[]{
            "我的项目", "我的项目申请", "项目申请记录", "已申请项目",
            "我申请的项目", "我创建的项目", "我的项目列表"
        })) {
            guide.append("- 用户想查看项目，执行：[ACTION]view_my_projects\n");
        }

        if (containsAny(msgLower, new String[]{
            "我的群聊", "我的群", "加入的群", "群聊记录",
            "我加入的群", "我的聊天群", "群消息"
        })) {
            guide.append("- 用户想查看群聊，执行：[ACTION]view_my_groups\n");
        }
        
        // ============================================
        // 奖项相关
        // ============================================
        if (containsAny(msgLower, new String[]{
            "申请奖项", "提交奖项", "获奖申请", "申报奖项",
            "我想申请奖项", "要申请奖项", "想申报奖项",
            "奖项申请", "申请个奖项"
        })) {
            guide.append("- 用户想申请奖项，执行：[ACTION]submit_award\n");
        }
        
        if (containsAny(msgLower, new String[]{
            "我的奖项", "我的获奖", "获奖记录", "已获奖项",
            "我获得的奖项", "我的奖项列表"
        })) {
            guide.append("- 用户想查看我的奖项，执行：[ACTION]list_my_awards\n");
        }

        // ============================================
        // 项目相关
        // ============================================
        if (containsAny(msgLower, new String[]{
            "项目列表", "查看项目", "查询项目", "所有项目", "全部项目",
            "项目有哪些", "项目有什么", "有些什么项目", "有什么项目",
            "有哪些项目", "项目信息", "项目详情"
        }) || (msgLower.contains("项目") && containsAny(msgLower, new String[]{"查看", "查询", "看", "找", "列表"}))) {
            guide.append("- 用户想查看项目列表，执行：[ACTION]list_public_projects\n");
        }
        
        boolean isCreateProject = containsAny(msgLower, new String[]{
            "发起项目", "创建项目", "申请项目", "新建项目",
            "我想发起项目", "想创建项目", "要申请项目",
            "项目申请", "申请个项目"
        }) && !containsAny(msgLower, new String[]{"查看", "查询", "看", "加入"});
        
        if (isCreateProject) {
            guide.append("- 用户想发起项目，执行：[ACTION]create_project_request\n");
        }

        // ============================================
        // 管理员操作
        // ============================================
        if (ROLE_ADMIN.equals(userRole)) {
            if (containsAny(msgLower, new String[]{
                "审核用户", "新用户", "待审核用户", "用户审核",
                "审核新用户", "查看新用户", "待处理用户"
            })) {
                guide.append("- 管理员想查看待审核用户列表\n");
            }
            if (containsAny(msgLower, new String[]{
                "审核活动", "待审核活动", "活动审核",
                "待批准活动", "待处理活动"
            })) {
                guide.append("- 管理员想查看待审核活动列表\n");
            }
            if (containsAny(msgLower, new String[]{
                "审核新闻", "待审核新闻", "新闻审核",
                "待批准新闻", "待处理新闻"
            })) {
                guide.append("- 管理员想查看待审核新闻列表\n");
            }
            if (containsAny(msgLower, new String[]{
                "处理问题", "待处理问题", "问题处理",
                "待审核问题", "待回复问题"
            })) {
                guide.append("- 管理员想查看待处理问题列表\n");
            }
            if (containsAny(msgLower, new String[]{
                "审核奖项", "待审核奖项", "奖项审核",
                "奖项申请审核", "待批准奖项"
            })) {
                guide.append("- 管理员想查看待审核奖项列表\n");
            }
        }

        // ============================================
        // 无法匹配时的处理
        // ============================================
        if (guide.length() == 0) {
            // 告诉AI尝试从用户问题中提取意图，或请用户澄清
            guide.append("- 【重要】无法确定用户意图时，请按以下规则处理：\n");
            guide.append("  1. 如果用户问题涉及上述操作关键词（如'活动'、'新闻'、'项目'、'奖项'等），尝试推断用户是想查询还是操作\n");
            guide.append("  2. 如果用户只是想了解信息（如'活动是什么'、'项目是什么'），请直接回答\n");
            guide.append("  3. 如果用户想执行操作但无法确定操作类型，请回复：\"您想对[活动/新闻/项目/奖项]做什么操作？查看详情、申请、还是其他？\"\n");
            guide.append("  4. 如果完全无法理解用户意图，请回复：\"抱歉，我没有理解您的问题。请您详细描述一下您想做什么？\"\n");
            guide.append("  5. 绝对不要胡乱编造活动、新闻、项目等信息，必须从数据库查询或如实说明无相关信息\n");
        }

        return guide.toString();
    }
    
    /**
     * 检查字符串是否包含任意一个关键词
     */
    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
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

        if ("list_all_news".equals(actionType) || "recent_news".equals(actionType) || "list_activities".equals(actionType) || "list_latest_activities".equals(actionType) || "get_organization_info".equals(actionType)) {
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
                    recordQuestionStatistics("创建活动");
                    return executeCreateActivity(params, user);
                case "submit_news":
                    recordQuestionStatistics("提交新闻");
                    return executeSubmitNews(params, user);
                case "submit_feedback":
                    recordQuestionStatistics("提交反馈");
                    return executeSubmitFeedback(params, user);
                case "signup_activity":
                    recordQuestionStatistics("报名活动");
                    return executeSignupActivity(params, user);
                case "view_my_activities":
                    recordQuestionStatistics("查看我的活动");
                    return executeViewMyActivities(params, user);
                case "view_my_projects":
                    recordQuestionStatistics("查看我的项目");
                    return executeViewMyProjects(params, user);
                case "view_my_groups":
                    recordQuestionStatistics("查看我的群组");
                    return executeViewMyGroups(params, user);
                case "list_my_groups":
                    recordQuestionStatistics("列出我的群组");
                    return executeListMyGroups(params, user);
                case "view_group_detail":
                    recordQuestionStatistics("查看群组详情");
                    return executeViewGroupDetail(params, user);
                case "join_group":
                    recordQuestionStatistics("加入群组");
                    return executeJoinGroup(params, user);
                case "leave_group":
                    recordQuestionStatistics("退出群组");
                    return executeLeaveGroup(params, user);
                case "list_group_members":
                    recordQuestionStatistics("列出群组成员");
                    return executeListGroupMembers(params, user);
                case "list_activities":
                    recordQuestionStatistics("查看活动");
                    return executeListActivities(params, user);
                case "list_latest_activities":
                    recordQuestionStatistics("最新活动");
                    return executeListLatestActivities(params);
                case "apply_activity":
                    recordQuestionStatistics("申请活动");
                    return executeSignupActivity(params, user);
                case "create_activity_request":
                    recordQuestionStatistics("创建活动请求");
                    return executeCreateActivityRequest(params, user);
                case "list_all_projects":
                    recordQuestionStatistics("列出所有项目");
                    return executeListAllProjects(params, user);
                case "list_public_projects":
                    recordQuestionStatistics("列出公开项目");
                    return executeListPublicProjects(params, user);
                case "create_project_request":
                    recordQuestionStatistics("创建项目请求");
                    return executeCreateProjectRequest(params, user);
                case "submit_award":
                    recordQuestionStatistics("提交奖项");
                    return executeSubmitAward(params, user);
                case "list_my_awards":
                    recordQuestionStatistics("查看我的奖项");
                    return executeListMyAwards(params, user);
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
        String questionForStats = null;
        switch (actionType) {
            case "list_all_news":
                questionForStats = "查看新闻";
                break;
            case "recent_news":
                questionForStats = "最新新闻";
                break;
            case "list_activities":
                questionForStats = "查看活动";
                break;
            case "list_latest_activities":
                questionForStats = "最新活动";
                break;
            case "get_organization_info":
                questionForStats = "小组介绍";
                break;
        }
        
        if (questionForStats != null) {
            recordQuestionStatistics(questionForStats);
        }
        
        switch (actionType) {
            case "list_all_news":
                return executeListAllNews(params);
            case "recent_news":
                return executeRecentNews(params);
            case "list_activities":
                return executeListActivities(params, null);
            case "list_latest_activities":
                return executeListLatestActivities(params);
            case "get_organization_info":
                return executeGetOrganizationInfo(params);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "未知查询类型: " + actionType);
        return result;
    }

    private Map<String, Object> executeListActivities(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        List<Activity> activities = activityDAO.findInRegistrationPeriod();
        
        System.out.println("[AIService] executeListActivities - found " + activities.size() + " activities");
        for (Activity a : activities) {
            System.out.println("  Activity: id=" + a.getId() + ", title=" + a.getTitle() + ", startTime=" + a.getActivityStartTime());
        }
        
        if (activities.isEmpty()) {
            result.put("success", true);
            result.put("type", "table");
            result.put("message", "当前没有可报名的活动");
            result.put("columns", new String[]{"ID", "活动名称", "时间", "地点"});
            result.put("data", new ArrayList<>());
            return result;
        }
        
        result.put("success", true);
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
        
        if (user == null) {
            result.put("message", "登陆后可进行报名");
        } else {
            result.put("message", "以上是当前可报名的活动，请告诉我活动ID，我为您一键报名：");
        }
        return result;
    }

    private Map<String, Object> executeListLatestActivities(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        List<Activity> activities = activityDAO.findAll();
        
        if (activities.isEmpty()) {
            result.put("success", true);
            result.put("type", "table");
            result.put("message", "当前没有任何活动");
            result.put("columns", new String[]{"ID", "活动名称", "类型", "时间", "状态"});
            result.put("data", new ArrayList<>());
            return result;
        }
        
        result.put("success", true);
        result.put("message", "以上是最新活动列表：");
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
            row.put("类型", getActivityTypeText(a.getActivityType()));
            row.put("时间", a.getActivityStartTime() != null ? sdf.format(a.getActivityStartTime()) : "-");
            row.put("状态", getActivityStatusText(a.getStatus()));
            data.add(row);
            count++;
        }
        result.put("data", data);
        return result;
    }

    private Map<String, Object> executeGetOrganizationInfo(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("type", "text");
        result.put("message", "黄山学院软件小组介绍：\n\n黄山学院软件小组是一个由计算机相关专业学生组成的学术组织，致力于软件开发学习与实践。小组提供以下服务：\n\n1. 学习资源分享\n2. 项目实战锻炼\n3. 技术交流活动\n4. 竞赛指导培训\n5. 就业经验分享\n\n如需了解更多，请浏览网站首页或联系管理员。");
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
    
    private String getActivityTypeText(String type) {
        if (type == null) return "-";
        switch (type) {
            case "LECTURE": return "讲座";
            case "TRAINING": return "培训";
            case "COMPETITION": return "比赛";
            case "SEMINAR": return "讨论会";
            case "WORKSHOP": return "工作坊";
            case "PROJECT_INTRO": return "项目介绍";
            case "TEAM_BUILDING": return "团建";
            case "OTHER": return "其他";
            default: return type;
        }
    }

    private Map<String, Object> executeAdminAction(String actionType, Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            switch (actionType) {
                case "list_pending_users":
                    recordQuestionStatistics("待审核用户");
                    return executeListPendingUsers();
                case "list_all_users":
                    recordQuestionStatistics("所有用户");
                    return executeListAllUsers(params);
                case "approve_user":
                    recordQuestionStatistics("审核通过用户");
                    return executeApproveUser(params);
                case "reject_user":
                    recordQuestionStatistics("拒绝用户");
                    return executeRejectUser(params);
                case "list_pending_activities":
                    recordQuestionStatistics("待审核活动");
                    return executeListPendingActivities();
                case "list_all_activities":
                    recordQuestionStatistics("所有活动");
                    return executeListAllActivities(params);
                case "approve_activity":
                    recordQuestionStatistics("通过活动");
                    return executeApproveActivity(params);
                case "reject_activity":
                    recordQuestionStatistics("拒绝活动");
                    return executeRejectActivity(params);
                case "view_participants":
                    recordQuestionStatistics("查看参与者");
                    return executeViewParticipants(params);
                case "list_pending_news":
                    recordQuestionStatistics("待审核新闻");
                    return executeListPendingNews();
                case "list_all_news":
                    recordQuestionStatistics("所有新闻");
                    return executeListAllNews(params);
                case "approve_news":
                    recordQuestionStatistics("通过新闻");
                    return executeApproveNews(params);
                case "reject_news":
                    recordQuestionStatistics("拒绝新闻");
                    return executeRejectNews(params);
                case "submit_news":
                    recordQuestionStatistics("提交新闻");
                    return executeSubmitNews(params, user);
                case "create_activity":
                    recordQuestionStatistics("创建活动");
                    return executeCreateActivity(params, user);
                case "list_all_awards":
                    recordQuestionStatistics("所有奖项");
                    return executeListAllAwards(params);
                case "list_pending_problems":
                    recordQuestionStatistics("待处理问题");
                    return executeListPendingProblems();
                case "list_all_problems":
                    recordQuestionStatistics("所有问题");
                    return executeListAllProblems(params);
                case "classify_problem":
                    recordQuestionStatistics("分类问题");
                    return executeClassifyProblem(params);
                case "resolve_problem":
                    recordQuestionStatistics("解决问题");
                    return executeResolveProblem(params);
                case "fix_problem":
                    recordQuestionStatistics("修复问题");
                    return executeFixProblem(params);
                case "statistics":
                    recordQuestionStatistics("统计数据");
                    return executeStatistics();
                case "recent_activities":
                    recordQuestionStatistics("近期活动");
                    return executeRecentActivities(params);
                case "recent_news":
                    recordQuestionStatistics("近期新闻");
                    return executeRecentNews(params);
                case "view_activity_detail":
                    recordQuestionStatistics("查看活动详情");
                    return executeViewActivityDetail(params);
                case "view_news_detail":
                    recordQuestionStatistics("查看新闻详情");
                    return executeViewNewsDetail(params);
                case "view_user_detail":
                    recordQuestionStatistics("查看用户详情");
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
        List<News> newsList = newsDAO.findByConditions(null, null, 1);
        
        Map<String, List<Map<String, Object>>> categorizedData = new LinkedHashMap<>();
        Map<String, String> typeNames = new HashMap<>();
        typeNames.put("activity", "活动新闻");
        typeNames.put("notice", "通知公告");
        typeNames.put("tech", "技术分享");
        typeNames.put("award", "获奖荣誉");
        typeNames.put("recruit", "招新招聘");
        typeNames.put("other", "其他");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (News n : newsList) {
            String type = n.getType() != null ? n.getType() : "other";
            String typeName = typeNames.getOrDefault(type, "其他");
            
            Map<String, Object> newsMap = new HashMap<>();
            newsMap.put("id", n.getId());
            newsMap.put("title", n.getTitle());
            newsMap.put("summary", n.getSummary());
            newsMap.put("type", typeName);
            newsMap.put("createdAt", n.getCreatedAt() != null ? sdf.format(n.getCreatedAt()) : "");
            
            categorizedData.computeIfAbsent(typeName, k -> new ArrayList<>()).add(newsMap);
        }
        
        List<Map<String, Object>> categories = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : categorizedData.entrySet()) {
            Map<String, Object> cat = new HashMap<>();
            cat.put("name", entry.getKey());
            cat.put("count", entry.getValue().size());
            cat.put("items", entry.getValue());
            categories.add(cat);
        }
        
        result.put("success", true);
        result.put("type", "categorized_news");
        result.put("categories", categories);
        result.put("message", "新闻共 " + newsList.size() + " 条，分为 " + categories.size() + " 个类别");
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
        
        Map<String, String> typeNames = new HashMap<>();
        typeNames.put("activity", "活动新闻");
        typeNames.put("notice", "通知公告");
        typeNames.put("tech", "技术分享");
        typeNames.put("award", "获奖荣誉");
        typeNames.put("recruit", "招新招聘");
        typeNames.put("other", "其他");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> convertedList = new ArrayList<>();
        int count = Math.min(newsList.size(), limit);
        for (int i = 0; i < count; i++) {
            News n = newsList.get(i);
            Map<String, Object> newsMap = new HashMap<>();
            newsMap.put("编号", n.getId());
            newsMap.put("标题", n.getTitle());
            String type = n.getType() != null ? n.getType() : "other";
            newsMap.put("类型", typeNames.getOrDefault(type, "其他"));
            newsMap.put("发布时间", n.getCreatedAt() != null ? sdf.format(n.getCreatedAt()) : "");
            convertedList.add(newsMap);
        }
        
        result.put("success", true);
        result.put("type", "table");
        result.put("data", convertedList);
        result.put("columns", new String[]{"编号", "标题", "类型", "发布时间"});
        result.put("message", "最近 " + count + " 条新闻");
        return result;
    }

    private Map<String, Object> executeCreateActivity(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String title = params.get("name");
        String description = params.get("description");
        String activityType = params.get("activity_type");
        String location = params.get("location");
        String activityStartTime = params.get("start_time");
        String activityEndTime = params.get("end_time");
        String registrationStartTime = params.get("reg_start");
        String registrationEndTime = params.get("reg_end");
        String maxParticipantsStr = params.get("max_participants");
        String createGroupChatStr = params.get("createGroupChat");

        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("need_more_info", true);
            result.put("message", "请提供以下信息来完成活动发布：\n活动名称、地点、时间");
            return result;
        }

        Activity activity = new Activity();
        activity.setTitle(title.trim());
        activity.setDescription(description != null ? description.trim() : "");
        activity.setActivityType(activityType != null ? activityType : "OTHER");
        activity.setLocation(location != null ? location.trim() : "");
        activity.setCreatorId(user.getId());
        activity.setApprovalStatus("approved");
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
        if (success) {
            boolean createGroup = "true".equalsIgnoreCase(createGroupChatStr);
            if (createGroup) {
                ActivityGroup group = new ActivityGroup();
                group.setGroupName(title.trim() + " 交流群");
                group.setGroupOwnerId(user.getId());
                group.setActivityId(activity.getId());
                group.setCreatedAt(new Date());
                if (groupDAO.insert(group)) {
                    ActivityGroup createdGroup = groupDAO.findByActivityId(activity.getId());
                    if (createdGroup != null) {
                        groupMemberDAO.insertOwner(createdGroup.getId(), user.getId());
                        userGroupDAO.insertUserToGroup(user.getId(), createdGroup.getId());
                    }
                }
            }
            result.put("message", "活动创建成功！" + (createGroup ? " 已同时创建活动群聊，您已设置为群主。" : ""));
        } else {
            result.put("message", "活动创建失败，请重试");
        }
        result.put("success", success);
        return result;
    }

    private Map<String, Object> executeSubmitNews(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        
        if (!ROLE_ADMIN.equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "只有管理员才能发布新闻");
            return result;
        }
        
        String title = params.get("title");
        String summary = params.get("summary");
        String type = params.get("type");
        String content = params.get("content");

        if (title == null || title.trim().isEmpty()) {
            result.put("success", false);
            result.put("need_more_info", true);
            result.put("message", "请提供以下信息来完成新闻发布：\n标题、摘要、内容、分类");
            return result;
        }

        News news = new News();
        news.setTitle(title.trim());
        news.setSummary(summary != null ? summary.trim() : "");
        news.setType(type != null ? type : "activity");
        news.setContentPath(content != null ? content : "");
        news.setAuthorId(user.getId());
        news.setStatus(1);

        try {
            boolean success = newsDAO.insert(news);
            result.put("success", success);
            if (success) {
                result.put("message", "新闻发布成功！");
            } else {
                result.put("message", "新闻发布失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发布出错: " + e.getMessage());
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
                result.put("message", "以上是当前可报名的活动，请告诉我活动ID进行报名：");
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

    private Map<String, Object> executeListAllProjects(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        List<Project> projects;
        
        if (ROLE_ADMIN.equals(user.getRole())) {
            projects = projectDAO.findAll();
        } else {
            projects = projectDAO.findProjectsByUserId(user.getId());
        }
        
        if (projects.isEmpty()) {
            result.put("success", false);
            result.put("message", "当前没有项目");
            return result;
        }
        result.put("success", true);
        result.put("message", ROLE_ADMIN.equals(user.getRole()) ? "项目共 " + projects.size() + " 个" : "您参与的项目共 " + projects.size() + " 个");
        result.put("type", "table");
        result.put("columns", new String[]{"编号", "项目名称", "负责人ID", "状态", "创建时间"});
        List<Map<String, Object>> data = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (Project p : projects) {
            Map<String, Object> row = new HashMap<>();
            row.put("编号", p.getId());
            row.put("项目名称", p.getName());
            row.put("负责人ID", p.getLeaderId());
            row.put("状态", getProjectStatusText(p.getStatus()));
            row.put("创建时间", p.getCreatedAt() != null ? sdf.format(p.getCreatedAt()) : "-");
            data.add(row);
        }
        result.put("data", data);
        return result;
    }

    private Map<String, Object> executeListPublicProjects(Map<String, String> params, User user) {
        return executeListAllProjects(params, user);
    }

    private Map<String, Object> executeCreateProjectRequest(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String name = params.get("name");
        String description = params.get("description");
        String category = params.get("category");
        String startDate = params.get("expected_start_date");
        String endDate = params.get("expected_end_date");
        String repoUrl = params.get("repo_url");
        String budget = params.get("budget");

        if (name == null || name.trim().isEmpty()) {
            result.put("success", false);
            result.put("need_more_info", true);
            result.put("message", "请提供以下信息来完成项目申请：\n项目名称、项目描述、项目分类");
            return result;
        }

        try {
            Project project = new Project();
            project.setName(name.trim());
            project.setDescription(description != null ? description.trim() : "");
            project.setCategory(category != null ? category.trim() : "其他");
            project.setLeaderId(user.getId());
            project.setStatus(Project.STATUS_PENDING);
            project.setYear(Calendar.getInstance().get(Calendar.YEAR));
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    project.setExpectedStartDate(new SimpleDateFormat("yyyy-MM-dd").parse(startDate.trim()));
                } catch (Exception e) { }
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    project.setExpectedEndDate(new SimpleDateFormat("yyyy-MM-dd").parse(endDate.trim()));
                } catch (Exception e) { }
            }
            if (repoUrl != null && !repoUrl.trim().isEmpty()) {
                project.setRepoUrl(repoUrl.trim());
            }
            if (budget != null && !budget.trim().isEmpty()) {
                try {
                    project.setBudget(new java.math.BigDecimal(budget.trim()));
                } catch (Exception e) { }
            }

            boolean success = projectDAO.insert(project);
            result.put("success", success);
            if (success) {
                result.put("message", "项目「" + name + "」申请已提交，等待管理员审核！");
            } else {
                result.put("message", "项目申请提交失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建项目出错: " + e.getMessage());
        }
        return result;
    }

    private String getProjectStatusText(String status) {
        if (status == null) return "-";
        switch (status) {
            case "pending": return "待审核";
            case "approved": return "已通过";
            case "rejected": return "已拒绝";
            case "completed": return "已完成";
            default: return status;
        }
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
        List<ActivityGroup> memberGroups = groupDAO.findByUserId(user.getId());
        List<ActivityGroup> ownedGroups = groupDAO.findByOwnerId(user.getId());

        Set<Integer> addedIds = new HashSet<>();
        List<Map<String, Object>> groupList = new ArrayList<>();

        for (ActivityGroup g : ownedGroups) {
            if (!addedIds.contains(g.getId())) {
                Map<String, Object> item = buildGroupInfo(g, true);
                groupList.add(item);
                addedIds.add(g.getId());
            }
        }
        for (ActivityGroup g : memberGroups) {
            if (!addedIds.contains(g.getId())) {
                Map<String, Object> item = buildGroupInfo(g, false);
                groupList.add(item);
                addedIds.add(g.getId());
            }
        }

        result.put("success", true);
        result.put("type", "group_list");
        result.put("data", groupList);
        result.put("message", "您共有 " + groupList.size() + " 个群聊");
        return result;
    }

    private Map<String, Object> executeListMyGroups(Map<String, String> params, User user) {
        return executeViewMyGroups(params, user);
    }

    private Map<String, Object> executeViewGroupDetail(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String groupIdStr = params.get("group_id");

        if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供群聊ID (group_id)");
            return result;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);

            if (group == null) {
                result.put("success", false);
                result.put("message", "未找到该群聊");
                return result;
            }

            boolean isOwner = groupMemberDAO.isOwner(groupId, user.getId());
            boolean isMember = userGroupDAO.exists(user.getId(), groupId);

            if (!isMember && !isOwner) {
                result.put("success", false);
                result.put("message", "您不是该群聊的成员");
                return result;
            }

            List<GroupMember> members = groupMemberDAO.findByGroupId(groupId);

            result.put("success", true);
            result.put("type", "group_detail");
            result.put("data", buildGroupInfo(group, isOwner));
            result.put("members", members);
            result.put("memberCount", members.size());
            result.put("message", "群聊详情获取成功");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的群聊ID");
        }
        return result;
    }

    private Map<String, Object> executeJoinGroup(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String groupIdStr = params.get("group_id");

        if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供群聊ID (group_id)");
            return result;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);

            if (group == null) {
                result.put("success", false);
                result.put("message", "未找到该群聊");
                return result;
            }

            if (userGroupDAO.exists(user.getId(), groupId)) {
                result.put("success", false);
                result.put("message", "您已在该群聊中");
                return result;
            }

            boolean success = userGroupDAO.insertUserToGroup(user.getId(), groupId);
            result.put("success", success);
            result.put("message", success ? "加入群聊成功" : "加入失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的群聊ID");
        }
        return result;
    }

    private Map<String, Object> executeLeaveGroup(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String groupIdStr = params.get("group_id");

        if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供群聊ID (group_id)");
            return result;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);
            ActivityGroup group = groupDAO.findById(groupId);

            if (group == null) {
                result.put("success", false);
                result.put("message", "未找到该群聊");
                return result;
            }

            if (groupMemberDAO.isOwner(groupId, user.getId())) {
                result.put("success", false);
                result.put("message", "群主无法退出群聊，请先转让群主权限");
                return result;
            }

            boolean success = userGroupDAO.delete(user.getId(), groupId);
            result.put("success", success);
            result.put("message", success ? "已退出群聊" : "退出失败");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的群聊ID");
        }
        return result;
    }

    private Map<String, Object> executeListGroupMembers(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String groupIdStr = params.get("group_id");

        if (groupIdStr == null || groupIdStr.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "请提供群聊ID (group_id)");
            return result;
        }

        try {
            int groupId = Integer.parseInt(groupIdStr);

            if (!userGroupDAO.exists(user.getId(), groupId)) {
                result.put("success", false);
                result.put("message", "您不是该群聊的成员");
                return result;
            }

            List<GroupMember> members = groupMemberDAO.findByGroupId(groupId);

            result.put("success", true);
            result.put("type", "member_list");
            result.put("data", members);
            result.put("message", "该群聊共有 " + members.size() + " 名成员");
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的群聊ID");
        }
        return result;
    }

    private Map<String, Object> buildGroupInfo(ActivityGroup group, boolean isOwner) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", group.getId());
        info.put("name", group.getGroupName());
        info.put("activityName", group.getActivityName());
        info.put("ownerName", group.getOwnerName());
        info.put("memberCount", groupMemberDAO.countByGroupId(group.getId()));
        info.put("isOwner", isOwner);
        info.put("createdAt", group.getCreatedAt());
        return info;
    }

    private Map<String, Object> executeSubmitAward(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        String competition = params.get("competition");
        String compTime = params.get("compTime");
        String compLocation = params.get("compLocation");
        String compSession = params.get("compSession");
        String compLevel = params.get("compLevel");
        String awardType = params.get("awardType");
        String awardCategory = params.get("awardCategory");
        String teamName = params.get("teamName");
        String awardLevel = params.get("awardLevel");

        if (competition == null || competition.trim().isEmpty()) {
            result.put("success", false);
            result.put("need_more_info", true);
            result.put("message", "请提供以下信息来完成奖项申请：\n比赛名称、比赛时间（必填）\n比赛等级、奖项类型、奖项等级（必填）");
            return result;
        }

        try {
            Award award = new Award();
            award.setName(competition.trim());
            award.setCompetition(competition.trim());
            if (compTime != null && !compTime.trim().isEmpty()) {
                try {
                    award.setCompetitionTime(new SimpleDateFormat("yyyy-MM-dd").parse(compTime.trim()));
                } catch (Exception e) { }
            }
            award.setCompetitionLocation(compLocation != null ? compLocation.trim() : "");
            award.setCompetitionSession(compSession != null ? compSession.trim() : "");
            if (compLevel != null && !compLevel.trim().isEmpty()) {
                award.setCompetitionLevel(Integer.parseInt(compLevel.trim()));
            }
            if (awardType != null && !awardType.trim().isEmpty()) {
                award.setAwardType(Integer.parseInt(awardType.trim()));
            }
            if (awardCategory != null && !awardCategory.trim().isEmpty()) {
                award.setAwardCategory(Integer.parseInt(awardCategory.trim()));
            }
            award.setTeamName(teamName != null ? teamName.trim() : "");
            if (awardLevel != null && !awardLevel.trim().isEmpty()) {
                award.setAwardLevel(Integer.parseInt(awardLevel.trim()));
            }
            award.setAwardStatus(Award.STATUS_PENDING);
            award.setCreatedBy(user.getId());
            award.setUserId(user.getId());
            award.setYear(Calendar.getInstance().get(Calendar.YEAR));

            boolean success = awardDAO.insert(award);
            result.put("success", success);
            if (success) {
                result.put("message", "奖项申请已提交，等待管理员审核！");
            } else {
                result.put("message", "奖项申请提交失败，请重试");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建奖项出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeListMyAwards(Map<String, String> params, User user) {
        Map<String, Object> result = new HashMap<>();
        List<Award> awards = awardDAO.findByUserId(user.getId());

        if (awards.isEmpty()) {
            result.put("success", true);
            result.put("message", "您还没有获奖记录");
            return result;
        }

        result.put("success", true);
        result.put("type", "table");
        result.put("columns", new String[]{"编号", "奖项名称", "获奖赛事/项目", "获奖等级", "团队", "状态", "申请时间"});
        result.put("data", awards);
        result.put("message", "您共有 " + awards.size() + " 条获奖记录");
        return result;
    }

    private Map<String, Object> executeListAllAwards(Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        List<Award> awards = awardDAO.findAll();

        if (awards.isEmpty()) {
            result.put("success", true);
            result.put("message", "暂无奖项记录");
            return result;
        }

        result.put("success", true);
        result.put("type", "table");
        result.put("columns", new String[]{"编号", "奖项名称", "获奖赛事/项目", "获奖等级", "团队", "状态", "申请人", "申请时间"});
        result.put("data", awards);
        result.put("message", "共有 " + awards.size() + " 条奖项记录");
        return result;
    }

    public Map<String, String> parseActionParams(String actionString) {
        Map<String, String> params = new HashMap<>();
        if (actionString == null || actionString.isEmpty()) {
            return params;
        }

        try {
            actionString = java.net.URLDecoder.decode(actionString, "UTF-8");
        } catch (Exception e) {
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