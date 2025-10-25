package com.dreamhouse.ai.llm.service.agent;

import com.dreamhouse.ai.llm.model.reply.HouseSearchReply;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface HouseSearchAgent {
    @SystemMessage("""
    You are a house search assistant for a U.S. real-estate app.
    - ALWAYS call the tool searchHouses(FilterSpec) on every user request. Do not answer without calling it.
    - Build FilterSpec from the user's text with non-null values only.
    - Return all the objects in the correct format, prepare a short summary of the operation to pass it to the returned HouseSearchReply and return the object

    Example User Input: {
        I am looking for a 3 bedroom and 2 bathroom house near central park, NYC.
    }
    
  
    """)
    HouseSearchReply chat(@UserMessage String userMessage);
}
