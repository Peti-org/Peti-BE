package com.peti.backend.controller;

import com.peti.backend.dto.user.RequestUpdatePassword;
import com.peti.backend.dto.user.RequestUpdateUser;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.RoleService;
import com.peti.backend.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Operations for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
  //todo add admin methods of managing users
  //todo add tests coverage to controller and service

  private final UserService userService;
  private final RoleService roleService;

  @HasAdminRole
  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @HasAdminRole
  @GetMapping("/roles/refresh")
  public ResponseEntity<Void> refreshRoles() {
    roleService.updateRoles();
    return ResponseEntity.ok().build();
  }

  @HasUserRole
  @GetMapping("/me")
  public ResponseEntity<UserDto> getUserById(@Parameter(hidden = true) UserProjection userProjection) {
    return userService.getUserById(userProjection.getUserId())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasUserRole
  @PutMapping("/me")
  public ResponseEntity<UserDto> updateUser(@Parameter(hidden = true) UserProjection userProjection,
      @Valid @RequestBody RequestUpdateUser requestUpdateUser) {
    UserDto updatedUser = userService.updateUser(userProjection.getUserId(), requestUpdateUser);
    if (updatedUser != null) {
      return ResponseEntity.ok(updatedUser);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @HasUserRole
  @PutMapping("/me/password")
  public ResponseEntity<Void> updatePassword(@Parameter(hidden = true) UserProjection userProjection,
      @Valid @RequestBody RequestUpdatePassword requestUpdatePassword) {
    if (userService.updatePassword(userProjection.getUserId(), requestUpdatePassword)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @HasUserRole
  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteUser(@Parameter(hidden = true) UserProjection userProjection) {
    userService.deleteUser(userProjection.getUserId());
    return ResponseEntity.noContent().build();
  }

  @ModelAttribute("userProjection")
  public UserProjection getUserProjection(Authentication authentication) {
    try {
      return (UserProjection) authentication.getPrincipal();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }
}
