package com.dreamhouse.ai.listener.event;

import java.util.List;
import java.util.Objects;

public record ImageDeleteBatchEvent(List<String> storageKeys) {
    public ImageDeleteBatchEvent {
        Objects.requireNonNull(storageKeys, "Malformed storage keys supplied");
        storageKeys = List.copyOf(storageKeys);
    }
}
