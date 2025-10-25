package com.dreamhouse.ai.house.model.response;

import io.micrometer.common.lang.Nullable;

public record StoragePutResponse(String key, String url, @Nullable String thumbnailUrl) {
}
