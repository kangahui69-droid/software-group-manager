package servlet;

import dao.FileStorageDAO;
import model.FileStorage;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import util.FileUtil;

/**
 * 统一文件接口Servlet
 */
@WebServlet(name = "FileStorageServlet", urlPatterns = "/file")
@MultipartConfig(maxFileSize = 1024 * 1024 * 100, maxRequestSize = 1024 * 1024 * 100)
public class FileStorageServlet extends HttpServlet {
    private FileStorageDAO fileStorageDAO = new FileStorageDAO();
    private static final String UPLOAD_BASE_DIR = "files";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/error.jsp");
            return;
        }

        // 对于view操作，不需要用户登录
        if ("view".equals(action)) {
            viewFile(request, response);
            return;
        }

        // 其他操作需要用户登录
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if ("download".equals(action)) {
            downloadFile(request, response);
        } else if ("list".equals(action)) {
            listFiles(request, response);
        } else if ("delete".equals(action)) {
            deleteFile(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/error.jsp");
            return;
        }

        if ("upload".equals(action)) {
            uploadFile(request, response);
        }
    }

    /**
     * 上传文件
     */
    private void uploadFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        String category = request.getParameter("category");
        if (category == null) category = "general";

        String categorySubDir = UPLOAD_BASE_DIR + "/" + category;
        String uploadPath = FileUtil.getCategoryDir(categorySubDir);

        List<Integer> uploadedFileIds = new ArrayList<>();

        for (Part part : request.getParts()) {
            if (part.getContentType() != null) {
                String fileName = extractFileName(part);
                if (fileName != null && !fileName.isEmpty()) {
                    String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                    String filePath = uploadPath + File.separator + uniqueFileName;

                    try (InputStream input = part.getInputStream()) {
                        Files.copy(input, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    FileStorage fileStorage = new FileStorage();
                    fileStorage.setCreateBy(user.getId());
                    fileStorage.setOriginalName(fileName);
                    fileStorage.setStoredName(uniqueFileName);
                    fileStorage.setFilePath("/localstorage/" + UPLOAD_BASE_DIR + "/" + category + "/" + uniqueFileName);
                    fileStorage.setFileType(part.getContentType());
                    fileStorage.setFileSize(part.getSize());
                    fileStorage.setCategory(category);

                    // 保存到数据库
                    Integer fileId = fileStorageDAO.insert(fileStorage);
                    if (fileId != null) {
                        uploadedFileIds.add(fileId);
                    }
                }
            }
        }

        // 返回上传结果
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (uploadedFileIds.isEmpty()) {
            response.getWriter().write("{\"success\": false, \"message\": \"文件上传失败\"}");
        } else {
            response.getWriter().write("{\"success\": true, \"fileIds\": " + uploadedFileIds + "}");
        }
    }

    /**
     * 下载文件
     */
    private void downloadFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer fileId = Integer.parseInt(request.getParameter("id"));
        FileStorage fileStorage = fileStorageDAO.findById(fileId);

        if (fileStorage == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        String filePath = FileUtil.resolvePhysicalPath(fileStorage.getFilePath());
        File file = new File(filePath);

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        // 设置响应头
        response.setContentType(fileStorage.getFileType());
        response.setContentLengthLong(fileStorage.getFileSize());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileStorage.getOriginalName() + "\"");

        // 发送文件
        try (InputStream input = new FileInputStream(file);
             OutputStream output = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 在线查看文件
     */
    private void viewFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件ID不能为空");
                return;
            }

            Integer fileId = Integer.parseInt(idParam);
            FileStorage fileStorage = fileStorageDAO.findById(fileId);

            if (fileStorage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return;
            }

            String filePath = FileUtil.resolvePhysicalPath(fileStorage.getFilePath());
            File file = new File(filePath);

            // 兼容旧数据：如果外部路径没找到，尝试从webapp部署目录查找
            if (!file.exists()) {
                String legacyPath = getServletContext().getRealPath(fileStorage.getFilePath());
                File legacyFile = new File(legacyPath);
                if (legacyFile.exists()) {
                    file = legacyFile;
                }
            }

            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return;
            }

            String contentType = fileStorage.getFileType() != null ? fileStorage.getFileType() : "application/octet-stream";
            response.setContentType(contentType);
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "inline");

            try (FileInputStream input = new FileInputStream(file);
                 ServletOutputStream output = response.getOutputStream()) {
                byte[] buffer = new byte[1024 * 8];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "发送文件时出错");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文件ID格式");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "查看文件时出错: " + e.getMessage());
        }
    }

    /**
     * 列出文件
     */
    private void listFiles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        String category = request.getParameter("category");

        List<FileStorage> files;
        if (category != null) {
            files = fileStorageDAO.findByCategory(category);
        } else {
            files = fileStorageDAO.findByCreateBy(user.getId());
        }

        request.setAttribute("files", files);
        request.getRequestDispatcher("/member/file/list.jsp").forward(request, response);
    }

    /**
     * 删除文件
     */
    private void deleteFile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        Integer fileId = Integer.parseInt(request.getParameter("id"));

        FileStorage fileStorage = fileStorageDAO.findById(fileId);
        if (fileStorage == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        // 验证权限
        if (!fileStorage.getCreateBy().equals(user.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "没有权限删除该文件");
            return;
        }

        // 删除服务器上的文件（兼容新旧路径）
        String filePath = FileUtil.resolvePhysicalPath(fileStorage.getFilePath());
        File file = new File(filePath);
        if (!file.exists()) {
            String legacyPath = getServletContext().getRealPath(fileStorage.getFilePath());
            file = new File(legacyPath);
        }
        if (file.exists()) {
            file.delete();
        }

        // 删除数据库记录
        boolean success = fileStorageDAO.delete(fileId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        if (success) {
            response.getWriter().write("{\"success\": true, \"message\": \"文件删除成功\"}");
        } else {
            response.getWriter().write("{\"success\": false, \"message\": \"文件删除失败\"}");
        }
    }

    /**
     * 提取文件名
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split("; ");
        for (String s : items) {
            if (s.startsWith("filename=")) {
                try {
                    return URLDecoder.decode(s.substring(s.indexOf("=") + 2, s.length() - 1), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}