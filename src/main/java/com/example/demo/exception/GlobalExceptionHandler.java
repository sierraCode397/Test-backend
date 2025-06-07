package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler that captures and processes different types of exceptions
 * thrown by the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles ResourceNotFoundException and returns a 404 error response.
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
          ResourceNotFoundException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            "RESOURCE_NOT_FOUND",
            request.getDescription(false),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles BadRequestException and returns a 400 error response.
   */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(
          BadRequestException ex, HttpServletRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            "BAD_REQUEST",
            ex.toString(),
            request.getRequestURI()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles UnauthorizedException and returns a 401 error response.
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(
          UnauthorizedException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getMessage(),
            "UNAUTHORIZED",
            request.getDescription(false),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles ForbiddenException and returns a 403 error response.
   */
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
          ForbiddenException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage(),
            "FORBIDDEN",
            request.getDescription(false),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
  }

  /**
   * Handles ConflictException and returns a 409 error response.
   */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
          ConflictException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            "CONFLICT",
            request.getDescription(false),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  /**
   * Handles ServiceUnavailableException and returns a 503 error response.
   */
  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
          ServiceUnavailableException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            ex.getMessage(),
            "SERVICE_UNAVAILABLE",
            request.getDescription(false),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
  }

  /**
   * Handles uncaught exceptions and returns a 500 error response.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(
          Exception ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            "INTERNAL_SERVER_ERROR",
            ex.getLocalizedMessage(),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Handles validation exceptions thrown when input data fails to meet defined constraints,
   * such as empty fields or incorrect formats (e.g., an invalid email).
   * This exception is automatically thrown by Spring when annotations like
   * {@code @NotBlank}, {@code @Email}, etc.
   * are used in DTOs and validation fails.
   *
   * @param ex the exception that contains validation errors.
   * @param request the web request that triggered the exception, used to obtain the endpoint path.
   * @return a response with HTTP status 400 (Bad Request) and an error response body.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
          MethodArgumentNotValidException ex,
          WebRequest request) {

    String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message,
            "VALIDATION_ERROR",
            request.getDescription(false),
            request.getDescription(false).split("=")[1]
    );

    return ResponseEntity.badRequest().body(errorResponse);
  }



}
