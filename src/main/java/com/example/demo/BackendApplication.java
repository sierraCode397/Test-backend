package com.example.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application configuration class that defines the necessary beans.
 */
@SpringBootApplication

public class BackendApplication {

  /**
   * Main entry point of the Spring Boot application.*
   * Loads environment variables from a .env file using the Dotenv library,
   * and sets the necessary system properties for database connection,
   * JWT configuration, and server port.
   * Configured properties are:
   * - spring.datasource.url: Database connection URL.
   * - spring.datasource.username: Database username.
   * - spring.datasource.password: Database password.
   * - jwt.secret: Secret key for generating JWT tokens.
   * - jwt.expiration: Expiration time for JWT tokens.
   * - server.port: Port on which the server will run (default is 8080 if not defined).
   * Finally, it runs the Spring Boot application.
   *
   * @param args command line arguments (not used).
   */
  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

  /**
   * Prints the loaded environment variables to the console, useful for verifying the configuration.
   */
  @PostConstruct
  public void logDatabaseUrl() {
    System.out.println("Conectando a la DB en: " + System.getProperty("spring.datasource.url"));
  }

}

