package com.peti.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peti.backend.ResourceLoader;
import com.peti.backend.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.LoginUserDto;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RegisterUserDto;
import com.peti.backend.model.User;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.security.JwtService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

  @Mock
  private UserRepository userRepository;
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
    RegisterUserDto registerRequest = ResourceLoader.loadResource("registration-data.json", RegisterUserDto.class);

    CityDto cityDto = ResourceLoader.loadResource("city-response.json", CityDto.class);
    when(cityService.fetchById(anyInt())).thenReturn(Optional.of(cityDto));

    Role role = new Role();
    role.setRoleName("USER");
    when(roleService.getLowestRole()).thenReturn(role);

    when(userRepository.save(any(User.class))).then(AdditionalAnswers.returnsFirstArg());

    AuthResponse authResponse = new AuthResponse("test_token", 111111L);
    when(jwtService.generateAuthResponse(any())).thenReturn(authResponse);

    RegisterResponse response = authenticationService.signup(registerRequest);

    assertNotNull(response);
    assertEquals("USER", response.getRoleName());
    assertEquals(1, response.getCity().getId());
    assertEquals("test_token", response.getAuthResponse().getToken());
    verify(cityService).fetchById(anyInt());
  }

  @Test
  public void testAuthenticate_Success() {
    String email = "test@example.com";
    String password = "password";
    LoginUserDto loginInput = new LoginUserDto();
    ReflectionTestUtils.setField(loginInput, "email", email);
    ReflectionTestUtils.setField(loginInput, "password", password);

    User user = new User();
    user.setEmail(email);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    AuthResponse authResponse = new AuthResponse("test_token", 111111L);
    when(jwtService.generateAuthResponse(user)).thenReturn(authResponse);

    authenticationService.authenticate(loginInput);
    verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(email, password));
  }

  @Test
  public void testAuthenticate_InvalidCredentials() {
    String email = "invalid@example.com";
    String password = "wrong";
    LoginUserDto loginInput = new LoginUserDto();
    ReflectionTestUtils.setField(loginInput, "email", email);
    ReflectionTestUtils.setField(loginInput, "password", password);

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    BadRequestException exception = assertThrows(BadRequestException.class, () ->
        authenticationService.authenticate(loginInput)
    );
    assertEquals("Invalid email or password", exception.getMessage());
  }
}
