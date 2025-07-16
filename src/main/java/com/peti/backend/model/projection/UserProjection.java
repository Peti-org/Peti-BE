package com.peti.backend.model.projection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
public class UserProjection  implements UserDetails {

  @Getter
  private final UUID userId;
  private final String email;
  @Getter
  private final String password;
  @Getter
  private final int roleId;
  @Getter
  @Setter
  private Collection<SimpleGrantedAuthority> authorities;

  @Override
  public String getUsername() {
    return email;
  }
}
