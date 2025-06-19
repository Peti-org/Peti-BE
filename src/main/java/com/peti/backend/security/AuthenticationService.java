package com.peti.backend.security;

import com.peti.backend.dto.CityDto;
import com.peti.backend.dto.LoginUserDto;
import com.peti.backend.dto.RegisterUserDto;
import com.peti.backend.dto.UserDto;
import com.peti.backend.dto.exception.BadRequestException;
import com.peti.backend.model.City;
import com.peti.backend.model.User;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final CityService cityService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public UserDto signup(RegisterUserDto registrationData) {
    User user = registrationData.toUser(passwordEncoder);

    // Save city (required)
    Optional<CityDto> cityDto = cityService.fetchById(registrationData.getCityId());
    if (cityDto.isEmpty()) {
      throw new BadRequestException(String.format("Selected city with id %s not found", registrationData.getCityId()));
    }
    City city = new City();
    city.setCityId(cityDto.get().getId());
    user.setCityByCityId(city);
    User registeredUser = userRepository.save(user);
    return UserDto.fromUser(registeredUser, cityDto.get());
  }

  public User authenticate(LoginUserDto input) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword()));
    //TODO: password check??
    return userRepository.findByEmail(input.getEmail()).orElseThrow();
  }
}
