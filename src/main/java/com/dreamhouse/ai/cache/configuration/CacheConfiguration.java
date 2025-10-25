package com.dreamhouse.ai.cache.configuration;

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

        cacheManager.registerCustomCache("users", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(15))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("userProfiles", Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(60))
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("houseAds", Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofMinutes(20))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("houseAdsList", Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(3))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("houseAdsSearch", Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(Duration.ofMinutes(10))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("houseAdDetails", Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .expireAfterAccess(Duration.ofMinutes(8))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("secrets", Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("roles", Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(Duration.ofHours(2))
                .expireAfterAccess(Duration.ofMinutes(60))
                .recordStats()
                .build());

        cacheManager.registerCustomCache("authorities", Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofHours(2))
                .expireAfterAccess(Duration.ofMinutes(60))
                .recordStats()
                .build());

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());

        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .recordStats()
                .maximumSize(1000);
    }

}

