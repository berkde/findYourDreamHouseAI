package com.dreamhouse.ai.listener.event;


import com.dreamhouse.ai.cloud.exception.NoStorageKeysException;

import java.util.List;
import java.util.Objects;

public record ImageDeleteBatchEvent(List<String> storageKeys) {
    public ImageDeleteBatchEvent {
        Objects.requireNonNull(storageKeys, "Malformed storage keys supplied");
        if (storageKeys.isEmpty()) {
            throw new NoStorageKeysException("No storage keys supplied");
        }
        storageKeys = List.copyOf(storageKeys);
    }
}
