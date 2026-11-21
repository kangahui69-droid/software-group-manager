package com.softwaregroup.activity.controller;

import com.softwaregroup.activity.service.AttendanceService;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.RequireRole;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

/**
 * 考勤管理 Controller
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 签到
     */
    @PostMapping("/check-in")
    public Result checkIn(@AuthenticatedUser Integer userId) {
        return attendanceService.checkIn(userId);
    }

    /**
     * 签退
     */
    @PostMapping("/check-out")
    public Result checkOut(@AuthenticatedUser Integer userId) {
        return attendanceService.checkOut(userId);
    }

    /**
     * 考勤列表
     */
    @GetMapping
    @RequireRole("ADMIN")
    public Result listAttendance(@RequestParam(required = false) Map<String, Object> filter,
                                  @RequestParam(defaultValue = "1") int page) {
        return attendanceService.listAttendance(filter, page);
    }

    /**
     * 考勤统计
     */
    @GetMapping("/stats")
    public Result getAttendanceStats(@AuthenticatedUser Integer userId) {
        return attendanceService.getAttendanceStats(userId);
    }

    /**
     * 我的考勤
     */
    @GetMapping("/my")
    public Result getMyAttendance(@AuthenticatedUser Integer userId,
                                  @RequestParam(defaultValue = "1") int page) {
        return attendanceService.getMyAttendance(userId, page);
    }

    /**
     * 我的统计
     */
    @GetMapping("/my/stats")
    public Result getMyStats(@AuthenticatedUser Integer userId) {
        return attendanceService.getMyStats(userId);
    }

    /**
     * 申请补签
     */
    @PostMapping("/makeup")
    public Result applyMakeup(@RequestBody Map<String, Object> body,
                               @AuthenticatedUser Integer userId) {
        Date date = body.get("date") != null ? new Date(((Number) body.get("date")).longValue()) : null;
        String reason = (String) body.get("reason");
        return attendanceService.applyMakeup(date, reason, userId);
    }

    /**
     * 审批补签
     */
    @PostMapping("/makeup/{id}/approve")
    @RequireRole("ADMIN")
    public Result approveMakeup(@PathVariable Integer id,
                                 @AuthenticatedUser Integer operatorId) {
        return attendanceService.approveMakeup(id, operatorId);
    }

    /**
     * 拒绝补签
     */
    @PostMapping("/makeup/{id}/reject")
    @RequireRole("ADMIN")
    public Result rejectMakeup(@PathVariable Integer id,
                                @AuthenticatedUser Integer operatorId) {
        return attendanceService.rejectMakeup(id, operatorId);
    }

    /**
     * 获取待处理的补签申请列表
     */
    @GetMapping("/makeup/pending")
    @RequireRole("ADMIN")
    public Result getPendingMakeupList(@RequestParam(defaultValue = "1") int page) {
        return attendanceService.getPendingMakeupList(page);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "attendance-service"));
    }
}
