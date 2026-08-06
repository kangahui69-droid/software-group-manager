package com.softwaregroup.ai.service;

import com.softwaregroup.ai.dao.*;
import com.softwaregroup.ai.feign.*;
import com.softwaregroup.ai.model.dto.*;
import com.softwaregroup.ai.model.entity.*;
import com.softwaregroup.common.util.Result;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI服务（混合模式）
 * - AI自身数据（AIConversation、AIMessage、AIKnowledgeBase、AIFaqStatistics）直连DB
 * - 业务数据（User、Activity、Project、Award、Group）通过Feign获取
 */
@Service
public class AIService {

    public static final String ROLE_GUEST = "GUEST";
    public static final String ROLE_MEMBER = "MEMBER";
    public static final String ROLE_ADMIN = "ADMIN";

    // AI自身数据DAO（直连DB）
    private final AIConversationDAO conversationDAO;
    private final AIMessageDAO messageDAO;
    private final AIKnowledgeBaseDAO knowledgeBaseDAO;
    private final AIFaqStatisticsDAO faqStatsDAO;
    private final AIMessageStatusDAO messageStatusDAO;

    // 业务数据Feign客户端
    private final UserFeignClient userFeignClient;
    private final ActivityFeignClient activityFeignClient;
    private final ProjectFeignClient projectFeignClient;
    private final AwardFeignClient awardFeignClient;
    private final GroupFeignClient groupFeignClient;
    private final FileFeignClient fileFeignClient;

    // 用户信息缓存
    private final Map<Integer, UserDTO> userCache = new ConcurrentHashMap<>();

    public AIService(AIConversationDAO conversationDAO, AIMessageDAO messageDAO,
                     AIKnowledgeBaseDAO knowledgeBaseDAO, AIFaqStatisticsDAO faqStatsDAO,
                     AIMessageStatusDAO messageStatusDAO,
                     UserFeignClient userFeignClient, ActivityFeignClient activityFeignClient,
                     ProjectFeignClient projectFeignClient, AwardFeignClient awardFeignClient,
                     GroupFeignClient groupFeignClient, FileFeignClient fileFeignClient) {
        this.conversationDAO = conversationDAO;
        this.messageDAO = messageDAO;
        this.knowledgeBaseDAO = knowledgeBaseDAO;
        this.faqStatsDAO = faqStatsDAO;
        this.messageStatusDAO = messageStatusDAO;
        this.userFeignClient = userFeignClient;
        this.activityFeignClient = activityFeignClient;
        this.projectFeignClient = projectFeignClient;
        this.awardFeignClient = awardFeignClient;
        this.groupFeignClient = groupFeignClient;
        this.fileFeignClient = fileFeignClient;
    }

    // ==================== 对话管理（AI自身数据直连DB） ====================

    public AIConversation createOrGetConversation(String sessionId, Integer userId) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        if (conversation == null) {
            conversation = new AIConversation(userId, sessionId);
            Integer id = conversationDAO.save(conversation);
            conversation.setId(id);
        }
        return conversation;
    }

    public List<AIMessage> getConversationHistory(String sessionId) {
        try {
            AIConversation conversation = conversationDAO.findBySessionId(sessionId);
            if (conversation == null) {
                return null;
            }
            return messageDAO.findByConversationId(conversation.getId());
        } catch (Exception e) {
            return null;
        }
    }

    public void clearConversation(String sessionId) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        if (conversation != null) {
            messageDAO.deleteByConversationId(conversation.getId());
            conversationDAO.delete(conversation.getId());
        }
    }

    // ==================== 消息处理 ====================

    public AIMessage saveMessage(Integer conversationId, String role, String content) {
        AIMessage message = new AIMessage(conversationId, role, content);
        Integer id = messageDAO.save(message);
        message.setId(id);
        return message;
    }

    // ==================== 知识库（AI自身数据直连DB） ====================

    public List<AIKnowledgeBase> getAllKnowledgeBase() {
        return knowledgeBaseDAO.findAll();
    }

    public List<AIKnowledgeBase> searchKnowledgeBase(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<AIKnowledgeBase> all = knowledgeBaseDAO.findAll();
        if (all == null) {
            return Collections.emptyList();
        }
        List<AIKnowledgeBase> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (AIKnowledgeBase kb : all) {
            if (kb.getKeywords() != null && kb.getKeywords().toLowerCase().contains(lowerKeyword)) {
                result.add(kb);
            }
        }
        return result;
    }

    // ==================== FAQ统计（AI自身数据直连DB） ====================

    public List<AIFaqStatistics> getTopQuestions(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        return faqStatsDAO.findTopQuestions(limit);
    }

    public List<AIFaqStatistics> getAllFaqStatistics() {
        return faqStatsDAO.findAllOrderByCount();
    }

    public void recordQuestionStatistics(String question) {
        if (question == null || question.trim().isEmpty()) {
            return;
        }
        String normalized = question.trim().toLowerCase();
        AIFaqStatistics stats = faqStatsDAO.findByHash(normalized);
        if (stats == null) {
            stats = new AIFaqStatistics();
            stats.setQuestionHash(normalized);
            stats.setNormalizedQuestion(question.trim());
            stats.setQueryCount(1);
            stats.setLastQueryAt(new Date());
            faqStatsDAO.save(stats);
        } else {
            stats.setQueryCount(stats.getQueryCount() + 1);
            stats.setLastQueryAt(new Date());
            faqStatsDAO.update(stats);
        }
    }

    // ==================== Feign调用 - 用户服务 ====================

    public UserDTO getUserInfo(Integer userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return callFeign(() -> userFeignClient.getUserById(userId),
                this::convertToUserDTO, null);
    }

    public UserDTO getUserInfoCached(Integer userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        UserDTO user = getUserInfo(userId);
        if (user != null) {
            userCache.put(userId, user);
        }
        return user;
    }

    private UserDTO convertToUserDTO(Object data) {
        if (data instanceof UserDTO) {
            return (UserDTO) data;
        }
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            UserDTO dto = new UserDTO();
            dto.setId(toInteger(map.get("id")));
            dto.setUsername(toString(map.get("username")));
            dto.setName(toString(map.get("name")));
            dto.setRole(toString(map.get("role")));
            dto.setEmail(toString(map.get("email")));
            dto.setPhone(toString(map.get("phone")));
            dto.setStatus(toInteger(map.get("status")));
            return dto;
        }
        return null;
    }

    // ==================== Feign调用 - 活动服务 ====================

    public List<ActivityDTO> getActivities() {
        return callFeign(() -> activityFeignClient.getActivities(),
                data -> convertToList(data, this::convertToActivityDTO),
                Collections.emptyList());
    }

    public ActivityDTO getActivityDetail(Integer activityId) {
        if (activityId == null || activityId <= 0) {
            return null;
        }
        return callFeign(() -> activityFeignClient.getActivityById(activityId),
                this::convertToActivityDTO, null);
    }

    public List<ActivityDTO> getUpcomingActivities() {
        return callFeign(() -> activityFeignClient.getUpcomingActivities(),
                data -> convertToList(data, this::convertToActivityDTO),
                Collections.emptyList());
    }

    public List<ActivityDTO> getActivitiesCached() {
        return getActivities();
    }

    private <T> List<T> convertToList(Object data, java.util.function.Function<Object, T> converter) {
        List<T> list = new ArrayList<>();
        if (data instanceof List) {
            for (Object item : (List<?>) data) {
                T dto = converter.apply(item);
                if (dto != null) {
                    list.add(dto);
                }
            }
        }
        return list;
    }

    private ActivityDTO convertToActivityDTO(Object data) {
        if (data instanceof ActivityDTO) {
            return (ActivityDTO) data;
        }
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            ActivityDTO dto = new ActivityDTO();
            dto.setId(toInteger(map.get("id")));
            dto.setTitle(toString(map.get("title")));
            dto.setActivityType(toString(map.get("activityType")));
            dto.setStatus(toString(map.get("status")));
            dto.setLocation(toString(map.get("location")));
            dto.setDescription(toString(map.get("description")));
            dto.setStartTime(toString(map.get("startTime")));
            dto.setEndTime(toString(map.get("endTime")));
            return dto;
        }
        return null;
    }

    // ==================== Feign调用 - 项目服务 ====================

    public List<ProjectDTO> getProjects() {
        return callFeign(() -> projectFeignClient.getProjects(),
                data -> convertToList(data, this::convertToProjectDTO),
                Collections.emptyList());
    }

    public List<ProjectDTO> getUserProjects(Integer userId) {
        return callFeign(() -> projectFeignClient.getUserProjects(userId),
                data -> convertToList(data, this::convertToProjectDTO),
                Collections.emptyList());
    }

    public List<ProjectDTO> getPublicProjects() {
        return callFeign(() -> projectFeignClient.getPublicProjects(),
                data -> convertToList(data, this::convertToProjectDTO),
                Collections.emptyList());
    }

    private ProjectDTO convertToProjectDTO(Object data) {
        if (data instanceof ProjectDTO) {
            return (ProjectDTO) data;
        }
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            ProjectDTO dto = new ProjectDTO();
            dto.setId(toInteger(map.get("id")));
            dto.setName(toString(map.get("name")));
            dto.setDescription(toString(map.get("description")));
            dto.setStatus(toString(map.get("status")));
            dto.setCategory(toString(map.get("category")));
            dto.setLeaderId(toInteger(map.get("leaderId")));
            return dto;
        }
        return null;
    }

    // ==================== Feign调用 - 奖项服务 ====================

    public List<AwardDTO> getUserAwards(Integer userId) {
        return callFeign(() -> awardFeignClient.getUserAwards(userId),
                data -> convertToList(data, this::convertToAwardDTO),
                Collections.emptyList());
    }

    public List<AwardDTO> getAllAwards() {
        return callFeign(() -> awardFeignClient.getAwards(),
                data -> convertToList(data, this::convertToAwardDTO),
                Collections.emptyList());
    }

    public List<AwardDTO> getPendingAwards() {
        return callFeign(() -> awardFeignClient.getPendingAwards(),
                data -> convertToList(data, this::convertToAwardDTO),
                Collections.emptyList());
    }

    private AwardDTO convertToAwardDTO(Object data) {
        if (data instanceof AwardDTO) {
            return (AwardDTO) data;
        }
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            AwardDTO dto = new AwardDTO();
            dto.setId(toInteger(map.get("id")));
            dto.setName(toString(map.get("name")));
            dto.setCompetition(toString(map.get("competition")));
            dto.setAwardLevel(toString(map.get("awardLevel")));
            dto.setAwardType(toString(map.get("awardType")));
            dto.setAwardStatus(toString(map.get("awardStatus")));
            dto.setUserId(toInteger(map.get("userId")));
            return dto;
        }
        return null;
    }

    // ==================== Feign调用 - 群组服务 ====================

    public List<GroupDTO> getUserGroups(Integer userId) {
        return callFeign(() -> groupFeignClient.getUserGroups(userId),
                data -> convertToList(data, this::convertToGroupDTO),
                Collections.emptyList());
    }

    public GroupDTO getGroupDetail(Integer groupId) {
        return callFeign(() -> groupFeignClient.getGroupById(groupId),
                this::convertToGroupDTO, null);
    }

    private GroupDTO convertToGroupDTO(Object data) {
        if (data instanceof GroupDTO) {
            return (GroupDTO) data;
        }
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            GroupDTO dto = new GroupDTO();
            dto.setId(toInteger(map.get("id")));
            dto.setName(toString(map.get("name")));
            dto.setDescription(toString(map.get("description")));
            dto.setCreatorId(toInteger(map.get("creatorId")));
            dto.setType(toString(map.get("type")));
            return dto;
        }
        return null;
    }

    // ==================== AI对话生成 ====================

    public String chat(String userMessage, String sessionId, Integer userId) {
        createOrGetConversation(sessionId, userId);
        if (userId != null && userId > 0) {
            try {
                userFeignClient.getUserById(userId);
            } catch (Exception e) {
                // Feign调用失败不影响对话流程
            }
        }
        saveMessage(getConversationId(sessionId), "user", userMessage);
        recordQuestionStatistics(userMessage);
        return "AI回复: " + userMessage;
    }

    private Integer getConversationId(String sessionId) {
        AIConversation conversation = conversationDAO.findBySessionId(sessionId);
        return conversation != null ? conversation.getId() : null;
    }

    // ==================== 系统提示词构建 ====================

    public String buildSystemPrompt(String userRole) {
        if (userRole == null) {
            userRole = ROLE_GUEST;
        }
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
               "3. 遇到不确定的情况，先执行ACTION查询再回答\n\n" +
               "【关于软件小组的介绍 - 直接回答，无需查询】\n" +
               "黄山学院软件小组是一个由计算机相关专业学生组成的学术组织，致力于软件开发学习与实践。\n\n" +
               "【加入方式】\n" +
               "访问网站注册账号，管理员审核通过后即可成为正式成员。\n\n" +
               "【操作触发规则】\n" +
               "- 活动相关：查看活动、有哪些活动 → [ACTION]list_activities\n" +
               "- 项目相关：查看项目、项目列表 → [ACTION]list_public_projects";
    }

    private String buildMemberPrompt() {
        return "你是黄山学院软件小组的智能助手，直接用中文回答用户问题。\n\n" +
               "用户身份：正式成员\n\n" +
               "【核心原则】\n" +
               "1. 绝对不要编造活动、新闻、项目、奖项等信息！\n" +
               "2. 所有列表查询必须通过[ACTION]从数据库获取真实数据\n" +
               "3. 遇到不确定的情况，先执行ACTION查询再回答\n\n" +
               "【操作触发规则】\n" +
               "- 查看活动：list_latest_activities\n" +
               "- 我的奖项：list_my_awards\n" +
               "- 查看项目：list_public_projects\n" +
               "- 我的群组：view_my_groups";
    }

    private String buildAdminPrompt() {
        return "你是黄山学院软件小组的管理员助手，直接用中文回答问题。\n\n" +
               "用户身份：系统管理员\n\n" +
               "【核心原则】\n" +
               "1. 绝对不要编造活动、新闻、项目、用户等信息！\n" +
               "2. 所有列表查询必须通过[ACTION]从数据库获取真实数据\n" +
               "3. 遇到不确定的情况，先执行ACTION查询再回答\n\n" +
               "【操作触发规则】\n" +
               "- 查看活动：list_activities\n" +
               "- 审核奖项：list_all_awards\n" +
               "- 查看项目：list_all_projects\n" +
               "- 查看成员：list_all_users\n" +
               "- 数据统计：statistics";
    }

    // ==================== 操作执行 ====================

    public Map<String, Object> executeAction(String actionType, Map<String, String> params) {
        return executeAction(actionType, params, null);
    }

    public Map<String, Object> executeAction(String actionType, Map<String, String> params, UserDTO user) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            switch (actionType) {
                case "list_activities":
                    result = executeListActivities(params);
                    break;
                case "list_public_projects":
                    result = executeListPublicProjects(params, user);
                    break;
                case "list_my_awards":
                    result = executeListMyAwards(params, user);
                    break;
                case "view_my_groups":
                    result = executeViewMyGroups(params, user);
                    break;
                default:
                    result.put("message", "未知操作: " + actionType);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行出错: " + e.getMessage());
        }
        return result;
    }

    private Map<String, Object> executeListActivities(Map<String, String> params) {
        try {
            Result feignResult = activityFeignClient.getActivities();
            List<ActivityDTO> activities = convertToList(feignResult.getData(), this::convertToActivityDTO);
            return buildSuccessResult(activities, "获取活动列表成功");
        } catch (Exception e) {
            return buildErrorResult("获取活动列表失败: " + e.getMessage(), Collections.emptyList());
        }
    }

    private Map<String, Object> executeListPublicProjects(Map<String, String> params, UserDTO user) {
        List<ProjectDTO> projects = getPublicProjects();
        return buildSuccessResult(projects, "获取项目列表成功");
    }

    private Map<String, Object> executeListMyAwards(Map<String, String> params, UserDTO user) {
        if (user == null) {
            return buildErrorResult("请先登录", null);
        }
        List<AwardDTO> awards = getUserAwards(user.getId());
        return buildSuccessResult(awards, "获取奖项列表成功");
    }

    private Map<String, Object> executeViewMyGroups(Map<String, String> params, UserDTO user) {
        if (user == null) {
            return buildErrorResult("请先登录", null);
        }
        List<GroupDTO> groups = getUserGroups(user.getId());
        return buildSuccessResult(groups, "获取群组列表成功");
    }

    private Map<String, Object> buildSuccessResult(Object data, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    private Map<String, Object> buildErrorResult(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    // ==================== 会话ID生成 ====================

    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== 消息状态 ====================

    public Integer createMessageStatus(String sessionId, Integer userId, String userMessage) {
        AIMessageStatus status = new AIMessageStatus();
        status.setSessionId(sessionId);
        status.setUserId(userId);
        status.setUserMessage(userMessage);
        status.setStatus(AIMessageStatus.STATUS_PENDING);
        return messageStatusDAO.save(status);
    }

    public boolean updateMessageStatus(Integer statusId, String newStatus) {
        AIMessageStatus status = messageStatusDAO.findById(statusId);
        if (status == null) {
            return false;
        }
        status.setStatus(newStatus);
        messageStatusDAO.update(status);
        return true;
    }

    // ==================== 参数解析 ====================

    public Map<String, String> parseActionParams(String actionString) {
        Map<String, String> params = new HashMap<>();
        if (actionString == null || actionString.trim().isEmpty()) {
            return params;
        }
        try {
            String decoded = URLDecoder.decode(actionString, StandardCharsets.UTF_8.name());
            for (String pair : decoded.split("\\|")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                } else if (kv.length == 1 && !kv[0].isEmpty()) {
                    params.put(kv[0], "");
                }
            }
        } catch (Exception e) {
            // 解析失败，返回空Map
        }
        return params;
    }

    // ==================== 工具方法 ====================

    private <T> T callFeign(java.util.function.Supplier<Result> feignCall,
                            java.util.function.Function<Object, T> converter,
                            T defaultValue) {
        try {
            Result result = feignCall.get();
            if (result != null && result.isSuccess() && result.getData() != null) {
                return converter.apply(result.getData());
            }
        } catch (Exception e) {
            // Feign调用失败
        }
        return defaultValue;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }
}
