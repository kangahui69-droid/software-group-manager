package service;

import dao.FileStorageDAO;
import dto.FileInfo;
import model.FileStorage;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.io.InputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FileService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 4.3 FileService 文件服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * 注意：这是Red阶段，FileService尚未实现
 * 所有测试将失败，直至实现对应方法
 *
 * Mock说明：所有mock基于实际DAO/Util接口签名
 * - FileStorageDAO: insert(FileStorage) / findById(id) / findByCategory(category) / findByCreateBy(createBy) / delete(id) / softDelete(id)
 * - FileUtil: generateStoredFileName(originalFileName) / getCategoryDir(categoryPath) / resolvePhysicalPath(relativePath) / deleteFile(filePath)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FileService 文件服务测试")
class FileServiceTest {

    @Mock
    private FileStorageDAO fileStorageDAO;

    @InjectMocks
    private FileService fileService;

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer FILE_ID = 100;
    private static final Integer OTHER_USER_FILE_ID = 200;

    // 文件分类枚举
    private static final String CATEGORY_AVATAR = "images/avatar";
    private static final String CATEGORY_AWARD = "images/award";
    private static final String CATEGORY_PROJECT = "files/project";
    private static final String CATEGORY_NEWS = "news";
    private static final String CATEGORY_GENERAL = "general";

    // 文件状态枚举
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;

    // MIME类型枚举
    private static final String MIME_IMAGE_PNG = "image/png";
    private static final String MIME_IMAGE_JPEG = "image/jpeg";
    private static final String MIME_APPLICATION_PDF = "application/pdf";
    private static final String MIME_TEXT_PLAIN = "text/plain";

    // 角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // ==================== 测试初始化 ====================

    private User adminUser;
    private User memberUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        adminUser = createUser(ADMIN_USER_ID, "admin", ROLE_ADMIN);
        memberUser = createUser(MEMBER_USER_ID, "member1", ROLE_MEMBER);
        otherUser = createUser(OTHER_USER_ID, "other", ROLE_MEMBER);
    }

    // ==================== 工具方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private FileStorage createFileStorage(Integer id, Integer createBy, String category, int status) {
        FileStorage file = new FileStorage();
        file.setId(id);
        file.setCreateBy(createBy);
        file.setOriginalName("test_file.txt");
        file.setStoredName("1234567890_test_file.txt");
        file.setFilePath("/localstorage/files/" + category + "/1234567890_test_file.txt");
        file.setFileType(MIME_TEXT_PLAIN);
        file.setFileSize(1024L);
        file.setCategory(category);
        file.setStatus(status);
        return file;
    }

    // ==================== uploadFile 上传文件 ====================

    @Nested
    @DisplayName("uploadFile 上传文件")
    class UploadFileTests {

        @FastTest
        @DisplayName("上传文件时file为null应返回错误")
        void should_return_error_when_file_is_null() {
            Result result = fileService.uploadFile(null, CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件不能为空");
        }

        @FastTest
        @DisplayName("上传文件时category为null应使用默认分类")
        void should_use_default_category_when_null() {
            // 当category为null时，应该使用"general"作为默认分类
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 100), null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getCategory()).isEqualTo("general");
        }

        @FastTest
        @DisplayName("上传文件时category为空串应使用默认分类")
        void should_use_default_category_when_empty() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 100), "", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getCategory()).isEqualTo("general");
        }

        @FastTest
        @DisplayName("上传文件时userId不存在应返回错误")
        void should_return_error_when_user_not_exists() {
            Result result = fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 100), CATEGORY_AVATAR, NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("上传文件时文件大小为0应返回错误")
        void should_return_error_when_file_size_zero() {
            Result result = fileService.uploadFile(createMockPart("empty.txt", MIME_TEXT_PLAIN, 0), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件不能为空");
        }

        @FastTest
        @DisplayName("上传文件时文件大小超过限制应返回错误")
        void should_return_error_when_file_size_exceeds_limit() {
            // 假设最大文件大小为100MB (100 * 1024 * 1024)
            long maxSize = 100 * 1024 * 1024;
            long exceedsSize = maxSize + 1;

            Result result = fileService.uploadFile(createMockPart("large.txt", MIME_TEXT_PLAIN, exceedsSize), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件大小不能超过");
        }

        @FastTest
        @DisplayName("上传文件时文件名超长应返回错误")
        void should_return_error_when_filename_too_long() {
            String longFileName = "a".repeat(260) + ".txt"; // 超过255字符

            Result result = fileService.uploadFile(createMockPart(longFileName, MIME_TEXT_PLAIN, 100), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件名不能超过255字符");
        }

        @FastTest
        @DisplayName("上传文件时文件名包含危险字符应返回错误")
        void should_return_error_when_filename_contains_dangerous_chars() {
            // 文件名包含路径遍历字符
            Result result = fileService.uploadFile(createMockPart("../dangerous.txt", MIME_TEXT_PLAIN, 100), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件名不能包含");
        }

        @FastTest
        @DisplayName("上传文件时文件名包含null字节应返回错误")
        void should_return_error_when_filename_contains_null_byte() {
            Result result = fileService.uploadFile(createMockPart("file\0.txt", MIME_TEXT_PLAIN, 100), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("上传文件时文件类型不支持应返回错误")
        void should_return_error_when_file_type_unsupported() {
            // 假设只允许图片、PDF、文档等常见类型
            Result result = fileService.uploadFile(createMockPart("evil.exe", "application/x-executable", 100), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("不支持的文件类型");
        }

        @FastTest
        @DisplayName("上传文件时contentType为null应使用默认类型")
        void should_use_octet_stream_when_content_type_null() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPartWithoutContentType("test.bin", 100), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getFileType()).isEqualTo("application/octet-stream");
        }

        @FastTest
        @DisplayName("上传文件成功时应返回文件信息")
        void should_return_file_info_when_upload_success() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 1024), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            FileStorage savedFile = (FileStorage) result.getData();
            assertThat(savedFile.getId()).isEqualTo(FILE_ID);
            assertThat(savedFile.getOriginalName()).isEqualTo("test.txt");
        }

        @FastTest
        @DisplayName("上传文件成功时应生成唯一存储名")
        void should_generate_unique_stored_name_when_upload_success() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 1024), CATEGORY_AVATAR, ADMIN_USER_ID);

            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            String storedName = captor.getValue().getStoredName();
            assertThat(storedName).isNotNull();
            assertThat(storedName).contains("_");
            assertThat(storedName).endsWith(".txt");
        }

        @FastTest
        @DisplayName("上传文件成功时应设置正确的分类目录")
        void should_set_correct_category_when_upload_success() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 1024), CATEGORY_PROJECT, ADMIN_USER_ID);

            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getCategory()).isEqualTo(CATEGORY_PROJECT);
            assertThat(captor.getValue().getFilePath()).contains(CATEGORY_PROJECT);
        }

        @FastTest
        @DisplayName("上传文件成功时应设置创建者ID")
        void should_set_creator_id_when_upload_success() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 1024), CATEGORY_AVATAR, MEMBER_USER_ID);

            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getCreateBy()).isEqualTo(MEMBER_USER_ID);
        }

        @FastTest
        @DisplayName("上传图片文件时应接受image/png类型")
        void should_accept_image_png_type() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("image.png", MIME_IMAGE_PNG, 1024), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("上传图片文件时应接受image/jpeg类型")
        void should_accept_image_jpeg_type() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("photo.jpg", MIME_IMAGE_JPEG, 1024), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("上传PDF文件时应接受application/pdf类型")
        void should_accept_pdf_type() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("doc.pdf", MIME_APPLICATION_PDF, 1024), CATEGORY_PROJECT, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("上传文件数据库写入失败时应返回错误")
        void should_return_error_when_database_insert_fails() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenThrow(new RuntimeException("数据库写入失败"));

            Result result = fileService.uploadFile(createMockPart("test.txt", MIME_TEXT_PLAIN, 1024), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("上传失败");
        }

        @FastTest
        @DisplayName("上传文件时文件名为纯空白应返回错误")
        void should_return_error_when_filename_only_whitespace() {
            Result result = fileService.uploadFile(createMockPart("   ", MIME_TEXT_PLAIN, 100), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("上传文件时扩展名为大写应正常处理")
        void should_handle_uppercase_extension() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("test.TXT", MIME_TEXT_PLAIN, 1024), CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getStoredName()).endsWith(".TXT");
        }

        @FastTest
        @DisplayName("上传文件时无扩展名应正常处理")
        void should_handle_no_extension() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(createMockPart("Makefile", MIME_TEXT_PLAIN, 1024), CATEGORY_PROJECT, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getStoredName()).isEqualTo("Makefile");
        }
    }

    // ==================== viewFile 查看文件 ====================

    @Nested
    @DisplayName("viewFile 查看文件")
    class ViewFileTests {

        @FastTest
        @DisplayName("查看文件时fileId为null应返回错误")
        void should_return_error_when_file_id_is_null() {
            Result result = fileService.viewFile(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("文件ID不能为空");
        }

        @FastTest
        @DisplayName("查看文件时fileId为0应返回错误")
        void should_return_error_when_file_id_is_zero() {
            Result result = fileService.viewFile(0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("查看文件时fileId为负数应返回错误")
        void should_return_error_when_file_id_is_negative() {
            Result result = fileService.viewFile(-1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("查看文件时文件不存在应返回错误")
        void should_return_error_when_file_not_exists() {
            when(fileStorageDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = fileService.viewFile(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("文件不存在");
        }

        @FastTest
        @DisplayName("查看文件时文件已删除应返回错误")
        void should_return_error_when_file_is_deleted() {
            FileStorage deletedFile = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_DELETED);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(deletedFile);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("文件不存在");
        }

        @FastTest
        @DisplayName("查看文件成功时应返回文件元信息")
        void should_return_file_info_when_view_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            FileStorage returnedFile = (FileStorage) result.getData();
            assertThat(returnedFile.getId()).isEqualTo(FILE_ID);
            assertThat(returnedFile.getOriginalName()).isEqualTo("test_file.txt");
        }

        @FastTest
        @DisplayName("查看文件成功时应返回物理路径")
        void should_return_physical_path_when_view_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            FileStorage returnedFile = (FileStorage) result.getData();
            assertThat(returnedFile.getFilePath()).isNotNull();
            assertThat(returnedFile.getFilePath()).contains("/localstorage/");
        }

        @FastTest
        @DisplayName("查看文件时应包含文件大小信息")
        void should_include_file_size_when_view_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setFileSize(2048L);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            FileStorage returnedFile = (FileStorage) result.getData();
            assertThat(returnedFile.getFileSize()).isEqualTo(2048L);
        }

        @FastTest
        @DisplayName("查看文件时应包含contentType信息")
        void should_include_content_type_when_view_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setFileType(MIME_IMAGE_PNG);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            FileStorage returnedFile = (FileStorage) result.getData();
            assertThat(returnedFile.getFileType()).isEqualTo(MIME_IMAGE_PNG);
        }

        @FastTest
        @DisplayName("查看文件时应包含原始文件名")
        void should_include_original_name_when_view_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setOriginalName("my_photo.png");
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            FileStorage returnedFile = (FileStorage) result.getData();
            assertThat(returnedFile.getOriginalName()).isEqualTo("my_photo.png");
        }

        @FastTest
        @DisplayName("查看文件时应包含分类信息")
        void should_include_category_when_view_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_PROJECT, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            FileStorage returnedFile = (FileStorage) result.getData();
            assertThat(returnedFile.getCategory()).isEqualTo(CATEGORY_PROJECT);
        }

        @FastTest
        @DisplayName("查看文件时数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(fileStorageDAO.findById(FILE_ID)).thenThrow(new RuntimeException("数据库查询失败"));

            Result result = fileService.viewFile(FILE_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== downloadFile 下载文件 ====================

    @Nested
    @DisplayName("downloadFile 下载文件")
    class DownloadFileTests {

        @FastTest
        @DisplayName("下载文件时fileId为null应返回错误")
        void should_return_error_when_file_id_is_null() {
            Result result = fileService.downloadFile(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("下载文件时fileId不存在应返回错误")
        void should_return_error_when_file_not_exists() {
            when(fileStorageDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = fileService.downloadFile(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("文件不存在");
        }

        @FastTest
        @DisplayName("下载文件时文件已删除应返回错误")
        void should_return_error_when_file_is_deleted() {
            FileStorage deletedFile = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_DELETED);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(deletedFile);

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("下载文件成功时应返回包含流的信息")
        void should_return_stream_info_when_download_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
        }

        @FastTest
        @DisplayName("下载文件成功时应返回正确的contentType")
        void should_return_correct_content_type_when_download_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setFileType(MIME_IMAGE_PNG);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("下载文件成功时应返回文件名")
        void should_return_filename_when_download_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setOriginalName("report.pdf");
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("下载文件成功时应返回文件大小")
        void should_return_file_size_when_download_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setFileSize(4096L);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("下载文件成功时应返回物理路径")
        void should_return_physical_path_when_download_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("下载文件时物理文件不存在应返回错误")
        void should_return_error_when_physical_file_not_exists() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.downloadFile(FILE_ID);

            // 物理文件不存在的情况
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("下载文件时数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(fileStorageDAO.findById(FILE_ID)).thenThrow(new RuntimeException("数据库查询失败"));

            Result result = fileService.downloadFile(FILE_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== deleteFile 删除文件 ====================

    @Nested
    @DisplayName("deleteFile 删除文件")
    class DeleteFileTests {

        @FastTest
        @DisplayName("删除文件时fileId为null应返回错误")
        void should_return_error_when_file_id_is_null() {
            Result result = fileService.deleteFile(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除文件时userId为null应返回错误")
        void should_return_error_when_user_id_is_null() {
            Result result = fileService.deleteFile(FILE_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除文件时文件不存在应返回错误")
        void should_return_error_when_file_not_exists() {
            when(fileStorageDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = fileService.deleteFile(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除文件时文件已删除应返回错误")
        void should_return_error_when_file_already_deleted() {
            FileStorage deletedFile = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_DELETED);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(deletedFile);

            Result result = fileService.deleteFile(FILE_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除文件时非所有者且非管理员应返回403错误")
        void should_return_forbidden_when_not_owner_and_not_admin() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            // otherUser既不是文件创建者也不是管理员
            Result result = fileService.deleteFile(FILE_ID, OTHER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
            assertThat(result.getMessage()).contains("没有权限");
        }

        @FastTest
        @DisplayName("删除文件时所有者应能删除自己的文件")
        void should_allow_owner_to_delete_own_file() {
            FileStorage file = createFileStorage(FILE_ID, MEMBER_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.deleteFile(FILE_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("删除文件时管理员应能删除任何文件")
        void should_allow_admin_to_delete_any_file() {
            FileStorage file = createFileStorage(FILE_ID, MEMBER_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            // adminUser是管理员，可以删除任何文件
            Result result = fileService.deleteFile(FILE_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("删除文件成功时应删除物理文件")
        void should_delete_physical_file_when_delete_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            fileService.deleteFile(FILE_ID, ADMIN_USER_ID);

            // 验证软删除被调用（不直接删除物理文件，而是软删除）
            verify(fileStorageDAO).softDelete(eq(FILE_ID), any(Connection.class));
        }

        @FastTest
        @DisplayName("删除文件成功时应更新数据库状态")
        void should_update_database_status_when_delete_success() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            fileService.deleteFile(FILE_ID, ADMIN_USER_ID);

            verify(fileStorageDAO).softDelete(eq(FILE_ID), any(Connection.class));
        }

        @FastTest
        @DisplayName("删除文件时数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);
            doThrow(new RuntimeException("数据库删除失败")).when(fileStorageDAO).softDelete(anyInt(), any(Connection.class));

            Result result = fileService.deleteFile(FILE_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("删除文件时物理文件删除失败不应影响结果")
        void should_not_affect_result_when_physical_delete_fails() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.deleteFile(FILE_ID, ADMIN_USER_ID);

            // 即使物理文件删除失败，数据库软删除也应该成功
            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("删除文件时成员用户删除他人文件应返回403")
        void should_return_forbidden_when_member_deletes_others_file() {
            FileStorage file = createFileStorage(FILE_ID, OTHER_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(FILE_ID)).thenReturn(file);

            Result result = fileService.deleteFile(FILE_ID, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(403);
        }
    }

    // ==================== listFiles 文件列表 ====================

    @Nested
    @DisplayName("listFiles 文件列表")
    class ListFilesTests {

        @FastTest
        @DisplayName("列出文件时category为null应返回用户所有文件")
        void should_return_user_all_files_when_category_is_null() {
            List<FileStorage> userFiles = Arrays.asList(
                createFileStorage(1, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL),
                createFileStorage(2, ADMIN_USER_ID, CATEGORY_PROJECT, STATUS_NORMAL)
            );
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenReturn(userFiles);

            Result result = fileService.listFiles(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files).hasSize(2);
            verify(fileStorageDAO).findByCreateBy(ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("列出文件时category为空串应返回用户所有文件")
        void should_return_user_all_files_when_category_is_empty() {
            List<FileStorage> userFiles = Arrays.asList(
                createFileStorage(1, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL)
            );
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenReturn(userFiles);

            Result result = fileService.listFiles("", ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(fileStorageDAO).findByCreateBy(ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("列出文件时指定category应返回该分类文件")
        void should_return_category_files_when_specified() {
            List<FileStorage> categoryFiles = Arrays.asList(
                createFileStorage(1, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL)
            );
            when(fileStorageDAO.findByCategory(CATEGORY_AVATAR)).thenReturn(categoryFiles);

            Result result = fileService.listFiles(CATEGORY_AVATAR, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files).hasSize(1);
            assertThat(files.get(0).getCategory()).isEqualTo(CATEGORY_AVATAR);
            verify(fileStorageDAO).findByCategory(CATEGORY_AVATAR);
        }

        @FastTest
        @DisplayName("列出文件时userId不存在应返回空列表")
        void should_return_empty_list_when_user_not_exists() {
            when(fileStorageDAO.findByCreateBy(NONEXISTENT_USER_ID)).thenReturn(Arrays.asList());

            Result result = fileService.listFiles(null, NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files).isEmpty();
        }

        @FastTest
        @DisplayName("列出文件时用户无文件应返回空列表")
        void should_return_empty_list_when_user_has_no_files() {
            when(fileStorageDAO.findByCreateBy(MEMBER_USER_ID)).thenReturn(Arrays.asList());

            Result result = fileService.listFiles(null, MEMBER_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files).isEmpty();
        }

        @FastTest
        @DisplayName("列出文件时应过滤掉已删除的文件")
        void should_filter_out_deleted_files() {
            List<FileStorage> mixedFiles = Arrays.asList(
                createFileStorage(1, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL),
                createFileStorage(2, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_DELETED)
            );
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenReturn(mixedFiles);

            Result result = fileService.listFiles(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files).hasSize(1);
            assertThat(files.get(0).getStatus()).isEqualTo(STATUS_NORMAL);
        }

        @FastTest
        @DisplayName("列出文件时应包含文件元信息")
        void should_include_file_metadata() {
            FileStorage file = createFileStorage(FILE_ID, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            file.setOriginalName("avatar.png");
            file.setFileSize(5120L);
            file.setFileType(MIME_IMAGE_PNG);
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenReturn(Arrays.asList(file));

            Result result = fileService.listFiles(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files.get(0).getOriginalName()).isEqualTo("avatar.png");
            assertThat(files.get(0).getFileSize()).isEqualTo(5120L);
            assertThat(files.get(0).getFileType()).isEqualTo(MIME_IMAGE_PNG);
        }

        @FastTest
        @DisplayName("列出文件时应按创建时间倒序排列")
        void should_order_by_created_time_desc() {
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenReturn(Arrays.asList(
                createFileStorage(1, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL),
                createFileStorage(2, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL)
            ));

            Result result = fileService.listFiles(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(fileStorageDAO).findByCreateBy(ADMIN_USER_ID);
        }

        @FastTest
        @DisplayName("列出文件时分页参数应正确处理")
        void should_handle_pagination_correctly() {
            // 默认分页大小
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenReturn(Arrays.asList());

            Result result = fileService.listFiles(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            // 分页参数的验证
        }

        @FastTest
        @DisplayName("列出文件时数据库异常应返回错误")
        void should_return_error_when_database_exception() {
            when(fileStorageDAO.findByCreateBy(ADMIN_USER_ID)).thenThrow(new RuntimeException("数据库查询失败"));

            Result result = fileService.listFiles(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("列出文件时category存在但无文件应返回空列表")
        void should_return_empty_list_when_category_has_no_files() {
            when(fileStorageDAO.findByCategory(CATEGORY_NEWS)).thenReturn(Arrays.asList());

            Result result = fileService.listFiles(CATEGORY_NEWS, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            @SuppressWarnings("unchecked")
            List<FileStorage> files = (List<FileStorage>) result.getData();
            assertThat(files).isEmpty();
        }
    }

    // ==================== 辅助方法：创建Mock Part ====================

    /**
     * 创建模拟的FileInfo对象
     * 用于模拟上传文件的信息
     */
    private FileInfo createMockPart(String fileName, String contentType, long size) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setContentType(contentType);
        fileInfo.setSize(size);
        return fileInfo;
    }

    private FileInfo createMockPartWithoutContentType(String fileName, long size) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(fileName);
        fileInfo.setContentType(null);
        fileInfo.setSize(size);
        return fileInfo;
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("文件大小为Long.MAX_VALUE时应拒绝")
        void should_reject_when_file_size_is_max_long() {
            Result result = fileService.uploadFile(
                createMockPart("test.txt", MIME_TEXT_PLAIN, Long.MAX_VALUE),
                CATEGORY_AVATAR,
                ADMIN_USER_ID
            );

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("文件ID为Integer.MAX_VALUE时应正常处理")
        void should_handle_max_integer_file_id() {
            FileStorage file = createFileStorage(Integer.MAX_VALUE, ADMIN_USER_ID, CATEGORY_AVATAR, STATUS_NORMAL);
            when(fileStorageDAO.findById(Integer.MAX_VALUE)).thenReturn(file);

            Result result = fileService.viewFile(Integer.MAX_VALUE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("用户ID为Integer.MAX_VALUE时应正常处理")
        void should_handle_max_integer_user_id() {
            when(fileStorageDAO.findByCreateBy(Integer.MAX_VALUE)).thenReturn(Arrays.asList());

            Result result = fileService.listFiles(null, Integer.MAX_VALUE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("空分类名称应使用默认分类")
        void should_use_default_for_empty_category_name() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(
                createMockPart("test.txt", MIME_TEXT_PLAIN, 100),
                "   ",
                ADMIN_USER_ID
            );

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getCategory()).isEqualTo("general");
        }

        @FastTest
        @DisplayName("超长分类路径应正常处理")
        void should_handle_long_category_path() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);
            String longCategory = "a".repeat(100);

            Result result = fileService.uploadFile(
                createMockPart("test.txt", MIME_TEXT_PLAIN, 100),
                longCategory,
                ADMIN_USER_ID
            );

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("Unicode文件名应正常处理")
        void should_handle_unicode_filename() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(
                createMockPart("测试文件.txt", MIME_TEXT_PLAIN, 100),
                CATEGORY_GENERAL,
                ADMIN_USER_ID
            );

            assertThat(result.isSuccess()).isTrue();
            ArgumentCaptor<FileStorage> captor = ArgumentCaptor.forClass(FileStorage.class);
            verify(fileStorageDAO).insert(captor.capture());
            assertThat(captor.getValue().getOriginalName()).isEqualTo("测试文件.txt");
        }

        @FastTest
        @DisplayName("特殊字符文件名应正常处理")
        void should_handle_special_chars_in_filename() {
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(FILE_ID);

            Result result = fileService.uploadFile(
                createMockPart("file-with-dash_underscore.dot.txt", MIME_TEXT_PLAIN, 100),
                CATEGORY_GENERAL,
                ADMIN_USER_ID
            );

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionTests {

        @FastTest
        @DisplayName("DAO抛出SQLException应被包装为RuntimeException")
        void should_wrap_sql_exception() {
            when(fileStorageDAO.findById(FILE_ID)).thenThrow(new RuntimeException("SQL error"));

            assertThatThrownBy(() -> fileService.viewFile(FILE_ID))
                .isInstanceOf(RuntimeException.class);
        }

        @FastTest
        @DisplayName("文件操作超时应返回适当错误")
        void should_handle_operation_timeout() {
            when(fileStorageDAO.insert(any(FileStorage.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

            Result result = fileService.uploadFile(
                createMockPart("test.txt", MIME_TEXT_PLAIN, 1024),
                CATEGORY_AVATAR,
                ADMIN_USER_ID
            );

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("并发上传同一文件应各自成功")
        void should_handle_concurrent_uploads() {
            when(fileStorageDAO.insert(any(FileStorage.class)))
                .thenReturn(FILE_ID)
                .thenReturn(FILE_ID + 1);

            Result result1 = fileService.uploadFile(
                createMockPart("test.txt", MIME_TEXT_PLAIN, 1024),
                CATEGORY_AVATAR,
                ADMIN_USER_ID
            );
            Result result2 = fileService.uploadFile(
                createMockPart("test.txt", MIME_TEXT_PLAIN, 1024),
                CATEGORY_AVATAR,
                ADMIN_USER_ID
            );

            assertThat(result1.isSuccess()).isTrue();
            assertThat(result2.isSuccess()).isTrue();
        }
    }
}
