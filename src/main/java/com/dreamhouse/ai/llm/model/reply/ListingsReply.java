package com.dreamhouse.ai.llm.model.reply;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record ListingsReply(@JsonProperty("houses") List<HouseAdDTO> houseDTOs,
                            @JsonProperty("summary") String summary) implements SearchReply {
    public ListingsReply {
        Objects.requireNonNull(summary, "Summary is required");
        Objects.requireNonNull(houseDTOs, "Houses are required");

        houseDTOs = !houseDTOs.isEmpty() ? List.copyOf(houseDTOs) : Collections.emptyList();
    }
}
