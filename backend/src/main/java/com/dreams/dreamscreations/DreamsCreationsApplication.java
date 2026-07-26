package com.dreams.dreamscreations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableScheduling activates Spring's task scheduling.
 * Without this, the @Scheduled annotation in AlertServiceImpl does nothing.
 * With this, Spring Boot automatically calls checkAndCreateOverdueAlerts()
 * every day at 8 AM as configured in the cron expression.
 */
@SpringBootApplication
@EnableScheduling
public class DreamsCreationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreamsCreationsApplication.class, args);
    }
}
