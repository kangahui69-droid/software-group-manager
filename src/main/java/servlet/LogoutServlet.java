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
                System.out.println("[LogoutServlet] 用户登出: " + currentUser.getUsername() + ", ID: " + currentUser.getId());
                try {
                    // 结束用户进行中的学习时段
                    int updated = studyDAO.endStudyForUser(currentUser.getId());
                    System.out.println("[LogoutServlet] 已结束用户 " + currentUser.getId() + " 的学习会话, 更新行数: " + updated);
                } catch (Exception e) {
                    System.err.println("登出时结束学习时段失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("[LogoutServlet] session中的user为null");
            }
            session.invalidate();
        } else {
            System.out.println("[LogoutServlet] session为null");
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}