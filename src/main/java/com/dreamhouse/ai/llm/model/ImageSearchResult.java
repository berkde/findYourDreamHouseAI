package com.dreamhouse.ai.llm.model;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import org.springframework.util.StringUtils;

import java.util.List;


public record ImageSearchResult(String inferredDescription,
                                float[] queryVector,
                                List<HouseAdDTO> results) {
    public ImageSearchResult {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("Results cannot be null or empty");
        }

        if (!StringUtils.hasText(inferredDescription)) {
            throw new IllegalArgumentException("Inferred description cannot be null or empty");
        }
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("Query vector cannot be null or empty");
        }
    }

}