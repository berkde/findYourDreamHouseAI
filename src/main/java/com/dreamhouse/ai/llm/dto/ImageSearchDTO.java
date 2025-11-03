package com.dreamhouse.ai.llm.dto;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;


public record ImageSearchDTO(String inferredDescription,
                             float[] queryVector,
                             List<HouseAdDTO> results) {
    public ImageSearchDTO {
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("Results cannot be null or empty");
        }
        if (!StringUtils.hasText(inferredDescription)) {
            throw new IllegalArgumentException("Inferred description cannot be null or empty");
        }
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("Query vector cannot be null or empty");
        }

        queryVector = Arrays.copyOf(queryVector, queryVector.length);
        results = List.copyOf(results);
    }

    @Override
    public String toString() {
        return "ImageSearchResult{" +
                "inferredDescription='" + inferredDescription + '\'' +
                ", queryVector=" + Arrays.toString(queryVector) +
                ", results=" + results.toString() +
                '}';
    }

}