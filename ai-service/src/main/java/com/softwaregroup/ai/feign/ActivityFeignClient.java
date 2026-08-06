package com.softwaregroup.ai.feign;

import com.softwaregroup.ai.model.dto.ActivityDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动服务Feign客户端
 */
@FeignClient(name = "activity-service")
public interface ActivityFeignClient {
    @GetMapping("/api/activities")
    Result getActivities();

    @GetMapping("/api/activities/{id}")
    Result getActivityById(@PathVariable("id") Integer id);

    @GetMapping("/api/activities/upcoming")
    Result getUpcomingActivities();
}
