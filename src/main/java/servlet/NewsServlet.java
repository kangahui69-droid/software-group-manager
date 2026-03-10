package servlet;

import dao.NewsDAO;
import model.News;
import model.User;
import util.FileUtil;

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
        System.out.println("NewsServlet doPost entered");
        try {
            request.setCharacterEncoding("UTF-8");

            // Log all parameters
            System.out.println("--- Request Parameters ---");
            java.util.Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String paramName = params.nextElement();
                String paramValue = request.getParameter(paramName);
                // Truncate long content for logging
                if (paramValue != null && paramValue.length() > 100) {
                    System.out.println(paramName + ": " + paramValue.substring(0, 100) + "... [length="
                            + paramValue.length() + "]");
                } else {
                    System.out.println(paramName + ": " + paramValue);
                }
            }
            System.out.println("--------------------------");

            HttpSession session = request.getSession(false);
            if (session == null) {
                System.out.println("Debug: Session is NULL");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            if (session.getAttribute("user") == null) {
                System.out.println("Debug: Session exists but 'user' attribute is NULL");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            User user = (User) session.getAttribute("user");
            System.out.println("Debug: User: " + (user != null ? user.getUsername() : "null"));

            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                System.out.println("Debug: User is not ADMIN.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String action = request.getParameter("action");
            if (action != null) {
                action = action.trim();
            }
            System.out.println("Debug: Received action: '" + action + "'");

            if ("create".equals(action)) {
                createNews(request, response, user.getId());
            } else if ("update".equals(action)) {
                updateNews(request, response);
            } else if ("delete".equals(action)) {
                deleteNews(request, response);
            } else {
                System.out.println("Debug: Action not matched. Redirecting to manage.");
                response.sendRedirect(request.getContextPath() + "/news?action=manage");
            }
        } catch (Exception e) {
            System.out.println("CRITICAL ERROR in doPost:");
            e.printStackTrace();
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        User user = (User) session.getAttribute("user");
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
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

        try {
            // 生成HTML文件路径
            String fileName = System.currentTimeMillis() + "_" + type + ".html";
            String relativePath = "localstorage/news/" + type + "/" + fileName;
            String realPath = getServletContext().getRealPath("/" + relativePath);

            // 确保目录存在
            File htmlFile = new File(realPath);
            FileUtil.ensureDirectoryExists(htmlFile.getParent());

            // 保存HTML内容
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
            news.setStatus(1);

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

            // 更新HTML内容
            String realPath = getServletContext().getRealPath("/" + news.getContentPath());
            System.out.println("Processing Update for News ID: " + id);
            System.out.println("Content Received Length: " + (content != null ? content.length() : "null"));
            System.out.println("Target File Path: " + realPath);

            File htmlFile = new File(realPath);
            FileUtil.ensureDirectoryExists(htmlFile.getParent());
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(htmlFile), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(content);
            }

            // Verify write
            if (htmlFile.exists()) {
                System.out.println("Verified: File exists. Size: " + htmlFile.length());
            } else {
                System.out.println("Verified: File DOES NOT exist after write!");
            }
            System.out.println("File write operation completed.");

            news.setTitle(title);
            news.setType(type);
            news.setSummary(summary);
            if (statusStr != null) {
                news.setStatus(Integer.parseInt(statusStr));
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
