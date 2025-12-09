package com.dreamhouse.ai.llm.configuration.guardrails;

import com.dreamhouse.ai.llm.configuration.guardrails.properties.GuardrailProperties;
import com.dreamhouse.ai.llm.util.GuardrailUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class OutputFormattingGuardrail implements OutputGuardrail {

    private final GuardrailProperties.FormattingProperties formattingProperties;
    private final ObjectMapper mapper;

    public OutputFormattingGuardrail(GuardrailProperties properties, ObjectMapper mapper) {
        this.formattingProperties = properties.formatting();
        this.mapper = mapper;
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        String text = GuardrailUtil.textFromAiMessage(request);
        if (text.trim().isBlank()) {
            return success();
        }

        String original = text;
        text = stripCodeFences(text);

        int maxChars = formattingProperties.maxOutputChars();
        if (maxChars > 0 && text.length() > maxChars) {
            text = text.substring(0, maxChars);
        }

        if (formattingProperties.jsonOutputEnabled() && looksLikeJson(text)) {
            try {
                mapper.readTree(text);
            } catch (Exception e) {
                return failure("Invalid JSON format from model: " + e.getMessage());
            }
        }

        if (text.equals(original)) {
            return OutputGuardrailResult.success();
        }

        return OutputGuardrailResult.successWith(text);
    }

    @NotNull
    private String stripCodeFences(@NotNull String s) {
        String trimmed = s.trim();
        if (!trimmed.startsWith("```")) {
            return s;
        }

        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline > 0) {
            trimmed = trimmed.substring(firstNewline + 1);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        return trimmed.trim();
    }

    private boolean looksLikeJson(@NotNull String s) {
        String t = s.trim();
        return (t.startsWith("{") && t.endsWith("}"))
                || (t.startsWith("[") && t.endsWith("]"));
    }
}
