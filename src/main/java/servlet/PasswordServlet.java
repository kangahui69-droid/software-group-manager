package servlet;

import dao.UserDAO;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/member/password")
public class PasswordServlet extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        boolean mustChangePassword = Boolean.TRUE.equals(currentUser.getMustChangePassword());

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // 如果不是必须修改密码，则验证原密码
        if (!mustChangePassword) {
            if (oldPassword == null || oldPassword.isEmpty()) {
                request.setAttribute("error", "请输入原密码");
                request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
                return;
            }

            if (!userDAO.verifyPassword(currentUser.getId(), oldPassword)) {
                request.setAttribute("error", "原密码错误");
                request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
                return;
            }
        }

        if (newPassword == null || newPassword.length() < 6) {
            request.setAttribute("error", "新密码长度不能少于6位");
            request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "两次输入的新密码不一致");
            request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
            return;
        }

        boolean updated = userDAO.updatePassword(currentUser.getId(), newPassword);
        if (updated) {
            // 清除必须修改密码标志
            userDAO.clearMustChangePassword(currentUser.getId());

            // 使session失效，防止攻击者继续使用旧session
            session.invalidate();
            // 重定向到登录页并显示成功消息
            response.sendRedirect(request.getContextPath() + "/login.jsp?message=" + java.net.URLEncoder.encode("密码修改成功，请重新登录", "UTF-8"));
            return;
        } else {
            request.setAttribute("error", "密码修改失败");
        }

        request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
    }
}
