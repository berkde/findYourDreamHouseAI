package com.dreamhouse.ai.cloud.service;

import com.dreamhouse.ai.cloud.model.StoragePutResponse;

import java.time.Duration;
import java.util.Optional;

public interface StorageService {
    Optional<String> presignedGetUrl(String key, Duration duration);
    Optional<StoragePutResponse> putObject(String key, byte[] bytes, String contentType);
    void deleteObject(String key);
}
