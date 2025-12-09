package com.dreamhouse.ai.cache.service;

import java.util.Map;

public interface CacheMonitoringService {
    void logCacheStatistics();
    Map<String, Object> getCacheStatistics(String cacheName);
    void clearAllCaches();
    void clearCache(String cacheName);
}

