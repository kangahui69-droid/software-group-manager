package com.softwaregroup.activity.controller;

import com.softwaregroup.activity.service.StudyService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学习时段管理 Controller
 */
@RestController
@RequestMapping("/api/study")
public class StudyController {

    @Autowired
    private StudyService studyService;

    /**
     * 开始学习
     */
    @PostMapping("/start")
    public Result startSession(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return studyService.startSession(userId);
    }

    /**
     * 结束学习
     */
    @PostMapping("/end")
    public Result endSession(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return studyService.endSession(userId);
    }

    /**
     * 自动结束所有超时会话
     */
    @PostMapping("/auto-end")
    public Result autoEndSession() {
        return studyService.autoEndSession();
    }

    /**
     * 获取学习记录详情
     */
    @GetMapping("/{id}")
    public Result getSessionDetail(@PathVariable Integer id) {
        return studyService.getSessionDetail(id);
    }

    /**
     * 获取学习记录列表
     */
    @GetMapping
    public Result listSessions(@RequestParam(required = false) Map<String, Object> filter,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int pageSize) {
        return studyService.listSessions(filter, page, pageSize);
    }

    /**
     * 获取我的学习记录
     */
    @GetMapping("/my")
    public Result getMySessions(@RequestHeader(value = "X-User-Id", required = false) Integer userId,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int pageSize) {
        return studyService.getMySessions(userId, page, pageSize);
    }

    /**
     * 获取今日进行中会话
     */
    @GetMapping("/today")
    public Result getTodaySession(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return studyService.getTodaySession(userId);
    }

    /**
     * 获取学习统计
     */
    @GetMapping("/stats")
    public Result getStatistics(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return studyService.getStatistics(userId);
    }

    /**
     * 获取本周学习统计
     */
    @GetMapping("/week-stats")
    public Result getWeekStatistics(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return studyService.getWeekStatistics(userId);
    }

    /**
     * 获取连续学习天数
     */
    @GetMapping("/consecutive-days")
    public Result getConsecutiveDays(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return studyService.getConsecutiveDays(userId);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "study-service"));
    }
}
