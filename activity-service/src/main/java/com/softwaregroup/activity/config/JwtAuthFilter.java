package com.softwaregroup.activity.config;

import com.softwaregroup.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 公开路径跳过认证
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(response, "缺少认证令牌");
            return;
        }

        String token = authHeader.substring(7);
        try {
            // 验证令牌
            if (!JwtUtil.validateToken(token)) {
                sendUnauthorized(response, "无效的令牌");
                return;
            }

            int userId = JwtUtil.getUserId(token);
            String username = JwtUtil.getUsername(token);
            String role = JwtUtil.getRole(token);

            // 将用户信息设置到请求属性，供后续使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            // 设置 Spring Security 上下文
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            sendUnauthorized(response, "令牌验证失败: " + e.getMessage());
        }
    }

    private boolean isPublicPath(String path) {
        return path.endsWith("/health") ||
               path.startsWith("/api/activities") ||  // 所有活动相关路径
               path.startsWith("/api/attendance") ||   // 所有考勤相关路径
               path.startsWith("/api/study");          // 所有学习相关路径
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
