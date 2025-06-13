package com.example.demo.controller;

import com.example.demo.dto.EmailRequestForgotPass;
import com.example.demo.dto.KeysDTO;
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
import org.springframework.web.server.ResponseStatusException;


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
              .body(new ApiResult<>(true, "Código de verificación enviado al email", "2FA_CODE_SENT"));
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
   * Endpoint para iniciar el proceso de recuperación de contraseña mediante correo electrónico.
   * <p>
   * Este metodo recibe un objeto {@link EmailRequestForgotPass} que contiene el correo electrónico del usuario.
   * Verifica que el correo no esté vacío y luego delega al servicio de autenticación {@code authService}
   * la generación de un código de recuperación, su almacenamiento, y el envío al correo proporcionado.
   * </p>
   *
   * @param request Objeto que contiene el correo electrónico del usuario que solicita recuperar su contraseña.
   * @return Una respuesta HTTP con mensaje de confirmación si el proceso se inicia correctamente.
   * @throws IllegalArgumentException si el correo electrónico está vacío o es nulo.
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(@RequestBody EmailRequestForgotPass request) {
    if(request.getEmail() == null || request.getEmail().isEmpty()) {
      throw new IllegalArgumentException("El email no puede estar vacío");
    }
    return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
  }

  /**   * Endpoint to reset the user's password using a verification code.
   * * This method checks if the new password and repeated password match.
   * * If they do not match, it throws a `ResponseStatusException` with a BAD_REQUEST status.
   * * If they match, it calls the `resetPassword` method of the `authService`
   * * to update the user's password using the provided code and new password.
   * * @param contraDTO the DTO containing the new password, repeated password, and verification code
   * @param contraDTO
   * @return ResponseEntity with a success message if the password is updated successfully
   * @throws ResponseStatusException if the new password and repeated password do not match
   *
   */

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody KeysDTO contraDTO) {
      if (!contraDTO.getNewPassword().equals(contraDTO.getRepeatPassword())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
      }

      authService.resetPassword(contraDTO.getCode(), contraDTO.getNewPassword());
      return ResponseEntity.ok("Contraseña actualizada correctamente");
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

