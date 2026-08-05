package com.softwaregroup.activity.controller;

import com.softwaregroup.activity.service.AttendanceService;
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
    public Result checkIn(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return attendanceService.checkIn(userId);
    }

    /**
     * 签退
     */
    @PostMapping("/check-out")
    public Result checkOut(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return attendanceService.checkOut(userId);
    }

    /**
     * 考勤列表
     */
    @GetMapping
    public Result listAttendance(@RequestParam(required = false) Map<String, Object> filter,
                                  @RequestParam(defaultValue = "1") int page) {
        return attendanceService.listAttendance(filter, page);
    }

    /**
     * 考勤统计
     */
    @GetMapping("/stats")
    public Result getAttendanceStats(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return attendanceService.getAttendanceStats(userId);
    }

    /**
     * 我的考勤
     */
    @GetMapping("/my")
    public Result getMyAttendance(@RequestHeader(value = "X-User-Id", required = false) Integer userId,
                                  @RequestParam(defaultValue = "1") int page) {
        return attendanceService.getMyAttendance(userId, page);
    }

    /**
     * 我的统计
     */
    @GetMapping("/my/stats")
    public Result getMyStats(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return attendanceService.getMyStats(userId);
    }

    /**
     * 申请补签
     */
    @PostMapping("/makeup")
    public Result applyMakeup(@RequestBody Map<String, Object> body,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        Date date = body.get("date") != null ? new Date(((Number) body.get("date")).longValue()) : null;
        String reason = (String) body.get("reason");
        return attendanceService.applyMakeup(date, reason, userId);
    }

    /**
     * 审批补签
     */
    @PostMapping("/makeup/{id}/approve")
    public Result approveMakeup(@PathVariable Integer id,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return attendanceService.approveMakeup(id, operatorId);
    }

    /**
     * 拒绝补签
     */
    @PostMapping("/makeup/{id}/reject")
    public Result rejectMakeup(@PathVariable Integer id,
                                @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return attendanceService.rejectMakeup(id, operatorId);
    }

    /**
     * 获取待处理的补签申请列表
     */
    @GetMapping("/makeup/pending")
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
