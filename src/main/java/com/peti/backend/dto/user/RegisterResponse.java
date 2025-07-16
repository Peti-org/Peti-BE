package com.peti.backend.dto.user;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {

  @JsonUnwrapped
  private UserDto userDto;
  @JsonUnwrapped
  private AuthResponse authResponse;

  public static RegisterResponse fromUser(UserDto userDto, AuthResponse authResponse) {
    RegisterResponse response = new RegisterResponse();
    response.setUserDto(userDto);
    response.setAuthResponse(authResponse);

    return response;
  }
}
