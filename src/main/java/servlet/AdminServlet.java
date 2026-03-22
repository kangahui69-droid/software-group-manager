package servlet;

import dao.NewsDAO;
import dao.OperationLogDAO;
import dao.ProjectDAO;
import dao.UserDAO;
import dao.AdminProfileDAO;
import dao.FileStorageDAO;
import model.User;
import model.AdminProfile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员Servlet
 */
@javax.servlet.annotation.MultipartConfig(maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class AdminServlet extends HttpServlet {
    private NewsDAO newsDAO = new NewsDAO();
    private ProjectDAO projectDAO = new ProjectDAO();
    private UserDAO userDAO = new UserDAO();
    private OperationLogDAO logDAO = new OperationLogDAO();
    private AdminProfileDAO adminProfileDAO = new AdminProfileDAO();
    private FileStorageDAO fileStorageDAO = new FileStorageDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUri = uri.substring(contextPath.length());

        // 添加日志输出
        // System.out.println("[AdminServlet] GET Request Received");
        // System.out.println("[AdminServlet] Full URI: " + uri);
        // System.out.println("[AdminServlet] Context Path: " + contextPath);
        // System.out.println("[AdminServlet] Relative URI: " + relativeUri);

        // 处理 /admin/dashboard
        if (relativeUri.equals("/admin/dashboard")) {
            showDashboard(request, response);
            return;
        }

        // 处理 /admin/user
        if (relativeUri.equals("/admin/user")) {
            // System.out.println("[AdminServlet] Handling /admin/user");
            handleUserRequest(request, response);
            return;
        }

        // 处理 /admin/profile
        if (relativeUri.equals("/admin/profile")) {
            // System.out.println("[AdminServlet] Handling /admin/profile");
            showProfile(request, response);
            return;
        }

        // 处理 /admin/api/* 路径
        if (relativeUri.startsWith("/admin/api/")) {
            // System.out.println("[AdminServlet] Handling API request: " + relativeUri);
            String apiPath = relativeUri.substring("/admin/api/".length());

            if (apiPath.equals("dashboard")) {
                // System.out.println("[AdminServlet] Handling API dashboard");
                showDashboard(request, response);
            } else if (apiPath.equals("user")) {
                // System.out.println("[AdminServlet] Handling API user");
                handleUserRequest(request, response);
            } else if (apiPath.equals("profile")) {
                // System.out.println("[AdminServlet] Handling API profile");
                showProfile(request, response);
            } else {
                // System.out.println("[AdminServlet] 404 Not Found for API: " + apiPath);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        // 默认返回404
        // System.out.println("[AdminServlet] 404 Not Found for: " + relativeUri);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void showProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("[AdminServlet] showProfile - Start");

        HttpSession session = request.getSession(false);
        System.out.println("[AdminServlet] showProfile - Session: " + session);

        if (session == null || session.getAttribute("user") == null) {
            System.out.println("[AdminServlet] showProfile - No session or user, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        System.out.println("[AdminServlet] showProfile - Current user: " + currentUser.getUsername() + ", ID: " + currentUser.getId());

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            System.out.println("[AdminServlet] showProfile - User is not admin, sending 403");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 查询管理员个人信息
        System.out.println("[AdminServlet] showProfile - Querying admin profile for user ID: " + currentUser.getId());
        AdminProfile adminProfile = adminProfileDAO.findByUserId(currentUser.getId());
        System.out.println("[AdminServlet] showProfile - Admin profile found: " + (adminProfile != null));
        if (adminProfile != null) {
            System.out.println("[AdminServlet] showProfile - Admin profile title: " + adminProfile.getTitle());
            System.out.println("[AdminServlet] showProfile - Admin profile department: " + adminProfile.getDepartment());
            System.out.println("[AdminServlet] showProfile - Admin profile education: " + adminProfile.getEducation());
            System.out.println("[AdminServlet] showProfile - Admin profile researchArea: " + adminProfile.getResearchArea());
            System.out.println("[AdminServlet] showProfile - Admin profile bio: " + adminProfile.getBio());
            System.out.println("[AdminServlet] showProfile - Admin profile avatarFileId: " + adminProfile.getAvatarFileId());
        } else {
            System.out.println("[AdminServlet] showProfile - Admin profile NOT FOUND for user ID: " + currentUser.getId());
        }

        request.setAttribute("adminProfile", adminProfile);
        session.setAttribute("adminProfile", adminProfile);
        System.out.println("[AdminServlet] showProfile - Set adminProfile attribute to request and session");
        System.out.println("[AdminServlet] showProfile - Session adminProfile now: " + session.getAttribute("adminProfile"));

        System.out.println("[AdminServlet] showProfile - Forwarding to edit-profile.jsp");
        request.getRequestDispatcher("/admin/edit-profile.jsp").forward(request, response);
        System.out.println("[AdminServlet] showProfile - End");
    }

    private void handleUserRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String queryString = request.getQueryString();
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        if (action == null || action.isEmpty()) {
            showUserList(request, response);
        } else if ("disable".equals(action) && idParam != null) {
            disableUser(request, response, Integer.parseInt(idParam));
        } else if ("enable".equals(action) && idParam != null) {
            enableUser(request, response, Integer.parseInt(idParam));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showUserList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<User> users = userDAO.findAll();
        request.setAttribute("users", users);
        request.getRequestDispatcher("/admin/user-management.jsp").forward(request, response);
    }

    private void disableUser(HttpServletRequest request, HttpServletResponse response, Integer id)
            throws ServletException, IOException {
        // 检查是否是当前登录的管理员在禁用自己
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser.getId().equals(id) && "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                // 如果是管理员在禁用自己，重定向回用户列表并显示错误信息
                request.setAttribute("error", "管理员不能禁用自己的账号");
                showUserList(request, response);
                return;
            }
        }

        userDAO.updateStatus(id, 0);
        response.sendRedirect(request.getContextPath() + "/admin/api/user");
    }

    private void enableUser(HttpServletRequest request, HttpServletResponse response, Integer id)
            throws ServletException, IOException {
        userDAO.updateStatus(id, 1);
        response.sendRedirect(request.getContextPath() + "/admin/api/user");
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
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

        // 获取统计数据
        int newsCount = newsDAO.findAll().size();
        int projectCount = projectDAO.findAll().size();
        int userCount = userDAO.findAll().size();
        int logCount = logDAO.countAll();

        // 将数据存入request
        request.setAttribute("newsCount", newsCount);
        request.setAttribute("projectCount", projectCount);
        request.setAttribute("userCount", userCount);
        request.setAttribute("logCount", logCount);

        // 直接forward到admin/index.jsp
        request.getRequestDispatcher("/admin/index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String relativeUri = uri.substring(contextPath.length());

        // 处理 /admin/password - 密码修改
        if (relativeUri.equals("/admin/password")) {
            handlePasswordChange(request, response);
            return;
        }

        // 添加日志输出
        // System.out.println("[AdminServlet] POST Request Received");
        // System.out.println("[AdminServlet] Full URI: " + uri);
        // System.out.println("[AdminServlet] Context Path: " + contextPath);
        // System.out.println("[AdminServlet] Relative URI: " + relativeUri);

        // 处理 /admin/api/* 路径
        if (relativeUri.startsWith("/admin/api/")) {
            // System.out.println("[AdminServlet] Handling API POST request: " + relativeUri);
            String apiPath = relativeUri.substring("/admin/api/".length());

            if (apiPath.equals("profile/update")) {
                // System.out.println("[AdminServlet][AJAX-V5-FINAL] Processing profile update - " + new java.util.Date());
                try {
                    updateProfile(request, response);
                } catch (Throwable t) {
                    // System.err.println("[AdminServlet] CRITICAL ERROR calling updateProfile: " + t.getMessage());
                    // t.printStackTrace();
                    // 在 AJAX 模式下，直接打印错误
                }
            } else if (apiPath.startsWith("user/")) {
                // System.out.println("[AdminServlet] Handling user POST request: " + apiPath);
                String action = apiPath.substring("user/".length());
                handleUserPostRequest(request, response, action);
            } else {
                // System.out.println("[AdminServlet] 404 Not Found for API: " + apiPath);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }

        // 默认返回404
        // System.out.println("[AdminServlet] 404 Not Found for: " + relativeUri);
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleUserPostRequest(HttpServletRequest request, HttpServletResponse response, String action)
            throws ServletException, IOException {
        if ("add".equals(action)) {
            addUser(request, response);
        } else if ("update".equals(action)) {
            updateUser(request, response);
        } else if ("reset-password".equals(action)) {
            resetUserPassword(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String statusStr = request.getParameter("status");
        Integer status = Integer.parseInt(statusStr);

        // 如果name为空，使用username作为默认值
        if (name == null || name.trim().isEmpty()) {
            name = username;
        }

        // 如果email为空，生成一个默认邮箱
        if (email == null || email.trim().isEmpty()) {
            email = username + "@default.com";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 密码会在UserDAO中进行加密
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(status);

        try {
            boolean success = userDAO.insert(user);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/admin/api/user");
            } else {
                request.setAttribute("error", "添加用户失败");
                showUserList(request, response);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "添加用户失败: " + e.getMessage());
            showUserList(request, response);
        }
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        String username = request.getParameter("username");
        String role = request.getParameter("role");
        String statusStr = request.getParameter("status");

        Integer id = Integer.parseInt(idStr);
        Integer status = Integer.parseInt(statusStr);

        User user = userDAO.findById(id);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "用户不存在");
            return;
        }

        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);

        boolean success = userDAO.update(user);
        if (success) {
            response.sendRedirect(request.getContextPath() + "/admin/api/user");
        } else {
            request.setAttribute("error", "更新用户失败");
            showUserList(request, response);
        }
    }

    private void resetUserPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        Integer id = Integer.parseInt(idStr);

        boolean success = userDAO.updatePassword(id, "123456");
        if (success) {
            request.setAttribute("success", "密码重置成功，新密码：123456");
            showUserList(request, response);
        } else {
            request.setAttribute("error", "重置密码失败");
            showUserList(request, response);
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // System.out.println("[DEBUG] updateProfile - Entered method");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            // System.out.println("[DEBUG] updateProfile - Session or User null, redirecting");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        // System.out.println("[DEBUG] updateProfile - Current User: " + currentUser.getUsername());
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            // System.out.println("[DEBUG] updateProfile - Not Admin, Access Denied");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            // System.out.println("[DEBUG][SA] updateProfile - Starting manual part extraction");
            Map<String, String> partsMap = new java.util.HashMap<>();
            if (request.getContentType() != null && request.getContentType().startsWith("multipart/form-data")) {
                for (javax.servlet.http.Part part : request.getParts()) {
                    if (part.getContentType() == null && part.getName() != null) {
                        try (java.io.InputStream is = part.getInputStream()) {
                            byte[] bytes = readInputStream(is);
                            String value = new String(bytes, "UTF-8");
                            partsMap.put(part.getName(), value);
                            // System.out.println("  [DEBUG] Extracted Part: " + part.getName() + " = "
                            //         + (value.length() > 50 ? value.substring(0, 50) + "..." : value));
                        }
                    } else if (part.getName() != null) {
                        // System.out.println("  [DEBUG] Skipping file part: " + part.getName());
                    }
                }
            }

            // 获取表单数据
            String oldPassword = getPara(request, partsMap, "oldPassword");
            String newPassword = getPara(request, partsMap, "newPassword");
            String confirmPassword = getPara(request, partsMap, "confirmPassword");

            // 获取管理员个人信息字段
            String name = getPara(request, partsMap, "name");
            String phone = getPara(request, partsMap, "phone");
            String email = getPara(request, partsMap, "email");
            String title = getPara(request, partsMap, "title");
            String department = getPara(request, partsMap, "department");
            String education = getPara(request, partsMap, "education");
            String researchArea = getPara(request, partsMap, "researchArea");
            String bio = getPara(request, partsMap, "bio");

            // System.out.println("[DEBUG][SA] updateProfile - All parameters ready:");
            // System.out.println("  name: " + name + ", phone: " + phone + ", email: " + email);
            // System.out.println("  title: " + title + ", department: " + department + ", education: " + education);

            // System.out.println("[DEBUG] updateProfile - Parameters received:");
            // System.out.println("  name: " + name);
            // System.out.println("  phone: " + phone);
            // System.out.println("  email: " + email);
            // System.out.println("  title: " + title);
            // System.out.println("  department: " + department);
            // System.out.println("  education: " + education);
            // System.out.println("  researchArea: " + researchArea);
            // System.out
            //         .println("  bio: " + (bio != null ? bio.substring(0, Math.min(bio.length(), 20)) + "..." : "null"));

            // 处理密码更新
            boolean updated = false;
            if (newPassword != null && !newPassword.isEmpty()) {
                // 验证原密码
                if (!userDAO.verifyPassword(currentUser.getId(), oldPassword)) {
                    // System.out.println("[DEBUG][AJAX-V5] Password verification FAILED");
                    sendJsonResponse(response, false, "原密码错误");
                    return;
                }

                // 验证新密码和确认密码
                if (!newPassword.equals(confirmPassword)) {
                    // System.out.println("[DEBUG][AJAX-V5] Password mismatch");
                    sendJsonResponse(response, false, "新密码和确认密码不一致");
                    return;
                }

                // 验证新密码长度
                if (newPassword.length() < 6) {
                    // System.out.println("[DEBUG][AJAX-V5] Password too short");
                    sendJsonResponse(response, false, "新密码长度不能少于6位");
                    return;
                }

                // 更新密码
                // System.out.println("[DEBUG] updateProfile - Updating password...");
                updated = userDAO.updatePassword(currentUser.getId(), newPassword);
                // System.out.println("[DEBUG] updateProfile - Password update result: " + updated);
            }

            // 处理头像上传
            Part avatarPart = request.getPart("avatar");
            if (avatarPart != null && avatarPart.getSize() > 0) {
                // 获取上传文件的文件名
                String fileName = getSubmittedFileName(avatarPart);
                // 获取文件扩展名
                String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                // 验证文件类型
                String[] allowedExtensions = { "jpg", "jpeg", "png", "gif", "webp" };
                boolean isValidExtension = false;
                for (String ext : allowedExtensions) {
                    if (fileExtension.equals(ext)) {
                        isValidExtension = true;
                        break;
                    }
                }
                if (!isValidExtension) {
                    // System.out.println("[DEBUG][AJAX-V5] Invalid avatar extension");
                    sendJsonResponse(response, false, "只支持JPG、PNG、GIF、WebP格式的图片");
                    return;
                }
                // 验证文件大小（500KB）
                if (avatarPart.getSize() > 500 * 1024) {
                    // System.out.println("[DEBUG][AJAX-V5] Avatar too large");
                    sendJsonResponse(response, false, "文件大小不能超过500KB");
                    return;
                }
                // 生成唯一文件名
                String uniqueFileName = "avatar_" + currentUser.getId() + "_" + System.currentTimeMillis() + "."
                        + fileExtension;

                // 头像要求存储于 localstorage 的 image/avatar 文件夹 (这里理解为 WebContent 下的 localstorage/images/avatar)
                String uploadDir = getServletContext().getRealPath("/localstorage/images/avatar");
                java.io.File uploadDirFile = new java.io.File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                String filePath = uploadDir + java.io.File.separator + uniqueFileName;
                avatarPart.write(filePath);

                // 存入 file_storage 表
                dao.FileStorageDAO fileStorageDAO = new dao.FileStorageDAO();
                model.FileStorage fileStorage = new model.FileStorage();
                fileStorage.setCreateBy(currentUser.getId());
                fileStorage.setOriginalName(uniqueFileName);
                fileStorage.setFilePath("/localstorage/images/avatar/" + uniqueFileName);
                fileStorage.setFileType(avatarPart.getContentType());
                fileStorage.setFileSize(avatarPart.getSize());
                fileStorage.setCategory("avatar_image"); // 设置文件分类为头像

                Integer fileId = fileStorageDAO.insert(fileStorage);
                // System.out.println("[DEBUG] updateProfile - File storage insert ID: " + fileId);
                if (fileId != null) {
                    // 获取旧的头像文件ID用于软删除
                    Integer oldAvatarFileId = null;
                    
                    // 更新管理员档案的头像文件ID
                    AdminProfile adminProfile = adminProfileDAO.findByUserId(currentUser.getId());
                    if (adminProfile == null) {
                        // System.out.println("[DEBUG] updateProfile - Admin profile not found, creating new one");
                        adminProfile = new AdminProfile();
                        adminProfile.setUserId(currentUser.getId());
                    } else {
                        oldAvatarFileId = adminProfile.getAvatarFileId();
                    }
                    adminProfile.setAvatarFileId(fileId);
                    boolean apUpdated = adminProfileDAO.saveOrUpdate(adminProfile);
                    // System.out.println("[DEBUG] updateProfile - Admin profile avatar update result: " + apUpdated);
                    updated = apUpdated || updated;
                    
                    // 软删除旧头像
                    if (oldAvatarFileId != null) {
                        fileStorageDAO.softDelete(oldAvatarFileId);
                    }
                    
                    // 更新用户对象的头像文件ID
                    currentUser.setAvatarFileId(fileId);
                    userDAO.update(currentUser);
                }
            }

            // 处理用户基本信息更新（name、email、phone）
            if (name != null || phone != null || email != null) {
                // 即使字段为空字符串，也需要更新，以确保数据一致性
                if (name != null) {
                    currentUser.setName(name);
                }
                if (phone != null) {
                    currentUser.setPhone(phone);
                }
                if (email != null) {
                    currentUser.setEmail(email);
                }
                boolean userUpdated = userDAO.update(currentUser);
                // System.out.println("[DEBUG] updateProfile - User general info update result: " + userUpdated);
                if (userUpdated) {
                    updated = true;
                }
            }

            // 处理管理员个人信息更新（title、department、education、researchArea、bio）
            if (title != null || department != null || education != null || researchArea != null || bio != null) {
                // System.out.println("[DEBUG] updateProfile - Updating AdminProfile fields...");
                AdminProfile adminProfile = adminProfileDAO.findByUserId(currentUser.getId());
                if (adminProfile == null) {
                    // System.out.println("[DEBUG] updateProfile - Creating NEW AdminProfile");
                    adminProfile = new AdminProfile();
                    adminProfile.setUserId(currentUser.getId());
                }

                if (title != null)
                    adminProfile.setTitle(title);
                if (department != null)
                    adminProfile.setDepartment(department);
                if (education != null)
                    adminProfile.setEducation(education);
                if (researchArea != null)
                    adminProfile.setResearchArea(researchArea);
                if (bio != null)
                    adminProfile.setBio(bio);

                boolean profileUpdated = adminProfileDAO.saveOrUpdate(adminProfile);
                // System.out.println("[DEBUG] updateProfile - Admin profile fields update result: " + profileUpdated);
                if (profileUpdated) {
                    updated = true;
                }
            }

            if (updated) {
                // 更新session中的用户信息
                User updatedUser = userDAO.findById(currentUser.getId());
                session.setAttribute("user", updatedUser);

                // 重新查询管理员个人信息并更新到session中
                AdminProfile updatedAdminProfile = adminProfileDAO.findByUserId(currentUser.getId());
                session.setAttribute("adminProfile", updatedAdminProfile);

                // System.out.println("[DEBUG][FINAL-CHECK] updateProfile - SUCCESS");
                sendJsonResponse(response, true, "资料更新成功");
            } else {
                // System.out.println("[DEBUG][FINAL-CHECK] updateProfile - NO CHANGES or FAILURE");
                sendJsonResponse(response, false, "资料没有变化或更新失败");
            }

        } catch (Exception e) {
            // System.err.println("[DEBUG][FINAL-CHECK] updateProfile - EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            try {
                sendJsonResponse(response, false, "系统错误: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void sendJsonResponse(HttpServletResponse response, boolean success, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();
        out.print("{\"success\":" + success + ",\"message\":\"" + message + "\"}");
        out.flush();
    }

    // 辅助方法：从 request 或 map 中获取参数
    private String getPara(HttpServletRequest request, Map<String, String> map, String name) {
        String val = request.getParameter(name);
        if (val == null && map != null) {
            val = map.get(name);
        }
        return val;
    }

    // 获取上传文件的文件名
    private String getSubmittedFileName(javax.servlet.http.Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    private void handlePasswordChange(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            request.setAttribute("error", "请输入原密码");
            request.getRequestDispatcher("/admin/password-change.jsp").forward(request, response);
            return;
        }

        if (newPassword == null || newPassword.length() < 6) {
            request.setAttribute("error", "新密码长度不能少于6位");
            request.getRequestDispatcher("/admin/password-change.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "两次输入的新密码不一致");
            request.getRequestDispatcher("/admin/password-change.jsp").forward(request, response);
            return;
        }

        // 验证原密码
        if (!userDAO.verifyPassword(currentUser.getId(), oldPassword)) {
            request.setAttribute("error", "原密码错误");
            request.getRequestDispatcher("/admin/password-change.jsp").forward(request, response);
            return;
        }

        // 更新密码
        boolean updated = userDAO.updatePassword(currentUser.getId(), newPassword);
        if (updated) {
            request.setAttribute("success", "密码修改成功");
        } else {
            request.setAttribute("error", "密码修改失败");
        }
        request.getRequestDispatcher("/admin/password-change.jsp").forward(request, response);
    }
    
    /**
     * 读取InputStream中的所有字节（Java 8兼容版本）
     * @param is 输入流
     * @return 字节数组
     * @throws IOException
     */
    private byte[] readInputStream(java.io.InputStream is) throws java.io.IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }
}
