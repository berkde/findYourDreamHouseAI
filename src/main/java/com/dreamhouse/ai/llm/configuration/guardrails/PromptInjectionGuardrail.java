package com.dreamhouse.ai.llm.configuration.guardrails;

import com.dreamhouse.ai.llm.util.GuardrailUtil;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PromptInjectionGuardrail implements InputGuardrail {
    private static final List<Pattern> BLOCK_PATTERNS = List.of(
            Pattern.compile("(?i)ignore\\s+previous\\s+instructions"),
            Pattern.compile("(?i)disregard\\s+all\\s+prior\\s+rules"),
            Pattern.compile("(?i)jailbreak"),
            Pattern.compile("(?i)do\\s+not\\s+follow\\s+system\\s+prompt"),
            Pattern.compile("(?i)\\{\\{.*system.*\\}\\}")
    );

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        var textFromUserMessage = GuardrailUtil.textFromUserMessage(request);
        if (textFromUserMessage.isBlank())
            return failure("Empty message.");
        for (Pattern p : BLOCK_PATTERNS) {
            if (p.matcher(textFromUserMessage).find()) {
                return failure("Prompt injection detected.");
            }
        }
        return InputGuardrailResult.success();
    }
}
