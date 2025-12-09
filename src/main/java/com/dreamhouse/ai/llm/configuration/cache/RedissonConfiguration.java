package com.dreamhouse.ai.llm.configuration.cache;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {
    private static final int CONNECTION_MIN_IDLE_SIZE = 5;
    private static final int CONNECTION_POOL_SIZE = 16;
    private static final int TIMEOUT = 3000;
    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int RETRY_ATTEMPTS_MAX = 2;
    private static final int RETRY_INTERVAL = 1500;
    private static final int PING_INTERVAL = 15000;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        String address = System.getenv("REDIS_SERVER");
        Config config = new Config();

        config.setThreads(2);
        config.setNettyThreads(2);

        config.useSingleServer()
                .setAddress(address)
                .setConnectionMinimumIdleSize(CONNECTION_MIN_IDLE_SIZE)
                .setConnectionPoolSize(CONNECTION_POOL_SIZE)
                .setTimeout(TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setRetryAttempts(RETRY_ATTEMPTS_MAX)
                .setRetryInterval(RETRY_INTERVAL)
                .setPingConnectionInterval(PING_INTERVAL)
                .setTcpNoDelay(Boolean.TRUE)
                .setKeepAlive(Boolean.TRUE);

        return Redisson.create(config);
    }
}
