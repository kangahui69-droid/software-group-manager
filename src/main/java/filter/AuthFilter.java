package filter;

import dao.MemberProfileDAO;
import model.MemberProfile;
import model.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 权限控制过滤器
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        String requestURI = httpRequest.getRequestURI();

        // 检查是否已登录
        if (session == null || session.getAttribute("user") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        String userRole = user.getRole();

        // 检查管理员权限
        if (requestURI.contains("/admin/") && !"ADMIN".equalsIgnoreCase(userRole)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return;
        }

        // 检查成员权限
        if (requestURI.contains("/member/") && !"MEMBER".equalsIgnoreCase(userRole)
                && !"ADMIN".equalsIgnoreCase(userRole)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
            return;
        }

        // 加载成员档案信息
        if ("MEMBER".equalsIgnoreCase(userRole)) {
            MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
            MemberProfile memberProfile = memberProfileDAO.findByUserId(user.getId());
            if (memberProfile != null) {
                session.setAttribute("memberProfile", memberProfile);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
