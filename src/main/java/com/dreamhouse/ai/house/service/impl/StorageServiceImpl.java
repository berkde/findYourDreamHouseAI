package com.dreamhouse.ai.house.service.impl;

import com.dreamhouse.ai.house.exception.CloudException;
import com.dreamhouse.ai.house.model.response.StoragePutResult;
import com.dreamhouse.ai.house.service.SecretsService;
import com.dreamhouse.ai.house.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Service
public class StorageServiceImpl implements StorageService {
    private final S3Client s3Client;
    private final S3Utilities s3Utilities;
    private final S3Presigner s3Presigner;
    private final SecretsService secretsService;
    private final String secretId;
    private final static Logger log = LoggerFactory.getLogger(StorageServiceImpl.class);

    public StorageServiceImpl(S3Client s3Client,
                              S3Presigner s3Presigner,
                              SecretsService secretsService,
                              @Value("${security.jwt.secret-id}") String secretId
                              ) {
        this.s3Client = s3Client;
        this.s3Utilities = s3Client.utilities();
        this.s3Presigner = s3Presigner;
        this.secretsService = secretsService;
        this.secretId = secretId;
    }

    @Override
    public Optional<String> presignedGetUrl(String key, Duration expiry) {
        try {
            Objects.requireNonNull(key, "key must not be null");
            if (expiry == null || expiry.isNegative() || expiry.isZero()) {
                expiry = Duration.ofMinutes(15);
            }

            var bucket = secretsService.getSecret(secretId, "bucket_name").replace("\"", "");

            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    // .responseContentType("image/jpeg")      // optional: force headers
                    // .responseContentDisposition("inline")    // optional: ensure inline display
                    .build();

            GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(getReq)
                    .build();

            return Optional.of(s3Presigner.presignGetObject(presignReq).url().toString());
        } catch (Exception e) {
            log.error("presignedGetUrl - Error creating presigned get url for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public Optional<StoragePutResult> putObject(String key, byte[] bytes, String contentType) {
        try {
            var basePath = secretsService.getSecret(secretId, "basePath").replace("\"", "");
            var bucket = secretsService.getSecret(secretId, "bucket_name").replace("\"", "");
            String objectKey = (basePath.isEmpty() ? "" : basePath) + key;

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .serverSideEncryption("AES256")
                    .build();

            var resp = s3Client.putObject(req, RequestBody.fromBytes(bytes));
            URL url = s3Utilities.getUrl(b -> b.bucket(bucket).key(objectKey));

            return Optional.of(new StoragePutResult(objectKey, resp.eTag(), url.toString()));
        } catch (AwsServiceException e) {
            throw new CloudException(e.getMessage());
        } catch (SdkClientException e) {
            log.error("putObject - Error storing object: {}", key, e);
            return Optional.empty();
        }
    }

    @Transactional
    @Override
    public void deleteObject(String key) {
        try {
            var basePath = secretsService.getSecret(secretId, "basePath").replace("\"", "");
            var bucket = secretsService.getSecret(secretId, "bucket_name").replace("\"", "");
            String objectKey = (basePath.isEmpty() ? "" : basePath) + key;

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (AwsServiceException e) {
            throw new CloudException(e.getMessage());
        } catch (SdkClientException e) {
            log.error("deleteObject - Error deleting object: {}", key, e);
        }
    }
}
