package com.example.demo.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception to indicate that a JWT token is invalid or expired.
 * This exception extends {@link AuthenticationException} and can be thrown
 * during the authentication process when the token validation fails.
 */
public class InvalidTokenException extends AuthenticationException {
  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
