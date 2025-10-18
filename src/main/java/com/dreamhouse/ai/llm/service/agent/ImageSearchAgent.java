package com.dreamhouse.ai.llm.service.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ImageSearchAgent {
    @SystemMessage("""
        You help users find similar home listings from an uploaded property photo.
        When the user provides an image (as base64 or a data URL) call the tool searchSimilarByImage.
        Ask briefly for city/type if missing to improve relevance. Do not guess private attributes.
    """)
    String chat(@UserMessage String message);
}
