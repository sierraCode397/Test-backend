package com.example.demo.entity;

import com.example.demo.constant.Role;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Entity that represents a User in the system.
 * Contains authentication and authorization details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "email")
})
@Access(AccessType.FIELD)
public class User implements UserDetails {


  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 100)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column()
  private Role role;

  @Builder.Default
  @Column(name = "created_at")
  private Date createdAt = new Date();

  @Column(nullable = false)
  private String fullname;

  @Column(nullable = false, name = "two_factor_enabled")
  private Boolean twoFactorEnabled;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private CompanyProfile companyProfile;

  @Override
  @Transient
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public boolean isTwoFactorEnabled() {
    return twoFactorEnabled;
  }

}