package com.peti.backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChangeNotifier {

  private final JavaMailSender mailSender;

  @Value("${peti.mail.from}")
  private String fromAddress;

  @Async
  public void notifyEmailChanged(String oldEmail, String newEmail) {
    sendToOldEmail(oldEmail, newEmail);
    sendToNewEmail(newEmail);
  }

  private void sendToOldEmail(String oldEmail, String newEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(oldEmail);
    message.setSubject("Peti — Your email address has been changed");
    message.setText(
        "Hello,\n\n"
            + "Your account email has been changed to: " + newEmail + "\n\n"
            + "If you did not make this change, please contact our support immediately.\n\n"
            + "— Peti Team");
    send(message, oldEmail);
  }

  private void sendToNewEmail(String newEmail) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(newEmail);
    message.setSubject("Peti — Email address confirmed");
    message.setText(
        "Hello,\n\n"
            + "This email address has been successfully linked to your Peti account.\n\n"
            + "If you did not request this, please contact our support.\n\n"
            + "— Peti Team");
    send(message, newEmail);
  }

  private void send(SimpleMailMessage message, String recipient) {
    try {
      mailSender.send(message);
      log.info("Email notification sent to '{}'", recipient);
    } catch (MailException e) {
      log.error("Failed to send email notification to '{}': {}", recipient, e.getMessage());
    }
  }
}

