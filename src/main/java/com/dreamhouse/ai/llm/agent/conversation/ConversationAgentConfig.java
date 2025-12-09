package com.dreamhouse.ai.llm.agent.conversation;

import com.dreamhouse.ai.llm.configuration.guardrails.LengthAndRateGuardrail;
import com.dreamhouse.ai.llm.configuration.guardrails.OutputFormattingGuardrail;
import com.dreamhouse.ai.llm.configuration.guardrails.PromptInjectionGuardrail;
import com.dreamhouse.ai.llm.configuration.guardrails.SafetyGuardrail;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConversationAgentConfig {
    private static final Logger log = LoggerFactory.getLogger(ConversationAgentConfig.class);
    @Bean
    public ConversationalistAgent conversationalistAgent(@Qualifier("qwenChatModel") OllamaChatModel ollamaChatModel,
                                                         ChatMemoryProvider chatMemoryProvider,
                                                         LengthAndRateGuardrail lengthAndRateGuardrail,
                                                         PromptInjectionGuardrail promptInjectionGuardrail,
                                                         SafetyGuardrail safetyGuardrail,
                                                         OutputFormattingGuardrail outputFormattingGuardrail) {
        return AgenticServices
                .agentBuilder(ConversationalistAgent.class)
                .chatModel(ollamaChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .async(Boolean.TRUE)
                .inputGuardrails(lengthAndRateGuardrail, promptInjectionGuardrail, safetyGuardrail)
                .outputGuardrails(outputFormattingGuardrail)
                .beforeAgentInvocation(e -> {
                    var message = e.inputs().get("message");
                    log.info("Invoking ConversationalistAgent with message: {}", message);
                })
                .afterAgentInvocation(e -> {
                    var chatResponse = e.output();
                    log.info("ConversationalistAgent response: {}", chatResponse);
                })
                .outputKey("chat")
                .build();
    }

}
