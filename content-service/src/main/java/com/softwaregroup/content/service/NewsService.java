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

import java.util.Date;
import java.util.List;

/**
 * 新闻服务层
 */
@Service
public class NewsService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_TITLE_LENGTH = 256;
    private static final int MAX_SUMMARY_LENGTH = 500;

    @Autowired
    private NewsDAO newsDAO;

    @Autowired
    private UserDAO userDAO;

    public NewsService() {
    }

    public NewsService(NewsDAO newsDAO, UserDAO userDAO) {
        this.newsDAO = newsDAO;
        this.userDAO = userDAO;
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

        List<News> news = newsDAO.findByType(type);
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

        boolean inserted = newsDAO.insert(news);
        if (!inserted) {
            return Result.error(500, "创建新闻失败");
        }

        return Result.ok(news);
    }

    private Result validateNewsCreation(NewsDTO dto, Integer authorId) {
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
        if (dto.getSummary() != null && dto.getSummary().length() > MAX_SUMMARY_LENGTH) {
            return Result.error(400, "新闻摘要不能超过" + MAX_SUMMARY_LENGTH + "字符");
        }
        return null;
    }

    private News buildNewsFromDTO(NewsDTO dto, Integer authorId) {
        News news = new News();
        news.setTitle(dto.getTitle().trim());
        news.setType(dto.getType());
        news.setSummary(dto.getSummary());
        news.setAuthorId(authorId);
        news.setStatus(News.STATUS_UNPUBLISHED);
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
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            return Result.error(400, "新闻类型不能为空");
        }

        news.setTitle(dto.getTitle().trim());
        news.setType(dto.getType());
        news.setSummary(dto.getSummary());
        news.setUpdatedAt(new Date());

        boolean updated = newsDAO.update(news);
        if (!updated) {
            return Result.error(500, "更新新闻失败");
        }

        return Result.ok(news);
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
}
