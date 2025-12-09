package com.dreamhouse.ai.llm.listener;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Objects;


public sealed interface AIListener extends ChatModelListener
        permits HouseSearchListener, ImageSearchListener{
    Logger log = LoggerFactory.getLogger(AIListener.class);

    default void onRequest(@NotNull ChatModelRequestContext requestContext) {
        var request = requestContext.chatRequest();
        var model = request.modelName();
        var messages = request.messages().stream().filter(Objects::nonNull).map(ChatMessage::type).toList().toString();
        log.info("Model :{} | Messages: {}" ,model, messages);
    }

    default void onResponse(@NotNull ChatModelResponseContext responseContext) {
        var response = responseContext.chatResponse();
        var model = response.modelName();
        var message = response.aiMessage().text();
        var input_token_count = response.tokenUsage().inputTokenCount();
        var output_token_count = response.tokenUsage().outputTokenCount();
        var total_token_count = response.tokenUsage().totalTokenCount();
        log.info("Model :{} | Message: {} | total input tokens: {} - total output tokens: {} - total tokens: {}" ,
                model, message, input_token_count, output_token_count, total_token_count);
    }

    default void onError(@NotNull ChatModelErrorContext errorContext) {
        var model = errorContext.modelProvider().name();
        var errorMessage= errorContext.error().getMessage();
        log.error( "Model: {} | Error: {}", model, errorMessage);
    }
}
