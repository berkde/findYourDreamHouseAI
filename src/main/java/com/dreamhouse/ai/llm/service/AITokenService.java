package com.dreamhouse.ai.llm.service;


import com.dreamhouse.ai.llm.entity.AITokenEntity;
import com.dreamhouse.ai.llm.model.dto.AITokenDTO;

public interface AITokenService  {
    Boolean isTokenValid(String rawToken, String username);
    String generateToken(String userId, String planCode);
    AITokenEntity getToken(String token);
    AITokenDTO updateTokenPlanAndQuota(String token, String planCode);
    int consumeQuota(String token, String userId);
    String ensureFreemiumTokenIfMissing(String username);
}
