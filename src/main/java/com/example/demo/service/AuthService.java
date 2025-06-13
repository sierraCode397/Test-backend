package com.example.demo.service;

import com.example.demo.constant.Role;
import com.example.demo.dto.auth.JwtDataDto;
import com.example.demo.dto.auth.LoginRequestDto;
import com.example.demo.dto.auth.RegisterRequestDto;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ServiceUnavailableException;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.security.SecureRandom;



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
    private final PasswordResetService passwordResetService;
    private final JavaMailSender mailSender;
    private final PasswordResetTokenRepository tokenRepo;
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
   * Retrieves the email of the currently authenticated user.
   *
   * @return the email of the authenticated user
   * @throws ResourceNotFoundException if the user is not found
   */
  public String getAuthenticatedUserEmail() {
    String email = jwtService.extractUsernameFromSecurityContext(); // Extraer email desde el contexto de seguridad
    User user = userService.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    return user.getEmail(); // Retornar UUID del usuario autenticado
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
     * Inicia el proceso de recuperación de contraseña para el usuario asociado al correo proporcionado.
     * <p>
     * Este método realiza los siguientes pasos:
     * <ul>
     *   <li>Verifica que exista un usuario registrado con el correo electrónico dado.</li>
     *   <li>Genera un token de recuperación de contraseña (UUID) y lo asocia al usuario.</li>
     *   <li>Genera un código numérico aleatorio de 6 dígitos para verificación.</li>
     *   <li>Guarda el código en la base de datos junto al usuario.</li>
     *   <li>Envía el código de verificación al correo electrónico del usuario.</li>
     * </ul>
     * </p>
     *
     * @param email Correo electrónico del usuario que solicita restablecer su contraseña.
     * @return Un mensaje indicando que se ha enviado un correo con el código de verificación.
     * @throws RuntimeException si no se encuentra un usuario registrado con el correo proporcionado.
     */
    public String forgotPassword(String email) {
        System.out.println("Email del usuario: " + email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No se encontró ningún usuario con este email"));
        String token = UUID.randomUUID().toString();
        passwordResetService.createResetTokenForUser(user, token);
        SecureRandom random = new SecureRandom();
        int verificationCode = 100000 + random.nextInt(900000);
        String verificationCodeStr = String.valueOf(verificationCode);
        passwordResetService.saveVerificationCode(user, verificationCodeStr);
        sendPasswordResetEmail(user.getEmail(), verificationCodeStr );
        return "Se ha enviado un correo con el codigo de verificacion para restablecer tu contraseña.";
    }

    /**
     * Sends the 2FA verification code to the user's email.
     *
     * @param user the user to whom the 2FA code is sent
     */
    public void sendTwoFactorCodeToEmail(User user) {
        twoFactorService.sendVerificationCode(user);
    }

    /**
     * Envía un correo electrónico al usuario con el código de verificación para restablecer su contraseña.
     * <p>
     * Este metodo utiliza {@link org.springframework.mail.javamail.JavaMailSender} para enviar un mensaje
     * de texto plano con el código de recuperación. El asunto del correo es "Recuperación de Contraseña"
     * y el cuerpo incluye el código de verificación generado.
     * </p>
     *
     * @param email Correo electrónico del destinatario (usuario que solicitó la recuperación).
     * @param verificationCode Código de verificación de 6 dígitos que debe ingresar el usuario para validar el cambio de contraseña.
     */
    private void sendPasswordResetEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Recuperación de Contraseña");
        message.setText("Pega el siguiente codigo para restablecer tu contraseña: " + verificationCode);
        mailSender.send(message);
    }

    /**
     * Restablece la contraseña de un usuario utilizando un token de recuperación previamente generado.
     * <p>
     * Este metodo realiza las siguientes validaciones y acciones:
     * <ul>
     *   <li>Verifica que el token exista en la base de datos y sea válido.</li>
     *   <li>Rechaza tokens que ya hayan sido utilizados.</li>
     *   <li>Verifica que la nueva contraseña no sea nula ni vacía.</li>
     *   <li>Rechaza tokens expirados basados en la fecha y hora actuales.</li>
     *   <li>Actualiza la contraseña del usuario utilizando codificación segura con {@link org.springframework.security.crypto.password.PasswordEncoder}.</li>
     *   <li>Marca el token como utilizado para evitar reutilización.</li>
     * </ul>
     * </p>
     *
     * @param token Token de recuperación enviado previamente al correo del usuario.
     * @param newPassword Nueva contraseña que será asignada al usuario.
     * @throws RuntimeException si el token es inválido, ya ha sido utilizado o ha expirado.
     * @throws IllegalArgumentException si la nueva contraseña es nula o está vacía.
     */
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if(resetToken.isUsed()) {
            throw new RuntimeException("El código ya ha sido utilizado");
        }

        if(newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }

        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código ha expirado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepo.save(resetToken);
    }
}

