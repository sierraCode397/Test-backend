package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending email messages.
 */
@Service
@RequiredArgsConstructor
public class EmailService {
  private final JavaMailSender mailSender;

  /**
   * Sends a 2FA (Two-Factor Authentication) verification code via email.
   *
   * @param to   The recipient's email address.
   * @param code The verification code to be sent.
   */
  public void send2FaCode(String to, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject("Tu código de verificación 2FA");
    message.setText("Tu código de verificación es: "
            + code + ". No compartas este código con nadie.");
    mailSender.send(message);
  }
}
