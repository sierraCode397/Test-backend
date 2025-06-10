package com.example.demo.exception;

public class RecaptchaValidationException extends RuntimeException {

    public RecaptchaValidationException(String message) {
        super(message);
    }

    public RecaptchaValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}