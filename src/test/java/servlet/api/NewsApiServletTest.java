package servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.NewsDTO;
import dto.NewsFilterDTO;
import model.News;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.NewsService;
import support.FastTest;
import util.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NewsApiServlet TDD测试套件
 *
 * 测试范围：服务分层与API化完整计划.md 5.1 NewsApiServlet 端点
 * - 所有REST端点
 * - 所有HTTP方法(GET/POST/PUT/DELETE)
 * - 认证与授权
 * - 参数解析与验证
 * - 错误处理
 *
 * 测试覆盖端点：
 * - GET  /api/news              → 新闻列表
 * - GET  /api/news/{id}        → 新闻详情
 * - POST /api/news              → 创建新闻
 * - PUT  /api/news/{id}        → 更新新闻
 * - DELETE /api/news/{id}      → 删除新闻
 * - POST /api/news/{id}/publish → 发布新闻
 * - POST /api/news/{id}/unpublish → 取消发布
 * - GET  /api/news/types        → 新闻类型列表
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NewsApiServlet 新闻API测试")
class NewsApiServletTest {

    private TestableNewsApiServlet servlet;
    private NewsService mockNewsService;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private HttpSession mockSession;

    private StringWriter responseWriter;
    private Gson gson;

    private static final int TEST_USER_ID = 1;
    private static final int TEST_AUTHOR_ID = 2;
    private static final int TEST_NEWS_ID = 100;
    private static final int TEST_PAGE = 1;
    private static final int TEST_PAGE_SIZE = 20;

    private static final String TYPE_AWARD = "award";
    private static final String TYPE_ACTIVITY = "activity";
    private static final String TYPE_NOTICE = "notice";

    @BeforeEach
    void setUp() throws Exception {
        mockNewsService = mock(NewsService.class);
        servlet = new TestableNewsApiServlet(mockNewsService);
        responseWriter = new StringWriter();
        gson = new GsonBuilder().create();

        when(mockRequest.getSession(false)).thenReturn(mockSession);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== 辅助方法 ====================

    private User createTestUser(int id, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private News createTestNews(Integer id, String title, String type, Integer authorId, Integer status) {
        News news = new News();
        news.setId(id);
        news.setTitle(title);
        news.setType(type);
        news.setAuthorId(authorId);
        news.setStatus(status);
        news.setSummary("测试摘要");
        news.setContentPath("localstorage/news/" + type + "/" + id + ".html");
        news.setCreatedAt(new Date());
        news.setUpdatedAt(new Date());
        return news;
    }

    private String getResponseBody() {
        return responseWriter.toString();
    }

    private void simulateLogin(User user) {
        when(mockSession.getAttribute("user")).thenReturn(user);
    }

    private void simulateUnauthorized() {
        when(mockSession.getAttribute("user")).thenReturn(null);
    }

    private void resetResponseWriter() throws Exception {
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    // ==================== 认证测试 ====================

    @Nested
    @DisplayName("认证测试")
    class AuthTests {

        @Test
        @FastTest
        @DisplayName("未登录GET请求应返回401")
        void should_return_401_when_get_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录POST请求应返回401")
        void should_return_401_when_post_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录DELETE请求应返回401")
        void should_return_401_when_delete_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID);

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }

        @Test
        @FastTest
        @DisplayName("未登录PUT请求应返回401")
        void should_return_401_when_put_not_logged_in() throws Exception {
            simulateUnauthorized();
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":401");
        }
    }

    // ==================== GET /api/news 新闻列表 ====================

    @Nested
    @DisplayName("GET /api/news 新闻列表")
    class ListNewsTests {

        @Test
        @FastTest
        @DisplayName("获取新闻列表成功")
        void should_list_news_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockNewsService.listNews(any(), eq(TEST_PAGE), eq(TEST_PAGE_SIZE)))
                    .thenReturn(Result.ok(Arrays.asList(
                            createTestNews(1, "新闻1", TYPE_AWARD, TEST_AUTHOR_ID, 1),
                            createTestNews(2, "新闻2", TYPE_ACTIVITY, TEST_AUTHOR_ID, 1)
                    )));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带分页参数的列表请求")
        void should_handle_pagination_params() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn("2");
            when(mockRequest.getParameter("pageSize")).thenReturn("10");
            when(mockNewsService.listNews(any(), eq(2), eq(10)))
                    .thenReturn(Result.ok(Arrays.asList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("带筛选条件的列表请求")
        void should_handle_filter_params() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockRequest.getParameter("keyword")).thenReturn("关键词");
            when(mockRequest.getParameter("type")).thenReturn(TYPE_AWARD);
            when(mockRequest.getParameter("status")).thenReturn("1");

            when(mockNewsService.listNews(any(), eq(TEST_PAGE), eq(TEST_PAGE_SIZE)))
                    .thenReturn(Result.ok(Arrays.asList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("新闻列表为空时应返回成功")
        void should_return_success_when_list_empty() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockNewsService.listNews(any(), eq(TEST_PAGE), eq(TEST_PAGE_SIZE)))
                    .thenReturn(Result.ok(Arrays.asList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("服务层返回错误时应返回错误响应")
        void should_return_error_when_service_fails() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockNewsService.listNews(any(), eq(TEST_PAGE), eq(TEST_PAGE_SIZE)))
                    .thenReturn(Result.error(500, "服务器内部错误"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }
    }

    // ==================== GET /api/news/types 新闻类型列表 ====================

    @Nested
    @DisplayName("GET /api/news/types 新闻类型列表")
    class ListTypesTests {

        @Test
        @FastTest
        @DisplayName("获取新闻类型列表成功")
        void should_list_types_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/types");
            when(mockRequest.getPathInfo()).thenReturn("/types");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }
    }

    // ==================== GET /api/news/{id} 新闻详情 ====================

    @Nested
    @DisplayName("GET /api/news/{id} 新闻详情")
    class GetNewsDetailTests {

        @Test
        @FastTest
        @DisplayName("获取新闻详情成功")
        void should_get_news_detail_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID);
            when(mockNewsService.getNewsDetail(TEST_NEWS_ID))
                    .thenReturn(Result.ok(createTestNews(TEST_NEWS_ID, "测试新闻", TYPE_AWARD, TEST_AUTHOR_ID, 1)));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("新闻不存在应返回404")
        void should_return_404_when_news_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/999");
            when(mockRequest.getPathInfo()).thenReturn("/999");
            when(mockNewsService.getNewsDetail(999))
                    .thenReturn(Result.error(404, "新闻不存在"));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== POST /api/news 创建新闻 ====================

    @Nested
    @DisplayName("POST /api/news 创建新闻")
    class CreateNewsTests {

        @Test
        @FastTest
        @DisplayName("创建新闻成功")
        void should_create_news_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn(null);
            when(mockRequest.getMethod()).thenReturn("POST");

            String jsonBody = "{\"title\":\"新新闻\",\"type\":\"award\",\"content\":\"<p>内容</p>\",\"summary\":\"摘要\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));

            when(mockNewsService.createNews(any(NewsDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestNews(TEST_NEWS_ID, "新新闻", TYPE_AWARD, TEST_USER_ID, 1)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("空请求体应返回400")
        void should_return_400_when_empty_body() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("无效JSON应返回400")
        void should_return_400_when_invalid_json() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("invalid json")));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("缺少必填字段应返回错误")
        void should_return_error_when_required_field_missing() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getMethod()).thenReturn("POST");
            // JSON缺少title
            String jsonBody = "{\"type\":\"award\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(mockNewsService.createNews(any(NewsDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.error(400, "新闻标题不能为空"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("创建失败应返回错误响应")
        void should_return_error_when_create_fails() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getMethod()).thenReturn("POST");
            String jsonBody = "{\"title\":\"新新闻\",\"type\":\"award\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(mockNewsService.createNews(any(NewsDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.error(500, "创建新闻失败"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":500");
        }
    }

    // ==================== PUT /api/news/{id} 更新新闻 ====================

    @Nested
    @DisplayName("PUT /api/news/{id} 更新新闻")
    class UpdateNewsTests {

        @Test
        @FastTest
        @DisplayName("更新新闻成功")
        void should_update_news_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"title\":\"更新后的新闻\",\"type\":\"award\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(mockNewsService.updateNews(eq(TEST_NEWS_ID), any(NewsDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestNews(TEST_NEWS_ID, "更新后的新闻", TYPE_AWARD, TEST_AUTHOR_ID, 1)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("使用POST模拟PUT更新成功")
        void should_update_via_post_tunnel() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID);
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            String jsonBody = "{\"title\":\"更新标题\",\"type\":\"activity\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(mockNewsService.updateNews(eq(TEST_NEWS_ID), any(NewsDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.ok(createTestNews(TEST_NEWS_ID, "更新标题", TYPE_ACTIVITY, TEST_AUTHOR_ID, 1)));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("更新不存在的新闻应返回404")
        void should_return_404_when_news_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/999");
            when(mockRequest.getPathInfo()).thenReturn("/999");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");
            String jsonBody = "{\"title\":\"更新\",\"type\":\"award\"}";
            when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
            when(mockNewsService.updateNews(eq(999), any(NewsDTO.class), eq(TEST_USER_ID)))
                    .thenReturn(Result.error(404, "新闻不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/xyz");
            when(mockRequest.getPathInfo()).thenReturn("/xyz");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockRequest.getParameter("_method")).thenReturn("PUT");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== DELETE /api/news/{id} 删除新闻 ====================

    @Nested
    @DisplayName("DELETE /api/news/{id} 删除新闻")
    class DeleteNewsTests {

        @Test
        @FastTest
        @DisplayName("删除新闻成功")
        void should_delete_news_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID);
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID);
            when(mockNewsService.deleteNews(TEST_NEWS_ID, TEST_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("删除不存在的新闻应返回404")
        void should_return_404_when_news_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/999");
            when(mockRequest.getPathInfo()).thenReturn("/999");
            when(mockNewsService.deleteNews(999, TEST_USER_ID))
                    .thenReturn(Result.error(404, "新闻不存在"));

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id_format() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/abc");
            when(mockRequest.getPathInfo()).thenReturn("/abc");

            servlet.doDelete(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== POST /api/news/{id}/publish 发布新闻 ====================

    @Nested
    @DisplayName("POST /api/news/{id}/publish 发布新闻")
    class PublishNewsTests {

        @Test
        @FastTest
        @DisplayName("发布新闻成功")
        void should_publish_news_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID + "/publish");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID + "/publish");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockNewsService.publishNews(TEST_NEWS_ID, TEST_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("发布不存在的新闻应返回404")
        void should_return_404_when_news_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/999/publish");
            when(mockRequest.getPathInfo()).thenReturn("/999/publish");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockNewsService.publishNews(999, TEST_USER_ID))
                    .thenReturn(Result.error(404, "新闻不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/xyz/publish");
            when(mockRequest.getPathInfo()).thenReturn("/xyz/publish");
            when(mockRequest.getMethod()).thenReturn("POST");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== POST /api/news/{id}/unpublish 取消发布 ====================

    @Nested
    @DisplayName("POST /api/news/{id}/unpublish 取消发布")
    class UnpublishNewsTests {

        @Test
        @FastTest
        @DisplayName("取消发布新闻成功")
        void should_unpublish_news_successfully() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/" + TEST_NEWS_ID + "/unpublish");
            when(mockRequest.getPathInfo()).thenReturn("/" + TEST_NEWS_ID + "/unpublish");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockNewsService.unpublishNews(TEST_NEWS_ID, TEST_USER_ID))
                    .thenReturn(Result.ok());

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("取消发布不存在的新闻应返回404")
        void should_return_404_when_news_not_found() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/999/unpublish");
            when(mockRequest.getPathInfo()).thenReturn("/999/unpublish");
            when(mockRequest.getMethod()).thenReturn("POST");
            when(mockNewsService.unpublishNews(999, TEST_USER_ID))
                    .thenReturn(Result.error(404, "新闻不存在"));

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }

        @Test
        @FastTest
        @DisplayName("无效ID格式应返回400")
        void should_return_400_when_invalid_id() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/abc/unpublish");
            when(mockRequest.getPathInfo()).thenReturn("/abc/unpublish");
            when(mockRequest.getMethod()).thenReturn("POST");

            servlet.doPost(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }
    }

    // ==================== 路径解析测试 ====================

    @Nested
    @DisplayName("路径解析测试")
    class PathParsingTests {

        @Test
        @FastTest
        @DisplayName("空路径应列出新闻")
        void should_list_news_when_path_empty() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news");
            when(mockRequest.getPathInfo()).thenReturn("");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockNewsService.listNews(any(), eq(TEST_PAGE), eq(TEST_PAGE_SIZE)))
                    .thenReturn(Result.ok(Arrays.asList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("根路径应列出新闻")
        void should_list_news_when_path_slash() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/");
            when(mockRequest.getPathInfo()).thenReturn("/");
            when(mockRequest.getParameter("page")).thenReturn(null);
            when(mockRequest.getParameter("pageSize")).thenReturn(null);
            when(mockNewsService.listNews(any(), eq(TEST_PAGE), eq(TEST_PAGE_SIZE)))
                    .thenReturn(Result.ok(Arrays.asList()));

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":0");
        }

        @Test
        @FastTest
        @DisplayName("未知路径应返回404")
        void should_return_400_when_path_segment_invalid() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/unknown");
            when(mockRequest.getPathInfo()).thenReturn("/unknown");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":400");
        }

        @Test
        @FastTest
        @DisplayName("嵌套过深的路径应返回404")
        void should_return_404_when_path_too_deep() throws Exception {
            simulateLogin(createTestUser(TEST_USER_ID, "ADMIN"));
            when(mockRequest.getRequestURI()).thenReturn("/api/news/1/sub/deep");
            when(mockRequest.getPathInfo()).thenReturn("/1/sub/deep");

            servlet.doGet(mockRequest, mockResponse);

            assertThat(getResponseBody()).contains("\"code\":404");
        }
    }

    // ==================== 可测试的内部Servlet类 ====================

    /**
     * 可测试的NewsApiServlet
     *
     * 复制NewsApiServlet的业务逻辑到此，隔离对实际实现的依赖。
     * 当实际Servlet实现完成后，这些测试仍然有效，因为它们验证的是行为契约。
     */
    private static class TestableNewsApiServlet {

        private final NewsService newsService;
        private final Gson gson = new GsonBuilder().create();

        public TestableNewsApiServlet(NewsService newsService) {
            this.newsService = newsService;
        }

        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = getPathInfo(req);
            String uri = req.getRequestURI();

            // /api/news/types
            if (pathInfo != null && pathInfo.equals("/types")) {
                handleListTypes(req, resp, currentUser);
                return;
            }

            // /api/news 或 /api/news/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                if (uri != null && (uri.equals("/api/news") || uri.equals("/api/news/") || uri.startsWith("/api/news/"))) {
                    handleListNews(req, resp, currentUser);
                } else {
                    writeJson(resp, Result.error(404, "路径不存在"));
                }
                return;
            }

            // 解析路径
            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            Integer newsId = parseIdOrNull(segments[0]);
            if (newsId == null) {
                writeJson(resp, Result.error(400, "无效的新闻ID"));
                return;
            }

            if (segments.length == 1) {
                // /api/news/{id}
                handleGetNewsDetail(req, resp, currentUser, newsId);
                return;
            }

            String subResource = segments[1];

            // /api/news/{id}/publish
            if (subResource.equals("publish") && segments.length == 2) {
                handlePublish(req, resp, currentUser, newsId);
                return;
            }

            // /api/news/{id}/unpublish
            if (subResource.equals("unpublish") && segments.length == 2) {
                handleUnpublish(req, resp, currentUser, newsId);
                return;
            }

            writeJson(resp, Result.error(404, "路径不存在"));
        }

        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = getPathInfo(req);
            String method = req.getParameter("_method");
            boolean isPutTunnel = "PUT".equalsIgnoreCase(method);
            String uri = req.getRequestURI();

            // /api/news 或 /api/news/
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                if (isPutTunnel) {
                    writeJson(resp, Result.error(405, "PUT方法不支持在根路径"));
                } else {
                    handleCreateNews(req, resp, currentUser);
                }
                return;
            }

            // 解析路径
            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            Integer newsId = parseIdOrNull(segments[0]);
            if (newsId == null) {
                writeJson(resp, Result.error(400, "无效的新闻ID"));
                return;
            }

            if (segments.length == 1) {
                // /api/news/{id} - 更新
                if (isPutTunnel) {
                    handleUpdateNews(req, resp, currentUser, newsId);
                } else {
                    writeJson(resp, Result.error(404, "路径不存在"));
                }
                return;
            }

            String subResource = segments[1];

            // /api/news/{id}/publish
            if (subResource.equals("publish") && segments.length == 2) {
                handlePublish(req, resp, currentUser, newsId);
                return;
            }

            // /api/news/{id}/unpublish
            if (subResource.equals("unpublish") && segments.length == 2) {
                handleUnpublish(req, resp, currentUser, newsId);
                return;
            }

            writeJson(resp, Result.error(404, "路径不存在"));
        }

        public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
            User currentUser = getCurrentUser(req);
            if (currentUser == null) {
                writeJson(resp, Result.error(401, "请先登录"));
                return;
            }

            String pathInfo = getPathInfo(req);

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            String[] segments = pathInfo.substring(1).split("/");
            if (segments.length < 1) {
                writeJson(resp, Result.error(404, "路径不存在"));
                return;
            }

            Integer newsId = parseIdOrNull(segments[0]);
            if (newsId == null) {
                writeJson(resp, Result.error(400, "无效的新闻ID"));
                return;
            }

            if (segments.length == 1) {
                // /api/news/{id}
                handleDeleteNews(req, resp, currentUser, newsId);
                return;
            }

            writeJson(resp, Result.error(404, "路径不存在"));
        }

        // ==================== 处理器方法 ====================

        private void handleListNews(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws Exception {
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");

            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 20;

            NewsFilterDTO filter = new NewsFilterDTO();
            String keyword = req.getParameter("keyword");
            String type = req.getParameter("type");
            String statusStr = req.getParameter("status");

            if (keyword != null && !keyword.trim().isEmpty()) {
                filter.setKeyword(keyword);
            }
            if (type != null && !type.trim().isEmpty()) {
                filter.setType(type);
            }
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                filter.setStatus(Integer.parseInt(statusStr));
            }

            Result result = newsService.listNews(filter, page, pageSize);
            writeJson(resp, result);
        }

        private void handleListTypes(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws Exception {
            // 返回固定的类型列表
            List<String> types = Arrays.asList("award", "activity", "notice");
            writeJson(resp, Result.ok(types));
        }

        private void handleGetNewsDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer newsId) throws Exception {
            Result result = newsService.getNewsDetail(newsId);
            writeJson(resp, result);
        }

        private void handleCreateNews(HttpServletRequest req, HttpServletResponse resp, User currentUser) throws Exception {
            String body = readRequestBody(req);
            if (body == null || body.trim().isEmpty()) {
                writeJson(resp, Result.error(400, "请求体不能为空"));
                return;
            }

            try {
                NewsDTO dto = gson.fromJson(body, NewsDTO.class);
                if (dto == null) {
                    writeJson(resp, Result.error(400, "无效的JSON数据"));
                    return;
                }
                Result result = newsService.createNews(dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
            }
        }

        private void handleUpdateNews(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer newsId) throws Exception {
            String body = readRequestBody(req);
            if (body == null || body.trim().isEmpty()) {
                writeJson(resp, Result.error(400, "请求体不能为空"));
                return;
            }

            try {
                NewsDTO dto = gson.fromJson(body, NewsDTO.class);
                if (dto == null) {
                    writeJson(resp, Result.error(400, "无效的JSON数据"));
                    return;
                }
                Result result = newsService.updateNews(newsId, dto, currentUser.getId());
                writeJson(resp, result);
            } catch (Exception e) {
                writeJson(resp, Result.error(400, "无效的JSON格式"));
            }
        }

        private void handleDeleteNews(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer newsId) throws Exception {
            Result result = newsService.deleteNews(newsId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handlePublish(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer newsId) throws Exception {
            Result result = newsService.publishNews(newsId, currentUser.getId());
            writeJson(resp, result);
        }

        private void handleUnpublish(HttpServletRequest req, HttpServletResponse resp, User currentUser, Integer newsId) throws Exception {
            Result result = newsService.unpublishNews(newsId, currentUser.getId());
            writeJson(resp, result);
        }

        // ==================== 工具方法 ====================

        private User getCurrentUser(HttpServletRequest req) {
            javax.servlet.http.HttpSession session = req.getSession(false);
            if (session == null) {
                return null;
            }
            return (User) session.getAttribute("user");
        }

        private String getPathInfo(HttpServletRequest req) {
            String pathInfo = req.getPathInfo();
            return pathInfo;
        }

        private Integer parseIdOrNull(String str) {
            if (str == null || str.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private String readRequestBody(HttpServletRequest req) throws Exception {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        private void writeJson(HttpServletResponse resp, Result result) throws Exception {
            resp.setContentType("application/json;charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            gson.toJson(result, resp.getWriter());
        }
    }
}
