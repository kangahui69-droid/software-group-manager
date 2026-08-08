package com.softwaregroup.project.controller;

import com.softwaregroup.project.model.dto.AwardDTO;
import com.softwaregroup.project.service.AwardService;
import com.softwaregroup.common.util.Result;
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
                            @RequestParam(defaultValue = "1") Integer page) {
        return awardService.listAwards(status, page);
    }

    @GetMapping("/{awardId}")
    public Result getAwardDetail(@PathVariable Integer awardId) {
        return awardService.getAwardDetail(awardId);
    }

    @GetMapping("/{awardId}/images")
    public Result getAwardImages(@PathVariable Integer awardId) {
        return awardService.getAwardImages(awardId);
    }

    @GetMapping("/my")
    public Result getMyAwards(@RequestParam Integer userId) {
        return awardService.getMyAwards(userId);
    }

    @GetMapping("/statistics")
    public Result getAwardStatistics(@RequestParam Integer userId) {
        return awardService.getAwardStatistics(userId);
    }

    @PostMapping
    public Result submitAward(@RequestBody AwardDTO dto,
                              @RequestParam Integer userId) {
        return awardService.submitAward(dto, userId, null);
    }

    @PutMapping("/{awardId}")
    public Result updateAward(@PathVariable Integer awardId,
                              @RequestBody AwardDTO dto,
                              @RequestParam Integer userId) {
        return awardService.updateAward(awardId, dto, userId);
    }

    @DeleteMapping("/{awardId}")
    public Result deleteAward(@PathVariable Integer awardId,
                              @RequestParam Integer userId) {
        return awardService.deleteAward(awardId, userId);
    }

    @PutMapping("/{awardId}/approve")
    public Result approveAward(@PathVariable Integer awardId,
                               @RequestBody Map<String, Integer> body) {
        Integer operatorId = body.get("operatorId");
        return awardService.approveAward(awardId, operatorId);
    }

    @PutMapping("/{awardId}/reject")
    public Result rejectAward(@PathVariable Integer awardId,
                             @RequestBody Map<String, Object> body) {
        String reason = (String) body.get("reason");
        Integer operatorId = (Integer) body.get("operatorId");
        return awardService.rejectAward(awardId, reason, operatorId);
    }
}
