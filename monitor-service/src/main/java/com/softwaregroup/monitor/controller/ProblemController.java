package com.softwaregroup.monitor.controller;

import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.model.dto.ProblemFilterDTO;
import com.softwaregroup.monitor.service.ProblemService;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.RequireRole;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 问题反馈 Controller
 */
@RestController
@RequestMapping(value = {"/api/problems", "/api/problems/"})
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @PostMapping
    public Result submitProblem(@RequestBody ProblemDTO dto,
                                 @AuthenticatedUser Integer userId) {
        return problemService.submitProblem(dto, userId);
    }

    @GetMapping("/{id}")
    public Result getProblemDetail(@PathVariable Integer id,
                                   @AuthenticatedUser Integer userId) {
        return problemService.getProblemDetail(id, userId);
    }

    @GetMapping
    @RequireRole("ADMIN")
    public Result listProblems(@RequestParam(required = false) String category,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false, defaultValue = "1") Integer page,
                                @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        ProblemFilterDTO filter = new ProblemFilterDTO();
        filter.setCategory(category);
        filter.setStatus(status);
        return problemService.listProblems(filter, page, pageSize);
    }

    @GetMapping("/my")
    public Result getMyProblems(@AuthenticatedUser Integer userId,
                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                 @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return problemService.getMyProblems(userId, page, pageSize);
    }

    @PutMapping("/{id}")
    public Result updateProblem(@PathVariable Integer id,
                                 @RequestBody ProblemDTO dto,
                                 @AuthenticatedUser Integer operatorId) {
        return problemService.updateProblem(id, dto, operatorId);
    }

    @PutMapping("/{id}/status")
    @RequireRole("ADMIN")
    public Result updateStatus(@PathVariable Integer id,
                                @RequestBody Map<String, String> body,
                                @AuthenticatedUser Integer operatorId) {
        String status = body.get("status");
        String comment = body.get("comment");
        return problemService.updateStatus(id, status, comment, operatorId);
    }

    @PutMapping("/{id}/category")
    @RequireRole("ADMIN")
    public Result updateCategory(@PathVariable Integer id,
                                  @RequestBody Map<String, String> body,
                                  @AuthenticatedUser Integer operatorId) {
        String category = body.get("category");
        return problemService.updateCategory(id, category, operatorId);
    }

    @PostMapping("/{id}/comment")
    @RequireRole("ADMIN")
    public Result addComment(@PathVariable Integer id,
                               @RequestBody Map<String, String> body,
                               @AuthenticatedUser Integer operatorId) {
        String comment = body.get("comment");
        return problemService.addComment(id, comment, operatorId);
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public Result deleteProblem(@PathVariable Integer id,
                                 @AuthenticatedUser Integer operatorId) {
        return problemService.deleteProblem(id, operatorId);
    }

    @GetMapping("/statistics")
    @RequireRole("ADMIN")
    public Result getStatistics() {
        return problemService.getStatistics();
    }

    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "monitor-service"));
    }
}
