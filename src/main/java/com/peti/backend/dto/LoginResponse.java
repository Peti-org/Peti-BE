package com.peti.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {

  private String token;
  private long expiresIn;
}