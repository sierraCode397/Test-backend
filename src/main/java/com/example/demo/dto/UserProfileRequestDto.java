package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a user profile.
 * Contains essential information related to the user.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequestDto {

    private UUID id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 3, max = 100)
    private String fullname;

    @Pattern(regexp = "\\d{9,15}", message = "Debe contener solo números y tener entre 9 y 15 caracteres.")
    private String phone;

    @NotBlank
    private String address;
}
