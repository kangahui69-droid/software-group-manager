package com.softwaregroup.file.feign;

import com.softwaregroup.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务Feign客户端接口
 *
 * 供其他微服务调用file-service的文件上传功能
 *
 * 使用方式：
 * <pre>
 * {@code
 * @Autowired
 * private FileFeignClient fileFeignClient;
 *
 * // 上传文件
 * Result result = fileFeignClient.uploadFile(file, "images/avatar", userId);
 *
 * // 查看文件元信息
 * Result result = fileFeignClient.getFileInfo(fileId);
 *
 * // 获取预签名下载URL
 * Result result = fileFeignClient.getDownloadUrl(fileId);
 *
 * // 删除文件
 * Result result = fileFeignClient.deleteFile(fileId, userId);
 * }
 * </pre>
 */
@FeignClient(name = "file-service", path = "/api/files")
public interface FileFeignClient {

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param category 文件分类（目录）
     * @param userId 上传用户ID
     * @return 上传结果（包含文件ID和元信息）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Result uploadFile(@RequestPart("file") MultipartFile file,
                      @RequestParam("category") String category,
                      @RequestParam("userId") Integer userId);

    /**
     * 获取文件元信息
     *
     * @param fileId 文件ID
     * @return 文件元信息
     */
    @GetMapping("/{fileId}")
    Result getFileInfo(@PathVariable("fileId") Integer fileId);

    /**
     * 获取预签名下载URL
     *
     * @param fileId 文件ID
     * @return 预签名URL（有效期1小时）
     */
    @GetMapping("/{fileId}/url")
    Result getDownloadUrl(@PathVariable("fileId") Integer fileId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 操作者用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{fileId}")
    Result deleteFile(@PathVariable("fileId") Integer fileId,
                     @RequestParam("userId") Integer userId);

    /**
     * 列出用户的文件
     *
     * @param category 文件分类（可选，null表示所有）
     * @param userId 用户ID
     * @return 文件列表
     */
    @GetMapping
    Result listFiles(@RequestParam(value = "category", required = false) String category,
                    @RequestParam("userId") Integer userId);
}
