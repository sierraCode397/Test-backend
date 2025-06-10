package com.example.demo.service;

import com.example.demo.constant.Role;
import com.example.demo.dto.auth.JwtDataDto;
import com.example.demo.dto.auth.LoginRequestDto;
import com.example.demo.dto.auth.RegisterRequestDto;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ServiceUnavailableException;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class responsible for authentication operations like login and registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final TwoFactorAuthService twoFactorService;
  private final UserService userService;

  /**
   * Generates a JWT token for the given email.
   *
   * @param email the user's email address
   * @return a JWT token if authentication is successful
   * @throws ResourceNotFoundException if the user is not found
   */
  public String generateJwtToken(String email) {
    User user = userService.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    JwtDataDto jwtDataDto = new JwtDataDto();
    jwtDataDto.setUuid(user.getId());
    jwtDataDto.setFullname(user.getFullname());
    jwtDataDto.setEmail(user.getEmail());
    jwtDataDto.setRole(user.getRole().name());
    jwtDataDto.setTwoFactorEnabled(user.isTwoFactorEnabled());
    return jwtService.generateToken(jwtDataDto);
  }

  /**
   * Registers a new user in the system with the default role of USER.
   *
   * @param request DTO containing the new user's registration data
   * @return a JWT token generated for the new user
   * @throws ConflictException if the email is already registered
   * @throws ServiceUnavailableException if an error occurs during registration
   */
  @Transactional
  public String register(RegisterRequestDto request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("La confirmación de contraseña no coincide");
    }
    try {
      User u = new User();
      u.setFullname(request.getFullname());
      u.setEmail(request.getEmail());
      u.setPassword(passwordEncoder.encode(request.getPassword()));
      u.setRole(Role.USER);
      u.setTwoFactorEnabled(false);
      userRepository.save(u);

      JwtDataDto jwtDataDto = new JwtDataDto();
      jwtDataDto.setUuid(u.getId());
      jwtDataDto.setFullname(u.getFullname());
      jwtDataDto.setEmail(u.getEmail());
      jwtDataDto.setRole(u.getRole().name());
      jwtDataDto.setTwoFactorEnabled(u.getTwoFactorEnabled());

      return jwtService.generateToken(jwtDataDto);
    } catch (DataIntegrityViolationException e) {
      throw new ConflictException("El email ya está en uso");
    } catch (Exception e) {
      throw new ServiceUnavailableException("Error al registrar el usuario");
    }
  }

  /**
   * Extracts user information from the given JWT token.
   *
   * @param token the JWT token from which to extract the user's email
   * @return a JwtDataDto containing the user's UUID, full name, email, and role
   * @throws ResourceNotFoundException if no user is found with the extracted email
   */
  public JwtDataDto infoUser(String token) {
    String email = jwtService.extractUsername(token);
    User user = userService.findByEmail(email)
            .orElseThrow(() ->
                    new ResourceNotFoundException("No se encontró el usuario con email: " + email));

    JwtDataDto dto = new JwtDataDto();
    dto.setUuid(user.getId());
    dto.setFullname(user.getFullname());
    dto.setEmail(user.getEmail());
    dto.setRole(user.getRole().name());
    dto.setTwoFactorEnabled(user.isTwoFactorEnabled());
    return dto;
  }

  /**
   * Validates user credentials using the authentication manager.
   *
   * @param request DTO containing the user's login credentials
   * @return the User entity if authentication is successful
   * @throws BadRequestException if credentials are invalid
   * @throws ResourceNotFoundException if the user is not found
   */
  public User validateUserCredentials(LoginRequestDto request) {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
      );
    } catch (BadCredentialsException ex) {
      throw new BadRequestException("Usuario o contraseña inválidos");
    }

    return userService.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
  }

  /**
   * Sends the 2FA verification code to the user's email.
   *
   * @param user the user to whom the 2FA code is sent
   */
  public void sendTwoFactorCodeToEmail(User user) {
    twoFactorService.sendVerificationCode(user);
  }


}

