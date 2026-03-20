package util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF Token 工具类
 * 用于生成和验证 CSRF Token
 */
public class CSRFTokenUtil {

    // CSRF Token 在 Session 中的属性名
    public static final String CSRF_TOKEN_SESSION_ATTR = "CSRF_TOKEN";


    // CSRF Token 在请求中的参数名
    public static final String CSRF_TOKEN_NAME = "_csrf";


    // CSRF Token 在请求头中的名称
    public static final String CSRF_TOKEN_HEADER = "X-CSRF-TOKEN";


    // Token 长度（字节）
    private static final int TOKEN_LENGTH = 32;


    // 安全随机数生成器
    private static final SecureRandom secureRandom = new SecureRandom();


    /**
     * 私有构造方法，防止实例化
     */
    private CSRFTokenUtil() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }


    /**
     * 生成新的 CSRF Token
     *
     * @return Base64 编码的随机 Token
     */
    public static String generateToken() {
        byte[] token = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }


    /**
     * 将 CSRF Token 存储到 Session 中
     *
     * @param session HttpSession 对象
     * @return 生成的 Token
     */
    public static String storeTokenInSession(HttpSession session) {
        String token = generateToken();
        session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
        return token;
    }

    /**
     * 从 Session 中获取 CSRF Token
     *
     * @param session HttpSession 对象
     * @return Token 字符串，如果不存在返回 null
     */
    public static String getTokenFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object token = session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        return token != null ? token.toString() : null;
    }

    /**
     * 从请求中获取 CSRF Token
     * 优先从请求头获取，其次从请求参数获取
     *
     * @param request HttpServletRequest 对象
     * @return Token 字符串，如果不存在返回 null
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        // 优先从请求头获取
        String headerToken = request.getHeader(CSRF_TOKEN_HEADER);
        if (headerToken != null && !headerToken.trim().isEmpty()) {
            return headerToken.trim();
        }

        // 从请求参数获取
        String paramToken = request.getParameter(CSRF_TOKEN_NAME);
        if (paramToken != null && !paramToken.trim().isEmpty()) {
            return paramToken.trim();
        }

        return null;
    }

    /**
     * 验证 CSRF Token 是否有效
     *
     * @param request HttpServletRequest 对象
     * @return true 验证通过，false 验证失败
     */
    public static boolean validateToken(HttpServletRequest request) {
        // 获取 Session 中的 Token
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = getTokenFromSession(session);
        if (sessionToken == null) {
            return false;
        }

        // 获取请求中的 Token
        String requestToken = getTokenFromRequest(request);
        if (requestToken == null) {
            return false;
        }

        // 比较 Token 是否匹配
        return sessionToken.equals(requestToken);
    }


    /**
     * 获取或创建 CSRF Token（用于表单页面）
     *
     * @param request HttpServletRequest 对象
     * @return Token 字符串
     */
    public static String getOrCreateToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String token = getTokenFromSession(session);
        if (token == null) {
            token = storeTokenInSession(session);
        }
        return token;
    }

    /**
     * 清除 Session 中的 CSRF Token
     *
     * @param session HttpSession 对象
     */
    public static void clearToken(HttpSession session) {
        if (session != null) {
            session.removeAttribute(CSRF_TOKEN_SESSION_ATTR);
        }
    }
}
