package com.dreamhouse.ai.house.listener;

import com.dreamhouse.ai.house.model.event.ImageDeleteEvent;
import com.dreamhouse.ai.house.service.StorageService;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ImageDeleteListener {
    private final StorageService storage;
    public ImageDeleteListener(StorageService storage) { this.storage = storage; }

    @Async
    @TransactionalEventListener
    public void on(@NonNull ImageDeleteEvent event) {
        storage.deleteObject(event.storageKey());
    }
}
