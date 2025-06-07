package com.example.demo.exception;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Standard structure for API error responses.
 * Contains information such as HTTP status, error message,
 * error code, detailed message, and the request path.
 */
@Getter
@Setter
public class ErrorResponse {
  private int statusCode;
  private String message;
  private String errorCode;
  private String details;
  private String path;

  /**
   * Constructs an ErrorResponse object, automatically setting the timestamp to the current time.
   *
   * @param statusCode HTTP status code (e.g. 400, 404, 500)
   * @param message Human-readable error message
   * @param errorCode Application-specific error code (e.g. "USER_NOT_FOUND")
   * @param details Technical details about the error
   * @param path URI path where the error occurred
   */
  public ErrorResponse(int statusCode,
                       String message,
                       String errorCode,
                       String details,
                       String path) {

    this.statusCode = statusCode;
    this.message = message;
    this.errorCode = errorCode;
    this.details = details;
    this.path = path;
  }

}
