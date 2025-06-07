package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application, including authentication and authorization.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthenticationProvider authProvider;

  /**
   * Configures the security filter chain by disabling CSRF, form login, and HTTP basic auth,
   * setting the authentication provider, session management as stateless,
   * and authorization rules.
   *
   * @param http HttpSecurity instance to configure
   * @return configured SecurityFilterChain
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authenticationProvider(authProvider)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/auth/login",
                            "/auth/register",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/swagger-ui.html",
                            "/webjars/**"
                    ).permitAll()
                    .anyRequest().authenticated());

    return http.build();
  }


}