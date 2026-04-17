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

/**
 * 统一文件接口Servlet
 */
@WebServlet(name = "FileStorageServlet", urlPatterns = "/file")
@MultipartConfig(maxFileSize = 1024 * 1024 * 100, maxRequestSize = 1024 * 1024 * 100)
public class FileStorageServlet extends HttpServlet {
    private FileStorageDAO fileStorageDAO = new FileStorageDAO();
    private final String UPLOAD_BASE_DIR = "localstorage/files";

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

        // 确保上传目录存在
        String uploadPath = getServletContext().getRealPath("/" + UPLOAD_BASE_DIR);
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        List<Integer> uploadedFileIds = new ArrayList<>();

        // 处理上传的文件
        for (Part part : request.getParts()) {
            if (part.getContentType() != null) { // 确保是文件部分
                String fileName = extractFileName(part);
                if (fileName != null && !fileName.isEmpty()) {
                    // 生成唯一文件名
                    String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                    String filePath = uploadPath + File.separator + uniqueFileName;

                    // 保存文件到服务器
                    try (InputStream input = part.getInputStream()) {
                        Files.copy(input, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    // 创建FileStorage对象
                    FileStorage fileStorage = new FileStorage();
                    fileStorage.setCreateBy(user.getId());
                    fileStorage.setOriginalName(fileName);
                    fileStorage.setStoredName(uniqueFileName);
                    fileStorage.setFilePath("/" + UPLOAD_BASE_DIR + "/" + uniqueFileName);
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

        String filePath = getServletContext().getRealPath(fileStorage.getFilePath());
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
            // 获取文件ID
            String idParam = request.getParameter("id");
            System.out.println("文件查看请求 - id: " + idParam);
            
            if (idParam == null || idParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件ID不能为空");
                System.out.println("错误：文件ID不能为空");
                return;
            }
            
            Integer fileId = Integer.parseInt(idParam);
            
            // 从数据库获取文件记录
            FileStorage fileStorage = fileStorageDAO.findById(fileId);
            System.out.println("从数据库获取文件记录: " + (fileStorage != null ? "成功" : "失败"));

            if (fileStorage == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                System.out.println("错误：文件不存在（数据库中找不到记录）");
                return;
            }

            // 输出文件记录信息
            System.out.println("文件记录详情：");
            System.out.println("  id: " + fileStorage.getId());
            System.out.println("  originalName: " + fileStorage.getOriginalName());
            System.out.println("  storedName: " + fileStorage.getStoredName());
            System.out.println("  filePath: " + fileStorage.getFilePath());
            System.out.println("  fileType: " + fileStorage.getFileType());
            System.out.println("  fileSize: " + fileStorage.getFileSize());
            
            // 构建实际文件路径
            String contextPath = getServletContext().getRealPath("");
            String filePath = getServletContext().getRealPath(fileStorage.getFilePath());
            
            // 调试信息
            System.out.println("ServletContext RealPath: " + contextPath);
            System.out.println("fileStorage.getFilePath(): " + fileStorage.getFilePath());
            System.out.println("构建的文件路径: " + filePath);
            
            File file = new File(filePath);
            
            // 如果文件不存在，尝试其他可能的路径
            if (!file.exists()) {
                System.out.println("尝试从WebContent目录查找文件...");
                // 尝试直接从WebContent目录构建路径
                String webContentPath = contextPath;
                if (!webContentPath.endsWith("/") && !webContentPath.endsWith("\\")) {
                    webContentPath += "/";
                }
                
                // 尝试多种可能的路径组合
                String[] possiblePaths = {
                    webContentPath + fileStorage.getFilePath().substring(1), // 去掉开头的/或\
                    webContentPath + "localstorage/images/award/" + fileStorage.getStoredName(),
                    webContentPath + "localstorage/files/" + fileStorage.getStoredName(),
                    webContentPath + "localstorage/images/avatar/" + fileStorage.getStoredName() // 尝试头像路径
                };
                
                for (String path : possiblePaths) {
                    File altFile = new File(path);
                    System.out.println("尝试路径: " + altFile.getAbsolutePath());
                    System.out.println("是否存在: " + altFile.exists());
                    
                    if (altFile.exists()) {
                        file = altFile;
                        filePath = path;
                        System.out.println("找到文件: " + filePath);
                        break;
                    }
                }
            }
            
            System.out.println("最终文件路径: " + filePath);
            System.out.println("文件是否存在: " + file.exists());
            System.out.println("文件是否可读: " + file.canRead());
            System.out.println("文件大小: " + (file.exists() ? file.length() : 0));

            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                System.out.println("错误：文件不存在（实际文件路径不存在）");
                return;
            }

            // 简化响应头设置，只保留必要的头信息
            // 确保Content-Type只包含正确的MIME类型，不添加任何字符集参数
            String contentType = fileStorage.getFileType() != null ? fileStorage.getFileType() : "application/octet-stream";
            
            // 直接设置Content-Type，不添加任何charset参数
            response.setContentType(contentType);
            
            // 设置Content-Length
            response.setContentLength((int) file.length());
            
            // 设置Content-Disposition为inline，确保图片在浏览器中显示
            response.setHeader("Content-Disposition", "inline");
            
            // 移除所有可能影响图片显示的头
            response.setHeader("Content-Encoding", null);
            response.setHeader("Transfer-Encoding", null);
            response.setHeader("Accept-Ranges", null);
            response.setHeader("Cache-Control", null);
            response.setHeader("Pragma", null);
            response.setHeader("Expires", null);
            
            System.out.println("设置的Content-Type: " + response.getContentType());
            System.out.println("设置的Content-Length: " + file.length());

            // 直接发送文件内容
            try {
                // 使用FileCopyUtils简化文件发送
                javax.servlet.http.HttpServletResponseWrapper wrapper = new javax.servlet.http.HttpServletResponseWrapper(response) {
                    @Override
                    public void setHeader(String name, String value) {
                        // 防止其他代码修改响应头
                        if ("Content-Type".equals(name) || "Content-Length".equals(name) || "Content-Disposition".equals(name)) {
                            super.setHeader(name, value);
                        }
                    }
                };
                
                // 直接将文件写入响应输出流
                FileInputStream input = new FileInputStream(file);
                ServletOutputStream output = response.getOutputStream();
                
                // 使用缓冲区复制文件
                byte[] buffer = new byte[1024 * 8]; // 8KB缓冲区
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                
                // 确保输出缓冲区被刷新
                output.flush();
                
                // 关闭流
                input.close();
                output.close();
                
                System.out.println("文件发送完成");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("发送文件时出错: " + e.getMessage());
                // 只在文件未开始发送时才发送错误
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "发送文件时出错");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文件ID格式");
            System.out.println("错误：无效的文件ID格式: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "查看文件时出错: " + e.getMessage());
            System.out.println("错误：查看文件时出错: " + e.getMessage());
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

        // 删除服务器上的文件
        String filePath = getServletContext().getRealPath(fileStorage.getFilePath());
        File file = new File(filePath);
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