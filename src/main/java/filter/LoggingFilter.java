package filter;

import dao.OperationLogDAO;
import model.AuditLogCategory;
import model.OperationLog;
import model.User;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 操作日志过滤器
 */
public class LoggingFilter implements Filter {
    public static final String REQUEST_ID_ATTRIBUTE = "audit.requestId";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private final OperationLogDAO logDAO;
    private final ClientIpResolver clientIpResolver;

    public LoggingFilter() {
        this(new OperationLogDAO());
    }

    LoggingFilter(OperationLogDAO logDAO) {
        this(logDAO, new ClientIpResolver());
    }

    LoggingFilter(OperationLogDAO logDAO, ClientIpResolver clientIpResolver) {
        this.logDAO = logDAO;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String method = httpRequest.getMethod();

        AuditLogCategory.Operation operation = AuditLogCategory.loggedOperation(method, httpRequest.getRequestURI());
        if (operation == null) {
            chain.doFilter(request, response);
            return;
        }

        long startedAt = System.nanoTime();
        String requestId = UUID.randomUUID().toString();
        httpRequest.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
        User userBeforeRequest = getCurrentUser(httpRequest);
        AuditResponseWrapper wrappedResponse = new AuditResponseWrapper(httpResponse, requestId);
        boolean completed = false;
        try {
            chain.doFilter(request, wrappedResponse);
            completed = true;
        } finally {
            // The business request must finish before the log is written.
            try {
                User user = userBeforeRequest != null ? userBeforeRequest : getCurrentUser(httpRequest);
                int status = wrappedResponse.getStatus();
                if (!completed && status < 400 && !wrappedResponse.isCommitted()) {
                    status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                }
                String result = classifyResult(httpRequest, wrappedResponse, completed, userBeforeRequest, status);
                String targetType = extractTargetType(httpRequest.getRequestURI());
                OperationLog log = new OperationLog();
                log.setUserId(user == null ? null : user.getId());
                log.setUsername(user == null ? "游客" : AuditLogSanitizer.maskedUsername(user.getUsername()));
                log.setOperation(operation.name());
                log.setModule(AuditLogCategory.moduleForUri(httpRequest.getRequestURI()).getValue());
                log.setDescription(buildDescription(httpRequest, method));
                ClientIpResolver.ClientAddress clientAddress = clientIpResolver.resolve(httpRequest);
                log.setIpAddress(clientAddress.clientIp());
                log.setProxyIp(clientAddress.proxyIp());
                log.setUserAgent(AuditLogSanitizer.safeUserAgent(httpRequest.getHeader("User-Agent")));
                log.setResult(result);
                log.setHttpStatus(status);
                log.setDurationMs(TimeUnit.NANOSECONDS.toMillis(Math.max(0L, System.nanoTime() - startedAt)));
                log.setRequestId(requestId);
                log.setErrorCode(resolveErrorCode(httpRequest, completed, result, status));
                log.setTargetType(targetType);
                log.setTargetId(extractTargetId(httpRequest, targetType));
                if (!logDAO.insert(log)) {
                    System.err.println("[LoggingFilter] 操作日志写入失败, requestId=" + requestId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Audit storage errors must not change the business response.
            }
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            Object principal = session == null ? null : session.getAttribute("user");
            return principal instanceof User ? (User) principal : null;
        } catch (RuntimeException ignored) {
            // Logout may invalidate the session before this filter finishes.
            return null;
        }
    }

    private String classifyResult(HttpServletRequest request, AuditResponseWrapper response,
                                  boolean completed, User userBeforeRequest, int status) {
        if (!completed) return "FAILED";

        if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN) {
            return "DENIED";
        }
        if (request.getRequestURI().endsWith("/login") && request.getAttribute("error") != null) {
            return "DENIED";
        }
        if (status >= 400 || request.getAttribute("error") != null
                || hasErrorRedirect(response.getRedirectLocation())) {
            return "FAILED";
        }
        if (status >= 300 && status < 400 && userBeforeRequest == null
                && isLoginRedirect(response.getRedirectLocation())) {
            return "DENIED";
        }
        return "SUCCESS";
    }

    private String resolveErrorCode(HttpServletRequest request, boolean completed, String result, int status) {
        if (!completed) return "UNHANDLED_EXCEPTION";
        if (status >= 400) return "HTTP_" + status;
        if ("DENIED".equals(result)) {
            return request.getRequestURI().endsWith("/login") ? "AUTH_FAILED" : "AUTH_REQUIRED";
        }
        return "FAILED".equals(result) ? "BUSINESS_ERROR" : null;
    }

    private String extractTargetType(String uri) {
        if (uri == null) return null;
        if (uri.contains("/project")) return "PROJECT";
        if (uri.contains("/award")) return "AWARD";
        if (uri.contains("/activity")) return "ACTIVITY";
        if (uri.contains("/news")) return "NEWS";
        if (uri.contains("/admin/member") || uri.contains("/api/user")) return "USER";
        if (uri.contains("/recruit")) return "RECRUIT";
        if (uri.contains("/resume")) return "RESUME";
        if (uri.contains("/file") || uri.contains("/upload")) return "FILE";
        if (uri.contains("/group")) return "GROUP";
        if (uri.contains("/problem")) return "PROBLEM";
        return null;
    }

    private String extractTargetId(HttpServletRequest request, String targetType) {
        if (targetType == null) return null;
        String id = request.getParameter("id");
        if (id == null || id.isBlank()) {
            String namedId = targetType.toLowerCase(Locale.ROOT) + "Id";
            id = request.getParameter(namedId);
        }
        if (id == null) return null;
        return AuditLogSanitizer.safeTargetId(id);
    }

    private boolean hasErrorRedirect(String location) {
        return location != null && (location.contains("?error=") || location.contains("&error="));
    }

    private boolean isLoginRedirect(String location) {
        if (location == null) return false;
        String path = location.split("\\?", 2)[0];
        return path.endsWith("/login.jsp") || path.endsWith("/login");
    }

    private static final class AuditResponseWrapper extends HttpServletResponseWrapper {
        private final String requestId;
        private int status;
        private String redirectLocation;

        private AuditResponseWrapper(HttpServletResponse response, String requestId) {
            super(response);
            this.requestId = requestId;
            this.status = response.getStatus() > 0 ? response.getStatus() : SC_OK;
        }

        @Override
        public void setStatus(int status) {
            super.setStatus(status);
            this.status = status;
        }

        @Override
        public void sendError(int status) throws IOException {
            super.sendError(status);
            this.status = status;
        }

        @Override
        public void sendError(int status, String message) throws IOException {
            super.sendError(status, message);
            this.status = status;
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            super.sendRedirect(location);
            this.status = SC_FOUND;
            this.redirectLocation = location;
        }

        @Override
        public void reset() {
            super.reset();
            this.status = SC_OK;
            this.redirectLocation = null;
            setHeader(REQUEST_ID_HEADER, requestId);
        }

        @Override
        public int getStatus() {
            return status;
        }

        private String getRedirectLocation() {
            return redirectLocation;
        }
    }

    /**
     * 构建操作描述（增强版 - 提取敏感操作详情）
     */
    private String buildDescription(HttpServletRequest request, String method) {
        String uri = request.getRequestURI();
        String actionDesc = extractActionDescription(request, uri);
        return AuditLogSanitizer.description(request, method, actionDesc);
    }

    /**
     * 提取操作描述
     */
    private String extractActionDescription(HttpServletRequest request, String uri) {
        String action = request.getParameter("action");

        // 用户管理相关
        if (uri.contains("/admin/api/user/reset-password") || (uri.contains("/admin/member/") && "resetPassword".equals(action))) {
            return "重置用户密码";
        }
        if (uri.contains("/admin/api/user/add") || (uri.contains("/admin/member/") && "add".equals(action))) {
            return "添加新用户";
        }
        if (uri.contains("/admin/api/user?action=update") || uri.contains("/admin/api/user/update")) {
            return "更新用户信息";
        }
        if (uri.contains("/admin/api/user?action=delete") || uri.contains("/admin/api/user/delete")) {
            return "删除用户";
        }
        if (uri.contains("/admin/member/") && "update".equals(action)) {
            return "更新成员信息";
        }
        if (uri.contains("/admin/member/") && "delete".equals(action)) {
            return "删除成员";
        }

        // 新闻管理相关
        if (uri.contains("/news") && "create".equals(action)) {
            return "创建新闻";
        }
        if (uri.contains("/news") && "update".equals(action)) {
            return "更新新闻";
        }
        if (uri.contains("/news") && "delete".equals(action)) {
            return "删除新闻";
        }
        if (uri.contains("/news") && "hardDelete".equals(action)) {
            return "彻底删除新闻";
        }

        // 活动管理相关
        if (uri.contains("/activity") && "create".equals(action)) {
            return "创建活动";
        }
        if (uri.contains("/activity") && "update".equals(action)) {
            return "更新活动";
        }
        if (uri.contains("/activity") && "delete".equals(action)) {
            return "删除活动";
        }
        if (uri.contains("/activity") && "approve".equals(action)) {
            return "审核通过报名";
        }
        if (uri.contains("/activity") && "reject".equals(action)) {
            return "驳回报名申请";
        }
        if (uri.contains("/activity") && "batchApprove".equals(action)) {
            return "批量审核通过报名";
        }
        if (uri.contains("/activity") && "batchReject".equals(action)) {
            return "批量驳回报名申请";
        }

        // 项目管理相关
        if (uri.contains("/project") && "create".equals(action)) {
            return "创建项目";
        }
        if (uri.contains("/project") && "update".equals(action)) {
            return "更新项目信息";
        }
        if (uri.contains("/project") && "delete".equals(action)) {
            return "删除项目";
        }

        // 奖项管理相关
        if (uri.contains("/award") && "approve".equals(action)) {
            return "审核奖项申请";
        }
        if (uri.contains("/award") && "reject".equals(action)) {
            return "驳回奖项申请";
        }

        // 招新管理相关
        if (uri.contains("/admin/recruit") && uri.contains("/approve")) {
            return "审核通过招新申请";
        }
        if (uri.contains("/admin/recruit") && uri.contains("/reject")) {
            return "驳回招新申请";
        }
        if (uri.contains("/recruit/submit") && "POST".equals(request.getMethod())) {
            return "提交招新申请";
        }

        // 简历相关
        if (uri.contains("/resume") && "POST".equals(request.getMethod())) {
            return "更新简历信息";
        }

        // 个人资料相关
        if (uri.contains("/member/profile") || uri.contains("/admin/profile")) {
            return "更新个人资料";
        }
        if (uri.contains("/member/password") || uri.contains("/admin/password")) {
            return "修改密码";
        }

        // 文件上传相关
        if (uri.contains("/upload") || uri.contains("/file")) {
            String type = request.getParameter("type");
            if ("avatar".equals(type)) {
                return "上传头像";
            } else if ("award".equals(type)) {
                return "上传奖项图片";
            } else {
                return "上传文件";
            }
        }

        return null;
    }

    @Override
    public void destroy() {
    }
}

