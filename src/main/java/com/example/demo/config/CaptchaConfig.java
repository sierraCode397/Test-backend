package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration for Google reCAPTCHA keys.
 * This class holds the site key and secret key for reCAPTCHA.
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "google.recaptcha.key")
public class CaptchaConfig {
  private String site;
  private String secret;
}
