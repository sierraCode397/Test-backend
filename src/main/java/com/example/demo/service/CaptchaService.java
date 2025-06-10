package com.example.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.example.demo.config.CaptchaConfig;
import com.example.demo.exception.RecaptchaValidationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CaptchaService {


    private final CaptchaConfig captchaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaptchaService(CaptchaConfig captchaConfig) {
        this.captchaConfig = captchaConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public boolean verify(String recaptchaResponse) {

        HttpHeaders headers = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("response", recaptchaResponse);
        params.add("secret", captchaConfig.getSecret());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
        ResponseEntity<String> response = restTemplate.postForEntity(VERIFY_URL, request, String.class);

        String body = response.getBody();

        if (body.isEmpty()) {
            throw new RecaptchaValidationException("La respuesta de reCAPTCHA está vacía.");
        }

        try {
            Map<String, Object> json = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            return Boolean.TRUE.equals(json.get("success"));
        } catch (IOException e) {
            throw new RecaptchaValidationException("Error parseando la respuesta de reCAPTCHA", e);
        }



    }
}
