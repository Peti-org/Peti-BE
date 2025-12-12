package com.peti.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RequestRefreshToken {

  @NotBlank(message = "Refresh token cannot be blank")
  @Schema(description = "Jwt refresh token", defaultValue = "wewemdskdsazfsdfsfd")
  private String refreshToken;
}
