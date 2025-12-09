package com.dreamhouse.ai.llm.agent.keyword;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeywordExtractorAgentConfig {
    private static final Logger log = LoggerFactory.getLogger(KeywordExtractorAgentConfig.class);

    @Bean
    public KeywordExtractorAgent keywordExtractorAgent(@Qualifier("qwenChatModel") OllamaChatModel ollamaChatModel) {
        return AgenticServices
                .agentBuilder(KeywordExtractorAgent.class)
                .chatModel(ollamaChatModel)
                .async(Boolean.TRUE)
                .beforeAgentInvocation(e -> {
                    var message = e.inputs().get("message");
                    log.info("Invoking KeywordExtractorAgent with user input: {}", message);
                })
                .afterAgentInvocation(e -> {
                    var filterSpec = e.output();
                    log.info("KeywordExtractorAgent produced FilterSpec: {}", filterSpec);
                })
                .outputKey("filterSpec")
                .build();
    }
}
