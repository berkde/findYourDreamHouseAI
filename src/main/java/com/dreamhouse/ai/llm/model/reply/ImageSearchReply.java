package com.dreamhouse.ai.llm.model.reply;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public record ImageSearchReply(String inferredDescription,
                               float[] queryVector,
                               List<HouseAdDTO> results) {
    public ImageSearchReply {
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
    public boolean equals(Object o) {
        if (!(o instanceof ImageSearchReply(String description, float[] vector, List<HouseAdDTO> results1))) return false;
        return Objects.deepEquals(queryVector, vector) && List.of(results).equals(results1) && Objects.equals(inferredDescription, description);
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