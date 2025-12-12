package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.RequestLogin;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RequestRefreshToken;
import com.peti.backend.dto.user.RequestRegister;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

  @Mock
  private UserService userService;
  @Mock
  private CityService cityService;
  @Mock
  private RoleService roleService;
  @Mock
  private JwtService jwtService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthenticationService authenticationService;

  @Test
  public void testSignup_Success() {
    RequestRegister registerRequest = ResourceLoader.loadResource("registration-data.json", RequestRegister.class);

    CityDto cityDto = ResourceLoader.loadResource("city-response.json", CityDto.class);
    when(cityService.fetchById(anyLong())).thenReturn(Optional.of(cityDto));

    Role role = new Role();
    role.setRoleName("USER");
    when(roleService.getUserRole()).thenReturn(role);

    UserDto userDto = ResourceLoader.loadResource("user-response.json", UserDto.class);
    when(userService.createUser(any(User.class))).thenReturn(userDto);

    AuthResponse authResponse = new AuthResponse("test_token", 111111L, "test_refresh_token", 222222L);
    when(jwtService.generateAuthResponse(any())).thenReturn(authResponse);

    RegisterResponse response = authenticationService.signup(registerRequest);

    assertNotNull(response);
    assertEquals("USER", response.getUserDto().getRoleName());
    assertEquals(1, response.getUserDto().getCity().getId());
    assertEquals("test_token", response.getAuthResponse().getToken());
    assertEquals("test_refresh_token", response.getAuthResponse().getRefreshToken());
    verify(cityService).fetchById(anyLong());
  }

  @Test
  public void testSignup_CityNotFound_ThrowsBadRequestException() {
    RequestRegister registerRequest = new RequestRegister();
    ReflectionTestUtils.setField(registerRequest, "cityId", 999L);

    when(cityService.fetchById(999L)).thenReturn(Optional.empty());

    BadRequestException exception = assertThrows(BadRequestException.class, () ->
        authenticationService.signup(registerRequest)
    );
    assertEquals("Selected city with id 999 not found", exception.getMessage());
  }

  @Test
  public void testAuthenticate_Success() {
    String email = "user@example.com";
    String password = "password";
    RequestLogin loginInput = new RequestLogin();
    ReflectionTestUtils.setField(loginInput, "email", email);
    ReflectionTestUtils.setField(loginInput, "password", password);

    AuthResponse authResponse = new AuthResponse("test_token", 111111L, "test_refresh_token", 222222L);
    when(jwtService.generateAuthResponse(email)).thenReturn(authResponse);

    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(userProjection, null));

    AuthResponse result = authenticationService.authenticate(loginInput);
    assertNotNull(result);
    assertEquals("test_token", result.getToken());
    assertEquals("test_refresh_token", result.getRefreshToken());
    verify(authenticationManager).authenticate(any());
  }

  @Test
  public void testAuthenticate_InvalidCredentials() {
    String email = "invalid@example.com";
    String password = "wrong";
    RequestLogin loginInput = new RequestLogin();
    ReflectionTestUtils.setField(loginInput, "email", email);
    ReflectionTestUtils.setField(loginInput, "password", password);

    when(authenticationManager.authenticate(any())).thenThrow(new BadRequestException("Invalid email or password"));

    BadRequestException exception = assertThrows(BadRequestException.class, () ->
        authenticationService.authenticate(loginInput)
    );
    assertEquals("Invalid email or password", exception.getMessage());
  }

  @Test
  public void testRefreshToken_Success() {
    RequestRefreshToken refreshTokenInput = new RequestRefreshToken();
    ReflectionTestUtils.setField(refreshTokenInput, "refreshToken", "valid_refresh_token");

    AuthResponse authResponse = new AuthResponse("new_token", 333333L, "new_refresh_token", 444444L);
    when(jwtService.isRefreshTokenValid("valid_refresh_token")).thenReturn(true);
    when(jwtService.updateAuthResponse("valid_refresh_token")).thenReturn(authResponse);

    AuthResponse result = authenticationService.authenticate(refreshTokenInput);
    assertNotNull(result);
    assertEquals("new_token", result.getToken());
    assertEquals("new_refresh_token", result.getRefreshToken());
  }

  @Test
  public void testRefreshToken_InvalidToken_ThrowsBadRequestException() {
    RequestRefreshToken refreshTokenInput = new RequestRefreshToken();
    ReflectionTestUtils.setField(refreshTokenInput, "refreshToken", "invalid_refresh_token");

    when(jwtService.isRefreshTokenValid("invalid_refresh_token")).thenReturn(false);

    BadRequestException exception = assertThrows(BadRequestException.class, () ->
        authenticationService.authenticate(refreshTokenInput)
    );
    assertEquals("Invalid refresh token", exception.getMessage());
  }
}
