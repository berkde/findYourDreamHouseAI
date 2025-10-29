package com.dreamhouse.ai.cache.service.impl;

import com.dreamhouse.ai.cache.service.CacheMonitoringService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;


@Service
public class CacheMonitoringServiceImpl implements CacheMonitoringService {
    
    private static final Logger log = LoggerFactory.getLogger(CacheMonitoringServiceImpl.class);
    
    private final CacheManager cacheManager;
    
    @Autowired
    public CacheMonitoringServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    /**
     * Logs statistics for all caches in the system.
     */
    @Scheduled(fixedRate = 300000)
    @Override
    public void logCacheStatistics() {
        log.info("=== Cache Statistics Report ===");

        cacheManager.getCacheNames().forEach(cacheName -> {
            CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
            if (cache != null) {
                CacheStats stats = cache.getNativeCache().stats();

                log.info(
                        String.format(
                                "Cache: %s - Size: %d, Hit Rate: %.2f%%, Miss Rate: %.2f%%, Evictions: %d, Load Time: %.2f ms",
                                cacheName,
                                cache.getNativeCache().estimatedSize(),
                                stats.hitRate() * 100,
                                stats.missRate() * 100,
                                stats.evictionCount(),
                                stats.totalLoadTime() / 1_000_000.0
                        )
                );
            } else {
                log.warn("Cache '{}' not found or is null.", cacheName);
            }
        });

        log.info("=== End Cache Statistics Report ===");
    }

    /**
     * Retrieves statistics for a specific cache.
     * @param cacheName the name of the cache to get statistics for
     * @return Map containing cache statistics
     */
    @Override
    public Map<String, Object> getCacheStatistics(String cacheName) {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache(cacheName);
        if (cache == null) {
            return Map.of("error", "Cache not found: " + cacheName);
        }
        
        CacheStats stats = cache.getNativeCache().stats();
        
        return Map.of(
            "cacheName", cacheName,
            "size", cache.getNativeCache().estimatedSize(),
            "hitRate", String.format("%.2f", stats.hitRate() * 100),
            "missRate", String.format("%.2f", stats.missRate() * 100),
            "hitCount", stats.hitCount(),
            "missCount", stats.missCount(),
            "evictionCount", stats.evictionCount(),
            "loadCount", stats.loadCount(),
            "totalLoadTime", stats.totalLoadTime() / 1_000_000.0 + "ms",
            "averageLoadTime", stats.totalLoadTime() / 1_000_000.0 + "ms"
        );
    }

    /**
     * Clears all caches in the system.
     */
    @Override
    public void clearAllCaches() {
        log.info("Clearing all caches...");
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
            log.info("Cleared cache: {}", cacheName);
        });
    }

    /**
     * Clears a specific cache by name.
     * @param cacheName the name of the cache to clear
     */
    @Override
    public void clearCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cleared cache: {}", cacheName);
        } else {
            log.warn("Cache not found: {}", cacheName);
        }
    }
}
