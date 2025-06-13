package com.example.demo.service;

import com.example.demo.dto.UserProfileResponseDto;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar perfiles de usuario.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    /**
     * Obtiene el perfil de usuario basado en su email.
     *
     * @param email el email del usuario.
     * @return el perfil del usuario en formato UserProfileResponseDto.
     * @throws ResourceNotFoundException si el usuario no es encontrado.
     */
    public UserProfileResponseDto getUserProfile(String email) {
        User user = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return new UserProfileResponseDto(user);
    }
}
