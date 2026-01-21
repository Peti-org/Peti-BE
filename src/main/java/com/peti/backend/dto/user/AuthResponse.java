package com.peti.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@AllArgsConstructor
@Getter
public class AuthResponse {
  private String token;
  private long expiresIn;
  private String refreshToken;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private long refreshTokenExpiresIn;
}
