package com.dreamhouse.ai.house.service.impl;

import com.dreamhouse.ai.house.exception.NoSecretKeyFoundException;
import com.dreamhouse.ai.house.exception.SecretFormatException;
import com.dreamhouse.ai.house.exception.SecretParsingException;
import com.dreamhouse.ai.house.service.SecretsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Service
public class SecretsServiceImpl implements SecretsService {
    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper mapper;

    public SecretsServiceImpl(SecretsManagerClient sm,
                              ObjectMapper mapper) {
        this.secretsManagerClient = sm;
        this.mapper = mapper;
    }

    @Cacheable(cacheNames = "secrets", key = "'raw:' + #secretId")
    public String getSecretRaw(String secretId) {
        var resp = secretsManagerClient.getSecretValue(b -> b.secretId(secretId).versionStage("AWSCURRENT"));
        var str = resp.secretString();
        if (str == null) throw new NoSecretKeyFoundException("Secret has no SecretString: " + secretId);
        return str;
    }

    @Override
    @Cacheable(cacheNames = "secrets", key = "'field:' + #secretId + ':' + #jsonField")
    public String getSecret(String secretId, String jsonField) {
        String raw = getSecretRaw(secretId);
        try {
            var node = mapper.readTree(raw);
            var field = node.get(jsonField);
            if (field == null || field.isNull())
                throw new NoSecretKeyFoundException("Field '" + jsonField + "' not found in secret: " + secretId);
            return field.asText();
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            if (jsonField == null || jsonField.isBlank()) return raw;
            throw new SecretFormatException("Secret is not JSON but a field was requested: " + secretId, e);
        } catch (Exception e) {
            throw new SecretParsingException("Failed to parse secret '" + secretId + "'", e);
        }
    }
}
