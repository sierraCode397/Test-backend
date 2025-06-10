package com.example.demo.service;

import com.example.demo.config.CaptchaConfig;
import com.example.demo.exception.RecaptchaValidationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Service responsible for validating the client's response against reCAPTCHA.
 * Uses the secret key provided in application configuration
 * to send a request to Google's verification API and validate
 * whether the user's response is valid.
 */
@Service
public class CaptchaService {

  private final CaptchaConfig captchaConfig;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Constructor for CaptchaService.
   *
   * @param captchaConfig Configuration properties for reCAPTCHA.
   */
  @Autowired
  public CaptchaService(CaptchaConfig captchaConfig) {
    this.captchaConfig = captchaConfig;
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Validates the reCAPTCHA response from the client.
   *
   * @param recaptchaResponse The reCAPTCHA response sent from the frontend.
   * @return {@code true} if verification is successful, {@code false} otherwise.
   * @throws RecaptchaValidationException if the response from reCAPTCHA
   *                                      is empty or if there is an error parsing the response.
   */
  public boolean verify(String recaptchaResponse) {
    HttpHeaders headers = new HttpHeaders();
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("response", recaptchaResponse);
    params.add("secret", captchaConfig.getSecret());

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    String verifyUrl = "https://www.google.com/recaptcha/api/siteverify";
    ResponseEntity<String> response = restTemplate.postForEntity(verifyUrl, request, String.class);
    String body = response.getBody();

    if (body.isEmpty()) {
      throw new RecaptchaValidationException("La respuesta de reCAPTCHA está vacía.");
    }

    try {
      Map<String, Object> json = objectMapper.readValue(
              body, new TypeReference<Map<String, Object>>() {}
      );
      return Boolean.TRUE.equals(json.get("success"));
    } catch (IOException e) {
      throw new RecaptchaValidationException("Error parseando la respuesta de reCAPTCHA", e);
    }
  }
}
