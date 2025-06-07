package com.example.demo.controller;

import com.example.demo.constant.ApiResponse;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication handling:
 * user registration and login.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * Endpoint for user login.
   *
   * @param request login request data
   * @return JWT token in header and body
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
    String token = authService.login(request);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    Map<String, String> data = Map.of("token", token);
    ApiResponse<Map<String, String>> response = new ApiResponse<>(true, "Login exitoso", data);
    return ResponseEntity.ok().headers(headers).body(response);

  }

  /**
   * Endpoint for user registration.
   *
   * @param request registration request data
   * @return JWT token in header and body
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
    String token = authService.register(request);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    Map<String, String> data = Map.of("token", token);
    ApiResponse<Map<String, String>> response = new ApiResponse<>(true, "Registro exitoso", data);
    return ResponseEntity.ok().headers(headers).body(response);
  }
}

