package com.peti.backend.service;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.dto.user.AuthResponse;
import com.peti.backend.dto.user.LoginUserDto;
import com.peti.backend.dto.user.RegisterResponse;
import com.peti.backend.dto.user.RegisterUserDto;
import com.peti.backend.model.User;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final CityService cityService;
  private final RoleService roleService;
  private final JwtService jwtService;

  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public RegisterResponse signup(RegisterUserDto registrationData) {
    // Validation for city existence
    CityDto cityDto = cityService.fetchById(registrationData.getCityId())
        .orElseThrow(() -> new BadRequestException(
            String.format("Selected city with id %s not found", registrationData.getCityId())));

    User user = registrationData.toUser(passwordEncoder);
    user.setCityByCityId(cityDto.toCityWithId());
    user.setRole(roleService.getLowestRole());

    User registeredUser = userRepository.save(user);
    AuthResponse authResponse = jwtService.generateAuthResponse(registeredUser);
    return RegisterResponse.fromUser(registeredUser, authResponse, cityDto);
  }

  public AuthResponse authenticate(LoginUserDto input) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));

    User user = userRepository.findByEmail(input.getEmail())
        .orElseThrow(() -> new BadRequestException("Invalid email or password"));
    return jwtService.generateAuthResponse(user);
  }
}
