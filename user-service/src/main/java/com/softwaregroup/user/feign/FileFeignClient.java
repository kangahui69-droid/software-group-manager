package com.softwaregroup.user.feign;

import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务Feign客户端接口
 *
 * 供user-service调用file-service的文件上传功能
 */
@FeignClient(name = "file-service", path = "/api/files")
public interface FileFeignClient {

    /**
     * 上传文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result uploadFile(@RequestPart("file") MultipartFile file,
                      @RequestParam("category") String category,
                      @RequestParam("userId") Integer userId);

    /**
     * 获取文件元信息
     */
    @GetMapping("/{fileId}")
    Result getFileInfo(@PathVariable("fileId") Integer fileId);

    /**
     * 获取预签名下载URL
     */
    @GetMapping("/{fileId}/url")
    Result getDownloadUrl(@PathVariable("fileId") Integer fileId);

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    Result deleteFile(@PathVariable("fileId") Integer fileId,
                     @RequestParam("userId") Integer userId);
}
