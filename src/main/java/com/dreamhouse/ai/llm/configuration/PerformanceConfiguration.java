package com.dreamhouse.ai.llm.configuration;

import com.dreamhouse.ai.llm.dto.HouseSearchDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
public class PerformanceConfiguration {
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
        ex.setCorePoolSize(8);      // tune: = 2–4× cores for I/O heavy
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("house-search-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }


    @Bean("storageExecutor")
    public Executor storageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("storage-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
