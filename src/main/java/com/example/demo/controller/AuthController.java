package com.example.demo.controller;


import com.example.demo.constant.ApiResult;
import com.example.demo.dto.auth.JwtDataDto;
import com.example.demo.dto.auth.LoginRequestDto;
import com.example.demo.dto.auth.RegisterRequestDto;
import com.example.demo.service.AuthService;
import com.example.demo.service.CaptchaService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
   *
   * @param request login request data
   * @return JWT token in header and body
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) throws JsonProcessingException {

    //Verify CAPTCHA
    if(!captchaService.verify(request.getRecaptchaToken())){
      System.out.println("Token recibido: " + request.getRecaptchaToken());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResult<>(false, "Captcha inválido" ));
    }

    //Authenticated user
    String token = authService.login(request);
    System.out.println(token);
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
  public ResponseEntity<?> getUserInfo(
          HttpServletRequest request) {
    String token = request.getHeader("Authorization").substring(7);
    JwtDataDto jwtDataDto = authService.infoUser(token);
    Map<String, JwtDataDto> data = new HashMap<>();
    data.put("user", jwtDataDto);
    ApiResult<Map<String, JwtDataDto>> response = new ApiResult<>(
            true, "Autenticacion exitosa", data);
    return ResponseEntity.ok(response);
  }

}

