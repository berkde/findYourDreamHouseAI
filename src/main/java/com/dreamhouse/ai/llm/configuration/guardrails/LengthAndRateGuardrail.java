package com.dreamhouse.ai.llm.configuration.guardrails;

import com.dreamhouse.ai.llm.configuration.guardrails.properties.GuardrailProperties;
import com.dreamhouse.ai.llm.util.GuardrailUtil;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import io.micrometer.core.instrument.MeterRegistry;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LengthAndRateGuardrail implements InputGuardrail {
    private final GuardrailProperties.FormattingProperties formattingProperties;
    private final GuardrailProperties.RateLimitProperties  rateLimitProperties;
    private final MeterRegistry metrics;
    private final Cache<String, AtomicInteger> window;

    public LengthAndRateGuardrail(GuardrailProperties guardrailProperties, MeterRegistry registry) {
        this.formattingProperties = Objects.requireNonNull(
                guardrailProperties.formatting(),
                "guardrails.formatting must be configured"
        );
        this.rateLimitProperties = Objects.requireNonNull(
                guardrailProperties.rateLimit(),
                "guardrails.rate-limit must be configured"
        );
        this.metrics = registry;
        this.window = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        if(formattingProperties.enabled()){
            String textFromUserMessage = GuardrailUtil.textFromUserMessage(request);
            int maxInput = formattingProperties.maxInputChars();
            if (maxInput > 0 && textFromUserMessage.length() > maxInput) {
                metrics.counter("guardrails.input.too_long").increment();
                return failure("Your message is too long.");
            }
        }

        if (!rateLimitProperties.enabled()) {
            return success();
        }

        String userId = GuardrailUtil.userKey(request);
        var counter = window.get(userId, __ -> new AtomicInteger(0));
        int current = counter.incrementAndGet();

        if (rateLimitProperties.perMinute() > 0 &&
        current > rateLimitProperties.perMinute()) {
            metrics.counter("guardrails.input.rate_limited").increment();
            return failure("Rate limit exceeded. Please try again shortly.");
        }

        if (rateLimitProperties.burstSize() > 0 &&
                current > rateLimitProperties.burstSize()) {
            metrics.counter("guardrails.input.burst_blocked").increment();
            return failure("Too many requests in a short time. Please slow down.");
        }

        return success();
    }
}
