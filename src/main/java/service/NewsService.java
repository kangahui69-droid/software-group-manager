package service;

import dao.FileStorageDAO;
import dao.NewsDAO;
import dao.UserDAO;
import dto.NewsDTO;
import dto.NewsFilterDTO;
import model.News;
import model.User;
import util.Result;

import java.util.List;

/**
 * 新闻服务
 *
 * 服务分层与API化完整计划.md 5.1 NewsService 新闻服务
 */
public class NewsService {

    // ==================== 常量定义 ====================
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_TITLE_LENGTH = 256;
    private static final int MAX_SUMMARY_LENGTH = 500;

    private static final Integer STATUS_PUBLISHED = 1;
    private static final Integer STATUS_DELETED = 0;

    // ==================== 依赖注入 ====================
    private final NewsDAO newsDAO;
    private final UserDAO userDAO;
    private final FileStorageDAO fileStorageDAO;

    public NewsService() {
        this.newsDAO = new NewsDAO();
        this.userDAO = new UserDAO();
        this.fileStorageDAO = new FileStorageDAO();
    }

    public NewsService(NewsDAO newsDAO, UserDAO userDAO, FileStorageDAO fileStorageDAO) {
        this.newsDAO = newsDAO;
        this.userDAO = userDAO;
        this.fileStorageDAO = fileStorageDAO;
    }

    // ==================== 公开业务方法 ====================

    public Result listNews(NewsFilterDTO filter, int page, int pageSize) {
        Result validation = validatePageParams(page, pageSize);
        if (validation != null) {
            return validation;
        }

        String keyword = extractFilterKeyword(filter);
        String type = extractFilterType(filter);
        Integer status = extractFilterStatus(filter);

        List<News> newsList = newsDAO.findByConditions(keyword, type, status);
        return Result.ok(newsList);
    }

    public Result getNewsByType(String type, int page, int pageSize) {
        Result validation = validateTypeAndPageParams(type, page, pageSize);
        if (validation != null) {
            return validation;
        }

        List<News> newsList = newsDAO.findByType(type);
        return Result.ok(newsList);
    }

    public Result getNewsDetail(Integer id) {
        if (id == null) {
            return Result.error(400, "新闻ID不能为空");
        }

        News news = newsDAO.findById(id);
        if (!isNewsExistingAndActive(news)) {
            return Result.error(404, "新闻不存在");
        }

        return Result.ok(news);
    }

    public Result createNews(NewsDTO dto, Integer authorId) {
        Result validation = validateCreateParams(dto, authorId);
        if (validation != null) {
            return validation;
        }

        if (!isAuthorExists(authorId)) {
            return Result.error(404, "作者不存在");
        }

        News news = buildNewsFromDTO(dto, authorId);
        return insertNews(news);
    }

    public Result updateNews(Integer id, NewsDTO dto, Integer operatorId) {
        Result validation = validateUpdateParams(id, dto, operatorId);
        if (validation != null) {
            return validation;
        }

        News existingNews = findActiveNewsOrFail(id);
        if (existingNews == null) {
            return Result.error(404, "新闻不存在");
        }
        applyDTOToNews(existingNews, dto);

        return updateNewsAndReturn(existingNews);
    }

    public Result deleteNews(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperatorId(id, operatorId);
        if (validation != null) {
            return validation;
        }

        News existingNews = findActiveNewsOrFail(id);
        if (existingNews == null) {
            return Result.error(404, "新闻不存在");
        }
        return updateStatusAndReturn(id, STATUS_DELETED, "删除新闻失败");
    }

    public Result publishNews(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperatorId(id, operatorId);
        if (validation != null) {
            return validation;
        }

        if (!isNewsExists(id)) {
            return Result.error(404, "新闻不存在");
        }

        return updateStatusAndReturn(id, STATUS_PUBLISHED, "发布新闻失败");
    }

    public Result unpublishNews(Integer id, Integer operatorId) {
        Result validation = validateIdAndOperatorId(id, operatorId);
        if (validation != null) {
            return validation;
        }

        if (!isNewsExists(id)) {
            return Result.error(404, "新闻不存在");
        }

        return updateStatusAndReturn(id, STATUS_DELETED, "取消发布失败");
    }

    // ==================== 验证方法 ====================

    private Result validatePageParams(int page, int pageSize) {
        if (page <= 0) {
            return Result.error(400, "页码必须大于0");
        }
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return Result.error(400, "每页数量必须在1-100之间");
        }
        return null;
    }

    private Result validateTypeAndPageParams(String type, int page, int pageSize) {
        if (type == null || type.trim().isEmpty()) {
            return Result.error(400, "新闻类型不能为空");
        }
        if (page <= 0) {
            return Result.error(400, "页码必须大于0");
        }
        if (pageSize <= 0) {
            return Result.error(400, "每页数量必须大于0");
        }
        return null;
    }

    private Result validateCreateParams(NewsDTO dto, Integer authorId) {
        if (dto == null) {
            return Result.error(400, "新闻信息不能为空");
        }
        if (isBlank(dto.getTitle())) {
            return Result.error(400, "新闻标题不能为空");
        }
        if (dto.getTitle().length() > MAX_TITLE_LENGTH) {
            return Result.error(400, "新闻标题不能超过256个字符");
        }
        if (isBlank(dto.getType())) {
            return Result.error(400, "新闻类型不能为空");
        }
        if (dto.getSummary() != null && dto.getSummary().length() > MAX_SUMMARY_LENGTH) {
            return Result.error(400, "新闻摘要不能超过500个字符");
        }
        if (authorId == null) {
            return Result.error(400, "作者ID不能为空");
        }
        return null;
    }

    private Result validateUpdateParams(Integer id, NewsDTO dto, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "新闻ID不能为空");
        }
        if (dto == null) {
            return Result.error(400, "新闻信息不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        if (isBlank(dto.getTitle())) {
            return Result.error(400, "新闻标题不能为空");
        }
        if (isBlank(dto.getType())) {
            return Result.error(400, "新闻类型不能为空");
        }
        return null;
    }

    private Result validateIdAndOperatorId(Integer id, Integer operatorId) {
        if (id == null) {
            return Result.error(400, "新闻ID不能为空");
        }
        if (operatorId == null) {
            return Result.error(400, "操作者ID不能为空");
        }
        return null;
    }

    // ==================== 辅助方法 ====================

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isNewsExistingAndActive(News news) {
        return news != null && news.getStatus() != null && news.getStatus() == STATUS_PUBLISHED;
    }

    private boolean isNewsExists(Integer id) {
        News news = newsDAO.findById(id);
        return news != null;
    }

    private News findActiveNewsOrFail(Integer id) {
        News news = newsDAO.findById(id);
        if (!isNewsExistingAndActive(news)) {
            return null;
        }
        return news;
    }

    private boolean isAuthorExists(Integer authorId) {
        User author = userDAO.findById(authorId);
        return author != null;
    }

    private String extractFilterKeyword(NewsFilterDTO filter) {
        if (filter == null) {
            return null;
        }
        String keyword = filter.getKeyword();
        return (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
    }

    private String extractFilterType(NewsFilterDTO filter) {
        return filter != null ? filter.getType() : null;
    }

    private Integer extractFilterStatus(NewsFilterDTO filter) {
        return filter != null ? filter.getStatus() : null;
    }

    private News buildNewsFromDTO(NewsDTO dto, Integer authorId) {
        News news = new News();
        news.setTitle(dto.getTitle().trim());
        news.setType(dto.getType());
        news.setSummary(trimToNull(dto.getSummary()));
        news.setAuthorId(authorId);
        news.setStatus(STATUS_PUBLISHED);
        news.setContentPath(buildContentPath(dto.getType()));
        return news;
    }

    private void applyDTOToNews(News news, NewsDTO dto) {
        news.setTitle(dto.getTitle().trim());
        news.setType(dto.getType());
        news.setSummary(trimToNull(dto.getSummary()));
    }

    private String trimToNull(String str) {
        return str != null ? str.trim() : null;
    }

    private String buildContentPath(String type) {
        String fileName = System.currentTimeMillis() + "_" + type + ".html";
        return "localstorage/news/" + type + "/" + fileName;
    }

    private Result insertNews(News news) {
        boolean inserted = newsDAO.insert(news);
        if (!inserted) {
            return Result.error(500, "创建新闻失败");
        }
        return Result.ok(news);
    }

    private Result updateNewsAndReturn(News news) {
        boolean updated = newsDAO.update(news);
        if (!updated) {
            return Result.error(500, "更新新闻失败");
        }
        return Result.ok(news);
    }

    private Result updateStatusAndReturn(Integer id, Integer status, String errorMessage) {
        boolean updated = newsDAO.updateStatus(id, status);
        if (!updated) {
            return Result.error(500, errorMessage);
        }
        return Result.ok();
    }
}
