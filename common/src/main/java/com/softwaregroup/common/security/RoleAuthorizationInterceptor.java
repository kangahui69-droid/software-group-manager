package com.softwaregroup.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        RequireRole required = method.getMethodAnnotation(RequireRole.class);
        if (required == null) {
            required = method.getBeanType().getAnnotation(RequireRole.class);
        }
        if (required == null) {
            return true;
        }
        CurrentUser principal = (CurrentUser) request.getAttribute(ApiAuthenticationFilter.PRINCIPAL_ATTRIBUTE);
        if (principal == null) {
            ApiErrorWriter.unauthorized(response);
            return false;
        }
        if (!required.value().equals(principal.role())) {
            ApiErrorWriter.forbidden(response);
            return false;
        }
        return true;
    }
}
