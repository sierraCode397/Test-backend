package com.example.demo.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad UserProfile.
 *
 * Proporciona métodos para acceder a los perfiles de usuario en la base de datos.
 */
public interface UserProfileRepository extends JpaRepository<User, UUID> {

    /**
     * Busca un perfil de usuario por el email del usuario asociado.
     *
     * @param email el email del usuario.
     * @return un Optional que contiene el perfil de usuario si se encuentra.
     */
    Optional<User> findByEmail(String email);
}
