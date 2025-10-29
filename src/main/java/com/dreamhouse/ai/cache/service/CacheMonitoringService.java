package com.dreamhouse.ai.cache.service;

import java.util.Map;

public interface CacheMonitoringService {
    /**
     * Logs statistics for all caches in the system.
     */
    void logCacheStatistics();
    
    /**
     * Retrieves statistics for a specific cache.
     * @param cacheName the name of the cache to get statistics for
     * @return Map containing cache statistics
     */
    Map<String, Object> getCacheStatistics(String cacheName);
    
    /**
     * Clears all caches in the system.
     */
    void clearAllCaches();
    
    /**
     * Clears a specific cache by name.
     * @param cacheName the name of the cache to clear
     */
    void clearCache(String cacheName);
}

