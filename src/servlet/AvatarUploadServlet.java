package servlet;

import dao.FileStorageDAO;
import dao.MemberProfileDAO;
import dao.AdminProfileDAO;
import dao.UserDAO;
import model.FileStorage;
import model.MemberProfile;
import model.AdminProfile;
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
 * 头像上传Servlet
 */
@MultipartConfig(maxFileSize = 500 * 1024) // 最大500KB
public class AvatarUploadServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "images/avatar/";
    private static final long MAX_FILE_SIZE = 500 * 1024; // 500KB
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    private FileStorageDAO fileStorageDAO = new FileStorageDAO();
    private MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
    private AdminProfileDAO adminProfileDAO = new AdminProfileDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "未登录");
            return;
        }

        User user = (User) session.getAttribute("user");
        Integer userId = user.getId();

        try {
            Part filePart = request.getPart("avatar");
            if (filePart == null || filePart.getSize() == 0) {
                sendError(response, "请选择要上传的头像图片");
                return;
            }

            // 检查文件大小
            if (filePart.getSize() > MAX_FILE_SIZE) {
                sendError(response, "头像图片大小不能超过500KB");
                return;
            }

            // 检查文件类型
            String fileType = filePart.getContentType();
            if (!isAllowedImageType(fileType)) {
                sendError(response, "仅支持JPEG、JPG、PNG、GIF、WebP格式的图片");
                return;
            }

            // 处理文件上传
            String originalFileName = getFileName(filePart);
            String storedFileName = FileUtil.generateStoredFileName(originalFileName);

            // 确保目录存在
            String realPath = getServletContext().getRealPath("/");
            File uploadDir = new File(realPath + UPLOAD_DIR);
            FileUtil.ensureDirectoryExists(uploadDir.getAbsolutePath());

            // 保存文件
            String filePath = uploadDir.getAbsolutePath() + File.separator + storedFileName;
            filePart.write(filePath);

            // 保存文件记录到数据库
            FileStorage fileStorage = new FileStorage();
            fileStorage.setCreateBy(userId);
            fileStorage.setOriginalName(originalFileName);
            fileStorage.setStoredName(storedFileName);
            fileStorage.setFilePath(UPLOAD_DIR + storedFileName);
            fileStorage.setFileType(fileType);
            fileStorage.setFileSize(filePart.getSize());
            fileStorage.setCategory("avatar");

            Integer fileStorageId = fileStorageDAO.insert(fileStorage);
            if (fileStorageId == null) {
                sendError(response, "文件记录保存失败");
                return;
            }

            // 获取旧的头像文件ID用于软删除
            Integer oldAvatarFileId = null;

            // 根据用户角色决定存储位置
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                // 管理员：优先从admin_profile获取和更新头像
                AdminProfile adminProfile = adminProfileDAO.findByUserId(userId);
                if (adminProfile != null) {
                    oldAvatarFileId = adminProfile.getAvatarFileId();
                    adminProfile.setAvatarFileId(fileStorageId);
                    adminProfileDAO.update(adminProfile);
                    session.setAttribute("adminProfile", adminProfile);
                } else {
                    // 如果admin_profile不存在，创建新记录
                    AdminProfile newAdminProfile = new AdminProfile();
                    newAdminProfile.setUserId(userId);
                    newAdminProfile.setAvatarFileId(fileStorageId);
                    newAdminProfile.setStatus(1);
                    adminProfileDAO.insert(newAdminProfile);
                    session.setAttribute("adminProfile", adminProfileDAO.findByUserId(userId));
                }
            } else {
                // 成员：头像存储在member_profile
                MemberProfile profile = memberProfileDAO.findByUserId(userId);
                if (profile != null) {
                    oldAvatarFileId = profile.getAvatarFileId();
                    profile.setAvatarFileId(fileStorageId);
                    memberProfileDAO.update(profile);
                } else {
                    // 兜底：更新user表
                    user.setAvatarFileId(fileStorageId);
                    userDAO.update(user);
                }
            }

            // 软删除旧头像
            if (oldAvatarFileId != null) {
                fileStorageDAO.softDelete(oldAvatarFileId);
            }

            // 更新session中的用户信息
            session.setAttribute("user", user);

            // 返回成功响应
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("{\"success\":true,\"message\":\"头像上传成功\",\"avatarUrl\":\"" + request.getContextPath() + "/file?action=view&id=" + fileStorageId + "\"}");
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件名
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] tokens = contentDisposition.split("; ");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }

    /**
     * 检查是否为允许的图片类型
     */
    private boolean isAllowedImageType(String fileType) {
        if (fileType == null) {
            return false;
        }
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String safeMessage = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        out.print("{\"success\":false,\"message\":\"" + safeMessage + "\"}");
        out.flush();
    }
}