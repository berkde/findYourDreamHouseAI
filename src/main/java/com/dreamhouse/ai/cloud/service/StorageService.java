package com.dreamhouse.ai.house.service;

import com.dreamhouse.ai.house.model.response.StoragePutResponse;

import java.time.Duration;
import java.util.Optional;

public interface StorageService {
    Optional<String> presignedGetUrl(String key, Duration duration);
    Optional<StoragePutResponse> putObject(String key, byte[] bytes, String contentType);
    void deleteObject(String key);
}
