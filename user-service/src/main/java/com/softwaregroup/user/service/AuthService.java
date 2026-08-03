package com.softwaregroup.user.service;

import com.softwaregroup.common.util.JwtUtil;
import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.dao.MemberProfileDAO;
import com.softwaregroup.user.dao.UserDAO;
import com.softwaregroup.user.model.dto.LoginRequest;
import com.softwaregroup.user.model.dto.ProfileDTO;
import com.softwaregroup.user.model.dto.RegisterRequest;
import com.softwaregroup.user.model.dto.UserDTO;
import com.softwaregroup.user.model.entity.MemberProfile;
import com.softwaregroup.user.model.entity.User;
import com.softwaregroup.user.util.DESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 认证服务层
 *
 * 处理用户登录、注册、密码修改等认证相关业务
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DISABLED = 0;
    private static final String ADMIN_ROLE = "ADMIN";

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String GITHUB_URL_PATTERN = "^(https?://)?(www\\.)?github\\.com/[A-Za-z0-9_-]+/?$";
    private static final String BLOG_URL_PATTERN = "^(https?://)?[A-Za-z0-9.-]+\\.[A-Za-z]{2,}/?$";

    private final UserDAO userDAO;
    private final MemberProfileDAO memberProfileDAO;
    private final DESUtil desUtil;

    @Value("${spring.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    public AuthService(UserDAO userDAO, MemberProfileDAO memberProfileDAO, DESUtil desUtil) {
        this.userDAO = userDAO;
        this.memberProfileDAO = memberProfileDAO;
        this.desUtil = desUtil;
    }

    /**
     * 用户登录
     */
    public Result login(LoginRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return Result.error(400, "用户名或密码不能为空");
        }

        User existingUser = userDAO.findByUsername(request.getUsername());
        if (existingUser == null) {
            return Result.error(404, "用户名或密码错误");
        }

        // 密码加密后比对
        String encryptedPassword = desUtil.encrypt(request.getPassword());
        User user = userDAO.findByUsernameAndPassword(request.getUsername(), encryptedPassword);
        if (user == null) {
            return Result.error(400, "密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == STATUS_DISABLED) {
            return Result.error(403, "用户已被禁用");
        }

        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), jwtExpirationMs);

        return Result.ok(Map.of(
                "token", token,
                "user", UserDTO.fromUser(user)
        ));
    }

    /**
     * 用户注册
     */
    public Result register(RegisterRequest request) {
        // 验证输入
        Result validation = validateRegisterInput(request);
        if (validation != null) {
            return validation;
        }

        // 检查用户名是否已存在
        if (userDAO.existsByUsername(request.getUsername())) {
            return Result.error(400, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userDAO.existsByEmail(request.getEmail())) {
            return Result.error(400, "邮箱已被使用");
        }

        // 构建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(desUtil.encrypt(request.getPassword()));
        user.setName(request.getName() != null ? request.getName() : request.getUsername());
        user.setEmail(request.getEmail() != null ? request.getEmail() : request.getUsername() + "@default.com");
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setStatus(STATUS_NORMAL);

        // 保存用户
        Integer userId = userDAO.insert(user);
        if (userId == null) {
            return Result.error(500, "创建用户失败");
        }
        user.setId(userId);

        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), jwtExpirationMs);

        return Result.ok(Map.of(
                "token", token,
                "user", UserDTO.fromUser(user)
        ));
    }

    /**
     * 修改密码
     */
    public Result changePassword(Integer userId, String oldPwd, String newPwd) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (isBlank(oldPwd)) {
            return Result.error(400, "旧密码不能为空");
        }
        if (isBlank(newPwd)) {
            return Result.error(400, "新密码不能为空");
        }
        if (newPwd.length() < MIN_PASSWORD_LENGTH) {
            return Result.error(400, "新密码长度至少6位");
        }
        if (newPwd.length() > MAX_PASSWORD_LENGTH) {
            return Result.error(400, "新密码长度不能超过20位");
        }
        if (newPwd.equals(oldPwd)) {
            return Result.error(400, "新密码不能与旧密码相同");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 验证旧密码
        String oldEncrypted = desUtil.encrypt(oldPwd);
        if (!oldEncrypted.equals(user.getPassword())) {
            return Result.error(400, "旧密码错误");
        }

        // 更新密码
        String newEncrypted = desUtil.encrypt(newPwd);
        boolean updated = userDAO.updatePassword(userId, newEncrypted);
        if (!updated) {
            return Result.error(500, "密码修改失败");
        }

        return Result.ok();
    }

    /**
     * 获取用户详情（含档案）
     */
    public Result getUserDetail(Integer userId) {
        if (userId == null || userId <= 0) {
            return Result.error(400, "用户ID无效");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        MemberProfile profile = memberProfileDAO.findByUserId(userId);
        return Result.ok(UserDTO.fromUserWithProfile(user, profile));
    }

    /**
     * 更新个人档案
     */
    public Result updateProfile(Integer userId, ProfileDTO profileDTO) {
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (profileDTO == null) {
            return Result.error(400, "档案信息不能为空");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 验证字段
        Result validation = validateProfileFields(profileDTO);
        if (validation != null) {
            return validation;
        }

        // 更新用户邮箱和手机
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }

        try {
            userDAO.update(user);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                return Result.error(400, "邮箱已被使用");
            }
            throw e;
        }

        // 更新档案
        MemberProfile profile = memberProfileDAO.findByUserId(userId);
        profile = buildProfileFromDTO(userId, profileDTO, profile);
        memberProfileDAO.saveOrUpdate(profile);

        return Result.ok();
    }

    // ==================== 私有辅助方法 ====================

    private Result validateRegisterInput(RegisterRequest request) {
        if (isBlank(request.getUsername())) {
            return Result.error(400, "用户名不能为空");
        }
        if (isBlank(request.getPassword())) {
            return Result.error(400, "密码不能为空");
        }
        if (isBlank(request.getRole())) {
            return Result.error(400, "角色不能为空");
        }
        if (!isValidRole(request.getRole())) {
            return Result.error(400, "无效的角色");
        }
        if (request.getUsername().length() > 32) {
            return Result.error(400, "用户名不能超过32个字符");
        }
        if (request.getPassword().length() < MIN_PASSWORD_LENGTH) {
            return Result.error(400, "密码不能少于6个字符");
        }
        if (request.getPassword().length() > MAX_PASSWORD_LENGTH) {
            return Result.error(400, "密码不能超过20个字符");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() && !request.getEmail().matches(EMAIL_PATTERN)) {
            return Result.error(400, "邮箱格式不正确");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty() && !request.getPhone().matches(PHONE_PATTERN)) {
            return Result.error(400, "手机号格式不正确");
        }
        return null;
    }

    private Result validateProfileFields(ProfileDTO profileDTO) {
        if (profileDTO.getEmail() != null && !profileDTO.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matches(profileDTO.getEmail().trim())) {
                return Result.error(400, "邮箱格式错误");
            }
        }
        if (profileDTO.getPhone() != null && !profileDTO.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matches(profileDTO.getPhone().trim())) {
                return Result.error(400, "手机号格式错误");
            }
        }
        if (profileDTO.getBirthday() != null && !profileDTO.getBirthday().trim().isEmpty()) {
            if (!DATE_PATTERN.matches(profileDTO.getBirthday().trim())) {
                return Result.error(400, "生日格式错误");
            }
        }
        if (profileDTO.getIntroduction() != null && profileDTO.getIntroduction().length() > 500) {
            return Result.error(400, "简介不能超过500字符");
        }
        if (profileDTO.getGithub() != null && !profileDTO.getGithub().trim().isEmpty()) {
            if (!GITHUB_URL_PATTERN.matches(profileDTO.getGithub().trim())) {
                return Result.error(400, "GitHub链接格式错误");
            }
        }
        if (profileDTO.getBlog() != null && !profileDTO.getBlog().trim().isEmpty()) {
            if (!BLOG_URL_PATTERN.matches(profileDTO.getBlog().trim())) {
                return Result.error(400, "博客链接格式错误");
            }
        }
        return null;
    }

    private MemberProfile buildProfileFromDTO(Integer userId, ProfileDTO dto, MemberProfile existing) {
        MemberProfile profile = existing != null ? existing : new MemberProfile();
        profile.setUserId(userId);

        if (dto.getBirthday() != null && !dto.getBirthday().trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                profile.setBirthday(sdf.parse(dto.getBirthday()));
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        maybeSetField(dto.getMajor(), profile::setMajor);
        maybeSetField(dto.getGrade(), profile::setGrade);
        maybeSetField(dto.getIntroduction(), profile::setIntroduction);
        maybeSetField(dto.getGithub(), profile::setGithub);
        maybeSetField(dto.getBlog(), profile::setBlog);

        return profile;
    }

    private boolean isValidRole(String role) {
        return "ADMIN".equals(role) || "MEMBER".equals(role) || "TEACHER".equals(role);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void maybeSetField(String value, Consumer<String> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
