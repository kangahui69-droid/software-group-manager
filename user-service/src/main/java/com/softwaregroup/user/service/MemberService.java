package com.softwaregroup.user.service;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.dao.MemberProfileDAO;
import com.softwaregroup.user.dao.UserDAO;
import com.softwaregroup.user.model.dto.UserDTO;
import com.softwaregroup.user.model.entity.MemberProfile;
import com.softwaregroup.user.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 成员服务层
 *
 * 处理成员的增删改查、状态管理、档案管理等功能
 */
@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_USERNAME_LENGTH = 32;
    private static final String DEFAULT_RESET_PASSWORD = "123456";
    private static final Integer STATUS_ENABLED = 1;
    private static final Integer STATUS_DISABLED = 0;

    private static final String EMAIL_PATTERN = "^[\\w.-]+@[\\w.-]+\\.\\w+$";
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private static final String STUDENT_ID_PATTERN = "^\\d+$";
    private static final String GRADE_PATTERN = "^\\d{4}$";

    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;

    public MemberService(UserDAO userDAO, MemberProfileDAO memberProfileDAO) {
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
    }

    /**
     * 获取成员列表（分页）
     */
    public Result listMembers(String keyword, String role, String status, Integer page, Integer pageSize) {
        page = normalizePage(page);
        pageSize = normalizePageSize(pageSize);

        List<User> members = userDAO.findByConditions(keyword, role, status);
        int total = userDAO.count();

        List<UserDTO> memberDTOs = members.stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());

        return Result.ok(buildPageResult(memberDTOs, total, page, pageSize));
    }

    /**
     * 获取成员详情（含档案）
     */
    public Result getMemberDetail(Integer id) {
        if (id == null) {
            return Result.error(400, "成员ID不能为空");
        }

        User user = userDAO.findById(id);
        if (user == null) {
            return Result.error(404, "成员不存在");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(id);
        return Result.ok(buildMemberDetailResult(user, profile));
    }

    /**
     * 创建成员
     */
    public Result createMember(Map<String, Object> dto, Integer operatorId) {
        // 验证操作者权限
        Result authCheck = requireAdminRole(operatorId);
        if (authCheck != null) {
            return authCheck;
        }

        String username = extractString(dto, "username");
        String password = extractString(dto, "password");
        String role = extractString(dto, "role");
        String email = extractString(dto, "email");
        String phone = extractString(dto, "phone");
        String name = extractString(dto, "name");

        // 验证输入
        if (isBlank(username)) return Result.error(400, "用户名不能为空");
        if (isBlank(password)) return Result.error(400, "密码不能为空");
        if (isBlank(role)) return Result.error(400, "角色不能为空");
        if (!isValidRole(role)) return Result.error(400, "无效的角色");
        if (username.length() > MAX_USERNAME_LENGTH) {
            return Result.error(400, "用户名不能超过" + MAX_USERNAME_LENGTH + "个字符");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Result.error(400, "密码不能少于" + MIN_PASSWORD_LENGTH + "个字符");
        }
        if (!isBlank(email) && !isValidEmail(email)) {
            return Result.error(400, "邮箱格式不正确");
        }
        if (!isBlank(phone) && !isValidPhone(phone)) {
            return Result.error(400, "手机号格式不正确");
        }

        // 检查用户名是否存在
        if (userDAO.existsByUsername(username)) {
            return Result.error(400, "用户名已存在");
        }

        // 构建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 密码应该在调用方已经加密
        user.setName(name != null ? name : username);
        user.setEmail(isBlank(email) ? username + "@default.com" : email);
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(STATUS_ENABLED);

        Integer userId = userDAO.insert(user);
        if (userId == null) {
            return Result.error(500, "创建成员失败");
        }
        user.setId(userId);

        return Result.ok(UserDTO.fromUser(user));
    }

    /**
     * 更新成员
     */
    public Result updateMember(Integer id, Map<String, Object> dto, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "成员ID不能为空");
        }
        if (dto == null || dto.isEmpty()) {
            return Result.error(400, "更新信息不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }

        User user = userDAO.findById(id);
        if (user == null) {
            return Result.error(404, "成员不存在");
        }

        // 验证权限：管理员或本人
        Result authResult = validateUpdateAuthorization(id, operatorId);
        if (authResult != null) {
            return authResult;
        }

        String email = extractString(dto, "email");
        String phone = extractString(dto, "phone");
        String name = extractString(dto, "name");

        if (!isBlank(email) && !isValidEmail(email)) {
            return Result.error(400, "邮箱格式不正确");
        }
        if (!isBlank(phone) && !isValidPhone(phone)) {
            return Result.error(400, "手机号格式不正确");
        }

        // 更新字段
        if (!isBlank(name)) user.setName(name);
        if (!isBlank(email)) user.setEmail(email);
        if (!isBlank(phone)) user.setPhone(phone);

        boolean updated = userDAO.update(user);
        return updated ? Result.ok(UserDTO.fromUser(user)) : Result.error(500, "更新成员失败");
    }

    /**
     * 删除成员
     */
    public Result deleteMember(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) {
            return validation;
        }

        User user = userDAO.findById(id);
        if (user == null) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId);
        if (authResult != null) {
            return authResult;
        }

        if (id.equals(operatorId)) {
            return Result.error(400, "不能删除自己");
        }

        boolean deleted = userDAO.delete(id);
        return deleted ? Result.ok() : Result.error(500, "删除成员失败");
    }

    /**
     * 启用成员
     */
    public Result enableMember(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) {
            return validation;
        }

        if (!isMemberExists(id)) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId);
        if (authResult != null) {
            return authResult;
        }

        return executeStatusUpdate(id, STATUS_ENABLED, "启用成员");
    }

    /**
     * 禁用成员
     */
    public Result disableMember(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) {
            return validation;
        }

        if (!isMemberExists(id)) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId);
        if (authResult != null) {
            return authResult;
        }

        if (id.equals(operatorId)) {
            return Result.error(400, "不能禁用自己");
        }

        return executeStatusUpdate(id, STATUS_DISABLED, "禁用成员");
    }

    /**
     * 重置密码
     */
    public Result resetPassword(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperator(id, operatorId, "成员ID", "操作者ID");
        if (validation != null) {
            return validation;
        }

        if (!isMemberExists(id)) {
            return Result.error(404, "成员不存在");
        }

        Result authResult = requireAdminRole(operatorId);
        if (authResult != null) {
            return authResult;
        }

        if (id.equals(operatorId)) {
            return Result.error(400, "不能重置自己的密码");
        }

        // 重置为默认密码123456（实际应该加密后存储）
        boolean reset = userDAO.resetPassword(id, DEFAULT_RESET_PASSWORD);
        return reset ? Result.ok() : Result.error(500, "重置密码失败");
    }

    // ==================== 私有辅助方法 ====================

    private int normalizePage(Integer page) {
        if (page == null || page <= 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    private Map<String, Object> buildPageResult(List<UserDTO> members, int total, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", members);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    private Map<String, Object> buildMemberDetailResult(User user, MemberProfile profile) {
        Map<String, Object> result = new HashMap<>();
        result.put("user", UserDTO.fromUser(user));
        result.put("profile", profile);
        return result;
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "MEMBER".equals(role) || "TEACHER".equals(role);
    }

    private boolean isValidEmail(String email) {
        return email.matches(EMAIL_PATTERN);
    }

    private boolean isValidPhone(String phone) {
        return phone.matches(PHONE_PATTERN);
    }

    private boolean isAdmin(Integer operatorId) {
        if (operatorId == null) return false;
        User operator = userDAO.findById(operatorId);
        return operator != null && "ADMIN".equals(operator.getRole());
    }

    private boolean isSelfOperation(Integer id, Integer operatorId) {
        return id != null && id.equals(operatorId);
    }

    private Result requireAdminRole(Integer operatorId) {
        if (!isAdmin(operatorId)) {
            return Result.error(403, "无权限操作");
        }
        return null;
    }

    private Result validateUpdateAuthorization(Integer id, Integer operatorId) {
        if (!isAdmin(operatorId) && !isSelfOperation(id, operatorId)) {
            return Result.error(403, "无权限更新此成员信息");
        }
        return null;
    }

    private Result validateIdAndOperator(Integer id, Integer operatorId, String idName, String operatorName) {
        if (id == null) {
            return Result.error(400, idName + "不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, operatorName + "不能为空");
        }
        return null;
    }

    private String extractString(Map<String, Object> dto, String key) {
        Object value = dto.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private boolean isMemberExists(Integer id) {
        return userDAO.findById(id) != null;
    }

    private Result executeStatusUpdate(Integer id, Integer status, String operation) {
        boolean updated = userDAO.updateStatus(id, status);
        return updated ? Result.ok() : Result.error(500, operation + "失败");
    }
}
