package servlet;

import util.DBUtil;
import util.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 文件下载Servlet（遗留接口，优先使用 /file?action=download&id=xxx）
 */
public class FileDownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filePath = request.getPathInfo();
        if (filePath == null || filePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件路径不能为空");
            return;
        }

        String realPath = FileUtil.resolvePhysicalPath(filePath);
        File file = new File(realPath);
        if (!file.exists()) {
            String legacyPath = getServletContext().getRealPath(filePath);
            file = new File(legacyPath);
        }

        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }

        // 获取原始文件名（从数据库或文件路径中提取）
        String originalFileName = extractOriginalFileName(filePath);

        // 设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + 
                new String(originalFileName.getBytes("UTF-8"), "ISO-8859-1") + "\"");
        response.setContentLength((int) file.length());

        // 输出文件
        try (InputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    /**
     * 提取原始文件名（简化版，实际应从数据库查询）
     */
    private String extractOriginalFileName(String filePath) {
        // 这里简化处理，实际应该从file_storage表查询原始文件名
        String[] parts = filePath.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return "download";
    }
}

