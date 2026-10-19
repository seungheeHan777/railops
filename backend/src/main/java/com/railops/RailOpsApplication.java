package com.railops;

import com.railops.config.FareProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties(FareProperties.class)
@SpringBootApplication
public class RailOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RailOpsApplication.class, args);
    }
}
