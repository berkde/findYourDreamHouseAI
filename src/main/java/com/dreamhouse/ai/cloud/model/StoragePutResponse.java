package com.dreamhouse.ai.cloud.model;

import io.micrometer.common.lang.Nullable;

public record StoragePutResponse(String key, String url, @Nullable String thumbnailUrl) {
}
