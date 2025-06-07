package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception that represents a bad request error (HTTP 400).
 * Thrown when the request parameters or data do not meet the expected validations.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
