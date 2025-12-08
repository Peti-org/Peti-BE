package com.peti.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.RequestLogin;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RequestRefreshToken;
import com.peti.backend.dto.user.RequestRegister;
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
    RequestRegister registerUserDto = ResourceLoader.loadResource("registration-data.json", RequestRegister.class);
    RegisterResponse registerResponse = ResourceLoader.loadResource("register-response.json", RegisterResponse.class);
    when(authenticationService.signup(any(RequestRegister.class))).thenReturn(registerResponse);

    ResponseEntity<RegisterResponse> response = authenticationController.register(registerUserDto);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(registerResponse.getUserDto().getEmail(), response.getBody().getUserDto().getEmail());
    assertEquals(registerResponse.getUserDto().getRoleName(), response.getBody().getUserDto().getRoleName());
  }

  @Test
  public void testAuthenticate_Success() {
    RequestLogin requestLogin = ResourceLoader.loadResource("login-data.json", RequestLogin.class);
    AuthResponse authResponse = new AuthResponse("test_token", 111111L, "test_refresh_token", 222222L);
    when(authenticationService.authenticate(any(RequestLogin.class))).thenReturn(authResponse);

    ResponseEntity<AuthResponse> response = authenticationController.authenticate(requestLogin);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("test_token", response.getBody().getToken());
    assertEquals("test_refresh_token", response.getBody().getRefreshToken());
    assertEquals(222222L, response.getBody().getRefreshTokenExpiresIn());
  }

  @Test
  public void testRefreshToken_Success() {
    RequestRefreshToken requestRefreshToken = ResourceLoader.loadResource("refresh-token-data.json", RequestRefreshToken.class);
    AuthResponse authResponse = new AuthResponse("new_token", 333333L, "new_refresh_token", 444444L);
    when(authenticationService.authenticate(any(RequestRefreshToken.class))).thenReturn(authResponse);

    ResponseEntity<AuthResponse> response = authenticationController.refreshToken(requestRefreshToken);
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("new_token", response.getBody().getToken());
    assertEquals("new_refresh_token", response.getBody().getRefreshToken());
    assertEquals(444444L, response.getBody().getRefreshTokenExpiresIn());
  }
}

