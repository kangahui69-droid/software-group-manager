package com.softwaregroup.monitor.controller;

import com.softwaregroup.monitor.service.LogService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 操作日志 Controller
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public Result listLogs(@RequestParam(required = false) Map<String, Object> filter,
                            @RequestParam(required = false, defaultValue = "1") Integer page,
                            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return logService.listLogs(filter, page, pageSize);
    }

    @GetMapping("/{id}")
    public Result getLogDetail(@PathVariable Integer id) {
        return logService.getLogDetail(id);
    }

    @GetMapping("/health")
    public Result health() {
        return Result.ok(Map.of("status", "UP", "service", "monitor-service"));
    }
}
