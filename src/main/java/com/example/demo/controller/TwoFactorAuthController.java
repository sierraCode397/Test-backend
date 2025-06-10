package com.example.demo.controller;

import com.example.demo.constant.ApiResult;
import com.example.demo.dto.TwoFactorRequest;
import com.example.demo.service.AuthService;
import com.example.demo.service.TwoFactorAuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling Two-Factor Authentication (2FA) operations.
 * Provides endpoints to toggle 2FA settings and to validate 2FA codes during login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TwoFactorAuthController {
  private final TwoFactorAuthService twoFactorService;
  private final AuthService authService;

  /**
   * Enables or disables Two-Factor Authentication for the authenticated user.
   *
   * @param enabled boolean flag to enable (true) or disable (false) 2FA
   * @param authentication current user's authentication object
   * @return ApiResult indicating success of the update operation
   */
  @PatchMapping("/2fa/toggle")
  public ResponseEntity<?> toggle2Fa(
          @RequestParam boolean enabled, Authentication authentication) {
    String email = authentication.getName();
    twoFactorService.updateTwoFactorSetting(email, enabled);
    return ResponseEntity.ok(new ApiResult<>(
            true, "2FA actualizado", null));
  }

  /**
   * Validates the provided 2FA code for the given user email.
   * If the code is valid, generates a JWT token and returns it in the response header and body.
   *
   * @param request DTO containing the user's email and 2FA code
   * @return ApiResult with JWT token on successful validation
   */
  @PostMapping("/2fa/validate")
  public ResponseEntity<?> validateTwoFactorCode(@RequestBody TwoFactorRequest request) {
    twoFactorService.validateCode(request.getEmail(), request.getCode());
    String token = authService.generateJwtToken(request.getEmail());
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    Map<String, String> data = Map.of("token", token);
    ApiResult<Map<String, String>> response = new ApiResult<>(
            true, "Código 2FA válido, acceso permitido.", data);
    return ResponseEntity.ok().headers(headers).body(response);
  }
}
