package com.example.demo.service;

import com.example.demo.constant.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class that handles user-related business logic such as
 * registration, authentication, and role assignment.
 */
@RequiredArgsConstructor
@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByEmail(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException("Usuario no encontrado con email: " + username));
  }

  public Optional<User> userByEmailGoogle(String email) {
    return this.userRepository.findByEmail(email);
  }

  /**
   * Creates and saves a new user in the system with the provided full name and email,
   * assigning the default role {@code Role.USER} and an empty password.
   *
   * @param fullname the full name of the new user
   * @param email the email address of the new user
   * @return an {@link Optional} containing the saved {@link User} entity
   */
  public Optional<User> createUserGoogle(String fullname, String email) {
    User newUser = new User();
    newUser.setFullname(fullname);
    newUser.setEmail(email);
    newUser.setRole(Role.USER);
    newUser.setTwoFactorEnabled(false);
    newUser.setPassword(passwordEncoder.encode(""));
    return Optional.of(this.userRepository.save(newUser));
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }
}
