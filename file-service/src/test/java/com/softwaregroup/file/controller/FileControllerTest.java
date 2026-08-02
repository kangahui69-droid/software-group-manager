package com.softwaregroup.file.controller;

import com.softwaregroup.common.util.Result;
import com.softwaregroup.file.model.entity.FileStorage;
import com.softwaregroup.file.service.FileService;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FileController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
    }

    @Test
    void uploadFile_withValidRequest_shouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        FileStorage fileStorage = new FileStorage();
        fileStorage.setId(1);
        fileStorage.setOriginalName("test.txt");

        when(fileService.uploadFile(any(), eq("general"), eq(1)))
                .thenReturn(Result.ok(fileStorage));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "general")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void uploadFile_withoutUserId_shouldReturnUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("category", "general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void viewFile_withValidId_shouldReturnFileInfo() throws Exception {
        FileStorage file = new FileStorage();
        file.setId(1);
        file.setOriginalName("test.txt");
        file.setStatus(1);

        when(fileService.viewFile(1)).thenReturn(Result.ok(file));

        mockMvc.perform(get("/api/files/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.originalName").value("test.txt"));
    }

    @Test
    void viewFile_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(fileService.viewFile(999)).thenReturn(Result.error(404, "文件不存在"));

        mockMvc.perform(get("/api/files/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void listFiles_withUserId_shouldReturnFileList() throws Exception {
        when(fileService.listFiles(isNull(), eq(1)))
                .thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/files")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listFiles_withoutUserId_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void health_shouldReturnUp() throws Exception {
        mockMvc.perform(get("/api/files/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
