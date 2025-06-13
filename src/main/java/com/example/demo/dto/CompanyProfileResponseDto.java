package com.example.demo.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning company profile information.
 * Contains identifiers, legal details, location, representative,
 * uploaded file URL and the current status of the company profile.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfileResponseDto {
  private UUID id;
  private String tradeName;
  private String legalName;
  private String cuit;
  private String country;
  private String companyLocation;
  private String legalRepresentative;
  private String fileUrl;
  private String status;
  private String phone;
}
