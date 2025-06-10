package com.example.demo.service;

import com.example.demo.constant.CompanyProfileStatus;
import com.example.demo.constant.Role;
import com.example.demo.dto.CompanyProfileRequestDto;
import com.example.demo.dto.CompanyProfileResponseDto;
import com.example.demo.entity.CompanyProfile;
import com.example.demo.entity.User;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.FileUploadException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CompanyProfileRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.ConversionUtil;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class for managing company profiles.
 * Provides methods to create, update, and retrieve company profiles,
 * including file upload handling via Cloudinary.
 */
@Service
@RequiredArgsConstructor
public class CompanyProfileService {

  private final CloudinaryService cloudinaryService;
  private final CompanyProfileRepository companyProfileRepository;
  private final UserRepository userRepository;
  private final ConversionUtil conversionUtil;

  /**
   * Creates a new company profile for a user.
   *
   * @param file                     file to upload as part of the profile
   * @param companyProfileRequestDto data for the company profile
   * @throws ResourceNotFoundException if user is not found
   * @throws ConflictException         if profile already exists for the user
   * @throws FileUploadException       if file upload fails
   */
  public CompanyProfileResponseDto create(
          MultipartFile file,
          CompanyProfileRequestDto companyProfileRequestDto) {
    User user = userRepository.findById(companyProfileRequestDto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    if (companyProfileRepository.existsByUser(user)) {
      throw new ConflictException("Ya existe un perfil para este usuario");
    }
    CompanyProfile profile = new CompanyProfile();
    saveProfile(profile, user, companyProfileRequestDto, file);
    return new CompanyProfileResponseDto(
            profile.getId(),
            profile.getTradeName(),
            profile.getLegalName(),
            profile.getCuit(),
            profile.getCountry(),
            profile.getCompanyLocation(),
            profile.getLegalRepresentative(),
            profile.getFileUrl(),
            profile.getStatus().name()
    );
  }

  /**
   * Updates the status of a company profile by user ID.
   *
   * @param id     user ID
   * @param status new status to set
   * @throws ResourceNotFoundException if user or profile not found
   */
  public String updateStatus(UUID id, CompanyProfileStatus status) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));



    CompanyProfile companyProfile = companyProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil de empresa no encontrado"));

    companyProfile.setStatus(status);

    if (status == CompanyProfileStatus.APPROVED) {
      user.setRole(Role.COMPANY);
      userRepository.save(user);

    }
    companyProfileRepository.save(companyProfile);

    return companyProfile.getStatus().name();
  }

  /**
   * Updates a rejected company profile with new file and data.
   *
   * @param file file to upload
   * @param companyProfileRequestDto updated profile data
   * @throws ResourceNotFoundException if user or profile not found
   * @throws FileUploadException if file upload fails
   */
  public void updateRejectedProfile(
          MultipartFile file,
          CompanyProfileRequestDto companyProfileRequestDto) {
    User user = userRepository.findById(companyProfileRequestDto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    CompanyProfile profile = companyProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil de empresa no encontrado"));

    saveProfile(profile, user, companyProfileRequestDto, file);
  }

  /**
   * Saves or updates the company profile with data and uploads the file.
   *
   * @param profile existing or new company profile entity
   * @param user user linked to the profile
   * @param companyProfileRequestDto profile data
   * @param file file to upload
   * @throws FileUploadException if file upload fails
   */
  private void saveProfile(
          CompanyProfile profile,
          User user,
          CompanyProfileRequestDto companyProfileRequestDto,
          MultipartFile file) {

    String folder = "users/" + companyProfileRequestDto.getUserId();
    String publicId = folder + "/file-" + UUID.randomUUID();
    String url;

    try {
      url = cloudinaryService.uploadAsync(file, folder, publicId)
              .thenApply(result -> (String) result.get("secure_url"))
              .get();
    } catch (Exception e) {
      throw new FileUploadException(
              "Error al subir archivo para el perfil de la compañía", e);
    }

    profile.setTradeName(companyProfileRequestDto.getTradeName());
    profile.setLegalName(companyProfileRequestDto.getLegalName());
    profile.setCuit(companyProfileRequestDto.getCuit());
    profile.setCountry(companyProfileRequestDto.getCountry());
    profile.setCompanyLocation(companyProfileRequestDto.getCompanyLocation());
    profile.setLegalRepresentative(companyProfileRequestDto.getLegalRepresentative());
    profile.setFileUrl(url);
    profile.setUser(user);
    profile.setStatus(CompanyProfileStatus.PENDING);

    companyProfileRepository.save(profile);
  }

  /**
   * Retrieves a company profile by the user's email.
   *
   * @param email user's email address
   * @return the company profile as a DTO
   * @throws ResourceNotFoundException if user or profile not found
   */
  public CompanyProfileResponseDto getByUserEmail(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    CompanyProfile profile = companyProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil de empresa no encontrado"));

    return conversionUtil.convertToDto(profile, CompanyProfileResponseDto.class);
  }
}
