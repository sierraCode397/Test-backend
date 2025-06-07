package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO that represents the data required for a login request.
 */
@Getter
@Setter
@Data
public class LoginRequestDto {

  @NotBlank(message = "El email no puede estar vacío")
  @Email(message = "El email debe tener un formato válido")
  private String email;

  @NotBlank(message = "La contraseña no puede estar vacía")
  private String password;

}
