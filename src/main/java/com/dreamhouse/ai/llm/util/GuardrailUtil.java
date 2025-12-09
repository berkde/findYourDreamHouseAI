package com.dreamhouse.ai.llm.util;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import org.jetbrains.annotations.NotNull;


public class GuardrailUtil {
    public GuardrailUtil(){}

    @NotNull
    public static String textFromUserMessage(@NotNull InputGuardrailRequest req) {
        ChatMessage m = req.userMessage();
        if (m instanceof UserMessage um && um.singleText() != null) {
            return um.singleText().trim();
        }
        return "";
    }

    @NotNull
    public static String textFromAiMessage( @NotNull OutputGuardrailRequest req) {
        var ai = req.responseFromLLM().aiMessage();
        return ai == null || ai.text() == null ? "" : ai.text().trim();
    }

    @NotNull
    public static String userKey(@NotNull InputGuardrailRequest request) {
        String memoryId = request.requestParams().invocationContext().chatMemoryId().toString();
        return memoryId != null ? memoryId : "anonymous";
    }
}
