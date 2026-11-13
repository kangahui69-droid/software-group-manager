package com.softwaregroup.content.controller;

import com.softwaregroup.content.model.dto.GroupDTO;
import com.softwaregroup.content.service.GroupService;
import com.softwaregroup.common.security.CurrentUser;
import com.softwaregroup.common.security.AuthenticatedUser;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 群组管理 REST API
 */
@RestController
@RequestMapping(value = {"/api/groups", "/api/groups/"})
public class GroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping
    public Result listGroups(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int pageSize) {
        return groupService.listGroups(page, pageSize);
    }

    @GetMapping("/{groupId}")
    public Result getGroupDetail(@PathVariable Integer groupId,
                                @AuthenticatedUser Integer userId) {
        return groupService.getGroupDetail(groupId, userId);
    }

    @GetMapping("/user/{userId}/created")
    public Result getCreatedGroups(@PathVariable Integer userId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @AuthenticatedUser Integer currentUserId) {
        CurrentUser.requireSameId(userId, currentUserId);
        return groupService.getCreatedGroups(userId, page);
    }

    @GetMapping("/user/{userId}/my-groups")
    public Result getMyGroups(@PathVariable Integer userId,
                            @RequestParam(defaultValue = "1") int page,
                            @AuthenticatedUser Integer currentUserId) {
        CurrentUser.requireSameId(userId, currentUserId);
        return groupService.getMyGroups(userId, page);
    }

    @PostMapping
    public Result createGroup(@RequestBody GroupDTO dto,
                            @AuthenticatedUser Integer userId) {
        return groupService.createGroup(dto, userId);
    }

    @PutMapping("/{groupId}")
    public Result updateGroup(@PathVariable Integer groupId,
                             @RequestBody GroupDTO dto,
                             @AuthenticatedUser Integer userId) {
        return groupService.updateGroup(groupId, dto, userId);
    }

    @DeleteMapping("/{groupId}")
    public Result deleteGroup(@PathVariable Integer groupId,
                            @AuthenticatedUser Integer userId) {
        return groupService.deleteGroup(groupId, userId);
    }

    @PostMapping("/{groupId}/members")
    public Result addMember(@PathVariable Integer groupId,
                           @RequestBody Map<String, Integer> body,
                           @AuthenticatedUser Integer operatorId) {
        Integer userId = body.get("userId");
        return groupService.addMember(groupId, userId, operatorId);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public Result removeMember(@PathVariable Integer groupId,
                              @PathVariable Integer userId,
                              @AuthenticatedUser Integer operatorId) {
        return groupService.removeMember(groupId, userId, operatorId);
    }

    @GetMapping("/{groupId}/messages")
    public Result getMessages(@PathVariable Integer groupId,
                             @RequestParam(defaultValue = "1") int page,
                             @AuthenticatedUser Integer userId) {
        return groupService.getMessages(groupId, page, userId);
    }

    @PostMapping("/{groupId}/messages")
    public Result sendMessage(@PathVariable Integer groupId,
                             @RequestBody Map<String, Object> body,
                             @AuthenticatedUser Integer userId) {
        String content = (String) body.get("content");
        return groupService.sendMessage(groupId, userId, content);
    }

    @DeleteMapping("/messages/{messageId}")
    public Result deleteMessage(@PathVariable Integer messageId,
                               @AuthenticatedUser Integer userId) {
        return groupService.deleteMessage(messageId, userId);
    }

    @PostMapping("/{groupId}/mute")
    public Result muteMember(@PathVariable Integer groupId,
                            @RequestBody Map<String, Object> body,
                            @AuthenticatedUser Integer operatorId) {
        Integer targetUserId = (Integer) body.get("targetUserId");
        return groupService.muteMember(groupId, targetUserId, null, (String) body.get("reason"), operatorId);
    }

    @DeleteMapping("/{groupId}/mute")
    public Result unmuteMember(@PathVariable Integer groupId,
                              @AuthenticatedUser Integer operatorId) {
        return groupService.unmuteMember(groupId, operatorId);
    }
}
