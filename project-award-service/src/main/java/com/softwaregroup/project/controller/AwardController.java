package com.softwaregroup.project.controller;

import com.softwaregroup.project.model.dto.AwardDTO;
import com.softwaregroup.project.service.AwardService;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.security.ApiAuthenticationFilter;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.security.RequireRole;
import com.softwaregroup.common.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 奖项管理 REST API
 */
@RestController
@RequestMapping(value = {"/api/awards", "/api/awards/"})
public class AwardController {

    @Autowired
    private AwardService awardService;

    @GetMapping
    public Result listAwards(@RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "1") Integer page,
                            HttpServletRequest request) {
        return awardService.listAwards(status, page,
                (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE));
    }

    @GetMapping("/{awardId}")
    public Result getAwardDetail(@PathVariable Integer awardId, HttpServletRequest request) {
        return awardService.getAwardDetail(awardId,
                (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE));
    }

    @GetMapping("/{awardId}/images")
    public Result getAwardImages(@PathVariable Integer awardId, HttpServletRequest request) {
        return awardService.getAwardImages(awardId,
                (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE));
    }

    @GetMapping("/my")
    public Result getMyAwards(@AuthenticatedUser Integer userId) {
        return awardService.getMyAwards(userId);
    }

    @GetMapping("/statistics")
    public Result getAwardStatistics(@AuthenticatedUser Integer userId) {
        return awardService.getAwardStatistics(userId);
    }

    @PostMapping
    public Result submitAward(@RequestBody AwardDTO dto,
                              @AuthenticatedUser Integer userId) {
        return awardService.submitAward(dto, userId, null);
    }

    @PutMapping("/{awardId}")
    public Result updateAward(@PathVariable Integer awardId,
                              @RequestBody AwardDTO dto,
                              @AuthenticatedUser Integer userId) {
        return awardService.updateAward(awardId, dto, userId);
    }

    @DeleteMapping("/{awardId}")
    public Result deleteAward(@PathVariable Integer awardId,
                              @AuthenticatedUser Integer userId) {
        return awardService.deleteAward(awardId, userId);
    }

    @PutMapping("/{awardId}/approve")
    @RequireRole("ADMIN")
    public Result approveAward(@PathVariable Integer awardId,
                               @AuthenticatedUser Integer operatorId) {
        return awardService.approveAward(awardId, operatorId);
    }

    @PutMapping("/{awardId}/reject")
    @RequireRole("ADMIN")
    public Result rejectAward(@PathVariable Integer awardId,
                             @RequestBody Map<String, Object> body,
                             @AuthenticatedUser Integer operatorId) {
        String reason = (String) body.get("reason");
        return awardService.rejectAward(awardId, reason, operatorId);
    }
}
