package com.dreamhouse.ai.listener;

import com.dreamhouse.ai.listener.event.ImageDeleteEvent;
import com.dreamhouse.ai.cloud.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ImageDeleteListener {
    private final StorageService storage;
    private static final Logger log = LoggerFactory.getLogger(ImageDeleteListener.class);
    public ImageDeleteListener(StorageService storage) { this.storage = storage; }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(@NonNull ImageDeleteEvent event) {
        storage.deleteObject(event.storageKey());
        log.info("S3 Image deleted");
    }
}
