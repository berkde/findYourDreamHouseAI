package com.dreamhouse.ai;


import com.dreamhouse.ai.authentication.configuration.SecurityConfiguration;
import com.dreamhouse.ai.cache.configuration.CacheConfiguration;
import com.dreamhouse.ai.cloud.configuration.AwsConfiguration;
import com.dreamhouse.ai.house.configuration.HouseAdConfiguration;
import com.dreamhouse.ai.llm.configuration.AiConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan(basePackageClasses = {AiConfiguration.class, HouseAdConfiguration.class, AwsConfiguration.class, CacheConfiguration.class, SecurityConfiguration.class })
public class FindYourDreamHouseAiApplication {
    public static void main(String[] args) {
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "10");
        SpringApplication.run(FindYourDreamHouseAiApplication.class, args);
    }
}
