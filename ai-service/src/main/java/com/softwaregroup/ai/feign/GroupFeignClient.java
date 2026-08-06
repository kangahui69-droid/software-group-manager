package com.softwaregroup.ai.feign;

import com.softwaregroup.ai.model.dto.GroupDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组服务Feign客户端
 */
@FeignClient(name = "content-service")
public interface GroupFeignClient {
    @GetMapping("/api/groups/user/{userId}/my-groups")
    Result getUserGroups(@PathVariable("userId") Integer userId);

    @GetMapping("/api/groups/{id}")
    Result getGroupById(@PathVariable("id") Integer id);
}
