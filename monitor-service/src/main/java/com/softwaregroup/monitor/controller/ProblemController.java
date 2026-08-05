package com.softwaregroup.monitor.controller;

import com.softwaregroup.monitor.model.dto.ProblemDTO;
import com.softwaregroup.monitor.model.dto.ProblemFilterDTO;
import com.softwaregroup.monitor.service.ProblemService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 问题反馈 Controller
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @PostMapping
    public Result submitProblem(@RequestBody ProblemDTO dto,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return problemService.submitProblem(dto, userId);
    }

    @GetMapping("/{id}")
    public Result getProblemDetail(@PathVariable Integer id) {
        return problemService.getProblemDetail(id);
    }

    @GetMapping
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
    public Result getMyProblems(@RequestHeader(value = "X-User-Id", required = false) Integer userId,
                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                 @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return problemService.getMyProblems(userId, page, pageSize);
    }

    @PutMapping("/{id}")
    public Result updateProblem(@PathVariable Integer id,
                                 @RequestBody ProblemDTO dto,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return problemService.updateProblem(id, dto, operatorId);
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Integer id,
                                @RequestBody Map<String, String> body,
                                @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        String status = body.get("status");
        String comment = body.get("comment");
        return problemService.updateStatus(id, status, comment, operatorId);
    }

    @PutMapping("/{id}/category")
    public Result updateCategory(@PathVariable Integer id,
                                  @RequestBody Map<String, String> body,
                                  @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        String category = body.get("category");
        return problemService.updateCategory(id, category, operatorId);
    }

    @PostMapping("/{id}/comment")
    public Result addComment(@PathVariable Integer id,
                               @RequestBody Map<String, String> body,
                               @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        String comment = body.get("comment");
        return problemService.addComment(id, comment, operatorId);
    }

    @DeleteMapping("/{id}")
    public Result deleteProblem(@PathVariable Integer id,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer operatorId) {
        return problemService.deleteProblem(id, operatorId);
    }

    @GetMapping("/statistics")
    public Result getStatistics() {
        return problemService.getStatistics();
    }

    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "monitor-service"));
    }
}
