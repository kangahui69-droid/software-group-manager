package com.softwaregroup.activity.controller;

import com.softwaregroup.activity.model.dto.ActivityDTO;
import com.softwaregroup.activity.model.dto.ActivityFilterDTO;
import com.softwaregroup.activity.service.ActivityService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动管理 Controller
 */
@RestController
@RequestMapping(value = {"/api/activities", "/api/activities/"})
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 创建活动
     */
    @PostMapping
    public Result createActivity(@RequestBody ActivityDTO dto,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.createActivity(dto, userId);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result updateActivity(@PathVariable Integer id,
                                 @RequestBody ActivityDTO dto,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.updateActivity(id, dto, userId);
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result deleteActivity(@PathVariable Integer id,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.deleteActivity(id, userId);
    }

    /**
     * 活动列表
     */
    @GetMapping
    public Result listActivities(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String activityType,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String approvalStatus) {
        ActivityFilterDTO filter = new ActivityFilterDTO();
        filter.setKeyword(keyword);
        filter.setActivityType(activityType);
        filter.setStatus(status);
        filter.setApprovalStatus(approvalStatus);
        return activityService.listActivities(filter);
    }

    /**
     * 活动详情
     */
    @GetMapping("/{id}")
    public Result getActivityDetail(@PathVariable Integer id,
                                    @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.getActivityDetail(id, userId);
    }

    /**
     * 我报名的活动
     */
    @GetMapping("/my")
    public Result getMyActivities(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.getMyActivities(userId);
    }

    /**
     * 我创建的活动
     */
    @GetMapping("/created")
    public Result getMyCreatedActivities(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.getMyCreatedActivities(userId);
    }

    /**
     * 报名活动
     */
    @PostMapping("/{id}/register")
    public Result register(@PathVariable Integer id,
                           @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return activityService.register(id, userId);
    }

    /**
     * 审批参与者通过
     */
    @PostMapping("/{activityId}/participants/{userId}/approve")
    public Result approveParticipant(@PathVariable Integer activityId,
                                     @PathVariable Integer userId,
                                     @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return activityService.approveParticipant(activityId, userId, operatorId);
    }

    /**
     * 拒绝参与者
     */
    @PostMapping("/{activityId}/participants/{userId}/reject")
    public Result rejectParticipant(@PathVariable Integer activityId,
                                     @PathVariable Integer userId,
                                     @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return activityService.rejectParticipant(activityId, userId, operatorId);
    }

    /**
     * 批量审批通过
     */
    @PostMapping("/{activityId}/participants/batch-approve")
    public Result batchApprove(@PathVariable Integer activityId,
                                @RequestBody Map<String, Object> body,
                                @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        List<Integer> userIds = (List<Integer>) body.get("userIds");
        return activityService.batchApprove(activityId, userIds, operatorId);
    }

    /**
     * 批量拒绝
     */
    @PostMapping("/{activityId}/participants/batch-reject")
    public Result batchReject(@PathVariable Integer activityId,
                               @RequestBody Map<String, Object> body,
                               @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        List<Integer> userIds = (List<Integer>) body.get("userIds");
        return activityService.batchReject(activityId, userIds, operatorId);
    }

    /**
     * 活动审核通过
     */
    @PostMapping("/{id}/approve")
    public Result approveActivity(@PathVariable Integer id,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return activityService.approveActivity(id, operatorId);
    }

    /**
     * 活动审核驳回
     */
    @PostMapping("/{id}/reject")
    public Result rejectActivity(@PathVariable Integer id,
                                   @RequestBody Map<String, String> body,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        String reason = body.get("reason");
        return activityService.rejectActivity(id, reason, operatorId);
    }

    /**
     * 取消活动
     */
    @PostMapping("/{id}/cancel")
    public Result cancelActivity(@PathVariable Integer id,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return activityService.cancelActivity(id, operatorId);
    }

    /**
     * 获取活动报名列表
     */
    @GetMapping("/{id}/participants")
    public Result getActivityParticipants(@PathVariable Integer id) {
        return activityService.getActivityParticipants(id);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "activity-service"));
    }
}
