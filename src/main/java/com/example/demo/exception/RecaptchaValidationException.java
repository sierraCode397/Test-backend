package com.example.demo.exception;

/**
 * Exception thrown when reCAPTCHA validation fails.
 * This exception is used to indicate that the reCAPTCHA response
 * from the client is invalid or could not be verified.
 */
public class RecaptchaValidationException extends RuntimeException {

  public RecaptchaValidationException(String message) {
    super(message);
  }

  public RecaptchaValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}