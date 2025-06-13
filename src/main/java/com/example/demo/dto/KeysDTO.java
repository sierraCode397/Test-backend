package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for handling password reset keys.
 * Contains fields for new password, repeated password, and verification code.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeysDTO {
    @NotBlank(message = "La nueva contraseña no puede estar vacía")
    private String newPassword;

    @NotBlank(message = "La contraseña repetida no puede estar vacía")
    private String repeatPassword;

    @NotBlank(message = "El código es requerido")
    private String code;
}
