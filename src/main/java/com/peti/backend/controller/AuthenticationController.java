package com.peti.backend.controller;

import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RequestLogin;
import com.peti.backend.dto.user.RequestRefreshToken;
import com.peti.backend.dto.user.RequestRegister;
import com.peti.backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@Tag(name = "Auth", description = "Operations needed to authenticate user")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/signup")
  public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RequestRegister registerUserDto) {
    RegisterResponse registeredUser = authenticationService.signup(registerUserDto);

    return ResponseEntity.ok(registeredUser);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody RequestLogin requestLogin) {
    AuthResponse authResponse = authenticationService.authenticate(requestLogin);
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RequestRefreshToken requestRefreshToken) {
    AuthResponse authResponse = authenticationService.authenticate(requestRefreshToken);
    return ResponseEntity.ok(authResponse);
  }
}
