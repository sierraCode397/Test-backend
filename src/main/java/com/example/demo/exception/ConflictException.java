package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception that represents a conflict error (HTTP 409).
 * Thrown when a request conflicts with the current state of the resource,
 * such as attempting to create a duplicate entry.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
