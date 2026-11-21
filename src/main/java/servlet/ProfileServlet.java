package servlet;

import dao.UserDAO;
import dao.MemberProfileDAO;
import dao.FileStorageDAO;
import dto.ProfileDTO;
import model.MemberProfile;
import model.User;
import service.UserService;
import util.Result;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;

/**
 * 用户资料Servlet - 4.7 Servlet改造
 * 调用UserService处理业务逻辑
 */
@MultipartConfig(maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class ProfileServlet extends HttpServlet {

    private UserService userService;
    private UserDAO userDAO;
    private MemberProfileDAO memberProfileDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        memberProfileDAO = new MemberProfileDAO();
        FileStorageDAO fileStorageDAO = new FileStorageDAO();
        this.userService = new UserService(userDAO, memberProfileDAO, fileStorageDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
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
        if ("GUEST".equals(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

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

        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String birthday = request.getParameter("birthday");
        String studentId = request.getParameter("studentId");
        String major = request.getParameter("major");
        String grade = request.getParameter("grade");
        String bio = request.getParameter("bio");
        String github = request.getParameter("github");
        String blog = request.getParameter("blog");

        ProfileDTO profile = new ProfileDTO();
        profile.setName(name);
        profile.setPhone(phone);
        profile.setEmail(email);
        profile.setBirthday(birthday);
        profile.setStudentId(studentId);
        profile.setMajor(major);
        profile.setGrade(grade);
        profile.setIntroduction(bio);
        profile.setGithub(github);
        profile.setBlog(blog);
        request.setAttribute("formProfile", profile);

        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "姓名不能为空");
            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
            return;
        }

        if (name.length() > 50) {
            request.setAttribute("error", "姓名不能超过50个字符");
            request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
            return;
        }

        if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
            if (!phone.isEmpty()) {
                request.setAttribute("error", "手机号格式不正确");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }
        }

        if (email != null && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            if (!email.isEmpty()) {
                request.setAttribute("error", "邮箱格式不正确");
                request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
                return;
            }
        }

        try {
            Result result = userService.updateProfile(currentUser.getId(), profile);

            if (result.isSuccess()) {
                request.removeAttribute("formProfile");
                try {
                    Part avatarPart = request.getPart("avatar");
                    if (avatarPart != null && avatarPart.getSize() > 0) {
                        Result avatarResult = userService.uploadAvatar(currentUser.getId(), avatarPart);
                        if (!avatarResult.isSuccess()) {
                            request.setAttribute("error", "个人信息已保存，但头像上传失败: " + avatarResult.getMessage());
                        }
                    }
                } catch (Exception e) {
                    request.setAttribute("error", "个人信息已保存，但头像处理失败，请重试头像上传");
                }
                User updatedUser = userDAO.findById(currentUser.getId());
                MemberProfile updatedProfile = memberProfileDAO.findByUserId(currentUser.getId());
                if (updatedUser != null) session.setAttribute("user", updatedUser);
                if (updatedProfile != null) session.setAttribute("memberProfile", updatedProfile);
                if (request.getAttribute("error") == null) {
                    response.sendRedirect(request.getContextPath() + "/member/profile.jsp?saved=1");
                    return;
                }
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "更新失败: " + e.getMessage());
        }

        request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
    }
}
