package com.tollgate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TollgateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TollgateApplication.class, args);
    }
}
