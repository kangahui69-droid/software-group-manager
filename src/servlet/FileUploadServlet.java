package servlet;

import dao.OperationLogDAO;
import model.OperationLog;
import model.User;
import util.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 文件上传Servlet
 */
@MultipartConfig(maxFileSize = 10 * 1024 * 1024) // 最大10MB
public class FileUploadServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "localstorage/files/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        Integer userId = user.getId();

        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                sendError(response, "请选择要上传的文件");
                return;
            }

            String originalFileName = getFileName(filePart);
            String storedFileName = FileUtil.generateStoredFileName(originalFileName);
            String userDir = UPLOAD_DIR + userId + "/";

            // 确保目录存在
            String realPath = getServletContext().getRealPath("/");
            File userDirFile = new File(realPath + userDir);
            FileUtil.ensureDirectoryExists(userDirFile.getAbsolutePath());

            // 保存文件
            String filePath = userDirFile.getAbsolutePath() + File.separator + storedFileName;
            filePart.write(filePath);

            // 返回文件路径
            String relativePath = userDir + storedFileName;
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"success\":true,\"filePath\":\"" + relativePath + "\",\"fileName\":\"" + originalFileName + "\"}");
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件名
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"success\":false,\"message\":\"" + message + "\"}");
        out.flush();
    }
}

