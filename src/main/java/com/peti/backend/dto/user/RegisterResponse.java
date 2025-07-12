package com.peti.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.peti.backend.dto.CityDto;
import com.peti.backend.model.User;
import com.peti.backend.service.CityService;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class RegisterResponse {

  private UUID userId;
  private String email;
  private String firstName;
  private String lastName;
  private LocalDate birthDate;
  private CityDto city;
  private String roleName;
  @JsonUnwrapped
  private AuthResponse authResponse;

  /**
   * Converts a User entity to a UserDto
   *
   * @param user the User entity to convert
   * @return a new UserDto with data from the User entity
   */
  public static RegisterResponse fromUser(User user, AuthResponse authResponse, CityDto cityDto) {
    if (user == null) {
      return null;
    }

    RegisterResponse userDto = new RegisterResponse();
    userDto.setUserId(user.getUserId());
    userDto.setEmail(user.getEmail());
    userDto.setFirstName(user.getFirstName());
    userDto.setLastName(user.getLastName());
    userDto.setBirthDate(user.getBirthday().toLocalDate());
    userDto.setRoleName(user.getRole().getRoleName());
    userDto.setAuthResponse(authResponse);
    userDto.setCity(cityDto);

    return userDto;
  }
}
