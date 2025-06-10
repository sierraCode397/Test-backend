package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a company profile.
 * Contains legal information, location, and associated user data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProfileRequestDto {

  @NotBlank(message = "El nombre comercial es obligatorio")
  @Size(max = 300, message = "El nombre comercial no puede superar los 300 caracteres")
  private String tradeName;

  @NotBlank(message = "La razon social es obligatorio")
  @Size(max = 300, message = "La razon social no puede superar los 300 caracteres")
  private String legalName;

  @NotBlank(message = "El CUIT es obligatorio")
  @Pattern(
          regexp = "\\d{11}",
          message = "El CUIT debe tener exactamente 11 números y solo debe contener dígitos")
  private String cuit;

  @NotBlank(message = "El país es obligatorio")
  private String country;

  @NotBlank(message = "La ubicación legal es obligatoria")
  @Size(max = 300, message = "La ubicación legal no puede superar los 300 caracteres")
  private String companyLocation;

  @NotBlank(message = "El representante legal es obligatorio")
  @Size(max = 300, message = "El representante legal no puede superar los 300 caracteres")
  private String legalRepresentative;

  @NotNull(message = "El userId es obligatorio")
  private UUID userId;
}
