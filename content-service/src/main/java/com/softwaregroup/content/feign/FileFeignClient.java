package com.softwaregroup.content.feign;

import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 文件服务Feign客户端
 */
@FeignClient(name = "file-service", path = "/api/files")
public interface FileFeignClient {

    @GetMapping("/{id}/urls")
    Result getFileUrls(@PathVariable("id") List<Integer> ids);
}
