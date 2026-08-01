package util;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Base64;
import java.util.Enumeration;

/**
 * Redis Session 存储工具类
 * 用于将 HttpSession 的内容存储到 Redis，实现多实例 Session 共享
 */
public class RedisSessionUtil {

    private static final String SESSION_PREFIX = "session:";
    private static final int SESSION_TTL = 1800; // 30分钟，与 Tomcat session-timeout 一致

    /**
     * 将 Session 存储到 Redis
     */
    public static void saveSession(HttpSession session) {
        if (session == null) {
            return;
        }

        String sessionId = session.getId();
        String key = SESSION_PREFIX + sessionId;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            // 序列化所有 session 属性
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                Object value = session.getAttribute(name);
                // 跳过不可序列化的对象
                if (value instanceof Serializable) {
                    oos.writeObject(new SessionAttribute(name, value));
                } else {
                    System.err.println("[RedisSession] 跳过不可序列化属性: " + name + " (" + value.getClass().getName() + ")");
                }
            }

            byte[] data = baos.toByteArray();
            String base64Data = Base64.getEncoder().encodeToString(data);
            try (redis.clients.jedis.Jedis jedis = RedisUtil.getJedis()) {
                jedis.setex(key, SESSION_TTL, base64Data);
            }
        } catch (Exception e) {
            System.err.println("[RedisSession] 保存Session失败: " + e.getMessage());
        }
    }

    /**
     * 从 Redis 恢复 Session 属性
     * 注意：此方法需要与 Tomcat 的 Session 创建机制配合使用
     */
    public static void restoreSession(HttpSession session) {
        if (session == null) {
            return;
        }

        String sessionId = session.getId();
        String key = SESSION_PREFIX + sessionId;

        try (redis.clients.jedis.Jedis jedis = RedisUtil.getJedis()) {
            String base64Data = jedis.get(key);
            if (base64Data != null && !base64Data.isEmpty()) {
                byte[] data = Base64.getDecoder().decode(base64Data);
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {

                    while (bais.available() > 0) {
                        try {
                            SessionAttribute attr = (SessionAttribute) ois.readObject();
                            session.setAttribute(attr.name, attr.value);
                        } catch (ClassNotFoundException e) {
                            System.err.println("[RedisSession] 恢复Session属性失败: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[RedisSession] 读取Session失败: " + e.getMessage());
        }
    }

    /**
     * 检查 Redis 中是否存在指定的 Session
     */
    public static boolean existsSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        try (redis.clients.jedis.Jedis jedis = RedisUtil.getJedis()) {
            return jedis.exists(key);
        } catch (Exception e) {
            System.err.println("[RedisSession] 检查Session失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除 Session
     */
    public static void deleteSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        try (redis.clients.jedis.Jedis jedis = RedisUtil.getJedis()) {
            jedis.del(key);
        } catch (Exception e) {
            System.err.println("[RedisSession] 删除Session失败: " + e.getMessage());
        }
    }

    /**
     * 刷新 Session 过期时间
     */
    public static void refreshSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        try (redis.clients.jedis.Jedis jedis = RedisUtil.getJedis()) {
            jedis.expire(key, SESSION_TTL);
        } catch (Exception e) {
            System.err.println("[RedisSession] 刷新Session过期时间失败: " + e.getMessage());
        }
    }

    /**
     * Session 属性包装类（必须可序列化）
     */
    public static class SessionAttribute implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public Object value;

        public SessionAttribute(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
