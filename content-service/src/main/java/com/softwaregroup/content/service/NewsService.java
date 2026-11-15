package com.softwaregroup.content.service;

import com.softwaregroup.content.dao.NewsDAO;
import com.softwaregroup.content.dao.UserDAO;
import com.softwaregroup.content.model.News;
import com.softwaregroup.content.model.User;
import com.softwaregroup.content.model.dto.NewsDTO;
import com.softwaregroup.content.model.dto.NewsFilterDTO;
import com.softwaregroup.common.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 新闻服务层
 */
@Service
public class NewsService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_SUMMARY_LENGTH = 500;

    @Autowired
    private NewsDAO newsDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private NewsContentStorage contentStorage;

    public NewsService() {
    }

    public NewsService(NewsDAO newsDAO, UserDAO userDAO) {
        this.newsDAO = newsDAO;
        this.userDAO = userDAO;
    }

    public NewsService(NewsDAO newsDAO, UserDAO userDAO, NewsContentStorage contentStorage) {
        this(newsDAO, userDAO);
        this.contentStorage = contentStorage;
    }

    public Result listNews(NewsFilterDTO filter, int page, int pageSize) {
        Result validationResult = validatePaginationParams(page, pageSize);
        if (validationResult != null) {
            return validationResult;
        }

        String keyword = filter != null ? filter.getKeyword() : null;
        String type = filter != null ? filter.getType() : null;

        List<News> news = newsDAO.findByConditions(keyword, type, 1);
        return Result.ok(news);
    }

    public Result getNewsByType(String type, int page, int pageSize) {
        if (type == null || type.trim().isEmpty()) {
            return Result.error(400, "新闻类型不能为空");
        }
        Result validationResult = validatePaginationParams(page, pageSize);
        if (validationResult != null) {
            return validationResult;
        }

        List<News> news = newsDAO.findByConditions(null, type, 1);
        return Result.ok(news);
    }

    public Result getNewsDetail(Integer newsId) {
        if (newsId == null) {
            return Result.error(400, "新闻ID不能为空");
        }

        News news = newsDAO.findById(newsId);
        if (news == null) {
            return Result.error(404, "新闻不存在");
        }
        if (news.getStatus() == null || news.getStatus() != 1) {
            return Result.error(404, "新闻不存在");
        }

        if (news.getContentPath() != null) {
            try {
                news.setContent(contentStorage.read(news.getContentPath()));
            } catch (IOException | IllegalArgumentException e) {
                return Result.error(500, "新闻正文读取失败");
            }
        }
        return Result.ok(news);
    }

    public Result createNews(NewsDTO dto, Integer authorId) {
        Result validationResult = validateNewsCreation(dto, authorId);
        if (validationResult != null) {
            return validationResult;
        }

        User user = userDAO.findById(authorId);
        if (user == null) {
            return Result.error(404, "作者不存在");
        }

        News news = buildNewsFromDTO(dto, authorId);
        String generatedPath = null;
        try {
            if (dto.getContent() != null) {
                generatedPath = contentStorage.save(dto.getType(), dto.getContent());
                news.setContentPath(generatedPath);
            }
            if (!newsDAO.insert(news)) {
                return Result.error(500, "创建新闻失败");
            }
            generatedPath = null;
            return Result.ok(news);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (IOException | RuntimeException e) {
            return Result.error(500, "创建新闻失败");
        } finally {
            deleteUncommittedContent(generatedPath);
        }
    }

    private Result validateNewsCreation(NewsDTO dto, Integer authorId) {
        return validateNewsInput(dto, authorId, true);
    }

    private Result validateNewsInput(NewsDTO dto, Integer authorId, boolean requireContentPath) {
        if (dto == null) {
            return Result.error(400, "新闻信息不能为空");
        }
        if (authorId == null) {
            return Result.error(400, "作者ID不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(400, "新闻标题不能为空");
        }
        if (dto.getTitle().length() > MAX_TITLE_LENGTH) {
            return Result.error(400, "新闻标题不能超过" + MAX_TITLE_LENGTH + "字符");
        }
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            return Result.error(400, "新闻类型不能为空");
        }
        if (!isValidType(dto.getType())) {
            return Result.error(400, "新闻类型无效");
        }
        boolean hasContent = dto.getContent() != null;
        boolean hasPath = dto.getContentPath() != null;
        if (requireContentPath && !hasContent && !hasPath) {
            return Result.error(400, "新闻正文或已存储内容路径不能为空");
        }
        if (hasContent && hasPath) {
            return Result.error(400, "新闻正文与内容路径只能提供一个");
        }
        if (hasContent && (dto.getContent().isBlank()
                || dto.getContent().getBytes(StandardCharsets.UTF_8).length > 1024 * 1024)) {
            return Result.error(400, "新闻正文不能为空且不能超过 1 MB");
        }
        if (dto.getContentPath() != null && (dto.getContentPath().isBlank()
                || dto.getContentPath().length() > 255)) {
            return Result.error(400, "新闻内容路径不能超过255字符");
        }
        if (hasPath && !contentStorage.exists(dto.getContentPath())) {
            return Result.error(400, "新闻内容路径不存在或无效");
        }
        if (dto.getSummary() != null && dto.getSummary().length() > MAX_SUMMARY_LENGTH) {
            return Result.error(400, "新闻摘要不能超过" + MAX_SUMMARY_LENGTH + "字符");
        }
        return null;
    }

    private News buildNewsFromDTO(NewsDTO dto, Integer authorId) {
        News news = new News();
        news.setTitle(dto.getTitle().trim());
        news.setType(dto.getType().toLowerCase(java.util.Locale.ROOT));
        news.setContentPath(dto.getContentPath());
        news.setSummary(dto.getSummary());
        news.setAuthorId(authorId);
        news.setStatus(News.STATUS_PUBLISHED);
        news.setCreatedAt(new Date());
        return news;
    }

    public Result updateNews(Integer newsId, NewsDTO dto, Integer operatorId) {
        Result validationResult = validateNewsUpdateInput(newsId, dto, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        News news = newsDAO.findById(newsId);
        if (news == null) {
            return Result.error(404, "新闻不存在");
        }
        if (news.getStatus() == null || news.getStatus() != 1) {
            return Result.error(404, "新闻不存在");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.error(400, "新闻标题不能为空");
        }
        Result contentValidation = validateNewsInput(dto, operatorId, false);
        if (contentValidation != null) return contentValidation;

        news.setTitle(dto.getTitle().trim());
        news.setType(dto.getType().toLowerCase(java.util.Locale.ROOT));
        news.setSummary(dto.getSummary());
        if (dto.getContentPath() != null) news.setContentPath(dto.getContentPath());
        news.setUpdatedAt(new Date());
        String generatedPath = null;
        try {
            if (dto.getContent() != null) {
                generatedPath = contentStorage.save(dto.getType(), dto.getContent());
                news.setContentPath(generatedPath);
            }
            if (!newsDAO.update(news)) {
                return Result.error(500, "更新新闻失败");
            }
            generatedPath = null;
            return Result.ok(news);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (IOException | RuntimeException e) {
            return Result.error(500, "更新新闻失败");
        } finally {
            deleteUncommittedContent(generatedPath);
        }
    }

    private Result validateNewsUpdateInput(Integer newsId, NewsDTO dto, Integer operatorId) {
        if (newsId == null) {
            return Result.error(400, "新闻ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "新闻信息不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    public Result deleteNews(Integer newsId, Integer operatorId) {
        Result validationResult = validateNewsOperation(newsId, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        News news = newsDAO.findById(newsId);
        if (news == null) {
            return Result.error(404, "新闻不存在");
        }
        if (news.getStatus() == null || news.getStatus() != 1) {
            return Result.error(404, "新闻不存在");
        }

        boolean deleted = newsDAO.updateStatus(newsId, 0);
        if (!deleted) {
            return Result.error(500, "删除新闻失败");
        }

        return Result.ok();
    }

    public Result publishNews(Integer newsId, Integer operatorId) {
        Result validationResult = validateNewsOperation(newsId, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        News news = newsDAO.findById(newsId);
        if (news == null) {
            return Result.error(404, "新闻不存在");
        }

        boolean published = newsDAO.updateStatus(newsId, 1);
        if (!published) {
            return Result.error(500, "发布新闻失败");
        }

        return Result.ok();
    }

    public Result unpublishNews(Integer newsId, Integer operatorId) {
        Result validationResult = validateNewsOperation(newsId, operatorId);
        if (validationResult != null) {
            return validationResult;
        }

        News news = newsDAO.findById(newsId);
        if (news == null) {
            return Result.error(404, "新闻不存在");
        }

        boolean unpublished = newsDAO.updateStatus(newsId, 0);
        if (!unpublished) {
            return Result.error(500, "取消发布失败");
        }

        return Result.ok();
    }

    private Result validatePaginationParams(int page, int pageSize) {
        if (page <= 0) {
            return Result.error(400, "页码必须大于0");
        }
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return Result.error(400, "每页数量必须在1-100之间");
        }
        return null;
    }

    private Result validateNewsOperation(Integer newsId, Integer operatorId) {
        if (newsId == null) {
            return Result.error(400, "新闻ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    private boolean isValidType(String type) {
        String normalized = type.toLowerCase(java.util.Locale.ROOT);
        return "award".equals(normalized) || "activity".equals(normalized) || "notice".equals(normalized);
    }

    private void deleteUncommittedContent(String path) {
        if (path == null) return;
        try {
            contentStorage.delete(path);
        } catch (IOException ignored) {
            // The database was not updated; a failed cleanup may leave an orphan file.
        }
    }
}
