package com.softwaregroup.hr.controller;

import com.softwaregroup.hr.model.dto.RecruitApplicationDTO;
import com.softwaregroup.hr.service.RecruitService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 招新管理 Controller
 */
@RestController
@RequestMapping(value = {"/api/recruit", "/api/recruit/"})
public class RecruitController {

    @Autowired
    private RecruitService recruitService;

    @PostMapping("/apply")
    public Result submitApplication(@RequestBody RecruitApplicationDTO dto) {
        return recruitService.submitApplication(dto);
    }

    @PostMapping("/{id}/approve")
    public Result approveApplication(@PathVariable Integer id,
                                     @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return recruitService.approveApplication(id, operatorId);
    }

    @PostMapping("/{id}/reject")
    public Result rejectApplication(@PathVariable Integer id,
                                     @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return recruitService.rejectApplication(id, operatorId);
    }

    @GetMapping("/list")
    public Result listApplications(@RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer round) {
        return recruitService.listApplications(year, status, keyword, round);
    }

    @GetMapping("/{id}")
    public Result getApplicationDetail(@PathVariable Integer id) {
        return recruitService.getApplicationDetail(id);
    }

    @DeleteMapping("/{id}")
    public Result deleteApplication(@PathVariable Integer id) {
        return recruitService.deleteApplication(id);
    }

    @GetMapping("/pending/count")
    public Result countPending() {
        return recruitService.countPending();
    }

    @GetMapping("/years")
    public Result findAllYears() {
        return recruitService.findAllYears();
    }

    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "hr-service"));
    }

    /**
     * 获取申请列表（根路径）
     * GET /api/recruit
     */
    @GetMapping
    public Result listApplicationsRoot(@RequestParam(required = false) Integer year,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer round) {
        return recruitService.listApplications(year, status, keyword, round);
    }
}
