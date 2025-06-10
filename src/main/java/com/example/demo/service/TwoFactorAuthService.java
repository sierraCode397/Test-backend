package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.TwoFactorAuthUtil;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

/**
 * Service responsible for handling Two-Factor Authentication (2FA)
 * operations such as enabling/disabling 2FA, sending verification codes,
 * and validating user-submitted codes.
 */
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

  public final UserService userService;
  public final UserRepository userRepository;
  public final EmailService emailService;
  private final RedisTemplate<String, String> redisTemplate;
  private static final long CODE_EXPIRATION_MINUTES = 3;

  /**
   * Updates the 2FA setting for a user based on their email.
   *
   * @param email   User's email address.
   * @param enabled Flag indicating whether 2FA should be enabled or disabled.
   */
  public void updateTwoFactorSetting(String email, boolean enabled) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "User not found with email: " + email));
    user.setTwoFactorEnabled(enabled);
    userRepository.save(user);
  }

  /**
   * Sends a 2FA verification code to the user's email and stores
   * it temporarily in Redis with an expiration time.
   *
   * @param user The user to whom the verification code is sent.
   */
  public void sendVerificationCode(User user) {
    String code = TwoFactorAuthUtil.generateVerificationCode();
    ValueOperations<String, String> ops = redisTemplate.opsForValue();
    ops.set(user.getEmail(), code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);
    emailService.send2FaCode(user.getEmail(), code);
  }

  /**
   * Validates the 2FA code submitted by the user.
   *
   * @param email The email of the user.
   * @param code  The code submitted for verification.
   * @throws UnauthorizedException if the code is invalid or expired.
   */
  public void validateCode(String email, String code) {
    ValueOperations<String, String> ops = redisTemplate.opsForValue();
    String storedCode = ops.get(email);
    if (storedCode == null || !storedCode.equals(code)) {
      throw new UnauthorizedException("Invalid or expired 2FA code.");
    }
    redisTemplate.delete(email);
  }
}