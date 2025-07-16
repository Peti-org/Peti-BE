package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.user.UpdatePasswordDto;
import com.peti.backend.dto.user.UpdateUserDto;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.UserRepository;
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
    UpdateUserDto updateUserDto = ResourceLoader.loadResource("update-user-request.json", UpdateUserDto.class);
    City city = ResourceLoader.loadResource("city-entity.json", City.class);

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
    when(entityManager.getReference(City.class, updateUserDto.getCityId())).thenReturn(city);
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserDto result = userService.updateUser(user.getUserId(), updateUserDto);
    assertNotNull(result);
    assertEquals(updateUserDto.getFirstName(), result.getFirstName());
  }

  @Test
  public void testUpdateUser_NotFound() {
    UUID userId = UUID.randomUUID();
    UpdateUserDto updateUserDto = ResourceLoader.loadResource("update-user-request.json", UpdateUserDto.class);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    UserDto result = userService.updateUser(userId, updateUserDto);
    assertNull(result);
  }

  @Test
  public void testUpdatePassword_Success() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    UpdatePasswordDto passwordDto = ResourceLoader.loadResource("update-password-request.json", UpdatePasswordDto.class);

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())).thenReturn(true);

    boolean result = userService.updatePassword(user.getUserId(), passwordDto);
    assertTrue(result);
  }

  @Test
  public void testUpdatePassword_Failure() {
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    UpdatePasswordDto passwordDto = ResourceLoader.loadResource("update-password-request.json", UpdatePasswordDto.class);

    when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())).thenReturn(false);

    boolean result = userService.updatePassword(user.getUserId(), passwordDto);
    assertFalse(result);
  }

  @Test
  public void testDeleteUser() {
    UUID userId = UUID.randomUUID();
    doNothing().when(userRepository).deleteById(userId);

    userService.deleteUser(userId);
    verify(userRepository).deleteById(userId);
  }
}
