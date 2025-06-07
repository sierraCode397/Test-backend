package com.example.demo.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO that represents the data of the JWT token.
 */
@Getter
@Setter
public class JwtDataDto {

  private UUID uuid;
  private String fullname;
  private String email;
  private String role;

}
