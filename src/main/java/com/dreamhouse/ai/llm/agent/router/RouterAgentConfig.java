package com.dreamhouse.ai.llm.agent.router;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterAgentConfig {
    private static final Logger log = LoggerFactory.getLogger(RouterAgentConfig.class);

    @Bean
    public RouterAgent routerAgent(@Qualifier("qwenChatModel") OllamaChatModel ollamaChatModel) {
        return AgenticServices
                .agentBuilder(RouterAgent.class)
                .chatModel(ollamaChatModel)
                .async(Boolean.TRUE)
                .outputKey("isChat")
                .beforeAgentInvocation(e -> {
                    var request = e.inputs().get("request");
                    log.info("Invoking RouterAgent with user request: {}", request);
                })
                .afterAgentInvocation(e -> {
                    var category = e.output();
                    log.info("RouterAgent categorized request as: {}", category);
                })
                .build();
    }

}
