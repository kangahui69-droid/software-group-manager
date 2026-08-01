package util;

/**
 * Redis 缓存 Key 常量定义
 * 遵循规范：{service}:{entity}:{id} 或 {prefix}:{type}:{identifier}
 */
public class CacheConsts {

    // 缓存 key 前缀
    public static final String PREFIX_DICTIONARY = "dict:type:";
    public static final String PREFIX_USER_INFO = "user:info:";
    public static final String PREFIX_STATISTICS = "stat:";
    public static final String PREFIX_AI_HISTORY = "ai:history:";
    public static final String PREFIX_SESSION = "session:";

    // 缓存 TTL（秒）
    public static final int TTL_DICTIONARY = 86400;      // 字典表 24小时
    public static final int TTL_USER_INFO = 1800;        // 用户信息 30分钟
    public static final int TTL_STATISTICS = 300;        // 统计数据 5分钟
    public static final int TTL_AI_HISTORY = 7200;       // AI对话历史 2小时
    public static final int TTL_SESSION = 1800;          // Session 30分钟
    public static final int TTL_DEFAULT = 3600;          // 默认 1小时

    private CacheConsts() {}
}
