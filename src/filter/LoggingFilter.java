package filter;

import dao.OperationLogDAO;
import model.OperationLog;
import model.User;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 操作日志过滤器
 */
public class LoggingFilter implements Filter {
    private OperationLogDAO logDAO = new OperationLogDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // 只记录POST、PUT、DELETE等修改操作
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            try {
                HttpSession session = httpRequest.getSession(false);
                User user = null;
                Integer userId = null;
                String username = "游客";

                if (session != null && session.getAttribute("user") != null) {
                    user = (User) session.getAttribute("user");
                    userId = user.getId();
                    username = user.getUsername();
                }

                OperationLog log = new OperationLog();
                log.setUserId(userId);
                log.setUsername(username);
                log.setOperation(method);
                log.setModule(extractModule(requestURI));
                log.setDescription(buildDescription(httpRequest, method));
                log.setIpAddress(getClientIpAddress(httpRequest));
                log.setUserAgent(httpRequest.getHeader("User-Agent"));

                logDAO.insert(log);
            } catch (Exception e) {
                e.printStackTrace();
                // 日志记录失败不应影响业务
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 提取模块名
     */
    private String extractModule(String uri) {
        if (uri.contains("/admin/")) {
            return "管理员";
        } else if (uri.contains("/member/")) {
            return "成员";
        } else if (uri.contains("/news")) {
            return "新闻";
        } else if (uri.contains("/activity")) {
            return "活动";
        } else if (uri.contains("/award")) {
            return "奖项";
        } else if (uri.contains("/project")) {
            return "项目";
        } else if (uri.contains("/recruit")) {
            return "招新";
        }
        return "其他";
    }

    /**
     * 构建操作描述
     */
    private String buildDescription(HttpServletRequest request, String method) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;
        return method + " " + fullPath;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public void destroy() {
    }
}

