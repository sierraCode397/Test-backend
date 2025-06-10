package com.example.demo.utils;

import com.example.demo.dto.auth.JwtDataDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 */
@Service
public class JwtService {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  private Key signingKey;

  /**
   * Initializes the JWT signing key after injecting the properties.
   * Ensures that the secret has at least 32 characters for the HS256 algorithm.
   */
  @PostConstruct
  public void init() {
    if (secretKey.length() < 32) {
      throw new IllegalArgumentException(
              "El secret JWT debe tener al menos 32 caracteres para HS256.");
    }
    this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
  }

  /**
   * Generates a JWT token using user data.
   *
   * @param d DTO with user data
   * @return JWT token string
   */
  public String generateToken(JwtDataDto d) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", d.getUuid());
    claims.put("fullname", d.getFullname());
    claims.put("email", d.getEmail());
    claims.put("role", "ROLE_" + d.getRole());
    claims.put("twoFaEnabled", d.isTwoFactorEnabled());
    return buildToken(claims, d.getEmail(), jwtExpiration);
  }

  /**
   * Builds a JWT token with custom claims and expiration.
   *
   * @param claims     map of claims
   * @param subject    subject (usually username/email)
   * @param expiration expiration time in ms
   * @return JWT token string
   */
  private String buildToken(Map<String, Object> claims, String subject, long expiration) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + expiration))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * Extracts username (email) from token.
   *
   * @param token JWT token string
   * @return username/email
   */
  public String extractUsername(String token) {
    return extractAllClaims(token).getSubject();
  }

  /**
   * Extracts specific claim using a resolver.
   *
   * @param <T>            return type
   * @param token          JWT token string
   * @param claimsResolver function to extract claim
   * @return extracted claim
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    return claimsResolver.apply(extractAllClaims(token));
  }

  /**
   * Validates if token is not expired and belongs to the user.
   *
   * @param token       JWT token string
   * @param userDetails user details object
   * @return true if valid
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  /**
   * Checks if the token is expired.
   *
   * @param token JWT token string
   * @return true if expired
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Gets token expiration date.
   *
   * @param token JWT token string
   * @return expiration date
   */
  private Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }

  /**
   * Parses all claims from the token.
   *
   * @param token JWT token string
   * @return claims object
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  /**
   * Extracts the "twoFaPending" claim from the token.
   *
   * @param token JWT token string
   * @return value of twoFaPending
   */
  public Boolean extractTwoFaPending(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get("twoFaPending", Boolean.class);
  }
}

