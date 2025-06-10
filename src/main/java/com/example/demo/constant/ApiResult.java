package com.example.demo.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class that represents the standard structure of an API response,
 * including success status, message, and optional data.
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApiResult<T> {

  private boolean success;
  private String message;
  private T data;

  public ApiResult(boolean b, String captchaInválido) {
  }
}
