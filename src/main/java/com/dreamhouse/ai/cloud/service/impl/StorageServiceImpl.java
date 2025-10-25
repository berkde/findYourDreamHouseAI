package com.dreamhouse.ai.cloud.service.impl;

import com.dreamhouse.ai.cloud.exception.CloudException;
import com.dreamhouse.ai.cloud.model.StoragePutResponse;
import com.dreamhouse.ai.cloud.service.SecretsService;
import com.dreamhouse.ai.cloud.service.StorageService;
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
import software.amazon.awssdk.services.s3.model.*;
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
    private static final String AWS_BUCKET_JSON_FIELD = "bucket_name";
    private static final String BASE_PATH_JSON_FIELD = "basePath";
    private static final String SERVER_ENCRYPTION_TYPE = "AES256";
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

    @Transactional
    @Override
    public Optional<String> presignedGetUrl(String key, Duration expiry) {
        try {
            Objects.requireNonNull(key, "key must not be null");
            if (expiry == null || expiry.isNegative() || expiry.isZero()) {
                expiry = Duration.ofMinutes(15);
            }

            var bucket = secretsService.getSecret(secretId, AWS_BUCKET_JSON_FIELD).replace("\"", "");

            log.info("Bucket {}", bucket);

            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                     .responseContentType("image/jpeg")      // optional: force headers
                     .responseContentDisposition("inline")    // optional: ensure inline display
                    .build();

            log.info("presignedGetUrl getReq = {}", getReq);

            GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(getReq)
                    .build();

            log.info("presignedGetUrl presignReq = {}", presignReq.getObjectRequest());

            var url = s3Presigner.presignGetObject(presignReq).url().toString();
            log.info("presignedGetUrl url = {}", url);
            return Optional.of(url);
        } catch (Exception e) {
            log.error("presignedGetUrl - Error creating presigned get url for key: {}", key, e);
            throw new CloudException("Error creating presigned get url for key");
        }
    }

    @Transactional
    @Override
    public Optional<StoragePutResponse> putObject(String key, byte[] bytes, String contentType) {
        try {
            var basePath = secretsService.getSecret(secretId, BASE_PATH_JSON_FIELD).replace("\"", "");
            var bucket = secretsService.getSecret(secretId, AWS_BUCKET_JSON_FIELD).replace("\"", "");

            log.info("basePath: {}", basePath);
            log.info("bucket: {}", bucket);

            log.info("key: {}", key);


            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .serverSideEncryption(SERVER_ENCRYPTION_TYPE)
                    .build();

            var resp = s3Client.putObject(req, RequestBody.fromBytes(bytes));
            URL url = s3Utilities.getUrl(b -> b.bucket(bucket).key(key));

            log.info("url: {}", url);

            return Optional.of(new StoragePutResponse(key, resp.eTag(), url.toString()));
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
            var basePath = secretsService.getSecret(secretId, BASE_PATH_JSON_FIELD).replace("\"", "");
            var bucket = secretsService.getSecret(secretId, AWS_BUCKET_JSON_FIELD).replace("\"", "");
            String objectKey = (basePath.isEmpty() ? "" : basePath) + key;

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (NoSuchKeyException e) {
            log.warn("deleteObject - Object does not exist: {}", key, e);
            throw new CloudException("Object does not exist: " + key);
        } catch (AwsServiceException e) {
            log.error("deleteObject - Error deleting object: {}", key, e);
            throw new CloudException("Aws service exception: " + e.getMessage());
        } catch (SdkClientException e) {
            log.error("deleteObject - Error deleting object: {}", key, e);
            throw new CloudException("Sdk client exception :" + e.getMessage());
        }
    }
}
