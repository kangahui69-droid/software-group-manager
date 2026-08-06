package com.softwaregroup.ai.feign;

import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务Feign客户端
 */
@FeignClient(name = "file-service")
public interface FileFeignClient {
    @GetMapping("/api/files/{id}")
    Result getFileInfo(@PathVariable("id") Integer id);
}
