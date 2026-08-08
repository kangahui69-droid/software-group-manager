package com.softwaregroup.file.config;

import com.softwaregroup.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * 从 Authorization 头提取 JWT Token，验证并解析用户信息，
 * 将用户ID设置到请求属性中，传递给后续处理
 */
@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    // 公开路径（不需要认证）
    private static final String[] PUBLIC_PATHS = {
        "/api/files/health",
        "/actuator"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 公开路径跳过认证
        if (isPublicPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取 Authorization
        String authHeader = request.getHeader("Authorization");
        String token = JwtUtil.extractTokenFromHeader(authHeader);

        if (token != null) {
            try {
                // 验证 Token
                if (JwtUtil.validateToken(token)) {
                    // 解析 Token，获取用户信息
                    int userId = JwtUtil.getUserId(token);
                    String username = JwtUtil.getUsername(token);
                    String role = JwtUtil.getRole(token);

                    // 将用户信息设置到请求属性中
                    request.setAttribute("userId", userId);
                    request.setAttribute("username", username);
                    request.setAttribute("role", role);

                    // 同时设置到响应头，供上游服务使用
                    response.setHeader("X-User-Id", String.valueOf(userId));
                    response.setHeader("X-Username", username);
                    response.setHeader("X-User-Role", role);
                }
            } catch (Exception e) {
                // JWT 解析失败，但不影响继续处理（某些接口可能允许匿名访问）
                logger.debug("JWT 解析失败: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String requestUri) {
        for (String path : PUBLIC_PATHS) {
            if (requestUri.contains(path)) {
                return true;
            }
        }
        return false;
    }
}
