package com.hackaton.aicreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AicreatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AicreatorApplication.class, args);
    }
}
