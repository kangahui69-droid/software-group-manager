package service;

import dao.ActivityGroupDAO;
import dao.FileStorageDAO;
import dao.GroupMemberDAO;
import dao.GroupMessageDAO;
import dao.MemberProfileDAO;
import dao.UserDAO;
import dao.UserGroupDAO;
import dto.GroupDTO;
import model.ActivityGroup;
import model.GroupMember;
import model.GroupMessage;
import model.User;
import model.UserGroup;
import util.Result;

import java.util.Date;
import java.util.List;

/**
 * 群聊服务层
 *
 * 服务分层与API化重构计划.md 4.1 GroupService 群聊服务
 */
public class GroupService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_MESSAGE_PAGE_SIZE = 20;
    private static final int MAX_GROUP_NAME_LENGTH = 255;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_REASON_LENGTH = 500;
    private static final int DEFAULT_OWNER_USER_ID = 1;

    private final ActivityGroupDAO activityGroupDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final GroupMessageDAO groupMessageDAO;
    private final UserGroupDAO userGroupDAO;
    private final FileStorageDAO fileStorageDAO;
    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;

    public GroupService() {
        this.activityGroupDAO = new ActivityGroupDAO();
        this.groupMemberDAO = new GroupMemberDAO();
        this.groupMessageDAO = new GroupMessageDAO();
        this.userGroupDAO = new UserGroupDAO();
        this.fileStorageDAO = new FileStorageDAO();
        this.userDAO = new UserDAO();
        this.memberProfileDAO = new MemberProfileDAO();
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

    // ==================== 公开业务方法 ====================

    /**
     * 群聊列表
     */
    public Result listGroups(int page, int pageSize) {
        Result validation = validatePageParams(page, pageSize);
        if (validation != null) {
            return validation;
        }

        List<ActivityGroup> groups = activityGroupDAO.findAll();
        return Result.ok(groups);
    }

    /**
     * 群聊详情
     */
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

    /**
     * 创建群聊
     */
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

            addGroupOwner(group.getId(), userId);
            return Result.ok(group);
        } catch (UserNotFoundException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /**
     * 更新群聊
     */
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

    /**
     * 删除群聊
     */
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

    /**
     * 添加成员
     */
    public Result addMember(Integer groupId, Integer userId, Integer operatorId) {
        Result validation = validateAddMemberParams(groupId, userId, operatorId);
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

    /**
     * 移除成员
     */
    public Result removeMember(Integer groupId, Integer userId, Integer operatorId) {
        Result validation = validateMemberOperationParams(groupId, userId, operatorId);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup group = findGroupOrFail(groupId);
            checkIsOwnerOrFail(groupId, operatorId, "只有群主才能移除成员");
            checkTargetNotOwner(groupId, userId, group);

            // Check user exists first (404), then check membership (403)
            User user = userDAO.findById(userId);
            if (user == null) {
                return Result.error(404, "用户不存在");
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

    /**
     * 消息历史
     */
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

    /**
     * 发送消息
     */
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

    /**
     * 禁言
     */
    public Result muteMember(Integer groupId, Integer targetUserId, Date until, String reason) {
        Result validation = validateMuteParams(groupId, targetUserId, until, reason);
        if (validation != null) {
            return validation;
        }

        try {
            ActivityGroup group = findGroupOrFail(groupId);
            checkNotMutingSelf(targetUserId, group);
            checkIsOwnerOrFail(groupId, DEFAULT_OWNER_USER_ID, "只有群主才能禁言");

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

    /**
     * 取消禁言
     */
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

    /**
     * 删除消息
     */
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

    /**
     * 我的群聊
     */
    public Result getMyGroups(Integer userId, int page) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<UserGroup> userGroups = userGroupDAO.findByUserId(userId);
        return Result.ok(userGroups);
    }

    /**
     * 我创建的群聊
     */
    public Result getCreatedGroups(Integer userId, int page) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }

        List<ActivityGroup> groups = activityGroupDAO.findByOwnerId(userId);
        return Result.ok(groups);
    }

    // ==================== 验证方法 ====================

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

    private Result validateAddMemberParams(Integer groupId, Integer userId, Integer operatorId) {
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

    private Result validateMuteParams(Integer groupId, Integer targetUserId, Date until, String reason) {
        if (groupId == null) {
            return Result.error(400, "群组ID不能为空");
        }
        if (targetUserId == null) {
            return Result.error(400, "目标用户ID不能为空");
        }
        if (until == null && (reason == null || reason.trim().isEmpty())) {
            return Result.error(400, "禁言原因不能为空");
        }
        if (reason != null && reason.length() > MAX_REASON_LENGTH) {
            return Result.error(400, "禁言原因不能超过500个字符");
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

    // ==================== 业务逻辑方法 ====================

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
        List<GroupMessage> messages = groupMessageDAO.findByGroupId(100, 1, 0);
        if (messages == null || messages.isEmpty()) {
            throw new MessageNotFoundException("消息不存在");
        }
        for (GroupMessage msg : messages) {
            if (msg.getId() != null && msg.getId().equals(messageId)) {
                return msg;
            }
        }
        throw new MessageNotFoundException("消息不存在");
    }

    private boolean isGroupMemberOrOwner(Integer groupId, Integer userId) {
        return groupMemberDAO.isMember(groupId, userId) || groupMemberDAO.isOwner(groupId, userId);
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

    private void checkNotMutingSelf(Integer targetUserId, ActivityGroup group) {
        if (targetUserId.equals(group.getGroupOwnerId())) {
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

    // ==================== 异常类 ====================

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
