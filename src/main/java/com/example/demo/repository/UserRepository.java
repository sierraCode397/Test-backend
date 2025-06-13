package com.example.demo.repository;

import com.example.demo.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing User entities.
 * Provides database operations such as finding a user by email.
 */
public interface UserRepository extends JpaRepository<User, UUID> {



  Optional<User> findByEmail(String email);
}
