package com.dreamhouse.ai.llm.agent.router;

import com.dreamhouse.ai.llm.model.auxilary.RequestCategory;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RouterAgent {
    @UserMessage("""
                    You are an intelligent router that categorizes user requests into one of the following categories:
                    1. CHAT - General conversation, inquiries, or discussions.
                    2. SEARCH - Requests related to searching for house listings or properties.
                    Given the user request: '{{request}}', determine whether it falls under CHAT or SEARCH.
                    Respond with the appropriate category only.
                """)
    @Agent("classify")
    RequestCategory classify(@V("request") String request);
}
