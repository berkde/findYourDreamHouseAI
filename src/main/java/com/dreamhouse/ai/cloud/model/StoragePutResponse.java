package com.dreamhouse.ai.cloud.model;

import io.micrometer.common.lang.Nullable;

import java.util.Objects;

public record StoragePutResponse(String key, String url, @Nullable String thumbnailUrl) {
    public StoragePutResponse {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(url, "url cannot be null");
    }
}
