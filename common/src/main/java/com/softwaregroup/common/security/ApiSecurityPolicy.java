package com.softwaregroup.common.security;

import java.util.Set;
import java.util.regex.Pattern;

/** Explicit anonymous routes. Every other route requires a verified JWT. */
public final class ApiSecurityPolicy {
    private static final Set<String> HEALTH_ROUTES = Set.of(
            "/api/files/health", "/api/users/health", "/api/activities/health",
            "/api/attendance/health", "/api/study/health", "/api/recruit/health",
            "/api/resumes/health", "/api/problems/health", "/api/logs/health"
    );
    private static final Pattern PUBLIC_NEWS_DETAIL = Pattern.compile("^/api/news/[1-9][0-9]*$");
    private static final Pattern PUBLIC_RESOURCE_DETAIL = Pattern.compile("^/api/(activities|projects|awards)/[1-9][0-9]*$");
    private static final Pattern PUBLIC_AWARD_IMAGES = Pattern.compile("^/api/awards/[1-9][0-9]*/images$");
    private static final Pattern PUBLIC_NEWS_TYPE = Pattern.compile("^/api/news/type/[^/]+$");

    private ApiSecurityPolicy() {
    }

    public static boolean isPublic(String method, String servletPath) {
        String path = normalize(servletPath);
        if ("OPTIONS".equals(method)) {
            return true;
        }
        if ("GET".equals(method)) {
            return path.equals("/actuator/health") || path.startsWith("/actuator/health/")
                    || path.equals("/actuator/info") || HEALTH_ROUTES.contains(path)
                    || path.equals("/api/news") || PUBLIC_NEWS_DETAIL.matcher(path).matches()
                    || PUBLIC_NEWS_TYPE.matcher(path).matches()
                    || path.equals("/api/activities") || path.equals("/api/projects") || path.equals("/api/awards")
                    || PUBLIC_RESOURCE_DETAIL.matcher(path).matches() || PUBLIC_AWARD_IMAGES.matcher(path).matches()
                    || path.equals("/api/recruit/years");
        }
        return "POST".equals(method) && (path.equals("/api/users/login")
                || path.equals("/api/users/register") || path.equals("/api/recruit/apply"));
    }

    private static String normalize(String path) {
        if (path == null) {
            return "";
        }
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
}
