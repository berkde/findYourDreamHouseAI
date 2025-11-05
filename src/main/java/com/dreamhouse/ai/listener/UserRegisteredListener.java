package com.dreamhouse.ai.listener;

import com.dreamhouse.ai.listener.event.UserRegisteredEvent;
import com.dreamhouse.ai.llm.service.AITokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisteredListener {
    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);
    private final AITokenService aiTokenService;

    public UserRegisteredListener(AITokenService aiTokenService) {
        this.aiTokenService = aiTokenService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserRegisteredEvent event) {
        aiTokenService.ensureFreemiumTokenIfMissing(event.username());
        log.info("Issued initial freemium AI token for {}", event.username());
    }
}
