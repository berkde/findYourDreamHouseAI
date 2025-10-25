package com.dreamhouse.ai.cloud.configuration;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration;
import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfiguration {
    private static final String CLOUD_WATCH_KEY_PREFIX = "cloudwatch.";
    private static final String CLOUD_WATCH_ENV_PROPERTY = "management.metrics.export.cloudwatch.";

    @Bean
    public S3Client s3Client(AwsProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsProperties properties) {
        return S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SecretsManagerClient secretManagerClient(AwsProperties properties) {
        return SecretsManagerClient
                .builder()
                .region(Region.of(properties.region()))
                .build();
    }

    @Bean
    public SecretCache cache(SecretsManagerClient secretsManagerClient) {
        SecretCacheConfiguration configuration = new SecretCacheConfiguration();
        configuration.setClient(secretsManagerClient);
        configuration.setCacheItemTTL(Duration.ofDays(1).getSeconds());
        configuration.setMaxCacheSize(1024 * 1024);
        return new SecretCache(configuration);
    }

//    @Bean
//    public CloudWatchConfig cloudWatchConfig(Environment env) {
//        return key -> {
//            String k = key.startsWith(CLOUD_WATCH_KEY_PREFIX) ? key.substring(CLOUD_WATCH_KEY_PREFIX.length()) : key;
//            return env.getProperty(CLOUD_WATCH_ENV_PROPERTY + k);
//        };
//    }
//
//    @Bean
//    public CloudWatchAsyncClient cloudWatchAsyncClient(AwsProperties properties) {
//        return CloudWatchAsyncClient.builder()
//                .region(Region.of(properties.region()))
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
//    }

//    @Bean
//    public Clock micrometerClock() { return Clock.SYSTEM; }

//    @Bean
//    public CloudWatchMeterRegistry cloudWatchMeterRegistry(
//            CloudWatchConfig config, Clock clock, CloudWatchAsyncClient client) {
//        return new CloudWatchMeterRegistry(config, clock, client);
//    }

}
