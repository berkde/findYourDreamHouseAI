package com.dreamhouse.ai.llm.service.impl;

import com.dreamhouse.ai.authentication.repository.UserRepository;
import com.dreamhouse.ai.llm.entity.AITokenEntity;
import com.dreamhouse.ai.llm.exception.AITokenNotFoundException;
import com.dreamhouse.ai.llm.exception.QuotaExceededException;
import com.dreamhouse.ai.llm.model.dto.AITokenDTO;
import com.dreamhouse.ai.llm.repository.AITokenRepository;
import com.dreamhouse.ai.llm.service.AITokenService;
import com.dreamhouse.ai.llm.util.AIUtil;
import io.micrometer.common.util.StringUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
public class AITokenServiceImpl implements AITokenService {
    private static final Logger log = LoggerFactory.getLogger(AITokenServiceImpl.class);
    private static final int MAX_TOKEN_LIMIT = 1200;
    private final AITokenRepository aiTokenRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
    private final AIUtil aiUtil;

    @Autowired
    public AITokenServiceImpl(AITokenRepository aiTokenRepository,
                              UserRepository userRepository,
                              RedissonClient redissonClient,
                              AIUtil aiUtil) {
        this.aiTokenRepository = aiTokenRepository;
        this.userRepository = userRepository;
        this.redissonClient = redissonClient;
        this.aiUtil = aiUtil;
    }

    @Override
    public Boolean isTokenValid(String rawToken, String username) {
        if (StringUtils.isEmpty(rawToken)) {
            return Boolean.FALSE;
        }
        var stored_token = aiTokenRepository
                .findByToken(rawToken)
                .orElseThrow(() -> new AITokenNotFoundException("Token not found"));

        var stored_user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isNotExpired = stored_token.getExpiryDate().isAfter(LocalDateTime.now());
        boolean isWithingMaxPlanLimit = stored_token.getMonthlyQuota() <= MAX_TOKEN_LIMIT;
        boolean isOwnerMatching = stored_token.getUserId().equals(stored_user.getUserID());

        return  isNotExpired && isWithingMaxPlanLimit && isOwnerMatching;
    }

    @Override
    public String generateToken(String username, String planCode) {
        var token = UUID.randomUUID().toString().replaceAll("-", "");
        var aiTokenEntity = new AITokenEntity();
        aiTokenEntity.setToken(token);
        aiTokenEntity.setExpiryDate();
        aiTokenEntity.setMonthlyQuota();
        aiTokenEntity.setPlanCode(planCode);
        aiTokenEntity.setActive(Boolean.TRUE);

        var stored_user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        aiTokenEntity.setUserId(stored_user.getUserID());

        var savedTokenEntity = aiTokenRepository.save(aiTokenEntity);
        return savedTokenEntity.getToken();
    }

    @Override
    public AITokenEntity getToken(String userId) {
        return aiTokenRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AITokenNotFoundException("Token not found"));
    }

    @Override
    public AITokenDTO updateTokenPlanAndQuota(String token, String planCode) {
        var aiTokenEntity = aiTokenRepository.findByToken(token).orElseThrow(() -> new AITokenNotFoundException("Token not found"));
        aiTokenEntity.setPlanCode(planCode);
        aiTokenEntity.setMonthlyQuota();
        var savedToken = aiTokenRepository.save(aiTokenEntity);
        return new AITokenDTO(savedToken.getToken());
    }

    @Override
    public int consumeQuota(String token, String username) {
        if (!isTokenValid(token, username)) {
            throw new QuotaExceededException("Invalid or expired token");
        }

        String monthKey = YearMonth.now().toString();
        String usageKey = "ai:usage:" + token + ":" + monthKey;

        RAtomicLong usage = redissonClient.getAtomicLong(usageKey);

        if (usage.remainTimeToLive() <= 0) {
            long ttlMillis = aiUtil.millisUntilEndOfMonth();
            usage.expire(Duration.ofMillis(ttlMillis));
        }

        long current = usage.get();
        long after = usage.addAndGet(1);

        AITokenEntity tokenEntity = aiTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new AITokenNotFoundException("Token not found"));
        int monthlyQuota = tokenEntity.getMonthlyQuota();


        if (current + 1 > monthlyQuota) {
            throw new QuotaExceededException("Monthly quota exceeded");
        }

        return (int) (monthlyQuota - after);
    }

    @Override
    public String ensureFreemiumTokenIfMissing(String username) {
        var userID = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"))
                .getUserID();

        return aiTokenRepository
                .findByUserId(userID)
                .map(AITokenEntity::getToken)
                .orElseGet(() -> this.generateToken(username, "freemium"));
    }
}
