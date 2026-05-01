package com.peti.backend.dto.order;

import com.peti.backend.model.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Brief user info embedded in admin views")
public record UserInfoDto(
    @Schema(description = "User ID") UUID userId,
    @Schema(description = "First name") String firstName,
    @Schema(description = "Last name") String lastName,
    @Schema(description = "Email") String email) {

  public static UserInfoDto from(User user) {
    if (user == null) {
      return null;
    }
    return new UserInfoDto(
        user.getUserId(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail()
    );
  }
}

