package com.dreamhouse.ai.cloud.service;

public interface SecretsService {
    String getSecret(String secretId, String jsonField);
}
