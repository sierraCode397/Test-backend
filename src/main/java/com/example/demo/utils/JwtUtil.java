package com.example.demo.utils;

import com.example.demo.dto.JwtDataDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 */
@Service
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  /**
   * Generates a JWT token using the data provided in the JwtDataDto.
   *
   * @param d the DTO containing username, email, and role.
   * @return the generated JWT token as a String.
   */
  public String generateToken(JwtDataDto d) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("fullname", d.getFullname());
    claims.put("email", d.getEmail());
    claims.put("role", d.getRole());
    return generateToken(claims, d);
  }

  /**
   * Generates a JWT token with extra custom claims and subject.
   *
   * @param extraClaims additional claims to include in the token.
   * @param d the DTO containing the subject (email).
   * @return the generated JWT token.
   */
  public String generateToken(Map<String, Object> extraClaims, JwtDataDto d) {
    return buildToken(extraClaims, d.getEmail(), jwtExpiration);
  }

  /**
   * Builds a JWT token with given claims, subject, and expiration time.
   *
   * @param extraClaims additional claims to include.
   * @param subject the subject of the token (typically an email or username).
   * @param expiration the expiration time in milliseconds.
   * @return the generated JWT token.
   */
  private String buildToken(
          Map<String, Object> extraClaims,
          String subject,
          long expiration
  ) {
    return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * Gets the signing key derived from the secret key.
   *
   * @return the HMAC SHA key.
   */
  private Key getSignInKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }
}
