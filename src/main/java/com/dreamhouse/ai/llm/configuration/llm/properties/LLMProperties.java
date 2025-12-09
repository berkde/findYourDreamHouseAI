package com.dreamhouse.ai.llm.configuration.llm.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.common.util.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LLMProperties(
        @JsonProperty("base-url") String baseUrl,
        @JsonProperty("native-base-url") String nativeBaseUrl,
        @JsonProperty("api-key") String apiKey,
        @JsonProperty("model") String model,
        @JsonProperty("temperature") Double temperature,
        @JsonProperty("embedding-model-name") String embeddingModelName
) {
    public LLMProperties {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("API key cannot be null or blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }
        if (StringUtils.isBlank(nativeBaseUrl)) {
            throw new IllegalArgumentException("Native Base URL cannot be null or blank");
        }
        if (StringUtils.isBlank(model)) {
            throw new IllegalArgumentException("Model cannot be null or blank");
        }
        if (temperature == null) {
            throw new IllegalArgumentException("Temperature cannot be null");
        }
        if (embeddingModelName == null) {
            throw new IllegalArgumentException("Embedding model name cannot be null");
        }
    }

}
