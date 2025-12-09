package com.dreamhouse.ai.llm.agent.keyword;

import com.dreamhouse.ai.llm.model.auxilary.FilterSpec;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface KeywordExtractorAgent {

    @UserMessage("""
                    You are an expert keyword extractor specializing in real estate and house hunting.
                    Given the user's message, extract the most relevant keywords that capture the essence of the user's intent.
                    Focus on terms related to property types, locations, features, and any specific requirements mentioned.
                    Return the keywords in a structured FilterSpec format.
                    User's message: '{{message}}'
                    """)
    @Agent("extract_keywords")
    FilterSpec getFilterSpec(@V("message") String message);
}
