package com.softwaregroup.ai.feign;

import com.softwaregroup.ai.model.dto.UserDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service")
public interface UserFeignClient {
    @GetMapping("/api/users/{userId}")
    Result getUserById(@PathVariable("userId") Integer userId);
}
