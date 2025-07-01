package com.peti.backend.controller;

import com.peti.backend.dto.LoginResponse;
import com.peti.backend.dto.LoginUserDto;
import com.peti.backend.dto.RegisterUserDto;
import com.peti.backend.dto.UserDto;
import com.peti.backend.model.User;
import com.peti.backend.security.AuthenticationService;
import com.peti.backend.security.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@Tag(name = "Auth", description = "Operations needed to authenticate user")
public class AuthenticationController {
  private final JwtService jwtService;

  private final AuthenticationService authenticationService;

  public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
    this.jwtService = jwtService;
    this.authenticationService = authenticationService;
  }

  @PostMapping("/signup")
  public ResponseEntity<UserDto> register(@RequestBody RegisterUserDto registerUserDto) {
    UserDto registeredUser = authenticationService.signup(registerUserDto);

    return ResponseEntity.ok(registeredUser);
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
    User authenticatedUser = authenticationService.authenticate(loginUserDto);

    String jwtToken = jwtService.generateToken(authenticatedUser);

    LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());

    return ResponseEntity.ok(loginResponse);
  }
}