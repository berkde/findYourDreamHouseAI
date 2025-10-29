package com.dreamhouse.ai.listener.event;

import java.util.Objects;

public record ImageDeleteEvent(String storageKey) {
    public ImageDeleteEvent {
        Objects.requireNonNull(storageKey, "Malformed storage key");
    }
}
