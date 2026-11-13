
package com.softwaregroup.ai.controller;

import com.softwaregroup.ai.model.dto.*;
import com.softwaregroup.ai.model.entity.*;
import com.softwaregroup.ai.service.AIService;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.security.RequireRole;
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
    public Result chat(@RequestBody Map<String, Object> request,
                       @AuthenticatedUser Integer userId) {
        String message = (String) request.get("message");
        String sessionId = (String) request.get("sessionId");
        String response = aiService.chat(message, sessionId, userId);
        return Result.ok(response);
    }

    @GetMapping("/history")
    public Result getHistory(@RequestParam String sessionId,
                             @AuthenticatedUser Integer userId) {
        List<AIMessage> history = aiService.getConversationHistory(sessionId, userId);
        return Result.ok(history);
    }

    @DeleteMapping("/clear")
    public Result clearConversation(@RequestParam String sessionId,
                                    @AuthenticatedUser Integer userId) {
        aiService.clearConversation(sessionId, userId);
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
    public Result executeAction(@RequestBody Map<String, Object> request,
                                @AuthenticatedUser CurrentUser currentUser) {
        String actionType = (String) request.get("actionType");
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) request.get("params");

        UserDTO user = new UserDTO();
        user.setId(currentUser.id());
        user.setUsername(currentUser.username());
        user.setRole(currentUser.role());

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
    public Result createStatus(@RequestBody Map<String, Object> request,
                               @AuthenticatedUser Integer userId) {
        String sessionId = (String) request.get("sessionId");
        String userMessage = (String) request.get("userMessage");

        Integer statusId = aiService.createMessageStatus(sessionId, userId, userMessage);
        return Result.ok(statusId);
    }

    @PutMapping("/status/{statusId}")
    public Result updateStatus(@PathVariable Integer statusId,
                                @RequestBody Map<String, String> request,
                                @AuthenticatedUser Integer userId) {
        String newStatus = request.get("newStatus");
        boolean success = aiService.updateMessageStatus(statusId, newStatus, userId);
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
    public Result getUserInfo(@PathVariable Integer userId,
                              @AuthenticatedUser CurrentUser currentUser) {
        if (!"ADMIN".equals(currentUser.role())) {
            CurrentUser.requireSameId(userId, currentUser.id());
        }
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
    @RequireRole("ADMIN")
    public Result getPendingAwards() {
        List<AwardDTO> list = aiService.getPendingAwards();
        return Result.ok(list);
    }

    @GetMapping("/groups/{userId}")
    public Result getUserGroups(@PathVariable Integer userId,
                                @AuthenticatedUser Integer currentUserId) {
        CurrentUser.requireSameId(userId, currentUserId);
        List<GroupDTO> list = aiService.getUserGroups(userId);
        return Result.ok(list);
    }
}
