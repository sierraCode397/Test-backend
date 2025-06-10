package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO that represents the data required for a login request.
 */
@Data
public class LoginRequestDto {

  @NotBlank(message = "El email no puede estar vacío")
  @Email(message = "El email debe tener un formato válido")
  private String email;

  @NotBlank(message = "La contraseña no puede estar vacía")
  private String password;

  @NotBlank(message = "No puede estar sin el token del recaptcha")
  private String recaptchaToken;

}
