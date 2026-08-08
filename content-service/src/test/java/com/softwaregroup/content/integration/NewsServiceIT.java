package com.softwaregroup.content.integration;

import com.softwaregroup.content.dao.NewsDAO;
import com.softwaregroup.content.dao.UserDAO;
import com.softwaregroup.content.model.News;
import com.softwaregroup.content.model.User;
import com.softwaregroup.content.model.dto.NewsDTO;
import com.softwaregroup.content.model.dto.NewsFilterDTO;
import com.softwaregroup.content.service.NewsService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NewsService 集成测试
 *
 * 测试新闻服务的核心功能：新闻CRUD、发布管理
 */
@ExtendWith(MockitoExtension.class)
class NewsServiceIT {

    @Mock
    private NewsDAO newsDAO;

    @Mock
    private UserDAO userDAO;

    private NewsService newsService;

    @BeforeEach
    void setUp() {
        newsService = new NewsService(newsDAO, userDAO);
    }

    @Test
    void listNews_withValidParams_shouldReturnNewsList() {
        News news = new News();
        news.setId(1);
        news.setTitle("测试新闻");
        when(newsDAO.findByConditions(isNull(), isNull(), eq(1))).thenReturn(Arrays.asList(news));

        Result result = newsService.listNews(null, 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(newsDAO).findByConditions(isNull(), isNull(), eq(1));
    }

    @Test
    void listNews_withKeywordFilter_shouldReturnFilteredList() {
        NewsFilterDTO filter = new NewsFilterDTO();
        filter.setKeyword("测试");
        when(newsDAO.findByConditions(eq("测试"), isNull(), eq(1))).thenReturn(Arrays.asList());

        Result result = newsService.listNews(filter, 1, 20);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void listNews_withInvalidPage_shouldReturnError() {
        Result result = newsService.listNews(null, 0, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void listNews_withInvalidPageSize_shouldReturnError() {
        Result result = newsService.listNews(null, 1, 200);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getNewsDetail_withValidId_shouldReturnNews() {
        News news = new News();
        news.setId(1);
        news.setTitle("测试新闻");
        news.setStatus(1);
        when(newsDAO.findById(1)).thenReturn(news);

        Result result = newsService.getNewsDetail(1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void getNewsDetail_withNullId_shouldReturnError() {
        Result result = newsService.getNewsDetail(null);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void getNewsDetail_withNonExistentId_shouldReturnError() {
        when(newsDAO.findById(9999)).thenReturn(null);

        Result result = newsService.getNewsDetail(9999);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(404);
    }

    @Test
    void createNews_withValidData_shouldReturnSuccess() {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("新新闻");
        dto.setType("通知");
        dto.setSummary("新闻摘要");

        User author = new User();
        author.setId(1);
        when(userDAO.findById(1)).thenReturn(author);
        when(newsDAO.insert(any(News.class))).thenReturn(true);

        Result result = newsService.createNews(dto, 1);

        assertThat(result.isSuccess()).isTrue();
        verify(newsDAO).insert(any(News.class));
    }

    @Test
    void createNews_withEmptyTitle_shouldReturnError() {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("");
        dto.setType("通知");

        Result result = newsService.createNews(dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createNews_withTooLongTitle_shouldReturnError() {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("a".repeat(300));
        dto.setType("通知");

        Result result = newsService.createNews(dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void createNews_withNullType_shouldReturnError() {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("测试新闻");
        dto.setType("");

        Result result = newsService.createNews(dto, 1);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    void deleteNews_withValidId_shouldReturnSuccess() {
        News news = new News();
        news.setId(1);
        news.setStatus(1);
        when(newsDAO.findById(1)).thenReturn(news);
        when(newsDAO.updateStatus(1, 0)).thenReturn(true);

        Result result = newsService.deleteNews(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void publishNews_withValidId_shouldReturnSuccess() {
        News news = new News();
        news.setId(1);
        when(newsDAO.findById(1)).thenReturn(news);
        when(newsDAO.updateStatus(1, 1)).thenReturn(true);

        Result result = newsService.publishNews(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void unpublishNews_withValidId_shouldReturnSuccess() {
        News news = new News();
        news.setId(1);
        when(newsDAO.findById(1)).thenReturn(news);
        when(newsDAO.updateStatus(1, 0)).thenReturn(true);

        Result result = newsService.unpublishNews(1, 1);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void getNewsByType_withValidType_shouldReturnNewsList() {
        News news = new News();
        news.setId(1);
        news.setType("通知");
        when(newsDAO.findByType("通知")).thenReturn(Arrays.asList(news));

        Result result = newsService.getNewsByType("通知", 1, 20);

        assertThat(result.isSuccess()).isTrue();
        verify(newsDAO).findByType("通知");
    }

    @Test
    void getNewsByType_withNullType_shouldReturnError() {
        Result result = newsService.getNewsByType(null, 1, 20);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getCode()).isEqualTo(400);
    }
}
