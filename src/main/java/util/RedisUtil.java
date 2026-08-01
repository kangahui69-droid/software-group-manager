package util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import config.Config;

/**
 * Redis 缓存工具类
 * 提供简单的缓存操作封装
 */
public class RedisUtil {

    private static JedisPool jedisPool;

    private static final int DEFAULT_REDIS_PORT = 6379;
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int DEFAULT_MAX_TOTAL = 20;
    private static final int DEFAULT_MAX_IDLE = 5;
    private static final int DEFAULT_MIN_IDLE = 2;

    static {
        initJedisPool();
    }

    private static void initJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(DEFAULT_MAX_TOTAL);
        poolConfig.setMaxIdle(DEFAULT_MAX_IDLE);
        poolConfig.setMinIdle(DEFAULT_MIN_IDLE);
        poolConfig.setTestOnBorrow(true);

        String host = Config.getProperty("redis.host", "localhost");
        int port = Config.getIntProperty("redis.port", DEFAULT_REDIS_PORT);
        String password = Config.getProperty("redis.password", null);
        int database = Config.getIntProperty("redis.database", 0);

        // 如果密码为空或为 "null" 字符串，设置 null
        if (password == null || password.trim().isEmpty() || "null".equalsIgnoreCase(password)) {
            password = null;
        }

        jedisPool = new JedisPool(poolConfig, host, port, DEFAULT_TIMEOUT, password, database);
    }

    /**
     * 获取 Jedis 连接
     */
    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * 设置缓存（带过期时间）
     * @param key 缓存key
     * @param value 缓存value
     * @param ttlSeconds 过期时间（秒）
     */
    public static String set(String key, String value, int ttlSeconds) {
        try (Jedis jedis = getJedis()) {
            return jedis.setex(key, ttlSeconds, value);
        }
    }

    /**
     * 设置缓存（不带过期时间）
     * @param key 缓存key
     * @param value 缓存value
     */
    public static String set(String key, String value) {
        try (Jedis jedis = getJedis()) {
            return jedis.set(key, value);
        }
    }

    /**
     * 获取缓存
     * @param key 缓存key
     * @return 缓存value，不存在返回null
     */
    public static String get(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.get(key);
        }
    }

    /**
     * 删除缓存
     * @param key 缓存key
     * @return 删除的key数量
     */
    public static Long del(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.del(key);
        }
    }

    /**
     * 设置过期时间
     * @param key 缓存key
     * @param seconds 过期时间（秒）
     * @return 是否成功
     */
    public static Long expire(String key, int seconds) {
        try (Jedis jedis = getJedis()) {
            return jedis.expire(key, seconds);
        }
    }

    /**
     * 判断 key 是否存在
     * @param key 缓存key
     * @return 是否存在
     */
    public static Boolean exists(String key) {
        try (Jedis jedis = getJedis()) {
            return jedis.exists(key);
        }
    }

    /**
     * 关闭连接池（应用关闭时调用）
     */
    public static void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}
