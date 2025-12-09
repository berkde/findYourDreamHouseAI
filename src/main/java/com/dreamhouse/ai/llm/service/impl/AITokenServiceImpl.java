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

/**
 * Default implementation of {@link com.dreamhouse.ai.llm.service.AITokenService} that manages
 * AI access tokens, ownership validation, and monthly quota consumption.
 * 
 * Persistence is handled via {@code AITokenRepository}, user lookups via {@code UserRepository},
 * and usage counting via Redis (Redisson) using an {@code RAtomicLong} per token-month key.
 * This class does not mutate business logic beyond token/quota management and avoids
 * transactional boundaries itself; callers may wrap operations in transactions if needed.
 *
 * Concurrency and rate limits:
 * Quota consumption uses Redis atomic increments to provide thread-safe counting across nodes.
 * A per-month TTL is applied to usage counters so they naturally reset at the end of the month.
 * 
 *
 * Constraints:
 * Tokens have an upper monthly quota cap enforced by {@code MAX_TOKEN_LIMIT}.
 * Ownership is validated against the authenticated user's id.
 */
@Service
public class AITokenServiceImpl implements AITokenService {
    private static final Logger log = LoggerFactory.getLogger(AITokenServiceImpl.class);
    private static final int MAX_TOKEN_LIMIT = 12000;
    private final AITokenRepository aiTokenRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
    private final AIUtil aiUtil;

    /**
     * Constructs the service with required collaborators.
     *
     * @param aiTokenRepository repository used to persist and query token records
     * @param userRepository    repository used to resolve user identities and ownership
     * @param redissonClient    Redis client used for distributed, atomic quota counters
     * @param aiUtil            helper utilities (e.g., end-of-month TTL calculation)
     */
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

    /** {@inheritDoc}
     * Validation includes expiry check, max monthly quota sanity, and ownership match.
     */
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
        boolean isWithinMaxPlanLimit = stored_token.getMonthlyQuota() <= MAX_TOKEN_LIMIT;
        boolean isOwnerMatching = stored_token.getUserId().equals(stored_user.getUserID());

        System.out.println("isNotExpired: " + isNotExpired);
        System.out.println("isWithinMaxPlanLimit: " + isWithinMaxPlanLimit);
        System.out.println("isOwnerMatching: " + isOwnerMatching);

        return  isNotExpired && isWithinMaxPlanLimit && isOwnerMatching;
    }

    /** {@inheritDoc}
     * Generates a new opaque token, sets expiry and default monthly quota derived from plan code,
     * and binds it to the resolved user id for the provided username.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the user cannot be resolved
     */
    @Override
    public String generateToken(String username, String planCode) {
        var token = UUID.randomUUID().toString().replaceAll("-", "");
        var aiTokenEntity = new AITokenEntity();
        aiTokenEntity.setToken(token);
        aiTokenEntity.setExpiryDate();
        aiTokenEntity.setPlanCode(planCode);
        aiTokenEntity.setMonthlyQuota();
        aiTokenEntity.setActive(Boolean.TRUE);

        var stored_user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        aiTokenEntity.setUserId(stored_user.getUserID());

        var savedTokenEntity = aiTokenRepository.save(aiTokenEntity);
        stored_user.setAiAuthToken(savedTokenEntity.getToken());
        userRepository.save(stored_user);

        return savedTokenEntity.getToken();
    }

    /** {@inheritDoc}
     * Looks up the token by user id and fails if none is found.
     * @throws com.dreamhouse.ai.llm.exception.AITokenNotFoundException if no token exists for the given user id
     */
    @Override
    public AITokenEntity getToken(String userId) {
        return aiTokenRepository
                .findByUserId(userId)
                .orElseThrow(() -> new AITokenNotFoundException("Token not found"));
    }

    /** {@inheritDoc}
     * Updates the stored plan code and resets monthly quota according to plan defaults.
     * @throws com.dreamhouse.ai.llm.exception.AITokenNotFoundException if the token cannot be found
     */
    @Override
    public AITokenDTO updateTokenPlanAndQuota(String token, String planCode) {
        var aiTokenEntity = aiTokenRepository.findByToken(token).orElseThrow(() -> new AITokenNotFoundException("Token not found"));
        aiTokenEntity.setPlanCode(planCode);
        aiTokenEntity.setMonthlyQuota();
        var savedToken = aiTokenRepository.save(aiTokenEntity);
        return new AITokenDTO(savedToken.getToken());
    }

    /** {@inheritDoc}
     * Atomically increments this token's monthly usage counter in Redis and enforces the stored monthly quota.
     * Initializes a TTL on the usage counter so it expires at the end of the current month.
     * Throws when the increment would exceed the monthly quota.
     *
     * @throws com.dreamhouse.ai.llm.exception.QuotaExceededException if the token is invalid/expired or quota would be exceeded
     * @throws com.dreamhouse.ai.llm.exception.AITokenNotFoundException if the token cannot be found when resolving quota
     */
    @Override
    public int consumeQuota(String token, String username) {
        if (isTokenValid(token, username) == Boolean.FALSE) {
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

    /** {@inheritDoc}
     * Returns an existing token for the user if present; otherwise creates a new token bound to the
     * {@code freemium} plan and returns it.
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException if the username cannot be resolved
     */
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
