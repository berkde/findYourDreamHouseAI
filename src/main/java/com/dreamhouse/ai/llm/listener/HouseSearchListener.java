package com.dreamhouse.ai.llm.listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.springframework.stereotype.Component;

@Component("houseSearchListener")
public record HouseSearchListener() implements ChatModelListener {
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        ChatModelListener.super.onRequest(requestContext);
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        ChatModelListener.super.onResponse(responseContext);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        ChatModelListener.super.onError(errorContext);
    }
}
