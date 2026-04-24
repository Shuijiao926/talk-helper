package com.talkhelper.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.talkhelper.common.util.ThRedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存服务（L1: Caffeine + L2: Redis）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThMultiLevelCache {

    private final ThRedisUtils redisUtils;

    // L1缓存：Caffeine本地缓存（热点数据，最大1000条，过期时间5分钟）
    private final Cache<String, Object> l1Cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats() // 记录命中率统计
            .build();

    /**
     * 获取缓存（多级查询）
     * 
     * @param key 缓存键
     * @param loader 数据加载器（缓存未命中时执行）
     * @param ttl Redis过期时间（秒）
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> loader, long ttl) {
        // L1: 查询Caffeine本地缓存
        Object cached = l1Cache.getIfPresent(key);
        if (cached != null) {
            log.debug("L1缓存命中: {}", key);
            return (T) cached;
        }

        // L2: 查询Redis分布式缓存
        String redisKey = "cache:" + key;
        cached = redisUtils.get(redisKey);
        if (cached != null) {
            log.debug("L2缓存命中: {}", key);
            // 回填L1缓存
            l1Cache.put(key, cached);
            return (T) cached;
        }

        // L3: 缓存未命中，执行loader加载数据
        log.debug("缓存未命中，加载数据: {}", key);
        T data = loader.get();
        
        if (data != null) {
            // 写入L2缓存（Redis）
            redisUtils.set(redisKey, data, ttl, TimeUnit.SECONDS);
            // 写入L1缓存（Caffeine）
            l1Cache.put(key, data);
            log.debug("数据已缓存: {}, TTL={}s", key, ttl);
        }
        
        return data;
    }

    /**
     * 获取缓存（默认TTL 1小时）
     */
    public <T> T get(String key, Supplier<T> loader) {
        return get(key, loader, 3600);
    }

    /**
     * 更新缓存
     */
    public void put(String key, Object value, long ttl) {
        // 写入L1
        l1Cache.put(key, value);
        
        // 写入L2
        String redisKey = "cache:" + key;
        redisUtils.set(redisKey, value, ttl, TimeUnit.SECONDS);
        
        log.debug("缓存已更新: {}", key);
    }

    /**
     * 删除缓存
     */
    public void evict(String key) {
        // 删除L1
        l1Cache.invalidate(key);
        
        // 删除L2
        String redisKey = "cache:" + key;
        redisUtils.delete(redisKey);
        
        log.debug("缓存已删除: {}", key);
    }

    /**
     * 批量删除缓存（支持通配符）
     */
    public void evictPattern(String pattern) {
        String redisPattern = "cache:" + pattern;
        redisUtils.deleteByPattern(redisPattern);
        
        // Caffeine不支持通配符删除，清空全部（简单实现）
        l1Cache.invalidateAll();
        
        log.debug("批量删除缓存: {}", pattern);
    }

    /**
     * 获取缓存统计信息
     */
    public String getStats() {
        return String.format("L1命中率: %.2f%%, L1大小: %d",
                l1Cache.stats().hitRate() * 100,
                l1Cache.estimatedSize());
    }
}

