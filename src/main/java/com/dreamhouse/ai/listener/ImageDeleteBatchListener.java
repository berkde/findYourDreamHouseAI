package com.dreamhouse.ai.listener;

import com.dreamhouse.ai.cloud.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ImageDeleteBatchListener {
    private static final Logger log = LoggerFactory.getLogger(ImageDeleteBatchListener.class);
    private final StorageService storageService;

    public ImageDeleteBatchListener(StorageService storageService) {
        this.storageService = storageService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(@NonNull ImageDeleteBatchEvent event) {
        event.storageKeys().forEach(storageService::deleteObject);
        log.info("S3 images deleted");
    }
}
