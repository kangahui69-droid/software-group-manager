package com.softwaregroup.file.integration;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.file.dao.FileStorageDAO;
import com.softwaregroup.file.model.entity.FileStorage;
import com.softwaregroup.file.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FileService 集成测试
 *
 * 测试文件服务的核心功能：参数验证、错误处理
 * 注意：实际MinIO调用需要真实MinIO环境，这些测试专注于业务逻辑验证
 */
@SpringBootTest
@ActiveProfiles("test")
class FileServiceIT {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileStorageDAO fileStorageDAO;

    @Test
    void uploadFile_withEmptyFile_shouldReturnError() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        Result result = fileService.uploadFile(emptyFile, "test", 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("不能为空");
    }

    @Test
    void uploadFile_withNullUserId_shouldReturnError() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        Result result = fileService.uploadFile(file, "test", null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("用户ID");
    }

    @Test
    void uploadFile_withPathTraversal_shouldReturnError() {
        MockMultipartFile maliciousFile = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "text/plain",
                "malicious".getBytes()
        );

        Result result = fileService.uploadFile(maliciousFile, "test", 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("路径遍历");
    }

    @Test
    void uploadFile_withTooLargeFileName_shouldReturnError() {
        String longFileName = "a".repeat(300) + ".txt";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                longFileName,
                "text/plain",
                "content".getBytes()
        );

        Result result = fileService.uploadFile(file, "test", 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("255");
    }

    @Test
    void viewFile_withNullId_shouldReturnError() {
        Result result = fileService.viewFile(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void viewFile_withZeroId_shouldReturnError() {
        Result result = fileService.viewFile(0);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void viewFile_withNegativeId_shouldReturnError() {
        Result result = fileService.viewFile(-1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }
}
