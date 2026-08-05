package com.softwaregroup.content.service;

import com.softwaregroup.content.dao.ActivityGroupDAO;
import com.softwaregroup.content.dao.GroupMemberDAO;
import com.softwaregroup.content.dao.GroupMessageDAO;
import com.softwaregroup.content.dao.UserGroupDAO;
import com.softwaregroup.content.dao.FileStorageDAO;
import com.softwaregroup.content.dao.UserDAO;
import com.softwaregroup.content.dao.MemberProfileDAO;
import com.softwaregroup.content.model.ActivityGroup;
import com.softwaregroup.content.model.GroupMember;
import com.softwaregroup.content.model.GroupMessage;
import com.softwaregroup.content.model.User;
import com.softwaregroup.content.model.UserGroup;
import com.softwaregroup.content.model.dto.GroupDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 群聊服务层
 */
@Service
public class GroupService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_MESSAGE_PAGE_SIZE = 20;
    private static final int MAX_GROUP_NAME_LENGTH = 255;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_REASON_LENGTH = 500;
    private static final int DEFAULT_OWNER_USER_ID = 1;

    @Autowired
    private ActivityGroupDAO activityGroupDAO;

    @Autowired
    private GroupMemberDAO groupMemberDAO;

    @Autowired
    private GroupMessageDAO groupMessageDAO;

    @Autowired
    private UserGroupDAO userGroupDAO;

    @Autowired
    private FileStorageDAO fileStorageDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private MemberProfileDAO memberProfileDAO;

    public GroupService() {
    }

    public GroupService(ActivityGroupDAO activityGroupDAO, GroupMemberDAO groupMemberDAO,
                       GroupMessageDAO groupMessageDAO, UserGroupDAO userGroupDAO,
                       FileStorageDAO fileStorageDAO, UserDAO userDAO, MemberProfileDAO memberProfileDAO) {
        this.activityGroupDAO = activityGroupDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.groupMessageDAO = groupMessageDAO;
        this.userGroupDAO = userGroupDAO;
        this.fileStorageDAO = fileStorageDAO;
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
    }

    public Result listGroups(int page, int pageSize) {
        Result validation = validatePageParams(page, pageSize);
        if (validation != null) {
            return validation;
        }

        List<ActivityGroup> groups = activityGroupDAO.findAll();
        return Result.ok(groups);
    }

    public Result getGroupDetail(Integer groupId, Integer userId) {
        Result validation = validateGroupIdAndUserId(groupId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup group = findGroupOrFail(groupId);
            if (!isGroupMemberOrOwner(groupId, userId)) {
                return Result.error(403, "无权限查看群组详情");
            }

            return Result.ok(group);
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        }
    }

    public Result createGroup(GroupDTO dto, Integer userId) {
        Result validation = validateCreateGroupParams(dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            User user = findUserOrFail(userId);
            ActivityGroup group = buildGroupFromDTO(dto, userId);

            int generatedId = activityGroupDAO.insert(group);
            if (generatedId <= 0) {
                return Result.error(500, "创建群组失败");
            }
            group.setId(generatedId);

            addGroupOwner(group.getId(), userId);
            return Result.ok(group);
        } catch (UserNotFoundException e) {
            return Result.error(404, e.getMessage());
        }
    }

    public Result updateGroup(Integer groupId, GroupDTO dto, Integer userId) {
        Result validation = validateGroupUpdateParams(groupId, dto, userId);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup existingGroup = findGroupOrFail(groupId);
            checkIsOwnerOrFail(groupId, userId, "只有群主才能更新群组");

            existingGroup.setGroupName(dto.getGroupName());
            existingGroup.setUpdatedAt(new Date());

            return Result.ok(existingGroup);
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        }
    }

    public Result deleteGroup(Integer groupId, Integer userId) {
        Result validation = validateGroupIdAndUserId(groupId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup existingGroup = findGroupOrFail(groupId);
            checkIsOwnerOrFail(groupId, userId, "只有群主才能删除群组");

            cascadeDeleteGroup(groupId);
            return Result.ok();
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        }
    }

    public Result addMember(Integer groupId, Integer userId, Integer operatorId) {
        Result validation = validateMemberOperationParams(groupId, userId, operatorId);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup group = findGroupOrFail(groupId);
            checkIsOwnerOrFail(groupId, operatorId, "只有群主才能添加成员");
            checkUserExists(userId);
            checkNotAlreadyMember(groupId, userId);

            boolean inserted = groupMemberDAO.insertMember(groupId, userId);
            if (!inserted) {
                return Result.error(500, "添加成员失败");
            }

            userGroupDAO.insertUserToGroup(userId, groupId);
            return Result.ok();
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        } catch (BadRequestException e) {
            return Result.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return Result.error(404, e.getMessage());
        }
    }

    public Result removeMember(Integer groupId, Integer userId, Integer operatorId) {
        Result validation = validateMemberOperationParams(groupId, userId, operatorId);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup group = findGroupOrFail(groupId);

            User user = userDAO.findById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            boolean isOperatorOwner = groupMemberDAO.isOwner(groupId, operatorId);
            if (!isOperatorOwner) {
                throw new UnauthorizedException("只有群主才能移除成员");
            }
            boolean isTargetOwner = groupMemberDAO.isOwner(groupId, userId);
            if (isTargetOwner) {
                throw new BadRequestException("不能移除群主");
            }
            if (!groupMemberDAO.isMember(groupId, userId)) {
                return Result.error(403, "非成员不能执行此操作");
            }

            groupMemberDAO.delete(groupId, userId);
            userGroupDAO.delete(userId, groupId);

            return Result.ok();
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (BadRequestException e) {
            return Result.error(400, e.getMessage());
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        }
    }

    public Result getMessages(Integer groupId, Integer page) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (page == null || page <= 0) {
            return Result.error(400, "页码必须大于0");
        }

        try {
            checkIsMemberOrFail(groupId, DEFAULT_OWNER_USER_ID);

            int offset = (page - 1) * DEFAULT_MESSAGE_PAGE_SIZE;
            List<GroupMessage> messages = groupMessageDAO.findByGroupId(groupId, DEFAULT_MESSAGE_PAGE_SIZE, offset);

            return Result.ok(messages);
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        }
    }

    public Result sendMessage(Integer groupId, Integer userId, String content) {
        Result validation = validateSendMessageParams(groupId, userId, content);
        if (validation != null) {
            return validation;
        }

        try {
            checkIsMemberOrFail(groupId, userId);
            checkGroupNotMuted(groupId);

            GroupMessage message = buildMessage(groupId, userId, content);
            int messageId = groupMessageDAO.insert(message);
            if (messageId <= 0) {
                return Result.error(500, "发送消息失败");
            }

            message.setId(messageId);
            return Result.ok(message);
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        }
    }

    public Result muteMember(Integer groupId, Integer targetUserId, Date until, String reason) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (targetUserId == null) {
            return Result.error(400, "目标用户ID不能为空");
        }
        if (until == null && (reason == null || reason.trim().isEmpty())) {
            return Result.error(400, "禁言原因不能为空");
        }

        try {
            ActivityGroup group = findGroupOrFail(groupId);
            checkIsOwnerOrFail(groupId, DEFAULT_OWNER_USER_ID, "只有群主才能禁言");
            checkNotMutingSelf(groupId, targetUserId, group);

            if (reason != null && reason.length() > MAX_REASON_LENGTH) {
                return Result.error(400, "禁言原因不能超过" + MAX_REASON_LENGTH + "字符");
            }

            boolean muted = activityGroupDAO.muteGroup(groupId, toSqlDate(until), reason);
            if (!muted) {
                return Result.error(500, "禁言失败");
            }

            return Result.ok();
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        } catch (BadRequestException e) {
            return Result.error(400, e.getMessage());
        }
    }

    public Result unmuteMember(Integer groupId, Integer operatorId) {
        Result validation = validateGroupIdAndUserId(groupId, operatorId);
        if (validation != null) {
            return validation;
        }

        try {
            findGroupOrFail(groupId);
            checkIsOwnerOrFail(groupId, operatorId, "只有群主才能取消禁言");

            boolean unmuted = activityGroupDAO.unmuteGroup(groupId);
            if (!unmuted) {
                return Result.error(500, "取消禁言失败");
            }

            return Result.ok();
        } catch (GroupNotFoundException e) {
            return Result.error(404, e.getMessage());
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        }
    }

    public Result deleteMessage(Integer messageId, Integer userId) {
        Result validation = validateMessageDeleteParams(messageId, userId);
        if (validation != null) {
            return validation;
        }

        try {
            GroupMessage message = findMessageOrFail(messageId);
            checkIsMessageSender(message, userId);

            boolean deleted = groupMessageDAO.delete(messageId);
            if (!deleted) {
                return Result.error(500, "删除消息失败");
            }

            return Result.ok();
        } catch (UnauthorizedException e) {
            return Result.error(403, e.getMessage());
        } catch (MessageNotFoundException e) {
            return Result.error(404, e.getMessage());
        }
    }

    public Result getMyGroups(Integer userId, int page) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<UserGroup> userGroups = userGroupDAO.findByUserId(userId);
        return Result.ok(userGroups);
    }

    public Result getCreatedGroups(Integer userId, int page) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<ActivityGroup> groups = activityGroupDAO.findByOwnerId(userId);
        return Result.ok(groups);
    }

    private Result validatePageParams(int page, int pageSize) {
        if (page <= 0) {
            return Result.error(400, "页码必须大于0");
        }
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return Result.error(400, "每页数量必须在1-100之间");
        }
        return null;
    }

    private Result validateGroupIdAndUserId(Integer groupId, Integer userId) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return null;
    }

    private Result validateCreateGroupParams(GroupDTO dto, Integer userId) {
        if (dto == null) {
            return Result.error(400, "群组信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (dto.getGroupName() == null || dto.getGroupName().trim().isEmpty()) {
            return Result.error(400, "群组名称不能为空");
        }
        if (dto.getGroupName().length() > MAX_GROUP_NAME_LENGTH) {
            return Result.error(400, "群组名称不能超过255个字符");
        }
        return null;
    }

    private Result validateGroupUpdateParams(Integer groupId, GroupDTO dto, Integer userId) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "群组信息不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (dto.getGroupName() == null || dto.getGroupName().trim().isEmpty()) {
            return Result.error(400, "群组名称不能为空");
        }
        return null;
    }

    private Result validateMemberOperationParams(Integer groupId, Integer userId, Integer operatorId) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    private Result validateSendMessageParams(Integer groupId, Integer userId, String content) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error(400, "消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            return Result.error(400, "消息内容不能超过5000个字符");
        }
        return null;
    }

    private Result validateMessageDeleteParams(Integer messageId, Integer userId) {
        if (messageId == null) {
            return Result.error(400, "消息ID不能为空");
        }
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        return null;
    }

    private ActivityGroup findGroupOrFail(Integer groupId) {
        ActivityGroup group = activityGroupDAO.findById(groupId);
        if (group == null) {
            throw new GroupNotFoundException("群组不存在");
        }
        return group;
    }

    private User findUserOrFail(Integer userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("用户不存在");
        }
        return user;
    }

    private GroupMessage findMessageOrFail(Integer messageId) {
        List<GroupMessage> messages = groupMessageDAO.findByGroupId(messageId, 1, 0);
        if (messages == null || messages.isEmpty()) {
            throw new MessageNotFoundException("消息不存在");
        }
        return messages.get(0);
    }

    private boolean isGroupMemberOrOwner(Integer groupId, Integer userId) {
        boolean isMember = groupMemberDAO.isMember(groupId, userId);
        boolean isOwner = groupMemberDAO.isOwner(groupId, userId);
        return isMember || isOwner;
    }

    private void checkIsOwnerOrFail(Integer groupId, Integer userId, String errorMessage) {
        if (!groupMemberDAO.isOwner(groupId, userId)) {
            throw new UnauthorizedException(errorMessage);
        }
    }

    private void checkNotAlreadyMember(Integer groupId, Integer userId) {
        if (groupMemberDAO.isMember(groupId, userId)) {
            throw new BadRequestException("该用户已是群成员");
        }
    }

    private void checkIsMemberOrFail(Integer groupId, Integer userId) {
        if (!groupMemberDAO.isMember(groupId, userId)) {
            throw new UnauthorizedException("非成员不能执行此操作");
        }
    }

    private void checkTargetNotOwner(Integer groupId, Integer userId, ActivityGroup group) {
        if (groupMemberDAO.isOwner(groupId, userId)) {
            throw new BadRequestException("不能移除群主");
        }
    }

    private void checkNotMutingSelf(Integer groupId, Integer targetUserId, ActivityGroup group) {
        if (groupMemberDAO.isOwner(groupId, targetUserId)) {
            throw new BadRequestException("不能禁言自己");
        }
    }

    private void checkGroupNotMuted(Integer groupId) {
        ActivityGroup group = activityGroupDAO.findById(groupId);
        if (group != null && group.isMuted()) {
            throw new UnauthorizedException("群组已被禁言");
        }
    }

    private void checkUserExists(Integer userId) {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
    }

    private void checkIsMessageSender(GroupMessage message, Integer userId) {
        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedException("只能删除自己发送的消息");
        }
    }

    private ActivityGroup buildGroupFromDTO(GroupDTO dto, Integer userId) {
        ActivityGroup group = new ActivityGroup();
        group.setGroupName(dto.getGroupName());
        group.setActivityId(dto.getActivityId());
        group.setGroupOwnerId(userId);
        group.setCreatedAt(new Date());
        return group;
    }

    private GroupMessage buildMessage(Integer groupId, Integer userId, String content) {
        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(userId);
        message.setContent(content);
        message.setMessageType(GroupMessage.MESSAGE_TYPE_TEXT);
        message.setSentAt(new Date());
        return message;
    }

    private void addGroupOwner(Integer groupId, Integer userId) {
        groupMemberDAO.insertOwner(groupId, userId);
        userGroupDAO.insertUserToGroup(userId, groupId);
    }

    private void cascadeDeleteGroup(Integer groupId) {
        groupMessageDAO.deleteByGroupId(groupId);
        groupMemberDAO.deleteByGroupId(groupId);
        userGroupDAO.deleteByGroupId(groupId);
        activityGroupDAO.delete(groupId);
    }

    private java.sql.Date toSqlDate(Date date) {
        return date != null ? new java.sql.Date(date.getTime()) : null;
    }

    private static class GroupNotFoundException extends RuntimeException {
        GroupNotFoundException(String message) {
            super(message);
        }
    }

    private static class UserNotFoundException extends RuntimeException {
        UserNotFoundException(String message) {
            super(message);
        }
    }

    private static class MessageNotFoundException extends RuntimeException {
        MessageNotFoundException(String message) {
            super(message);
        }
    }

    private static class UnauthorizedException extends RuntimeException {
        UnauthorizedException(String message) {
            super(message);
        }
    }

    private static class BadRequestException extends RuntimeException {
        BadRequestException(String message) {
            super(message);
        }
    }

    private static class NotFoundException extends RuntimeException {
        NotFoundException(String message) {
            super(message);
        }
    }
}
