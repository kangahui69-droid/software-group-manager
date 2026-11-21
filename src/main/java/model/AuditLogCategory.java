package model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** The values written by the WAR audit filter and offered by the log page. */
public final class AuditLogCategory {
    private AuditLogCategory() {
    }

    public enum Operation {
        GET, POST, PUT, PATCH, DELETE
    }

    public enum Module {
        ADMIN("管理员"), MEMBER("成员"), NEWS("新闻"), ACTIVITY("活动"),
        AWARD("奖项"), PROJECT("项目"), RECRUIT("招新"), OTHER("其他");

        private final String value;

        Module(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static Operation loggedOperation(String method) {
        return loggedOperation(method, null);
    }

    public static Operation loggedOperation(String method, String uri) {
        if (method == null) return null;
        if ("GET".equalsIgnoreCase(method)) {
            // Only admin reads of member records are audited. No export GET exists in this WAR.
            return uri != null && (uri.endsWith("/admin/member/list")
                    || uri.endsWith("/admin/member/view")
                    || uri.endsWith("/admin/member/edit")) ? Operation.GET : null;
        }
        try {
            return Operation.valueOf(method.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            // Other read methods are not audit operations.
            return null;
        }
    }

    public static Module moduleForUri(String uri) {
        if (uri == null) return Module.OTHER;
        if (uri.contains("/admin/")) return Module.ADMIN;
        if (uri.contains("/member/")) return Module.MEMBER;
        if (uri.contains("/news")) return Module.NEWS;
        if (uri.contains("/activity")) return Module.ACTIVITY;
        if (uri.contains("/award")) return Module.AWARD;
        if (uri.contains("/project")) return Module.PROJECT;
        if (uri.contains("/recruit")) return Module.RECRUIT;
        return Module.OTHER;
    }

    public static List<Operation> operations() {
        return Arrays.asList(Operation.values());
    }

    public static List<Module> modules() {
        return Arrays.stream(Module.values()).collect(Collectors.toList());
    }
}
