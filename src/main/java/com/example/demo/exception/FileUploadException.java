package com.example.demo.exception;

/**
 * Exception thrown when an error occurs during file upload.
 * This can be due to I/O issues, invalid file formats, or service failures.
 */
public class FileUploadException extends RuntimeException {
  public FileUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}