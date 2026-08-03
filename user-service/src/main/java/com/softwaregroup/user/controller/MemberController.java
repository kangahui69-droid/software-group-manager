package com.softwaregroup.user.controller;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 成员管理控制器
 *
 * 处理成员的增删改查、状态管理等操作
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 获取成员列表
     * GET /api/members
     */
    @GetMapping
    public Result listMembers(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "status", required = false) String status,
                              @RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        return memberService.listMembers(keyword, role, status, page, pageSize);
    }

    /**
     * 获取成员详情
     * GET /api/members/{id}
     */
    @GetMapping("/{id}")
    public Result getMemberDetail(@PathVariable("id") Integer id) {
        return memberService.getMemberDetail(id);
    }

    /**
     * 创建成员
     * POST /api/members
     */
    @PostMapping
    public Result createMember(@RequestBody Map<String, Object> memberData,
                                @RequestHeader("X-User-Id") Integer operatorId) {
        return memberService.createMember(memberData, operatorId);
    }

    /**
     * 更新成员
     * PUT /api/members/{id}
     */
    @PutMapping("/{id}")
    public Result updateMember(@PathVariable("id") Integer id,
                                @RequestBody Map<String, Object> memberData,
                                @RequestHeader("X-User-Id") Integer operatorId) {
        return memberService.updateMember(id, memberData, operatorId);
    }

    /**
     * 删除成员
     * DELETE /api/members/{id}
     */
    @DeleteMapping("/{id}")
    public Result deleteMember(@PathVariable("id") Integer id,
                                @RequestHeader("X-User-Id") Integer operatorId) {
        return memberService.deleteMember(id, operatorId);
    }

    /**
     * 启用成员
     * PUT /api/members/{id}/enable
     */
    @PutMapping("/{id}/enable")
    public Result enableMember(@PathVariable("id") Integer id,
                                @RequestHeader("X-User-Id") Integer operatorId) {
        return memberService.enableMember(id, operatorId);
    }

    /**
     * 禁用成员
     * PUT /api/members/{id}/disable
     */
    @PutMapping("/{id}/disable")
    public Result disableMember(@PathVariable("id") Integer id,
                                 @RequestHeader("X-User-Id") Integer operatorId) {
        return memberService.disableMember(id, operatorId);
    }

    /**
     * 重置成员密码
     * PUT /api/members/{id}/reset-password
     */
    @PutMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable("id") Integer id,
                                 @RequestHeader("X-User-Id") Integer operatorId) {
        return memberService.resetPassword(id, operatorId);
    }
}
