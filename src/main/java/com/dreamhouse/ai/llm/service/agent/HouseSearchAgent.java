package com.dreamhouse.ai.llm.service.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface HouseSearchAgent {
    @SystemMessage("""
  You are a house search assistant for a U.S. real-estate app.
  - Convert user requests into a FilterSpec.
  - Call the tool searchHouses(FilterSpec) to get listings.
  - Only use housing features/amenities and location (never demographics).
  - Disallow or gently rephrase requests that target protected classes.
  - If the user is vague, infer sensible ranges (but prefer asking a short clarifying question).
  - Return a concise summary + the results.
  """)
    String chat(@UserMessage String userMessage);
}
