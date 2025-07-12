package com.peti.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.LoginUserDto;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RegisterUserDto;
import com.peti.backend.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

  @Mock
  private AuthenticationService authenticationService;

  @InjectMocks
  private AuthenticationController authenticationController;

  @Test
  public void testRegister_Success() {
    RegisterUserDto registerUserDto = ResourceLoader.loadResource("registration-data.json", RegisterUserDto.class);
    RegisterResponse registerResponse = ResourceLoader.loadResource("register-response.json", RegisterResponse.class);
    when(authenticationService.signup(any(RegisterUserDto.class))).thenReturn(registerResponse);

    ResponseEntity<RegisterResponse> response = authenticationController.register(registerUserDto);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(registerResponse.getEmail(), response.getBody().getEmail());
    assertEquals(registerResponse.getRoleName(), response.getBody().getRoleName());
  }

  @Test
  public void testAuthenticate_Success() {
    LoginUserDto loginUserDto = ResourceLoader.loadResource("login-data.json", LoginUserDto.class);
    AuthResponse authResponse = new AuthResponse("test_token", 111111L);
    when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(authResponse);

    ResponseEntity<AuthResponse> response = authenticationController.authenticate(loginUserDto);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("test_token", response.getBody().getToken());
  }
}

