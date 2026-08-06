
package com.softwaregroup.ai.controller;

import com.softwaregroup.ai.model.dto.*;
import com.softwaregroup.ai.model.entity.*;
import com.softwaregroup.ai.service.AIService;
import com.softwaregroup.common.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI服务 REST API控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AIService aiService;

    public AiController(AIService aiService) {
        this.aiService = aiService;
    }

    // ==================== 对话管理 ====================

    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        String sessionId = (String) request.get("sessionId");
        Integer userId = request.get("userId") != null
                ? Integer.valueOf(request.get("userId").toString()) : null;

        String response = aiService.chat(message, sessionId, userId);
        return Result.ok(response);
    }

    @GetMapping("/history")
    public Result getHistory(@RequestParam String sessionId) {
        List<AIMessage> history = aiService.getConversationHistory(sessionId);
        return Result.ok(history);
    }

    @DeleteMapping("/clear")
    public Result clearConversation(@RequestParam String sessionId) {
        aiService.clearConversation(sessionId);
        return Result.ok(null);
    }

    // ==================== 知识库 ====================

    @GetMapping("/knowledge")
    public Result getAllKnowledge() {
        List<AIKnowledgeBase> list = aiService.getAllKnowledgeBase();
        return Result.ok(list);
    }

    @GetMapping("/knowledge/search")
    public Result searchKnowledge(@RequestParam String keyword) {
        List<AIKnowledgeBase> list = aiService.searchKnowledgeBase(keyword);
        return Result.ok(list);
    }

    // ==================== FAQ统计 ====================

    @GetMapping("/faq/top")
    public Result getTopQuestions(@RequestParam(defaultValue = "10") int limit) {
        List<AIFaqStatistics> list = aiService.getTopQuestions(limit);
        return Result.ok(list);
    }

    @GetMapping("/faq/all")
    public Result getAllFaqStatistics() {
        List<AIFaqStatistics> list = aiService.getAllFaqStatistics();
        return Result.ok(list);
    }

    // ==================== ACTION执行 ====================

    @PostMapping("/execute")
    public Result executeAction(@RequestBody Map<String, Object> request) {
        String actionType = (String) request.get("actionType");
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) request.get("params");

        Object userObj = request.get("user");
        UserDTO user = null;
        if (userObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) userObj;
            user = new UserDTO();
            if (userMap.get("id") != null) {
                user.setId(Integer.valueOf(userMap.get("id").toString()));
            }
            user.setUsername((String) userMap.get("username"));
            user.setRole((String) userMap.get("role"));
        }

        Map<String, Object> result = aiService.executeAction(actionType, params, user);
        return Result.ok(result);
    }

    // ==================== 系统提示词 ====================

    @GetMapping("/prompt")
    public Result getPrompt(@RequestParam(required = false) String role) {
        String prompt = aiService.buildSystemPrompt(role);
        return Result.ok(prompt);
    }

    // ==================== 消息状态 ====================

    @PostMapping("/status")
    public Result createStatus(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("sessionId");
        Integer userId = request.get("userId") != null
                ? Integer.valueOf(request.get("userId").toString()) : null;
        String userMessage = (String) request.get("userMessage");

        Integer statusId = aiService.createMessageStatus(sessionId, userId, userMessage);
        return Result.ok(statusId);
    }

    @PutMapping("/status/{statusId}")
    public Result updateStatus(@PathVariable Integer statusId,
                                @RequestBody Map<String, String> request) {
        String newStatus = request.get("newStatus");
        boolean success = aiService.updateMessageStatus(statusId, newStatus);
        return Result.ok(success);
    }

    // ==================== 参数解析 ====================

    @PostMapping("/parse-params")
    public Result parseParams(@RequestBody Map<String, String> request) {
        String actionString = request.get("actionString");
        Map<String, String> params = aiService.parseActionParams(actionString);
        return Result.ok(params);
    }

    // ==================== 业务数据查询（Feign调用） ====================

    @GetMapping("/user/{userId}")
    public Result getUserInfo(@PathVariable Integer userId) {
        UserDTO user = aiService.getUserInfo(userId);
        return Result.ok(user);
    }

    @GetMapping("/activities")
    public Result getActivities() {
        List<ActivityDTO> list = aiService.getActivities();
        return Result.ok(list);
    }

    @GetMapping("/activities/upcoming")
    public Result getUpcomingActivities() {
        List<ActivityDTO> list = aiService.getUpcomingActivities();
        return Result.ok(list);
    }

    @GetMapping("/projects")
    public Result getProjects() {
        List<ProjectDTO> list = aiService.getProjects();
        return Result.ok(list);
    }

    @GetMapping("/projects/public")
    public Result getPublicProjects() {
        List<ProjectDTO> list = aiService.getPublicProjects();
        return Result.ok(list);
    }

    @GetMapping("/awards")
    public Result getAllAwards() {
        List<AwardDTO> list = aiService.getAllAwards();
        return Result.ok(list);
    }

    @GetMapping("/awards/pending")
    public Result getPendingAwards() {
        List<AwardDTO> list = aiService.getPendingAwards();
        return Result.ok(list);
    }

    @GetMapping("/groups/{userId}")
    public Result getUserGroups(@PathVariable Integer userId) {
        List<GroupDTO> list = aiService.getUserGroups(userId);
        return Result.ok(list);
    }
}
