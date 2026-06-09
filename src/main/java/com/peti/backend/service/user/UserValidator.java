package com.peti.backend.service.user;

import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User findUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BadRequestException("User not found"));
  }

  public void verifyPassword(User user, String rawPassword) {
    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new BadRequestException("Current password is incorrect");
    }
  }

  public void verifyEmailNotSame(User user, String newEmail) {
    if (user.getEmail().equalsIgnoreCase(newEmail)) {
      throw new BadRequestException("New email must be different from the current one");
    }
  }

  public void verifyEmailNotTaken(String email) {
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException(
          "This email is already in use, you can't change email to it. Use another one");
    }
  }
}

