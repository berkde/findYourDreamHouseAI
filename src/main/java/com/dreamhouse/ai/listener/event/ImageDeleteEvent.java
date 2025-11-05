package com.dreamhouse.ai.listener.event;

import com.dreamhouse.ai.cloud.exception.NoStorageKeysException;

import java.util.Objects;

public record ImageDeleteEvent(String storageKey) {
    public ImageDeleteEvent {
        Objects.requireNonNull(storageKey, "Malformed storage key");
        if(storageKey.trim().isEmpty()) {
            throw new NoStorageKeysException("Storage key cannot be empty");
        }
    }
}
