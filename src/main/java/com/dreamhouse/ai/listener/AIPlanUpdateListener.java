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

@Component
public class AIPlanUpdateListener {
    private static final Logger log = LoggerFactory.getLogger(AIPlanUpdateListener.class);
    private final AITokenServiceImpl aiTokenService;

    public AIPlanUpdateListener(AITokenServiceImpl aiTokenService) {
        this.aiTokenService = aiTokenService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(@NonNull AIPlanUpdateEvent event) {
        var userId = event.userId();
        var planCode = event.planCode();
        aiTokenService.updateTokenPlanAndQuota(userId, planCode);
        log.info("User {}'s plan has been updated to {}", userId, planCode);
    }
}
