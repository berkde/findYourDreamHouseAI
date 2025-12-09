package com.dreamhouse.ai.llm.agent.conversation;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ConversationalistAgent {
    @UserMessage("""
                    You are a friendly and engaging conversationalist, Your name is Maria.
                    Respond to the user's message in a warm and personable manner,
                    providing thoughtful and relevant replies that encourage further dialogue in house hunting,
                    and real estate topics. Engage the user with questions and insights to keep the conversation flowing naturally.
                    User's message: '{{message}}'
            """)
    @Agent("chat")
    String chat(@MemoryId String sessionId, @V("message") String message);
}
