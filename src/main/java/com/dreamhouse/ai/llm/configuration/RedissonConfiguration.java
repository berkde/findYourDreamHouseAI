package com.dreamhouse.ai.llm.configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        String address = System.getenv("REDIS_SERVER");
        Config config = new Config();

        config.setThreads(2);
        config.setNettyThreads(2);

        config.useSingleServer()
                .setAddress(address)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(16)
                .setTimeout(3000)
                .setConnectTimeout(3000)
                .setRetryAttempts(2)
                .setRetryInterval(1500)
                .setPingConnectionInterval(15000)
                .setTcpNoDelay(true)
                .setKeepAlive(true);

        return Redisson.create(config);
    }
}
