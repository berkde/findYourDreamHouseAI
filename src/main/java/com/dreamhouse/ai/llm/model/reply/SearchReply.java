package com.dreamhouse.ai.llm.model.reply;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public sealed interface SearchReply permits ListingsReply, ChatReply {}
