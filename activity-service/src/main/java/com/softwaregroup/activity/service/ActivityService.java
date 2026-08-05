package com.softwaregroup.activity.service;

import com.softwaregroup.activity.dao.ActivityDAO;
import com.softwaregroup.activity.dao.ActivityParticipantDAO;
import com.softwaregroup.activity.feign.UserFeignClient;
import com.softwaregroup.activity.model.dto.ActivityDTO;
import com.softwaregroup.activity.model.dto.ActivityFilterDTO;
import com.softwaregroup.activity.model.entity.Activity;
import com.softwaregroup.activity.model.entity.Registration;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 活动服务层
 */
@Service
public class ActivityService {

    private static final int CODE_SUCCESS = 0;
    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_FORBIDDEN = 403;
    private static final int CODE_INTERNAL_ERROR = 500;

    private static final String STATUS_UPCOMING = "upcoming";
    private static final String STATUS_ONGOING = "ongoing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";
    private static final String APPROVAL_PENDING = "pending";
    private static final String APPROVAL_APPROVED = "approved";
    private static final String APPROVAL_REJECTED = "rejected";
    private static final String REG_STATUS_PENDING = "pending";
    private static final String REG_STATUS_CONFIRMED = "confirmed";
    private static final String REG_STATUS_REJECTED = "rejected";

    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_LOCATION_LENGTH = 200;
    private static final int MAX_REJECT_REASON_LENGTH = 500;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private ActivityDAO activityDAO;

    @Autowired
    private ActivityParticipantDAO registrationDAO;

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 创建活动
     */
    public Result createActivity(ActivityDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "请求参数不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "标题不能为空");
        }
        if (dto.getActivityType() == null || dto.getActivityType().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "活动类型不能为空");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            return Result.error(CODE_BAD_REQUEST, "描述不能超过" + MAX_DESCRIPTION_LENGTH + "字符");
        }
        if (dto.getLocation() != null && dto.getLocation().length() > MAX_LOCATION_LENGTH) {
            return Result.error(CODE_BAD_REQUEST, "地点不能超过" + MAX_LOCATION_LENGTH + "字符");
        }
        if (dto.getMaxParticipants() != null && dto.getMaxParticipants() < 0) {
            return Result.error(CODE_BAD_REQUEST, "最大参与人数不能为负数");
        }

        Date regStart = dto.getRegistrationStartTime();
        Date regEnd = dto.getRegistrationEndTime();
        if (regStart != null && regEnd != null && regStart.after(regEnd)) {
            return Result.error(CODE_BAD_REQUEST, "报名开始时间不能晚于结束时间");
        }

        Date actStart = dto.getActivityStartTime();
        Date actEnd = dto.getActivityEndTime();
        if (regEnd != null && actStart != null && actStart.before(regEnd)) {
            return Result.error(CODE_BAD_REQUEST, "活动开始时间应晚于报名截止时间");
        }

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle().trim());
        activity.setDescription(dto.getDescription());
        activity.setActivityType(dto.getActivityType());
        activity.setActivityStartTime(dto.getActivityStartTime());
        activity.setActivityEndTime(dto.getActivityEndTime());
        activity.setLocation(dto.getLocation());
        activity.setOrganizers(dto.getOrganizers());
        activity.setContactInfo(dto.getContactInfo());
        activity.setRegistrationStartTime(dto.getRegistrationStartTime());
        activity.setRegistrationEndTime(dto.getRegistrationEndTime());
        activity.setMaxParticipants(dto.getMaxParticipants() != null ? dto.getMaxParticipants() : 0);
        activity.setStatus(STATUS_UPCOMING);
        activity.setApprovalStatus(APPROVAL_PENDING);
        activity.setCreatorId(userId);

        Activity saved = activityDAO.insert(activity);
        return Result.ok(saved.getId());
    }

    /**
     * 更新活动
     */
    public Result updateActivity(Integer id, ActivityDTO dto, Integer userId) {
        if (id == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "请求参数不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "标题不能为空");
        }

        Activity activity = activityDAO.findById(id);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        Result authResult = checkModifyPermission(activity, userId);
        if (!authResult.isSuccess()) {
            return authResult;
        }

        if (isActivityOngoing(activity)) {
            return Result.error(CODE_BAD_REQUEST, "活动已开始或已结束，无法修改");
        }

        activity.setTitle(dto.getTitle().trim());
        activity.setDescription(dto.getDescription());
        activity.setActivityType(dto.getActivityType());
        activity.setActivityStartTime(dto.getActivityStartTime());
        activity.setActivityEndTime(dto.getActivityEndTime());
        activity.setLocation(dto.getLocation());
        activity.setOrganizers(dto.getOrganizers());
        activity.setContactInfo(dto.getContactInfo());
        activity.setRegistrationStartTime(dto.getRegistrationStartTime());
        activity.setRegistrationEndTime(dto.getRegistrationEndTime());
        if (dto.getMaxParticipants() != null) {
            activity.setMaxParticipants(dto.getMaxParticipants());
        }

        activityDAO.update(activity);
        return Result.ok(activity.getId());
    }

    /**
     * 删除活动
     */
    public Result deleteActivity(Integer id, Integer userId) {
        if (id == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }

        Activity activity = activityDAO.findById(id);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        Result authResult = checkModifyPermission(activity, userId);
        if (!authResult.isSuccess()) {
            return authResult;
        }

        if (STATUS_ONGOING.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动进行中，无法删除");
        }
        if (STATUS_COMPLETED.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动已结束，无法删除");
        }

        int confirmedCount = registrationDAO.getParticipantCount(id, REG_STATUS_CONFIRMED);
        if (confirmedCount > 0) {
            return Result.error(CODE_BAD_REQUEST, "已有确认报名，无法删除");
        }

        registrationDAO.deleteByActivityId(id);
        activityDAO.delete(id);
        return Result.ok();
    }

    /**
     * 报名活动
     */
    public Result register(Integer activityId, Integer userId) {
        if (activityId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }

        Activity activity = activityDAO.findById(activityId);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        if (!APPROVAL_APPROVED.equals(activity.getApprovalStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动未通过审核");
        }
        if (STATUS_CANCELED.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动已取消");
        }
        if (isRegistrationEnded(activity)) {
            return Result.error(CODE_BAD_REQUEST, "报名已截止");
        }
        if (isRegistrationNotStarted(activity)) {
            return Result.error(CODE_BAD_REQUEST, "报名未开始");
        }
        if (userId.equals(activity.getCreatorId())) {
            return Result.error(CODE_BAD_REQUEST, "不能报名自己创建的活动");
        }
        if (registrationDAO.isRegistered(activityId, userId)) {
            return Result.error(CODE_BAD_REQUEST, "您已报名此活动");
        }

        int maxParticipants = activity.getMaxParticipants();
        if (maxParticipants > 0) {
            int confirmed = registrationDAO.getParticipantCount(activityId, REG_STATUS_CONFIRMED);
            int pending = registrationDAO.getParticipantCount(activityId, REG_STATUS_PENDING);
            if (confirmed + pending >= maxParticipants) {
                return Result.error(CODE_BAD_REQUEST, "活动报名已满");
            }
        }

        boolean success = registrationDAO.register(activityId, userId);
        return success ? Result.ok() : Result.error(CODE_INTERNAL_ERROR, "报名失败");
    }

    /**
     * 审批参与者通过
     */
    public Result approveParticipant(Integer activityId, Integer userId, Integer operatorId) {
        if (activityId == null || userId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID和用户ID不能为空");
        }

        Result adminCheck = requireAdmin(activityId, operatorId);
        if (!adminCheck.isSuccess()) return adminCheck;

        Activity activity = (Activity) adminCheck.getData();

        String status = registrationDAO.getRegistrationStatus(activityId, userId);
        if (status == null) {
            return Result.error(CODE_NOT_FOUND, "报名记录不存在");
        }
        if (!REG_STATUS_PENDING.equals(status)) {
            return Result.error(CODE_BAD_REQUEST, "该报名非待审核状态");
        }

        int maxParticipants = activity.getMaxParticipants();
        if (maxParticipants > 0 && registrationDAO.getParticipantCount(activityId, REG_STATUS_CONFIRMED) >= maxParticipants) {
            return Result.error(CODE_BAD_REQUEST, "超过最大人数");
        }

        boolean success = registrationDAO.updateStatus(activityId, userId, REG_STATUS_CONFIRMED, "审批通过");
        return success ? Result.ok() : Result.error(CODE_INTERNAL_ERROR, "审批失败");
    }

    /**
     * 拒绝参与者
     */
    public Result rejectParticipant(Integer activityId, Integer userId, Integer operatorId) {
        if (activityId == null || userId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID和用户ID不能为空");
        }

        Result adminCheck = requireAdmin(activityId, operatorId);
        if (!adminCheck.isSuccess()) return adminCheck;

        String status = registrationDAO.getRegistrationStatus(activityId, userId);
        if (status == null) {
            return Result.error(CODE_NOT_FOUND, "报名记录不存在");
        }
        if (REG_STATUS_CONFIRMED.equals(status)) {
            return Result.error(CODE_BAD_REQUEST, "已确认的报名无法拒绝");
        }

        boolean success = registrationDAO.updateStatus(activityId, userId, REG_STATUS_REJECTED, "不符合报名条件");
        return success ? Result.ok() : Result.error(CODE_INTERNAL_ERROR, "拒绝失败");
    }

    /**
     * 批量审批通过
     */
    public Result batchApprove(Integer activityId, List<Integer> userIds, Integer operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.ok();
        }

        Result adminCheck = requireAdmin(activityId, operatorId);
        if (!adminCheck.isSuccess()) return adminCheck;

        Activity activity = (Activity) adminCheck.getData();

        int maxParticipants = activity.getMaxParticipants();
        if (maxParticipants > 0) {
            int confirmed = registrationDAO.getParticipantCount(activityId, REG_STATUS_CONFIRMED);
            long validCount = userIds.stream()
                    .filter(uid -> REG_STATUS_PENDING.equals(registrationDAO.getRegistrationStatus(activityId, uid)))
                    .count();
            if (confirmed + validCount > maxParticipants) {
                return Result.error(CODE_BAD_REQUEST, "批量审批后总人数将超过最大限制");
            }
        }

        int count = registrationDAO.batchUpdateStatus(userIds, activityId, REG_STATUS_CONFIRMED);
        return Result.ok(count);
    }

    /**
     * 批量拒绝
     */
    public Result batchReject(Integer activityId, List<Integer> userIds, Integer operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.ok();
        }

        Result adminCheck = requireAdmin(activityId, operatorId);
        if (!adminCheck.isSuccess()) return adminCheck;

        int count = registrationDAO.batchUpdateStatus(userIds, activityId, REG_STATUS_REJECTED);
        return Result.ok(count);
    }

    /**
     * 活动审核通过
     */
    public Result approveActivity(Integer activityId, Integer operatorId) {
        Result adminCheck = requireAdmin(activityId, operatorId);
        if (!adminCheck.isSuccess()) return adminCheck;

        Activity activity = (Activity) adminCheck.getData();
        if (!APPROVAL_PENDING.equals(activity.getApprovalStatus())) {
            return Result.error(CODE_BAD_REQUEST, "该活动已审核");
        }

        boolean success = activityDAO.approveActivity(activityId);
        return success ? Result.ok() : Result.error(CODE_INTERNAL_ERROR, "审核失败");
    }

    /**
     * 活动审核驳回
     */
    public Result rejectActivity(Integer activityId, String reason, Integer operatorId) {
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "拒绝原因不能为空");
        }
        if (reason.length() > MAX_REJECT_REASON_LENGTH) {
            return Result.error(CODE_BAD_REQUEST, "拒绝原因不能超过" + MAX_REJECT_REASON_LENGTH + "字符");
        }

        Result adminCheck = requireAdmin(activityId, operatorId);
        if (!adminCheck.isSuccess()) return adminCheck;

        Activity activity = (Activity) adminCheck.getData();
        if (!APPROVAL_PENDING.equals(activity.getApprovalStatus())) {
            return Result.error(CODE_BAD_REQUEST, "该活动已审核");
        }

        boolean success = activityDAO.rejectActivity(activityId);
        return success ? Result.ok() : Result.error(CODE_INTERNAL_ERROR, "拒绝失败");
    }

    /**
     * 取消活动
     */
    public Result cancelActivity(Integer activityId, Integer operatorId) {
        if (activityId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }

        Activity activity = activityDAO.findById(activityId);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        Result authResult = checkModifyPermission(activity, operatorId);
        if (!authResult.isSuccess()) {
            return authResult;
        }

        if (!isCancellable(activity)) {
            String msg = STATUS_CANCELED.equals(activity.getStatus()) ? "活动已取消" : "活动已结束";
            return Result.error(CODE_BAD_REQUEST, msg);
        }

        activity.setStatus(STATUS_CANCELED);
        activityDAO.update(activity);
        return Result.ok();
    }

    /**
     * 活动列表
     */
    public Result listActivities(ActivityFilterDTO filter) {
        String keyword = filter != null ? filter.getKeyword() : null;
        String activityType = filter != null ? filter.getActivityType() : null;
        String status = filter != null ? filter.getStatus() : null;
        String approvalStatus = filter != null ? filter.getApprovalStatus() : null;

        List<Activity> list = activityDAO.findByConditions(keyword, activityType, status, approvalStatus);
        return Result.ok(list);
    }

    /**
     * 活动详情
     */
    public Result getActivityDetail(Integer id, Integer userId) {
        if (id == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }

        Activity activity = activityDAO.findById(id);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        if (userId != null) {
            boolean registered = registrationDAO.isRegistered(id, userId);
            activity.setRegisteredByCurrentUser(registered);
        }

        int confirmedCount = registrationDAO.getParticipantCount(id, REG_STATUS_CONFIRMED);
        int pendingCount = registrationDAO.getParticipantCount(id, REG_STATUS_PENDING);
        activity.setCurrentParticipants(confirmedCount + pendingCount);

        return Result.ok(activity);
    }

    /**
     * 我报名的活动
     */
    public Result getMyActivities(Integer userId) {
        if (userId == null) {
            return Result.error(CODE_BAD_REQUEST, "用户ID不能为空");
        }

        List<Registration> registrations = registrationDAO.findByUserId(userId);
        return Result.ok(registrations);
    }

    /**
     * 我创建的活动
     */
    public Result getMyCreatedActivities(Integer userId) {
        if (userId == null) {
            return Result.error(CODE_BAD_REQUEST, "用户ID不能为空");
        }

        List<Activity> activities = activityDAO.findByCreatorId(userId);
        return Result.ok(activities);
    }

    /**
     * 获取活动的报名列表
     */
    public Result getActivityParticipants(Integer activityId) {
        if (activityId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }

        Activity activity = activityDAO.findById(activityId);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        List<Registration> registrations = registrationDAO.findByActivityId(activityId);
        return Result.ok(registrations);
    }

    // ========== 私有辅助方法 ==========

    private boolean isActivityOngoing(Activity activity) {
        return STATUS_ONGOING.equals(activity.getStatus()) || STATUS_COMPLETED.equals(activity.getStatus());
    }

    private boolean isRegistrationEnded(Activity activity) {
        if (activity == null || activity.getRegistrationEndTime() == null) {
            return true;
        }
        return new Date().after(activity.getRegistrationEndTime());
    }

    private boolean isRegistrationNotStarted(Activity activity) {
        if (activity == null || activity.getRegistrationStartTime() == null) {
            return true;
        }
        return new Date().before(activity.getRegistrationStartTime());
    }

    private boolean isCancellable(Activity activity) {
        if (activity == null) {
            return false;
        }
        return !STATUS_CANCELED.equals(activity.getStatus()) && !STATUS_COMPLETED.equals(activity.getStatus());
    }

    private Result checkModifyPermission(Activity activity, Integer operatorId) {
        Result adminCheck = requireAdmin(null, operatorId);
        if (adminCheck.isSuccess()) {
            return Result.ok(activity);
        }

        // 检查是否为创建者
        if (operatorId != null && operatorId.equals(activity.getCreatorId())) {
            return Result.ok(activity);
        }

        return Result.error(CODE_FORBIDDEN, "无权修改此活动");
    }

    private Result requireAdmin(Integer activityId, Integer operatorId) {
        if (operatorId == null) {
            return Result.error(CODE_BAD_REQUEST, "操作员ID不能为空");
        }

        try {
            Result result = userFeignClient.getUserRole(operatorId);
            if (!result.isSuccess()) {
                return Result.error(CODE_NOT_FOUND, "用户不存在");
            }
            String role = (String) result.getData();
            if (!"ADMIN".equals(role)) {
                return Result.error(CODE_FORBIDDEN, "需要管理员权限");
            }
        } catch (Exception e) {
            return Result.error(CODE_INTERNAL_ERROR, "用户验证失败");
        }

        if (activityId != null) {
            Activity activity = activityDAO.findById(activityId);
            if (activity == null) {
                return Result.error(CODE_NOT_FOUND, "活动不存在");
            }
            return Result.ok(activity);
        }
        return Result.ok();
    }
}
