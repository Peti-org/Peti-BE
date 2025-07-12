package com.peti.backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginUserDto {
  private String email;
  private String password;
}