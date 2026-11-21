package servlet;

import dao.NewsDAO;
import dao.NewsAssetDAO;
import model.News;
import model.NewsAsset;
import model.User;
import service.NewsMediaService;
import util.AuthHelper;
import util.FileUtil;
import util.HtmlSanitizer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * 新闻Servlet
 */
public class NewsServlet extends HttpServlet {
    private NewsDAO newsDAO = new NewsDAO();
    private NewsAssetDAO assetDAO = new NewsAssetDAO();
    private NewsMediaService mediaStorage = new NewsMediaService();
    private static final Pattern MEDIA_URL = Pattern.compile("/news/media/([1-9][0-9]*)$");
    private static final Pattern OWNED_HTML = Pattern.compile(
            "localstorage/news/(notice|award|activity)/[0-9]+_(notice|award|activity)\\.html");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                listNews(request, response);
                break;
            case "manage":
                manageNews(request, response);
                break;
            case "detail":
                showDetail(request, response);
                break;
            case "edit":
                showEditForm(request, response);
                break;
            default:
                listNews(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 使用统一权限检查工具
        if (!AuthHelper.checkAdmin(request, response)) {
            return;
        }

        User user = AuthHelper.getCurrentUser(request);
        request.setCharacterEncoding("UTF-8");
        request.setAttribute("mediaToken", request.getParameter("mediaToken"));

        String action = request.getParameter("action");
        if (action != null) {
            action = action.trim();
        }

        if ("create".equals(action)) {
            createNews(request, response, user.getId());
        } else if ("update".equals(action)) {
            updateNews(request, response);
        } else if ("delete".equals(action)) {
            deleteNews(request, response);
        } else if ("hardDelete".equals(action)) {
            hardDeleteNews(request, response);
        } else if ("publish".equals(action)) {
            publishNews(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/news?action=manage");
        }
    }

    /**
     * 列表新闻
     */
    private void listNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String type = request.getParameter("type");
        List<News> newsList;

        if (type != null && !type.isEmpty()) {
            newsList = newsDAO.findByType(type);
        } else {
            newsList = newsDAO.findByConditions(null, null, 1);
        }

        request.setAttribute("newsList", newsList);
        request.getRequestDispatcher("/jsp/news/list.jsp").forward(request, response);
    }

    /**
     * 管理新闻
     */
    private void manageNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 使用统一权限检查工具
        if (!AuthHelper.checkAdmin(request, response)) {
            return;
        }

        String keyword = request.getParameter("keyword");
        String type = request.getParameter("type");
        String statusStr = request.getParameter("status");
        Integer status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Integer.parseInt(statusStr);
            } catch (NumberFormatException e) {
            }
        }

        List<News> newsList = newsDAO.findByConditions(keyword, type, status);
        request.setAttribute("newsList", newsList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("type", type);
        request.setAttribute("status", statusStr);
        request.getRequestDispatcher("/admin/news/manage.jsp").forward(request, response);
    }

    /**
     * 显示编辑表单
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 使用统一权限检查工具
        if (!AuthHelper.checkAdmin(request, response)) {
            return;
        }

        String idStr = request.getParameter("id");
        request.setAttribute("mediaToken", UUID.randomUUID().toString());
        if (idStr != null && !idStr.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idStr);
                News news = newsDAO.findById(id);
                if (news != null) {
                    String realPath = FileUtil.resolvePhysicalPath("/" + news.getContentPath());
                    File htmlFile = new File(realPath);
                    if (!htmlFile.exists()) {
                        String legacyPath = getServletContext().getRealPath("/" + news.getContentPath());
                        File legacyFile = new File(legacyPath);
                        if (legacyFile.exists()) {
                            htmlFile = legacyFile;
                        }
                    }
                    String content = "";
                    if (htmlFile.exists()) {
                        content = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()), "UTF-8");
                    }
                    request.setAttribute("news", news);
                    request.setAttribute("content", content);
                    request.setAttribute("attachments", assetDAO.forNews(id, "ATTACHMENT"));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        request.getRequestDispatcher("/admin/news/edit.jsp").forward(request, response);
    }

    /**
     * 显示详情
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            News news = newsDAO.findById(id);
            if (news == null || (!Integer.valueOf(1).equals(news.getStatus()) && !AuthHelper.isAdmin(request))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 读取HTML内容
            String realPath = FileUtil.resolvePhysicalPath("/" + news.getContentPath());
            File htmlFile = new File(realPath);
            if (!htmlFile.exists()) {
                String legacyPath = getServletContext().getRealPath("/" + news.getContentPath());
                File legacyFile = new File(legacyPath);
                if (legacyFile.exists()) {
                    htmlFile = legacyFile;
                }
            }
            String content = "";
            if (htmlFile.exists()) {
                content = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()), "UTF-8");
            }

            request.setAttribute("news", news);
            request.setAttribute("content", content);
            request.setAttribute("attachments", assetDAO.forNews(id, "ATTACHMENT"));
            request.getRequestDispatcher("/jsp/news/detail.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * 创建新闻
     */
    private void createNews(HttpServletRequest request, HttpServletResponse response, Integer authorId)
            throws ServletException, IOException {
        String title = request.getParameter("title");
        String type = request.getParameter("type");
        String content = request.getParameter("content");
        String summary = request.getParameter("summary");
        String statusStr = request.getParameter("status");
        if (!isValidType(type)) { response.sendError(400); return; }

        // ===== XSS防护：净化用户输入 =====
        title = HtmlSanitizer.sanitizeBasic(title);
        summary = HtmlSanitizer.sanitizeBasic(summary);
        content = HtmlSanitizer.sanitizeRichText(content);
        // ==================================

        // 解析status，默认为1（正常）
        int status = 1;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = Integer.parseInt(statusStr);
                // 校验status值必须在有效范围内
                if (status != 0 && status != 1) {
                    status = 1; // 无效值默认为正常
                }
            } catch (NumberFormatException e) {
                status = 1; // 解析失败默认为正常
            }
        }

        try {
            List<Integer> images = imageIds(content);
            List<Integer> attachments = attachmentIds(request);
            assetDAO.validate(null, authorId, mediaToken(request), images, attachments);
            String fileName = System.currentTimeMillis() + "_" + type + ".html";
            String relativePath = "localstorage/news/" + type + "/" + fileName;
            String realPath = FileUtil.resolvePhysicalPath("/" + relativePath);

            File htmlFile = new File(realPath);
            FileUtil.ensureDirectoryExists(htmlFile.getParent());

            // 保存HTML内容（已净化）
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(htmlFile), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(content);
            }

            // 保存到数据库
            News news = new News();
            news.setTitle(title);
            news.setType(type);
            news.setContentPath(relativePath);
            news.setSummary(summary);
            news.setAuthorId(authorId);
            news.setStatus(0);

            if (newsDAO.insert(news)) {
                assetDAO.sync(news.getId(), authorId, mediaToken(request), images, attachments);
                if (status == 1 && !newsDAO.updateStatus(news.getId(), 1)) {
                    throw new IllegalStateException("新闻媒体已保存，但发布状态更新失败");
                }
                response.sendRedirect(request.getContextPath() + "/news?action=manage");
            } else {
                request.setAttribute("error", "创建失败");
                request.getRequestDispatcher("/admin/news/edit.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "创建失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/news/edit.jsp").forward(request, response);
        }
    }

    /**
     * 更新新闻
     */
    private void updateNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String title = request.getParameter("title");
        String type = request.getParameter("type");
        String content = request.getParameter("content");
        String summary = request.getParameter("summary");
        String statusStr = request.getParameter("status");
        if (!isValidType(type)) { response.sendError(400); return; }

        // ===== XSS防护：净化用户输入 =====
        title = HtmlSanitizer.sanitizeBasic(title);
        summary = HtmlSanitizer.sanitizeBasic(summary);
        content = HtmlSanitizer.sanitizeRichText(content);
        // ==================================

        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idStr);
            News news = newsDAO.findById(id);
            if (news == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            List<Integer> images = imageIds(content);
            List<Integer> attachments = attachmentIds(request);
            assetDAO.validate(id, AuthHelper.getCurrentUser(request).getId(), mediaToken(request), images, attachments);

            // 更新HTML内容（已净化）
            String realPath = FileUtil.resolvePhysicalPath("/" + news.getContentPath());

            File htmlFile = new File(realPath);
            FileUtil.ensureDirectoryExists(htmlFile.getParent());
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(htmlFile), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(content);
            }

            news.setTitle(title);
            news.setType(type);
            news.setSummary(summary);

            // 解析并校验status值
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    int status = Integer.parseInt(statusStr);
                    // 只接受有效的status值（0或1）
                    if (status == 0 || status == 1) {
                        news.setStatus(status);
                    }
                    // 无效值不更新，保持原值
                } catch (NumberFormatException e) {
                    // 解析失败，不更新，保持原值
                }
            }

            if (newsDAO.update(news)) {
                assetDAO.sync(id, AuthHelper.getCurrentUser(request).getId(), mediaToken(request), images, attachments);
                response.sendRedirect(request.getContextPath() + "/news?action=manage");
            } else {
                request.setAttribute("news", news);
                request.setAttribute("content", content);
                request.setAttribute("error", "更新失败");
                request.getRequestDispatcher("/admin/news/edit.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "更新失败：" + e.getMessage());
            request.getRequestDispatcher("/admin/news/edit.jsp").forward(request, response);
        }
    }

    /**
     * 删除新闻
     */
    private void deleteNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                newsDAO.updateStatus(id, 0);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/news?action=manage");
    }

    /** Permanently removes only a soft-deleted article and its dedicated files. */
    private void hardDeleteNews(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
            if (id <= 0) throw new NumberFormatException("Invalid news ID");
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        NewsDAO.PurgedNews purged = newsDAO.purgeDeleted(id);
        String outcome = "unavailable";
        if (purged != null) {
            boolean filesRemoved = removePurgedFiles(purged);
            outcome = filesRemoved ? "done" : "files-pending";
        }
        response.sendRedirect(request.getContextPath() + "/news?action=manage&status=0&purge=" + outcome);
    }

    private boolean removePurgedFiles(NewsDAO.PurgedNews purged) {
        boolean removed = true;
        for (String name : purged.getStoredNames()) {
            try {
                NewsAsset asset = new NewsAsset();
                asset.setStoredName(name);
                Files.deleteIfExists(mediaStorage.path(asset));
            } catch (IOException | RuntimeException e) {
                removed = false;
                getServletContext().log("新闻媒体文件清理失败: " + name, e);
            }
        }
        String contentPath = purged.getContentPath();
        if (contentPath != null && OWNED_HTML.matcher(contentPath).matches()) {
            try {
                Path base = Paths.get(FileUtil.getFileStorageBaseDir()).toAbsolutePath().normalize();
                Path html = base.resolve(contentPath.substring("localstorage/".length())).normalize();
                if (!html.startsWith(base.resolve("news"))) throw new IOException("Invalid news HTML path");
                Files.deleteIfExists(html);
            } catch (IOException | RuntimeException e) {
                removed = false;
                getServletContext().log("新闻正文文件清理失败: " + contentPath, e);
            }
        } else {
            removed = false;
            getServletContext().log("新闻正文路径不符合受控目录规则，需人工核对: " + contentPath);
        }
        return removed;
    }

    /**
     * 发布新闻
     */
    private void publishNews(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr != null) {
            try {
                Integer id = Integer.parseInt(idStr);
                newsDAO.updateStatus(id, 1);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/news?action=manage");
    }

    private boolean isValidType(String type) {
        return "notice".equals(type) || "award".equals(type) || "activity".equals(type);
    }

    private String mediaToken(HttpServletRequest request) {
        String token = request.getParameter("mediaToken");
        if (token == null || !token.matches("[a-f0-9-]{36}")) return "";
        return token;
    }

    private List<Integer> imageIds(String content) {
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        for (Element image : Jsoup.parseBodyFragment(content).select("img[src]")) {
            Matcher matcher = MEDIA_URL.matcher(image.attr("src"));
            if (matcher.find()) ids.add(Integer.parseInt(matcher.group(1)));
        }
        return new ArrayList<>(ids);
    }

    private List<Integer> attachmentIds(HttpServletRequest request) {
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        String[] values = request.getParameterValues("attachmentIds");
        if (values != null) for (String value : values) {
            try { ids.add(Integer.parseInt(value)); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("附件 ID 无效"); }
        }
        return new ArrayList<>(ids);
    }
}
