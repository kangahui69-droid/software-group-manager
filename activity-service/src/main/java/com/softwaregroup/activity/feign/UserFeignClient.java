package com.softwaregroup.activity.feign;

import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 调用 user-service 的 Feign 客户端
 */
@FeignClient(name = "user-service")
public interface UserFeignClient {

    /**
     * 获取用户信息
     */
    @GetMapping("/api/users/{userId}")
    Result getUserById(@PathVariable("userId") Integer userId);

    /**
     * 验证用户是否为管理员
     */
    @GetMapping("/api/users/{userId}/role")
    Result getUserRole(@PathVariable("userId") Integer userId);

    /**
     * 检查用户是否存在
     */
    @GetMapping("/api/users/{userId}/exists")
    Result checkUserExists(@PathVariable("userId") Integer userId);
}
