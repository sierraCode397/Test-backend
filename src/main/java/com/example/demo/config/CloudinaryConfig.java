package com.example.demo.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for integrating with the Cloudinary service.
 * Loads required environment variables and exposes a Cloudinary bean.
 */
@Configuration
public class CloudinaryConfig {

  @Autowired
  private Dotenv dotenv;

  /**
   * Initializes system properties with Cloudinary credentials
   * loaded from the .env file.
   */
  @PostConstruct
  public void init() {
    System.setProperty("cloudinary.cloud-name", dotenv.get("CLOUD_NAME"));
    System.setProperty("cloudinary.api-key", dotenv.get("API_KEY"));
    System.setProperty("cloudinary.api-secret", dotenv.get("API_SECRET"));
  }

  /**
   * Creates and configures a Cloudinary instance using environment variables.
   *
   * @return a configured Cloudinary instance
   */
  @Bean
  public Cloudinary cloudinary() {
    String cloudName = dotenv.get("CLOUD_NAME");
    String apiKey = dotenv.get("API_KEY");
    String apiSecret = dotenv.get("API_SECRET");

    System.out.println("cloudName = " + cloudName);
    System.out.println("apiKey = " + apiKey);
    System.out.println("apiSecret = " + apiSecret);

    return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
    ));
  }
}
