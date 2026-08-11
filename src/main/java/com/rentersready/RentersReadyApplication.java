package com.rentersready;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentersReadyApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentersReadyApplication.class, args);
    }
}
