package com.softwaregroup.user.controller;

import com.softwaregroup.common.util.JwtUtil;
import com.softwaregroup.common.util.Result;
import com.softwaregroup.user.model.dto.LoginRequest;
import com.softwaregroup.user.model.dto.ProfileDTO;
import com.softwaregroup.user.model.dto.RegisterRequest;
import com.softwaregroup.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * 处理用户登录、注册、密码修改等认证相关请求
 */
@RestController
@RequestMapping("/api/users")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * 修改密码
     * PUT /api/users/password
     */
    @PutMapping("/password")
    public Result changePassword(@RequestHeader("X-User-Id") Integer userId,
                                  @RequestBody Map<String, String> passwordData) {
        String oldPwd = passwordData.get("oldPassword");
        String newPwd = passwordData.get("newPassword");
        return authService.changePassword(userId, oldPwd, newPwd);
    }

    /**
     * 获取当前用户详情
     * GET /api/users/profile
     */
    @GetMapping("/profile")
    public Result getProfile(@RequestHeader("X-User-Id") Integer userId) {
        return authService.getUserDetail(userId);
    }

    /**
     * 更新个人档案
     * PUT /api/users/profile
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestHeader("X-User-Id") Integer userId,
                                 @RequestBody ProfileDTO profileDTO) {
        return authService.updateProfile(userId, profileDTO);
    }

    /**
     * 获取指定用户详情
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public Result getUserById(@PathVariable("userId") Integer userId) {
        return authService.getUserDetail(userId);
    }

    /**
     * 健康检查
     * GET /api/users/health
     */
    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "user-service"));
    }
}
