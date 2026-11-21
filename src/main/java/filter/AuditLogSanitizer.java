package filter;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import model.AuditLogCategory;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Keeps request-controlled secrets and personal data out of new audit rows. */
final class AuditLogSanitizer {
    private static final Pattern EMAIL = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");
    private static final Pattern PHONE = Pattern.compile("(?<![0-9])1[3-9][0-9]{9}(?![0-9])");
    private static final Pattern ID_CARD = Pattern.compile("(?<![0-9])[1-9][0-9]{16}[0-9Xx](?![0-9])");
    private static final Pattern ROUTE_PATTERN = Pattern.compile("[a-zA-Z0-9/_.*-]{1,100}");
    private static final Set<String> ALLOWED_ACTIONS = Set.of("list", "view", "edit", "create", "update",
            "delete", "approve", "reject", "batchApprove", "batchReject", "resetPassword",
            "enable", "disable", "submit");

    private AuditLogSanitizer() {
    }

    static String description(HttpServletRequest request, String method, String actionDescription) {
        if ("GET".equalsIgnoreCase(method)) {
            String uri = request.getRequestURI();
            return method + (uri != null && uri.endsWith("/admin/member/list")
                    ? " 查看成员列表" : " 查看成员资料");
        }
        if (actionDescription != null && !actionDescription.isEmpty()) {
            return method + " " + actionDescription;
        }

        // Servlet mapping patterns come from deployment configuration, not request paths.
        HttpServletMapping mapping = request.getHttpServletMapping();
        String route = mapping == null ? null : mapping.getPattern();
        if (route == null || !ROUTE_PATTERN.matcher(route).matches() || "/".equals(route)
                || "/*".equals(route)) {
            route = AuditLogCategory.moduleForUri(request.getRequestURI()).getValue() + "请求";
        }
        String action = allowedQueryAction(request.getQueryString());
        return method + " " + route + (action == null ? "" : " [action=" + action + "]");
    }

    /** Query values are discarded except for a fixed, harmless action vocabulary. */
    private static String allowedQueryAction(String query) {
        if (query == null) return null;
        for (String field : query.split("&")) {
            if (!field.startsWith("action=")) continue;
            String value = field.substring("action=".length());
            if (ALLOWED_ACTIONS.contains(value)) return value;
        }
        return null;
    }

    static String maskedUsername(String username) {
        if (username == null) return null;
        if (username.length() > 64 || username.chars().anyMatch(c -> Character.isWhitespace(c)
                || c == '=' || c == ':' || c == '?' || c == '&' || c == '/' || c == '\\')) {
            return "[用户名已隐藏]";
        }
        if (username.matches("[A-Za-z0-9_-]{24,}")) return "[用户名已隐藏]";
        String masked = ID_CARD.matcher(username).replaceAll("[身份证号已隐藏]");
        masked = PHONE.matcher(masked).replaceAll("[手机号已隐藏]");
        return EMAIL.matcher(masked).replaceAll("[邮箱已隐藏]");
    }

    static String safeUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return null;
        String value = userAgent.toLowerCase(Locale.ROOT);
        if (value.contains("edg/")) return "Edge";
        if (value.contains("chrome/")) return "Chrome";
        if (value.contains("firefox/")) return "Firefox";
        if (value.contains("safari/")) return "Safari";
        if (value.contains("curl/")) return "curl";
        if (value.contains("postmanruntime/")) return "Postman";
        return "其他客户端";
    }

    static String safeTargetId(String id) {
        if (id == null) return null;
        String value = id.trim();
        if (!value.matches("[1-9][0-9]{0,9}")) return null;
        try {
            return Long.parseLong(value) <= Integer.MAX_VALUE ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
