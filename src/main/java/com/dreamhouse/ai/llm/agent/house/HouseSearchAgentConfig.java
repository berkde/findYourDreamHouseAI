package com.dreamhouse.ai.llm.agent.house;

import com.dreamhouse.ai.llm.tool.HouseSearchTool;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class HouseSearchAgentConfig {
    private static final Logger log = LoggerFactory.getLogger(HouseSearchAgentConfig.class);

    @Bean
    public HouseSearchAgent houseSearchAgent(
            @Qualifier("qwenChatModel") OllamaChatModel ollamaChatModel,
            HouseSearchTool houseSearchTool) {
        return AgenticServices
                .agentBuilder(HouseSearchAgent.class)
                .chatModel(ollamaChatModel)
                .tools(houseSearchTool)
                .async(Boolean.TRUE)
                .beforeAgentInvocation(e -> {
                    var filterSpec = e.inputs().get("filterSpec");
                    var houseSearchDTO = e.inputs().get("houseSearchDTO");
                    log.info("Invoking HouseSearchAgent with FilterSpec: {} and houseSearchDTO: {}", filterSpec, houseSearchDTO);
                })
                .afterAgentInvocation(e -> {
                    var houseSearchResults = e.output();
                    log.info("HouseSearchAgent returned {} house ads",
                            houseSearchResults instanceof List<?> list ? list.size() : 0);
                })
                .outputKey("houseSearchResults")
                .build();
    }
}
