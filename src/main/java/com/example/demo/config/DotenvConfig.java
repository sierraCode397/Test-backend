package com.example.demo.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to provide access to environment variables
 * defined in a .env file using the Dotenv library.
 */
@Configuration
public class DotenvConfig {

  /**
   * Loads environment variables from the .env file.
   *
   * @return a Dotenv instance containing the loaded variables
   */
  @Bean
  public Dotenv dotenv() {
    return Dotenv.load();
  }
}