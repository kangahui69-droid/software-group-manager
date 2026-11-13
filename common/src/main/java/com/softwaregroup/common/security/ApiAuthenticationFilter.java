package com.softwaregroup.common.security;

import com.softwaregroup.common.util.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** One fail-closed authentication boundary for every microservice API. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiAuthenticationFilter extends OncePerRequestFilter {
    public static final String PRINCIPAL_ATTRIBUTE = CurrentUser.class.getName();

    private final JwtService jwtService;

    public ApiAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean publicRoute = ApiSecurityPolicy.isPublic(request.getMethod(), path);
        String token = jwtService.extractTokenFromHeader(request.getHeader("Authorization"));
        if (token == null) {
            if (publicRoute) {
                chain.doFilter(request, response);
            } else {
                ApiErrorWriter.unauthorized(response);
            }
            return;
        }
        CurrentUser user;
        try {
            Claims claims = jwtService.parseToken(token);
            user = new CurrentUser(Integer.parseInt(claims.getSubject()),
                    claims.get("username", String.class), claims.get("role", String.class));
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
            ApiErrorWriter.unauthorized(response);
            return;
        }
        request.setAttribute(PRINCIPAL_ATTRIBUTE, user);
        chain.doFilter(request, response);
    }
}
