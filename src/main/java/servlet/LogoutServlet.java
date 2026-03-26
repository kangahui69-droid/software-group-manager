package servlet;

import dao.StudySessionDAO;
import model.User;
import util.AuthHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登出Servlet
 */
public class LogoutServlet extends HttpServlet {
    private StudySessionDAO studyDAO = new StudySessionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        // 获取当前用户并结束进行中的学习时段
        if (session != null) {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser != null) {
                try {
                    // 结束用户进行中的学习时段
                    studyDAO.endStudyForUser(currentUser.getId());
                } catch (Exception e) {
                    // 忽略错误，不影响登出流程
                    System.err.println("登出时结束学习时段失败: " + e.getMessage());
                }
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}