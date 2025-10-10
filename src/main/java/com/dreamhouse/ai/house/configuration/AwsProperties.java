package com.dreamhouse.ai.house.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public record AwsProperties(String region, String secretId) {
}
