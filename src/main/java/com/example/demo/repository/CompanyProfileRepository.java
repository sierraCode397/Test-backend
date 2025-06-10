package com.example.demo.repository;

import com.example.demo.entity.CompanyProfile;
import com.example.demo.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the CompanyProfile entity.
 * Provides CRUD operations using JpaRepository.
 */
@Repository
public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, UUID> {

  boolean existsByUser(User user);

  Optional<CompanyProfile> findByUser(User user);
}
