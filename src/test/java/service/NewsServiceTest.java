package service;

import dao.FileStorageDAO;
import dao.NewsDAO;
import dao.UserDAO;
import dto.NewsDTO;
import dto.NewsFilterDTO;
import model.FileStorage;
import model.News;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import support.FastTest;
import util.Result;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NewsService TDD测试套件 - Red阶段
 *
 * 测试范围：服务分层与API化完整计划.md 5.1 NewsService 新闻服务
 * - 所有正常路径
 * - 所有边界情况
 * - 所有异常场景
 * - 所有状态枚举
 *
 * Mock说明：所有mock基于实际DAO接口签名
 * - NewsDAO: findByType(type) / findById(id) / findAll() / findByConditions(keyword, type, status)
 * - NewsDAO: insert(News) / update(News) / updateStatus(id, status)
 * - NewsDAO: findByActivityId(activityId) / existsByActivityId(activityId) / count()
 * - UserDAO: findById(id)
 * - FileStorageDAO: insert(FileStorage)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NewsService 新闻服务测试")
class NewsServiceTest {

    @Mock
    private NewsDAO newsDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private FileStorageDAO fileStorageDAO;

    private NewsService newsService;

    @BeforeEach
    void setUp() {
        newsService = new NewsService(newsDAO, userDAO, fileStorageDAO);
        // 默认mock：userDAO.findById对任何ID都返回有效用户
        when(userDAO.findById(anyInt())).thenReturn(createUser(1, "admin", ROLE_ADMIN));
    }

    // ==================== 测试数据常量 ====================

    private static final Integer ADMIN_USER_ID = 1;
    private static final Integer AUTHOR_USER_ID = 2;
    private static final Integer OTHER_USER_ID = 3;
    private static final Integer NONEXISTENT_USER_ID = 99999;
    private static final Integer NEWS_ID = 100;
    private static final Integer ACTIVITY_ID = 300;

    // 角色枚举
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    // 新闻状态枚举
    private static final Integer STATUS_PUBLISHED = 1;
    private static final Integer STATUS_DELETED = 0;

    // 新闻类型枚举
    private static final String TYPE_AWARD = "award";
    private static final String TYPE_ACTIVITY = "activity";
    private static final String TYPE_NOTICE = "notice";

    // 分页常量
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    // ==================== 测试初始化辅助方法 ====================

    private User createUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    private News createNews(Integer id, String title, String type, Integer authorId, Integer status) {
        News news = new News();
        news.setId(id);
        news.setTitle(title);
        news.setType(type);
        news.setAuthorId(authorId);
        news.setStatus(status);
        news.setContentPath("localstorage/news/" + type + "/" + id + ".html");
        news.setSummary("测试摘要");
        news.setCreatedAt(new Date());
        news.setUpdatedAt(new Date());
        return news;
    }

    private NewsDTO createNewsDTO(String title, String type) {
        NewsDTO dto = new NewsDTO();
        dto.setTitle(title);
        dto.setType(type);
        dto.setContent("测试内容");
        dto.setSummary("测试摘要");
        return dto;
    }

    private NewsFilterDTO createNewsFilterDTO(String keyword, String type, Integer status) {
        NewsFilterDTO dto = new NewsFilterDTO();
        dto.setKeyword(keyword);
        dto.setType(type);
        dto.setStatus(status);
        return dto;
    }

    // ==================== listNews 新闻列表(分页) ====================

    @Nested
    @DisplayName("listNews 新闻列表(分页)")
    class ListNewsTests {

        @FastTest
        @DisplayName("获取新闻列表成功应返回成功")
        void should_list_news_successfully() {
            List<News> newsList = Arrays.asList(
                createNews(1, "新闻1", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED),
                createNews(2, "新闻2", TYPE_ACTIVITY, AUTHOR_USER_ID, STATUS_PUBLISHED)
            );
            when(newsDAO.findByConditions(isNull(), isNull(), isNull())).thenReturn(newsList);

            Result result = newsService.listNews(null, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).findByConditions(isNull(), isNull(), isNull());
        }

        @FastTest
        @DisplayName("新闻列表为空时应返回空列表")
        void should_return_empty_list_when_no_news() {
            when(newsDAO.findByConditions(isNull(), isNull(), isNull())).thenReturn(Arrays.asList());

            Result result = newsService.listNews(null, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数page为1应正常返回")
        void should_handle_page_1() {
            when(newsDAO.findByConditions(isNull(), isNull(), isNull())).thenReturn(Arrays.asList());

            Result result = newsService.listNews(null, 1, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("分页参数pageSize为最大值应正常返回")
        void should_handle_large_page_size() {
            when(newsDAO.findByConditions(isNull(), isNull(), isNull())).thenReturn(Arrays.asList());

            Result result = newsService.listNews(null, DEFAULT_PAGE, MAX_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = newsService.listNews(null, 0, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize为0时应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = newsService.listNews(null, DEFAULT_PAGE, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize超过最大值时应返回错误")
        void should_return_error_when_page_size_exceeds_max() {
            Result result = newsService.listNews(null, DEFAULT_PAGE, MAX_PAGE_SIZE + 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为负数时应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = newsService.listNews(null, -1, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("带筛选条件应正确传递参数")
        void should_pass_filter_correctly() {
            NewsFilterDTO filter = createNewsFilterDTO("关键词", TYPE_AWARD, STATUS_PUBLISHED);
            when(newsDAO.findByConditions("关键词", TYPE_AWARD, STATUS_PUBLISHED)).thenReturn(Arrays.asList());

            Result result = newsService.listNews(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).findByConditions("关键词", TYPE_AWARD, STATUS_PUBLISHED);
        }

        @FastTest
        @DisplayName("keyword为空字符串时应作为null处理")
        void should_handle_empty_keyword_as_null() {
            NewsFilterDTO filter = createNewsFilterDTO("", TYPE_AWARD, STATUS_PUBLISHED);
            when(newsDAO.findByConditions(isNull(), eq(TYPE_AWARD), eq(STATUS_PUBLISHED))).thenReturn(Arrays.asList());

            Result result = newsService.listNews(filter, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== getNewsByType 按类型查询 ====================

    @Nested
    @DisplayName("getNewsByType 按类型查询")
    class GetNewsByTypeTests {

        @FastTest
        @DisplayName("按类型查询成功应返回成功")
        void should_get_news_by_type_successfully() {
            List<News> newsList = Arrays.asList(
                createNews(1, "奖项新闻1", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED),
                createNews(2, "奖项新闻2", TYPE_AWARD, AUTHOR_USER_ID, STATUS_PUBLISHED)
            );
            when(newsDAO.findByType(TYPE_AWARD)).thenReturn(newsList);

            Result result = newsService.getNewsByType(TYPE_AWARD, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).findByType(TYPE_AWARD);
        }

        @FastTest
        @DisplayName("按类型查询结果为空时应返回空列表")
        void should_return_empty_list_when_no_news_of_type() {
            when(newsDAO.findByType(TYPE_NOTICE)).thenReturn(Arrays.asList());

            Result result = newsService.getNewsByType(TYPE_NOTICE, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按award类型查询应成功")
        void should_get_award_type_news() {
            when(newsDAO.findByType(TYPE_AWARD)).thenReturn(Arrays.asList());

            Result result = newsService.getNewsByType(TYPE_AWARD, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按activity类型查询应成功")
        void should_get_activity_type_news() {
            when(newsDAO.findByType(TYPE_ACTIVITY)).thenReturn(Arrays.asList());

            Result result = newsService.getNewsByType(TYPE_ACTIVITY, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("按notice类型查询应成功")
        void should_get_notice_type_news() {
            when(newsDAO.findByType(TYPE_NOTICE)).thenReturn(Arrays.asList());

            Result result = newsService.getNewsByType(TYPE_NOTICE, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("类型为null时应返回错误")
        void should_return_error_when_type_is_null() {
            Result result = newsService.getNewsByType(null, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("类型为空字符串时应返回错误")
        void should_return_error_when_type_is_empty() {
            Result result = newsService.getNewsByType("", DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("page为0时应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = newsService.getNewsByType(TYPE_AWARD, 0, DEFAULT_PAGE_SIZE);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("pageSize为0时应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = newsService.getNewsByType(TYPE_AWARD, DEFAULT_PAGE, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    // ==================== getNewsDetail 新闻详情 ====================

    @Nested
    @DisplayName("getNewsDetail 新闻详情")
    class GetNewsDetailTests {

        @FastTest
        @DisplayName("获取新闻详情成功应返回成功")
        void should_get_news_detail_successfully() {
            News news = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(news);

            Result result = newsService.getNewsDetail(NEWS_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).findById(NEWS_ID);
        }

        @FastTest
        @DisplayName("新闻不存在时应返回错误")
        void should_return_error_when_news_not_exists() {
            when(newsDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = newsService.getNewsDetail(NONEXISTENT_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("id为null时应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.getNewsDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("已删除的新闻不应返回")
        void should_not_return_deleted_news() {
            News deletedNews = createNews(NEWS_ID, "已删除新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(deletedNews);

            Result result = newsService.getNewsDetail(NEWS_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("不同类型的新闻应正确返回")
        void should_return_news_of_different_types() {
            News awardNews = createNews(1, "奖项新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            News activityNews = createNews(2, "活动新闻", TYPE_ACTIVITY, ADMIN_USER_ID, STATUS_PUBLISHED);
            News noticeNews = createNews(3, "通知新闻", TYPE_NOTICE, ADMIN_USER_ID, STATUS_PUBLISHED);

            when(newsDAO.findById(1)).thenReturn(awardNews);
            when(newsDAO.findById(2)).thenReturn(activityNews);
            when(newsDAO.findById(3)).thenReturn(noticeNews);

            assertThat(newsService.getNewsDetail(1).isSuccess()).isTrue();
            assertThat(newsService.getNewsDetail(2).isSuccess()).isTrue();
            assertThat(newsService.getNewsDetail(3).isSuccess()).isTrue();
        }
    }

    // ==================== createNews 创建新闻 ====================

    @Nested
    @DisplayName("createNews 创建新闻")
    class CreateNewsTests {

        @FastTest
        @DisplayName("创建新闻成功应返回成功")
        void should_create_news_successfully() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).insert(any(News.class));
        }

        @FastTest
        @DisplayName("创建新闻时应设置作者ID")
        void should_set_author_id_when_create() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            newsService.createNews(dto, AUTHOR_USER_ID);

            ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
            verify(newsDAO).insert(captor.capture());
            assertThat(captor.getValue().getAuthorId()).isEqualTo(AUTHOR_USER_ID);
        }

        @FastTest
        @DisplayName("创建新闻时应设置正确的内容路径")
        void should_set_content_path_when_create() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            newsService.createNews(dto, AUTHOR_USER_ID);

            ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
            verify(newsDAO).insert(captor.capture());
            assertThat(captor.getValue().getContentPath()).contains("localstorage/news/" + TYPE_AWARD);
        }

        @FastTest
        @DisplayName("创建新闻时dto为null应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = newsService.createNews(null, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时标题为空应返回错误")
        void should_return_error_when_title_is_empty() {
            NewsDTO dto = createNewsDTO("", TYPE_AWARD);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时标题为null应返回错误")
        void should_return_error_when_title_is_null() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            dto.setTitle(null);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时类型为null应返回错误")
        void should_return_error_when_type_is_null() {
            NewsDTO dto = createNewsDTO("新新闻", null);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时类型为空应返回错误")
        void should_return_error_when_type_is_empty() {
            NewsDTO dto = createNewsDTO("新新闻", "");

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时authorId为null应返回错误")
        void should_return_error_when_author_id_is_null() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);

            Result result = newsService.createNews(dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时作者不存在应返回错误")
        void should_return_error_when_author_not_exists() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            when(userDAO.findById(AUTHOR_USER_ID)).thenReturn(null);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("创建新闻时数据库插入失败应返回错误")
        void should_return_error_when_database_insert_fails() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            when(newsDAO.insert(any(News.class))).thenReturn(false);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("创建award类型新闻应成功")
        void should_create_award_news() {
            NewsDTO dto = createNewsDTO("奖项新闻", TYPE_AWARD);
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("创建activity类型新闻应成功")
        void should_create_activity_news() {
            NewsDTO dto = createNewsDTO("活动新闻", TYPE_ACTIVITY);
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("创建notice类型新闻应成功")
        void should_create_notice_news() {
            NewsDTO dto = createNewsDTO("通知新闻", TYPE_NOTICE);
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("创建新闻时标题过长应返回错误")
        void should_return_error_when_title_too_long() {
            String longTitle = new String(new char[257]).replace('\0', '标');
            NewsDTO dto = createNewsDTO(longTitle, TYPE_AWARD);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时摘要过长应返回错误")
        void should_return_error_when_summary_too_long() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            dto.setSummary(new String(new char[501]).replace('\0', '摘'));

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("创建新闻时content为空应使用空字符串")
        void should_handle_empty_content() {
            NewsDTO dto = createNewsDTO("新新闻", TYPE_AWARD);
            dto.setContent("");
            when(newsDAO.insert(any(News.class))).thenReturn(true);
            when(fileStorageDAO.insert(any(FileStorage.class))).thenReturn(1);

            Result result = newsService.createNews(dto, AUTHOR_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ==================== updateNews 更新新闻 ====================

    @Nested
    @DisplayName("updateNews 更新新闻")
    class UpdateNewsTests {

        @FastTest
        @DisplayName("更新新闻成功应返回成功")
        void should_update_news_successfully() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);
            News existingNews = createNews(NEWS_ID, "原新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.update(any(News.class))).thenReturn(true);

            Result result = newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).findById(NEWS_ID);
        }

        @FastTest
        @DisplayName("更新新闻时应更新标题")
        void should_update_title_when_update() {
            NewsDTO dto = createNewsDTO("更新后的标题", TYPE_AWARD);
            News existingNews = createNews(NEWS_ID, "原标题", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.update(any(News.class))).thenReturn(true);

            newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            ArgumentCaptor<News> captor = ArgumentCaptor.forClass(News.class);
            verify(newsDAO).update(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo("更新后的标题");
        }

        @FastTest
        @DisplayName("更新新闻时新闻不存在应返回错误")
        void should_return_error_when_news_not_exists() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);
            when(newsDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = newsService.updateNews(NONEXISTENT_USER_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("更新新闻时id为null应返回错误")
        void should_return_error_when_id_is_null() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);

            Result result = newsService.updateNews(null, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新新闻时dto为null应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = newsService.updateNews(NEWS_ID, null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新新闻时operatorId为null应返回错误")
        void should_return_error_when_operator_id_is_null() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);

            Result result = newsService.updateNews(NEWS_ID, dto, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新新闻时标题为空应返回错误")
        void should_return_error_when_title_is_empty() {
            NewsDTO dto = createNewsDTO("", TYPE_AWARD);
            News existingNews = createNews(NEWS_ID, "原新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);

            Result result = newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新新闻时标题为null应返回错误")
        void should_return_error_when_title_is_null() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);
            dto.setTitle(null);
            News existingNews = createNews(NEWS_ID, "原新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);

            Result result = newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新新闻时类型为null应返回错误")
        void should_return_error_when_type_is_null() {
            NewsDTO dto = createNewsDTO("更新后的新闻", null);
            News existingNews = createNews(NEWS_ID, "原新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);

            Result result = newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("更新新闻时数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);
            News existingNews = createNews(NEWS_ID, "原新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.update(any(News.class))).thenReturn(false);

            Result result = newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }

        @FastTest
        @DisplayName("更新已删除的新闻应返回错误")
        void should_return_error_when_updating_deleted_news() {
            NewsDTO dto = createNewsDTO("更新后的新闻", TYPE_AWARD);
            News deletedNews = createNews(NEWS_ID, "原新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(deletedNews);

            Result result = newsService.updateNews(NEWS_ID, dto, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }
    }

    // ==================== deleteNews 删除新闻(软删除) ====================

    @Nested
    @DisplayName("deleteNews 删除新闻(软删除)")
    class DeleteNewsTests {

        @FastTest
        @DisplayName("删除新闻成功应返回成功")
        void should_delete_news_successfully() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_DELETED)).thenReturn(true);

            Result result = newsService.deleteNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).updateStatus(NEWS_ID, STATUS_DELETED);
        }

        @FastTest
        @DisplayName("删除新闻时应设置status为0")
        void should_set_status_to_zero_when_delete() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(eq(NEWS_ID), eq(STATUS_DELETED))).thenReturn(true);

            newsService.deleteNews(NEWS_ID, ADMIN_USER_ID);

            verify(newsDAO).updateStatus(NEWS_ID, STATUS_DELETED);
        }

        @FastTest
        @DisplayName("删除不存在的新闻应返回错误")
        void should_return_error_when_news_not_exists() {
            when(newsDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = newsService.deleteNews(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除新闻时id为null应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.deleteNews(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除新闻时operatorId为null应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.deleteNews(NEWS_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("删除已删除的新闻应返回错误")
        void should_return_error_when_news_already_deleted() {
            News deletedNews = createNews(NEWS_ID, "已删除新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(deletedNews);

            Result result = newsService.deleteNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("删除新闻时数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_DELETED)).thenReturn(false);

            Result result = newsService.deleteNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== publishNews 发布新闻 ====================

    @Nested
    @DisplayName("publishNews 发布新闻")
    class PublishNewsTests {

        @FastTest
        @DisplayName("发布新闻成功应返回成功")
        void should_publish_news_successfully() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_PUBLISHED)).thenReturn(true);

            Result result = newsService.publishNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).updateStatus(NEWS_ID, STATUS_PUBLISHED);
        }

        @FastTest
        @DisplayName("发布新闻时应设置status为1")
        void should_set_status_to_one_when_publish() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(eq(NEWS_ID), eq(STATUS_PUBLISHED))).thenReturn(true);

            newsService.publishNews(NEWS_ID, ADMIN_USER_ID);

            verify(newsDAO).updateStatus(NEWS_ID, STATUS_PUBLISHED);
        }

        @FastTest
        @DisplayName("发布不存在的新闻应返回错误")
        void should_return_error_when_news_not_exists() {
            when(newsDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = newsService.publishNews(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("发布新闻时id为null应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.publishNews(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("发布新闻时operatorId为null应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.publishNews(NEWS_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("发布已发布的新闻应返回成功")
        void should_succeed_when_news_already_published() {
            News publishedNews = createNews(NEWS_ID, "已发布新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(publishedNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_PUBLISHED)).thenReturn(true);

            Result result = newsService.publishNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("发布新闻时数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_PUBLISHED)).thenReturn(false);

            Result result = newsService.publishNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }

    // ==================== unpublishNews 取消发布 ====================

    @Nested
    @DisplayName("unpublishNews 取消发布")
    class UnpublishNewsTests {

        @FastTest
        @DisplayName("取消发布新闻成功应返回成功")
        void should_unpublish_news_successfully() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_DELETED)).thenReturn(true);

            Result result = newsService.unpublishNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO).updateStatus(NEWS_ID, STATUS_DELETED);
        }

        @FastTest
        @DisplayName("取消发布新闻时应设置status为0")
        void should_set_status_to_zero_when_unpublish() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(eq(NEWS_ID), eq(STATUS_DELETED))).thenReturn(true);

            newsService.unpublishNews(NEWS_ID, ADMIN_USER_ID);

            verify(newsDAO).updateStatus(NEWS_ID, STATUS_DELETED);
        }

        @FastTest
        @DisplayName("取消发布不存在的新闻应返回错误")
        void should_return_error_when_news_not_exists() {
            when(newsDAO.findById(NONEXISTENT_USER_ID)).thenReturn(null);

            Result result = newsService.unpublishNews(NONEXISTENT_USER_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @FastTest
        @DisplayName("取消发布新闻时id为null应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.unpublishNews(null, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("取消发布新闻时operatorId为null应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.unpublishNews(NEWS_ID, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @FastTest
        @DisplayName("取消发布已下线的新闻应返回成功")
        void should_succeed_when_news_already_unpublished() {
            News unpublishedNews = createNews(NEWS_ID, "已下线新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_DELETED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(unpublishedNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_DELETED)).thenReturn(true);

            Result result = newsService.unpublishNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isTrue();
        }

        @FastTest
        @DisplayName("取消发布新闻时数据库更新失败应返回错误")
        void should_return_error_when_database_update_fails() {
            News existingNews = createNews(NEWS_ID, "测试新闻", TYPE_AWARD, ADMIN_USER_ID, STATUS_PUBLISHED);
            when(newsDAO.findById(NEWS_ID)).thenReturn(existingNews);
            when(newsDAO.updateStatus(NEWS_ID, STATUS_DELETED)).thenReturn(false);

            Result result = newsService.unpublishNews(NEWS_ID, ADMIN_USER_ID);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
        }
    }
}
