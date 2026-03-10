package servlet;

import dao.UserDAO;
import model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            request.setAttribute("error", "请输入原密码");
            request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
            return;
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

        if (!userDAO.verifyPassword(currentUser.getId(), oldPassword)) {
            request.setAttribute("error", "原密码错误");
            request.getRequestDispatcher("/member/password-change.jsp").forward(request, response);
            return;
        }

        boolean updated = userDAO.updatePassword(currentUser.getId(), newPassword);
        if (updated) {
            request.setAttribute("success", "密码修改成功");
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
