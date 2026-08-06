package com.softwaregroup.user.config;

import com.softwaregroup.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * 从请求头中提取JWT Token，验证并解析用户信息，
 * 将用户ID和角色设置到请求头中，传递给后续处理
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // 白名单：不需要认证的路径
    private static final String[] WHITE_LIST = {
            "/api/users/login",
            "/api/users/register",
            "/api/users/health"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 排除 actuator 端点
        if (path.startsWith("/actuator")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 白名单检查
        if (isWhiteListed(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取Token
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);

        if (token == null) {
            // 没有Token，返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
            return;
        }

        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return;
        }

        // 解析Token，获取用户信息
        try {
            int userId = JwtUtil.getUserId(token);
            String username = JwtUtil.getUsername(token);
            String role = JwtUtil.getRole(token);

            // 将用户信息设置到请求头中，传递给Controller
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            // 同时设置到HTTP头，供Nginx或其他服务使用
            response.setHeader("X-User-Id", String.valueOf(userId));
            response.setHeader("X-Username", username);
            response.setHeader("X-User-Role", role);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JWT解析失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":401,\"message\":\"Token解析失败\"}");
        }
    }

    private boolean isWhiteListed(String requestUri) {
        for (String path : WHITE_LIST) {
            if (requestUri.endsWith(path)) {
                return true;
            }
        }
        return false;
    }
}
