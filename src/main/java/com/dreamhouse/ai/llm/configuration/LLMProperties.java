package com.dreamhouse.ai.llm.configuration;

import io.micrometer.common.util.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "langchain4j.open-ai")
public record LLMProperties(
        String baseUrl,
        String apiKey,
        String model,
        Double temperature
) {
    public LLMProperties {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalArgumentException("API key cannot be null or blank");
        }
        if (StringUtils.isBlank(baseUrl)) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }
        if (StringUtils.isBlank(model)) {
            throw new IllegalArgumentException("Model cannot be null or blank");
        }
        if (temperature == null) {
            throw new IllegalArgumentException("Temperature cannot be null");
        }
    }

}
