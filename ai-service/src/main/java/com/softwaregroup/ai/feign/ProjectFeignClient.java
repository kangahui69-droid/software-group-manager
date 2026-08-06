package com.softwaregroup.ai.feign;

import com.softwaregroup.ai.model.dto.ProjectDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目服务Feign客户端
 */
@FeignClient(name = "project-award-service")
public interface ProjectFeignClient {
    @GetMapping("/api/projects")
    Result getProjects();

    @GetMapping("/api/projects/user/{userId}")
    Result getUserProjects(@PathVariable("userId") Integer userId);

    @GetMapping("/api/projects/public")
    Result getPublicProjects();
}
