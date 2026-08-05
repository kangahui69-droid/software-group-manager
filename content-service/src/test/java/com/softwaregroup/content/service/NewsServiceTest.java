package com.softwaregroup.content.service;

import com.softwaregroup.content.dao.NewsDAO;
import com.softwaregroup.content.dao.UserDAO;
import com.softwaregroup.content.model.News;
import com.softwaregroup.content.model.User;
import com.softwaregroup.content.model.dto.NewsDTO;
import com.softwaregroup.content.model.dto.NewsFilterDTO;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NewsService 单元测试
 * 覆盖所有公开业务方法的正常路径、边界情况和异常场景
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("新闻服务测试")
class NewsServiceTest {

    @Mock
    private NewsDAO newsDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private com.softwaregroup.content.dao.FileStorageDAO fileStorageDAO;

    @InjectMocks
    private NewsService newsService;

    private News testNews;
    private NewsDTO testNewsDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testNews = new News();
        testNews.setId(1);
        testNews.setTitle("测试新闻");
        testNews.setType("NOTICE");
        testNews.setSummary("新闻摘要");
        testNews.setAuthorId(1);
        testNews.setStatus(1);
        testNews.setContentPath("/localstorage/news/notice/1.html");
        testNews.setCreatedAt(new Date());

        testNewsDTO = new NewsDTO();
        testNewsDTO.setTitle("新新闻标题");
        testNewsDTO.setType("NOTICE");
        testNewsDTO.setSummary("新新闻摘要");

        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("admin");
        testUser.setRole("ADMIN");
    }

    @Nested
    @DisplayName("listNews - 新闻列表")
    class ListNewsTests {

        @Test
        @DisplayName("正常路径：返回新闻列表")
        void should_return_news_list_when_valid_params() {
            when(newsDAO.findByConditions(any(), any(), any()))
                    .thenReturn(Arrays.asList(testNews));

            Result result = newsService.listNews(null, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
            verify(newsDAO, times(1)).findByConditions(any(), any(), any());
        }

        @Test
        @DisplayName("正常路径：带关键词过滤")
        void should_filter_by_keyword() {
            when(newsDAO.findByConditions(eq("测试"), any(), any()))
                    .thenReturn(Arrays.asList(testNews));

            NewsFilterDTO filter = new NewsFilterDTO();
            filter.setKeyword("测试");

            Result result = newsService.listNews(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO, times(1)).findByConditions(eq("测试"), any(), any());
        }

        @Test
        @DisplayName("正常路径：带类型过滤")
        void should_filter_by_type() {
            when(newsDAO.findByConditions(any(), eq("NOTICE"), any()))
                    .thenReturn(Arrays.asList(testNews));

            NewsFilterDTO filter = new NewsFilterDTO();
            filter.setType("NOTICE");

            Result result = newsService.listNews(filter, 1, 20);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：页码为0应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = newsService.listNews(null, 0, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("页码必须大于0");
        }

        @Test
        @DisplayName("异常场景：页码为负数应返回错误")
        void should_return_error_when_page_is_negative() {
            Result result = newsService.listNews(null, -1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：每页数量为0应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = newsService.listNews(null, 1, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("每页数量必须在1-100之间");
        }

        @Test
        @DisplayName("异常场景：每页数量超过100应返回错误")
        void should_return_error_when_page_size_exceeds_max() {
            Result result = newsService.listNews(null, 1, 101);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：每页数量为负数应返回错误")
        void should_return_error_when_page_size_is_negative() {
            Result result = newsService.listNews(null, 1, -1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("getNewsByType - 按类型获取新闻")
    class GetNewsByTypeTests {

        @Test
        @DisplayName("正常路径：返回指定类型的新闻列表")
        void should_return_news_by_type() {
            when(newsDAO.findByType("NOTICE")).thenReturn(Arrays.asList(testNews));

            Result result = newsService.getNewsByType("NOTICE", 1, 20);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("异常场景：类型为空应返回错误")
        void should_return_error_when_type_is_empty() {
            Result result = newsService.getNewsByType("", 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻类型不能为空");
        }

        @Test
        @DisplayName("异常场景：类型为null应返回错误")
        void should_return_error_when_type_is_null() {
            Result result = newsService.getNewsByType(null, 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：类型为纯空格应返回错误")
        void should_return_error_when_type_is_whitespace() {
            Result result = newsService.getNewsByType("   ", 1, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：页码为0应返回错误")
        void should_return_error_when_page_is_zero() {
            Result result = newsService.getNewsByType("NOTICE", 0, 20);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：每页数量为0应返回错误")
        void should_return_error_when_page_size_is_zero() {
            Result result = newsService.getNewsByType("NOTICE", 1, 0);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("getNewsDetail - 新闻详情")
    class GetNewsDetailTests {

        @Test
        @DisplayName("正常路径：返回已发布的新闻详情")
        void should_return_news_detail_when_published() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);

            Result result = newsService.getNewsDetail(1);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isInstanceOf(News.class);
            News news = (News) result.getData();
            assertThat(news.getId()).isEqualTo(1);
        }

        @Test
        @DisplayName("异常场景：新闻ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.getNewsDetail(null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻ID不能为空");
        }

        @Test
        @DisplayName("异常场景：新闻不存在应返回404")
        void should_return_404_when_news_not_found() {
            when(newsDAO.findById(999)).thenReturn(null);

            Result result = newsService.getNewsDetail(999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("新闻不存在");
        }

        @Test
        @DisplayName("异常场景：新闻已下线应返回404")
        void should_return_404_when_news_unpublished() {
            testNews.setStatus(0);
            when(newsDAO.findById(1)).thenReturn(testNews);

            Result result = newsService.getNewsDetail(1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("createNews - 创建新闻")
    class CreateNewsTests {

        @Test
        @DisplayName("正常路径：成功创建新闻")
        void should_create_news_successfully() {
            when(userDAO.findById(1)).thenReturn(testUser);
            when(newsDAO.insert(any(News.class))).thenReturn(true);

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO, times(1)).insert(any(News.class));
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = newsService.createNews(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻信息不能为空");
        }

        @Test
        @DisplayName("异常场景：作者ID为空应返回错误")
        void should_return_error_when_author_id_is_null() {
            Result result = newsService.createNews(testNewsDTO, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("作者ID不能为空");
        }

        @Test
        @DisplayName("异常场景：标题为空应返回错误")
        void should_return_error_when_title_is_empty() {
            testNewsDTO.setTitle("");

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻标题不能为空");
        }

        @Test
        @DisplayName("异常场景：标题为null应返回错误")
        void should_return_error_when_title_is_null() {
            testNewsDTO.setTitle(null);

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("边界情况：标题超过256字符应返回错误")
        void should_return_error_when_title_too_long() {
            testNewsDTO.setTitle("a".repeat(257));

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("256");
        }

        @Test
        @DisplayName("边界情况：标题为256字符应正常创建")
        void should_create_when_title_is_256_chars() {
            testNewsDTO.setTitle("a".repeat(256));
            when(userDAO.findById(1)).thenReturn(testUser);
            when(newsDAO.insert(any(News.class))).thenReturn(true);

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：类型为空应返回错误")
        void should_return_error_when_type_is_empty() {
            testNewsDTO.setType("");

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻类型不能为空");
        }

        @Test
        @DisplayName("异常场景：类型为null应返回错误")
        void should_return_error_when_type_is_null() {
            testNewsDTO.setType(null);

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("边界情况：摘要超过500字符应返回错误")
        void should_return_error_when_summary_too_long() {
            testNewsDTO.setSummary("a".repeat(501));

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("500");
        }

        @Test
        @DisplayName("异常场景：作者不存在应返回404")
        void should_return_404_when_author_not_found() {
            when(userDAO.findById(999)).thenReturn(null);

            Result result = newsService.createNews(testNewsDTO, 999);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
            assertThat(result.getMessage()).contains("作者不存在");
        }

        @Test
        @DisplayName("异常场景：数据库插入失败应返回500")
        void should_return_500_when_insert_fails() {
            when(userDAO.findById(1)).thenReturn(testUser);
            when(newsDAO.insert(any(News.class))).thenReturn(false);

            Result result = newsService.createNews(testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("创建新闻失败");
        }
    }

    @Nested
    @DisplayName("updateNews - 更新新闻")
    class UpdateNewsTests {

        @Test
        @DisplayName("正常路径：成功更新新闻")
        void should_update_news_successfully() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.update(any(News.class))).thenReturn(true);

            NewsDTO updateDTO = new NewsDTO();
            updateDTO.setTitle("更新后的标题");
            updateDTO.setType("NOTICE");

            Result result = newsService.updateNews(1, updateDTO, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO, times(1)).update(any(News.class));
        }

        @Test
        @DisplayName("异常场景：新闻ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.updateNews(null, testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻ID不能为空");
        }

        @Test
        @DisplayName("异常场景：DTO为空应返回错误")
        void should_return_error_when_dto_is_null() {
            Result result = newsService.updateNews(1, null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("新闻信息不能为空");
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.updateNews(1, testNewsDTO, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
            assertThat(result.getMessage()).contains("操作者ID不能为空");
        }

        @Test
        @DisplayName("异常场景：新闻不存在应返回404")
        void should_return_404_when_news_not_found() {
            when(newsDAO.findById(999)).thenReturn(null);

            Result result = newsService.updateNews(999, testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：新闻已下线应返回404")
        void should_return_404_when_news_unpublished() {
            testNews.setStatus(0);
            when(newsDAO.findById(1)).thenReturn(testNews);

            Result result = newsService.updateNews(1, testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：标题为空应返回错误")
        void should_return_error_when_title_is_empty() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);

            NewsDTO updateDTO = new NewsDTO();
            updateDTO.setTitle("");
            updateDTO.setType("NOTICE");

            Result result = newsService.updateNews(1, updateDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：类型为空应返回错误")
        void should_return_error_when_type_is_empty() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);

            NewsDTO updateDTO = new NewsDTO();
            updateDTO.setTitle("标题");
            updateDTO.setType("");

            Result result = newsService.updateNews(1, updateDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：数据库更新失败应返回500")
        void should_return_500_when_update_fails() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.update(any(News.class))).thenReturn(false);

            Result result = newsService.updateNews(1, testNewsDTO, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("更新新闻失败");
        }
    }

    @Nested
    @DisplayName("deleteNews - 删除新闻")
    class DeleteNewsTests {

        @Test
        @DisplayName("正常路径：成功删除新闻")
        void should_delete_news_successfully() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.updateStatus(1, 0)).thenReturn(true);

            Result result = newsService.deleteNews(1, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO, times(1)).updateStatus(1, 0);
        }

        @Test
        @DisplayName("异常场景：新闻ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.deleteNews(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.deleteNews(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：新闻不存在应返回404")
        void should_return_404_when_news_not_found() {
            when(newsDAO.findById(999)).thenReturn(null);

            Result result = newsService.deleteNews(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：新闻已下线应返回404")
        void should_return_404_when_news_already_deleted() {
            testNews.setStatus(0);
            when(newsDAO.findById(1)).thenReturn(testNews);

            Result result = newsService.deleteNews(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：数据库更新状态失败应返回500")
        void should_return_500_when_update_status_fails() {
            testNews.setStatus(1);
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.updateStatus(1, 0)).thenReturn(false);

            Result result = newsService.deleteNews(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("删除新闻失败");
        }
    }

    @Nested
    @DisplayName("publishNews - 发布新闻")
    class PublishNewsTests {

        @Test
        @DisplayName("正常路径：成功发布新闻")
        void should_publish_news_successfully() {
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.updateStatus(1, 1)).thenReturn(true);

            Result result = newsService.publishNews(1, 1);

            assertThat(result.isSuccess()).isTrue();
            verify(newsDAO, times(1)).updateStatus(1, 1);
        }

        @Test
        @DisplayName("异常场景：新闻ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.publishNews(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.publishNews(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：新闻不存在应返回404")
        void should_return_404_when_news_not_found() {
            when(newsDAO.findById(999)).thenReturn(null);

            Result result = newsService.publishNews(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：发布失败应返回500")
        void should_return_500_when_publish_fails() {
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.updateStatus(1, 1)).thenReturn(false);

            Result result = newsService.publishNews(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("发布新闻失败");
        }
    }

    @Nested
    @DisplayName("unpublishNews - 取消发布新闻")
    class UnpublishNewsTests {

        @Test
        @DisplayName("正常路径：成功取消发布新闻")
        void should_unpublish_news_successfully() {
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.updateStatus(1, 0)).thenReturn(true);

            Result result = newsService.unpublishNews(1, 1);

            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("异常场景：新闻ID为空应返回错误")
        void should_return_error_when_id_is_null() {
            Result result = newsService.unpublishNews(null, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：操作者ID为空应返回错误")
        void should_return_error_when_operator_id_is_null() {
            Result result = newsService.unpublishNews(1, null);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(400);
        }

        @Test
        @DisplayName("异常场景：新闻不存在应返回404")
        void should_return_404_when_news_not_found() {
            when(newsDAO.findById(999)).thenReturn(null);

            Result result = newsService.unpublishNews(999, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(404);
        }

        @Test
        @DisplayName("异常场景：取消发布失败应返回500")
        void should_return_500_when_unpublish_fails() {
            when(newsDAO.findById(1)).thenReturn(testNews);
            when(newsDAO.updateStatus(1, 0)).thenReturn(false);

            Result result = newsService.unpublishNews(1, 1);

            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getCode()).isEqualTo(500);
            assertThat(result.getMessage()).contains("取消发布失败");
        }
    }
}
