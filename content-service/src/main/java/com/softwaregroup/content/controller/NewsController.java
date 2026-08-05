package com.softwaregroup.content.controller;

import com.softwaregroup.content.model.dto.NewsDTO;
import com.softwaregroup.content.model.dto.NewsFilterDTO;
import com.softwaregroup.content.service.NewsService;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 新闻管理 REST API
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping
    public Result listNews(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String type,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int pageSize) {
        NewsFilterDTO filter = new NewsFilterDTO();
        filter.setKeyword(keyword);
        filter.setType(type);
        return newsService.listNews(filter, page, pageSize);
    }

    @GetMapping("/type/{type}")
    public Result getNewsByType(@PathVariable String type,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int pageSize) {
        return newsService.getNewsByType(type, page, pageSize);
    }

    @GetMapping("/{newsId}")
    public Result getNewsDetail(@PathVariable Integer newsId) {
        return newsService.getNewsDetail(newsId);
    }

    @PostMapping
    public Result createNews(@RequestBody NewsDTO dto,
                            @RequestParam Integer authorId) {
        return newsService.createNews(dto, authorId);
    }

    @PutMapping("/{newsId}")
    public Result updateNews(@PathVariable Integer newsId,
                            @RequestBody NewsDTO dto,
                            @RequestParam Integer operatorId) {
        return newsService.updateNews(newsId, dto, operatorId);
    }

    @DeleteMapping("/{newsId}")
    public Result deleteNews(@PathVariable Integer newsId,
                            @RequestParam Integer operatorId) {
        return newsService.deleteNews(newsId, operatorId);
    }

    @PutMapping("/{newsId}/publish")
    public Result publishNews(@PathVariable Integer newsId,
                             @RequestParam Integer operatorId) {
        return newsService.publishNews(newsId, operatorId);
    }

    @PutMapping("/{newsId}/unpublish")
    public Result unpublishNews(@PathVariable Integer newsId,
                               @RequestParam Integer operatorId) {
        return newsService.unpublishNews(newsId, operatorId);
    }
}
