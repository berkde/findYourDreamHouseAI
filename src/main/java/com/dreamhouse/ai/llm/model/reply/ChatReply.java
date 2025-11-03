package com.dreamhouse.ai.llm.model.reply;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record ChatReply(@JsonProperty("agentReply") String agentReply) implements SearchReply {
    public ChatReply {
        Objects.requireNonNull(agentReply, "Agent reply is required");
    }
}
