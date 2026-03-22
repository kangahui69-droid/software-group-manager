package servlet;

import dao.UserDAO;
import dao.AwardDAO;
import model.User;
import model.Award;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 成员管理Servlet
 */
public class MemberServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private AwardDAO awardDAO = new AwardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        if ("list".equals(action)) {
            listMembers(request, response);
        } else if ("add".equals(action)) {
            addMember(request, response);
        } else if ("edit".equals(action)) {
            editMember(request, response);
        } else if ("view".equals(action)) {
            viewMember(request, response);
        } else if ("delete".equals(action)) {
            deleteMember(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }

        if ("add".equals(action)) {
            addMember(request, response);
        } else if ("update".equals(action)) {
            updateMember(request, response);
        } else if ("delete".equals(action)) {
            deleteMember(request, response);
        } else if ("enable".equals(action)) {
            enableMember(request, response);
        } else if ("disable".equals(action)) {
            disableMember(request, response);
        } else if ("resetPassword".equals(action)) {
            resetPassword(request, response);
        }
    }

    private void listMembers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        
        List<User> users = userDAO.findByConditions(keyword, role, status);
        request.setAttribute("users", users);
        request.setAttribute("keyword", keyword);
        request.setAttribute("role", role);
        request.setAttribute("status", status);
        request.getRequestDispatcher("/jsp/admin/member/list.jsp").forward(request, response);
    }

    private void editMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            User user = userDAO.findById(id);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/admin/member/list");
                return;
            }

            request.setAttribute("user", user);
            request.getRequestDispatcher("/jsp/admin/member/edit.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        }
    }

    private void updateMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            User user = userDAO.findById(id);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/admin/member/list");
                return;
            }

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String role = request.getParameter("role");
            String statusStr = request.getParameter("status");

            if (username != null) {
                user.setUsername(username);
            }
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);
            }
            if (role != null) {
                user.setRole(role);
            }
            if (statusStr != null) {
                user.setStatus(Integer.parseInt(statusStr));
            }

            if (userDAO.update(user)) {
                response.sendRedirect(request.getContextPath() + "/admin/member/list");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "更新成员失败");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        }
    }

    private void enableMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            userDAO.updateStatus(id, 1);
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        }
    }

    private void disableMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            userDAO.updateStatus(id, 0);
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        }
    }

    private void viewMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            User user = userDAO.findById(id);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/admin/member/list");
                return;
            }

            request.setAttribute("user", user);

            // 加载该成员的获奖情况列表
            List<Award> awards = awardDAO.findByUserId(id);
            request.setAttribute("awards", awards);

            request.getRequestDispatcher("/jsp/admin/member/view.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        }
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            request.setAttribute("error", "参数错误：缺少用户ID");
            listMembers(request, response);
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            User user = userDAO.findById(id);
            if (user != null) {
                // Reset password to 123456
                boolean success = userDAO.updatePassword(id, "123456");
                if (success) {
                    request.setAttribute("success", "密码重置成功，新密码：123456");
                } else {
                    request.setAttribute("error", "密码重置失败");
                }
            } else {
                request.setAttribute("error", "用户不存在");
            }
            listMembers(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("error", "参数错误：用户ID格式不正确");
            listMembers(request, response);
        }
    }

    private void addMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String role = request.getParameter("role");
        String statusStr = request.getParameter("status");

        if (username != null && password != null && role != null) {
            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // 注意：实际应用中应该对密码进行加密
            user.setName(name != null ? name : username); // 如果没有提供姓名，使用用户名作为默认值
            user.setEmail(email != null && !email.isEmpty() ? email : username + "@default.com"); // 如果没有提供邮箱，生成默认邮箱
            user.setPhone(phone); // 手机号可以为空
            user.setRole(role);
            user.setStatus(statusStr != null ? Integer.parseInt(statusStr) : 1); // 默认启用
            try {
                if (userDAO.insert(user)) {
                    response.sendRedirect(request.getContextPath() + "/admin/member/list");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "添加成员失败");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "添加成员失败: " + e.getMessage());
            }
            return;
        }

        // 显示添加表单
        request.getRequestDispatcher("/jsp/admin/member/add.jsp").forward(request, response);
    }

    private void deleteMember(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            if (userDAO.delete(id)) {
                response.sendRedirect(request.getContextPath() + "/admin/member/list");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "删除成员失败");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/member/list");
        }
    }
}
