package com.dreamhouse.ai.listener.event;

import java.util.Objects;

public record UserRegisteredEvent(String username) {
    public UserRegisteredEvent {
        Objects.requireNonNull(username, "username cannot be null");
    }
}
