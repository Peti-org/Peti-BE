package com.peti.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.user.RequestUpdatePassword;
import com.peti.backend.dto.user.RequestUpdateUser;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.service.RoleService;
import com.peti.backend.service.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

  @Mock
  private UserService userService;

  @Mock
  private RoleService roleService;

  @InjectMocks
  private UserController userController;

  @Test
  public void testGetAllUsers() {
    List<UserDto> users = Collections.singletonList(ResourceLoader.loadResource("user-entity.json", UserDto.class));
    when(userService.getAllUsers()).thenReturn(users);

    ResponseEntity<List<UserDto>> response = userController.getAllUsers();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void testGetUserById_Found() {
    UserDto userDto = ResourceLoader.loadResource("user-entity.json", UserDto.class);
    when(userService.getUserById(userDto.getUserId())).thenReturn(Optional.of(userDto));
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<UserDto> response = userController.getUserById(userProjection);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userDto.getUserId(), response.getBody().getUserId());
  }

  @Test
  public void testGetUserById_NotFound() {
    UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    when(userService.getUserById(userId)).thenReturn(Optional.empty());
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<UserDto> response = userController.getUserById(userProjection);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testUpdateUser_Found() {
    UserDto userDto = ResourceLoader.loadResource("user-entity.json", UserDto.class);
    RequestUpdateUser requestUpdateUser = ResourceLoader.loadResource("update-user-request.json", RequestUpdateUser.class);
    when(userService.updateUser(userDto.getUserId(), requestUpdateUser)).thenReturn(userDto);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<UserDto> response = userController.updateUser(userProjection, requestUpdateUser);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userDto.getUserId(), response.getBody().getUserId());
  }

  @Test
  public void testUpdateUser_NotFound() {
    RequestUpdateUser requestUpdateUser = ResourceLoader.loadResource("update-user-request.json", RequestUpdateUser.class);
    when(userService.updateUser(any(UUID.class), any(RequestUpdateUser.class))).thenReturn(null);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<UserDto> response = userController.updateUser(userProjection, requestUpdateUser);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testUpdatePassword_Success() {
    RequestUpdatePassword passwordDto = ResourceLoader.loadResource("update-password-request.json",
        RequestUpdatePassword.class);
    when(userService.updatePassword(any(UUID.class), any(RequestUpdatePassword.class))).thenReturn(true);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<Void> response = userController.updatePassword(userProjection, passwordDto);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testUpdatePassword_Failure() {
    RequestUpdatePassword passwordDto = ResourceLoader.loadResource("update-password-request.json",
        RequestUpdatePassword.class);
    when(userService.updatePassword(any(UUID.class), any(RequestUpdatePassword.class))).thenReturn(false);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<Void> response = userController.updatePassword(userProjection, passwordDto);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void testDeleteUser() {
    doNothing().when(userService).deleteUser(any(UUID.class));
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);

    ResponseEntity<Void> response = userController.deleteUser(userProjection);
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(userService).deleteUser(any(UUID.class));
  }
}
