package com.example.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the Spring Boot application.
 */
@SpringBootApplication
public class BackendApplication {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * Main entry point of the Spring Boot application.
     * @param args command line arguments (not used).
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Prints the loaded environment variables to the console, useful for verifying the configuration.
     */
    @PostConstruct
    public void logStartupInfo() {
        System.out.printf("Conectando a la DB en: %s%n", dbUrl);
        System.out.printf("Servidor iniciado en el puerto: %d%n", serverPort);
    }
}
