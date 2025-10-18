package com.dreamhouse.ai.llm.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

//@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public record OpenAIProperties(@DefaultValue("") String apiKey) {
}
