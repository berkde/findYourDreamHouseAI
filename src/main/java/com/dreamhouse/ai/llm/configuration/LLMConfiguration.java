package com.dreamhouse.ai.llm.configuration;

import com.dreamhouse.ai.llm.agent.HouseSearchAgent;
import com.dreamhouse.ai.llm.agent.ImageSearchAgent;
import com.dreamhouse.ai.llm.listener.HouseSearchListener;
import com.dreamhouse.ai.llm.listener.ImageSearchListener;
import com.dreamhouse.ai.llm.tool.HouseSearchTool;
import com.dreamhouse.ai.llm.tool.ImageSearchTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.service.AiServices;


import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Objects;


@Configuration
@EnableConfigurationProperties({LLMProperties.class})
public class LLMConfiguration {

    @Bean(name = "qwenChatModel")
    @Primary
    public ChatLanguageModel qwenChatModel(LLMProperties properties,
                                           @Qualifier("houseSearchListener") HouseSearchListener houseSearchListener,
                                           @Qualifier("imageSearchListener") ImageSearchListener imageSearchListener) {
        String model = Objects.requireNonNull(properties.model(), "llm.model is null").trim();
        if (model.isEmpty()) throw new IllegalStateException("llm.model is blank");

        return OllamaChatModel.builder()
                .baseUrl(properties.nativeBaseUrl())
                .modelName(properties.model())
                .temperature(properties.temperature())
                .listeners(List.of(houseSearchListener, imageSearchListener))
                .timeout(Duration.ofMinutes(3))
                .logRequests(Boolean.TRUE)
                .logResponses(Boolean.TRUE)
                .build();
    }

    @Bean("houseAgent")
    public HouseSearchAgent houseSearchAgent(ChatLanguageModel model,
                                             HouseSearchTool tool,
                                             @Qualifier("houseChatMemoryProvider")ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(HouseSearchAgent.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(tool)
                .build();
    }

    @Bean("imageAgent")
    public ImageSearchAgent imageSearchAgent(ChatLanguageModel qwenModel,
                                             ImageSearchTool tool,
                                             @Qualifier("houseChatMemoryProvider")ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(ImageSearchAgent.class)
                .chatLanguageModel(qwenModel)
                .chatMemoryProvider(chatMemoryProvider)
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

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    @Bean("houseChatMemoryProvider")
    public ChatMemoryProvider houseChatMemoryProvider(ChatMemoryStore store) {
        return sessionId -> new MessageWindowChatMemory.Builder()
                .id(sessionId != null ? sessionId : "anonymous")
                .maxMessages(20)
                .chatMemoryStore(store)
                .build();
    }

}
