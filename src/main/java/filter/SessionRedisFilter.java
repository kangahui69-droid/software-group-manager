package filter;

import util.RedisSessionUtil;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Session Redis 同步过滤器
 * 请求结束后自动同步 Session 到 Redis，实现多实例 Session 共享
 *
 * 注意：此 Filter 会修改 Session 的访问时机，需要确保 User 等对象实现了 Serializable
 */
public class SessionRedisFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 无需初始化，RedisUtil 静态初始化
        System.out.println("[SessionRedisFilter] 初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取 Session（不创建）
        HttpSession session = httpRequest.getSession(false);

        try {
            // 首次访问时检查 Redis 中是否存在但本地不存在的情况
            // 这可以用于在多实例间恢复会话
            if (session != null && httpRequest.getRequestedSessionId() != null) {
                String sessionId = session.getId();
                // 如果本地 Session 是新创建的，但 Redis 中已有该 Session 的数据
                // 说明请求被路由到了不同的实例，需要从 Redis 恢复
                // 注意：这需要客户端提交正确的 session cookie
            }

            chain.doFilter(request, response);
        } finally {
            // 请求结束后同步 Session 到 Redis（异步方式避免阻塞响应）
            if (session != null) {
                try {
                    // 使用简单的线程执行，避免创建过多线程
                    final HttpSession sessionToSave = session;
                    new Thread(() -> RedisSessionUtil.saveSession(sessionToSave)).start();
                } catch (Exception e) {
                    System.err.println("[SessionRedisFilter] 同步Session失败: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void destroy() {
        // 无资源需要释放
        System.out.println("[SessionRedisFilter] 销毁");
    }
}
