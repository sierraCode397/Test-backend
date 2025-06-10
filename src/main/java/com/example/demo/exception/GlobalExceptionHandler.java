package com.example.demo.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler that captures and processes different types of exceptions
 * thrown by the application. It converts them into standardized HTTP error responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles {@link ResourceNotFoundException} and returns a 404 error response.
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
   * Handles {@link BadRequestException} and returns a 400 error response.
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
   * Handles {@link UnauthorizedException} and returns a 401 error response.
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
   * Handles {@link ForbiddenException} and returns a 403 error response.
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
   * Handles {@link ConflictException} and returns a 409 error response.
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
   * Handles {@link ServiceUnavailableException} and returns a 503 error response.
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
   * Handles any uncaught exceptions and returns a 500 error response.
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
   * Handles {@link MethodArgumentNotValidException} thrown when validation on an argument fails.
   * Typically triggered by annotations like {@code @NotBlank}, {@code @Email}, etc. in DTOs.
   *
   * @param ex the validation exception.
   * @param request the current web request.
   * @return a 400 Bad Request with validation error details.
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

  /**
   * Handles {@link FileUploadException} and returns a 500 error response.
   */
  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<ErrorResponse> handleFileUploadException(
          FileUploadException ex, WebRequest request) {

    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage(),
            "FILE_UPLOAD_ERROR",
            ex.getLocalizedMessage(),
            request.getDescription(false).replace("uri=", "")
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handles {@link ExpiredJwtException} when a JWT token has expired.
   */
  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ErrorResponse> handleExpiredJwtException(
          ExpiredJwtException ex,
          WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Token expirado.",
            "TOKEN_EXPIRED",
            ex.getLocalizedMessage(),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles general {@link JwtException} when the token is malformed or invalid.
   */
  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Token inválido o mal formado.",
            "TOKEN_INVALID",
            ex.getLocalizedMessage(),
            request.getDescription(false).split("=")[1]
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles {@link MethodArgumentTypeMismatchException}
   * when a controller parameter has the wrong type.
   * Especially useful for enum values.
   *
   * @param ex the mismatch exception.
   * @param request the web request that caused the exception.
   * @return a 400 Bad Request with explanation of expected values.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatchException(
          MethodArgumentTypeMismatchException ex, WebRequest request) {
    String message;
    if (ex.getRequiredType().isEnum()) {
      Object[] enumConstants = ex.getRequiredType().getEnumConstants();
      String validValues = String.join(", ",
              java.util.Arrays.stream(enumConstants)
                      .map(Object::toString)
                      .toList());
      message = String.format("Valor inválido '%s' para el parámetro '%s'. Valores válidos: %s",
              ex.getValue(), ex.getName(), validValues);
    } else {
      message = String.format("Valor inválido '%s' para el parámetro '%s'.",
              ex.getValue(), ex.getName());
    }
    ErrorResponse errorResponse = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            message,
            "INVALID_ENUM_VALUE",
            ex.getLocalizedMessage(),
            request.getDescription(false).replace("uri=", "")
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

}
