package com.example.demo.dto;

import java.util.UUID;

import com.example.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning user profile information.
 * Contains identifiers, email, full name, and company profile details if available.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponseDto {

    private UUID id;
    private String email;
    private String fullname;

    private String tradeName;
    private String legalName;
    private String cuit;
    private String country;
    private String companyLocation;
    private String legalRepresentative;
    private String phoneNumber;


    public UserProfileResponseDto(User user) {
        this.id = user.getId();
        this.fullname = user.getFullname();
        this.email = user.getEmail();

        if(user.getCompanyProfile() != null) {
            this.tradeName = user.getCompanyProfile().getTradeName();
            this.legalName = user.getCompanyProfile().getLegalName();
            this.cuit = user.getCompanyProfile().getCuit();
            this.country = user.getCompanyProfile().getCountry();
            this.companyLocation = user.getCompanyProfile().getCompanyLocation();
            this.legalRepresentative = user.getCompanyProfile().getLegalRepresentative();
            this.phoneNumber = user.getCompanyProfile().getPhone();
        }
    }


}
