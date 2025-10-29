package com.dreamhouse.ai;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FindYourDreamHouseAiApplication {
    public static void main(String[] args) {
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "10");
        SpringApplication.run(FindYourDreamHouseAiApplication.class, args);
    }
}
