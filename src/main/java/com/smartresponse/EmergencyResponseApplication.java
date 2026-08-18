package com.smartresponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication @EnableScheduling
public class EmergencyResponseApplication {
    public static void main(String[] args) { SpringApplication.run(EmergencyResponseApplication.class, args); }
}
