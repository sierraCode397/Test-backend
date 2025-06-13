package com.example.demo.controller;


import com.example.demo.dto.UserProfileResponseDto;
import com.example.demo.service.AuthService;

import com.example.demo.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * Controller for handling user profile related requests.
 * Provides an endpoint to retrieve the authenticated user's profile information.
 */

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserProfileController {

    public final UserProfileService userProfileService;
    private final AuthService authService;

    /**
     * Endpoint to get the user profile.
     * It retrieves the authenticated user's email and returns their profile information.
     *
     * @return ResponseEntity containing UserProfileResponseDto with user profile data
     */
    @GetMapping
    public ResponseEntity<UserProfileResponseDto> getUserProfile() {
        String email = authService.getAuthenticatedUserEmail();
        UserProfileResponseDto userProfile = userProfileService.getUserProfile(email);

        return ResponseEntity.ok(userProfile);
    }


}
