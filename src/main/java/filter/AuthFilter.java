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
        String contextPath = httpRequest.getContextPath();

        // ===== 公开路径白名单：不需要登录即可访问 =====
        // 允许访问的公开路径
        boolean isPublicPath =
                // AI助手（公开，游客可访问但权限受限）
                requestURI.contains("/ai") ||
                // 招新申请相关（公开）
                requestURI.contains("/recruit/apply") ||
                requestURI.endsWith(contextPath + "/recruit") ||
                requestURI.endsWith(contextPath + "/recruit/") ||
                requestURI.contains("/recruit/success") ||
                requestURI.contains("/recruit/submit") ||
                // 管理端招新管理页面（需要管理员权限，但通过AuthFilter后不重定向到登录页）
                requestURI.contains("/admin/recruit") ||
                // 新闻列表查看（公开）
                requestURI.contains("/news/list") ||
                requestURI.contains("/news?type=notice") ||
                requestURI.contains("/news?type=award") ||
                requestURI.contains("/news?type=activity") ||
                requestURI.contains("/news?type=news") ||
                requestURI.endsWith(contextPath + "/news") ||
                requestURI.endsWith(contextPath + "/news/") ||
                // 新闻详情查看（公开，URL格式：/news?action=detail&id=xxx）
                requestURI.contains("/news?action=detail") ||
                // 主页（公开）
                requestURI.endsWith(contextPath + "/index.jsp") ||
                requestURI.endsWith(contextPath + "/") ||
                // 问题反馈页面（公开，游客可提交）
                requestURI.endsWith(contextPath + "/problem-report.jsp") ||
                requestURI.equals(contextPath + "/problem") ||
                requestURI.startsWith(contextPath + "/problem?");

        // 如果是公开路径，直接放行
        if (isPublicPath) {
            chain.doFilter(request, response);
            return;
        }
        // ================================================

        // 检查是否已登录
        if (session == null || session.getAttribute("user") == null) {
            httpResponse.sendRedirect(contextPath + "/login.jsp");
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

        // 限制管理员访问群聊功能（管理员暂不添加群聊功能）
        if (requestURI.contains("/group/") && "ADMIN".equalsIgnoreCase(userRole)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "管理员暂不使用群聊功能");
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
