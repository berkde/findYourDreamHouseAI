package com.dreamhouse.ai.cloud.service;

public interface SecretsService {
    /**
     * Retrieves a secret value from AWS Secrets Manager.
     * @param secretId the identifier of the secret in AWS Secrets Manager
     * @param jsonField the JSON field name within the secret to retrieve
     * @return the secret value as a string
     */
    String getSecret(String secretId, String jsonField);
}
