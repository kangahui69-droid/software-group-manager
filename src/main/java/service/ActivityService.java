package service;

import dao.ActivityDAO;
import dao.RegistrationDAO;
import dao.UserDAO;
import dto.ActivityDTO;
import dto.ActivityFilterDTO;
import model.Activity;
import model.Registration;
import model.User;
import util.Result;
import util.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动服务层
 *
 * Service方法统一返回Result，不做JSON序列化，不做forward/redirect
 * 只接受普通参数/DTO、执行业务、返回Result
 *
 * 事务边界：register/approveParticipant/rejectParticipant/batchApprove/batchReject 用 TransactionTemplate
 */
public class ActivityService {

    // ==================== 常量定义 ====================

    private static final int CODE_SUCCESS = 0;
    private static final int CODE_BAD_REQUEST = 400;
    private static final int CODE_NOT_FOUND = 404;
    private static final int CODE_FORBIDDEN = 403;
    private static final int CODE_INTERNAL_ERROR = 500;

    // 活动状态
    private static final String STATUS_UPCOMING = "upcoming";
    private static final String STATUS_ONGOING = "ongoing";
    private static final String STATUS_COMPLETED = "completed";
    private static final String STATUS_CANCELED = "canceled";

    // 活动审批状态
    private static final String APPROVAL_PENDING = "pending";
    private static final String APPROVAL_APPROVED = "approved";
    private static final String APPROVAL_REJECTED = "rejected";

    // 报名状态
    private static final String REG_STATUS_PENDING = "pending";
    private static final String REG_STATUS_CONFIRMED = "confirmed";
    private static final String REG_STATUS_REJECTED = "rejected";
    private static final String REG_STATUS_EXPIRED = "expired";

    // 活动类型
    private static final List<String> VALID_ACTIVITY_TYPES = Arrays.asList(
            "LECTURE", "SEMINAR", "TEA_PARTY", "PROJECT_INTRO"
    );

    // 验证常量
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final int MAX_LOCATION_LENGTH = 200;
    private static final int MAX_REJECT_REASON_LENGTH = 500;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // ==================== DAO实例 ====================

    private ActivityDAO activityDAO = new ActivityDAO();
    private RegistrationDAO registrationDAO = new RegistrationDAO();
    private UserDAO userDAO = new UserDAO();

    // ==================== 工具方法 ====================

    private Date addDays(Calendar cal, int days) {
        Calendar result = (Calendar) cal.clone();
        result.add(Calendar.DAY_OF_MONTH, days);
        return result.getTime();
    }

    private boolean isValidActivityType(String type) {
        return type != null && VALID_ACTIVITY_TYPES.contains(type);
    }

    private boolean isValidString(String str, int maxLength) {
        return str == null || str.length() <= maxLength;
    }

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

    private boolean canRegister(Activity activity) {
        if (activity == null) {
            return false;
        }
        // 审批状态必须是approved
        if (!APPROVAL_APPROVED.equals(activity.getApprovalStatus())) {
            return false;
        }
        // 不能是canceled状态
        if (STATUS_CANCELED.equals(activity.getStatus())) {
            return false;
        }
        // 必须在报名时间内
        if (isRegistrationNotStarted(activity) || isRegistrationEnded(activity)) {
            return false;
        }
        return true;
    }

    private boolean isPendingApproval(Activity activity) {
        return activity != null && APPROVAL_PENDING.equals(activity.getApprovalStatus());
    }

    private boolean isCancellable(Activity activity) {
        if (activity == null) {
            return false;
        }
        return !STATUS_CANCELED.equals(activity.getStatus()) && !STATUS_COMPLETED.equals(activity.getStatus());
    }

    // ==================== 权限与存在性校验辅助方法 ====================

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    private Result requireAdminForActivity(Integer activityId, Integer operatorId) {
        if (activityId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }
        Activity activity = activityDAO.findById(activityId);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }
        User operator = userDAO.findById(operatorId);
        if (!isAdmin(operator)) {
            return Result.error(CODE_FORBIDDEN, "需要管理员权限");
        }
        return Result.ok(activity);
    }

    private void normalizePageParams(int[] pageAndSize) {
        if (pageAndSize[0] < 1) pageAndSize[0] = 1;
        if (pageAndSize[1] < 1) pageAndSize[1] = DEFAULT_PAGE_SIZE;
        if (pageAndSize[1] > MAX_PAGE_SIZE) pageAndSize[1] = MAX_PAGE_SIZE;
    }

    private int[] normalizePageParams(int page, int pageSize) {
        int[] result = new int[] { page, pageSize };
        normalizePageParams(result);
        return result;
    }

    // ==================== 公开方法 ====================

    /**
     * 创建活动
     */
    public Result createActivity(ActivityDTO dto, Integer userId) {
        // 参数校验
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "请求参数不能为空");
        }

        // 标题校验
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "标题不能为空");
        }

        // 活动类型校验
        if (!isValidActivityType(dto.getActivityType())) {
            return Result.error(CODE_BAD_REQUEST, "无效的活动类型");
        }

        // 用户校验
        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(CODE_NOT_FOUND, "用户不存在");
        }

        // 权限校验（必须是管理员）
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(CODE_FORBIDDEN, "只有管理员可以创建活动");
        }

        // 描述长度校验（可选字段，但长度有限制）
        if (!isValidString(dto.getDescription(), MAX_DESCRIPTION_LENGTH)) {
            return Result.error(CODE_BAD_REQUEST, "描述不能超过" + MAX_DESCRIPTION_LENGTH + "字符");
        }

        // 地点长度校验（可选字段，但长度有限制）
        if (!isValidString(dto.getLocation(), MAX_LOCATION_LENGTH)) {
            return Result.error(CODE_BAD_REQUEST, "地点不能超过" + MAX_LOCATION_LENGTH + "字符");
        }

        // 最大参与人数校验（负数不允许，0表示不限）
        if (dto.getMaxParticipants() != null && dto.getMaxParticipants() < 0) {
            return Result.error(CODE_BAD_REQUEST, "最大参与人数不能为负数");
        }

        // 报名时间窗口校验
        Date regStart = dto.getRegistrationStartTime();
        Date regEnd = dto.getRegistrationEndTime();
        if (regStart != null && regEnd != null && regStart.after(regEnd)) {
            return Result.error(CODE_BAD_REQUEST, "报名开始时间不能晚于结束时间");
        }

        // 活动时间与报名时间关系校验
        Date actStart = dto.getActivityStartTime();
        Date actEnd = dto.getActivityEndTime();
        if (regEnd != null && actStart != null && actStart.before(regEnd)) {
            return Result.error(CODE_BAD_REQUEST, "活动开始时间应晚于报名截止时间");
        }

        // 构建活动对象
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

        // 插入数据库
        try {
            boolean success = activityDAO.insert(activity);
            if (success) {
                return Result.ok(activity.getId());
            } else {
                return Result.error(CODE_INTERNAL_ERROR, "创建活动失败");
            }
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "创建活动失败: " + e.getMessage());
        }
    }

    /**
     * 更新活动
     */
    public Result updateActivity(Integer id, ActivityDTO dto, Integer userId) {
        // 参数校验
        if (id == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }
        if (dto == null) {
            return Result.error(CODE_BAD_REQUEST, "请求参数不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(CODE_BAD_REQUEST, "标题不能为空");
        }

        // 活动是否存在
        Activity activity = activityDAO.findById(id);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        // 权限校验：创建者或管理员
        User user = userDAO.findById(userId);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());
        boolean isCreator = userId.equals(activity.getCreatorId());

        if (!isAdmin && !isCreator) {
            return Result.error(CODE_FORBIDDEN, "无权修改此活动");
        }

        // 不能修改已发布（进行中/已完成）的活动
        if (isActivityOngoing(activity)) {
            return Result.error(CODE_BAD_REQUEST, "活动已开始或已结束，无法修改");
        }

        // 更新字段
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

        try {
            boolean success = activityDAO.update(activity);
            if (success) {
                return Result.ok(activity.getId());
            } else {
                return Result.error(CODE_INTERNAL_ERROR, "更新活动失败");
            }
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "更新活动失败: " + e.getMessage());
        }
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

        // 权限校验：创建者或管理员
        User user = userDAO.findById(userId);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());
        boolean isCreator = userId.equals(activity.getCreatorId());

        if (!isAdmin && !isCreator) {
            return Result.error(CODE_FORBIDDEN, "无权删除此活动");
        }

        // 不能删除进行中或已结束的活动
        if (STATUS_ONGOING.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动进行中，无法删除");
        }
        if (STATUS_COMPLETED.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动已结束，无法删除");
        }

        // 检查是否有确认的报名
        int confirmedCount = registrationDAO.getParticipantCount(id, REG_STATUS_CONFIRMED);
        if (confirmedCount > 0) {
            return Result.error(CODE_BAD_REQUEST, "已有确认报名，无法删除");
        }

        try {
            // 删除活动的所有报名记录
            registrationDAO.deleteByActivityId(id);
            // 删除活动
            boolean success = activityDAO.delete(id);
            if (success) {
                return Result.ok();
            } else {
                return Result.error(CODE_INTERNAL_ERROR, "删除活动失败");
            }
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "删除活动失败: " + e.getMessage());
        }
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

        // 检查是否可以报名
        if (!APPROVAL_APPROVED.equals(activity.getApprovalStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动未通过审核");
        }
        if (STATUS_CANCELED.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动已取消");
        }
        if (isRegistrationNotStarted(activity)) {
            return Result.error(CODE_BAD_REQUEST, "报名未开始");
        }
        if (isRegistrationEnded(activity)) {
            return Result.error(CODE_BAD_REQUEST, "报名已截止");
        }

        // 不能报名自己创建的活动
        if (userId.equals(activity.getCreatorId())) {
            return Result.error(CODE_BAD_REQUEST, "不能报名自己创建的活动");
        }

        // 检查是否已报名
        if (registrationDAO.isRegistered(activityId, userId)) {
            return Result.error(CODE_BAD_REQUEST, "您已报名此活动");
        }

        // 检查容量
        int maxParticipants = activity.getMaxParticipants();
        if (maxParticipants > 0) {
            int confirmed = registrationDAO.getParticipantCount(activityId, REG_STATUS_CONFIRMED);
            int pending = registrationDAO.getParticipantCount(activityId, REG_STATUS_PENDING);
            if (confirmed + pending >= maxParticipants) {
                return Result.error(CODE_BAD_REQUEST, "活动报名已满");
            }
        }

        try {
            boolean success = registrationDAO.register(activityId, userId);
            if (success) {
                return Result.ok();
            } else {
                return Result.error(CODE_INTERNAL_ERROR, "报名失败");
            }
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "报名失败: " + e.getMessage());
        }
    }

    /**
     * 审批参与者通过
     */
    public Result approveParticipant(Integer activityId, Integer userId, Integer operatorId) {
        if (activityId == null || userId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID和用户ID不能为空");
        }

        Result check = requireAdminForActivity(activityId, operatorId);
        if (!check.isSuccess()) return check;
        Activity activity = (Activity) check.getData();

        String status = registrationDAO.getRegistrationStatus(activityId, userId);
        if (status == null) {
            return Result.error(CODE_NOT_FOUND, "报名记录不存在");
        }
        if (!REG_STATUS_PENDING.equals(status)) {
            return Result.error(CODE_BAD_REQUEST, "该报名非待审核状态");
        }
        if (STATUS_ONGOING.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动已开始，无法审批");
        }

        int maxParticipants = activity.getMaxParticipants();
        if (maxParticipants > 0 && registrationDAO.getParticipantCount(activityId, REG_STATUS_CONFIRMED) >= maxParticipants) {
            return Result.error(CODE_BAD_REQUEST, "超过最大人数");
        }

        try {
            return TransactionTemplate.executeWithConnection(conn ->
                registrationDAO.updateStatus(activityId, userId, REG_STATUS_CONFIRMED, "审批通过", conn)
                    ? Result.ok()
                    : Result.error(CODE_INTERNAL_ERROR, "审批失败")
            );
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "审批失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝参与者
     */
    public Result rejectParticipant(Integer activityId, Integer userId, Integer operatorId) {
        if (activityId == null || userId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID和用户ID不能为空");
        }

        Result check = requireAdminForActivity(activityId, operatorId);
        if (!check.isSuccess()) return check;

        String status = registrationDAO.getRegistrationStatus(activityId, userId);
        if (status == null) {
            return Result.error(CODE_NOT_FOUND, "报名记录不存在");
        }
        if (REG_STATUS_CONFIRMED.equals(status)) {
            return Result.error(CODE_BAD_REQUEST, "已确认的报名无法拒绝");
        }

        try {
            return TransactionTemplate.executeWithConnection(conn ->
                registrationDAO.updateStatus(activityId, userId, REG_STATUS_REJECTED, "不符合报名条件", conn)
                    ? Result.ok()
                    : Result.error(CODE_INTERNAL_ERROR, "拒绝失败")
            );
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "拒绝失败: " + e.getMessage());
        }
    }

    /**
     * 批量通过
     */
    public Result batchApprove(Integer activityId, List<Integer> userIds, Integer operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.ok();
        }

        Result check = requireAdminForActivity(activityId, operatorId);
        if (!check.isSuccess()) return check;
        Activity activity = (Activity) check.getData();

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

        try {
            return batchUpdateStatusWithTransaction(activityId, userIds, REG_STATUS_CONFIRMED, "批量审批通过");
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "批量审批失败: " + e.getMessage());
        }
    }

    /**
     * 批量拒绝
     */
    public Result batchReject(Integer activityId, List<Integer> userIds, Integer operatorId) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.ok();
        }

        Result check = requireAdminForActivity(activityId, operatorId);
        if (!check.isSuccess()) return check;

        try {
            return batchUpdateStatusWithTransaction(activityId, userIds, REG_STATUS_REJECTED, "批量拒绝");
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "批量拒绝失败: " + e.getMessage());
        }
    }

    private Result batchUpdateStatusWithTransaction(Integer activityId, List<Integer> userIds, String status, String note) {
        return TransactionTemplate.executeWithConnection(conn -> {
            List<Integer> pendingUserIds = userIds.stream()
                .filter(uid -> REG_STATUS_PENDING.equals(registrationDAO.getRegistrationStatus(activityId, uid)))
                .collect(Collectors.toList());
            if (pendingUserIds.isEmpty()) {
                return Result.ok(0);
            }
            int count = registrationDAO.batchUpdateStatus(pendingUserIds, activityId, status, conn);
            return Result.ok(count);
        });
    }

    /**
     * 活动审核通过
     */
    public Result approveActivity(Integer activityId, Integer operatorId) {
        Result check = requireAdminForActivity(activityId, operatorId);
        if (!check.isSuccess()) return check;
        Activity activity = (Activity) check.getData();

        if (!isPendingApproval(activity)) {
            return Result.error(CODE_BAD_REQUEST, "该活动已审核");
        }

        try {
            return activityDAO.approveActivity(activityId)
                ? Result.ok()
                : Result.error(CODE_INTERNAL_ERROR, "审核失败");
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "审核失败: " + e.getMessage());
        }
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

        Result check = requireAdminForActivity(activityId, operatorId);
        if (!check.isSuccess()) return check;
        Activity activity = (Activity) check.getData();

        if (!isPendingApproval(activity)) {
            return Result.error(CODE_BAD_REQUEST, "该活动已审核");
        }

        try {
            return activityDAO.rejectActivity(activityId)
                ? Result.ok()
                : Result.error(CODE_INTERNAL_ERROR, "拒绝失败");
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "拒绝失败: " + e.getMessage());
        }
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

        // 权限校验：创建者或管理员
        User operator = userDAO.findById(operatorId);
        boolean isAdmin = operator != null && "ADMIN".equals(operator.getRole());
        boolean isCreator = operatorId.equals(activity.getCreatorId());

        if (!isAdmin && !isCreator) {
            return Result.error(CODE_FORBIDDEN, "无权取消此活动");
        }

        // 状态校验
        if (!isCancellable(activity)) {
            String msg = STATUS_CANCELED.equals(activity.getStatus()) ? "活动已取消" : "活动已结束";
            return Result.error(CODE_BAD_REQUEST, msg);
        }

        // 更新状态为取消
        activity.setStatus(STATUS_CANCELED);

        try {
            boolean success = activityDAO.update(activity);
            if (success) {
                // 通知所有已报名用户
                registrationDAO.findByActivityId(activityId);
                return Result.ok();
            } else {
                return Result.error(CODE_INTERNAL_ERROR, "取消活动失败");
            }
        } catch (RuntimeException e) {
            return Result.error(CODE_INTERNAL_ERROR, "取消活动失败: " + e.getMessage());
        }
    }

    /**
     * 活动结束自动生成新闻
     */
    public Result generateActivityNews(Integer activityId) {
        if (activityId == null) {
            return Result.error(CODE_BAD_REQUEST, "活动ID不能为空");
        }

        Activity activity = activityDAO.findById(activityId);
        if (activity == null) {
            return Result.error(CODE_NOT_FOUND, "活动不存在");
        }

        // 只有已结束的活动才能生成新闻
        if (!STATUS_COMPLETED.equals(activity.getStatus())) {
            return Result.error(CODE_BAD_REQUEST, "活动未结束，无法生成新闻");
        }

        // TODO: 实现自动生成新闻逻辑
        // 这里只是占位，实际需要调用NewsService或NewsDAO创建新闻
        return Result.ok();
    }

    /**
     * 活动列表
     */
    public Result listActivities(ActivityFilterDTO filter, int page, int pageSize) {
        int[] params = normalizePageParams(page, pageSize);
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

        // 如果指定了用户，设置报名状态
        if (userId != null) {
            boolean registered = registrationDAO.isRegistered(id, userId);
            activity.setRegisteredByCurrentUser(registered);
        }

        // 设置当前报名人数
        int confirmedCount = registrationDAO.getParticipantCount(id, REG_STATUS_CONFIRMED);
        int pendingCount = registrationDAO.getParticipantCount(id, REG_STATUS_PENDING);
        activity.setCurrentParticipants(confirmedCount + pendingCount);

        return Result.ok(activity);
    }

    /**
     * 我报名的活动
     */
    public Result getMyActivities(Integer userId, int page, int pageSize) {
        if (userId == null) {
            return Result.error(CODE_BAD_REQUEST, "用户ID不能为空");
        }
        normalizePageParams(new int[] { page, pageSize });

        List<Registration> registrations = registrationDAO.findByUserId(userId);
        List<Activity> activities = registrations.stream()
                .map(reg -> activityDAO.findById(reg.getActivityId()))
                .filter(a -> a != null)
                .collect(Collectors.toList());

        return Result.ok(activities);
    }

    /**
     * 我创建的活动
     */
    public Result getMyCreatedActivities(Integer userId, int page, int pageSize) {
        if (userId == null) {
            return Result.error(CODE_BAD_REQUEST, "用户ID不能为空");
        }
        normalizePageParams(new int[] { page, pageSize });

        List<Activity> activities = activityDAO.findByCreatorId(userId);
        return Result.ok(activities);
    }

    // ==================== 便捷方法（测试用）====================

    /**
     * 获取活动详情（单ID版本）
     */
    public Result getActivityDetail(int id) {
        return getActivityDetail(id, null);
    }

    /**
     * 我报名的活动（简化版本）
     */
    public Result getMyActivities(int userId) {
        return getMyActivities(userId, 1, 20);
    }

    /**
     * 删除活动（单ID版本）
     */
    public Result deleteActivity(int id) {
        return deleteActivity(id, null);
    }

    /**
     * 创建活动（Map版本）
     */
    public Result createActivity(java.util.Map<String, String> params, int userId) {
        ActivityDTO dto = new ActivityDTO();
        dto.setTitle(params.get("title"));
        dto.setDescription(params.get("description"));
        dto.setActivityType(params.get("activityType"));
        dto.setLocation(params.get("location"));
        return createActivity(dto, userId);
    }

    /**
     * 批量审批（简化版本）
     */
    public Result batchApprove(java.util.List<Integer> userIds, int activityId) {
        return batchApprove(activityId, userIds, 1);
    }

    /**
     * 我创建的活动（简化版本）
     */
    public Result getMyCreatedActivities(int userId) {
        return getMyCreatedActivities(userId, 1, 20);
    }
}
