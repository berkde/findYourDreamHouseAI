package com.dreamhouse.ai.llm.configuration;

import com.dreamhouse.ai.llm.service.agent.HouseSearchAgent;
import com.dreamhouse.ai.llm.service.agent.ImageSearchAgent;
import com.dreamhouse.ai.llm.tool.HouseSearchTool;
import com.dreamhouse.ai.llm.tool.ImageSearchTool;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.service.AiServices;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;


@Configuration
//@EnableConfigurationProperties({OpenAIProperties.class})
@EnableConfigurationProperties({LLMProperties.class})
@EnableAsync
public class AiConfiguration {
//    private final OpenAIProperties openAIProperties;
//
//    public AiConfiguration(OpenAIProperties openAIProperties) {
//        this.openAIProperties = openAIProperties;
//    }

    private final LLMProperties llmProperties;

    public AiConfiguration(LLMProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    @Bean(name = "qwenChatModel")
    @Primary
    public ChatLanguageModel qwenChatModel() {

        return OpenAiChatModel.builder()
                .baseUrl(llmProperties.baseUrl())
                .apiKey(llmProperties.apiKey() == null || llmProperties.apiKey().isBlank() ? "dummy" : llmProperties.apiKey())
                .modelName(llmProperties.model())
                .temperature(llmProperties.temperature() == null ? 0.2 : llmProperties.temperature())
                .build();
    }

    @Bean("houseAgent")
    public HouseSearchAgent houseSearchAgent(ChatLanguageModel qwenModel, HouseSearchTool tool) {
        return AiServices.builder(HouseSearchAgent.class)
                .chatLanguageModel(qwenModel)
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
    public EmbeddingModel embeddingModel() {
        // You can use a smaller embedding model if served separately (e.g. bge-m3)
        return OpenAiEmbeddingModel.builder()
                .baseUrl(llmProperties.baseUrl())
                .apiKey(llmProperties.apiKey())
                // You can use a smaller embedding model if served separately (e.g. bge-m3)
                .modelName("text-embedding-3-small")
                .build();
    }

//    @Bean
//    public OpenAiChatModel openAiChatModel() {
//        return OpenAiChatModel.builder()
//                .apiKey(openAIProperties.apiKey())
//                .modelName("gpt-4o-mini")
//                .temperature(0.2)
//                .build();
//    }
//
//    @Bean
//    public EmbeddingModel openAiEmbeddingModel() {
//        return OpenAiEmbeddingModel.builder()
//                .apiKey(openAIProperties.apiKey())
//                .modelName("text-embedding-3-small")
//                .build();
//    }

}
