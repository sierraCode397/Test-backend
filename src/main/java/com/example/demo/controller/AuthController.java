package com.example.demo.controller;

import com.example.demo.constant.ApiResult;
import com.example.demo.dto.auth.JwtDataDto;
import com.example.demo.dto.auth.LoginRequestDto;
import com.example.demo.dto.auth.RegisterRequestDto;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.service.AuthService;
import com.example.demo.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  private final CaptchaService captchaService;

  /**
   * Endpoint for user login.
   * This method validates the received reCAPTCHA token and user credentials.
   * If the reCAPTCHA token is invalid, it throws a `ForbiddenException`.
   * If the user has two-factor authentication (2FA) enabled,
   * a verification code is sent to their email, and the response returns
   * HTTP status 202 (Accepted) indicating that the code was sent.
   * If 2FA is not enabled, a JWT token is generated and returned
   * both in the "Authorization" header and in the response body.
   *
   * @param request object containing the login request
   *                data, including email, password, and reCAPTCHA token
   * @return ResponseEntity with:
   *         - HTTP 200 and the JWT token in the header and body if login
   *         is successful and 2FA is not enabled
   *         - HTTP 202 with a message indicating that the 2FA code has been sent
   *         if two-factor authentication is enabled
   * @throws ForbiddenException if the reCAPTCHA token is invalid
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
    if (!captchaService.verify(request.getRecaptchaToken())) {
      System.out.println("Token recibido: " + request.getRecaptchaToken());
      throw new ForbiddenException("Captcha inválido");
    }
    User user = authService.validateUserCredentials(request);
    if (user.isTwoFactorEnabled()) {
      authService.sendTwoFactorCodeToEmail(user);
      return ResponseEntity.status(HttpStatus.ACCEPTED)
              .body(new ApiResult<>(true, "Código de verificación enviado al email", null));
    }
    String token = authService.generateJwtToken(request.getEmail());
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + token);
    Map<String, String> data = Map.of("token", token);
    ApiResult<Map<String, String>> response = new ApiResult<>(true, "Login exitoso", data);
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
    ApiResult<Map<String, String>> response = new ApiResult<>(true, "Registro exitoso", data);
    return ResponseEntity.ok().headers(headers).body(response);
  }

  /**
   * Endpoint to retrieve authenticated user info from JWT.
   *
   * @param request the HTTP request containing the Authorization header
   * @return user information extracted from the token
   */
  @GetMapping("/details")
  public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
    String token = request.getHeader("Authorization").substring(7);
    JwtDataDto jwtDataDto = authService.infoUser(token);
    Map<String, JwtDataDto> data = new HashMap<>();
    data.put("user", jwtDataDto);
    ApiResult<Map<String, JwtDataDto>> response = new ApiResult<>(
            true, "Autenticacion exitosa", data);
    return ResponseEntity.ok(response);
  }

}

