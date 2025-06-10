package com.example.demo.utils;

import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Custom filter that intercepts HTTP requests to validate the JWT token
 * present in the Authorization header. If the token is valid, the user's
 * authentication is set in Spring Security's context.
 * This filter is executed once per request and ensures that only users with
 * a valid token can access protected resources.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserService userDetailsService;

  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String token = authHeader.substring(7);

    try {
      final String username = jwtService.extractUsername(token);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(token, userDetails)) {
          System.out.println("TOKEN MALO >;C");
          throw new InvalidTokenException("Token inválido");
        }
        Boolean twoFaPending = jwtService.extractTwoFaPending(token);
        String path = request.getRequestURI();
        boolean is2FaVerificationEndpoint = path.equals("/api/auth/2fa/validate");
        if (Boolean.TRUE.equals(twoFaPending) && !is2FaVerificationEndpoint) {
          throw new UnauthorizedException("2FA verification required");
        }


        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      throw new InvalidTokenException("Token expirado", e);
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException("Token inválido", e);
    }
  }
}