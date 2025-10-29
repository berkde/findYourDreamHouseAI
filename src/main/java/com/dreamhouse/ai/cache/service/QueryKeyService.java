package com.dreamhouse.ai.cache.service;

public interface QueryKeyService {
    /**
     * Builds a cache key from namespace, version, and additional parts.
     * @param namespace the namespace for the cache key
     * @param version the version number for the cache key
     * @param parts additional parts to include in the key
     * @return the constructed cache key string
     */
    String build(String namespace, int version, Object... parts);
    
    /**
     * Builds a lock key from namespace, version, and additional parts.
     * @param namespace the namespace for the lock key
     * @param version the version number for the lock key
     * @param parts additional parts to include in the key
     * @return the constructed lock key string
     */
    String lockKey(String namespace, int version, Object... parts);
}
