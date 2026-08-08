package com.softwaregroup.content.controller;

import com.softwaregroup.content.model.dto.NewsDTO;
import com.softwaregroup.content.model.dto.NewsFilterDTO;
import com.softwaregroup.content.service.NewsService;
import com.softwaregroup.common.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * NewsController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsController newsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(newsController).build();
    }

    @Test
    void listNews_shouldReturnNewsList() throws Exception {
        NewsDTO news = new NewsDTO();
        news.setId(1);
        news.setTitle("测试新闻");

        when(newsService.listNews(any(NewsFilterDTO.class), eq(1), eq(20))).thenReturn(Result.ok(List.of(news)));

        mockMvc.perform(get("/api/news")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].title").value("测试新闻"));
    }

    @Test
    void listNews_withKeyword_shouldFilterByKeyword() throws Exception {
        when(newsService.listNews(any(NewsFilterDTO.class), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/news")
                        .param("keyword", "测试")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listNews_withType_shouldFilterByType() throws Exception {
        when(newsService.listNews(any(NewsFilterDTO.class), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/news")
                        .param("type", "announcement")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void listNews_withDefaultPagination_shouldUseDefaults() throws Exception {
        when(newsService.listNews(any(NewsFilterDTO.class), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getNewsByType_shouldReturnNewsList() throws Exception {
        when(newsService.getNewsByType(eq("announcement"), eq(1), eq(20))).thenReturn(Result.ok(List.of()));

        mockMvc.perform(get("/api/news/type/announcement")
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void getNewsDetail_withValidId_shouldReturnNews() throws Exception {
        NewsDTO news = new NewsDTO();
        news.setId(1);
        news.setTitle("测试新闻");
        news.setContent("测试内容");

        when(newsService.getNewsDetail(eq(1))).thenReturn(Result.ok(news));

        mockMvc.perform(get("/api/news/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("测试新闻"))
                .andExpect(jsonPath("$.data.content").value("测试内容"));
    }

    @Test
    void getNewsDetail_withInvalidId_shouldReturn404() throws Exception {
        when(newsService.getNewsDetail(eq(999))).thenReturn(Result.error(404, "新闻不存在"));

        mockMvc.perform(get("/api/news/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void createNews_withValidData_shouldReturnSuccess() throws Exception {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("新新闻");
        dto.setType("announcement");

        when(newsService.createNews(any(NewsDTO.class), eq(1))).thenReturn(Result.ok(Map.of("id", 1)));

        mockMvc.perform(post("/api/news")
                        .param("authorId", "1")
                        .contentType("application/json")
                        .content("{\"title\":\"新新闻\",\"type\":\"announcement\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void createNews_withInvalidData_shouldReturn400() throws Exception {
        when(newsService.createNews(any(NewsDTO.class), eq(1))).thenReturn(Result.error(400, "标题不能为空"));

        mockMvc.perform(post("/api/news")
                        .param("authorId", "1")
                        .contentType("application/json")
                        .content("{\"title\":\"\",\"type\":\"announcement\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void updateNews_withValidData_shouldReturnSuccess() throws Exception {
        NewsDTO dto = new NewsDTO();
        dto.setTitle("更新后的新闻");

        when(newsService.updateNews(eq(1), any(NewsDTO.class), eq(1))).thenReturn(Result.ok(dto));

        mockMvc.perform(put("/api/news/1")
                        .param("operatorId", "1")
                        .contentType("application/json")
                        .content("{\"title\":\"更新后的新闻\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void updateNews_withNotFound_shouldReturn404() throws Exception {
        when(newsService.updateNews(eq(999), any(NewsDTO.class), eq(1))).thenReturn(Result.error(404, "新闻不存在"));

        mockMvc.perform(put("/api/news/999")
                        .param("operatorId", "1")
                        .contentType("application/json")
                        .content("{\"title\":\"更新后的新闻\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteNews_shouldReturnSuccess() throws Exception {
        when(newsService.deleteNews(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(delete("/api/news/1")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void deleteNews_withNotFound_shouldReturn404() throws Exception {
        when(newsService.deleteNews(eq(999), eq(1))).thenReturn(Result.error(404, "新闻不存在"));

        mockMvc.perform(delete("/api/news/999")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void publishNews_shouldReturnSuccess() throws Exception {
        when(newsService.publishNews(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/news/1/publish")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void publishNews_withNotFound_shouldReturn404() throws Exception {
        when(newsService.publishNews(eq(999), eq(1))).thenReturn(Result.error(404, "新闻不存在"));

        mockMvc.perform(put("/api/news/999/publish")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void unpublishNews_shouldReturnSuccess() throws Exception {
        when(newsService.unpublishNews(eq(1), eq(1))).thenReturn(Result.ok(null));

        mockMvc.perform(put("/api/news/1/unpublish")
                        .param("operatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
