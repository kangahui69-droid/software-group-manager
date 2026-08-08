package com.softwaregroup.user.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 HttpServletRequest 包装器
 *
 * 用于在 Filter 中动态添加请求头，供 Controller 的 @RequestHeader 读取
 */
public class CustomHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> customHeaders = new HashMap<>();

    public CustomHttpServletRequest(HttpServletRequest request, int userId, String username, String role) {
        super(request);
        customHeaders.put("X-User-Id", String.valueOf(userId));
        customHeaders.put("X-Username", username);
        customHeaders.put("X-User-Role", role);
    }

    @Override
    public String getHeader(String name) {
        String value = customHeaders.get(name);
        if (value != null) {
            return value;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        // 合并自定义头和原始头
        Map<String, String> allHeaders = new HashMap<>(customHeaders);
        java.util.Collections.list(super.getHeaderNames()).forEach(name -> {
            if (!allHeaders.containsKey(name)) {
                allHeaders.put(name, super.getHeader(name));
            }
        });
        return Collections.enumeration(allHeaders.keySet());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String customValue = customHeaders.get(name);
        if (customValue != null) {
            return Collections.enumeration(Collections.singletonList(customValue));
        }
        return super.getHeaders(name);
    }
}
