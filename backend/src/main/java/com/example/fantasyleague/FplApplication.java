package com.example.fantasyleague;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // Add this

@SpringBootApplication
@EnableScheduling // Add this
public class FplApplication {
    public static void main(String[] args) {
        SpringApplication.run(FplApplication.class, args);
    }
}