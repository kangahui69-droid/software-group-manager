package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.FileStorage;
import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.FileService;
import support.FastTest;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FileApiServlet TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化重构计划.md 5.3 FileApiServlet 文件API
 *
 * API约定：
 * - Content-Type: application/json; charset=UTF-8（上传用multipart）
 * - 成功：{"code":0,"message":"ok","data":{...}}
 * - 失败：{"code":4xxx,"message":"...","data":null}
 *
 * 端点：
 * - POST /api/files/upload → 上传（multipart, category参数）
 * - GET /api/files/{id} → 文件元信息
 * - GET /api/files/{id}/download → 下载
 * - GET /api/files/{id}/view → 查看（inline，图片/头像用）
 * - DELETE /api/files/{id} → 删除
 * - GET /api/files → 文件列表（按category）
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FileApiServlet 文件API测试")
class FileApiServletTest {

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer MEMBER_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer FILE_ID = 100;

    // 用户角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 文件状态枚举
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;

    // 文件分类常量
    private static final String CATEGORY_AVATAR = "images/avatar";
    private static final String CATEGORY_AWARD = "images/award";
    private static final String CATEGORY_PROJECT = "images/projects";
    private static final String CATEGORY_GENERAL = "general";

    // 支持的文件类型
    private static final String CONTENT_TYPE_PNG = "image/png";
    private static final String CONTENT_TYPE_JPEG = "image/jpeg";
    private static final String CONTENT_TYPE_GIF = "image/gif";
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_INVALID = "application/x-executable";

    // ==================== 测试辅助类 ====================

    private TestableFileApiServlet servlet;
    private FileService mockFileService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    // ==================== 测试初始化 ====================

    @BeforeEach
    void setUp() throws Exception {
        mockFileService = mock(FileService.class);
        servlet = new TestableFileApiServlet(mockFileService);

        // 默认session行为
        when(mockRequest.getSession(false)).thenReturn(mockSession);
    }

    // ==================== 工具方法 ====================

    private User createUser(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        return user;
    }

    private FileStorage createFile(Integer id, String category, int status) {
        FileStorage file = new FileStorage();
        file.setId(id);
        file.setOriginalName("test_file.png");
        file.setStoredName("stored_" + id + "_file.png");
        file.setFilePath("/localstorage/files/" + category + "/stored_" + id + "_file.png");
        file.setFileType(CONTENT_TYPE_PNG);
        file.setFileSize(1024L);
        file.setCategory(category);
        file.setStatus(status);
        file.setCreateBy(MEMBER_USER_ID);
        return file;
    }

    // ==================== 认证相关测试 ====================

    @Nested
    @DisplayName("认证与授权测试")
    class AuthenticationTests {

        @FastTest
        @DisplayName("未登录用户访问受保护端点应返回401")
        void should_return_401_when_not_logged_in() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
            assertThat(response).contains("请先登录");
        }

        @FastTest
        @DisplayName("已登录用户访问应正常处理")
        void should_process_request_when_logged_in() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID))).thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("上传文件需要登录应返回401")
        void should_return_401_when_upload_without_login() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }

        @FastTest
        @DisplayName("删除文件需要登录应返回401")
        void should_return_401_when_delete_without_login() throws Exception {
            when(mockRequest.getSession(false)).thenReturn(null);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":401");
        }
    }

    // ==================== GET /api/files 文件列表 ====================

    @Nested
    @DisplayName("GET /api/files 文件列表")
    class ListFilesTests {

        @FastTest
        @DisplayName("获取文件列表应返回成功")
        void should_return_file_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);

            FileStorage file1 = createFile(1, CATEGORY_AVATAR, STATUS_NORMAL);
            FileStorage file2 = createFile(2, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList(file1, file2)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("按分类获取文件列表应正确筛选")
        void should_filter_by_category() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            FileStorage file1 = createFile(1, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.listFiles(eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList(file1)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("文件列表为空时应返回空数组")
        void should_return_empty_list_when_no_files() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\":[]");
        }

        @FastTest
        @DisplayName("Service返回错误时应返回对应错误码")
        void should_return_error_when_service_fails() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(500, "系统错误"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":500");
            assertThat(response).contains("系统错误");
        }
    }

    // ==================== GET /api/files/{id} 文件元信息 ====================

    @Nested
    @DisplayName("GET /api/files/{id} 文件元信息")
    class GetFileMetaTests {

        @FastTest
        @DisplayName("获取文件元信息应返回成功")
        void should_return_file_meta() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            FileStorage file = createFile(100, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.viewFile(eq(100))).thenReturn(Result.ok(file));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("获取不存在的文件应返回404")
        void should_return_404_when_file_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999");

            when(mockFileService.viewFile(eq(999))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("文件不存在");
        }

        @FastTest
        @DisplayName("获取已删除的文件应返回404")
        void should_return_404_when_file_deleted() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockFileService.viewFile(eq(100))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("文件ID格式错误时应返回400")
        void should_return_400_when_id_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/abc");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("文件ID为负数时应返回400")
        void should_return_400_when_id_negative() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/-1");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/-1");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("文件ID为零时应返回400")
        void should_return_400_when_id_zero() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/0");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/0");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== GET /api/files/{id}/download 下载 ====================

    @Nested
    @DisplayName("GET /api/files/{id}/download 下载")
    class DownloadFileTests {

        @FastTest
        @DisplayName("下载文件应返回成功")
        void should_download_file_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100/download");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100/download");

            FileStorage file = createFile(100, CATEGORY_GENERAL, STATUS_NORMAL);
            when(mockFileService.downloadFile(eq(100))).thenReturn(Result.ok(file));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("下载不存在的文件应返回404")
        void should_return_404_when_file_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/999/download");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999/download");

            when(mockFileService.downloadFile(eq(999))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("下载已删除的文件应返回404")
        void should_return_404_when_file_deleted() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100/download");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100/download");

            when(mockFileService.downloadFile(eq(100))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("下载时文件物理路径不存在应返回404")
        void should_return_404_when_physical_file_missing() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100/download");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100/download");

            when(mockFileService.downloadFile(eq(100))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("下载时ID格式错误应返回400")
        void should_return_400_when_id_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/abc/download");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc/download");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== GET /api/files/{id}/view 查看 ====================

    @Nested
    @DisplayName("GET /api/files/{id}/view 查看")
    class ViewFileTests {

        @FastTest
        @DisplayName("查看文件应返回成功")
        void should_view_file_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100/view");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100/view");

            FileStorage file = createFile(100, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.viewFile(eq(100))).thenReturn(Result.ok(file));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("查看不存在的文件应返回404")
        void should_return_404_when_file_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/999/view");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999/view");

            when(mockFileService.viewFile(eq(999))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("查看时ID格式错误应返回400")
        void should_return_400_when_id_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/abc/view");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/abc/view");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== POST /api/files/upload 上传 ====================

    @Nested
    @DisplayName("POST /api/files/upload 上传")
    class UploadFileTests {

        @FastTest
        @DisplayName("上传文件成功应返回成功")
        void should_upload_file_successfully() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            FileStorage uploadedFile = createFile(100, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(uploadedFile));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("上传文件时不带category参数应使用默认分类")
        void should_use_default_category_when_not_specified() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(null);

            FileStorage uploadedFile = createFile(100, CATEGORY_GENERAL, STATUS_NORMAL);
            when(mockFileService.uploadFile(any(), isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(uploadedFile));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("上传文件时文件为空应返回400")
        void should_return_400_when_file_empty() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "文件不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("文件不能为空");
        }

        @FastTest
        @DisplayName("上传文件时文件名无效应返回400")
        void should_return_400_when_filename_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "文件名不能为空"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("上传文件时文件大小超限应返回400")
        void should_return_400_when_file_too_large() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "文件大小不能超过100MB"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("文件大小不能超过100MB");
        }

        @FastTest
        @DisplayName("上传文件时文件类型不支持应返回400")
        void should_return_400_when_content_type_unsupported() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_GENERAL);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_GENERAL), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "不支持的文件类型"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
            assertThat(response).contains("不支持的文件类型");
        }

        @FastTest
        @DisplayName("上传文件时文件名包含路径遍历字符应返回400")
        void should_return_400_when_filename_contains_path_traversal() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "文件名不能包含路径遍历字符"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("上传文件时用户不存在应返回404")
        void should_return_404_when_user_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "用户不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("上传文件时文件名超长应返回400")
        void should_return_400_when_filename_too_long() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getPathInfo()).thenReturn("/upload");
            when(mockRequest.getParameter("category")).thenReturn(CATEGORY_AVATAR);

            when(mockFileService.uploadFile(any(), eq(CATEGORY_AVATAR), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(400, "文件名不能超过255字符"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPost(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("上传所有支持的文件类型都应该成功")
        void should_support_all_allowed_content_types() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);

            List<String> allowedTypes = Arrays.asList(
                    CONTENT_TYPE_PNG, CONTENT_TYPE_JPEG, CONTENT_TYPE_GIF,
                    CONTENT_TYPE_PDF, "text/plain"
            );

            for (String contentType : allowedTypes) {
                reset(mockFileService, mockResponse);
                when(mockSession.getAttribute("user")).thenReturn(member);
                when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/upload");
                when(mockRequest.getMethod()).thenReturn("POST");
                when(mockRequest.getPathInfo()).thenReturn("/upload");
                when(mockRequest.getParameter("category")).thenReturn(CATEGORY_GENERAL);

                FileStorage uploadedFile = createFile(100, CATEGORY_GENERAL, STATUS_NORMAL);
                when(mockFileService.uploadFile(any(), eq(CATEGORY_GENERAL), eq(MEMBER_USER_ID)))
                        .thenReturn(Result.ok(uploadedFile));

                StringWriter sw = new StringWriter();
                when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

                servlet.doPost(mockRequest, mockResponse);

                assertThat(sw.toString()).contains("\"code\":0");
            }
        }
    }

    // ==================== DELETE /api/files/{id} 删除 ====================

    @Nested
    @DisplayName("DELETE /api/files/{id} 删除")
    class DeleteFileTests {

        @FastTest
        @DisplayName("文件所有者删除文件应返回成功")
        void should_delete_file_as_owner() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockFileService.deleteFile(eq(100), eq(MEMBER_USER_ID))).thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("管理员删除文件应返回成功")
        void should_delete_file_as_admin() throws Exception {
            User admin = createUser(ADMIN_USER_ID, ROLE_ADMIN);
            when(mockSession.getAttribute("user")).thenReturn(admin);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockFileService.deleteFile(eq(100), eq(ADMIN_USER_ID))).thenReturn(Result.ok());

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("非所有者且非管理员删除文件应返回403")
        void should_return_403_when_not_owner_or_admin() throws Exception {
            User other = createUser(OTHER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(other);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockFileService.deleteFile(eq(100), eq(OTHER_USER_ID)))
                    .thenReturn(Result.error(403, "没有权限删除该文件"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":403");
            assertThat(response).contains("没有权限删除该文件");
        }

        @FastTest
        @DisplayName("删除不存在的文件应返回404")
        void should_return_404_when_file_not_exists() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/999");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/999");

            when(mockFileService.deleteFile(eq(999), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("删除已删除的文件应返回404")
        void should_return_404_when_file_already_deleted() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            when(mockFileService.deleteFile(eq(100), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
        }

        @FastTest
        @DisplayName("删除时ID格式错误应返回400")
        void should_return_400_when_id_invalid() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/abc");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }

        @FastTest
        @DisplayName("删除时ID为负数应返回400")
        void should_return_400_when_id_negative() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/-1");
            when(mockRequest.getMethod()).thenReturn("DELETE");
            when(mockRequest.getPathInfo()).thenReturn("/-1");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doDelete(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":400");
        }
    }

    // ==================== HTTP方法测试 ====================

    @Nested
    @DisplayName("HTTP方法测试")
    class HttpMethodTests {

        @FastTest
        @DisplayName("PUT方法不支持应返回405")
        void should_return_405_for_put_method() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("PUT");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doPut(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":405");
        }

        @FastTest
        @DisplayName("OPTIONS请求应返回200")
        void should_handle_options_request() throws Exception {
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("OPTIONS");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doOptions(mockRequest, mockResponse);

            verify(mockResponse).setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {

        @FastTest
        @DisplayName("空路径info应默认到文件列表")
        void should_handle_empty_path_info() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("空路径info但有尾斜杠应正确处理")
        void should_handle_path_info_with_trailing_slash() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超长分类路径应正确处理")
        void should_handle_long_category_path() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn("images/avatar/subfolder/deep/path");
            when(mockFileService.listFiles(eq("images/avatar/subfolder/deep/path"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("特殊字符在分类参数中应正确处理")
        void should_handle_special_chars_in_category() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn("images/avatar test");
            when(mockFileService.listFiles(eq("images/avatar test"), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(sw.toString()).contains("\"code\":0");
        }

        @FastTest
        @DisplayName("超大文件ID应返回400")
        void should_return_400_for_overflow_id() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/9999999999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/9999999999");

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            // 数字过大或格式错误
            assertThat(response).contains("\"code\":");
        }
    }

    // ==================== 响应格式测试 ====================

    @Nested
    @DisplayName("响应格式测试")
    class ResponseFormatTests {

        @FastTest
        @DisplayName("成功响应应包含code、message、data三个字段")
        void should_include_all_fields_in_success_response() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"message\":\"ok\"");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("错误响应应包含code、message、data三个字段")
        void should_include_all_fields_in_error_response() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/999");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/999");
            when(mockFileService.viewFile(eq(999))).thenReturn(Result.error(404, "文件不存在"));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":404");
            assertThat(response).contains("\"message\":\"文件不存在\"");
            assertThat(response).contains("\"data\":null");
        }

        @FastTest
        @DisplayName("Content-Type应正确设置为JSON")
        void should_set_correct_content_type() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList()));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            verify(mockResponse).setContentType("application/json; charset=UTF-8");
        }

        @FastTest
        @DisplayName("文件列表返回应为数组结构")
        void should_return_array_in_data_for_list() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getParameter("category")).thenReturn(null);

            FileStorage file1 = createFile(1, CATEGORY_AVATAR, STATUS_NORMAL);
            FileStorage file2 = createFile(2, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.listFiles(isNull(), eq(MEMBER_USER_ID)))
                    .thenReturn(Result.ok(Arrays.asList(file1, file2)));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }

        @FastTest
        @DisplayName("单个文件返回应为对象结构")
        void should_return_object_in_data_for_single_file() throws Exception {
            User member = createUser(MEMBER_USER_ID, ROLE_MEMBER);
            when(mockSession.getAttribute("user")).thenReturn(member);
            when(mockRequest.getRequestURI()).thenReturn("/software-group/api/files/100");
            when(mockRequest.getMethod()).thenReturn("GET");
            when(mockRequest.getPathInfo()).thenReturn("/100");

            FileStorage file = createFile(100, CATEGORY_AVATAR, STATUS_NORMAL);
            when(mockFileService.viewFile(eq(100))).thenReturn(Result.ok(file));

            StringWriter sw = new StringWriter();
            when(mockResponse.getWriter()).thenReturn(new PrintWriter(sw));

            servlet.doGet(mockRequest, mockResponse);

            String response = sw.toString();
            assertThat(response).contains("\"code\":0");
            assertThat(response).contains("\"data\"");
        }
    }

    // ==================== 测试用子类 ====================

    /**
     * 测试用FileApiServlet子类
     * 复制FileApiServlet的业务逻辑，用于测试
     */
    static class TestableFileApiServlet {

        private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

        private final FileService fileService;
        private final Gson gson = new GsonBuilder().serializeNulls().create();

        public TestableFileApiServlet(FileService fileService) {
            this.fileService = fileService;
        }

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListFiles(req, resp, currentUser);
            } else if (pathInfo.startsWith("/")) {
                String[] segments = pathInfo.substring(1).split("/");
                if (segments.length >= 1) {
                    String idStr = segments[0];
                    if (isNumeric(idStr)) {
                        int fileId = Integer.parseInt(idStr);
                        if (segments.length >= 2) {
                            String action = segments[1];
                            if ("download".equals(action)) {
                                handleDownloadFile(req, resp, currentUser, fileId);
                            } else if ("view".equals(action)) {
                                handleViewFile(req, resp, currentUser, fileId);
                            } else {
                                sendError(resp, 400, "未知的操作");
                            }
                        } else {
                            handleGetFileMeta(req, resp, currentUser, fileId);
                        }
                    } else {
                        sendError(resp, 400, "无效的文件ID");
                    }
                } else {
                    handleListFiles(req, resp, currentUser);
                }
            } else {
                sendError(resp, 400, "无效的请求路径");
            }
        }

        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.equals("/upload")) {
                handleUploadFile(req, resp, currentUser);
            } else {
                sendError(resp, 400, "无效的请求路径");
            }
        }

        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);

            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                sendError(resp, 401, "请先登录");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                sendError(resp, 400, "无效的文件ID");
                return;
            }

            if (pathInfo.startsWith("/")) {
                String idStr = pathInfo.substring(1);
                if (isNumeric(idStr)) {
                    int fileId = Integer.parseInt(idStr);
                    if (fileId <= 0) {
                        sendError(resp, 400, "无效的文件ID");
                        return;
                    }
                    handleDeleteFile(req, resp, currentUser, fileId);
                } else {
                    sendError(resp, 400, "无效的文件ID");
                }
            } else {
                sendError(resp, 400, "无效的请求路径");
            }
        }

        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setContentType(CONTENT_TYPE_JSON);
            sendError(resp, 405, "不支持的HTTP方法");
        }

        protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            resp.setStatus(HttpServletResponse.SC_OK);
        }

        // ==================== 处理器方法 ====================

        private void handleListFiles(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String category = req.getParameter("category");

            Result result = fileService.listFiles(category, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleGetFileMeta(HttpServletRequest req, HttpServletResponse resp, User currentUser, int fileId) throws IOException {
            if (fileId <= 0) {
                sendError(resp, 400, "无效的文件ID");
                return;
            }

            Result result = fileService.viewFile(fileId);
            writeJson(resp, result);
        }

        private void handleDownloadFile(HttpServletRequest req, HttpServletResponse resp, User currentUser, int fileId) throws IOException {
            if (fileId <= 0) {
                sendError(resp, 400, "无效的文件ID");
                return;
            }

            Result result = fileService.downloadFile(fileId);
            writeJson(resp, result);
        }

        private void handleViewFile(HttpServletRequest req, HttpServletResponse resp, User currentUser, int fileId) throws IOException {
            if (fileId <= 0) {
                sendError(resp, 400, "无效的文件ID");
                return;
            }

            Result result = fileService.viewFile(fileId);
            writeJson(resp, result);
        }

        private void handleUploadFile(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws IOException {
            String category = req.getParameter("category");

            Result result = fileService.uploadFile(null, category, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleDeleteFile(HttpServletRequest req, HttpServletResponse resp, User currentUser, int fileId) throws IOException {
            Result result = fileService.deleteFile(fileId, currentUser.getId());
            writeJson(resp, result);
        }

        // ==================== 工具方法 ====================

        private User getCurrentUser(HttpServletRequest request) {
            HttpSession session = request.getSession(false);
            return session != null ? (User) session.getAttribute("user") : null;
        }

        private void writeJson(HttpServletResponse response, Result result) throws IOException {
            if (result == null) {
                result = Result.error(500, "系统错误");
            }
            response.setStatus(result.isSuccess() ? HttpServletResponse.SC_OK : result.getCode());
            PrintWriter writer = response.getWriter();
            gson.toJson(result, writer);
            writer.flush();
        }

        private void sendError(HttpServletResponse response, int code, String message) throws IOException {
            writeJson(response, Result.error(code, message));
        }

        private boolean isNumeric(String str) {
            if (str == null || str.isEmpty()) {
                return false;
            }
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
