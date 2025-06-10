package com.example.demo.config;

import com.example.demo.dto.auth.JwtDataDto;
import com.example.demo.utils.CustomOauth2UserService;
import com.example.demo.utils.CustomOidcUserService;
import com.example.demo.utils.JwtAuthFilter;
import com.example.demo.utils.JwtAuthenticationEntryPoint;
import com.example.demo.utils.JwtService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application, including authentication and authorization.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthenticationProvider authProvider;
  private final JwtAuthFilter jwtAuthFilter;
  private final JwtService jwtService;
  private final CustomOauth2UserService customOauth2UserService;
  private final CustomOidcUserService customOidcUserService;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  /**
   * Configures the security filter chain for OAuth2 login with stateless session management.
   *
   * @param http HttpSecurity instance to configure
   * @return configured SecurityFilterChain for OAuth2
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  @Order(1)
  public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authorizeRequest ->
                    authorizeRequest
                            .anyRequest().permitAll()
            )
            .sessionManagement(httpSecuritySessionManagementConfigurer ->
                    httpSecuritySessionManagementConfigurer
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2Login(oauth -> oauth
                    .userInfoEndpoint(userInfo ->
                            userInfo.userService(customOauth2UserService)
                                    .oidcUserService(customOidcUserService)
                    )
                    .successHandler(authenticationSuccessHandler())
            )
            .securityMatcher("/oauth2/**", "/login/oauth2/**");
    return http.build();
  }

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
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authenticationProvider(authProvider)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/login",
                            "/auth/register",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/oauth2/**",
                            "/swagger-resources/**",
                            "/swagger-ui.html",
                            "/webjars/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Defines the authentication success handler for OAuth2 login that generates
   * a JWT token and returns it in the response header and body.
   *
   * @return AuthenticationSuccessHandler instance
   */
  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      UUID userId = (UUID) oauth2User.getAttributes().get("userId");
      String email = (String) oauth2User.getAttributes().get("email");
      String fullname = (String) oauth2User.getAttributes().get("name");
      String role = (String) oauth2User.getAttributes().get("role");
      JwtDataDto jwtDataDto = new JwtDataDto();
      jwtDataDto.setUuid(userId);
      jwtDataDto.setFullname(fullname);
      jwtDataDto.setEmail(email);
      jwtDataDto.setRole(role);
      String token = jwtService.generateToken(jwtDataDto);
      response.setHeader("Authorization", "Bearer " + token);
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      String jsonResponse = String.format("{\"token\": \"%s\"}", token);
      response.getWriter().write(jsonResponse);
    };
  }
}