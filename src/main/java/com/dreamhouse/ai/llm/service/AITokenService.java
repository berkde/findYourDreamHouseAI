package com.dreamhouse.ai.llm.service;


import com.dreamhouse.ai.llm.entity.AITokenEntity;
import com.dreamhouse.ai.llm.model.dto.AITokenDTO;

/**
 * Service contract for managing AI access tokens and usage quotas.
 *
 * Implementations are responsible for generating tokens, validating them,
 * tracking consumption against plan quotas, and synchronizing plan changes
 * (e.g., when a user's subscription is updated).
 */
public interface AITokenService  {

    /**
     * Validates that the provided raw token is active and associated with the given user.
     *
     * @param rawToken the raw token string as provided by the client (e.g., from an Authorization header)
     * @param username the username or user identifier that should own the token
     * @return {@code true} if the token exists, has not expired or been revoked, and belongs to {@code username};
     *         {@code false} otherwise
     */
    Boolean isTokenValid(String rawToken, String username);

    /**
     * Generates a new token for the specified user and plan.
     *
     * @param userId   the unique identifier of the user
     * @param planCode the plan code to bind to this token (e.g., freemium, pro)
     * @return a newly generated, opaque token string that can be used to authenticate AI requests
     */
    String generateToken(String userId, String planCode);

    /**
     * Retrieves the persisted token entity for the given token string.
     *
     * @param token the opaque token string
     * @return the corresponding {@link AITokenEntity} if found; may be {@code null} or throw depending on implementation
     */
    AITokenEntity getToken(String token);

    /**
     * Updates the plan and resets/adjusts quota for the token owner.
     * Implementations may interpret the {@code token} parameter as either a token value or a user identifier,
     * depending on internal conventions. The return value provides a DTO view after the update.
     *
     *
     * @param token    token string (or user identifier, as per implementation) used to locate the token record
     * @param planCode the new plan code to apply
     * @return an {@link AITokenDTO} reflecting the updated plan and quota
     */
    AITokenDTO updateTokenPlanAndQuota(String token, String planCode);

    /**
     * Consumes one unit (or a plan-defined amount) from the user's quota for the given token.
     *
     * @param token  the token used to authenticate the request
     * @param userId the user identifier to validate ownership and apply consumption against
     * @return the remaining quota after consumption
     */
    int consumeQuota(String token, String userId);

    /**
     * Ensures the user has a valid freemium token; creates one if missing.
     *
     * @param username the user's username/identifier
     * @return the token string the user should use going forward
     */
    String ensureFreemiumTokenIfMissing(String username);
}
