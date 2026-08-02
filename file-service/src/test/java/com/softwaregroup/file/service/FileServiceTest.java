package com.softwaregroup.file.service;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.file.model.entity.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * FileService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private com.softwaregroup.file.dao.FileStorageDAO fileStorageDAO;

    @InjectMocks
    private FileService fileService;

    @Test
    void uploadFile_withNullFile_shouldReturnError() {
        Result result = fileService.uploadFile(null, "test", 1);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("不能为空");
    }

    @Test
    void uploadFile_withEmptyFile_shouldReturnError() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", new byte[0]);

        Result result = fileService.uploadFile(emptyFile, "test", 1);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("不能为空");
    }

    @Test
    void uploadFile_withNullUserId_shouldReturnError() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        Result result = fileService.uploadFile(file, "test", null);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("用户ID");
    }

    @Test
    void uploadFile_withFilenameContainingPathTraversal_shouldReturnError() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../../etc/passwd", "text/plain", "content".getBytes());

        Result result = fileService.uploadFile(file, "test", 1);
        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).contains("路径遍历");
    }

    @Test
    void viewFile_withInvalidId_shouldReturnError() {
        Result result = fileService.viewFile(null);
        assertThat(result.getCode()).isEqualTo(400);

        result = fileService.viewFile(0);
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void viewFile_withNonExistentId_shouldReturnNotFound() {
        when(fileStorageDAO.findById(999)).thenReturn(null);

        Result result = fileService.viewFile(999);
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void viewFile_withNormalFile_shouldReturnFileInfo() {
        FileStorage file = new FileStorage();
        file.setId(1);
        file.setOriginalName("test.txt");
        file.setStatus(1);
        when(fileStorageDAO.findById(1)).thenReturn(file);

        Result result = fileService.viewFile(1);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isInstanceOf(FileStorage.class);
    }

    @Test
    void viewFile_withDeletedFile_shouldReturnNotFound() {
        FileStorage file = new FileStorage();
        file.setId(1);
        file.setStatus(0); // 已删除
        when(fileStorageDAO.findById(1)).thenReturn(file);

        Result result = fileService.viewFile(1);
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void deleteFile_withInvalidId_shouldReturnError() {
        Result result = fileService.deleteFile(null, 1);
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void deleteFile_withNullUserId_shouldReturnError() {
        Result result = fileService.deleteFile(1, null);
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void deleteFile_withNonExistentFile_shouldReturnNotFound() {
        when(fileStorageDAO.findById(999)).thenReturn(null);

        Result result = fileService.deleteFile(999, 1);
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void listFiles_withNullUserId_shouldReturnError() {
        Result result = fileService.listFiles(null, null);
        assertThat(result.getCode()).isEqualTo(400);
    }
}
