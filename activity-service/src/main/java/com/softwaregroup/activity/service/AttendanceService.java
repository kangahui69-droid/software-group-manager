package com.softwaregroup.activity.service;

import com.softwaregroup.activity.dao.AttendanceDAO;
import com.softwaregroup.activity.dao.AttendanceMakeupDAO;
import com.softwaregroup.activity.feign.UserFeignClient;
import com.softwaregroup.activity.model.entity.Attendance;
import com.softwaregroup.activity.model.entity.AttendanceMakeup;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 考勤服务层
 */
@Service
public class AttendanceService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_LIST_LIMIT = 100;

    private static final String CHECK_IN_STATUS_NORMAL = "NORMAL";
    private static final String CHECK_IN_STATUS_LATE = "LATE";
    private static final String CHECK_OUT_STATUS_NORMAL = "NORMAL";
    private static final String CHECK_OUT_STATUS_EARLY = "EARLY";
    private static final String MAKEUP_TYPE_CHECK_IN = "CHECK_IN";
    private static final String MAKEUP_TYPE_CHECK_OUT = "CHECK_OUT";
    private static final String MAKEUP_STATUS_PENDING = "PENDING";
    private static final String MAKEUP_STATUS_APPROVED = "APPROVED";
    private static final String MAKEUP_STATUS_REJECTED = "REJECTED";

    private static final int LATE_THRESHOLD_HOUR = 9;
    private static final int LATE_THRESHOLD_MINUTE = 30;
    private static final int EARLY_THRESHOLD_HOUR = 17;
    private static final int EARLY_THRESHOLD_MINUTE = 30;

    @Autowired
    private AttendanceDAO attendanceDAO;

    @Autowired
    private AttendanceMakeupDAO attendanceMakeupDAO;

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 签到
     */
    public Result checkIn(Integer userId) {
        Result validation = validateUserId(userId);
        if (validation != null) return validation;

        try {
            Attendance today = attendanceDAO.getTodayAttendance(userId);
            if (today != null && today.getCheckInTime() != null) {
                return Result.error(400, "您今日已签到，无需重复签到");
            }

            String status = determineCheckInStatus();
            Attendance attendance = buildCheckInAttendance(userId, status);
            attendanceDAO.checkIn(attendance);
            return Result.ok(status);
        } catch (Exception e) {
            return Result.error(500, "签到失败: " + e.getMessage());
        }
    }

    /**
     * 签退
     */
    public Result checkOut(Integer userId) {
        Result validation = validateUserId(userId);
        if (validation != null) return validation;

        try {
            Attendance today = attendanceDAO.getTodayAttendance(userId);
            Result checkResult = validateCheckOutPreconditions(today);
            if (checkResult != null) return checkResult;

            String status = determineCheckOutStatus();
            Attendance attendance = buildCheckOutAttendance(userId, status);
            attendanceDAO.checkOut(attendance);
            return Result.ok(status);
        } catch (Exception e) {
            return Result.error(500, "签退失败: " + e.getMessage());
        }
    }

    /**
     * 考勤列表
     */
    public Result listAttendance(Map<String, Object> filter, int page) {
        if (!isValidPage(page)) {
            return Result.error(400, "页码必须大于0");
        }

        try {
            int offset = calculateOffset(page);
            Date startDate = filter != null ? parseFilterDate(filter.get("startDate")) : null;
            Date endDate = filter != null ? parseFilterDate(filter.get("endDate")) : null;
            Integer userId = filter != null ? parseFilterUserId(filter.get("userId")) : null;

            List<Attendance> list = attendanceDAO.getAttendanceList(userId, startDate, endDate, offset, DEFAULT_PAGE_SIZE);
            int total = attendanceDAO.getTotalCount(userId, startDate, endDate);

            return Result.ok(buildPaginatedResult(list, total, page, DEFAULT_PAGE_SIZE));
        } catch (Exception e) {
            return Result.error(500, "获取考勤列表失败: " + e.getMessage());
        }
    }

    /**
     * 考勤统计
     */
    public Result getAttendanceStats(Integer userId) {
        Result validation = validateUserId(userId);
        if (validation != null) return validation;

        try {
            Map<String, Object> stats = attendanceDAO.getStatistics(userId, null, null);
            return Result.ok(stats);
        } catch (Exception e) {
            return Result.error(500, "获取考勤统计失败: " + e.getMessage());
        }
    }

    /**
     * 审批补签
     */
    public Result approveMakeup(Integer id, Integer operatorId) {
        Result validation = validateOperator(operatorId);
        if (validation != null) return validation;

        try {
            return processMakeupApproval(id, operatorId, MAKEUP_STATUS_APPROVED, "审批");
        } catch (Exception e) {
            return Result.error(500, "审批失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝补签
     */
    public Result rejectMakeup(Integer id, Integer operatorId) {
        Result validation = validateOperator(operatorId);
        if (validation != null) return validation;

        try {
            return processMakeupApproval(id, operatorId, MAKEUP_STATUS_REJECTED, "拒绝");
        } catch (Exception e) {
            return Result.error(500, "拒绝失败: " + e.getMessage());
        }
    }

    /**
     * 我的考勤
     */
    public Result getMyAttendance(Integer userId, int page) {
        Result validation = validateUserForQuery(userId, page);
        if (validation != null) return validation;

        try {
            int offset = calculateOffset(page);
            List<Attendance> list = attendanceDAO.getAttendanceList(userId, null, null, offset, DEFAULT_PAGE_SIZE);
            int total = attendanceDAO.getTotalCount(userId, null, null);
            return Result.ok(buildPaginatedResult(list, total, page, DEFAULT_PAGE_SIZE));
        } catch (Exception e) {
            return Result.error(500, "获取考勤记录失败: " + e.getMessage());
        }
    }

    /**
     * 我的统计
     */
    public Result getMyStats(Integer userId) {
        Result validation = validateUserId(userId);
        if (validation != null) return validation;

        try {
            Map<String, Object> stats = attendanceDAO.getStatistics(userId, null, null);
            return Result.ok(stats);
        } catch (Exception e) {
            return Result.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 申请补签
     */
    public Result applyMakeup(Date date, String reason, Integer userId) {
        Result validation = validateMakeupApplication(date, reason, userId);
        if (validation != null) return validation;

        if (isFutureDate(date)) {
            return Result.error(400, "不能申请未来日期的补签");
        }

        try {
            String makeupType = inferMakeupType(reason);
            if (attendanceMakeupDAO.hasPendingApplication(userId, date, makeupType)) {
                return Result.error(400, "该日期已有待处理的补签申请");
            }

            AttendanceMakeup makeup = buildMakeupApplication(userId, date, makeupType, reason);
            attendanceMakeupDAO.apply(makeup);
            return Result.ok();
        } catch (Exception e) {
            return Result.error(500, "申请补签失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理的补签申请列表
     */
    public Result getPendingMakeupList(int page) {
        if (!isValidPage(page)) {
            return Result.error(400, "页码必须大于0");
        }

        try {
            int offset = calculateOffset(page);
            List<AttendanceMakeup> list = attendanceMakeupDAO.getPendingList(offset, DEFAULT_PAGE_SIZE);
            return Result.ok(list);
        } catch (Exception e) {
            return Result.error(500, "获取补签列表失败: " + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    private Result validateUserId(Integer userId) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return null;
    }

    private Result validateUserForQuery(Integer userId, int page) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (!isValidPage(page)) {
            return Result.error(400, "页码必须大于0");
        }
        return null;
    }

    private Result validateOperator(Integer operatorId) {
        if (operatorId == null) {
            return Result.error(400, "操作员ID不能为空");
        }
        return null;
    }

    private Result validateMakeupApplication(Date date, String reason, Integer userId) {
        if (date == null) {
            return Result.error(400, "补签日期不能为空");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(400, "补签原因不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return null;
    }

    private Result validateCheckOutPreconditions(Attendance today) {
        if (today == null || today.getCheckInTime() == null) {
            return Result.error(400, "请先签到后再签退");
        }
        if (today.getCheckOutTime() != null) {
            return Result.error(400, "您今日已签退");
        }
        return null;
    }

    private Attendance buildCheckInAttendance(Integer userId, String status) {
        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setAttendanceDate(new Date());
        attendance.setCheckInTime(new Date());
        attendance.setCheckInStatus(status);
        return attendance;
    }

    private Attendance buildCheckOutAttendance(Integer userId, String status) {
        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setAttendanceDate(new Date());
        attendance.setCheckOutTime(new Date());
        attendance.setCheckOutStatus(status);
        return attendance;
    }

    private AttendanceMakeup buildMakeupApplication(Integer userId, Date date, String makeupType, String reason) {
        AttendanceMakeup makeup = new AttendanceMakeup();
        makeup.setUserId(userId);
        makeup.setAttendanceDate(date);
        makeup.setMakeUpType(makeupType);
        makeup.setApplyReason(reason);
        makeup.setStatus(MAKEUP_STATUS_PENDING);
        return makeup;
    }

    private Map<String, Object> buildPaginatedResult(List<?> list, int total, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    private String determineCheckInStatus() {
        return isLate() ? CHECK_IN_STATUS_LATE : CHECK_IN_STATUS_NORMAL;
    }

    private String determineCheckOutStatus() {
        return isEarly() ? CHECK_OUT_STATUS_EARLY : CHECK_OUT_STATUS_NORMAL;
    }

    private Result processMakeupApproval(Integer id, Integer operatorId, String targetStatus, String action) {
        List<AttendanceMakeup> pendingList = attendanceMakeupDAO.getPendingList(0, DEFAULT_LIST_LIMIT);
        AttendanceMakeup target = findMakeupById(pendingList, id);

        if (target == null) {
            List<AttendanceMakeup> allList = attendanceMakeupDAO.getListByUser(null, 0, DEFAULT_LIST_LIMIT);
            target = findMakeupById(allList, id);
            if (target != null && !MAKEUP_STATUS_PENDING.equals(target.getStatus())) {
                return Result.error(400, "该补签申请已处理");
            }
            return Result.error(404, "补签申请不存在");
        }

        attendanceMakeupDAO.approve(id, operatorId, targetStatus, "");
        return Result.ok();
    }

    private boolean isLate() {
        Calendar now = Calendar.getInstance();
        Calendar threshold = Calendar.getInstance();
        threshold.set(Calendar.HOUR_OF_DAY, LATE_THRESHOLD_HOUR);
        threshold.set(Calendar.MINUTE, LATE_THRESHOLD_MINUTE);
        return now.after(threshold);
    }

    private boolean isEarly() {
        Calendar now = Calendar.getInstance();
        Calendar threshold = Calendar.getInstance();
        threshold.set(Calendar.HOUR_OF_DAY, EARLY_THRESHOLD_HOUR);
        threshold.set(Calendar.MINUTE, EARLY_THRESHOLD_MINUTE);
        return now.before(threshold);
    }

    private boolean isFutureDate(Date date) {
        if (date == null) return true;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar checkDate = Calendar.getInstance();
        checkDate.setTime(date);
        checkDate.set(Calendar.HOUR_OF_DAY, 0);
        checkDate.set(Calendar.MINUTE, 0);
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);

        return checkDate.after(today);
    }

    private String inferMakeupType(String reason) {
        if (reason != null && reason.contains("签退")) {
            return MAKEUP_TYPE_CHECK_OUT;
        }
        return MAKEUP_TYPE_CHECK_IN;
    }

    private AttendanceMakeup findMakeupById(List<AttendanceMakeup> list, Integer id) {
        if (list == null || id == null) return null;
        for (AttendanceMakeup m : list) {
            if (id.equals(m.getId())) {
                return m;
            }
        }
        return null;
    }

    private boolean isValidPage(int page) {
        return page > 0;
    }

    private int calculateOffset(int page) {
        return (page - 1) * DEFAULT_PAGE_SIZE;
    }

    private Date parseFilterDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        return null;
    }

    private Integer parseFilterUserId(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return null;
    }
}
