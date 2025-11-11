package com.dreamhouse.ai.listener;

import com.dreamhouse.ai.listener.event.AIPlanUpdateEvent;
import com.dreamhouse.ai.llm.service.impl.AITokenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Asynchronous listener that reacts to AI plan update events after the enclosing
 * transaction successfully commits. It updates the user's token plan and quota
 * using the AI token service implementation.
 * Annotations:
 *   Component - registers this listener as a Spring bean.
 *   Async - processes events on a task executor thread so callers are not blocked.
 *   TransactionalEventListener with phase AFTER_COMMIT - ensures the handler runs only when the publishing
 *   transaction commits.
 */
@Component
public class AIPlanUpdateListener {
    private static final Logger log = LoggerFactory.getLogger(AIPlanUpdateListener.class);
    private final AITokenServiceImpl aiTokenService;

    /**
     * Creates the listener with required dependencies.
     *
     * @param aiTokenService service used to update token plan and quotas
     */
    public AIPlanUpdateListener(AITokenServiceImpl aiTokenService) {
        this.aiTokenService = aiTokenService;
    }

    /**
     * Handles a plan update event by applying the new plan to the user's token and logging the change.
     *
     * @param event the plan update event; both {@code userId} and {@code planCode} are expected to be valid
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(@NonNull AIPlanUpdateEvent event) {
        var userId = event.userId();
        var planCode = event.planCode();
        aiTokenService.updateTokenPlanAndQuota(userId, planCode);
        log.info("User {}'s plan has been updated to {}", userId, planCode);
    }
}
