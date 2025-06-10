package com.example.demo.entity;

import com.example.demo.constant.CompanyProfileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing the profile of a company.
 * Includes legal, location, and representative data,
 * along with file URL and profile status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "company_profile")
public class CompanyProfile {

  @Id
  @GeneratedValue
  @JdbcTypeCode(SqlTypes.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;
  @Column(nullable = false, length = 300, name = "trade_name")
  private String tradeName;
  @Column(nullable = false, length = 300, name = "legal_name")
  private String legalName;
  @Column(nullable = false)
  private String cuit;
  @Column(nullable = false)
  private String country;
  @Column(nullable = false, length = 300, name = "company_location")
  private String companyLocation;
  @Column(nullable = false, length = 300, name = "legal_representative")
  private String legalRepresentative;
  @Column(nullable = false, name = "file_url")
  private String fileUrl;
  @OneToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
  private User user;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CompanyProfileStatus status;
}
