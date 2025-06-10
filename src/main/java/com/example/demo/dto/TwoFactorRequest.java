package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Two-Factor Authentication request.
 * Contains the user's email and the 2FA verification code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoFactorRequest {
  private String email;
  private String code;
}
