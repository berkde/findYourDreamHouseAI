package com.dreamhouse.ai.listener.event;

import java.util.Objects;

public record UserRegisteredEvent(String username) {
    public UserRegisteredEvent {
        Objects.requireNonNull(username, "username cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserRegisteredEvent(String username1))) return false;
        return Objects.equals(username, username1);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
