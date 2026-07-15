package servlet;

import dao.UserDAO;
import dao.MemberProfileDAO;
import dao.FileStorageDAO;
import dto.ProfileDTO;
import model.User;
import service.UserService;
import util.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;

/**
 * 用户资料Servlet - 4.7 Servlet改造
 * 调用UserService处理业务逻辑
 */
@MultipartConfig(maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class ProfileServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        UserDAO userDAO = new UserDAO();
        MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
        FileStorageDAO fileStorageDAO = new FileStorageDAO();
        this.userService = new UserService(userDAO, memberProfileDAO, fileStorageDAO);
    }

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

        String userType = request.getParameter("userType");
        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String birthday = request.getParameter("birthday");
        String studentId = request.getParameter("studentId");
        String major = request.getParameter("major");
        String grade = request.getParameter("grade");
        String bio = request.getParameter("bio");

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
            Result result = userService.updateProfile(
                    currentUser.getId(), name, phone, email, birthday, studentId, major, grade, bio);

            if (result.isSuccess()) {
                request.setAttribute("success", "个人信息更新成功");
            } else {
                request.setAttribute("error", result.getMessage());
            }
        } catch (Exception e) {
            request.setAttribute("error", "更新失败: " + e.getMessage());
        }

        request.getRequestDispatcher("/member/edit-profile.jsp").forward(request, response);
    }
}
