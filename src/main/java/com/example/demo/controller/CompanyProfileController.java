package com.example.demo.controller;

import com.example.demo.constant.ApiResult;
import com.example.demo.constant.CompanyProfileStatus;
import com.example.demo.dto.CompanyProfileRequestDto;
import com.example.demo.dto.CompanyProfileResponseDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.CompanyProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for managing company profiles.
 */
@RestController
@RequiredArgsConstructor
public class CompanyProfileController {

  public final CompanyProfileService companyProfileService;

  /**
   * Creates a new company profile.
   *
   * @param file file to upload
   * @param companyProfileRequestDtoJson JSON string with profile data
   * @return ApiResponse with success message
   * @throws JsonProcessingException if JSON is invalid
   */
  @PreAuthorize("hasRole('USER')")
  @PostMapping(value = "/create/company-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createCompanyProfile(
          @RequestParam("file") MultipartFile file,
          @RequestParam("data") String companyProfileRequestDtoJson)
          throws JsonProcessingException {

    validateFile(file);
    CompanyProfileRequestDto companyProfileRequestDto = valid(companyProfileRequestDtoJson);
    CompanyProfileResponseDto createdProfile = companyProfileService.create(
            file, companyProfileRequestDto);
    ApiResult<CompanyProfileResponseDto> response = new ApiResult<>(
            true, "Perfil de empresa creado correctamente", createdProfile);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Updates the status of a company profile.
   *
   * @param id UUID of the profile
   * @param status new status to set
   * @return ApiResponse with success message
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/update/status")
  public ResponseEntity<?> updateStatus(
          @RequestParam UUID id,
          @RequestParam CompanyProfileStatus status) {

    String updateStatus = companyProfileService.updateStatus(id, status);
    ApiResult<String> response = new ApiResult<>(
            true, "Cambio exitoso del estado del perfil de empresa.", updateStatus);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates a rejected company profile.
   *
   * @param file file to upload
   * @param companyProfileRequestDtoJson JSON string with profile data
   * @return ApiResponse with success message
   * @throws JsonProcessingException if JSON is invalid
   */
  @PreAuthorize("hasRole('USER')")
  @PutMapping(value = "/update/company-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> updateCompanyProfile(
          @RequestParam("file") MultipartFile file,
          @RequestParam("data") String companyProfileRequestDtoJson)
          throws JsonProcessingException {

    validateFile(file);
    CompanyProfileRequestDto companyProfileRequestDto = valid(companyProfileRequestDtoJson);
    CompanyProfileResponseDto updateProfile = companyProfileService.updateRejectedProfile(file, companyProfileRequestDto);
    ApiResult<CompanyProfileResponseDto> response = new ApiResult<>(
            true, "Se actualizo exitosamente el perfil de empresa.", updateProfile);
    return ResponseEntity.ok(response);
  }

  /**
   * Gets the company profile of the authenticated user.
   *
   * @param authentication Authentication object with user details
   * @return company profile DTO
   */
  @PreAuthorize("hasAnyRole('USER', 'COMPANY')")
  @GetMapping("/details/company-profile")
  public ResponseEntity<CompanyProfileResponseDto> getMyCompanyProfile(
          Authentication authentication) {
    String email = authentication.getName();

    CompanyProfileResponseDto profile = companyProfileService.getByUserEmail(email);

    return ResponseEntity.ok(profile);
  }

  private CompanyProfileRequestDto valid(
          String companyProfileRequestDtoJson) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    CompanyProfileRequestDto dto = objectMapper.readValue(
            companyProfileRequestDtoJson, CompanyProfileRequestDto.class);

    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      Validator validator = factory.getValidator();
      Set<ConstraintViolation<CompanyProfileRequestDto>> violations = validator.validate(dto);
      if (!violations.isEmpty()) {
        String message = violations.iterator().next().getMessage();
        throw new BadRequestException("Error de validación: " + message);
      }
    }
    return dto;
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("El archivo no puede estar vacío");
    }
    String contentType = Objects.requireNonNull(file.getContentType(), "Tipo MIME nulo");
    String fileName = Objects.requireNonNull(file.getOriginalFilename(), "Nombre de archivo nulo");
    boolean isValidMime = contentType.equals("application/pdf")
            || contentType.equals("application/msword")
            || contentType.equals(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    boolean hasValidExtension = fileName.toLowerCase().endsWith(".pdf")
            || fileName.toLowerCase().endsWith(".doc")
            || fileName.toLowerCase().endsWith(".docx");
    if (!isValidMime || !hasValidExtension) {
      throw new BadRequestException("Solo se permiten archivos PDF o Word (.doc, .docx)");
    }
  }
}
