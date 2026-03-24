package servlet;

import dao.NewsDAO;
import model.News;
import model.User;
import util.AuthHelper;
import util.FileUtil;
import util.HtmlSanitizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 新闻Servlet
 */
public class NewsServlet extends HttpServlet {
    private NewsDAO newsDAO = new NewsDAO();

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
            newsList = newsDAO.findAll();
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
        if (idStr != null && !idStr.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idStr);
                News news = newsDAO.findById(id);
                if (news != null) {
                    // 读取HTML内容
                    String realPath = getServletContext().getRealPath("/" + news.getContentPath());
                    File htmlFile = new File(realPath);
                    String content = "";
                    if (htmlFile.exists()) {
                        content = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()), "UTF-8");
                    }
                    request.setAttribute("news", news);
                    request.setAttribute("content", content);
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
            if (news == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 读取HTML内容
            String realPath = getServletContext().getRealPath("/" + news.getContentPath());
            File htmlFile = new File(realPath);
            String content = "";
            if (htmlFile.exists()) {
                content = new String(java.nio.file.Files.readAllBytes(htmlFile.toPath()), "UTF-8");
            }

            request.setAttribute("news", news);
            request.setAttribute("content", content);
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
            // 生成HTML文件路径
            String fileName = System.currentTimeMillis() + "_" + type + ".html";
            String relativePath = "localstorage/news/" + type + "/" + fileName;
            String realPath = getServletContext().getRealPath("/" + relativePath);

            // 确保目录存在
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
            news.setStatus(status);

            if (newsDAO.insert(news)) {
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

            // 更新HTML内容（已净化）
            String realPath = getServletContext().getRealPath("/" + news.getContentPath());

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
}
