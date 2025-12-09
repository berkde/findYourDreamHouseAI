package com.dreamhouse.ai.llm.model.reply;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record ListingsReply(@JsonProperty("houseAdDTOs") List<HouseAdDTO> houses, @JsonProperty("agentReply") String agentReply) implements SearchReply {
    public ListingsReply {
        Objects.requireNonNull(agentReply, "agentReply can't be null");
        Objects.requireNonNull(houses, "Houses are required");
        houses = !houses.isEmpty() ? List.copyOf(houses) : Collections.emptyList();
    }
}
