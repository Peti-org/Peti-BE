package com.peti.backend.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.ChangeEmailResponse;
import com.peti.backend.dto.user.RequestChangeEmail;
import com.peti.backend.dto.user.RequestUpdatePassword;
import com.peti.backend.dto.user.RequestUpdateUser;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.security.JwtService;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EntityManager entityManager;

  @Mock
  private RoleService roleService;

  @Mock
  private JwtService jwtService;

  @Mock
  private EmailChangeNotifier emailChangeNotifier;

  @Mock
  private UserValidator userValidator;

  @InjectMocks
  private UserService userService;

  @Test
  public void testGetUserById_Found() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

    Optional<UserDto> result = userService.getUserById(user.getUserId());
    assertTrue(result.isPresent());
    assertEquals(user.getUserId(), result.get().getUserId());
    assertEquals(user.getEmail(), result.get().getEmail());
  }

  @Test
  public void testGetUserById_NotFound() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    Optional<UserDto> result = userService.getUserById(userId);
    assertFalse(result.isPresent());
  }

  @Test
  public void testCreateUser() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = userService.createUser(user);
    assertNotNull(result);
    assertEquals(user.getEmail(), result.getEmail());
  }

  @Test
  public void testUpdateUser_Found() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestUpdateUser requestUpdateUser = ResourceLoader.loadResource("update-user-request.json", RequestUpdateUser.class);
    City city = ResourceLoader.loadResource("city-entity.json", City.class);

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);
    when(entityManager.getReference(City.class, requestUpdateUser.getCityId())).thenReturn(city);
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = userService.updateUser(user.getUserId(), requestUpdateUser);
    assertNotNull(result);
    assertEquals(requestUpdateUser.getFirstName(), result.getFirstName());
  }

  @Test
  public void testUpdateUser_NotFound() {
    UUID userId = UUID.randomUUID();
    RequestUpdateUser requestUpdateUser = ResourceLoader.loadResource("update-user-request.json", RequestUpdateUser.class);
    when(userValidator.findUserOrThrow(userId))
        .thenThrow(new BadRequestException("User not found"));

    assertThrows(BadRequestException.class,
        () -> userService.updateUser(userId, requestUpdateUser));
  }

  @Test
  public void testUpdatePassword_Success() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestUpdatePassword passwordDto = ResourceLoader.loadResource("update-password-request.json", RequestUpdatePassword.class);

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);

    boolean result = userService.updatePassword(user.getUserId(), passwordDto);
    assertTrue(result);
    verify(userValidator).verifyPassword(user, passwordDto.getOldPassword());
  }

  @Test
  public void testUpdatePassword_WrongPassword() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestUpdatePassword passwordDto = ResourceLoader.loadResource("update-password-request.json", RequestUpdatePassword.class);

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);
    org.mockito.Mockito.doThrow(new BadRequestException("Current password is incorrect"))
        .when(userValidator).verifyPassword(user, passwordDto.getOldPassword());

    assertThrows(BadRequestException.class,
        () -> userService.updatePassword(user.getUserId(), passwordDto));
    verify(userRepository, never()).save(any());
  }

  @Test
  public void testDeleteUser() {
    UUID userId = UUID.randomUUID();
    doNothing().when(userRepository).deleteById(userId);

    userService.deleteUser(userId);
    verify(userRepository).deleteById(userId);
  }

  @Test
  public void testChangeEmail_Success() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestChangeEmail request = new RequestChangeEmail("newemail@example.com", "rawPassword");
    AuthResponse authResponse = new AuthResponse("token", 123L, "refresh", 456L);

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    when(jwtService.generateAuthResponse("newemail@example.com")).thenReturn(authResponse);

    ChangeEmailResponse result = userService.changeEmail(user.getUserId(), request);

    assertNotNull(result);
    assertEquals("newemail@example.com", result.user().getEmail());
    assertEquals("token", result.auth().getToken());
    verify(userValidator).verifyPassword(user, "rawPassword");
    verify(userValidator).verifyEmailNotSame(user, "newemail@example.com");
    verify(userValidator).verifyEmailNotTaken("newemail@example.com");
    verify(emailChangeNotifier).notifyEmailChanged("john.doe@example.com", "newemail@example.com");
  }

  @Test
  public void testChangeEmail_WrongPassword() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestChangeEmail request = new RequestChangeEmail("newemail@example.com", "wrongPass");

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);
    org.mockito.Mockito.doThrow(new BadRequestException("Current password is incorrect"))
        .when(userValidator).verifyPassword(user, "wrongPass");

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> userService.changeEmail(user.getUserId(), request));
    assertTrue(ex.getMessage().contains("password is incorrect"));
    verify(userRepository, never()).save(any());
  }

  @Test
  public void testChangeEmail_SameEmail() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestChangeEmail request = new RequestChangeEmail(user.getEmail(), "rawPassword");

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);
    org.mockito.Mockito.doThrow(new BadRequestException("New email must be different from the current one"))
        .when(userValidator).verifyEmailNotSame(user, user.getEmail());

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> userService.changeEmail(user.getUserId(), request));
    assertTrue(ex.getMessage().contains("different"));
    verify(userRepository, never()).save(any());
  }

  @Test
  public void testChangeEmail_EmailAlreadyInUse() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    RequestChangeEmail request = new RequestChangeEmail("taken@example.com", "rawPassword");

    when(userValidator.findUserOrThrow(user.getUserId())).thenReturn(user);
    org.mockito.Mockito.doThrow(new BadRequestException(
            "This email is already in use, you can't change email to it. Use another one"))
        .when(userValidator).verifyEmailNotTaken("taken@example.com");

    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> userService.changeEmail(user.getUserId(), request));
    assertTrue(ex.getMessage().contains("already in use"));
    verify(userRepository, never()).save(any());
  }
}
