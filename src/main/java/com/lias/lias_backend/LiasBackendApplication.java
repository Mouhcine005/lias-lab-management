package com.lias.lias_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LiasBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiasBackendApplication.class, args);
    }
}