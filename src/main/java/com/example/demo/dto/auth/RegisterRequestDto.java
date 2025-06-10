package com.example.demo.dto.auth;

import com.example.demo.constant.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO that represents the data required for a user registration request.
 */
@Setter
@Getter
public class RegisterRequestDto {

  @NotBlank(message = "El nombre completo no puede estar vacío")
  @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
  @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$",
          message = "El nombre solo puede contener letras y espacios")
  private String fullname;

  @NotBlank(message = "El email no puede estar vacío")
  @Email(message = "El email debe ser válido")
  private String email;

  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$",
          message = "La contraseña debe contener mayúsculas, minúsculas, números y símbolos")
  @NotBlank(message = "La contraseña no puede estar vacía")
  @Size(min = 8, max = 30, message = "La contraseña debe tener entre 8 y 30 caracteres")
  private String password;

  @NotBlank(message = "La confirmación de contraseña no puede estar vacía")
  private String confirmPassword;

  private Role role;

}
