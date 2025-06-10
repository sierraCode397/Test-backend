package com.example.demo.dto.auth;

import java.util.UUID;
import lombok.Data;


/**
 * DTO that represents the data of the JWT token.
 */
@Data
public class JwtDataDto {

  private UUID uuid;
  private String fullname;
  private String email;
  private String role;

}
