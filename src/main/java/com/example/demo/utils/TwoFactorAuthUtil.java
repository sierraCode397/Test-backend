package com.example.demo.utils;

import java.security.SecureRandom;

/**
 * Utility class for generating verification codes
 * used in two-factor authentication (2FA).
 *
 * <p>This class cannot be instantiated.</p>
 */
public final class TwoFactorAuthUtil {

  private static final SecureRandom random = new SecureRandom();

  private TwoFactorAuthUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String generateVerificationCode() {
    int code = 100000 + random.nextInt(900000);
    return String.valueOf(code);
  }
}
