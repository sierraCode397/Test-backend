package com.example.demo.service;

import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;




/*
    * Service for managing password reset tokens.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    /**
     * Crea y almacena un nuevo token de recuperación de contraseña para un usuario específico.
     * <p>
     * Este metodo construye una instancia de {@link PasswordResetToken} con:
     * <ul>
     *   <li>El token generado externamente (por ejemplo, un UUID).</li>
     *   <li>El usuario asociado que solicitó el restablecimiento de contraseña.</li>
     *   <li>Una fecha de expiración fijada a 15 minutos desde el momento de creación.</li>
     * </ul>
     * Luego, el token es guardado en el repositorio para su posterior validación durante el proceso de recuperación.
     * </p>
     *
     * @param user  Usuario que solicitó la recuperación de contraseña.
     * @param token Cadena única generada que funcionará como identificador del proceso de recuperación.
     */
    @Transactional
    public void createResetTokenForUser(User user, String token) {

        //Delete tokens for the user before creating a new one
        tokenRepository.deleteByUser(user);

        PasswordResetToken newToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        System.out.println("Token del serviicio: " + newToken.getToken());
        tokenRepository.save(newToken);
    }


    /**
     * Guarda un código de verificación para un usuario específico.
     * Este método crea un token de recuperación de contraseña con el código proporcionado
     * y lo almacena en la base de datos con una fecha de expiración de 15 minutos.
     *
     * @param user         El usuario al que se le asigna el código de verificación.
     * @param verificationCode El código de verificación a guardar.
     */
    public void saveVerificationCode(User user, String verificationCode) {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(String.valueOf(verificationCode))
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(token);
    }
}
