package com.dreamhouse.ai.llm.configuration;

import com.dreamhouse.ai.llm.model.dto.HouseSearchDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
public class PerformanceConfiguration {
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;
    private static final int QUEUE_CAPACITY = 1000;

    @Bean
    public ConcurrentHashMap<String, CompletableFuture<HouseSearchDTO>> houseSearchInflight() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentHashMap<String, CompletableFuture<String>> storageInflight() {
        return new ConcurrentHashMap<>();
    }

    @Bean("houseSearchExecutor")
    public Executor executor() {
        var ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(CORE_POOL_SIZE);
        ex.setMaxPoolSize(MAXIMUM_POOL_SIZE);
        ex.setQueueCapacity(QUEUE_CAPACITY);
        ex.setThreadNamePrefix("house-search-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }


    @Bean("storageExecutor")
    public Executor storageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAXIMUM_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("storage-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
