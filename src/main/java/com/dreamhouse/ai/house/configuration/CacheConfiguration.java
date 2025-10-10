package com.dreamhouse.ai.house.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Default fallback cache (optional)
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10)));

        // Register per-cache configurations
        cacheManager.registerCustomCache("users", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .build());

        cacheManager.registerCustomCache("houseAds", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .expireAfterAccess(Duration.ofMinutes(10))
                .build());

        cacheManager.registerCustomCache("houseAdsList", Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(3))
                .build());

        cacheManager.registerCustomCache("houseAdsSearch", Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .build());

        return cacheManager;
    }
}

