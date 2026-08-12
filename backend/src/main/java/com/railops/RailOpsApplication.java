package com.railops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RailOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RailOpsApplication.class, args);
    }
}