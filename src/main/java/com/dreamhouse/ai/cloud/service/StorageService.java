package com.dreamhouse.ai.cloud.service;

import com.dreamhouse.ai.cloud.model.StoragePutResponse;

import java.time.Duration;
import java.util.Optional;

public interface StorageService {
    /**
     * Generates a presigned URL for getting an object from storage.
     * @param key the storage key/path of the object
     * @param duration the duration for which the URL should be valid
     * @return Optional containing the presigned URL if successful
     */
    Optional<String> presignedGetUrl(String key, Duration duration);
    
    /**
     * Uploads an object to storage.
     * @param key the storage key/path where the object should be stored
     * @param bytes the byte array containing the object data
     * @param contentType the MIME type of the object
     * @return Optional containing the storage response if successful
     */
    Optional<StoragePutResponse> putObject(String key, byte[] bytes, String contentType);
    
    /**
     * Deletes an object from storage.
     * @param key the storage key/path of the object to delete
     */
    void deleteObject(String key);
}
