package com.softwaregroup.ai.feign;

import com.softwaregroup.ai.model.dto.AwardDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 奖项服务Feign客户端
 */
@FeignClient(name = "project-award-service")
public interface AwardFeignClient {
    @GetMapping("/api/awards")
    Result getAwards();

    @GetMapping("/api/awards/user/{userId}")
    Result getUserAwards(@PathVariable("userId") Integer userId);

    @GetMapping("/api/awards/pending")
    Result getPendingAwards();
}
