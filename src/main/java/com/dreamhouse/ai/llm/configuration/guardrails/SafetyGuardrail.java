package com.dreamhouse.ai.llm.configuration.guardrails;

import com.dreamhouse.ai.llm.configuration.guardrails.properties.GuardrailProperties;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class SafetyGuardrail implements InputGuardrail {

    private final OllamaChatModel guardianModel;
    private final GuardrailProperties.ModerationProperties moderationProps;

    public SafetyGuardrail(@Qualifier("guardianModel") OllamaChatModel guardianModel,
                           GuardrailProperties guardrailProperties) {
        this.guardianModel = guardianModel;
        this.moderationProps = Objects.requireNonNull(
                guardrailProperties.moderation(),
                "guardrail.moderation must not be null");
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        if(moderationProps != null && !moderationProps.enabled()){
            return success();
        }

        String input = extractText(request);
        if (input.isBlank()) {
            return failure("Empty or unsupported message for safety check.");
        }

        if(isSafe(input)){
            return success();
        }

        var systemMessage = SystemMessage.systemMessage("""
            You are a STRICT input safety classifier.
            Return EXACTLY one word:
            - ALLOW  (safe or mildly unsafe)
            - BLOCK  (hate, self-harm, illegal activity, strong sexual content, graphic violence, etc.)
            """);
        var userMessage = UserMessage.userMessage(input);

        ChatResponse chatResponse = guardianModel.chat(systemMessage, userMessage);
        String label = chatResponse.aiMessage() != null ? chatResponse.aiMessage().text() : "";
        label = label.trim().toUpperCase(Locale.ROOT);

        return switch (label) {
            case "ALLOW" -> success();
            case "BLOCK" -> failure("Sorry, that query violates our guidelines.");
            default ->      failure("Safety classifier returned an unknown label.");
        };
    }

    @NotNull
    private String extractText(@NotNull InputGuardrailRequest req) {
        ChatMessage m = req.userMessage();
        if (m instanceof UserMessage um && um.singleText() != null) {
            return um.singleText().trim();
        }
        return "";
    }

    private boolean isSafe(@NotNull String input) {
        var lowerCaseInput = input.toLowerCase(Locale.ROOT);

        if(lowerCaseInput.length() < 40 &&
                (lowerCaseInput.contains("hi") ||
                        lowerCaseInput.contains("hello") ||
                        lowerCaseInput.contains("hey"))) {
            return Boolean.TRUE;
        }

        List<String> redFlags = List.of(
                "kill", "murder", "suicide", "bomb",
                "terrorist", "rape", "child porn", "cp",
                "shoot", "stab", "overdose", "self harm",
                "hack bank", "credit card dump", "gun instructions"
        );

        boolean hasRedFlag = redFlags.stream().anyMatch(lowerCaseInput::contains);
        return !hasRedFlag;

    }
}
