package com.dreamhouse.ai.llm.model;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ImageSearchResult that)) return false;
        return Objects.deepEquals(queryVector, that.queryVector) && List.of(results).equals(that.results) && Objects.equals(inferredDescription, that.inferredDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inferredDescription, Arrays.hashCode(queryVector), results);
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