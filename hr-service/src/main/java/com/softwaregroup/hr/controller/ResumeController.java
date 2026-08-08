package com.softwaregroup.hr.controller;

import com.softwaregroup.hr.model.dto.ResumeDTO;
import com.softwaregroup.hr.model.dto.ResumeEducationDTO;
import com.softwaregroup.hr.model.dto.ResumeSkillDTO;
import com.softwaregroup.hr.model.dto.ResumeProjectDTO;
import com.softwaregroup.hr.model.dto.ResumeAwardDTO;
import com.softwaregroup.hr.service.ResumeService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 简历管理 Controller
 */
@RestController
@RequestMapping(value = {"/api/resumes", "/api/resumes/"})
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping
    public Result createResume(@RequestBody ResumeDTO dto,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.createResume(dto, userId);
    }

    @PutMapping("/{id}")
    public Result updateResume(@PathVariable Integer id,
                               @RequestBody ResumeDTO dto,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.updateResume(id, dto, userId);
    }

    @GetMapping("/{id}")
    public Result getResumeDetail(@PathVariable Integer id,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.getResumeDetail(id, userId);
    }

    @DeleteMapping("/{id}")
    public Result deleteResume(@PathVariable Integer id,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.deleteResume(id, userId);
    }

    @PutMapping("/{id}/default")
    public Result setDefaultResume(@PathVariable Integer id,
                                    @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.setDefaultResume(id, userId);
    }

    @GetMapping("/user/{userId}")
    public Result listResumes(@PathVariable Integer userId) {
        return resumeService.listResumes(userId, 1);
    }

    @GetMapping("/recycle-bin")
    public Result getRecycleBin(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.getRecycleBin(userId);
    }

    @PostMapping("/{id}/restore")
    public Result restoreResume(@PathVariable Integer id,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.restoreResume(id, userId);
    }

    @DeleteMapping("/{id}/permanent")
    public Result permanentDelete(@PathVariable Integer id,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.permanentDelete(id, userId);
    }

    @PostMapping("/{resumeId}/education")
    public Result addEducation(@PathVariable Integer resumeId,
                                @RequestBody ResumeEducationDTO dto,
                                @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.addEducation(resumeId, dto, userId);
    }

    @PutMapping("/education/{educationId}")
    public Result updateEducation(@PathVariable Integer educationId,
                                   @RequestBody ResumeEducationDTO dto,
                                   @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.updateEducation(educationId, dto, userId);
    }

    @DeleteMapping("/education/{educationId}")
    public Result deleteEducation(@PathVariable Integer educationId,
                                  @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.deleteEducation(educationId, userId);
    }

    @PostMapping("/{resumeId}/skill")
    public Result addSkill(@PathVariable Integer resumeId,
                            @RequestBody ResumeSkillDTO dto,
                            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.addSkill(resumeId, dto, userId);
    }

    @PutMapping("/skill/{skillId}")
    public Result updateSkill(@PathVariable Integer skillId,
                               @RequestBody ResumeSkillDTO dto,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.updateSkill(skillId, dto, userId);
    }

    @DeleteMapping("/skill/{skillId}")
    public Result deleteSkill(@PathVariable Integer skillId,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.deleteSkill(skillId, userId);
    }

    @PostMapping("/{resumeId}/project")
    public Result addProject(@PathVariable Integer resumeId,
                              @RequestBody ResumeProjectDTO dto,
                              @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.addProject(resumeId, dto, userId);
    }

    @PutMapping("/project/{projectId}")
    public Result updateProject(@PathVariable Integer projectId,
                                @RequestBody ResumeProjectDTO dto,
                                @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.updateProject(projectId, dto, userId);
    }

    @DeleteMapping("/project/{projectId}")
    public Result deleteProject(@PathVariable Integer projectId,
                                 @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.deleteProject(projectId, userId);
    }

    @PostMapping("/{resumeId}/award")
    public Result addAward(@PathVariable Integer resumeId,
                            @RequestBody ResumeAwardDTO dto,
                            @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.addAward(resumeId, dto, userId);
    }

    @PutMapping("/award/{awardId}")
    public Result updateAward(@PathVariable Integer awardId,
                               @RequestBody ResumeAwardDTO dto,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.updateAward(awardId, dto, userId);
    }

    @DeleteMapping("/award/{awardId}")
    public Result deleteAward(@PathVariable Integer awardId,
                               @RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        return resumeService.deleteAward(awardId, userId);
    }

    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "hr-service"));
    }

    /**
     * 获取简历列表（根路径）
     * GET /api/resumes
     */
    @GetMapping
    public Result listResumesRoot(@RequestHeader(value = "X-User-Id", required = false) Integer userId) {
        if (userId != null) {
            return resumeService.listResumes(userId, 1);
        }
        return resumeService.listResumes(0, 1);
    }
}
