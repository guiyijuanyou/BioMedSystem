package com.cqutcm.biomed.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 统一缓存服务，优先使用 Redis，Redis 不可用时自动降级到本地内存。
 *
 * <pre>
 * 使用示例：
 *   List<Map> data = cacheService.get("herbs", Duration.ofMinutes(5),
 *       () -> herbMapper.findAll().stream().map(this::herbToMap).toList(),
 *       new TypeReference<List<Map>>() {});
 *
 *   // 写入后清除：
 *   cacheService.evict("herbs");
 *   cacheService.evictByPrefix("list:");  // 批量清除
 * </pre>
 */
@Service
public class CacheService {
    private static final Logger log = LoggerFactory.getLogger(CacheService.class);
    private static final String KEY_PREFIX = "biomed:cache:";

    // 本地内存降级缓存
    private final Map<String, LocalEntry<?>> localCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "cache-cleaner");
        t.setDaemon(true);
        return t;
    });

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private volatile boolean redisAvailable = true;

    public CacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        // 每 60 秒清理一次过期本地缓存
        cleaner.scheduleAtFixedRate(this::cleanExpiredLocal, 60, 60, TimeUnit.SECONDS);
    }

    // ==================== 读缓存 ====================

    /**
     * 从缓存获取，未命中时调用 loader 加载并写入缓存。
     *
     * @param key      缓存键（自动加 biomed:cache: 前缀）
     * @param ttl      过期时间
     * @param loader   加载数据的回调，仅缓存未命中时调用
     * @param typeRef  反序列化的类型引用
     */
    public <T> T get(String key, Duration ttl, Supplier<T> loader, TypeReference<T> typeRef) {
        String fullKey = KEY_PREFIX + key;

        // 尝试 Redis
        if (redisAvailable) {
            try {
                String json = redisTemplate.opsForValue().get(fullKey);
                if (json != null) {
                    return objectMapper.readValue(json, typeRef);
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }

        // 尝试本地
        @SuppressWarnings("unchecked")
        LocalEntry<T> localEntry = (LocalEntry<T>) localCache.get(fullKey);
        if (localEntry != null && !localEntry.isExpired()) {
            return localEntry.data;
        }

        // 加载
        T data = loader.get();
        if (data == null) return null;

        // 写入缓存
        put(fullKey, data, ttl);
        return data;
    }

    // ==================== 写缓存 ====================

    /**
     * 主动写入缓存。
     */
    public <T> void put(String key, T data, Duration ttl) {
        String fullKey = KEY_PREFIX + key;
        try {
            if (redisAvailable) {
                try {
                    String json = objectMapper.writeValueAsString(data);
                    redisTemplate.opsForValue().set(fullKey, json, ttl);
                } catch (Exception e) {
                    markRedisDown(e);
                }
            }
            // 同时写本地作为备用
            localCache.put(fullKey, new LocalEntry<>(data, ttl));
        } catch (Exception e) {
            log.warn("Cache put failed for key={}", key, e);
        }
    }

    // ==================== 清除缓存 ====================

    /** 清除单个缓存键 */
    public void evict(String key) {
        String fullKey = KEY_PREFIX + key;
        localCache.remove(fullKey);
        if (redisAvailable) {
            try {
                redisTemplate.delete(fullKey);
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
    }

    /** 批量清除：根据前缀匹配所有键（优先用 scan 避免阻塞） */
    public void evictByPrefix(String prefix) {
        String fullPrefix = KEY_PREFIX + prefix;

        // 清除本地
        localCache.keySet().removeIf(k -> k.startsWith(fullPrefix));

        // 清除 Redis
        if (redisAvailable) {
            try {
                Set<String> keys = ConcurrentHashMap.newKeySet();
                redisTemplate.executeWithStickyConnection(connection -> {
                    try (Cursor<byte[]> cursor = connection.scan(
                            ScanOptions.scanOptions().match(fullPrefix + "*").count(100).build())) {
                        while (cursor.hasNext()) {
                            keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                        }
                    }
                    return null;
                });
                if (!keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
    }

    /** 清除所有业务缓存 */
    public void evictAll() {
        // 清除本地
        localCache.clear();

        // 清除 Redis
        if (redisAvailable) {
            try {
                Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                markRedisDown(e);
            }
        }
    }

    // ==================== 降级 ====================

    public boolean isRedisAvailable() {
        if (!redisAvailable) {
            // 定期重试 Redis 连接
            try {
                redisTemplate.opsForValue().get(KEY_PREFIX + "health");
                redisAvailable = true;
                log.info("Redis reconnected, switching back from local cache");
            } catch (Exception ignored) {}
        }
        return redisAvailable;
    }

    private void markRedisDown(Exception e) {
        if (redisAvailable) {
            redisAvailable = false;
            log.warn("Redis unavailable, falling back to in-memory cache: {}", e.getMessage());
        }
    }

    private void cleanExpiredLocal() {
        localCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // ==================== 内部类型 ====================

    private static class LocalEntry<T> {
        final T data;
        final long expireAtMs;

        LocalEntry(T data, Duration ttl) {
            this.data = data;
            this.expireAtMs = System.currentTimeMillis() + ttl.toMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAtMs;
        }
    }
}
