package com.dreamhouse.ai.llm.configuration.guardrails.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "guardrails")
public record GuardrailProperties(
        ModerationProperties moderation,
        InjectionProperties injection,
        RelevanceProperties relevance,
        FormattingProperties formatting,
        RateLimitProperties rateLimit
) {

    public record ModerationProperties(
            boolean enabled,
            double toxicityThreshold
    ) {}

    public record InjectionProperties(
            boolean enabled,
            boolean strictMode
    ) {}

    public record RelevanceProperties(
            boolean enabled,
            double minCosine
    ) {}

    public record FormattingProperties(
            boolean enabled,
            int maxInputChars,
            int maxOutputChars,
            boolean jsonOutputEnabled
    ) {}

    public record RateLimitProperties(
            boolean enabled,
            int perMinute,
            int burstSize
    ) {}
}

