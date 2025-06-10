package com.example.demo.utils;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Custom OIDC user service that extends {@link OidcUserService} to load and process
 * user information from an OpenID Connect (OIDC) provider.
 * It checks if the user already exists in the application database using the email,
 * creates the user if not present, and adds application-specific
 * attributes (such as user ID and role)
 * to the OIDC user attributes before returning the {@link OidcUser} instance.
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
  private final UserService userService;

  /**
   * Loads the user from the OIDC provider and enriches the user attributes with
   * application-specific data.
   *
   * @param userRequest the OIDC user request containing access tokens and configuration
   * @return a customized {@link OidcUser} containing additional attributes like userId and role
   */
  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) {
    OidcUser oidcUser = super.loadUser(userRequest);
    System.out.println("CustomOidcUserService: cargando usuario OIDC");

    String email = oidcUser.getEmail();
    String fullname = (String) oidcUser.getAttributes().get("name");

    Optional<User> existingUser = userService.usuarioPorCorreoGoogle(email);
    System.out.println("existingUser presente? " + existingUser.isPresent());

    if (existingUser.isEmpty()) {
      existingUser = this.userService.crearUsuarioGoogle(fullname, email);
    }

    Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());

    existingUser.ifPresent(user -> {
      attributes.put("userId", user.getId());
      attributes.put("role", user.getRole().name());
    });

    return new DefaultOidcUser(
            oidcUser.getAuthorities(),
            oidcUser.getIdToken(),
            oidcUser.getUserInfo(),
            "email") {
      @Override
      public Map<String, Object> getAttributes() {
        return attributes;
      }
    };
  }
}
