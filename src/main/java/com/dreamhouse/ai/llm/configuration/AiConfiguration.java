package com.dreamhouse.ai.llm.configuration;

import com.dreamhouse.ai.llm.service.agent.HouseSearchAgent;
import com.dreamhouse.ai.llm.service.agent.ImageSearchAgent;
import com.dreamhouse.ai.llm.tool.HouseSearchTool;
import com.dreamhouse.ai.llm.tool.ImageSearchTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.service.AiServices;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Objects;


@Configuration
@EnableConfigurationProperties({LLMProperties.class})
public class AiConfiguration {

    @Bean(name = "qwenChatModel")
    @Primary
    public ChatLanguageModel qwenChatModel(LLMProperties llmProperties) {
        String model = Objects.requireNonNull(llmProperties.model(), "llm.model is null").trim();
        if (model.isEmpty()) throw new IllegalStateException("llm.model is blank");

        return OllamaChatModel.builder()
                .baseUrl(llmProperties.nativeBaseUrl())
                .modelName(System.getenv("LLM_MODEL"))
                .temperature(llmProperties.temperature())
                .timeout(Duration.ofMinutes(3))
                .logRequests(Boolean.TRUE)
                .logResponses(Boolean.TRUE)
                .build();
    }

    @Bean("houseAgent")
    public HouseSearchAgent houseSearchAgent(ChatLanguageModel model, HouseSearchTool tool) {
        return AiServices.builder(HouseSearchAgent.class)
                .chatLanguageModel(model)
                .tools(tool)
                .build();
    }

    @Bean("imageAgent")
    public ImageSearchAgent imageSearchAgent(ChatLanguageModel qwenModel, ImageSearchTool tool) {
        return AiServices.builder(ImageSearchAgent.class)
                .chatLanguageModel(qwenModel)
                .tools(tool)
                .build();
    }

    @Bean
    public OllamaEmbeddingModel embeddingModel(LLMProperties llmProperties) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(llmProperties.nativeBaseUrl())
                .modelName(llmProperties.embeddingModelName())
                .timeout(Duration.ofMinutes(3))
                .build();
    }



}
