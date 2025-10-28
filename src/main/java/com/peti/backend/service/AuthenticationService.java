package com.peti.backend.service;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.RequestLogin;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RequestRegister;
import com.peti.backend.dto.user.UserDto;
import com.peti.backend.model.domain.User;
import com.peti.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserService userService;
  private final CityService cityService;
  private final RoleService roleService;
  private final JwtService jwtService;

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public RegisterResponse signup(RequestRegister registrationData) {
    // Validation for city existence
    CityDto cityDto = cityService.fetchById(registrationData.getCityId())
        .orElseThrow(() -> new BadRequestException(
            String.format("Selected city with id %s not found", registrationData.getCityId())));

    User user = registrationData.toUser(passwordEncoder);
    user.setCityByCityId(cityDto.toCityWithId());
    user.setRole(roleService.getUserRole());

    // todo check if really user has city in representation and role (don't has fixxxxx)
    UserDto registeredUser = userService.createUser(user);

    // Set city for the registered user, as a database doesn't return it automatically
    registeredUser.setCity(cityDto);
    AuthResponse authResponse = jwtService.generateAuthResponse(registeredUser.getEmail());
    return RegisterResponse.fromUser(registeredUser, authResponse);
  }

  public AuthResponse authenticate(RequestLogin input) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    return jwtService.generateAuthResponse(userDetails.getUsername());
  }
}
