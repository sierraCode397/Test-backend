package com.example.demo.utils;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * CustomOAuth2UserService extends DefaultOAuth2UserService to customize
 * the OAuth2 user loading process, integrating user data from the local database.
 */
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

  private final UserService userService;

  /**
   * Loads the OAuth2User from the user request, checks if the user exists in the local database,
   * creates a new user if not present, and enriches the OAuth2User attributes with additional info.
   *
   * @param userRequest the OAuth2 user request
   * @return a customized OAuth2User with additional attributes
   * @throws OAuth2AuthenticationException if an error occurs while loading the user
   */
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    System.out.println("OAuth2User loadUser");
    OAuth2User oauth2User = super.loadUser(userRequest);
    Map<String, Object> customAttributes = new HashMap<>(oauth2User.getAttributes());

    String email = (String) oauth2User.getAttributes().get("email");
    String fullname = (String) oauth2User.getAttributes().get("name");

    Optional<User> existingUser = userService.userByEmailGoogle(email);
    System.out.println("existingUser presente? " + existingUser.isPresent());

    if (existingUser.isEmpty()) {
      existingUser = this.userService.createUserGoogle(fullname, email);
    }

    existingUser.ifPresent(user -> {
      customAttributes.put("userId", user.getId());
      customAttributes.put("role", user.getRole().name());
      customAttributes.put("twoFactorEnabled", user.isTwoFactorEnabled());
    });

    return new DefaultOAuth2User(oauth2User.getAuthorities(), customAttributes, "email");
  }
}