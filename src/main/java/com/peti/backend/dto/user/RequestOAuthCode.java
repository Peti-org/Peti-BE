package com.peti.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RequestOAuthCode {

  @NotBlank(message = "OAuth code cannot be blank")
  @Schema(description = "OAuth authorization code", example = "4/0A...")
  private String code;

  @NotBlank(message = "Redirect URI cannot be blank")
  @Schema(description = "Redirect URI used in OAuth flow", example = "http://localhost:5173")
  private String redirectUri;
}