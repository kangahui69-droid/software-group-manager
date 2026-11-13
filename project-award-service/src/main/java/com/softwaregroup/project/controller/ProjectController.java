package com.softwaregroup.project.controller;

import com.softwaregroup.project.model.dto.ProjectDTO;
import com.softwaregroup.project.model.dto.ProjectFilterDTO;
import com.softwaregroup.project.model.dto.PlanDTO;
import com.softwaregroup.project.model.dto.ProgressDTO;
import com.softwaregroup.project.service.ProjectService;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.ApiAuthenticationFilter;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.security.RequireRole;
import com.softwaregroup.common.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 项目管理 REST API
 */
@RestController
@RequestMapping(value = {"/api/projects", "/api/projects/"})
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping
    public Result listProjects(ProjectFilterDTO filter,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int pageSize,
                              HttpServletRequest request) {
        return projectService.listProjects(filter, page, pageSize,
                (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE));
    }

    @GetMapping("/{projectId}")
    public Result getProjectDetail(@PathVariable Integer projectId, HttpServletRequest request) {
        CurrentUser user = (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
        return projectService.getProjectDetail(projectId, user == null ? null : user.id(),
                user != null && "ADMIN".equals(user.role()));
    }

    @GetMapping("/my")
    public Result getMyProjects(@AuthenticatedUser Integer userId,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int pageSize) {
        return projectService.getMyProjects(userId, page, pageSize);
    }

    @PostMapping
    public Result createProject(@RequestBody ProjectDTO dto,
                                @AuthenticatedUser Integer userId) {
        return projectService.createProject(dto, userId);
    }

    @PutMapping("/{projectId}")
    public Result updateProject(@PathVariable Integer projectId,
                                @RequestBody ProjectDTO dto,
                                @AuthenticatedUser Integer userId) {
        return projectService.updateProject(projectId, dto, userId);
    }

    @DeleteMapping("/{projectId}")
    public Result deleteProject(@PathVariable Integer projectId,
                                @AuthenticatedUser Integer userId) {
        return projectService.deleteProject(projectId, userId);
    }

    @PutMapping("/{projectId}/approve")
    @RequireRole("ADMIN")
    public Result approveProject(@PathVariable Integer projectId,
                                @AuthenticatedUser Integer operatorId) {
        return projectService.approveProject(projectId, operatorId);
    }

    @PutMapping("/{projectId}/reject")
    @RequireRole("ADMIN")
    public Result rejectProject(@PathVariable Integer projectId,
                                @RequestBody Map<String, Object> body,
                                @AuthenticatedUser Integer operatorId) {
        String reason = (String) body.get("reason");
        return projectService.rejectProject(projectId, reason, operatorId);
    }

    @PostMapping("/{projectId}/apply")
    public Result applyMember(@PathVariable Integer projectId,
                              @RequestBody Map<String, Object> body,
                              @AuthenticatedUser Integer userId) {
        String reason = (String) body.get("reason");
        return projectService.applyMember(projectId, userId, reason);
    }

    @PutMapping("/applications/{applicationId}/approve")
    public Result approveMember(@PathVariable Integer applicationId,
                                @AuthenticatedUser Integer operatorId) {
        return projectService.approveMember(applicationId, operatorId);
    }

    @PutMapping("/applications/{applicationId}/reject")
    public Result rejectMember(@PathVariable Integer applicationId,
                               @RequestBody Map<String, Object> body,
                               @AuthenticatedUser Integer operatorId) {
        String reason = (String) body.get("reason");
        return projectService.rejectMember(applicationId, reason, operatorId);
    }

    @PostMapping("/{projectId}/plans")
    public Result addPlan(@PathVariable Integer projectId,
                          @RequestBody PlanDTO dto,
                          @AuthenticatedUser Integer userId) {
        return projectService.addPlan(projectId, dto, userId);
    }

    @PostMapping("/{projectId}/progress")
    public Result addProgress(@PathVariable Integer projectId,
                              @RequestBody ProgressDTO dto,
                              @AuthenticatedUser Integer userId) {
        return projectService.addProgress(projectId, dto, userId);
    }
}
