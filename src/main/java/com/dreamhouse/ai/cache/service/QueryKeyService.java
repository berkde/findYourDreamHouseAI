package com.dreamhouse.ai.cache.service;

public interface QueryKeyService {
    String build(String namespace, int version, Object... parts);
    String lockKey(String namespace, int version, Object... parts);
}
