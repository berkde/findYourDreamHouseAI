package com.dreamhouse.ai;


import com.dreamhouse.ai.authentication.configuration.SecurityConfiguration;
import com.dreamhouse.ai.cache.configuration.CacheConfiguration;
import com.dreamhouse.ai.cloud.configuration.AwsConfiguration;
import com.dreamhouse.ai.house.configuration.HouseAdConfiguration;
import com.dreamhouse.ai.llm.configuration.LLMConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan(basePackageClasses = {LLMConfiguration.class, HouseAdConfiguration.class, AwsConfiguration.class, CacheConfiguration.class, SecurityConfiguration.class })
public class FindYourDreamHouseAiApplication {
    private static final Logger log = LoggerFactory.getLogger(FindYourDreamHouseAiApplication.class);
    public static void main(String[] args) {
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "10");
        SpringApplication.run(FindYourDreamHouseAiApplication.class, args);
    }
}
