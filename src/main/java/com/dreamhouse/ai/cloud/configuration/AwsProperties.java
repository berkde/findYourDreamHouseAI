package com.dreamhouse.ai.cloud.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(String region) {
    public AwsProperties {
        Objects.requireNonNull(region, "AWS region cannot be null");

        if(!region.startsWith("us-")){
            throw new IllegalArgumentException("Malformed S3 region");
        }
    }
}
