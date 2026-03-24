package util;

import model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 权限检查工具类
 */
public class AuthHelper {

    /**
     * 检查是否已登录
     * @return true-已登录，false-未登录（已重定向到登录页）
     */
    public static boolean checkLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return false;
        }
        return true;
    }

    /**
     * 检查是否为管理员
     * @return true-是管理员，false-不是（已重定向或返回403）
     */
    public static boolean checkAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!checkLogin(request, response)) {
            return false;
        }
        User user = (User) request.getSession().getAttribute("user");
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return false;
        }
        return true;
    }

    /**
     * 检查是否为成员或管理员
     * @return true-是成员或管理员，false-不是
     */
    public static boolean checkMember(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!checkLogin(request, response)) {
            return false;
        }
        User user = (User) request.getSession().getAttribute("user");
        if (!"MEMBER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return false;
        }
        return true;
    }

    /**
     * 获取当前登录用户
     * @return User对象，如果未登录返回null
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("user");
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
