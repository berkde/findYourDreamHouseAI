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


/**
 * Spring configuration for Large Language Model (LLM) integration.
 * Declares beans for chat models, embedding models, AI service agents, and
 * short-term chat memory. The configuration is backed by {@link LLMProperties}
 * and relies on an Ollama runtime for both chat and embeddings.
 * Key characteristics:
 * Chat model is configured with bounded context length and request timeout.
 * Listeners are attached for tool-calling use cases (house/image search).
 * Chat memory provider issues a per-session windowed memory with a max message count.
 * Embedding model uses the same Ollama base URL but a configurable model name.
 */
@Configuration
@EnableConfigurationProperties({LLMProperties.class})
public class LLMConfiguration {
    /**
     * Maximum number of tokens (context length) to allocate for the model's window.
     */
    private static final int CONTEXT_LENGTH = 2000;
    /**
     * Default timeout in minutes for chat/embedding requests to the model runtime.
     */
    private static final int TIMEOUT_MINUTES = 3;
    /**
     * Maximum number of recent messages to keep in the sliding window chat memory per session.
     */
    private static final int MAX_NUMBER_MESSAGES = 20;

    /**
     * Builds an {@link HouseSearchAgent} backed by the configured chat model and house search tool.
     * The agent is stateful per session via the provided chat memory provider.
     *
     * @param model               chat model to use for reasoning and tool calling
     * @param tool                domain tool exposing house search capabilities
     * @param chatMemoryProvider  provider that supplies per-session windowed chat memory
     * @return a configured {@link HouseSearchAgent}
     */
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

    /**
     * Builds an {@link ImageSearchAgent} backed by the configured chat model and image search tool.
     * Shares the same chat memory provider bean which issues per-session memory windows.
     *
     * @param qwenModel           chat model to use for reasoning and tool calling
     * @param tool                tool exposing image search capabilities
     * @param chatMemoryProvider  provider that supplies per-session windowed chat memory
     * @return a configured {@link ImageSearchAgent}
     */
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

    /**
     * Creates the primary chat language model backed by Ollama.
     *
     * @param properties            application LLM properties; must provide model name, base URL and temperature
     * @param houseSearchListener   listener enabling tool-calling for house search flows
     * @param imageSearchListener   listener enabling tool-calling for image search flows
     * @return configured {@link ChatLanguageModel}
     * @throws IllegalStateException if {@code llm.model} is null or blank
     */
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
                .numCtx(CONTEXT_LENGTH)
                .listeners(List.of(houseSearchListener, imageSearchListener))
                .timeout(Duration.ofMinutes(TIMEOUT_MINUTES))
                .logRequests(Boolean.TRUE)
                .logResponses(Boolean.TRUE)
                .build();
    }


    /**
     * Creates an {@link OllamaEmbeddingModel} pointing to the configured Ollama base URL
     * and the embeddings model defined in properties.
     *
     * @param llmProperties properties supplying base URL and embedding model name
     * @return a configured embedding model client
     */
    @Bean
    public OllamaEmbeddingModel embeddingModel(LLMProperties llmProperties) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(llmProperties.nativeBaseUrl())
                .modelName(llmProperties.embeddingModelName())
                .timeout(Duration.ofMinutes(3))
                .build();
    }


    /**
     * Provides an in-memory chat memory store suitable for single-node or ephemeral environments.
     * For production, replace with a distributed store if conversations must persist across nodes.
     *
     * @return a chat memory store implementation
     */
    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * Supplies a {@link ChatMemoryProvider} that creates a {@link MessageWindowChatMemory}
     * per session id. If no session id is provided, the conversation is tracked under
     * the {@code anonymous} id. The window size is limited by {@link #MAX_NUMBER_MESSAGES}.
     *
     * @param store backing store used to persist chat memories
     * @return a provider that yields per-session windowed chat memories
     */
    @Bean("houseChatMemoryProvider")
    public ChatMemoryProvider houseChatMemoryProvider(ChatMemoryStore store) {
        return sessionId -> new MessageWindowChatMemory.Builder()
                .id(sessionId != null ? sessionId : "anonymous")
                .maxMessages(MAX_NUMBER_MESSAGES)
                .chatMemoryStore(store)
                .build();
    }

}
