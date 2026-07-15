package servlet;

import dao.UserDAO;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part; // Added for Part class
import java.io.IOException;
import dao.MemberProfileDAO;
import model.MemberProfile;
import dao.FileStorageDAO;
import model.FileStorage;
import util.FileUtil;

/**
 * 用户资料Servlet
 */
@MultipartConfig(maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class ProfileServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
    private FileStorageDAO fileStorageDAO = new FileStorageDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String action = uri.substring(uri.lastIndexOf("/") + 1);

        if ("edit".equals(action)) {
            showEditProfilePage(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/member/profile.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String action = uri.substring(uri.lastIndexOf("/") + 1);

        if ("update".equals(action)) {
            updateProfile(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showEditProfilePage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        request.setAttribute("user", user);
        request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        try {
            // 获取表单数据
            String userType = request.getParameter("userType");
            String oldPassword = request.getParameter("oldPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            // 获取基本信息字段
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");

            // 获取成员档案字段
            String birthday = request.getParameter("birthday");
            String studentId = request.getParameter("studentId");
            String major = request.getParameter("major");
            String grade = request.getParameter("grade");
            String bio = request.getParameter("bio");
            String github = request.getParameter("github");
            String blog = request.getParameter("blog");

            boolean updated = false;

            // 检查name和student_id是否被修改（这些字段一旦创建不可修改）
            MemberProfile existingProfile = memberProfileDAO.findByUserId(currentUser.getId());
            String originalName = currentUser.getName();
            String originalStudentId = existingProfile != null ? existingProfile.getStudentId() : null;
            
            if (existingProfile != null) {
                // 检查姓名是否被修改
                if (name != null && !name.equals(originalName)) {
                    request.setAttribute("error", "姓名创建后不可修改，请联系管理员");
                    request.setAttribute("memberProfile", existingProfile);
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }
                
                // 检查学号是否被修改
                if (studentId != null && originalStudentId != null && !studentId.equals(originalStudentId)) {
                    request.setAttribute("error", "学号创建后不可修改，请联系管理员");
                    request.setAttribute("memberProfile", existingProfile);
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }
                
                // 使用原始值，不允许修改
                name = originalName;
                studentId = originalStudentId;
            }

            // 更新用户表基本信息
            currentUser.setName(name);
            currentUser.setPhone(phone);
            currentUser.setEmail(email);
            try {
                updated = userDAO.update(currentUser);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    request.setAttribute("error", "邮箱已被其他用户使用");
                    request.setAttribute("memberProfile", memberProfileDAO.findByUserId(currentUser.getId()));
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }
                throw e;
            }

            // 更新成员属性
            MemberProfile profile = memberProfileDAO.findByUserId(currentUser.getId());
            if (profile == null) {
                profile = new MemberProfile();
                profile.setUserId(currentUser.getId());
            }
            if (birthday != null && !birthday.isEmpty()) {
                try {
                    profile.setBirthday(java.sql.Date.valueOf(birthday));
                } catch (Exception e) {
                }
            }
            profile.setStudentId(studentId);
            profile.setMajor(major);
            profile.setGrade(grade);
            profile.setIntroduction(bio);
            profile.setGithub(github);
            profile.setBlog(blog);

            // 处理头像上传
            String contentType = request.getContentType();
            System.out.println("[头像上传] Content-Type: " + contentType);
            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
                try {
                    Part avatarPart = request.getPart("avatar");
                    System.out.println("[头像上传] avatarPart=" + (avatarPart != null) + ", size=" + (avatarPart != null ? avatarPart.getSize() : 0));
                    if (avatarPart != null && avatarPart.getSize() > 0) {
                        // 验证文件大小（500KB）
                        if (avatarPart.getSize() > 500 * 1024) {
                            request.setAttribute("error", "头像图片大小不能超过500KB");
                            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                            return;
                        }

                        // 验证文件类型
                        String fileType = avatarPart.getContentType();
                        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
                        boolean isValidType = false;
                        for (String type : allowedTypes) {
                            if (type.equalsIgnoreCase(fileType)) {
                                isValidType = true;
                                break;
                            }
                        }
                        if (!isValidType) {
                            request.setAttribute("error", "仅支持JPEG、JPG、PNG、GIF、WebP格式的图片");
                            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                            return;
                        }

                        String fileName = getSubmittedFileName(avatarPart);
                        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                        String uniqueFileName = "avatar_" + currentUser.getId() + "_" + System.currentTimeMillis() + "."
                                + fileExtension;
                        String uploadDir = FileUtil.getCategoryDir("images/avatar");
                        System.out.println("[头像上传] 上传目录: " + uploadDir);
                        String fullFilePath = uploadDir + java.io.File.separator + uniqueFileName;
                        avatarPart.write(fullFilePath);
                        System.out.println("[头像上传] 文件已保存到: " + fullFilePath);

                        // 保存文件记录到数据库
                        FileStorage fileStorage = new FileStorage();
                        fileStorage.setCreateBy(currentUser.getId());
                        fileStorage.setOriginalName(uniqueFileName);
                        fileStorage.setStoredName(uniqueFileName);
                        fileStorage.setFilePath("/localstorage/images/avatar/" + uniqueFileName);
                        fileStorage.setFileType(avatarPart.getContentType());
                        fileStorage.setFileSize(avatarPart.getSize());
                        fileStorage.setCategory("avatar_image");
                        Integer fileId = fileStorageDAO.insert(fileStorage);
                        System.out.println("[头像上传] 数据库插入结果 fileId=" + fileId);

                        // 获取旧头像ID用于软删除（必须在更新profile之前获取）
                        Integer oldAvatarFileId = profile.getAvatarFileId();
                        System.out.println("[头像上传] 旧头像ID=" + oldAvatarFileId);

                        if (fileId != null) {
                            profile.setAvatarFileId(fileId);
                            System.out.println("[头像上传] 设置新头像ID=" + fileId);
                        }

                        // 软删除旧头像
                        if (oldAvatarFileId != null) {
                            boolean deleted = fileStorageDAO.softDelete(oldAvatarFileId);
                            System.out.println("[头像上传] 软删除旧头像结果=" + deleted);
                        }
                    }
                } catch (Exception e) {
                    // 记录详细错误信息
                    System.err.println("[头像上传] 发生异常: " + e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    // 将错误信息设置到request属性中，以便在页面上显示
                    request.setAttribute("avatarUploadError", "头像上传失败: " + e.getMessage());
                    // 不要在这里返回，继续保存其他字段
                }
            }

            updated = memberProfileDAO.saveOrUpdate(profile) || updated;

            // 处理密码更新
            if (newPassword != null && !newPassword.isEmpty()) {
                // 验证原密码
                if (!userDAO.verifyPassword(currentUser.getId(), oldPassword)) {
                    request.setAttribute("error", "原密码错误");
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }

                // 验证新密码和确认密码
                if (!newPassword.equals(confirmPassword)) {
                    request.setAttribute("error", "新密码和确认密码不一致");
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }

                // 验证新密码长度
                if (newPassword.length() < 6) {
                    request.setAttribute("error", "新密码长度不能少于6位");
                    request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                    return;
                }

                // 更新密码
                updated = userDAO.updatePassword(currentUser.getId(), newPassword) || updated;
            }

            if (updated) {
                // 更新session中的用户信息
                User updatedUser = userDAO.findById(currentUser.getId());
                session.setAttribute("user", updatedUser);
                // 刷新session中的memberProfile，确保头像ID是最新的
                MemberProfile updatedProfile = memberProfileDAO.findByUserId(currentUser.getId());
                session.setAttribute("memberProfile", updatedProfile);
                response.sendRedirect(request.getContextPath() + "/member/profile.jsp?success=1");
            } else {
                request.setAttribute("error", "资料更新失败");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "资料更新失败：" + e.getMessage());
            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}