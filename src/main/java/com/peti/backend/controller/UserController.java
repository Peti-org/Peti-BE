package com.peti.backend.controller;

import com.peti.backend.dto.user.UserDto;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.service.RoleService;
import com.peti.backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Operations for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

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

//    @GetMapping("/{id}")
//    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
//        UserDto userDto = userService.getUserById(id);
//        if (userDto != null) {
//            return ResponseEntity.ok(userDto);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @PostMapping
//    public ResponseEntity<UserDto> saveUser(@RequestBody UserDto userDto) {
//        return new ResponseEntity<>(userService.saveUser(userDto), HttpStatus.CREATED);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto){
//        UserDto updatedUser = userService.updateUser(id,userDto);
//       if(updatedUser!=null){
//            return ResponseEntity.ok(updatedUser);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
//        if (userService.deleteUser(id)) {
//            return ResponseEntity.noContent().build();
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }
}
