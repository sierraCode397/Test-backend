package com.example.demo.config;

import com.example.demo.dto.auth.JwtDataDto;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.TwoFactorAuthService;
import com.example.demo.service.UserService;
import com.example.demo.utils.CustomOauth2UserService;
import com.example.demo.utils.CustomOidcUserService;
import com.example.demo.utils.JwtAuthFilter;
import com.example.demo.utils.JwtAuthenticationEntryPoint;
import com.example.demo.utils.JwtService;
import jakarta.servlet.http.HttpServletResponse;
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
 * Configuration class for application security.
 * Configures two security filter chains:
 * <ul>
 *   <li>OAuth2 login filter chain with stateless session management for OAuth2 endpoints.</li>
 *   <li>General security filter chain with JWT authentication and stateless sessions.</li>
 * </ul>
 * Also defines a custom authentication success handler that processes OAuth2 login success,
 * generating a JWT token or triggering 2FA if enabled.
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
  private final TwoFactorAuthService twoFactorAuthService;
  private final UserService userService;

  /**
   * Security filter chain for OAuth2 login endpoints.
   * Disables CSRF, enables CORS, permits all requests,
   * uses stateless session management, configures OAuth2 login
   * with custom user services and a custom success handler.
   * Applies only to paths under "/oauth2/**" and "/login/oauth2/**".
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception in case of configuration errors
   */
  @Order(1)
  @Bean
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
   * General security filter chain.
   * Disables CSRF, form login, and HTTP Basic auth,
   * sets the authentication provider,
   * uses stateless session management,
   * configures exception handling for JWT authentication entry point,
   * sets authorization rules permitting some endpoints and requiring authentication for others,
   * and adds a JWT authentication filter.
   *
   * @param http the HttpSecurity to configure
   * @return the configured SecurityFilterChain
   * @throws Exception in case of configuration errors
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
                            "/api/auth/2fa/validate",
                            "/webjars/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Custom authentication success handler for OAuth2 login.
   * When a user successfully authenticates with OAuth2, this handler:
   * <ul>
   *   <li>Extracts user attributes from the OAuth2User.</li>
   *   <li>Checks if 2FA is enabled for the user.</li>
   *   <li>If 2FA is enabled, sends a verification code and returns a 2FA message.</li>
   *   <li>If 2FA is not enabled, generates a JWT token and returns
   *        it in the response header and body.</li>
   * </ul>
   *
   * @return the AuthenticationSuccessHandler instance
   */
  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
      OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
      UUID userId = (UUID) oauth2User.getAttributes().get("userId");
      String email = (String) oauth2User.getAttributes().get("email");
      String fullname = (String) oauth2User.getAttributes().get("name");
      String role = (String) oauth2User.getAttributes().get("role");
      Boolean is2faEnabled = (Boolean) oauth2User.getAttributes().get("twoFactorEnabled");
      JwtDataDto jwtDataDto = new JwtDataDto();
      jwtDataDto.setUuid(userId);
      jwtDataDto.setFullname(fullname);
      jwtDataDto.setEmail(email);
      jwtDataDto.setRole(role);
      jwtDataDto.setTwoFactorEnabled(is2faEnabled);
      if (is2faEnabled) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        twoFactorAuthService.sendVerificationCode(user);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"message\": \"Código 2FA enviado\"}");
      } else {
        jwtDataDto.setTwoFactorEnabled(false);
        String token = jwtService.generateToken(jwtDataDto);
        response.setHeader("Authorization", "Bearer " + token);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"token\": \"%s\"}", token));
      }

    };
  }
}