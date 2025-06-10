package com.example.demo.utils;

import com.example.demo.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom entry point for handling unauthorized access attempts.
 * This class is triggered when a request to a protected resource is made
 * without proper authentication. It returns a structured JSON response
 * containing error details using the {@link ErrorResponse} format.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Handles unauthorized access by sending a 401 response with a custom JSON error body.
   *
   * @param request       the {@link HttpServletRequest} that resulted in the exception
   * @param response      the {@link HttpServletResponse} to send the error message to
   * @param authException the exception that caused the invocation
   * @throws IOException      in case of I/O errors
   * @throws ServletException in case of general servlet errors
   */
  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");

    ErrorResponse error = new ErrorResponse(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Acceso no autorizado",
            "AUTH_ERROR",
            authException.getMessage(),
            request.getRequestURI()
    );

    new ObjectMapper().writeValue(response.getWriter(), error);
  }
}
