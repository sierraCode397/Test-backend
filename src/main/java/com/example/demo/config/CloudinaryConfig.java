package com.example.demo.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for integrating with the Cloudinary service.
 * Reads credentials from environment variables via Spring @Value.
 */
@Configuration
public class CloudinaryConfig {

  @Value("${CLOUD_NAME}")
  private String cloudName;

  @Value("${API_KEY}")
  private String apiKey;

  @Value("${API_SECRET}")
  private String apiSecret;

  /**
   * Creates and configures a Cloudinary instance using environment variables.
   *
   * @return a configured Cloudinary instance
   */
  @Bean
  public Cloudinary cloudinary() {
    // Optional: log to verify
    System.out.printf("cloudName=%s, apiKey=%s, apiSecret=%s%n",
                      cloudName, apiKey, apiSecret);

    return new Cloudinary(ObjectUtils.asMap(
      "cloud_name", cloudName,
      "api_key",    apiKey,
      "api_secret", apiSecret
    ));
  }
}
