package com.peti.backend.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserValidator userValidator;

  @Test
  @DisplayName("findUserOrThrow - returns user when exists")
  void findUserOrThrow_found() {
    UUID userId = UUID.randomUUID();
    User user = new User(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userValidator.findUserOrThrow(userId);

    assertThat(result).isSameAs(user);
  }

  @Test
  @DisplayName("findUserOrThrow - throws when user missing")
  void findUserOrThrow_notFound() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userValidator.findUserOrThrow(userId))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("verifyPassword - passes when password matches")
  void verifyPassword_matches() {
    User user = new User(UUID.randomUUID());
    user.setPassword("encoded");
    when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

    assertThatCode(() -> userValidator.verifyPassword(user, "raw"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("verifyPassword - throws when password incorrect")
  void verifyPassword_incorrect() {
    User user = new User(UUID.randomUUID());
    user.setPassword("encoded");
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    assertThatThrownBy(() -> userValidator.verifyPassword(user, "wrong"))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("password is incorrect");
  }

  @Test
  @DisplayName("verifyEmailNotSame - passes when emails differ")
  void verifyEmailNotSame_different() {
    User user = new User(UUID.randomUUID());
    user.setEmail("old@example.com");

    assertThatCode(() -> userValidator.verifyEmailNotSame(user, "new@example.com"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("verifyEmailNotSame - throws when same (case-insensitive)")
  void verifyEmailNotSame_same() {
    User user = new User(UUID.randomUUID());
    user.setEmail("test@example.com");

    assertThatThrownBy(() -> userValidator.verifyEmailNotSame(user, "TEST@example.com"))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("different");
  }

  @Test
  @DisplayName("verifyEmailNotTaken - passes when email available")
  void verifyEmailNotTaken_available() {
    when(userRepository.existsByEmail("free@example.com")).thenReturn(false);

    assertThatCode(() -> userValidator.verifyEmailNotTaken("free@example.com"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("verifyEmailNotTaken - throws when email taken")
  void verifyEmailNotTaken_taken() {
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userValidator.verifyEmailNotTaken("taken@example.com"))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("already in use");
  }
}

