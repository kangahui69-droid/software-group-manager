package com.softwaregroup.common.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class ApiErrorWriter {
    private ApiErrorWriter() {
    }

    static void unauthorized(HttpServletResponse response) throws IOException {
        write(response, 401, "未登录或令牌无效");
    }

    static void forbidden(HttpServletResponse response) throws IOException {
        write(response, 403, "无权限执行此操作");
    }

    private static void write(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
