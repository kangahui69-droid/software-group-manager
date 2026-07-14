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
 *
 * 3.7扩展：
 * - 新增 /api/* 路径认证（未登录返回401 JSON）
 * - 抽出 isAuthenticated(req) 方法，为未来Token认证留扩展点
 * - 补全已知gap：/attendance/*、/study/* 加入受保护路径
 */
public class AuthFilter implements Filter {

    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MEMBER = "MEMBER";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String fullRequestUri = buildFullRequestUri(httpRequest);

        // ===== 公开路径白名单 =====
        if (isPublicPath(requestURI, fullRequestUri, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        // ===== 认证检查 =====
        if (!isAuthenticated(httpRequest)) {
            handleUnauthenticated(httpRequest, httpResponse, requestURI, contextPath);
            return;
        }

        // ===== 权限检查 =====
        User user = getCurrentUser(httpRequest);
        String userRole = user.getRole();

        if (!hasPermission(requestURI, userRole)) {
            sendForbidden(httpResponse, "权限不足");
            return;
        }

        // 加载成员档案信息
        loadMemberProfile(httpRequest, user);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    // ==================== 3.7接口：认证与路径判断方法 ====================

    /**
     * 判断用户是否已认证（3.7接口：抽出方法为未来Token认证留扩展点）
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object user = session.getAttribute("user");
        return user instanceof User;
    }

    /**
     * 判断是否为API路径（3.7接口：/api/*路径返回401 JSON）
     */
    protected boolean isApiPath(String path) {
        return !isEmpty(path) && path.contains("/api/");
    }

    /**
     * 判断是否为公开路径（无需登录即可访问）
     */
    protected boolean isPublicPath(String requestURI, String fullRequestUri, String contextPath) {
        return isAiPath(requestURI)
                || isRecruitPath(requestURI, contextPath)
                || isNewsPath(requestURI, fullRequestUri, contextPath)
                || isIndexPath(requestURI, contextPath)
                || isProblemPath(requestURI, contextPath);
    }

    /**
     * 判断是否为受保护路径（需要登录）（3.7接口：补全/attendance/*、/study/*）
     */
    protected boolean isProtectedPath(String path) {
        if (isEmpty(path)) {
            return false;
        }
        return path.contains("/admin/") || path.contains("/member/")
                || path.contains("/attendance/") || path.contains("/study/");
    }

    /**
     * 判断路径是否需要管理员权限
     */
    protected boolean requiresAdmin(String path) {
        return !isEmpty(path) && path.contains("/admin/");
    }

    /**
     * 判断路径是否需要成员权限
     */
    protected boolean requiresMember(String path) {
        return !isEmpty(path) && path.contains("/member/");
    }

    // ==================== 私有辅助方法 ====================

    private String buildFullRequestUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURI() + (queryString != null ? "?" + queryString : "");
    }

    private void handleUnauthenticated(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                        String requestURI, String contextPath) throws IOException {
        if (isApiPath(requestURI)) {
            sendUnauthorizedJson(httpResponse, "请先登录");
        } else {
            httpResponse.sendRedirect(contextPath + "/login.jsp");
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        return (User) request.getSession(false).getAttribute("user");
    }

    private boolean hasPermission(String requestURI, String userRole) {
        // 管理员权限检查
        if (requiresAdmin(requestURI) && !ROLE_ADMIN.equalsIgnoreCase(userRole)) {
            return false;
        }
        // 成员权限检查
        if (requiresMember(requestURI) && !hasMemberOrAdminRole(userRole)) {
            return false;
        }
        return true;
    }

    private boolean hasMemberOrAdminRole(String userRole) {
        return ROLE_MEMBER.equalsIgnoreCase(userRole) || ROLE_ADMIN.equalsIgnoreCase(userRole);
    }

    private void loadMemberProfile(HttpServletRequest request, User user) {
        if (ROLE_MEMBER.equalsIgnoreCase(user.getRole())) {
            MemberProfileDAO memberProfileDAO = new MemberProfileDAO();
            MemberProfile memberProfile = memberProfileDAO.findByUserId(user.getId());
            if (memberProfile != null) {
                request.getSession(false).setAttribute("memberProfile", memberProfile);
            }
        }
    }

    private boolean isEmpty(String path) {
        return path == null || path.isEmpty();
    }

    // ===== 公开路径判断子方法 =====

    private boolean isAiPath(String requestURI) {
        return requestURI.contains("/ai");
    }

    private boolean isRecruitPath(String requestURI, String contextPath) {
        return requestURI.contains("/recruit/apply")
                || requestURI.contains("/recruit/submit")
                || requestURI.contains("/recruit/success")
                || requestURI.endsWith(contextPath + "/recruit")
                || requestURI.endsWith(contextPath + "/recruit/");
    }

    private boolean isNewsPath(String requestURI, String fullRequestUri, String contextPath) {
        return requestURI.contains("/news/list")
                || requestURI.contains("/news?type=")
                || requestURI.endsWith(contextPath + "/news")
                || requestURI.endsWith(contextPath + "/news/")
                || fullRequestUri.contains("/news?action=detail");
    }

    private boolean isIndexPath(String requestURI, String contextPath) {
        return requestURI.endsWith(contextPath + "/index.jsp")
                || requestURI.endsWith(contextPath + "/")
                || requestURI.endsWith(contextPath);
    }

    private boolean isProblemPath(String requestURI, String contextPath) {
        return requestURI.endsWith(contextPath + "/problem-report.jsp")
                || requestURI.equals(contextPath + "/problem")
                || requestURI.startsWith(contextPath + "/problem?");
    }

    // ===== 响应辅助方法 =====

    private void sendUnauthorizedJson(HttpServletResponse response, String message) throws IOException {
        setJsonContentType(response);
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
        response.getWriter().flush();
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        setJsonContentType(response);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
    }

    private void setJsonContentType(HttpServletResponse response) {
        response.setContentType(CONTENT_TYPE_JSON);
    }
}
